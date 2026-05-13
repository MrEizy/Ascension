package net.thejadeproject.ascension.worldgen;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.worldgen.custom.WildHerbFeature;
import net.thejadeproject.ascension.worldgen.custom.WildHerbFeatureConfig;

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
