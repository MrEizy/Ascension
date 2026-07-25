package net.zic.ascension.impl.effect;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.core.effect.SkillEffectService;
import net.zic.ascension.api.core.effect.frozen.FrozenStateService;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class SkillEffectTicker {
    private SkillEffectTicker() {
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Pre event) {
        if (!(event.getEntity() instanceof LivingEntity entity) || entity.level().isClientSide()) {
            return;
        }
        FrozenStateService.tick(entity);
        SkillEffectService.tick(entity);
    }
}
