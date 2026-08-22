package net.zic.ascension.client.renderer;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.zic.ascension.api.ascension.value.HexColorCodec;
import net.zic.ascension.client.visual.DivineSenseClientState;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.OptionalInt;


public enum DivineSenseRenderer {
    INSTANCE;

    private static final float GROWTH_DURATION_MS = 600.0F;

    private final MappableRingBuffer effectUbo = new MappableRingBuffer(
            () -> "Divine Sense Effect UBO",
            GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_MAP_WRITE,
            new Std140SizeCalculator()
                    .putMat4f()
                    .putMat4f()
                    .putVec3()
                    .putVec3()
                    .putFloat()
                    .putVec3()
                    .get()
    );

    private final MappableRingBuffer resultUbo = new MappableRingBuffer(
            () -> "Divine Sense Result UBO",
            GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_MAP_WRITE,
            new Std140SizeCalculator()
                    .putMat4f()
                    .putMat4f()
                    .putFloat()
                    .putVec3()
                    .get()
    );

    public static void renderWave(Matrix4f viewMatrix, Matrix4f projectionMatrix) {
        INSTANCE.doRenderWave(viewMatrix, projectionMatrix);
    }

    public static void renderMarkers(PoseStack poseStack, Matrix4f projectionMatrix, float partialTick) {
        INSTANCE.doRenderMarkers(poseStack, projectionMatrix, partialTick);
    }

    private void doRenderWave(Matrix4f viewMatrix, Matrix4f projectionMatrix) {
        DivineSenseClientState state = DivineSenseClientState.get();
        if (!state.isActive()) {
            return;
        }

        float progress = Math.min(1.0F, (System.currentTimeMillis() - state.startTimeMs()) / GROWTH_DURATION_MS);
        float radius = state.radius() * progress;
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
                pass.bindTexture("DepthSampler", mainTarget.getDepthTextureView(), null);
                pass.setUniform("DivineSenseEffectUniform", effectUbo.currentBuffer());

                RenderSystem.AutoStorageIndexBuffer indices = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
                pass.setVertexBuffer(0, quadVertexBuffer);
                pass.setIndexBuffer(indices.getBuffer(6), indices.type());
                pass.drawIndexed(0, 0, 6, 1);
            }
        }
    }

    private void doRenderMarkers(PoseStack poseStack, Matrix4f projectionMatrix, float partialTick) {
        DivineSenseClientState state = DivineSenseClientState.get();
        Level level = Minecraft.getInstance().level;
        if (!state.isActive() || level == null) {
            return;
        }

        float[] rgb = HexColorCodec.toFloats(state.color());
        float time = (System.currentTimeMillis() - state.startTimeMs()) / 1000.0F;

        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
        resultUbo.rotate();
        try (GpuBuffer.MappedView view = encoder.mapBuffer(resultUbo.currentBuffer(), false, true)) {
            Std140Builder.intoBuffer(view.data())
                    .putMat4f(projectionMatrix)
                    .putMat4f(poseStack.last().pose())
                    .putFloat(time)
                    .putVec3(new Vector3f(rgb[0], rgb[1], rgb[2]));
        }

        Vec3 camPos = Minecraft.getInstance().gameRenderer.getMainCamera().position();
        byte r = (byte) Math.round(rgb[0] * 255.0F);
        byte g = (byte) Math.round(rgb[1] * 255.0F);
        byte b = (byte) Math.round(rgb[2] * 255.0F);

        BufferBuilder builder = new BufferBuilder(
                new ByteBufferBuilder(1536), VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR
        );

        for (int id : state.highlightedIds()) {
            Entity entity = level.getEntity(id);
            if (entity == null) {
                continue;
            }
            Vec3 relative = entity.getPosition(partialTick).subtract(camPos).add(0, entity.getBbHeight() * 0.5, 0);
            float size = Math.max(entity.getBbWidth(), entity.getBbHeight()) * 0.6F + 0.3F;
            addBillboard(builder, relative, size, r, g, b);
        }

        MeshData mesh = builder.build();
        if (mesh == null) {
            return;
        }

        try (GpuBuffer vertexBuffer = RenderSystem.getDevice().createBuffer(
                () -> "Divine Sense Markers", GpuBuffer.USAGE_VERTEX, mesh.vertexBuffer()
        )) {
            RenderTarget mainTarget = Minecraft.getInstance().getMainRenderTarget();
            try (RenderPass pass = encoder.createRenderPass(
                    () -> "Divine Sense Markers",
                    mainTarget.getColorTextureView(),
                    OptionalInt.empty()
            )) {
                pass.setPipeline(DivineSensePipelines.DIVINE_SENSE_RESULT);
                pass.setUniform("DivineSenseResultUniform", resultUbo.currentBuffer());
                pass.setVertexBuffer(0, vertexBuffer);
                if (mesh.indexBuffer() != null) {
                    RenderSystem.AutoStorageIndexBuffer indices = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
                    pass.setIndexBuffer(indices.getBuffer(mesh.drawState().indexCount()), indices.type());
                    pass.drawIndexed(0, 0, mesh.drawState().indexCount(), 1);
                }
            }
        }
    }

    private static void addBillboard(BufferBuilder builder, Vec3 relativePos, float size, byte r, byte g, byte b) {
        float x = (float) relativePos.x;
        float y = (float) relativePos.y;
        float z = (float) relativePos.z;
        builder.addVertex(x - size, y - size, z).setUv(0, 0).setColor(r, g, b, (byte) 200);
        builder.addVertex(x - size, y + size, z).setUv(0, 1).setColor(r, g, b, (byte) 200);
        builder.addVertex(x + size, y + size, z).setUv(1, 1).setColor(r, g, b, (byte) 200);
        builder.addVertex(x + size, y - size, z).setUv(1, 0).setColor(r, g, b, (byte) 200);
    }
}