package net.zic.ascension.configuration;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.DimensionTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.configuration.biome.v2.BiomeConfiguration;
import net.zic.ascension.configuration.dimension.v2.DimensionConfiguration;
import net.zic.ascension.configuration.item.qi_capacity.TierCapacityDefinition;
import net.zic.ascension.configuration.mobs.MobConfiguration;
@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public class ConfigurationDataMaps {
    public static final DataMapType<EntityType<?>, MobConfiguration> MOB_CONFIGURATION = DataMapType.builder(
            Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "mob_configuration"),
            Registries.ENTITY_TYPE,
            MobConfiguration.CODEC
    ).build();
    public static final DataMapType<Item, TierCapacityDefinition> ITEM_TIER_QI_CAPACITY = DataMapType.builder(
            Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID,"tier_capacity"),
            Registries.ITEM,
            TierCapacityDefinition.CODEC
    ).build();
    public static final DataMapType<Biome, BiomeConfiguration> BIOME_CONFIGURATION = DataMapType.builder(
            Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID,"biome_configuration"),
            Registries.BIOME,
            BiomeConfiguration.CODEC
    ).build();

    public static final DataMapType<Level, DimensionConfiguration> DIMENSION_CONFIGURATION = DataMapType.builder(
            Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID,"dimension_configuration"),
            Registries.DIMENSION,
            DimensionConfiguration.CODEC
    ).build();

    @SubscribeEvent // on the mod event bus
    public static void registerDataMapTypes(RegisterDataMapTypesEvent event) {
        System.out.println("registered data map");
        event.register(MOB_CONFIGURATION);
        event.register(ITEM_TIER_QI_CAPACITY);
        event.register(BIOME_CONFIGURATION);
        event.register(DIMENSION_CONFIGURATION);
    }

}
