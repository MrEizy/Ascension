package net.zic.ascension.mob_cultivation.oliver_rewrite;

import net.minecraft.core.RegistryAccess;
import net.minecraft.server.level.DistanceManager;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.configuration.mob_traits.MobTraitDefinition;
import net.zic.ascension.configuration.mob_traits.MobTraitDefinitionType;
import net.zic.ascension.configuration.mobs.MobConfiguration;
import net.zic.ascension.configuration.mobs.MobTierDefinition;
import net.zic.ascension.configuration.mobs.PotentialTrait;
import net.zic.ascension.mob_cultivation.generation.MobCultivationEliteTier;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Holds the resolved configuration details of a mob.
 * this is then applied during the load.
 *
 * for applyToOrigin source that should be done by listening to OriginSourceEvent.OriginSourceFinishedLoadingEvent
 * that way cached paths and skills are still present
 */
public class MobConfigurationInstance {

    private MobCultivationEliteTier tier;
    private final List<MobTraitDefinition> definitions = new ArrayList<>();

    public MobConfigurationInstance(){
        tier =MobCultivationEliteTier.NORMAL;
    }
    public MobConfigurationInstance(MobCultivationEliteTier tier, List<MobTraitDefinition> definitions){
        this.tier = tier;
        this.definitions.addAll(definitions);
    }

    public void applyToMob(Mob mob){
        for(MobTraitDefinition definition : definitions) definition.applyToMob(mob);
    }
    public void applyToSource(OriginSource source){
        for(MobTraitDefinition definition : definitions) definition.applyToSource(source);
    }


    public void write(ValueOutput output, RegistryAccess access){
        output.putString("tier",tier.name());
        ValueOutput.ValueOutputList traits = output.childrenList("traits");
        for(MobTraitDefinition definition : definitions){
            ValueOutput traitOutput = traits.addChild();
            traitOutput.store("trait", MobTraitDefinitionType.MOB_TRAIT_CODEC,definition);
        }
    }
    public void read(ValueInput input,RegistryAccess access){
        tier = MobCultivationEliteTier.valueOf(input.getStringOr("tier","NORMAL"));
        definitions.clear();
        ValueInput.ValueInputList inputs = input.childrenListOrEmpty("traits");
        for(ValueInput definitionInput : inputs){
            try {
                MobTraitDefinition definition = definitionInput.read("trait",MobTraitDefinitionType.MOB_TRAIT_CODEC).get();
                definitions.add(definition);

            }catch (Exception e){
                AscensionCraft.LOGGER.debug("unable to load mob trait definition");
            }
        }
    }
    public void freshApply(Mob mob){
        for(MobTraitDefinition definition : definitions) definition.initializeTrait(mob);
    }
    //only generates the traits and tiers, does not apply them
    public static MobConfigurationInstance generateInstance(MobConfiguration configuration,Mob mob){
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

        List<MobTraitDefinition> traits = new ArrayList<>();
        for(PotentialTrait potentialTrait : configuration.getPotentialTraits(mobTier)){
            if(ThreadLocalRandom.current().nextDouble(1) <= potentialTrait.chance()) traits.add(potentialTrait.trait().resolve(mob.registryAccess()));
        }
        return new MobConfigurationInstance(mobTier,traits);
    }
}
