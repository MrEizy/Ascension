package net.zic.ascension.mob_cultivation.oliver_rewrite;

import net.minecraft.world.entity.Mob;
import net.zic.ascension.api.ascension.capabilities.AscensionEntityDataProvider;
import net.zic.ascension.api.ascension.capabilities.CoreCapabilities;
import net.zic.ascension.common.data_attachements.AscensionAttachments;
import net.zic.ascension.configuration.ConfigurationDataMaps;
import net.zic.ascension.configuration.mob_traits.MobTraitReference;
import net.zic.ascension.configuration.mobs.MobConfiguration;
import net.zic.ascension.configuration.mobs.MobTierDefinition;
import net.zic.ascension.configuration.mobs.PotentialTrait;
import net.zic.ascension.mob_cultivation.generation.MobCultivationEliteTier;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class MobSpawnHelper {

    public static boolean hasConfig(Mob mob){
        return mob.getData(ConfigurationDataMaps.MOB_CONFIGURATION_DATA_MAP) != null;
    }
    public static MobConfiguration getConfig(Mob mob){
        return mob.getData(ConfigurationDataMaps.MOB_CONFIGURATION_DATA_MAP);
    }
    public static boolean hasConfigHolder(Mob mob){
        return mob.hasData(AscensionAttachments.MOB_CONFIG_HOLDER);
    }
    public static MobConfigurationHolder getConfigHolder(Mob mob){
        return  mob.getData(AscensionAttachments.MOB_CONFIG_HOLDER);
    }


    public static void buildConfigurationInstance(Mob mob){
        if(!hasConfig(mob)) return;

        System.out.println("attaching config instance");
        MobConfiguration config = getConfig(mob);
        MobConfigurationInstance configInstance = generateInstance(config,mob);

        MobConfigurationHolder configurableMobData = getConfigHolder(mob);

        configurableMobData.setMobConfigurationInstance(configInstance);

    }
    public static void applyConfigurationInstance(Mob mob){
        if(!hasConfigHolder(mob)) return;
        getConfigHolder(mob).applyConfigurationToEntity();
    }
    public static MobConfigurationInstance generateInstance(MobConfiguration configuration,Mob mob){
        //TODO update to create a new weight for each tier by using conditions
        int totalWeight = 0;
        for (MobTierDefinition definition : configuration.tierDefinitions()) totalWeight += definition.weight();
        int roll = ThreadLocalRandom.current().nextInt(totalWeight);
        totalWeight = 0;
        MobCultivationEliteTier mobTier = MobCultivationEliteTier.NORMAL;
        for(MobTierDefinition definition : configuration.tierDefinitions()){
            totalWeight += definition.weight();
            if(roll <= totalWeight){
                mobTier = definition.tier();
                break;
            }
        }

        List<MobTraitReference> traits = new ArrayList<>();
        for(PotentialTrait potentialTrait : configuration.getPotentialTraits(mobTier)){
            if(!potentialTrait.test(mob)) continue;
            if(ThreadLocalRandom.current().nextDouble(1) <= potentialTrait.chance()) traits.add(potentialTrait.trait());
        }
        return new MobConfigurationInstance(mobTier,traits);
    }
}
