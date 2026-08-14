package net.zic.ascension.configuration.mobs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.zic.ascension.configuration.mob_traits.MobTraitDefinition;
import net.zic.ascension.configuration.mob_traits.MobTraitDefinitionType;
import net.zic.ascension.configuration.mob_traits.MobTraitReference;

public record PotentialTrait(MobTraitReference trait, double chance){
    public static final Codec<PotentialTrait> CODEC = RecordCodecBuilder.create(
            instance->instance.group(
                    MobTraitReference.CODEC.fieldOf("trait").forGetter(PotentialTrait::trait),
                    Codec.doubleRange(0,1).fieldOf("chance").forGetter(PotentialTrait::chance)
            ).apply(instance, PotentialTrait::new)
    );
}