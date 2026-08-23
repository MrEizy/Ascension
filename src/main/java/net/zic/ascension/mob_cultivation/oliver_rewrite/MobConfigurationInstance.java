package net.zic.ascension.mob_cultivation.oliver_rewrite;

import net.minecraft.core.RegistryAccess;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.configuration.mob_traits.MobTraitReference;
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
    private final List<MobTraitReference> traits = new ArrayList<>();

    public MobConfigurationInstance(){
        tier =MobCultivationEliteTier.NORMAL;
    }
    public MobConfigurationInstance(MobCultivationEliteTier tier, List<MobTraitReference> traits){
        this.tier = tier;
        this.traits.addAll(traits);
    }
    public MobCultivationEliteTier getTier(){return tier;}
    public List<MobTraitReference> getTraits(){return traits;}
    public void applyToMob(Mob mob){
        for(MobTraitReference trait : traits) trait.getTrait(mob.registryAccess()).applyToMob(mob);
    }
    public void applyToSource(OriginSource source){
        for(MobTraitReference trait : traits) trait.getTrait(source.getRegistryAccess()).applyToSource(source);
    }

    public void removeFromMob(Mob mob){
        for(MobTraitReference trait : traits) trait.getTrait(mob.registryAccess()).removeFromMob(mob);
    }
    public void removeFromSource(OriginSource source){
        for(MobTraitReference trait : traits) trait.getTrait(source.getRegistryAccess()).removeFromSource(source);

    }

    public void write(ValueOutput output, RegistryAccess access){
        output.putString("tier",tier.name());
        ValueOutput.ValueOutputList traits = output.childrenList("traits");
        for(MobTraitReference trait : this.traits){
            ValueOutput traitOutput = traits.addChild();
            trait.write(traitOutput);
        }
    }
    public void read(ValueInput input,RegistryAccess access){
        tier = MobCultivationEliteTier.valueOf(input.getStringOr("tier","NORMAL"));
        traits.clear();
        ValueInput.ValueInputList inputs = input.childrenListOrEmpty("traits");
        for(ValueInput traitInput : inputs){
            try {
                MobTraitReference ref = MobTraitReference.of(traitInput);
                if(ref.isValid(access)) traits.add(ref);

            }catch (Exception e){
                AscensionCraft.LOGGER.debug("unable to load mob trait");
            }
        }
    }


}
