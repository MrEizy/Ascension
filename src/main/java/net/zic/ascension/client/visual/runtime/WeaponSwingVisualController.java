package net.zic.ascension.client.visual.runtime;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.zic.ascension.api.ascension.core.runtime.RuntimeVisualDefinition;
import net.zic.ascension.api.ascension.core.runtime.RuntimeVisualState;
import net.zic.ascension.impl.runtime.weapon.WeaponSwings;


public final class WeaponSwingVisualController implements RuntimeVisualController {
    private static final int FRAME_COUNT = 7;
    private static final int FRAME_TICKS = 1;
    private static final double DRAG = 0.92D;

    private WeaponSwingVisualController() {
    }

    public static void registerDefault() {
        ClientRuntimeVisuals.register(WeaponSwings.VISUAL_ID, new WeaponSwingVisualController());
    }

    @Override
    public void render(RuntimeVisualState state, RenderLevelStageEvent.AfterTranslucentFeatures event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || state.definition() == null || state.definition().layers().isEmpty()) {
            return;
        }

        RuntimeVisualDefinition.Layer layer = state.definition().layers().getFirst();
        Identifier textureBase = layer.resources().texture().orElse(null);
        if (textureBase == null) {
            return;
        }

        float partialTick = ClientRuntimeVisuals.partialTick();
        double now = minecraft.level.getGameTime() + partialTick;
        double age = Math.max(0.0D, now - state.primaryValue());
        int frame = Math.clamp((int) Math.floor(age / FRAME_TICKS), 0, FRAME_COUNT - 1);
        Identifier texture = Identifier.fromNamespaceAndPath(
                textureBase.getNamespace(),
                "textures/" + textureBase.getPath() + "/" + frame + ".png"
        );

        Vec3 center = positionAtAge(state.position(), state.offset(), age)
                .subtract(minecraft.gameRenderer.getMainCamera().position());
        Vec3 rotation = layer.transform().rotation();
        double width = Math.max(0.05D, layer.geometry().width().resolve(state, now));
        double length = Math.max(0.05D, layer.geometry().length().resolve(state, now));

        Vec3 a = transform(new Vec3(-0.5D * width, 0.0D, -0.5D * length), rotation).add(center);
        Vec3 b = transform(new Vec3(0.5D * width, 0.0D, -0.5D * length), rotation).add(center);
        Vec3 c = transform(new Vec3(0.5D * width, 0.0D, 0.5D * length), rotation).add(center);
        Vec3 d = transform(new Vec3(-0.5D * width, 0.0D, 0.5D * length), rotation).add(center);
        Vec3 normal = transform(new Vec3(0.0D, 1.0D, 0.0D), rotation).normalize();

        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        VertexConsumer vertices = buffers.getBuffer(RenderTypes.entityTranslucent(texture));
        PoseStack.Pose pose = event.getPoseStack().last();
        vertex(vertices, pose, a, 0.0F, 1.0F, normal);
        vertex(vertices, pose, b, 1.0F, 1.0F, normal);
        vertex(vertices, pose, c, 1.0F, 0.0F, normal);
        vertex(vertices, pose, d, 0.0F, 0.0F, normal);
        buffers.endLastBatch();
    }

    private static Vec3 positionAtAge(Vec3 start, Vec3 initialVelocity, double age) {
        if (initialVelocity.lengthSqr() <= 1.0E-10D || age <= 0.0D) {
            return start;
        }
        double travelled = (1.0D - Math.pow(DRAG, age)) / (1.0D - DRAG);
        return start.add(initialVelocity.scale(travelled));
    }

    private static Vec3 transform(Vec3 value, Vec3 degrees) {
        Vec3 result = rotateZ(value, Math.toRadians(degrees.z));
        result = rotateX(result, Math.toRadians(degrees.x));
        return rotateY(result, Math.toRadians(-degrees.y));
    }

    private static Vec3 rotateX(Vec3 value, double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        return new Vec3(value.x, value.y * cos - value.z * sin, value.y * sin + value.z * cos);
    }

    private static Vec3 rotateY(Vec3 value, double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        return new Vec3(value.x * cos + value.z * sin, value.y, -value.x * sin + value.z * cos);
    }

    private static Vec3 rotateZ(Vec3 value, double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        return new Vec3(value.x * cos - value.y * sin, value.x * sin + value.y * cos, value.z);
    }

    private static void vertex(
            VertexConsumer vertices,
            PoseStack.Pose pose,
            Vec3 point,
            float u,
            float v,
            Vec3 normal
    ) {
        vertices.addVertex(pose, (float) point.x, (float) point.y, (float) point.z)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(0x00F000F0)
                .setNormal(pose, (float) normal.x, (float) normal.y, (float) normal.z);
    }
}
