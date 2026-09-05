package net.zic.ascension.configuration;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.util.ModTags;
import net.zic.ascension.configuration.item.qi_capacity.TierCapacityDefinition;
import net.zic.ascension.configuration.mobs.MobConfiguration;
@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public class ConfigurationDataMaps {
    public static final DataMapType<EntityType<?>, MobConfiguration> MOB_CONFIGURATION_DATA_MAP = DataMapType.builder(
            Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "mob_configuration"),
            Registries.ENTITY_TYPE,
            MobConfiguration.CODEC
    ).build();
    public static final DataMapType<Item, TierCapacityDefinition> ITEM_TIER_QI_CAPACITY = DataMapType.builder(
            Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID,"tier_capacity"),
            Registries.ITEM,
            TierCapacityDefinition.CODEC
    ).build();


    @SubscribeEvent // on the mod event bus
    public static void registerDataMapTypes(RegisterDataMapTypesEvent event) {
        System.out.println("registered data map");
        event.register(MOB_CONFIGURATION_DATA_MAP);
        event.register(ITEM_TIER_QI_CAPACITY);
    }

}
