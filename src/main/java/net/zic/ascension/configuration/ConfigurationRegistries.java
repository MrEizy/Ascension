package net.zic.ascension.configuration;

import net.minecraft.core.Registry;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.configuration.mobs.condition.MobConfigurationConditionType;
import net.zic.ascension.configuration.mob_traits.MobTraitDefinition;
import net.zic.ascension.configuration.mob_traits.MobTraitDefinitionType;
import net.zic.zenithlib.registry.RegistryHelper;
@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public class ConfigurationRegistries {
    public static final RegistryHelper.DataPackRegistry<MobTraitDefinition> MOB_TRAIT_DEFINITION_REGISTRY =
            new RegistryHelper.DataPackRegistry<>(RegistryHelper.key(AscensionCraft.MOD_ID,"config/mob_traits"),()->MobTraitDefinitionType.MOB_TRAIT_CODEC);

    public static final Registry<MobConfigurationConditionType> MOB_CONFIGURATION_CONDITION_TYPES = RegistryHelper.registry(AscensionCraft.MOD_ID, "mob_configuration_condition_types");

    public static final Registry<MobTraitDefinitionType> MOB_TRAIT_TYPES = RegistryHelper.registry(AscensionCraft.MOD_ID, "mob_trait_types");

    @SubscribeEvent
    public static void registerDatapackRegistries(DataPackRegistryEvent.NewRegistry event) {
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
