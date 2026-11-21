package net.zic.ascension.client.renderer.weapons;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.zic.ascension.common.entities.weapons.ControlledSwordEntity;

public final class ControlledSwordRenderer extends EntityRenderer<ControlledSwordEntity, ControlledSwordRenderer.State> {
    private final ItemModelResolver models;

    public ControlledSwordRenderer(EntityRendererProvider.Context context) {
        super(context);
        models = context.getItemModelResolver();
        shadowRadius = 0.1F;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(ControlledSwordEntity sword, State state, float partialTick) {
        super.extractRenderState(sword, state, partialTick);
        models.updateForNonLiving(state.item, sword.sword(), ItemDisplayContext.GROUND, sword);
        state.yaw = Mth.rotLerp(partialTick, sword.yRotO, sword.getYRot());
        state.pitch = Mth.lerp(partialTick, sword.xRotO, sword.getXRot());
        state.hover = sword.tickCount + partialTick;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
        if (state.item.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.07D + Math.sin(state.hover * 0.11D) * 0.025D, 0.0D);
        poseStack.mulPose(Axis.YP.rotationDegrees(state.yaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(state.pitch));
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        poseStack.scale(1.6F, 1.6F, 1.6F);
        state.item.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }

    public static final class State extends EntityRenderState {
        public final ItemStackRenderState item = new ItemStackRenderState();
        public float yaw;
        public float pitch;
        public float hover;
    }
}
