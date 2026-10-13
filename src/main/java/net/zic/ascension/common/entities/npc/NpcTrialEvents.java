package net.zic.ascension.common.entities.npc;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.zic.ascension.AscensionCraft;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class NpcTrialEvents {
    private NpcTrialEvents() { }

    @SubscribeEvent
    public static void onKill(LivingDeathEvent event) {
        LivingEntity victim = event.getEntity();
        if (!(victim.level() instanceof ServerLevel server) || !(victim instanceof Monster)) return;
        ServerPlayer killer = victim.getKillCredit() instanceof ServerPlayer player ? player : event.getSource().getEntity() instanceof ServerPlayer player ? player : null;
        if (killer == null) return;
        for (CultivatorNpcEntity npc : server.getEntitiesOfClass(CultivatorNpcEntity.class,
                victim.getBoundingBox().inflate(48), entity -> entity.hasActiveTrial(killer))) {
            npc.recordHostileKill(killer);
        }
    }

    @SubscribeEvent
    public static void onExperience(LivingExperienceDropEvent event) {
        if (event.getEntity() instanceof CultivatorNpcEntity npc && npc.isSummonedGuard()) {
            event.setDroppedExperience(0);
        }
    }

    @SubscribeEvent
    public static void onDrops(LivingDropsEvent event) {
        if (event.getEntity() instanceof CultivatorNpcEntity npc && npc.isSummonedGuard()) {
            event.setCanceled(true);
        }
    }
}
