package net.zic.ascension.configuration;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.configuration.biome.RawBiomeConfiguration;
import net.zic.ascension.configuration.dimension.RawDimensionConfiguration;
import net.zic.ascension.api.ascension.core.path.interaction.PathInteraction;
import net.zic.zenithlib.registry.RegistryHelper;

import java.util.List;
@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public class ConfigurationRegistries {
    public static final RegistryHelper.DataPackRegistry<List<PathInteraction>> PATH_INTERACTION_REGISTRY = RegistryHelper.dataPackRegistry(
            AscensionCraft.MOD_ID,
            "config/path_interactions",
            ()-> PathInteraction.REGISTRY_CODEC
    );

    public static final RegistryHelper.DataPackRegistry<RawDimensionConfiguration> RAW_DIMENSION_CONFIGURATION_REGISTRY =
            new RegistryHelper.DataPackRegistry<>(RegistryHelper.key(AscensionCraft.MOD_ID,"config/dimension_configurations"),()->RawDimensionConfiguration.CODEC);
    public static final RegistryHelper.DataPackRegistry<RawBiomeConfiguration> RAW_BIOME_CONFIGURATION_REGISTRY =
            new RegistryHelper.DataPackRegistry<>(RegistryHelper.key(AscensionCraft.MOD_ID,"config/biome_configurations"),()->RawBiomeConfiguration.CODEC);

    public static final RegistryHelper.DataPackRegistry<RealmEffectivenessConfiguration> REALM_EFFECTIVENESS_REGISTRY =
            RegistryHelper.dataPackRegistry(AscensionCraft.MOD_ID, "config/realm_effectiveness", () -> RealmEffectivenessConfiguration.CODEC);

    @SubscribeEvent
    public static void registerDatapackRegistries(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(
                PATH_INTERACTION_REGISTRY.key(),
                PATH_INTERACTION_REGISTRY.codec().get(),
                PATH_INTERACTION_REGISTRY.codec().get()
        );
        event.dataPackRegistry(
                RAW_DIMENSION_CONFIGURATION_REGISTRY.key(),
                RAW_DIMENSION_CONFIGURATION_REGISTRY.codec().get(),
                RAW_DIMENSION_CONFIGURATION_REGISTRY.codec().get()
        );
        event.dataPackRegistry(
                RAW_BIOME_CONFIGURATION_REGISTRY.key(),
                RAW_BIOME_CONFIGURATION_REGISTRY.codec().get(),
                RAW_BIOME_CONFIGURATION_REGISTRY.codec().get()
        );
        event.dataPackRegistry(
                REALM_EFFECTIVENESS_REGISTRY.key(),
                REALM_EFFECTIVENESS_REGISTRY.codec().get(),
                REALM_EFFECTIVENESS_REGISTRY.codec().get()
        );
    }
}
