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
    }

    /**
     * @return whether vanilla should still submit the item as normal
     */
    public static boolean submit(SubmitNodeCollector submitNodeCollector, PoseStack poseStack, List<BakedQuad> quads) {
        QiCoating coating = current;
        QiCoatingLooks.SwordLook look = currentLook;
        if (coating == null || look == null || quads.isEmpty()) {
            return true;
        }
        // The render state reuses its quad list every frame, and custom geometry is drawn later.
        List<BakedQuad> snapshot = List.copyOf(quads);
        if (coating.echoes()) {
            QiCoatingLooks.SwordLook quietLook = currentQuietLook;
            if (!QiCoatingLayer.echoing(coating, currentEntityId)) {
                // A coating that echoes on a trigger, between triggers: its quiet look in place of the item, or the plain item.
                if (quietLook == null) {
                    return true;
                }
                submitMasked(submitNodeCollector, poseStack, snapshot, quietLook, 0xFF);
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
                submitMasked(submitNodeCollector, poseStack, snapshot, look, ARGB.alpha(ECHO_TINT));
                poseStack.popPose();
            }
            if (quietLook == null) {
                return true;
            }
            submitMasked(submitNodeCollector, poseStack, snapshot, quietLook, 0xFF);
            return false;
        }
        // Every other look redraws the item opaque in place of vanilla's: a translucent overlay on top of the item vanished on
        // the front in first person, where the item drew over it afterwards.
        submitMasked(submitNodeCollector, poseStack, snapshot, look, 0xFF);
        return false;
    }

    /**
     * Draws the item with a Sword Qi style on only the pixels its painted mask marks (see PAINTED_MASKS), each flagged in its
     * vertex colour's red channel as coated (1) or plain (0) for sword_qi.fsh. Every quad is split along its own texture
     * pixels, so this works for flat items and for 3D models drawn from a texture alike. Quads whose texture has no mask are
     * drawn whole and coated.
     *
     * @param alpha vertex alpha, 0-255
     */
    private static void submitMasked(SubmitNodeCollector submitNodeCollector, PoseStack poseStack, List<BakedQuad> quads,
                                     QiCoatingLooks.SwordLook look, int alpha) {
        int style = look.style();
        Identifier atlas = quads.getFirst().materialInfo().sprite().atlasLocation();
        int coated = (alpha << 24) | (COATED_COLOR & 0xFFFFFF);
        int plain = (alpha << 24) | (PLAIN_COLOR & 0xFFFFFF);
        boolean frost = look.data() == QiCoatingLooks.SwordData.SPREAD;
        boolean gale = look.data() == QiCoatingLooks.SwordData.GALE;
        float freeze = currentFreeze;
        int impact = currentImpact;
        submitNodeCollector.submitCustomGeometry(poseStack, ModRenderTypes.swordQi(style, atlas), (pose, buffer) -> {
            for (BakedQuad quad : quads) {
                TextureAtlasSprite sprite = quad.materialInfo().sprite();
                byte[] mask = paintedMask(sprite);
                if (mask == null) {
                    for (int vertex = 0; vertex < BakedQuad.VERTEX_COUNT; vertex++) {
                        Vector3fc position = quad.position(vertex);
                        long uv = quad.packedUV(vertex);
                        float u = UVPair.unpackU(uv);
                        float v = UVPair.unpackV(uv);
                        float uFrac = (u - sprite.getU0()) / (sprite.getU1() - sprite.getU0());
                        float vFrac = (v - sprite.getV0()) / (sprite.getV1() - sprite.getV0());
                        float along = (uFrac + 1.0F - vFrac) * 0.5F;
                        buffer.addVertex(pose, position.x(), position.y(), position.z())
                                .setUv(u, v)
                                .setColor(frost ? frostColor(coated, freeze, along, impact)
                                        : gale ? galeColor(coated, along, (uFrac + vFrac) * 0.5F) : coated);
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
                        int color = mask[pixel] == MASK_COATED ? coated : plain;
                        if (frost) {
                            int width = sprite.contents().width();
                            int height = sprite.contents().height();
                            float along = (float) (pixel % width + height - 1 - pixel / width) / Math.max(1, width + height - 2);
                            color = frostColor(color, freeze, along, impact);
                        } else if (gale) {
                            int width = sprite.contents().width();
                            int height = sprite.contents().height();
                            int span = Math.max(1, width + height - 2);
                            color = galeColor(color, (float) (pixel % width + height - 1 - pixel / width) / span,
                                    (float) (pixel % width + pixel / width) / span);
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
        Vector3f bottom = new Vector3f(quad.position(0)).lerp(quad.position(1), a);
        Vector3f top = new Vector3f(quad.position(3)).lerp(quad.position(2), a);
        Vector3f position = bottom.lerp(top, b);
        Vector3f uv = quadPoint(quad, a, b, new Vector3f());
        buffer.addVertex(pose, position.x, position.y, position.z).setUv(uv.y, uv.z).setColor(color);
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
