package net.zic.ascension.client.renderer.weapons;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.entities.weapons.ControlledSwordEntity;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID, value = Dist.CLIENT)
public final class ControlledSwordHandRenderer {
    private ControlledSwordHandRenderer() {
    }

    private static ControlledSwordEntity visibleSword(Player player) {
        if (player == null || player.level() == null) {
            return null;
        }
        for (ControlledSwordEntity sword : player.level().getEntitiesOfClass(
                ControlledSwordEntity.class, new AABB(player.blockPosition()).inflate(32.0D))) {
            if (sword.hidesHeldSword(player)) {
                return sword;
            }
        }
        return null;
    }

    @SubscribeEvent
    public static void onRenderPlayer(RenderPlayerEvent.Pre<?> event) {
        var level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        Entity entity = level.getEntity(event.getRenderState().id);
        if (!(entity instanceof Player player)) {
            return;
        }
        ControlledSwordEntity sword = visibleSword(player);
        if (sword == null) {
            return;
        }
        AvatarRenderState state = event.getRenderState();
        HumanoidArm arm = sword.swordHand() == InteractionHand.MAIN_HAND
                ? player.getMainArm() : player.getMainArm().getOpposite();
        if (arm == HumanoidArm.RIGHT) {
            state.rightHandItemState.clear();
            state.rightArmPose = HumanoidModel.ArmPose.EMPTY;
        } else {
            state.leftHandItemState.clear();
            state.leftArmPose = HumanoidModel.ArmPose.EMPTY;
        }
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        ControlledSwordEntity sword = visibleSword(Minecraft.getInstance().player);
        if (sword != null && event.getHand() == sword.swordHand()) {
            event.setCanceled(true);
        }
    }
}
