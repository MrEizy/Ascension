package net.zic.ascension.client.combat;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.zic.ascension.AscensionCraft;

/**
 * Draws the EMPTY offhand arm in first person while dual wielding is active.
 *
 * Why this is needed: vanilla's ItemInHandRenderer.renderArmWithItem only draws an empty arm for the main hand
 * ("if (isMainHand && !player.isInvisible())"), so an empty offhand shows nothing. A weapon in the offhand needs no help: vanilla
 * already draws it on the left (or right, for left-handed players) and plays its swing from the offhand attack progress.
 *
 * The arm is drawn with the same transforms as vanilla's private renderPlayerArm (copied from your ItemInHandRenderer source), using
 * the offhand's arm = getMainArm().getOpposite(), which is exactly vanilla's left/right mirroring when a player switches main hand in
 * the options. The swing and equip values come straight from the event, which vanilla fills in for the offhand: swing progress is the
 * offhand's own attack animation (the client calls swing(OFF_HAND) on each offhand attack), and equip progress is its raise animation.
 */
@EventBusSubscriber(modid = AscensionCraft.MOD_ID, value = Dist.CLIENT)
public final class DualWieldHandRenderer {
    private DualWieldHandRenderer() {
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        if (event.getHand() != InteractionHand.OFF_HAND || !event.getItemStack().isEmpty() || !DualWieldClientState.active()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        AbstractClientPlayer player = minecraft.player;
        if (player == null || player.isInvisible() || player.isScoping()) {
            return; // the same cases where vanilla draws no arm
        }

        PoseStack poseStack = event.getPoseStack();
        HumanoidArm arm = player.getMainArm().getOpposite();
        boolean isRightArm = arm != HumanoidArm.LEFT;
        float invert = isRightArm ? 1.0F : -1.0F;
        float attackValue = event.getSwingProgress();
        float inverseArmHeight = event.getEquipProgress();

        poseStack.pushPose();
        float sqrtAttackValue = Mth.sqrt(attackValue);
        float xSwingPosition = -0.3F * Mth.sin((double) (sqrtAttackValue * (float) Math.PI));
        float ySwingPosition = 0.4F * Mth.sin((double) (sqrtAttackValue * ((float) Math.PI * 2F)));
        float zSwingPosition = -0.4F * Mth.sin((double) (attackValue * (float) Math.PI));
        poseStack.translate(invert * (xSwingPosition + 0.64000005F), ySwingPosition + -0.6F + inverseArmHeight * -0.6F, zSwingPosition + -0.71999997F);
        poseStack.mulPose(Axis.YP.rotationDegrees(invert * 45.0F));
        float zSwingRotation = Mth.sin((double) (attackValue * attackValue * (float) Math.PI));
        float ySwingRotation = Mth.sin((double) (sqrtAttackValue * (float) Math.PI));
        poseStack.mulPose(Axis.YP.rotationDegrees(invert * ySwingRotation * 70.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(invert * zSwingRotation * -20.0F));
        poseStack.translate(invert * -1.0F, 3.6F, 3.5F);
        poseStack.mulPose(Axis.ZP.rotationDegrees(invert * 120.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(200.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(invert * -135.0F));
        poseStack.translate(invert * 5.6F, 0.0F, 0.0F);

        AvatarRenderer<AbstractClientPlayer> avatarRenderer = minecraft.getEntityRenderDispatcher().getPlayerRenderer(player);
        Identifier skinTexture = player.getSkin().body().texturePath();
        if (isRightArm) {
            avatarRenderer.renderRightHand(poseStack, event.getSubmitNodeCollector(), event.getPackedLight(), skinTexture,
                    player.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE), player);
        } else {
            avatarRenderer.renderLeftHand(poseStack, event.getSubmitNodeCollector(), event.getPackedLight(), skinTexture,
                    player.isModelPartShown(PlayerModelPart.LEFT_SLEEVE), player);
        }
        poseStack.popPose();
    }
}