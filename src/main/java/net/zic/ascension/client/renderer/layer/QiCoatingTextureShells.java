package net.zic.ascension.client.renderer.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Util;
import net.minecraft.world.entity.HumanoidArm;
import net.zic.ascension.api.ascension.core.qi_coating.QiCoatingDefinition;
import net.zic.ascension.client.renderer.IrisPipelineCompat;
import net.zic.ascension.client.renderer.ModRenderTypes;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Texture shells (QiCoatingDefinition.Texture): instead of a shader look, the boxes round the body are painted with an animated
 * texture laid out like a player skin. Each box is a copy of the player model's part, inflated, so it takes its pixels from that
 * part's skin area. A box is cut off where its reach ends (and while the coating grows or shrinks, where it has grown to), with
 * the texture cut along with it, so a hands coating shows the hand end of the arm's area.
 */
final class QiCoatingTextureShells {
    // Every box with this reach or more covers its whole part.
    private static final float WHOLE_PART = 64.0F;
    // Slack for vertices that undoing the transform leaves a hair off where they were, in pixels.
    private static final float EPSILON = 1.0E-3F;
    private static final Map<PartKey, ModelPart> PARTS = new HashMap<>();

    private QiCoatingTextureShells() {
    }

    /**
     * A body part, with the end its boxes start from (the hand, foot, waist or neck) and the end they reach toward, in its
     * local pixels (from PlayerModel.createMesh).
     */
    private enum Part {
        HEAD("head", "hat", 0.0F, -8.0F),
        BODY("body", "jacket", 12.0F, 0.0F),
        RIGHT_ARM("right_arm", "right_sleeve", 10.0F, -2.0F),
        LEFT_ARM("left_arm", "left_sleeve", 10.0F, -2.0F),
        RIGHT_LEG("right_leg", "right_pants", 12.0F, 0.0F),
        LEFT_LEG("left_leg", "left_pants", 12.0F, 0.0F);

        private final String name;
        private final String outerName;
        private final float start;
        private final float end;

        Part(String name, String outerName, float start, float end) {
            this.name = name;
            this.outerName = outerName;
            this.start = start;
            this.end = end;
        }
    }

    /**
     * A coating's layers round one arm. Expects the pose stack to already be in the arm's local space.
     *
     * @param growth how far the coating has grown, 0 to 1
     */
    static void submitArm(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, QiCoatingDefinition.Texture texture,
                          List<QiCoatingDefinition.Layer> layers, HumanoidArm side, boolean slim, int lightCoords, float growth,
                          QiCoatingDefinition.Gather gather) {
        if (growth <= 0.0F) {
            return;
        }
        Part part = side == HumanoidArm.RIGHT ? Part.RIGHT_ARM : Part.LEFT_ARM;
        int frame = frame(texture);
        // A gathering coating (QiCoatingGather) keeps its full reach and shows only round where its specks have landed, or fades in.
        boolean patches = gather != null && gather.style() == QiCoatingDefinition.GatherStyle.PATCHES;
        boolean fade = gather != null && gather.style() == QiCoatingDefinition.GatherStyle.FADE;
        float[][] landings = patches ? QiCoatingGather.landings(gather, side, slim, maxReach(layers)) : null;
        submitNodeCollector.submitCustomGeometry(poseStack, renderType(texture), (pose, buffer) -> {
            for (QiCoatingDefinition.Layer layer : layers) {
                // The inflated box's hand end already sits `inflate` past the hand, so it is moved out by the rest of the
                // overhang only, ending where a shader shell's does.
                float reach = patches || fade ? layer.reach() : layer.reach() * growth;
                int color = fade ? QiCoatingGather.fade(layer.color(), growth) : layer.color();
                draw(pose, buffer, texture, frame, part, slim, layer.inflate(), layer.outer(), reach,
                        layer.overhang() - layer.inflate(), color, lightCoords, landings, growth);
            }
        });
    }

    static float maxReach(List<QiCoatingDefinition.Layer> layers) {
        float reach = 0.0F;
        for (QiCoatingDefinition.Layer layer : layers) {
            reach = Math.max(reach, layer.reach());
        }
        return reach;
    }

    /**
     * A body coating's boxes in third person, as its Shells.body says: every part (film; just up to limb_reach on the limbs, and
     * without the head and torso unless torso_and_head) or each limb (limbs; or just the legs).
     */
    static void submitBody(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, PlayerModel model,
                           QiCoatingDefinition.Texture texture, QiCoatingDefinition.Body body, boolean slim, int lightCoords,
                           float growth) {
        if (growth <= 0.0F) {
            return;
        }
        boolean film = body.mode() == QiCoatingDefinition.BodyMode.FILM;
        float limbReach = film ? body.limbReach().orElse(WHOLE_PART) : WHOLE_PART;
        int frame = frame(texture);
        RenderType renderType = renderType(texture);
        poseStack.pushPose();
        model.root().translateAndRotate(poseStack);
        if (film && body.torsoAndHead()) {
            submitPart(poseStack, submitNodeCollector, renderType, model.head, texture, frame, Part.HEAD, slim, body, WHOLE_PART * growth, lightCoords);
            submitPart(poseStack, submitNodeCollector, renderType, model.body, texture, frame, Part.BODY, slim, body, WHOLE_PART * growth, lightCoords);
        }
        if (film || !body.legsOnly()) {
            submitPart(poseStack, submitNodeCollector, renderType, model.rightArm, texture, frame, Part.RIGHT_ARM, slim, body, limbReach * growth, lightCoords);
            submitPart(poseStack, submitNodeCollector, renderType, model.leftArm, texture, frame, Part.LEFT_ARM, slim, body, limbReach * growth, lightCoords);
        }
        submitPart(poseStack, submitNodeCollector, renderType, model.rightLeg, texture, frame, Part.RIGHT_LEG, slim, body, limbReach * growth, lightCoords);
        submitPart(poseStack, submitNodeCollector, renderType, model.leftLeg, texture, frame, Part.LEFT_LEG, slim, body, limbReach * growth, lightCoords);
        poseStack.popPose();
    }

    private static void submitPart(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, RenderType renderType, ModelPart posed,
                                   QiCoatingDefinition.Texture texture, int frame, Part part, boolean slim, QiCoatingDefinition.Body body,
                                   float reach, int lightCoords) {
        if (!posed.visible) {
            return;
        }
        poseStack.pushPose();
        posed.translateAndRotate(poseStack);
        submitNodeCollector.submitCustomGeometry(poseStack, renderType, (pose, buffer) ->
                draw(pose, buffer, texture, frame, part, slim, body.inflate(), body.outer(), reach, 0.0F, body.color(), lightCoords,
                        null, 1.0F));
        poseStack.popPose();
    }

    private static void draw(PoseStack.Pose pose, VertexConsumer buffer, QiCoatingDefinition.Texture texture, int frame, Part part,
                             boolean slim, float inflate, boolean outer, float reach, float overhang, int color, int lightCoords,
                             float[][] landings, float growth) {
        PoseStack partPose = new PoseStack();
        partPose.last().set(pose);
        box(slim, part, inflate, outer).render(partPose, new CutConsumer(buffer, pose.pose(), part, reach, overhang, frame, texture.frames(),
                        slim ? slimArmArea(part, outer) : null, landings, growth),
                lightCoords, OverlayTexture.NO_OVERLAY, color);
    }

    private static int frame(QiCoatingDefinition.Texture texture) {
        return (int) (Util.getMillis() / texture.frameMillis() % texture.frames());
    }

    static RenderType renderType(QiCoatingDefinition.Texture texture) {
        // Both write depth, so clouds and other translucent things drawn later don't show through.
        boolean shaderPack = IrisPipelineCompat.isShaderPackInUse();
        if (!texture.emissive(shaderPack)) {
            return RenderTypes.entityTranslucent(texture.path());
        }
        // Under a shader pack, vanilla's own emissive entity type: Iris binds its samplers in every pass, where our pipeline's
        // overlay sampler went missing in third person ("Missing sampler Sampler1").
        return shaderPack ? RenderTypes.entityTranslucentEmissive(texture.path()) : ModRenderTypes.qiCoatingTextureEmissive(texture.path());
    }

    /**
     * A private copy of one of the player model's parts, inflated, with just its inner box or just its outer layer box, baked at
     * the origin. Never re-posed, so safe to draw deferred.
     */
    private static ModelPart box(boolean slim, Part part, float inflate, boolean outer) {
        return PARTS.computeIfAbsent(new PartKey(slim, part, inflate, outer), key -> {
            ModelPart root = LayerDefinition.create(PlayerModel.createMesh(new CubeDeformation(key.inflate()), key.slim()), 64, 64)
                    .bakeRoot();
            ModelPart base = root.getChild(key.part().name);
            base.setInitialPose(PartPose.ZERO);
            base.resetPose();
            ModelPart outerBox = base.getChild(key.part().outerName);
            if (key.outer()) {
                return outerBox;
            }
            outerBox.visible = false;
            return base;
        });
    }

    private record PartKey(boolean slim, Part part, float inflate, boolean outer) {
    }

    /**
     * The top left corner of a slim arm's skin area (its box or its sleeve), in pixels, or null for any other part. Slim arms
     * are 3 wide, so their faces would read the wide arm's area shifted; CutConsumer stretches them onto the wide one instead,
     * so one texture fits both.
     */
    private static float[] slimArmArea(Part part, boolean outer) {
        return switch (part) {
            case RIGHT_ARM -> outer ? new float[]{40.0F, 32.0F} : new float[]{40.0F, 16.0F};
            case LEFT_ARM -> outer ? new float[]{48.0F, 48.0F} : new float[]{32.0F, 48.0F};
            default -> null;
        };
    }

    // A slim arm's u breakpoints from its area's left edge, and where they land on the wide arm's area: the end faces' row
    // (hand end and shoulder end), and the sides' row below it (outer, front, inner, back).
    private static final float[] SLIM_ENDS = {4.0F, 7.0F, 10.0F};
    private static final float[] WIDE_ENDS = {4.0F, 8.0F, 12.0F};
    private static final float[] SLIM_SIDES = {0.0F, 4.0F, 7.0F, 11.0F, 14.0F};
    private static final float[] WIDE_SIDES = {0.0F, 4.0F, 8.0F, 12.0F, 16.0F};

    private static float widen(float u, float[] slim, float[] wide) {
        for (int i = 1; i < slim.length; i++) {
            if (u <= slim[i] || i == slim.length - 1) {
                return wide[i - 1] + (u - slim[i - 1]) * (wide[i] - wide[i - 1]) / (slim[i] - slim[i - 1]);
            }
        }
        return u;
    }

    /**
     * Cuts a part's box off `reach` pixels from its start end (Part.start), stretches its start end `overhang` further out, and
     * picks the frame out of the stacked texture. Model parts send each face as four vertices, already transformed; the faces
     * along the part are cut where the reach ends, with their texture cut to match, and the far end's face is left off once it
     * is cut.
     */
    private static final class CutConsumer extends DelegatingVertexConsumer {
        private final Matrix4f partToWorld;
        private final Matrix4f worldToPart;
        private final Vector3f scratch = new Vector3f();
        private final float start;
        private final float direction;
        private final float reach;
        private final float overhang;
        private final int frame;
        private final int frames;
        private final float[][] quad = new float[4][11];
        private final float[] along = new float[4];
        private final float[] slimArea;
        // A PATCHES gather's landings (QiCoatingGather): when set, faces are split into skin pixels and only those near a landed
        // speck are drawn.
        private final float[][] landings;
        private final float growth;
        private final float[][] out = new float[4][5];
        private int count;

        private CutConsumer(VertexConsumer delegate, Matrix4fc partToWorld, Part part, float reach, float overhang, int frame, int frames,
                            float[] slimArea, float[][] landings, float growth) {
            super(delegate);
            this.slimArea = slimArea;
            this.landings = landings;
            this.growth = growth;
            this.partToWorld = new Matrix4f(partToWorld);
            this.worldToPart = new Matrix4f(partToWorld).invert();
            this.start = part.start;
            this.direction = Math.signum(part.end - part.start);
            this.reach = reach;
            this.overhang = overhang;
            this.frame = frame;
            this.frames = frames;
        }

        @Override
        public void addVertex(float x, float y, float z, int color, float u, float v, int overlayCoords, int lightCoords, float nx, float ny, float nz) {
            Vector3f local = worldToPart.transformPosition(x, y, z, scratch).mul(16.0F);
            float[] vertex = quad[count];
            vertex[0] = local.x;
            vertex[1] = local.y;
            vertex[2] = local.z;
            vertex[3] = Float.intBitsToFloat(color);
            vertex[4] = u;
            vertex[5] = v;
            vertex[6] = Float.intBitsToFloat(overlayCoords);
            vertex[7] = Float.intBitsToFloat(lightCoords);
            vertex[8] = nx;
            vertex[9] = ny;
            vertex[10] = nz;
            // How far along the part from its start end, in pixels.
            along[count] = direction * (local.y - start);
            if (++count == 4) {
                count = 0;
                emitQuad();
            }
        }

        private void emitQuad() {
            float near = Math.min(Math.min(along[0], along[1]), Math.min(along[2], along[3]));
            float far = Math.max(Math.max(along[0], along[1]), Math.max(along[2], along[3]));
            if (near >= reach) {
                // The far end's face once the box is cut short, or a face wholly past the cut.
                return;
            }
            // A face along the part has the same v at each end, so the cut's v is found between them. The ends are told apart
            // by which side of the middle a vertex is, as undoing the transform leaves them a hair off.
            float middle = (near + far) * 0.5F;
            // The start end's own face lies across the part, its corners all at the same distance give or take that hair, so
            // it is moved out by the overhang whole rather than corner by corner.
            boolean startFace = far - near <= 0.01F && near <= EPSILON;
            float nearV = 0.0F;
            float farV = 0.0F;
            // Whether the face is in the end faces' row of a slim arm's area, told by all its corners being above the sides' row.
            boolean endsRow = slimArea != null;
            for (int i = 0; endsRow && i < 4; i++) {
                endsRow = quad[i][5] * 64.0F <= slimArea[1] + 4.0F + EPSILON;
            }
            for (int i = 0; i < 4; i++) {
                if (along[i] <= middle) {
                    nearV = quad[i][5];
                } else {
                    farV = quad[i][5];
                }
            }
            for (int i = 0; i < 4; i++) {
                float[] vertex = quad[i];
                float distance = along[i];
                float v = vertex[5];
                if (distance > middle && distance > reach) {
                    v = nearV + (farV - nearV) * (reach - near) / (far - near);
                    distance = reach;
                } else if (startFace || (distance <= middle && near <= EPSILON)) {
                    distance -= overhang;
                }
                float u = vertex[4];
                if (slimArea != null) {
                    float fromEdge = u * 64.0F - slimArea[0];
                    u = (slimArea[0] + (endsRow ? widen(fromEdge, SLIM_ENDS, WIDE_ENDS) : widen(fromEdge, SLIM_SIDES, WIDE_SIDES))) / 64.0F;
                }
                float[] corner = out[i];
                corner[0] = vertex[0];
                corner[1] = start + direction * distance;
                corner[2] = vertex[2];
                corner[3] = u;
                corner[4] = v;
            }
            if (landings == null) {
                for (int i = 0; i < 4; i++) {
                    emit(out[i][0], out[i][1], out[i][2], out[i][3], out[i][4]);
                }
                return;
            }
            // One cell per skin pixel the face covers, s from corner 0 to 1 and t from corner 0 to 3.
            int cellsS = Math.max(1, Math.round((Math.abs(out[1][3] - out[0][3]) + Math.abs(out[1][4] - out[0][4])) * 64.0F));
            int cellsT = Math.max(1, Math.round((Math.abs(out[3][3] - out[0][3]) + Math.abs(out[3][4] - out[0][4])) * 64.0F));
            float[] centre = new float[5];
            for (int a = 0; a < cellsS; a++) {
                for (int b = 0; b < cellsT; b++) {
                    float s0 = (float) a / cellsS;
                    float s1 = (float) (a + 1) / cellsS;
                    float t0 = (float) b / cellsT;
                    float t1 = (float) (b + 1) / cellsT;
                    lerp((s0 + s1) * 0.5F, (t0 + t1) * 0.5F, centre);
                    if (!QiCoatingGather.revealed(landings, growth, centre[0], centre[1], centre[2],
                            QiCoatingGather.pixelJitter(centre[0], centre[1], centre[2]))) {
                        continue;
                    }
                    emitAt(s0, t0, centre);
                    emitAt(s1, t0, centre);
                    emitAt(s1, t1, centre);
                    emitAt(s0, t1, centre);
                }
            }
        }

        private void lerp(float s, float t, float[] result) {
            for (int k = 0; k < 5; k++) {
                float near = out[0][k] + (out[1][k] - out[0][k]) * s;
                float far = out[3][k] + (out[2][k] - out[3][k]) * s;
                result[k] = near + (far - near) * t;
            }
        }

        private void emitAt(float s, float t, float[] scratchCorner) {
            lerp(s, t, scratchCorner);
            emit(scratchCorner[0], scratchCorner[1], scratchCorner[2], scratchCorner[3], scratchCorner[4]);
        }

        private void emit(float x, float y, float z, float u, float v) {
            float[] vertex = quad[0];
            Vector3f world = partToWorld.transformPosition(x / 16.0F, y / 16.0F, z / 16.0F, scratch);
            delegate.addVertex(world.x, world.y, world.z, Float.floatToRawIntBits(vertex[3]), u, (v + frame) / frames,
                    Float.floatToRawIntBits(vertex[6]), Float.floatToRawIntBits(vertex[7]), vertex[8], vertex[9], vertex[10]);
        }

    }
}
