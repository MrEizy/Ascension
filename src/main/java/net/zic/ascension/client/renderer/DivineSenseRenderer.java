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
import net.zic.ascension.client.visual.DivineSenseClientState;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public enum DivineSenseRenderer {
    INSTANCE;

    private final MappableRingBuffer waveUniform = new MappableRingBuffer(
            () -> "Divine Sense Wave UBO",
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
        DivineSenseClientState state = DivineSenseClientState.get();
        if (!state.isWaveActive()) {
            return;
        }

        Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().position();
        Vec3 center = state.center();
        float[] color = HexColorCodec.toFloats(state.color());
        float progress = state.waveProgress();
        float radius = state.waveRadius();
        float trailWidth = Math.clamp(1.8F + state.radius() * 0.025F, 2.0F, 4.25F);
        float frontWidth = Math.clamp(0.32F + state.radius() * 0.0025F, 0.36F, 0.52F);

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
                    .putVec4(new Vector4f(state.elapsedSeconds(), trailWidth, frontWidth, 0.0F));
        }

        FullscreenEffectPass.drawDepthEffect(
                encoder,
                ModRenderPipelines.DIVINE_SENSE_WAVE,
                ModRenderPipelines.DIVINE_SENSE_UNIFORM,
                waveUniform.currentBuffer(),
                ModRenderPipelines.WORLD_DEPTH_SAMPLER
        );
    }
}
