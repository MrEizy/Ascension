package net.zic.ascension.client.renderer.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.zic.ascension.client.renderer.AfterShaderPackDraws;
import net.zic.ascension.client.renderer.ModRenderTypes;

/**
 * The Tornado Fist's wind rings (QiCoatingEffects.WIND_RINGS), round each arm of the coating.
 */
final class WindRingsEffect implements QiCoatingEffect {
    // The rings start WIND_RING_TIP_OVERHANG past the hand tip; sizes are in pixels. The bottom ring is WIND_RING_ALPHA opaque
    // and each one above WIND_RING_ALPHA_FALLOFF fainter.
    private static final int WIND_RING_COUNT = 5;
    private static final float WIND_RING_TIP_OVERHANG = 0.3F;
    private static final float WIND_RING_OUTSET = 0.5F;
    private static final float WIND_RING_HEIGHT = 0.9F;
    private static final float WIND_RING_GAP = 0.5F;
    private static final float WIND_RING_ALPHA = 0.9F;
    private static final float WIND_RING_ALPHA_FALLOFF = 0.12F;
    // The qi_coating.fsh style that shades the wind rings.
    private static final int WIND_RING_STYLE = 32;
    // How far around the ring each one's streak pattern starts, so stacked rings don't repeat each other.
    private static final float RING_PATTERN_SHIFT = 0.37F;

    /**
     * WIND_RING_COUNT rectangular rings stacked from the hand tip toward the elbow, each a band of four upright faces. Expects the
     * pose stack to already be in the arm's local space. u runs around each ring and v up its band.
     */
    private static void submitWindRings(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, HumanoidArm side, boolean slim,
                                        float alpha) {
        float styleOffset = WIND_RING_STYLE * 2.0F;
        float centerX = QiCoatingLayer.armCenterX(side, slim);
        float halfX = QiCoatingLayer.armWidth(slim) * 0.5F + WIND_RING_OUTSET;
        float halfZ = (QiCoatingLayer.ARM_MAX_Z - QiCoatingLayer.ARM_MIN_Z) * 0.5F + WIND_RING_OUTSET;
        float x0 = (centerX - halfX) / 16.0F;
        float x1 = (centerX + halfX) / 16.0F;
        float z0 = -halfZ / 16.0F;
        float z1 = halfZ / 16.0F;
        float[] cornerX = {x0, x1, x1, x0};
        float[] cornerZ = {z0, z0, z1, z1};
        AfterShaderPackDraws.submitCustomGeometry(submitNodeCollector, poseStack, ModRenderTypes.qiCoating(), (pose, buffer) -> {
            for (int ring = 0; ring < WIND_RING_COUNT; ring++) {
                float bottom = QiCoatingLayer.HAND_TIP_Y + WIND_RING_TIP_OVERHANG - ring * (WIND_RING_HEIGHT + WIND_RING_GAP);
                float top = bottom - WIND_RING_HEIGHT;
                float shift = Mth.frac(ring * RING_PATTERN_SHIFT);
                int color = ARGB.white(alpha * WIND_RING_ALPHA * (1.0F - ring * WIND_RING_ALPHA_FALLOFF));
                for (int face = 0; face < 4; face++) {
                    int next = (face + 1) % 4;
                    ShellGeometry.side(pose, buffer, cornerX[face], cornerZ[face], cornerX[next], cornerZ[next],
                            styleOffset + shift + face * 0.25F, styleOffset + shift + (face + 1) * 0.25F, top / 16.0F, bottom / 16.0F, color);
                }
            }
        });
    }

    @Override
    public void submitArm(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, Arm arm) {
        // The rings aren't made of pixels, so they fade in with a fade or patches gather.
        submitWindRings(poseStack, submitNodeCollector, arm.side(), arm.slim(), arm.fadeIn());
    }
}
