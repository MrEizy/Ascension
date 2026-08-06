package net.zic.ascension.worldgen;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.worldgen.features.LingzhiMushroomFeature;

public class AscFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(Registries.FEATURE, AscensionCraft.MOD_ID);

    public static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> LINGZHI_MUSHROOM =
            FEATURES.register("lingzhi_mushroom", () -> new LingzhiMushroomFeature(NoneFeatureConfiguration.CODEC));

    public static void register(IEventBus eventBus) {
        FEATURES.register(eventBus);
    }
}