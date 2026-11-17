package net.zic.ascension.client.renderer;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.zic.ascension.AscensionCraft;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Matrix4fc;

import java.util.ArrayList;
import java.util.List;

/**
 * Qi coating geometry drawn after an Iris shader pack has finished the frame, so our own shaders (qi_coating.fsh, the skin
 * looks, sword_qi.fsh) draw it: a pack replaces every shader drawn inside its passes with its own programs, which can't draw
 * the looks. Without a pack, submitCustomGeometry submits as normal.
 * <p>
 * With a pack on it records each draw (its pose, render type and geometry) instead, and draws it later: the level's at
 * RenderLevelStageEvent.AfterLevel, past the pack's passes, depth-tested against the world the pack leaves in the main depth
 * buffer (as Divine Sense and Spherical Destruction read it); the first-person hand's once the level and the hand are done, which vanilla
 * draws over a cleared depth buffer (GameRendererAfterShaderPackMixin), under the view and projection it was submitted with
 * (Iris draws the hand with its own). Nothing is recorded in the pack's shadow pass, which renders the entities again from
 * the sun.
 */
@EventBusSubscriber(modid = AscensionCraft.MOD_ID, value = Dist.CLIENT)
public final class AfterShaderPackDraws {
    private static final List<Draw> LEVEL = new ArrayList<>();
    // One per time the first-person hands are submitted this frame (Iris submits them once per pass of its own).
    private static final List<HandPass> HAND = new ArrayList<>();
    // Set while the first-person hands are submitted (ItemInHandRendererAfterShaderPackMixin).
    private static boolean inHand;

    private AfterShaderPackDraws() {
    }

    /**
     * SubmitNodeCollector.submitCustomGeometry, or under a shader pack, the same geometry recorded to draw after it.
     */
    public static void submitCustomGeometry(SubmitNodeCollector submitNodeCollector, PoseStack poseStack, RenderType renderType,
                                            SubmitNodeCollector.CustomGeometryRenderer renderer) {
        if (!IrisPipelineCompat.isShaderPackInUse()) {
            submitNodeCollector.submitCustomGeometry(poseStack, renderType, renderer);
            return;
        }
        if (IrisPipelineCompat.isRenderingShadowPass()) {
            return;
        }
        (inHand && !HAND.isEmpty() ? HAND.getLast().draws() : LEVEL).add(new Draw(poseStack.last().copy(), renderType, renderer));
    }

    /**
     * For geometry drawn straight to the buffers in a level stage (RenderLevelStageEvent) rather than submitted: drawn now, or
     * under a shader pack, recorded to draw with the level's after it.
     */
    public static void drawOrDefer(PoseStack poseStack, RenderType renderType, SubmitNodeCollector.CustomGeometryRenderer renderer) {
        if (IrisPipelineCompat.isShaderPackInUse()) {
            LEVEL.add(new Draw(poseStack.last().copy(), renderType, renderer));
            return;
        }
        MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();
        renderer.render(poseStack.last(), buffers.getBuffer(renderType));
        buffers.endLastBatch();
    }

    public static void beginHand() {
        // The hand's own view and projection: vanilla's, or Iris's, which draws the hand itself with its own (HandRenderer).
        HAND.add(new HandPass(new Matrix4f(RenderSystem.getModelViewMatrix()), RenderSystem.getProjectionMatrixBuffer(),
                RenderSystem.getProjectionType(), new ArrayList<>()));
        inHand = true;
    }

    public static void endHand() {
        inHand = false;
    }

    /**
     * Drops what the last frame recorded but never drew, at the start of each frame.
     */
    public static void clear() {
        LEVEL.clear();
        HAND.clear();
        inHand = false;
    }

    @SubscribeEvent
    public static void onAfterLevel(RenderLevelStageEvent.AfterLevel event) {
        Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushMatrix().mul(event.getModelViewMatrix());
        draw(LEVEL);
        modelViewStack.popMatrix();
    }

    /**
     * The first-person hand's draws, each pass under the view and projection it was submitted with. The depth here doesn't
     * hold the pack's hand, so QiCoatingLayer.onRenderArm records the arm into it first (ModRenderTypes.qiCoatingArmDepth).
     */
    public static void drawHand() {
        Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
        for (HandPass pass : HAND) {
            if (pass.draws().isEmpty() || pass.projection() == null) {
                continue;
            }
            RenderSystem.backupProjectionMatrix();
            RenderSystem.setProjectionMatrix(pass.projection(), pass.projectionType());
            modelViewStack.pushMatrix().set(pass.modelView());
            draw(pass.draws());
            modelViewStack.popMatrix();
            RenderSystem.restoreProjectionMatrix();
        }
        HAND.clear();
    }

    private static void draw(List<Draw> draws) {
        if (draws.isEmpty()) {
            return;
        }
        MultiBufferSource.BufferSource buffers = Minecraft.getInstance().renderBuffers().bufferSource();
        for (Draw draw : draws) {
            draw.renderer().render(draw.pose(), buffers.getBuffer(draw.renderType()));
        }
        buffers.endBatch();
        draws.clear();
    }

    private record Draw(PoseStack.Pose pose, RenderType renderType, SubmitNodeCollector.CustomGeometryRenderer renderer) {
    }

    private record HandPass(Matrix4fc modelView, GpuBufferSlice projection, ProjectionType projectionType, List<Draw> draws) {
    }
}
