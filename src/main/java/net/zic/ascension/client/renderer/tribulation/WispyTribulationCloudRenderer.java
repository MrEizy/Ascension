package net.zic.ascension.client.renderer.tribulation;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.zic.ascension.api.ascension.core.tribulation.TribulationCloudSettings;
import net.zic.ascension.api.ascension.core.tribulation.TribulationData;
import net.zic.ascension.client.renderer.ModRenderTypes;

import java.util.function.ToIntFunction;

/**
 * renders the storm as a vortex of wispy, see through streaks swirling into a glowing eye, instead of the solid pixel hurricane.
 * the flashes, formation and lightning work the same as in the hurricane.
 * <p>
 * the clouds are a few thin layers stacked above each other, each a smooth funnel that rises towards the eye.
 * the tribulation_wisps shader stretches noise along tight spirals into streaks and fades them out with alpha.
 * higher layers turn and drift slower, so the streaks slide past each other and the storm gets some depth
 */
public class WispyTribulationCloudRenderer<T extends TribulationData> extends TribulationCloudRenderer<T> {
    //must match WISP_LAYERS in the tribulation_wisps fragment shader
    private static final int LAYERS = 3;
    //how far each layer sits above the one below it, as a fraction of the radius
    private static final float LAYER_GAP = 0.012F;
    //how much higher the middle of the vortex is than its rim, as a fraction of the radius
    private static final float FUNNEL = 0.05F;
    //each layer up turns this much slower, as a fraction of the rotation speed
    private static final float LAYER_SPIN = 0.2F;
    //how fast the wisps slide into the eye, in log radius per tick. slower than the hurricane's billows
    private static final float WISP_INFLOW = 0.004F;
    //each layer up drifts inward this much slower
    private static final float LAYER_INFLOW = 0.25F;

    public WispyTribulationCloudRenderer(TribulationCloudSettings settings, ToIntFunction<T> strikeCounter, FormationProgress<T> formation) {
        super(settings, strikeCounter, formation);
    }

    //the funnel rises smoothly from the rim to the eye
    @Override
    protected float cloudBaseHeight(float rn) {
        float rise = 1.0F - Math.min(rn, 1.0F);
        return FUNNEL * settings.radius() * rise * rise;
    }

    @Override
    protected void renderClouds(MultiBufferSource.BufferSource buffers, PoseStack.Pose pose, CloudState state, Formation formation,
                                Vec3 camera, float time, float partialTick, int tint, float flash, float viewDistance) {
        float radius = settings.radius();
        int step = lodStep(state, (float) (radius / Math.max(camera.length(), 1.0D)));
        boolean lit = isLit(state, flash);
        //the layers blend over each other, so the furthest from the camera goes first.
        //from below that is the top one, from above the bottom one
        boolean fromBelow = camera.y < LAYER_GAP * radius * (LAYERS - 1) * 0.5F;
        for (int i = 0; i < LAYERS; i++) {
            int layer = fromBelow ? LAYERS - 1 - i : i;
            drawLayer(buffers, pose, state, layer, time, tint, flash, lit, step, (float) camera.x, (float) camera.z, viewDistance);
        }
    }

    private void drawLayer(MultiBufferSource.BufferSource buffers, PoseStack.Pose pose, CloudState state, int layer, float time,
                           int tint, float flash, boolean lit, int step, float cameraX, float cameraZ, float viewDistance) {
        VertexConsumer consumer = buffers.getBuffer(ModRenderTypes.tribulationWisps());
        float radius = settings.radius();
        float lift = layer * LAYER_GAP * radius;
        //higher layers lag behind, and each starts somewhere else around the circle so they do not line up
        float angle = state.angle * (1.0F - LAYER_SPIN * layer);
        float spiralShift = state.spiralOffset + layer * 0.37F - angle / Mth.TWO_PI;
        int unlitColor = ARGB.color(0, tint);

        for (int ring = 0; ring < RINGS - 1; ring += step) {
            int nextRing = Math.min(ring + step, RINGS - 1);
            //the middle of this ring's quads, and how far a corner can be from it, in blocks
            float middle = (ringRn[ring] + ringRn[nextRing]) * 0.5F * radius;
            float reach = (ringRn[nextRing] - ringRn[ring]) * radius + Mth.TWO_PI * middle * step / SEGMENTS;
            float limit = viewDistance + reach;
            for (int segment = 0; segment < SEGMENTS; segment += step) {
                int nextSegment = segment + step;
                //past the far end of the view, nothing of it would be drawn anyway
                float dx = segmentCos[segment] * middle - cameraX;
                float dz = segmentSin[segment] * middle - cameraZ;
                if (dx * dx + dz * dz > limit * limit) continue;

                vertex(consumer, pose, state, ring, segment, radius, lift, spiralShift, tint, lit, unlitColor, flash);
                vertex(consumer, pose, state, nextRing, segment, radius, lift, spiralShift, tint, lit, unlitColor, flash);
                vertex(consumer, pose, state, nextRing, nextSegment, radius, lift, spiralShift, tint, lit, unlitColor, flash);
                vertex(consumer, pose, state, ring, nextSegment, radius, lift, spiralShift, tint, lit, unlitColor, flash);
            }
        }
        //read through the texture matrix when the batch is drawn, so they have to be set before ending it
        ModRenderTypes.tribulationCloudLayer = layer;
        ModRenderTypes.tribulationCloudFlow = time * WISP_INFLOW * (1.0F - LAYER_INFLOW * layer) + state.flowOffset;
        buffers.endLastBatch();
        ModRenderTypes.tribulationCloudLayer = 0.0F;
    }

    private void vertex(VertexConsumer consumer, PoseStack.Pose pose, CloudState state, int ring, int segment, float radius,
                        float lift, float spiralShift, int tint, boolean lit, int unlitColor, float flash) {
        float rn = ringRn[ring];
        float x = segmentCos[segment] * rn * radius;
        float y = cloudBaseHeight(rn) + lift;
        float z = segmentSin[segment] * rn * radius;
        //turns around the circle, bent into a spiral by the log radius
        float spiral = segmentTurns[segment] + ringTwist[ring] + spiralShift;
        //rgb is the tint, alpha is how strongly lightning lights up this part of the cloud
        int color = lit ? ARGB.color(Math.round(glow(state, rn, x, y, z, flash) * 255.0F), tint) : unlitColor;
        consumer.addVertex(pose, x, y, z)
                .setUv(spiral, ringLog[ring])
                .setColor(color);
    }
}
