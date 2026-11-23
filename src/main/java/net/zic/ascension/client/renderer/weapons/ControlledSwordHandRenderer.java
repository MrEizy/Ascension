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

import java.util.EnumSet;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID, value = Dist.CLIENT)
public final class ControlledSwordHandRenderer {
    private ControlledSwordHandRenderer() {
    }

    private static EnumSet<InteractionHand> hiddenHands(Player player) {
        EnumSet<InteractionHand> hidden = EnumSet.noneOf(InteractionHand.class);
        if (player == null || player.level() == null) {
            return hidden;
        }
        for (ControlledSwordEntity sword : player.level().getEntitiesOfClass(
                ControlledSwordEntity.class, new AABB(player.blockPosition()).inflate(64.0D))) {
            if (sword.hidesHeldSword(player)) {
                hidden.add(sword.swordHand());
            }
            if (hidden.size() == 2) {
                break;
            }
        }
        return hidden;
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
        AvatarRenderState state = event.getRenderState();
        EnumSet<InteractionHand> hidden = hiddenHands(player);
        if (hidden.contains(InteractionHand.MAIN_HAND)) {
            clearHand(state, player.getMainArm());
        }
        if (hidden.contains(InteractionHand.OFF_HAND)) {
            clearHand(state, player.getMainArm().getOpposite());
        }
    }

    private static void clearHand(AvatarRenderState state, HumanoidArm arm) {
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
        if (hiddenHands(Minecraft.getInstance().player).contains(event.getHand())) {
            event.setCanceled(true);
        }
    }
}
