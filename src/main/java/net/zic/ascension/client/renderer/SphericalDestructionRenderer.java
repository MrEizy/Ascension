package net.zic.ascension.client.renderer;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.world.phys.Vec3;
import net.zic.ascension.api.ascension.value.HexColorCodec;
import net.zic.ascension.client.visual.SphericalDestructionClientState;
import org.joml.FrustumIntersection;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.Comparator;
import java.util.List;

public enum SphericalDestructionRenderer {
    INSTANCE;

    private static final float TRAIL_WIDTH = 4.5F;
    private static final float FRONT_WIDTH = 0.72F;
    private static final int MAX_RENDERED_SPHERES = 12;
    private static final double MAX_VIEW_DISTANCE = 384.0D;

    private final MappableRingBuffer[] waveUniforms = new MappableRingBuffer[MAX_RENDERED_SPHERES];

    public static void renderWave(Matrix4f viewMatrix, Matrix4f projectionMatrix) {
        INSTANCE.render(viewMatrix, projectionMatrix);
    }

    private void render(Matrix4f viewMatrix, Matrix4f projectionMatrix) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            SphericalDestructionClientState.get().clear();
            return;
        }
        List<SphericalDestructionClientState.Snapshot> snapshots = SphericalDestructionClientState.get()
                .snapshots(minecraft.level.dimension().identifier());
        if (snapshots.isEmpty()) {
            return;
        }

        Vec3 camera = minecraft.gameRenderer.getMainCamera().position();
        FrustumIntersection frustum = new FrustumIntersection(new Matrix4f(projectionMatrix).mul(new Matrix4f(viewMatrix).setTranslation(0.0F, 0.0F, 0.0F)));
        snapshots.removeIf(snapshot -> {
            Vec3 center = snapshot.center();
            float bound = snapshot.impact() ? Math.max(snapshot.radius() * 1.45F + 4.0F, snapshot.impactCoreRadius() * 2.1F + 2.0F) : snapshot.radius() * 1.85F + snapshot.trailLength();
            double maxDistance = MAX_VIEW_DISTANCE + bound;
            return camera.distanceToSqr(center) > maxDistance * maxDistance || !frustum.testSphere((float) (center.x - camera.x), (float) (center.y - camera.y), (float) (center.z - camera.z), bound);
        });
        snapshots.sort(Comparator.comparingDouble(snapshot -> camera.distanceToSqr(snapshot.center())));
        if (snapshots.isEmpty()) {
            return;
        }

        int visibleCount = Math.min(snapshots.size(), MAX_RENDERED_SPHERES);
        snapshots.subList(0, visibleCount).sort(
                Comparator.comparingDouble((SphericalDestructionClientState.Snapshot snapshot) ->
                        camera.distanceToSqr(snapshot.center())).reversed()
        );

        Matrix4f inverseView = new Matrix4f(viewMatrix).invert();
        Matrix4f inverseProjection = new Matrix4f(projectionMatrix).invert();
        float depthMode = RenderSystem.getDevice().isZZeroToOne() ? 1.0F : 0.0F;
        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();

        for (int index = 0; index < visibleCount; index++) {
            SphericalDestructionClientState.Snapshot snapshot = snapshots.get(index);
            final int slot = index;
            MappableRingBuffer buffer = waveUniforms[index];
            if (buffer == null) {
                buffer = new MappableRingBuffer(
                        () -> "Spherical Destruction Wave UBO " + slot,
                        GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_MAP_WRITE,
                        new Std140SizeCalculator()
                                .putMat4f()
                                .putMat4f()
                                .putVec4()
                                .putVec4()
                                .putVec4()
                                .putVec4()
                                .putVec4()
                                .get()
                );
                waveUniforms[index] = buffer;
            }
            Vec3 center = snapshot.center();
            Vec3 direction = snapshot.direction();
            float[] color = HexColorCodec.toFloats(snapshot.color());

            buffer.rotate();
            try (GpuBuffer.MappedView mapped = encoder.mapBuffer(buffer.currentBuffer(), false, true)) {
                Std140Builder.intoBuffer(mapped.data())
                        .putMat4f(inverseView)
                        .putMat4f(inverseProjection)
                        .putVec4(new Vector4f((float) camera.x, (float) camera.y, (float) camera.z, depthMode))
                        .putVec4(new Vector4f((float) center.x, (float) center.y, (float) center.z, snapshot.radius()))
                        .putVec4(new Vector4f(color[0], color[1], color[2], snapshot.progress()))
                        .putVec4(new Vector4f(snapshot.animationTime(), TRAIL_WIDTH, FRONT_WIDTH, snapshot.impact() ? 1.0F : 0.0F))
                        .putVec4(new Vector4f((float) direction.x, (float) direction.y, (float) direction.z, snapshot.impact() ? snapshot.impactCoreRadius() : snapshot.trailLength()));
            }
            FullscreenEffectPass.drawDepthEffect(
                    encoder,
                    ModRenderPipelines.SPHERICAL_DESTRUCTION_WAVE,
                    ModRenderPipelines.SPHERICAL_DESTRUCTION_UNIFORM,
                    buffer.currentBuffer(),
                    ModRenderPipelines.WORLD_DEPTH_SAMPLER
            );
        }
    }
}
