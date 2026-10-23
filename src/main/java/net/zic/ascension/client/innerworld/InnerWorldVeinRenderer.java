package net.zic.ascension.client.innerworld;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.client.renderer.ModRenderTypes;
import net.zic.ascension.client.visual.DivineSenseClientState;

/**
 * Outlines revealed treasure veins through walls, using the same line render type and vertex calls your runtime
 * visuals already use (linesNoDepth, addVertex/setColor/setNormal/setLineWidth). A vein lights up when the Divine Sense
 * wave reaches it and stays lit until that Divine Sense ends.
 *
 * Confidence note: the draw pattern is copied from DatapackRuntimeVisualController. The one addition is
 * endBatch(...) so the lines flush even if nothing else drew with that render type this frame; if you see double
 * drawing, remove that line.
 */
@EventBusSubscriber(modid = AscensionCraft.MOD_ID, value = Dist.CLIENT)
public final class InnerWorldVeinRenderer {
    private InnerWorldVeinRenderer() {
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        InnerWorldVeinClientState.clear();
    }

    @SubscribeEvent
    public static void onAfterTranslucent(RenderLevelStageEvent.AfterTranslucentFeatures event) {
        DivineSenseClientState state = DivineSenseClientState.get();
        if (!state.isActive() || InnerWorldVeinClientState.get().isEmpty()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }

        Vec3 camera = minecraft.gameRenderer.getMainCamera().position();
        PoseStack.Pose pose = event.getPoseStack().last();
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        VertexConsumer lines = buffers.getBuffer(ModRenderTypes.linesNoDepth());

        int color = state.color();
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;
        int alpha = (int) (170 + 85 * Math.sin(state.elapsedSeconds() * 5.0F));

        boolean drew = false;
        for (BlockPos pos : InnerWorldVeinClientState.get()) {
            Vec3 center = Vec3.atCenterOf(pos);
            if (!state.isWaveReaching(center)) {
                continue;
            }
            box(lines, pose, center.subtract(camera), 0.62, red, green, blue, alpha);
            drew = true;
        }
        if (drew) {
            buffers.endBatch(ModRenderTypes.linesNoDepth());
        }
    }

    private static void box(VertexConsumer lines, PoseStack.Pose pose, Vec3 c, double h, int r, int g, int b, int a) {
        double[] xs = {c.x - h, c.x + h};
        double[] ys = {c.y - h, c.y + h};
        double[] zs = {c.z - h, c.z + h};
        for (int y = 0; y < 2; y++) {
            for (int z = 0; z < 2; z++) {
                line(lines, pose, xs[0], ys[y], zs[z], xs[1], ys[y], zs[z], r, g, b, a);
            }
        }
        for (int x = 0; x < 2; x++) {
            for (int z = 0; z < 2; z++) {
                line(lines, pose, xs[x], ys[0], zs[z], xs[x], ys[1], zs[z], r, g, b, a);
            }
        }
        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 2; y++) {
                line(lines, pose, xs[x], ys[y], zs[0], xs[x], ys[y], zs[1], r, g, b, a);
            }
        }
    }

    private static void line(VertexConsumer lines, PoseStack.Pose pose, double x1, double y1, double z1,
                             double x2, double y2, double z2, int r, int g, int b, int a) {
        float nx = (float) (x2 - x1);
        float ny = (float) (y2 - y1);
        float nz = (float) (z2 - z1);
        float length = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
        if (length > 0.0F) {
            nx /= length;
            ny /= length;
            nz /= length;
        }
        lines.addVertex(pose, (float) x1, (float) y1, (float) z1).setColor(r, g, b, a).setNormal(pose, nx, ny, nz).setLineWidth(2.5F);
        lines.addVertex(pose, (float) x2, (float) y2, (float) z2).setColor(r, g, b, a).setNormal(pose, nx, ny, nz).setLineWidth(2.5F);
    }
}