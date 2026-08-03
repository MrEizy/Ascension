package net.zic.ascension.configuration.dimension;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.zenithlib.registry.RegistryHelper;

import java.util.HashMap;
import java.util.Map;

//TODO update to use raw identifier keys, rather than Holder<Level>
@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public class DimensionConfigurations {
    private static DimensionConfigurations instance;

    //used while resolving configurations
    private static final Map<Identifier, DimensionConfiguration.Builder> builders = new HashMap<>();

    private static final RegistryHelper.DataPackRegistry<RawDimensionConfiguration> RAW_CONFIGURATION_REGISTRY =
            new RegistryHelper.DataPackRegistry<>(RegistryHelper.key(AscensionCraft.MOD_ID,"dimension_configurations"),()->RawDimensionConfiguration.CODEC);

    public static DimensionConfigurations getInstance(){
        return instance;
    }

    @SubscribeEvent
    public static void onServerStarting(ServerAboutToStartEvent event){

        RegistryAccess access =event.getServer().registryAccess();
        Registry<RawDimensionConfiguration> rawConfigurations = RAW_CONFIGURATION_REGISTRY.get(access);
        builders.clear();
        //TODO a lot of nested looping, see if i can make it more efficient
        for(RawDimensionConfiguration rawConfiguration : rawConfigurations){
            for(Identifier dimension : rawConfiguration.dimensions()){
                DimensionConfiguration.Builder builder =
                        builders.computeIfAbsent(dimension, key -> new DimensionConfiguration.Builder());

                builder.setEnergyCap(rawConfiguration.energyCap());
                builder.setEnergyRegen(rawConfiguration.energyRegen());

                rawConfiguration.affinities().forEach(builder::addAffinity);
            }
        }

        Map<Identifier,DimensionConfiguration> configurations = new HashMap<>();

        builders.forEach((key,builder)->configurations.put(key,builder.build()));
        instance = new  DimensionConfigurations(configurations);
    }
    @SubscribeEvent
    public static void registerDatapackRegistries(DataPackRegistryEvent.NewRegistry event) {

        AscensionCraft.LOGGER.info("Creating Dimension Configuration Registry");
        event.dataPackRegistry(
                RAW_CONFIGURATION_REGISTRY.key(),
                RAW_CONFIGURATION_REGISTRY.codec().get(),
                RAW_CONFIGURATION_REGISTRY.codec().get()
        );
        AscensionCraft.LOGGER.info("Finished Creating Biome Configuration Registry");

    }

    private final Map<Identifier,DimensionConfiguration> configurations;

    public DimensionConfigurations(Map<Identifier,DimensionConfiguration> frozenConfigurations){

        this.configurations = Map.copyOf(frozenConfigurations);
    }

    public DimensionConfiguration getConfiguration(Identifier dimension){
        return configurations.get(dimension);
    }
    public boolean hasConfiguration(Identifier dimension){
        return configurations.containsKey(dimension);
    }


}
