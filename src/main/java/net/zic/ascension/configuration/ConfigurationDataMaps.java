package net.zic.ascension.configuration;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.util.ModTags;
import net.zic.ascension.configuration.mobs.MobConfiguration;
@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public class ConfigurationDataMaps {
    public static final DataMapType<EntityType<?>, MobConfiguration> MOB_CONFIGURATION_DATA_MAP = DataMapType.builder(
            // The ID of the data map. Data map files for this data map will be located at
            // <yourmodid>:examplemod/data_maps/item/example_data.json.
            Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "mob_configuration"),
            // The registry to register the data map for.
            Registries.ENTITY_TYPE,
            // The codec of the data map entries.
            MobConfiguration.CODEC
    ).build();


    @SubscribeEvent // on the mod event bus
    public static void registerDataMapTypes(RegisterDataMapTypesEvent event) {
        System.out.println("registered data map");
        event.register(MOB_CONFIGURATION_DATA_MAP);

    }
    @SubscribeEvent
    public static void onSpawn(FinalizeSpawnEvent event){
        if(event.getEntity().getType().equals(EntityType.ZOMBIE)){
            System.out.println("spawned zombie");
            System.out.println(event.getEntity().getData(MOB_CONFIGURATION_DATA_MAP));
            System.out.println(BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(event.getEntity().getType()).getData(MOB_CONFIGURATION_DATA_MAP));
        }
        if(event.getEntity().getData(MOB_CONFIGURATION_DATA_MAP) != null){
            System.out.println("found mob of type "+event.getEntity().getType());
        }
    }
}
