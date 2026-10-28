package net.zic.ascension.client.renderer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.client.renderer.layer.QiCoatingGather;
import net.zic.ascension.client.renderer.layer.QiCoatingLayer;
import net.zic.ascension.client.visual.QiCoatingClientState;
import net.zic.ascension.impl.core.skill.castable.QiCoatingService;
import net.zic.ascension.api.ascension.core.qi_coating.QiCoating;
import net.zic.ascension.api.ascension.core.qi_coating.QiCoatingChannel;
import net.zic.ascension.api.ascension.core.qi_coating.QiCoatingDefinition;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

/**
 * Redraws held items with a coating's sword look (QiCoatingLooks, drawn by sword_qi.fsh). The item-in-hand mixins mark which
 * entity's item is being submitted, and ItemLayerSwordQiMixin hands each item layer's quads here before vanilla submits them.
 */
public final class SwordQiRenderer {
    // Sword qi that grows (its sword look takes SPREAD data) spreads from the hilt to the tip when it comes on and melts
    // back down to it when it goes off, taking QiCoating.growMillis each way. The shader gets how far along the spread is
    // in the vertex green channel, and how far along the item each pixel sits in the blue (frostColor).

    // Echo copies jump to a new offset every ECHO_STEP_MILLIS, up to ECHO_SPREAD (in item model units, 1 = 16px). Only
    // ECHO_TINT's alpha reaches the shader, which carries its colour itself, since the vertex red channel holds the mask.
    private static final int ECHO_COUNT = 2;
    private static final long ECHO_STEP_MILLIS = 90L;
    private static final float ECHO_SPREAD = 0.06F;
    private static final int ECHO_TINT = 0x80B98CFF;

    private static final byte MASK_PLAIN = 1;
    private static final byte MASK_COATED = 2;
    // Red channel carries the mask (coated or plain) to sword_qi.fsh.
    private static final int COATED_COLOR = 0xFFFFFFFF;
    private static final int PLAIN_COLOR = 0xFF00FFFF;
    // Steps per model pixel the pattern position (setPatternPixel) is stored to; sword_qi.vsh divides it back out.
    private static final float PATTERN_PRECISION = 64.0F;
    // Hand-painted masks: white where the coating goes, black where it doesn't, the same size as the texture. Weapons of one
    // shape share a mask, textures/sword_qi_mask/<mask>.png, listed under its name in assets/ascension/sword_qi_masks.json; a
    // texture can also have its own at textures/sword_qi_mask/<texture namespace>/<texture path>.png, which wins. Per sprite,
    // since sprites are replaced on resource reload and old entries drop out; NO_MASK marks a texture without one, which is
    // coated whole.
    private static final Identifier SHARED_MASKS = AscensionCraft.prefix("sword_qi_masks.json");
    private static final Map<TextureAtlasSprite, byte[]> PAINTED_MASKS = new WeakHashMap<>();
    private static final byte[] NO_MASK = new byte[0];

    // Render-thread only; set around each held-item submit, cleared right after.
    private static QiCoating current;
    private static QiCoatingLooks.SwordLook currentLook;
    // The look drawn while a coating that echoes on a trigger isn't echoing (Sword.quiet_look), or null for none.
    private static QiCoatingLooks.SwordLook currentQuietLook;
    private static int currentEntityId;
    // Sword qi that grows: how far the current entity's has climbed, 0 to 1, and its last hit (QiCoatingLayer.impactData).
    private static float currentFreeze;
    private static int currentImpact = QiCoatingLayer.NO_IMPACT;
    // The current coating's gather (QiCoatingDefinition.Gather), or null: while it grows, specks fly into the weapon and the look
    // forms from them, fading in or in patches round where they land (currentFreeze is how far it has grown).
    private static QiCoatingDefinition.Gather currentGather;

    private SwordQiRenderer() {
    }

    public static void begin(int entityId, ItemStack itemStack) {
        Minecraft minecraft = Minecraft.getInstance();
        Entity entity = minecraft.level == null ? null : minecraft.level.getEntity(entityId);
        QiCoating coating = entity == null ? null : QiCoatingClientState.get(entity.getUUID(), QiCoatingChannel.EXTERNAL);
        currentLook = coating == null || !coating.swordQi() || !carriesSwordQi(itemStack) ? null
                : coating.definition().sword().map(sword -> QiCoatingLooks.sword(sword.look())).orElse(null);
        current = currentLook == null ? null : coating;
        currentQuietLook = current == null ? null
                : coating.definition().sword().flatMap(QiCoatingDefinition.Sword::quietLook).map(QiCoatingLooks::sword).orElse(null);
        currentEntityId = entityId;
        currentFreeze = current != null && current.grows() ? QiCoatingClientState.growth(entity.getUUID(), current) : 1.0F;
        currentImpact = current != null && current.reacts() ? QiCoatingLayer.latestImpactData(current, entityId) : QiCoatingLayer.NO_IMPACT;
        currentGather = current != null && current.grows() ? current.definition().gather().orElse(null) : null;
    }

    /**
     * Sword Qi only shows on bladed and polearm weapons: swords, blades, spears and axes.
     */
    private static boolean carriesSwordQi(ItemStack itemStack) {
        return QiCoatingService.coatsWeapon(itemStack);
    }

    public static void end() {
        current = null;
        currentLook = null;
        currentQuietLook = null;
        currentGather = null;
    }

    /**
     * @return whether vanilla should still submit the item as normal
     */
    public static boolean submit(SubmitNodeCollector submitNodeCollector, PoseStack poseStack, List<BakedQuad> quads,
                                 ItemDisplayContext displayContext, int lightCoords, int overlayCoords, int outlineColor,
                                 int[] tintLayers, ItemStackRenderState.FoilType foilType) {
        QiCoating coating = current;
        QiCoatingLooks.SwordLook look = currentLook;
        if (coating == null || look == null || quads.isEmpty()) {
            return true;
        }
        // The render state reuses its quad list every frame, and custom geometry is drawn later.
        List<BakedQuad> snapshot = List.copyOf(quads);
        QiCoatingDefinition.Gather gather = currentGather;
        float growth = currentFreeze;
        if (IrisPipelineCompat.isShaderPackInUse()) {
            // A shader pack can't draw the sword_qi.fsh looks (IrisPipelineCompat), so the item is submitted as a normal item,
            // which Iris draws properly, its coated pixels tinted in the look's colour. An echoing coating shows its quiet look's
            // tint, or the plain item.
            QiCoatingLooks.SwordLook shown = coating.echoes() ? currentQuietLook : look;
            if (shown == null) {
                return true;
            }
            float[][] landings = gather != null && growth < 1.0F ? QiCoatingGather.itemLandings(gather, coatedPixels(snapshot)) : null;
            TintedItem tinted = shaderPackTintedItem(snapshot, tintLayers, shown, gather, landings, growth);
            submitNodeCollector.submitItem(poseStack, displayContext, lightCoords, overlayCoords, outlineColor, tinted.tintLayers(),
                    tinted.quads(), foilType);
            submitSpecks(submitNodeCollector, poseStack, snapshot, gather, landings, growth);
            return false;
        }
        if (coating.echoes()) {
            QiCoatingLooks.SwordLook quietLook = currentQuietLook;
            if (!QiCoatingLayer.echoing(coating, currentEntityId)) {
                // A coating that echoes on a trigger, between triggers: its quiet look in place of the item, or the plain item.
                if (quietLook == null) {
                    return true;
                }
                submitMasked(submitNodeCollector, poseStack, snapshot, quietLook, 0xFF, null, null, 1.0F);
                return false;
            }
            // See-through copies beside the real item, which vanilla still draws (or the quiet look in its place, if there is one).
            long step = Util.getMillis() / ECHO_STEP_MILLIS;
            for (int echo = 0; echo < ECHO_COUNT; echo++) {
                RandomSource random = RandomSource.create(step * 31L + echo * 7L + currentEntityId);
                poseStack.pushPose();
                poseStack.translate((random.nextFloat() - 0.5F) * 2.0F * ECHO_SPREAD,
                        (random.nextFloat() - 0.5F) * 2.0F * ECHO_SPREAD,
                        (random.nextFloat() - 0.5F) * 2.0F * ECHO_SPREAD);
                submitMasked(submitNodeCollector, poseStack, snapshot, look, ARGB.alpha(ECHO_TINT), null, null, 1.0F);
                poseStack.popPose();
            }
            if (quietLook == null) {
                return true;
            }
            submitMasked(submitNodeCollector, poseStack, snapshot, quietLook, 0xFF, null, null, 1.0F);
            return false;
        }
        // Every other look redraws the item opaque in place of vanilla's: a translucent overlay on top of the item vanished on
        // the front in first person, where the item drew over it afterwards.
        float[][] landings = gather != null && growth < 1.0F ? QiCoatingGather.itemLandings(gather, coatedPixels(snapshot)) : null;
        submitMasked(submitNodeCollector, poseStack, snapshot, look, 0xFF, gather, landings, growth);
        submitSpecks(submitNodeCollector, poseStack, snapshot, gather, landings, growth);
        return false;
    }

    /**
     * The gather's specks flying into the item, while it forms (landings set).
     */
    private static void submitSpecks(SubmitNodeCollector submitNodeCollector, PoseStack poseStack, List<BakedQuad> quads,
                                     QiCoatingDefinition.Gather gather, float[][] landings, float growth) {
        if (landings == null) {
            return;
        }
        float[] bounds = modelBounds(quads);
        QiCoatingGather.submitItemSpecks(poseStack, submitNodeCollector, gather, landings,
                (bounds[0] + bounds[2]) * 8.0F, (bounds[1] + bounds[3]) * 8.0F, growth);
    }

    // How far a coated pixel is tinted toward its look's colour under a shader pack (the rest of the way stays the item).
    private static final float SHADER_PACK_TINT_STRENGTH = 0.7F;

    /**
     * The item's quads for a shader pack, to submit as a normal item (SubmitNodeCollector.submitItem, which Iris draws properly
     * in the world and the first-person hand): split into pixel quads where the painted mask or the gather's patches call for
     * it, the coated pixels given a tint index of their own, the last of the returned tint layers, holding the look's colour
     * (faded by a FADE gather). Plain pixels keep their quad's own tint index and the item's tint layers.
     */
    private static TintedItem shaderPackTintedItem(List<BakedQuad> quads, int[] tintLayers, QiCoatingLooks.SwordLook look,
                                                   QiCoatingDefinition.Gather gather, float[][] landings, float growth) {
        boolean patches = landings != null && gather.style() == QiCoatingDefinition.GatherStyle.PATCHES;
        float amount = SHADER_PACK_TINT_STRENGTH
                * (gather != null && gather.style() == QiCoatingDefinition.GatherStyle.FADE ? QiCoatingGather.fadeAmount(growth) : 1.0F);
        int coatedTintIndex = tintLayers.length;
        int[] tints = java.util.Arrays.copyOf(tintLayers, coatedTintIndex + 1);
        tints[coatedTintIndex] = ARGB.srgbLerp(amount, 0xFFFFFFFF, QiCoatingLooks.swordShaderPackTint(look.style()));
        List<BakedQuad> out = new java.util.ArrayList<>();
        for (BakedQuad quad : quads) {
            TextureAtlasSprite sprite = quad.materialInfo().sprite();
            byte[] mask = paintedMask(sprite);
            if (mask == null && !patches) {
                out.add(withTintIndex(quad, coatedTintIndex));
                continue;
            }
            Vector3f origin = quadPoint(quad, 0.0F, 0.0F, new Vector3f());
            int cellsA = pixelSpan(sprite, origin, quadPoint(quad, 1.0F, 0.0F, new Vector3f()));
            int cellsB = pixelSpan(sprite, origin, quadPoint(quad, 0.0F, 1.0F, new Vector3f()));
            for (int i = 0; i < cellsA; i++) {
                for (int j = 0; j < cellsB; j++) {
                    float a0 = (float) i / cellsA;
                    float a1 = (float) (i + 1) / cellsA;
                    float b0 = (float) j / cellsB;
                    float b1 = (float) (j + 1) / cellsB;
                    Vector3f centre = quadPoint(quad, (a0 + a1) * 0.5F, (b0 + b1) * 0.5F, new Vector3f());
                    int pixel = pixelIndex(sprite, centre.y, centre.z);
                    if (cellsA * cellsB > 1 && ARGB.alpha(sprite.getPixelRGBA(0, pixel % sprite.contents().width(),
                            pixel / sprite.contents().width())) < 26) {
                        continue;
                    }
                    boolean coatedPixel = mask == null || mask[pixel] == MASK_COATED;
                    if (coatedPixel && patches) {
                        Vector3f middle = quadPosition(quad, (a0 + a1) * 0.5F, (b0 + b1) * 0.5F);
                        float x = middle.x * 16.0F;
                        float y = middle.y * 16.0F;
                        float z = middle.z * 16.0F;
                        coatedPixel = QiCoatingGather.revealed(landings, growth, x, y, z, QiCoatingGather.pixelJitter(x, y, z));
                    }
                    out.add(subQuad(quad, a0, a1, b0, b1, coatedPixel ? coatedTintIndex : quad.materialInfo().tintIndex()));
                }
            }
        }
        return new TintedItem(out, tints);
    }

    private record TintedItem(List<BakedQuad> quads, int[] tintLayers) {
    }

    /**
     * The part of a quad from a0 to a1 along its first edge and b0 to b1 along its second, with the given tint index.
     */
    private static BakedQuad subQuad(BakedQuad quad, float a0, float a1, float b0, float b1, int tintIndex) {
        float[][] corners = {{a0, b0}, {a1, b0}, {a1, b1}, {a0, b1}};
        Vector3f[] positions = new Vector3f[4];
        long[] uvs = new long[4];
        for (int k = 0; k < 4; k++) {
            positions[k] = quadPosition(quad, corners[k][0], corners[k][1]);
            Vector3f uv = quadPoint(quad, corners[k][0], corners[k][1], new Vector3f());
            uvs[k] = UVPair.pack(uv.y, uv.z);
        }
        return new BakedQuad(positions[0], positions[1], positions[2], positions[3], uvs[0], uvs[1], uvs[2], uvs[3],
                quad.direction(), withTintIndex(quad.materialInfo(), tintIndex));
    }

    private static BakedQuad withTintIndex(BakedQuad quad, int tintIndex) {
        return new BakedQuad(quad.position0(), quad.position1(), quad.position2(), quad.position3(), quad.packedUV0(), quad.packedUV1(),
                quad.packedUV2(), quad.packedUV3(), quad.direction(), withTintIndex(quad.materialInfo(), tintIndex),
                quad.bakedNormals(), quad.bakedColors());
    }

    private static BakedQuad.MaterialInfo withTintIndex(BakedQuad.MaterialInfo info, int tintIndex) {
        return new BakedQuad.MaterialInfo(info.sprite(), info.layer(), info.itemRenderType(), tintIndex, info.shade(),
                info.lightEmission(), info.ambientOcclusion());
    }

    /**
     * The centres of an item's coated, non see-through pixels, in model pixels (1/16 of a block), for its gather's landings.
     */
    private static List<float[]> coatedPixels(List<BakedQuad> quads) {
        List<float[]> pixels = new java.util.ArrayList<>();
        for (BakedQuad quad : quads) {
            TextureAtlasSprite sprite = quad.materialInfo().sprite();
            byte[] mask = paintedMask(sprite);
            Vector3f origin = quadPoint(quad, 0.0F, 0.0F, new Vector3f());
            int cellsA = pixelSpan(sprite, origin, quadPoint(quad, 1.0F, 0.0F, new Vector3f()));
            int cellsB = pixelSpan(sprite, origin, quadPoint(quad, 0.0F, 1.0F, new Vector3f()));
            if (cellsA * cellsB <= 1) {
                continue;
            }
            for (int i = 0; i < cellsA; i++) {
                for (int j = 0; j < cellsB; j++) {
                    float a = (i + 0.5F) / cellsA;
                    float b = (j + 0.5F) / cellsB;
                    Vector3f uv = quadPoint(quad, a, b, new Vector3f());
                    int pixel = pixelIndex(sprite, uv.y, uv.z);
                    int width = sprite.contents().width();
                    if (ARGB.alpha(sprite.getPixelRGBA(0, pixel % width, pixel / width)) < 26
                            || mask != null && mask[pixel] != MASK_COATED) {
                        continue;
                    }
                    Vector3f position = quadPosition(quad, a, b);
                    pixels.add(new float[]{position.x * 16.0F, position.y * 16.0F, position.z * 16.0F});
                }
            }
        }
        return pixels;
    }

    /**
     * Draws the item with a Sword Qi style on only the pixels its painted mask marks (see PAINTED_MASKS), each flagged in its
     * vertex colour's red channel as coated (1) or plain (0) for sword_qi.fsh. Every quad is split along its own texture
     * pixels, so this works for flat items and for 3D models drawn from a texture alike. Quads whose texture has no mask are
     * drawn whole and coated.
     *
     * While a gather forms the look (QiCoatingGather): a FADE one fades the coated pixels in, for looks that blend by the red
     * channel (PLAIN data); a PATCHES one draws the coated pixels not yet reached plain, splitting even unmasked quads into pixels
     * to do it. Looks with their own spread (SPREAD data) keep it and only get the specks.
     *
     * @param alpha    vertex alpha, 0-255
     * @param gather   the coating's gather, or null
     * @param landings where its specks land (QiCoatingGather.itemLandings), or null when it isn't forming
     * @param growth   how far the coating has grown, 0 to 1
     */
    private static void submitMasked(SubmitNodeCollector submitNodeCollector, PoseStack poseStack, List<BakedQuad> quads,
                                     QiCoatingLooks.SwordLook look, int alpha, QiCoatingDefinition.Gather gather,
                                     float[][] landings, float growth) {
        int style = look.style();
        Identifier atlas = quads.getFirst().materialInfo().sprite().atlasLocation();
        boolean frost = look.data() == QiCoatingLooks.SwordData.SPREAD;
        boolean gale = look.data() == QiCoatingLooks.SwordData.GALE;
        boolean fade = gather != null && gather.style() == QiCoatingDefinition.GatherStyle.FADE
                && look.data() == QiCoatingLooks.SwordData.PLAIN;
        boolean patches = landings != null && gather.style() == QiCoatingDefinition.GatherStyle.PATCHES && !frost;
        int coated = (alpha << 24) | (fade
                ? (Math.round(QiCoatingGather.fadeAmount(growth) * 255.0F) << 16) | (COATED_COLOR & 0xFFFF)
                : COATED_COLOR & 0xFFFFFF);
        int plain = (alpha << 24) | (PLAIN_COLOR & 0xFFFFFF);
        float freeze = currentFreeze;
        int impact = currentImpact;
        float[] bounds = modelBounds(quads);
        submitNodeCollector.submitCustomGeometry(poseStack, ModRenderTypes.swordQi(style, atlas), (pose, buffer) -> {
            for (BakedQuad quad : quads) {
                TextureAtlasSprite sprite = quad.materialInfo().sprite();
                byte[] mask = paintedMask(sprite);
                if (mask == null && !patches) {
                    for (int vertex = 0; vertex < BakedQuad.VERTEX_COUNT; vertex++) {
                        Vector3fc position = quad.position(vertex);
                        long uv = quad.packedUV(vertex);
                        float along = along(bounds, position.x(), position.y());
                        VertexConsumer consumer = buffer.addVertex(pose, position.x(), position.y(), position.z())
                                .setUv(UVPair.unpackU(uv), UVPair.unpackV(uv))
                                .setColor(frost ? frostColor(coated, freeze, along, impact)
                                        : gale ? galeColor(coated, along, across(bounds, position.x(), position.y())) : coated);
                        setPatternPixel(consumer, quad, position);
                    }
                    continue;
                }
                // How many texture pixels the quad spans along each of its edges.
                Vector3f origin = quadPoint(quad, 0.0F, 0.0F, new Vector3f());
                int cellsA = pixelSpan(sprite, origin, quadPoint(quad, 1.0F, 0.0F, new Vector3f()));
                int cellsB = pixelSpan(sprite, origin, quadPoint(quad, 0.0F, 1.0F, new Vector3f()));
                for (int i = 0; i < cellsA; i++) {
                    for (int j = 0; j < cellsB; j++) {
                        float a0 = (float) i / cellsA;
                        float a1 = (float) (i + 1) / cellsA;
                        float b0 = (float) j / cellsB;
                        float b1 = (float) (j + 1) / cellsB;
                        Vector3f centre = quadPoint(quad, (a0 + a1) * 0.5F, (b0 + b1) * 0.5F, new Vector3f());
                        int pixel = pixelIndex(sprite, centre.y, centre.z);
                        // A flat item's front and back faces cover its whole sprite; skip the see-through pixels. One-pixel
                        // quads (a flat item's edges) are kept as they are.
                        if (cellsA * cellsB > 1 && ARGB.alpha(sprite.getPixelRGBA(0, pixel % sprite.contents().width(),
                                pixel / sprite.contents().width())) < 26) {
                            continue;
                        }
                        boolean coatedPixel = mask == null || mask[pixel] == MASK_COATED;
                        Vector3f middle = quadPosition(quad, (a0 + a1) * 0.5F, (b0 + b1) * 0.5F);
                        if (coatedPixel && patches) {
                            float x = middle.x * 16.0F;
                            float y = middle.y * 16.0F;
                            float z = middle.z * 16.0F;
                            coatedPixel = QiCoatingGather.revealed(landings, growth, x, y, z, QiCoatingGather.pixelJitter(x, y, z));
                        }
                        int color = coatedPixel ? coated : plain;
                        if (frost || gale) {
                            float along = along(bounds, middle.x, middle.y);
                            color = frost ? frostColor(color, freeze, along, impact)
                                    : galeColor(color, along, across(bounds, middle.x, middle.y));
                        }
                        subVertex(pose, buffer, quad, a0, b0, color);
                        subVertex(pose, buffer, quad, a1, b0, color);
                        subVertex(pose, buffer, quad, a1, b1, color);
                        subVertex(pose, buffer, quad, a0, b1, color);
                    }
                }
            }
        });
    }

    /**
     * Texture pixels between two (unused, u, v) points from quadPoint, at least 1.
     */
    private static int pixelSpan(TextureAtlasSprite sprite, Vector3f from, Vector3f to) {
        float across = Math.abs(to.y - from.y) / (sprite.getU1() - sprite.getU0()) * sprite.contents().width();
        float down = Math.abs(to.z - from.z) / (sprite.getV1() - sprite.getV0()) * sprite.contents().height();
        return Math.max(1, Math.round(Math.max(across, down)));
    }

    private static byte[] paintedMask(TextureAtlasSprite sprite) {
        byte[] mask = PAINTED_MASKS.computeIfAbsent(sprite, SwordQiRenderer::loadPaintedMask);
        return mask == NO_MASK ? null : mask;
    }

    /**
     * Reads a sprite's painted mask, its own or else the shared one sword_qi_masks.json lists it under: white pixels (all three channels at least half, and mostly opaque) are coated, everything
     * else is plain. A mask that is missing or not the texture's size counts as none.
     */
    private static byte[] loadPaintedMask(TextureAtlasSprite sprite) {
        Identifier name = sprite.contents().name();
        Identifier location = AscensionCraft.prefix("textures/sword_qi_mask/" + name.getNamespace() + "/" + name.getPath() + ".png");
        Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(location);
        if (resource.isEmpty()) {
            String shared = sharedMaskName(name);
            if (shared == null) {
                return NO_MASK;
            }
            location = AscensionCraft.prefix("textures/sword_qi_mask/" + shared + ".png");
            resource = Minecraft.getInstance().getResourceManager().getResource(location);
        }
        if (resource.isEmpty()) {
            return NO_MASK;
        }
        int width = sprite.contents().width();
        int height = sprite.contents().height();
        try (InputStream stream = resource.get().open(); NativeImage image = NativeImage.read(stream)) {
            if (image.getWidth() != width || image.getHeight() != height) {
                AscensionCraft.LOGGER.warn("Sword qi mask {} is {}x{}, but its texture is {}x{}; ignoring it", location,
                        image.getWidth(), image.getHeight(), width, height);
                return NO_MASK;
            }
            byte[] mask = new byte[width * height];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int pixel = image.getPixel(x, y);
                    boolean white = ARGB.alpha(pixel) >= 128
                            && Math.min(ARGB.red(pixel), Math.min(ARGB.green(pixel), ARGB.blue(pixel))) >= 128;
                    mask[y * width + x] = white ? MASK_COATED : MASK_PLAIN;
                }
            }
            return mask;
        } catch (IOException e) {
            AscensionCraft.LOGGER.warn("Could not read sword qi mask {}", location, e);
            return NO_MASK;
        }
    }

    /**
     * The shared mask sword_qi_masks.json lists this texture under, or null. Read once per sprite, so once per weapon texture
     * after each resource reload.
     */
    private static String sharedMaskName(Identifier texture) {
        Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(SHARED_MASKS);
        if (resource.isEmpty()) {
            return null;
        }
        try (Reader reader = resource.get().openAsReader()) {
            JsonObject masks = JsonParser.parseReader(reader).getAsJsonObject().getAsJsonObject("masks");
            for (Map.Entry<String, JsonElement> mask : masks.entrySet()) {
                for (JsonElement listed : mask.getValue().getAsJsonArray()) {
                    if (texture.toString().equals(listed.getAsString())) {
                        return mask.getKey();
                    }
                }
            }
        } catch (IOException | RuntimeException e) {
            AscensionCraft.LOGGER.warn("Could not read {}", SHARED_MASKS, e);
        }
        return null;
    }

    /**
     * Point (a, b) across a quad by bilinear interpolation, packed as (unused, u, v) for UV queries.
     */
    private static Vector3f quadPoint(BakedQuad quad, float a, float b, Vector3f out) {
        long uv0 = quad.packedUV(0);
        long uv1 = quad.packedUV(1);
        long uv2 = quad.packedUV(2);
        long uv3 = quad.packedUV(3);
        float u = Mth.lerp(b, Mth.lerp(a, UVPair.unpackU(uv0), UVPair.unpackU(uv1)), Mth.lerp(a, UVPair.unpackU(uv3), UVPair.unpackU(uv2)));
        float v = Mth.lerp(b, Mth.lerp(a, UVPair.unpackV(uv0), UVPair.unpackV(uv1)), Mth.lerp(a, UVPair.unpackV(uv3), UVPair.unpackV(uv2)));
        return out.set(0.0F, u, v);
    }

    private static void subVertex(PoseStack.Pose pose, VertexConsumer buffer, BakedQuad quad, float a, float b, int color) {
        Vector3f position = quadPosition(quad, a, b);
        Vector3f uv = quadPoint(quad, a, b, new Vector3f());
        setPatternPixel(buffer.addVertex(pose, position.x, position.y, position.z).setUv(uv.y, uv.z).setColor(color), quad, position);
    }

    private static Vector3f quadPosition(BakedQuad quad, float a, float b) {
        Vector3f bottom = new Vector3f(quad.position(0)).lerp(quad.position(1), a);
        Vector3f top = new Vector3f(quad.position(3)).lerp(quad.position(2), a);
        return bottom.lerp(top, b);
    }

    /**
     * Where the vertex sits on the item's model, in model pixels (1/16 of a block) times PATTERN_PRECISION, in the vertex's UV2,
     * which sword_qi.vsh hands to sword_qi.fsh as the space every look's pattern is laid out in. It is measured on the face's
     * own plane: across and down for faces looking north or south (so a flat item's front matches its texture's pixels), and
     * likewise on the others. Laying patterns out on the model rather than the texture keeps them the same size on 3D weapon
     * models, whose faces can stretch a few texture pixels a long way.
     */
    private static void setPatternPixel(VertexConsumer consumer, BakedQuad quad, Vector3fc position) {
        float x = position.x() * 16.0F;
        float down = (1.0F - position.y()) * 16.0F;
        float z = position.z() * 16.0F;
        float u;
        float v;
        switch (quad.direction()) {
            case EAST, WEST -> {
                u = z;
                v = down;
            }
            case UP, DOWN -> {
                u = x;
                v = z;
            }
            default -> {
                u = x;
                v = down;
            }
        }
        consumer.setUv2(Math.round(u * PATTERN_PRECISION), Math.round(v * PATTERN_PRECISION));
    }

    /**
     * The item model's extent across (x) and up (y), as {minX, minY, maxX, maxY}, which along and across measure against.
     */
    private static float[] modelBounds(List<BakedQuad> quads) {
        float[] bounds = {Float.MAX_VALUE, Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE};
        for (BakedQuad quad : quads) {
            for (int vertex = 0; vertex < BakedQuad.VERTEX_COUNT; vertex++) {
                Vector3fc position = quad.position(vertex);
                bounds[0] = Math.min(bounds[0], position.x());
                bounds[1] = Math.min(bounds[1], position.y());
                bounds[2] = Math.max(bounds[2], position.x());
                bounds[3] = Math.max(bounds[3], position.y());
            }
        }
        return bounds;
    }

    /**
     * How far along the item a point sits, 0 at the handle's bottom left to 1 at the tip's top right of the model.
     */
    private static float along(float[] bounds, float x, float y) {
        return (share(bounds[0], bounds[2], x) + share(bounds[1], bounds[3], y)) * 0.5F;
    }

    /**
     * How far across the item a point sits, 0 at its top left to 1 at its bottom right (0.5 down its middle).
     */
    private static float across(float[] bounds, float x, float y) {
        return (share(bounds[0], bounds[2], x) + 1.0F - share(bounds[1], bounds[3], y)) * 0.5F;
    }

    private static float share(float min, float max, float value) {
        return max - min < 1.0E-4F ? 0.5F : Mth.clamp((value - min) / (max - min), 0.0F, 1.0F);
    }

    /**
     * A SPREAD sword look's vertex colour: the mask (coated or plain) in the red channel's top bit and the last hit's seed in its
     * low 7 bits, how far the coating has spread (0 to 1) in the green, how far along the item the vertex sits (0 at the handle's
     * bottom left, 1 at the tip's top right) in the blue, and how long ago the last hit was in the alpha. sword_qi.fsh draws these
     * looks opaque, so the alpha isn't needed for blending.
     *
     * @param impact the last hit, from QiCoatingLayer.impactData
     */
    private static int frostColor(int color, float freeze, float along, int impact) {
        int mask = ARGB.red(color) >= 128 ? 128 : 0;
        return ARGB.color(impact >> 8 & 0xFF, mask | (impact & 0x7F), Math.round(freeze * 255.0F),
                Math.round(Mth.clamp(along, 0.0F, 1.0F) * 255.0F));
    }

    /**
     * SWORD_GALE's vertex colour: the mask (coated or plain) in the red channel, how far across the item the pixel sits (0 at the
     * top left corner, 1 at the bottom right, so a blade's middle is near 0.5) in the green, and how far along it (as frostColor)
     * in the blue.
     */
    private static int galeColor(int color, float along, float across) {
        return ARGB.color(ARGB.alpha(color), ARGB.red(color) >= 128 ? 255 : 0, Math.round(Mth.clamp(across, 0.0F, 1.0F) * 255.0F),
                Math.round(Mth.clamp(along, 0.0F, 1.0F) * 255.0F));
    }

    private static int pixelIndex(TextureAtlasSprite sprite, float u, float v) {
        int width = sprite.contents().width();
        int height = sprite.contents().height();
        int x = Mth.clamp((int) ((u - sprite.getU0()) / (sprite.getU1() - sprite.getU0()) * width), 0, width - 1);
        int y = Mth.clamp((int) ((v - sprite.getV0()) / (sprite.getV1() - sprite.getV0()) * height), 0, height - 1);
        return y * width + x;
    }
}
