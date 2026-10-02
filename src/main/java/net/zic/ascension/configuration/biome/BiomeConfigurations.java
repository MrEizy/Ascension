package net.zic.ascension.configuration.biome;

import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.zenithlib.registry.RegistryHelper;

import static net.zic.ascension.configuration.ConfigurationRegistries.RAW_BIOME_CONFIGURATION_REGISTRY;

/**
 * Holds the ascension specific configurations of Biomes
 * <br>
 * Is constructed on a per world biome after resolving from datapack entries
 * <br>
 * therefore it is built on server launch
 * does not support ./reload
 *
 * uses an instance so we can pass that for dependency injection rather that referenceing a static instance
 */
@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public class BiomeConfigurations {

    private static BiomeConfigurations instance;

    //used while resolving configurations
    private static final Reference2ObjectMap<Holder<Biome>,BiomeConfiguration.Builder> builders = new Reference2ObjectOpenHashMap<>();


    public static BiomeConfigurations getInstance(){
        return instance;
    }

    @SubscribeEvent
    public static void onServerStarting(ServerAboutToStartEvent event){

        RegistryAccess access =event.getServer().registryAccess();
        Registry<RawBiomeConfiguration> rawConfigurations = RAW_BIOME_CONFIGURATION_REGISTRY.get(access);
        builders.clear();
        //TODO a lot of nested looping, see if i can make it more efficient
        for(RawBiomeConfiguration rawConfiguration : rawConfigurations){
            for(Holder<Biome> biome : rawConfiguration.biomes()){
                BiomeConfiguration.Builder builder =
                        builders.computeIfAbsent(biome, key -> new BiomeConfiguration.Builder());

                builder.setEnergyCap(rawConfiguration.energyCap());
                builder.setEnergyRegen(rawConfiguration.energyRegen());

                rawConfiguration.affinities().forEach(builder::addAffinity);
            }
        }

        Reference2ObjectOpenHashMap<Holder<Biome>,BiomeConfiguration> configurations = new Reference2ObjectOpenHashMap<>();

        builders.forEach((key,builder)->configurations.put(key,builder.build()));
        instance = new  BiomeConfigurations(configurations);
    }

    private final Reference2ObjectMap<Holder<Biome>,BiomeConfiguration> configurations;

    public BiomeConfigurations(Reference2ObjectMap<Holder<Biome>,BiomeConfiguration> frozenConfigurations){
        this.configurations = frozenConfigurations;
    }

    public BiomeConfiguration getConfiguration(Holder<Biome> holder){
        return configurations.get(holder);
    }
    public boolean hasConfiguration(Holder<Biome> holder){
        return configurations.containsKey(holder);
    }


}
