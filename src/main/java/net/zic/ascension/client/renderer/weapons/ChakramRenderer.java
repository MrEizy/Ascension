package net.zic.ascension.client.renderer.weapons;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
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
import net.zic.ascension.common.entities.weapons.ChakramEntity;
import org.joml.Matrix4f;

public class ChakramRenderer extends EntityRenderer<ChakramEntity, ChakramRenderer.State> {

    private static final float SPIN_PER_TICK = 52.0F;
    private static final float SCALE = 0.72F;
    private static final float BANK = 18.0F;

    private final ItemModelResolver itemModelResolver;

    public ChakramRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.itemModelResolver = ctx.getItemModelResolver();
        this.shadowRadius = 0.15F;
    }

    @Override
    public State createRenderState() { return new State(); }

    @Override
    public void extractRenderState(ChakramEntity chakram, State state, float partialTick) {
        super.extractRenderState(chakram, state, partialTick);
        itemModelResolver.updateForNonLiving(state.item, chakram.getItem(),
                ItemDisplayContext.NONE, chakram);
        state.yaw   = Mth.rotLerp(partialTick, chakram.yRotO, chakram.getYRot());
        state.pitch = Mth.lerp(partialTick, chakram.xRotO, chakram.getXRot());
        state.spin  = (chakram.tickCount + partialTick) * SPIN_PER_TICK;

        Minecraft mc = Minecraft.getInstance();
        state.hidden = chakram.tickCount < 2
                && mc.player != null
                && chakram.getOwner() == mc.player;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector,
                       CameraRenderState cameraState) {
        if (state.hidden || state.item.isEmpty()) return;
        poseStack.pushPose();

        // Matches reference DiscPose.apply exactly:
        //   yaw → bank(Z 18°) → pitch (XP 90 - pitch*0.5) → spin(ZP -spin) → scale
        Matrix4f t = new Matrix4f()
                .translate(0.0F, 0.12F, 0.0F)
                .rotate(Axis.YP.rotationDegrees(state.yaw))
                .rotate(Axis.ZP.rotationDegrees(BANK))
                .rotate(Axis.XP.rotationDegrees(90.0F - state.pitch * 0.5F))
                .rotate(Axis.ZP.rotationDegrees(-state.spin))
                .scale(SCALE);

        poseStack.mulPose(t);
        state.item.submit(poseStack, collector, state.lightCoords,
                OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }

    public static class State extends EntityRenderState {
        public final ItemStackRenderState item = new ItemStackRenderState();
        public float yaw, pitch, spin;
        public boolean hidden;
    }
}