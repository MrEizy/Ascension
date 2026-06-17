package net.zic.ascension.impl.core.path;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.zic.ascension.api.core.tribulation.TribulationDefinition;
import net.zic.ascension.api.core.tribulation.TribulationDefinitionReference;

import java.util.Optional;

public record RealmDefinition(Component name, double progress, TribulationDefinitionReference tribulationReference){

    public static Codec<RealmDefinition> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    ComponentSerialization.CODEC.fieldOf("name").forGetter(RealmDefinition::name),
                    Codec.DOUBLE.fieldOf("progress").forGetter(RealmDefinition::progress),
                    TribulationDefinitionReference.CODEC.optionalFieldOf("tribulation").forGetter(
                            obj-> Optional.ofNullable(obj.tribulationReference))
            ).apply(instance, (name,progress,reference)->
                        new RealmDefinition(
                    name,
                    progress,
                    reference.orElse(null)
                )
            )

        );
}