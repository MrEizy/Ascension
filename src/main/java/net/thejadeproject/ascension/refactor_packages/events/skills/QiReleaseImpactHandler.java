package net.thejadeproject.ascension.refactor_packages.events.skills;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.refactor_packages.skills.custom.qi.QiRelease;

import java.util.Iterator;
import java.util.Map;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public class QiReleaseImpactHandler {

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel().isClientSide()) return;

        long gameTime = event.getLevel().getGameTime();

        Iterator<Map.Entry<LivingEntity, Float>> it = QiRelease.PUSH_CRASH_DAMAGE.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<LivingEntity, Float> entry = it.next();
            LivingEntity entity = entry.getKey();
            float damage = entry.getValue();
            Long expiry = QiRelease.PUSH_EXPIRY.get(entity);

            if (expiry == null || gameTime >= expiry || !entity.isAlive()) {
                it.remove();
                QiRelease.PUSH_EXPIRY.remove(entity);
                continue;
            }

            if (entity.horizontalCollision) {
                entity.hurt(entity.damageSources().generic(), damage);
                it.remove();
                QiRelease.PUSH_EXPIRY.remove(entity);
            }
        }
    }
}
