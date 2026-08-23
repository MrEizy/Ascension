package net.zic.ascension.configuration.mobs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.Mob;
import net.zic.ascension.configuration.mob_traits.MobTraitDefinition;
import net.zic.ascension.configuration.mob_traits.MobTraitDefinitionType;
import net.zic.ascension.configuration.mob_traits.MobTraitReference;
import net.zic.ascension.configuration.mob_traits.traits.SimpleTraitDefinition;
import net.zic.ascension.configuration.mobs.condition.MobConfigurationCondition;
import net.zic.ascension.configuration.mobs.condition.MobConfigurationConditionType;

import java.util.List;

public record PotentialTrait(MobTraitReference trait, List<MobConfigurationCondition> conditions, double chance){
    public static final Codec<PotentialTrait> CODEC = RecordCodecBuilder.create(
            instance->instance.group(
                    MobTraitReference.CODEC.fieldOf("trait").forGetter(PotentialTrait::trait),
                    MobConfigurationConditionType.MOB_CONFIGURATION_CONDITION_CODEC.listOf().optionalFieldOf("conditions",List.of()).forGetter(PotentialTrait::conditions),
                    Codec.doubleRange(0,1).fieldOf("chance").forGetter(PotentialTrait::chance)
            ).apply(instance, PotentialTrait::new)
    );

    public boolean test(Mob mob){
        for(MobConfigurationCondition condition : conditions) {
            if(!condition.test(mob)) return false;
        }
        return true;
    }
}