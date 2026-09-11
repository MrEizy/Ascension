package net.zic.ascension.impl.core.skill.castable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.zic.ascension.AscensionCraft;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID, value = Dist.CLIENT)
public final class SwordFlightRenderer {
    private SwordFlightRenderer() {
    }

    private static final float SWORD_SCALE = 3.2F;
    private static final double FEET_OFFSET_Y = -0.05D;
    private static final float SWORD_FLIGHT_PITCH_SCALE = 0.6F;

    @SubscribeEvent
    public static void onRenderPre(RenderPlayerEvent.Pre<?> event) {
        Player player = resolvePlayer(event.getRenderState());
        if (player == null || !SwordFlightPhysics.isFlying(player.getUUID())) {
            return;
        }
        freezeLimbSwing(event.getRenderState());
        hideFlightSword(player, event.getRenderState());
    }

    @SubscribeEvent
    public static void onRenderPost(RenderPlayerEvent.Post<?> event) {
        Player player = resolvePlayer(event.getRenderState());
        if (player == null || !SwordFlightPhysics.isFlying(player.getUUID())) {
            return;
        }
        renderFlatSword(player, event.getPartialTick(), event.getPoseStack(), event.getSubmitNodeCollector());
    }

    private static Player resolvePlayer(AvatarRenderState state) {
        var level = Minecraft.getInstance().level;
        if (level == null) {
            return null;
        }
        Entity entity = level.getEntity(state.id);
        return entity instanceof Player player ? player : null;
    }

    private static void freezeLimbSwing(AvatarRenderState state) {
        state.walkAnimationPos = 0.0F;
        state.walkAnimationSpeed = 0.0F;
    }

    private static void hideFlightSword(Player player, AvatarRenderState state) {
        InteractionHand hand = SwordFlightPhysics.heldSwordHand(player);
        if (hand == null) {
            return;
        }
        HumanoidArm arm = hand == InteractionHand.MAIN_HAND ? player.getMainArm() : opposite(player.getMainArm());
        if (arm == HumanoidArm.RIGHT) {
            state.rightHandItemState.clear();
            state.rightArmPose = HumanoidModel.ArmPose.EMPTY;
        } else {
            state.leftHandItemState.clear();
            state.leftArmPose = HumanoidModel.ArmPose.EMPTY;
        }
    }

    private static HumanoidArm opposite(HumanoidArm arm) {
        return arm == HumanoidArm.RIGHT ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
    }

    private static void renderFlatSword(Player player, float partialTick, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
        ItemStack sword = SwordFlightPhysics.heldSword(player);
        if (sword == null || sword.isEmpty()) {
            return;
        }

        double bank = SwordFlightPhysics.getBankDegrees(player.getUUID(), partialTick);
        double flightPitch = SwordFlightPhysics.getVelocityPitchDegrees(player.getUUID());
        float swordPitch = (float) flightPitch * SWORD_FLIGHT_PITCH_SCALE;
        int packedLight = LevelRenderer.getLightCoords(player.level(), player.blockPosition());
        ItemInHandRenderer itemInHandRenderer = Minecraft.getInstance().gameRenderer.itemInHandRenderer;

        poseStack.pushPose();
        poseStack.translate(0.0D, FEET_OFFSET_Y, 0.0D);
        poseStack.mulPose(Axis.ZP.rotationDegrees((float) bank));
        poseStack.mulPose(Axis.XP.rotationDegrees(swordPitch));
        poseStack.mulPose(Axis.YP.rotationDegrees(-player.yBodyRot));
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        poseStack.scale(SWORD_SCALE, SWORD_SCALE, SWORD_SCALE);

        itemInHandRenderer.renderItem(
                player,
                sword,
                ItemDisplayContext.GROUND,
                poseStack,
                submitNodeCollector,
                packedLight
        );

        poseStack.popPose();
    }
}
