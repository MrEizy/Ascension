package net.zic.ascension.impl.core.path.realms;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.zic.ascension.api.ascension.core.path.realm.RealmDefinition;
import net.zic.ascension.api.ascension.core.tribulation.TribulationDefinitionReference;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public record MinorRealmDefinition(
        Component name,
        TribulationDefinitionReference tribulation,
        BreakthroughBehaviour breakthroughBehaviour,
        double maxProgress) implements RealmDefinition {
    @Override
    public Component getName() {
        return name;
    }

    @Override
    public @Nullable TribulationDefinitionReference getTribulation() {
        return tribulation;
    }

    @Override
    public double getMaxProgression() {
        return maxProgress;
    }

    public static Codec<MinorRealmDefinition> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                            ComponentSerialization.CODEC.fieldOf("name").forGetter(MinorRealmDefinition::name),
                            Codec.DOUBLE.fieldOf("progress").forGetter(MinorRealmDefinition::maxProgress),
                            TribulationDefinitionReference.CODEC.optionalFieldOf("tribulation").forGetter(
                                    obj-> Optional.ofNullable(obj.tribulation)),
                            Codec.either(Codec.INT,Codec.STRING).optionalFieldOf("breakthrough_behaviour", Either.right("instant")).forGetter(
                                    obj->{
                                        if(obj.breakthroughBehaviour == BreakthroughBehaviour.INSTANT) return Either.right("instant");
                                        else if(obj.breakthroughBehaviour == BreakthroughBehaviour.NONE) return Either.right("none");
                                        return Either.left(obj.breakthroughBehaviour.delayTicks());
                                    }
                            )
                    ).apply(instance, (name,progress,reference,behaviour)->
                            new
                                    MinorRealmDefinition(
                                    name,
                                    reference.orElse(null),
                                    behaviour.map(BreakthroughBehaviour::new,type->type.equals("instant") ? BreakthroughBehaviour.INSTANT:BreakthroughBehaviour.NONE),
                                    progress
                            )
                    )

            );
}
