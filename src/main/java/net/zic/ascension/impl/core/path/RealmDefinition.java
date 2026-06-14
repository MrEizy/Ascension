package net.zic.ascension.impl.core.path;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;

public record RealmDefinition(Component name, double progress){

    public static Codec<RealmDefinition> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    ComponentSerialization.CODEC.fieldOf("name").forGetter(RealmDefinition::name),
                    Codec.DOUBLE.fieldOf("progress").forGetter(RealmDefinition::progress)
            ).apply(instance, RealmDefinition::new));
}