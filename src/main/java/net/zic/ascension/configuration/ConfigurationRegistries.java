package net.zic.ascension.configuration;

import net.minecraft.core.Registry;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.configuration.biome.RawBiomeConfiguration;
import net.zic.ascension.configuration.dimension.RawDimensionConfiguration;
import net.zic.ascension.api.ascension.core.path.interaction.PathInteraction;
import net.zic.ascension.configuration.mobs.condition.MobConfigurationConditionType;
import net.zic.ascension.configuration.mob_traits.MobTraitDefinition;
import net.zic.ascension.configuration.mob_traits.MobTraitDefinitionType;
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

    public static final RegistryHelper.DataPackRegistry<MobTraitDefinition> MOB_TRAIT_DEFINITION_REGISTRY =
            new RegistryHelper.DataPackRegistry<>(RegistryHelper.key(AscensionCraft.MOD_ID,"config/mob_traits"),()->MobTraitDefinitionType.MOB_TRAIT_CODEC);

    public static final Registry<MobConfigurationConditionType> MOB_CONFIGURATION_CONDITION_TYPES = RegistryHelper.registry(AscensionCraft.MOD_ID, "mob_configuration_condition_types");

    public static final Registry<MobTraitDefinitionType> MOB_TRAIT_TYPES = RegistryHelper.registry(AscensionCraft.MOD_ID, "mob_trait_types");

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
                MOB_TRAIT_DEFINITION_REGISTRY.key(),
                MOB_TRAIT_DEFINITION_REGISTRY.codec().get(),
                MOB_TRAIT_DEFINITION_REGISTRY.codec().get()
        );
    }
    @SubscribeEvent
    public static void registerRegistries(NewRegistryEvent event) {
        event.register(MOB_CONFIGURATION_CONDITION_TYPES);
        event.register(MOB_TRAIT_TYPES);
    }
}
