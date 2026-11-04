package net.zic.ascension.api.ascension.core.alchemy.medicinal;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.RegistryAccess;
import net.zic.ascension.impl.datapack.alchemy.AlchemyMaterialProviders;
import java.util.ArrayList;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.zic.ascension.api.ascension.core.alchemy.AlchemyContext;
import net.zic.ascension.api.ascension.core.alchemy.AlchemyMaterials;

public final class MedicinalHerbConsumption {
    private MedicinalHerbConsumption() {
    }

    public static MedicinalProfile profile(ItemStack stack, RegistryAccess access) {
        if (stack == null || stack.isEmpty() || access == null) {
            return new MedicinalProfile(MedicinalComposition.EMPTY, java.util.List.of());
        }
        return AlchemyMaterials.resolve(stack, AlchemyContext.EMPTY).map(material -> {
            MedicinalComposition composition = MedicinalComposition.of(material.substance());
            MedicinalProfile generated = MedicinalEffectResolver.resolve(composition, access);
            java.util.List<MedicinalProfile.Effect> effects = new ArrayList<>(generated.effects());
            if (stack.getData(AlchemyMaterials.MATERIAL) instanceof AlchemyMaterialProviders.Herb herb) {
                effects.addAll(herb.directEffects());
            }
            return new MedicinalProfile(composition, effects);
        }).orElseGet(() -> new MedicinalProfile(MedicinalComposition.EMPTY, java.util.List.of()));
    }

    public static void consume(ItemStack stack, LivingEntity consumer) {
        if (stack.isEmpty() || !(consumer.level() instanceof ServerLevel server)) return;
        MedicinalConsumptionService.consume(profile(stack, server.registryAccess()), consumer, false);
    }
}
