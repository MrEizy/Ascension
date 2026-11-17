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


@EventBusSubscriber(modid = AscensionCraft.MOD_ID, value = Dist.CLIENT)
public final class DualWieldHandRenderer {
    private DualWieldHandRenderer() {
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        if (event.getHand() != InteractionHand.MAIN_HAND || !DualWieldClientState.active()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        AbstractClientPlayer player = minecraft.player;
        if (player == null || !player.getOffhandItem().isEmpty() || player.isInvisible() || player.isScoping()) {
            return; // the same cases where vanilla draws no arm
        }

        float partialTick = event.getPartialTick(); // getPartialTicks() on older NeoForge
        float attackValue = DualWieldClientState.offhandSwingProgress(partialTick);
        if (attackValue <= 0.0F && player.swingingArm == InteractionHand.OFF_HAND) {
            attackValue = player.getAttackAnim(partialTick); // swings we didn't start ourselves
        }
        float inverseArmHeight = 0.0F; // this hand never "equips" anything, so it is never lowered

        PoseStack poseStack = event.getPoseStack();
        HumanoidArm arm = player.getMainArm().getOpposite();
        boolean isRightArm = arm != HumanoidArm.LEFT;
        float invert = isRightArm ? 1.0F : -1.0F;

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