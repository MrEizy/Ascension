package net.zic.ascension.shaders.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.entity.custom.shaders.RiftEntity;

/**
 * Renders the rift entity as a camera-facing square billboard.
 *
 * The quad is square so the shader's UV space is undistorted — equal angles
 * between the five crack arms look equal on screen.
 *
 * Uniform upload mirrors Lodestone's pattern:
 *   RenderHandler.HANDLERS stores a ShaderUniformHandler per RenderType,
 *   which is fired before each batch flush.  Here we call uploadRiftUniforms()
 *   directly before submitting geometry, which is equivalent.
 */
public class RiftRenderer extends EntityRenderer<RiftEntity> {

    private static final ResourceLocation DUMMY_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "textures/vfx/white.png");

    /** Square side length in blocks. Increase to scale the whole effect up. */
    private static final float SIZE = 3.0f;

    public RiftRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(RiftEntity entity, float yaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {

        // Push uniforms BEFORE geometry — mirrors Lodestone's RenderHandler.draw()
        // which calls handler.updateShaderData(shader) before flushing each buffer.
        ModShaders.uploadRiftUniforms(partialTicks);

        poseStack.pushPose();
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());

        float h = SIZE * 0.5f;
        float w = SIZE * 0.5f;

        VertexConsumer vc = bufferSource.getBuffer(ModRenderTypes.RIFT);
        var pose = poseStack.last();

        // Square quad centred at entity origin.
        // UV: v=1 at bottom, v=0 at top — crack texture roots (bright base) at centre.
        vc.addVertex(pose, -w, -h, 0f).setColor(255,255,255,255).setUv(0f,1f)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(pose,0,0,1);
        vc.addVertex(pose,  w, -h, 0f).setColor(255,255,255,255).setUv(1f,1f)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(pose,0,0,1);
        vc.addVertex(pose,  w,  h, 0f).setColor(255,255,255,255).setUv(1f,0f)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(pose,0,0,1);
        vc.addVertex(pose, -w,  h, 0f).setColor(255,255,255,255).setUv(0f,0f)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(pose,0,0,1);

        poseStack.popPose();
        super.render(entity, yaw, partialTicks, poseStack, bufferSource, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(RiftEntity entity) {
        return DUMMY_TEXTURE;
    }
}