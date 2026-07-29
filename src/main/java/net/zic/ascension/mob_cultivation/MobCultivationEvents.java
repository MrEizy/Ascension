package net.zic.ascension.mob_cultivation;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.zic.ascension.AscensionCraft;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class MobCultivationEvents {
    private MobCultivationEvents() {
    }

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof Mob mob)) {
            return;
        }
        MobCultivationManager.initialize(mob);
    }

    @SubscribeEvent
    public static void onEntityLeave(EntityLeaveLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof Mob mob)) {
            return;
        }
        MobCultivationManager.onLeaveLevel(mob);
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Pre event) {
        if (!(event.getEntity() instanceof Mob mob) || mob.level().isClientSide()) {
            return;
        }
        MobCultivationManager.tick(mob);
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof Mob mob)
                || !(mob.level() instanceof ServerLevel level)
                || !MobCultivationManager.isCultivated(mob)) {
            return;
        }

        for (ItemStack bonusLoot : MobCultivationManager.rollBonusLoot(mob)) {
            if (bonusLoot.isEmpty()) {
                continue;
            }
            event.getDrops().add(new ItemEntity(
                    level,
                    mob.getX(),
                    mob.getY(),
                    mob.getZ(),
                    bonusLoot
            ));
        }
    }
}
