package net.zic.ascension.handler;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.capabilities.CoreCapabilities;
import net.zic.ascension.api.ascension.capabilities.EntityQiProvider;
import net.zic.ascension.api.ascension.core.resource.ResourceTransactions;
import net.zic.ascension.common.util.AscensionAttributes;
import net.zic.ascension.impl.resource.AscensionResourceSources;
import net.zic.ascension.impl.resource.AscensionResourceTypes;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class QiTicker {
    private static final double TICKS_PER_SECOND = 20.0D;

    private QiTicker() {
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Pre event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        EntityQiProvider provider = player.getCapability(CoreCapabilities.ASCENSION_ENTITY_QI_PROVIDER);
        if (provider == null || !player.getAttributes().hasAttribute(AscensionAttributes.QI_REGEN_RATE)) {
            return;
        }

        double maximum = Math.max(0.0D, provider.getMaxQi());
        double current = Math.max(0.0D, provider.getQi());

        if (current > maximum) {
            provider.setQi(maximum);
            current = maximum;
        }

        if (current >= maximum) {
            return;
        }

        double rate = Math.max(0.0D, player.getAttributeValue(AscensionAttributes.QI_REGEN_RATE));
        if (rate <= 0.0D) {
            return;
        }

        ResourceTransactions.restore(
                player,
                AscensionResourceTypes.QI.getId(),
                AscensionResourceSources.NATURAL_REGENERATION,
                rate / TICKS_PER_SECOND
        );
    }
}
