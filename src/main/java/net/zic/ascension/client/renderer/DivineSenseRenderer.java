package net.zic.ascension.client.renderer;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.world.phys.Vec3;
import net.zic.ascension.api.ascension.value.HexColorCodec;
import net.zic.ascension.client.visual.DivineSenseClientState;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.OptionalDouble;
import java.util.OptionalInt;

public enum DivineSenseRenderer {
    INSTANCE;

    private final MappableRingBuffer effectUbo = new MappableRingBuffer(
            () -> "Divine Sense Effect UBO",
            GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_MAP_WRITE,
            new Std140SizeCalculator()
                    .putMat4f()
                    .putMat4f()
                    .putVec4()
                    .putVec4()
                    .putVec4()
                    .get()
    );

    public static void renderWave(Matrix4f viewMatrix, Matrix4f projectionMatrix) {
        INSTANCE.doRenderWave(viewMatrix, projectionMatrix);
    }

    private void doRenderWave(Matrix4f viewMatrix, Matrix4f projectionMatrix) {
        DivineSenseClientState state = DivineSenseClientState.get();
        if (!state.isWaveActive()) {
            return;
        }

        RenderTarget mainTarget = Minecraft.getInstance().getMainRenderTarget();
        GpuTextureView depth = mainTarget.getDepthTextureView();
        if (depth == null) {
            return;
        }

        Matrix4f invView = new Matrix4f(viewMatrix).invert();
        Matrix4f invProj = new Matrix4f(projectionMatrix).invert();
        Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().position();
        Vec3 center = state.center();
        float[] rgb = HexColorCodec.toFloats(state.color());

        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();

        effectUbo.rotate();
        try (GpuBuffer.MappedView view = encoder.mapBuffer(effectUbo.currentBuffer(), false, true)) {
            Std140Builder.intoBuffer(view.data())
                    .putMat4f(invView)
                    .putMat4f(invProj)
                    .putVec4(new Vector4f((float) cameraPos.x, (float) cameraPos.y, (float) cameraPos.z, 0.0F))
                    .putVec4(new Vector4f((float) center.x, (float) center.y, (float) center.z, state.waveRadius()))
                    .putVec4(new Vector4f(rgb[0], rgb[1], rgb[2], RenderSystem.getDevice().isZZeroToOne() ? 1.0F : 0.0F));
        }

        try (RenderPass pass = encoder.createRenderPass(
                () -> "Divine Sense Wave",
                mainTarget.getColorTextureView(),
                OptionalInt.empty(),
                null,
                OptionalDouble.empty()
        )) {
            pass.setPipeline(DivineSensePipelines.DIVINE_SENSE_EFFECT);
            RenderSystem.bindDefaultUniforms(pass);
            pass.setUniform(DivineSensePipelines.EFFECT_UNIFORM, effectUbo.currentBuffer());
            pass.bindTexture(
                    DivineSensePipelines.DEPTH_SAMPLER,
                    depth,
                    RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST)
            );
            pass.draw(0, 3);
        }
    }
}
