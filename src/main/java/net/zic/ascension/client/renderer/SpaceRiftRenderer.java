package net.zic.ascension.client.renderer;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.client.renderer.blockentity.AbstractEndPortalRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.world.phys.Vec3;
import net.zic.ascension.client.visual.SpaceRiftClientState;
import org.joml.FrustumIntersection;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.Comparator;
import java.util.List;

public enum SpaceRiftRenderer {
    INSTANCE;

    private static final int MAX_RENDERED_RIFTS = 8;
    private static final double MAX_VIEW_DISTANCE = 256.0D;
    // Must cover the furthest radial crack in space_rift.fsh (local radius 1.75).
    private static final float BOUND_SCALE = 1.8F;

    private final MappableRingBuffer[] riftUniforms = new MappableRingBuffer[MAX_RENDERED_RIFTS];
    private GpuTexture sceneCopy;
    private GpuTextureView sceneCopyView;

    public static void renderRifts(Matrix4f viewMatrix, Matrix4f projectionMatrix) {
        INSTANCE.render(viewMatrix, projectionMatrix);
    }

    private void render(Matrix4f viewMatrix, Matrix4f projectionMatrix) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            SpaceRiftClientState.get().clear();
            return;
        }
        List<SpaceRiftClientState.Snapshot> snapshots = SpaceRiftClientState.get()
                .snapshots(minecraft.level.dimension().identifier());
        if (snapshots.isEmpty()) {
            return;
        }

        Vec3 camera = minecraft.gameRenderer.getMainCamera().position();
        FrustumIntersection frustum = new FrustumIntersection(new Matrix4f(projectionMatrix).mul(new Matrix4f(viewMatrix).setTranslation(0.0F, 0.0F, 0.0F)));
        snapshots.removeIf(snapshot -> {
            Vec3 center = snapshot.center();
            float bound = snapshot.radius() * BOUND_SCALE;
            double maxDistance = MAX_VIEW_DISTANCE + bound;
            return camera.distanceToSqr(center) > maxDistance * maxDistance || !frustum.testSphere((float) (center.x - camera.x), (float) (center.y - camera.y), (float) (center.z - camera.z), bound);
        });
        if (snapshots.isEmpty()) {
            return;
        }
        snapshots.sort(Comparator.comparingDouble(snapshot -> camera.distanceToSqr(snapshot.center())));
        int visibleCount = Math.min(snapshots.size(), MAX_RENDERED_RIFTS);
        snapshots.subList(0, visibleCount).sort(
                Comparator.comparingDouble((SpaceRiftClientState.Snapshot snapshot) ->
                        camera.distanceToSqr(snapshot.center())).reversed()
        );

        // Same textures, sampler and clock as the vanilla End portal so the void matches it exactly.
        TextureManager textures = minecraft.getTextureManager();
        GpuSampler repeat = RenderSystem.getSamplerCache().getRepeat(FilterMode.NEAREST);
        FullscreenEffectPass.SamplerBinding endSky = new FullscreenEffectPass.SamplerBinding(
                ModRenderPipelines.END_SKY_SAMPLER,
                textures.getTexture(AbstractEndPortalRenderer.END_SKY_LOCATION).getTextureView(), repeat);
        FullscreenEffectPass.SamplerBinding endPortal = new FullscreenEffectPass.SamplerBinding(
                ModRenderPipelines.END_PORTAL_SAMPLER,
                textures.getTexture(AbstractEndPortalRenderer.END_PORTAL_LOCATION).getTextureView(), repeat);
        float portalTime = ((float) (minecraft.level.getGameTime() % 24000L)
                + minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false)) / 24000.0F;

        Matrix4f inverseView = new Matrix4f(viewMatrix).invert();
        Matrix4f inverseProjection = new Matrix4f(projectionMatrix).invert();
        // Camera-relative view-projection, used to warp the scene during the collapse.
        Matrix4f viewProjection = new Matrix4f(projectionMatrix).mul(new Matrix4f(viewMatrix).setTranslation(0.0F, 0.0F, 0.0F));
        float depthMode = RenderSystem.getDevice().isZZeroToOne() ? 1.0F : 0.0F;
        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
        GpuTexture mainColor = minecraft.getMainRenderTarget().getColorTexture();
        if (mainColor == null) {
            return;
        }
        ensureSceneCopy(mainColor);
        FullscreenEffectPass.SamplerBinding sceneColor = new FullscreenEffectPass.SamplerBinding(
                ModRenderPipelines.SCENE_COLOR_SAMPLER, sceneCopyView,
                RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR));

        for (int index = 0; index < visibleCount; index++) {
            SpaceRiftClientState.Snapshot snapshot = snapshots.get(index);
            final int slot = index;
            MappableRingBuffer buffer = riftUniforms[index];
            if (buffer == null) {
                buffer = new MappableRingBuffer(
                        () -> "Space Rift UBO " + slot,
                        GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_MAP_WRITE,
                        new Std140SizeCalculator()
                                .putMat4f()
                                .putMat4f()
                                .putMat4f()
                                .putVec4()
                                .putVec4()
                                .putVec4()
                                .putVec4()
                                .putVec4()
                                .putVec4()
                                .get()
                );
                riftUniforms[index] = buffer;
            }
            Vec3 center = snapshot.center();
            Vec3 right = snapshot.right();
            Vec3 up = snapshot.up();
            boolean distorts = snapshot.distorts();
            if (distorts) {
                // Snapshot what is on screen so far; the shader samples it to drag space inward.
                encoder.copyTextureToTexture(mainColor, sceneCopy, 0, 0, 0, 0, 0, mainColor.getWidth(0), mainColor.getHeight(0));
            }

            buffer.rotate();
            try (GpuBuffer.MappedView mapped = encoder.mapBuffer(buffer.currentBuffer(), false, true)) {
                Std140Builder.intoBuffer(mapped.data())
                        .putMat4f(inverseView)
                        .putMat4f(inverseProjection)
                        .putMat4f(viewProjection)
                        .putVec4(new Vector4f((float) camera.x, (float) camera.y, (float) camera.z, depthMode))
                        .putVec4(new Vector4f((float) center.x, (float) center.y, (float) center.z, snapshot.radius()))
                        .putVec4(new Vector4f((float) right.x, (float) right.y, (float) right.z, snapshot.time()))
                        .putVec4(new Vector4f((float) up.x, (float) up.y, (float) up.z, snapshot.seed()))
                        .putVec4(new Vector4f(snapshot.crackPhase(), snapshot.openPhase(), snapshot.fade(), portalTime))
                        .putVec4(new Vector4f(snapshot.pinch(), snapshot.coreRadius(), snapshot.glow(), distorts ? 1.0F : 0.0F));
            }
            FullscreenEffectPass.drawDepthEffect(
                    encoder,
                    ModRenderPipelines.SPACE_RIFT,
                    ModRenderPipelines.SPACE_RIFT_UNIFORM,
                    buffer.currentBuffer(),
                    ModRenderPipelines.WORLD_DEPTH_SAMPLER,
                    endSky,
                    endPortal,
                    sceneColor
            );
        }
    }

    private void ensureSceneCopy(GpuTexture mainColor) {
        int width = mainColor.getWidth(0);
        int height = mainColor.getHeight(0);
        if (sceneCopy != null && !sceneCopy.isClosed()
                && sceneCopy.getWidth(0) == width && sceneCopy.getHeight(0) == height) {
            return;
        }
        if (sceneCopyView != null) {
            sceneCopyView.close();
        }
        if (sceneCopy != null) {
            sceneCopy.close();
        }
        GpuDevice device = RenderSystem.getDevice();
        sceneCopy = device.createTexture(() -> "Space Rift Scene Copy",
                GpuTexture.USAGE_COPY_DST | GpuTexture.USAGE_TEXTURE_BINDING,
                mainColor.getFormat(), width, height, 1, 1);
        sceneCopyView = device.createTextureView(sceneCopy);
    }
}
