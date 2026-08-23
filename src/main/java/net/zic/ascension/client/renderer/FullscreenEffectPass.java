package net.zic.ascension.client.renderer;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;

import java.util.OptionalDouble;
import java.util.OptionalInt;

public final class FullscreenEffectPass {

    private static GpuBuffer vertices;

    private FullscreenEffectPass() {
    }

    public static void drawDepthEffect(
            CommandEncoder encoder,
            RenderPipeline pipeline,
            String uniformName,
            GpuBuffer uniformBuffer,
            String depthSampler
    ) {
        RenderTarget target = Minecraft.getInstance().getMainRenderTarget();
        GpuTextureView depth = target.getDepthTextureView();
        if (depth == null) {
            return;
        }

        GpuBuffer vertexBuffer = vertexBuffer();
        if (vertexBuffer == null) {
            return;
        }

        try (RenderPass pass = encoder.createRenderPass(
                () -> "Ascension Fullscreen Effect",
                target.getColorTextureView(),
                OptionalInt.empty(),
                null,
                OptionalDouble.empty()
        )) {
            pass.setPipeline(pipeline);
            RenderSystem.bindDefaultUniforms(pass);
            pass.setUniform(uniformName, uniformBuffer);
            pass.bindTexture(
                    depthSampler,
                    depth,
                    RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST)
            );
            pass.setVertexBuffer(0, vertexBuffer);
            pass.draw(0, 6);
        }
    }

    private static GpuBuffer vertexBuffer() {
        if (vertices != null && !vertices.isClosed()) {
            return vertices;
        }

        BufferBuilder builder = new BufferBuilder(
                new ByteBufferBuilder(256),
                VertexFormat.Mode.TRIANGLES,
                DefaultVertexFormat.POSITION
        );
        builder.addVertex(-1.0F, -1.0F, 0.0F);
        builder.addVertex(1.0F, -1.0F, 0.0F);
        builder.addVertex(1.0F, 1.0F, 0.0F);
        builder.addVertex(-1.0F, -1.0F, 0.0F);
        builder.addVertex(1.0F, 1.0F, 0.0F);
        builder.addVertex(-1.0F, 1.0F, 0.0F);

        MeshData mesh = builder.build();
        if (mesh == null) {
            return null;
        }

        vertices = RenderSystem.getDevice().createBuffer(
                () -> "Ascension Fullscreen Effect Vertices",
                GpuBuffer.USAGE_VERTEX,
                mesh.vertexBuffer()
        );
        return vertices;
    }
}
