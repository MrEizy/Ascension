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
import org.joml.Matrix4f;
import org.joml.Vector4f;

public enum SphericalDestructionRenderer {
    INSTANCE;

    private static final float TRAIL_WIDTH = 4.5F;
    private static final float FRONT_WIDTH = 0.72F;

    private final MappableRingBuffer waveUniform = new MappableRingBuffer(
            () -> "Spherical Destruction Wave UBO",
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

    public static void renderWave(Matrix4f viewMatrix, Matrix4f projectionMatrix) {
        INSTANCE.render(viewMatrix, projectionMatrix);
    }

    private void render(Matrix4f viewMatrix, Matrix4f projectionMatrix) {
        SphericalDestructionClientState state = SphericalDestructionClientState.get();
        if (!state.isWaveActive()) {
            return;
        }

        Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().position();
        Vec3 center = state.center();
        Vec3 direction = state.direction();
        float[] color = HexColorCodec.toFloats(state.color());

        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();

        waveUniform.rotate();
        try (GpuBuffer.MappedView view = encoder.mapBuffer(waveUniform.currentBuffer(), false, true)) {
            Std140Builder.intoBuffer(view.data())
                    .putMat4f(new Matrix4f(viewMatrix).invert())
                    .putMat4f(new Matrix4f(projectionMatrix).invert())
                    .putVec4(new Vector4f(
                            (float) camera.x,
                            (float) camera.y,
                            (float) camera.z,
                            RenderSystem.getDevice().isZZeroToOne() ? 1.0F : 0.0F
                    ))
                    .putVec4(new Vector4f(
                            (float) center.x,
                            (float) center.y,
                            (float) center.z,
                            state.waveRadius()
                    ))
                    .putVec4(new Vector4f(color[0], color[1], color[2], state.waveProgress()))
                    .putVec4(new Vector4f(
                            state.shaderTimeSeconds(),
                            TRAIL_WIDTH,
                            FRONT_WIDTH,
                            state.isImpactPhase() ? 1.0F : 0.0F
                    ))
                    .putVec4(new Vector4f(
                            (float) direction.x,
                            (float) direction.y,
                            (float) direction.z,
                            state.trailLength()
                    ));
        }

        FullscreenEffectPass.drawDepthEffect(
                encoder,
                ModRenderPipelines.SPHERICAL_DESTRUCTION_WAVE,
                ModRenderPipelines.SPHERICAL_DESTRUCTION_UNIFORM,
                waveUniform.currentBuffer(),
                ModRenderPipelines.WORLD_DEPTH_SAMPLER
        );
    }
}
