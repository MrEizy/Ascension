package net.zic.ascension.client.renderer;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.world.phys.Vec3;
import net.zic.ascension.api.ascension.value.HexColorCodec;
import net.zic.ascension.client.visual.DivineSenseClientState;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.OptionalInt;


public enum DivineSenseRenderer {
    INSTANCE;

    private final MappableRingBuffer effectUbo = new MappableRingBuffer(
            () -> "Divine Sense Effect UBO",
            GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_MAP_WRITE,
            new Std140SizeCalculator()
                    .putMat4f()
                    .putMat4f()
                    .putVec3()
                    .putVec3()
                    .putFloat()
                    .putFloat()
                    .putVec3()
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

        float radius = state.waveRadius();
        Matrix4f invView = new Matrix4f(viewMatrix).invert();
        Matrix4f invProj = new Matrix4f(projectionMatrix).invert();
        Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().position();
        float[] rgb = HexColorCodec.toFloats(state.color());

        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();

        effectUbo.rotate();
        try (GpuBuffer.MappedView view = encoder.mapBuffer(effectUbo.currentBuffer(), false, true)) {
            Std140Builder.intoBuffer(view.data())
                    .putMat4f(invView)
                    .putMat4f(invProj)
                    .putVec3(new Vector3f((float) cameraPos.x, (float) cameraPos.y, (float) cameraPos.z))
                    .putVec3(state.center().toVector3f())
                    .putFloat(radius)
                    .putFloat(RenderSystem.getDevice().isZZeroToOne() ? 1.0F : 0.0F)
                    .putVec3(new Vector3f(rgb[0], rgb[1], rgb[2]));
        }

        BufferBuilder quadBuilder = new BufferBuilder(
                new ByteBufferBuilder(256), VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX
        );
        quadBuilder.addVertex(-1.0F, -1.0F, 0.0F).setUv(0.0F, 0.0F);
        quadBuilder.addVertex(1.0F, -1.0F, 0.0F).setUv(1.0F, 0.0F);
        quadBuilder.addVertex(1.0F, 1.0F, 0.0F).setUv(1.0F, 1.0F);
        quadBuilder.addVertex(-1.0F, 1.0F, 0.0F).setUv(0.0F, 1.0F);
        MeshData quadMesh = quadBuilder.build();
        if (quadMesh == null) {
            return;
        }

        RenderTarget mainTarget = Minecraft.getInstance().getMainRenderTarget();

        try (GpuBuffer quadVertexBuffer = RenderSystem.getDevice().createBuffer(
                () -> "Divine Sense Wave Quad", GpuBuffer.USAGE_VERTEX, quadMesh.vertexBuffer()
        )) {
            try (RenderPass pass = encoder.createRenderPass(
                    () -> "Divine Sense Wave",
                    mainTarget.getColorTextureView(),
                    OptionalInt.empty()
            )) {
                pass.setPipeline(DivineSensePipelines.DIVINE_SENSE_EFFECT);
                pass.bindTexture("DepthSampler", mainTarget.getDepthTextureView(), RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST));
                pass.setUniform("DivineSenseEffectUniform", effectUbo.currentBuffer());

                RenderSystem.AutoStorageIndexBuffer indices = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
                pass.setVertexBuffer(0, quadVertexBuffer);
                pass.setIndexBuffer(indices.getBuffer(6), indices.type());
                pass.drawIndexed(0, 0, 6, 1);
            }
        }
    }
}
