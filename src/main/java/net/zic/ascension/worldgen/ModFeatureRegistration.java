package net.zic.ascension.worldgen;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.worldgen.custom.WildHerbFeature;
import net.zic.ascension.worldgen.custom.WildHerbFeatureConfig;

import java.util.function.Supplier;

public class ModFeatureRegistration {

    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, AscensionCraft.MOD_ID);

    public static final Supplier<Feature<WildHerbFeatureConfig>> WILD_HERB_FEATURE =
            FEATURES.register("wild_herb_feature",
                    () -> new WildHerbFeature(WildHerbFeatureConfig.CODEC));


    public static void register(IEventBus eventBus) {
        FEATURES.register(eventBus);
    }
}
