package net.zic.ascension.common.entities.npc;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.zic.ascension.AscensionCraft;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class NpcDuelEvents {
    private NpcDuelEvents() { }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onDamage(LivingDamageEvent.Pre event) {
        LivingEntity victim = event.getEntity();
        if (!(victim.level() instanceof ServerLevel) || event.getNewDamage() <= 0.0F) return;
        if (victim instanceof CultivatorNpcEntity npc && event.getSource().getEntity() instanceof ServerPlayer attacker
                && npc.isFriendlyDuelWith(attacker)) {
            checkFinish(event, npc, attacker, victim, true);
        } else if (victim instanceof ServerPlayer player && event.getSource().getEntity() instanceof CultivatorNpcEntity npc
                && npc.isFriendlyDuelWith(player)) {
            checkFinish(event, npc, player, victim, false);
        }
    }

    private static void checkFinish(LivingDamageEvent.Pre event, CultivatorNpcEntity npc, ServerPlayer player,
                                    LivingEntity victim, boolean playerWon) {
        float threshold = npc.duelFinishHealth(victim);
        float damage = event.getNewDamage();
        if (victim.getHealth() - damage > threshold) return;
        event.setNewDamage(Math.max(0.0F, victim.getHealth() - threshold));
        npc.resolveFriendlyDuel(player, playerWon);
    }
}
