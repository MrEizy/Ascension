package net.zic.ascension.configuration.mob_traits.traits.cultivation_traits;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record PotentialRealmDefinition(int realm, int minMinorRealm, int maxMinorRealm, int weight){
    public static final Codec<PotentialRealmDefinition> CODEC = RecordCodecBuilder.create(
            instance->instance.group(
                    Codec.INT.fieldOf("major_realm").forGetter(PotentialRealmDefinition::realm),
                    Codec.INT.fieldOf("min_realm").forGetter(PotentialRealmDefinition::minMinorRealm),
                    Codec.INT.fieldOf("max_realm").forGetter(PotentialRealmDefinition::maxMinorRealm),
                    Codec.INT.fieldOf("weight").forGetter(PotentialRealmDefinition::weight)
                    ).apply(instance,PotentialRealmDefinition::new)
    );
}
