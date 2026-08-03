package net.zic.ascension.handler;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.impl.core.effect.FrozenStateService;
import net.zic.ascension.impl.core.effect.SkillEffectManager;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class SkillEffectTicker {
    private SkillEffectTicker() {
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Pre event) {
        if (!(event.getEntity() instanceof LivingEntity entity) || entity.level().isClientSide()) {
            return;
        }

        if (FrozenStateService.isActive(entity)) {
            FrozenStateService.tick(entity);
        }
        if (SkillEffectManager.hasActiveEffects(entity)) {
            SkillEffectManager.tick(entity);
        }
    }
}
