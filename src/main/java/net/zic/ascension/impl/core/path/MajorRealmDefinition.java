package net.zic.ascension.impl.core.path;


import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;

import java.util.List;

public record MajorRealmDefinition(Component name, List<RealmDefinition> minorRealms) {


    public static Codec<MajorRealmDefinition> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    ComponentSerialization.CODEC.fieldOf("name").forGetter(MajorRealmDefinition::name),
                    RealmDefinition.CODEC
                            .listOf()
                            .fieldOf("minor_realms")
                            .forGetter(MajorRealmDefinition::minorRealms)
            ).apply(instance, MajorRealmDefinition::new));


}