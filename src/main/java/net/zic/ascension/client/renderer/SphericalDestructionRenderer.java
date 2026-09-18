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

/**
 * Mirrors DivineSenseRenderer's shape exactly: same UBO layout (2 mat4 + 4
 * vec4), same MappableRingBuffer + FullscreenEffectPass call, just feeding
 * spherical_destruction_wave.fsh instead of divine_sense_wave.fsh.
 */
public enum SphericalDestructionRenderer {
    INSTANCE;

    private static final float TRAIL_WIDTH = 3.0F;
    private static final float FRONT_WIDTH = 0.6F;

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
        float[] color = HexColorCodec.toFloats(state.color());
        float progress = state.waveProgress();
        float radius = state.waveRadius();

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
                            radius
                    ))
                    .putVec4(new Vector4f(color[0], color[1], color[2], progress))
                    .putVec4(new Vector4f(state.elapsedSeconds(), TRAIL_WIDTH, FRONT_WIDTH, state.radius()));
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