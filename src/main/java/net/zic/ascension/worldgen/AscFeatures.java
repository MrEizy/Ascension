package net.zic.ascension.worldgen;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.worldgen.features.HerbFeature;
import net.zic.ascension.worldgen.features.LingzhiMushroomConfiguration;
import net.zic.ascension.worldgen.features.LingzhiMushroomFeature;
import net.zic.ascension.worldgen.features.SurfaceRockFeature;
import net.zic.ascension.worldgen.features.SurfaceScatterFeature;

public class AscFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(Registries.FEATURE, AscensionCraft.MOD_ID);

    public static final DeferredHolder<Feature<?>, Feature<HerbFeature.Configuration>> HERB =
            FEATURES.register("herb", () -> new HerbFeature(HerbFeature.Configuration.CODEC));

    public static final DeferredHolder<Feature<?>, Feature<LingzhiMushroomConfiguration>> LINGZHI_MUSHROOM =
            FEATURES.register("lingzhi_mushroom", () -> new LingzhiMushroomFeature(LingzhiMushroomConfiguration.CODEC));

    public static final DeferredHolder<Feature<?>, Feature<SurfaceRockFeature.Configuration>> SURFACE_ROCK =
            FEATURES.register("surface_rock", () -> new SurfaceRockFeature(SurfaceRockFeature.Configuration.CODEC));

    public static final DeferredHolder<Feature<?>, Feature<SurfaceScatterFeature.Configuration>> SURFACE_SCATTER =
            FEATURES.register("surface_scatter", () -> new SurfaceScatterFeature(SurfaceScatterFeature.Configuration.CODEC));


    public static void register(IEventBus eventBus) {
        FEATURES.register(eventBus);
    }
}