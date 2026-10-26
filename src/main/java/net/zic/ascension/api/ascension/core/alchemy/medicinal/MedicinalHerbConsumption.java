package net.zic.ascension.api.ascension.core.alchemy.medicinal;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.zic.ascension.api.ascension.core.alchemy.AlchemyContext;
import net.zic.ascension.api.ascension.core.alchemy.AlchemyMaterials;

/**
 * Turns harvested herbs into direct medicinal compositions without furnace extraction.
 * Uses the same expression rules as pills while preserving raw-herb inefficiency and risk.
 */
public final class MedicinalHerbConsumption {
    private MedicinalHerbConsumption() {
    }

    public static void consume(ItemStack stack, LivingEntity consumer) {
        if (stack.isEmpty() || !(consumer.level() instanceof ServerLevel server)) return;
        AlchemyMaterials.resolve(stack, AlchemyContext.of(consumer)).ifPresent(material -> {
            MedicinalComposition composition = MedicinalComposition.of(material.substance());
            MedicinalProfile profile = MedicinalEffectResolver.resolve(composition, server.registryAccess());
            MedicinalConsumptionService.consume(profile, consumer, false);
        });
    }
}
