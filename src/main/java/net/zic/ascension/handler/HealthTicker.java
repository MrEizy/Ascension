package net.zic.ascension.handler;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.util.AscensionAttributes;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class HealthTicker {
    private static final int REGENERATION_INTERVAL = 20;
    private static final float VANILLA_HEALTH_THRESHOLD = 20.0F;

    private HealthTicker() {
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Pre event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || player.tickCount % REGENERATION_INTERVAL != 0
                || player.isSpectator()
                || !player.isAlive()
                || !player.getAttributes().hasAttribute(AscensionAttributes.HEALTH_REGEN_RATE)) {
            return;
        }

        float maximum = player.getMaxHealth();
        float current = player.getHealth();

        if (current >= maximum || current < Math.min(VANILLA_HEALTH_THRESHOLD, maximum)) {
            return;
        }

        double rate = Math.max(0.0D, player.getAttributeValue(AscensionAttributes.HEALTH_REGEN_RATE));
        if (rate <= 0.0D) {
            return;
        }

        player.heal((float) Math.min(rate, maximum - current));
    }
}
