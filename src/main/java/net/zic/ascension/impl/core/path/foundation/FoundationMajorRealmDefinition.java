package net.zic.ascension.impl.core.path.foundation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.zic.ascension.api.core.progression.ProgressActionHolder;
import net.zic.ascension.impl.core.path.MajorRealmDefinition;
import net.zic.ascension.impl.core.path.RealmDefinition;

import java.util.List;

public record FoundationMajorRealmDefinition(
        Component name,
        List<RealmDefinition> minorRealms,
        List<RealmDefinition> foundationRealms,
        ProgressActionHolder foundationActionHolder) {


    public static Codec<FoundationMajorRealmDefinition> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    ComponentSerialization.CODEC.fieldOf("name").forGetter(FoundationMajorRealmDefinition::name),
                    RealmDefinition.CODEC
                            .listOf()
                            .fieldOf("minor_realms")
                            .forGetter(FoundationMajorRealmDefinition::minorRealms),
                    RealmDefinition.CODEC
                            .listOf().
                            fieldOf("foundation_realms")
                            .forGetter(FoundationMajorRealmDefinition::foundationRealms),
                    ProgressActionHolder.PROGRESS_HOLDER_CODEC
                            .fieldOf("foundation_change_handler")
                            .forGetter(FoundationMajorRealmDefinition::foundationActionHolder)
            ).apply(instance, FoundationMajorRealmDefinition::new));


}