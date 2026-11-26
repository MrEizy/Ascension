package net.zic.ascension.handler;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.impl.core.attribute.ScaledAttributes;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class AttributeScalingTicker {
    private AttributeScalingTicker() {}

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Pre event) {
        if (!(event.getEntity() instanceof LivingEntity entity) || entity.level().isClientSide() || !entity.isAlive()) return;
        if (!(entity instanceof ServerPlayer) && Math.floorMod((long) entity.tickCount + entity.getId(), 10) != 0) return;
        ScaledAttributes.refresh(entity);
    }
}
