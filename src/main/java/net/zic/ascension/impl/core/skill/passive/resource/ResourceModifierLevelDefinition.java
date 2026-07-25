package net.zic.ascension.impl.core.skill.passive.resource;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.zic.ascension.api.core.resource.modifier.ResourceModifierDefinition;

import java.util.List;

public record ResourceModifierLevelDefinition(
        List<ResourceModifierDefinition> modifiers
) {
    public static final MapCodec<ResourceModifierLevelDefinition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceModifierDefinition.CODEC.codec().listOf().fieldOf("modifiers")
                    .forGetter(ResourceModifierLevelDefinition::modifiers)
    ).apply(instance, ResourceModifierLevelDefinition::new));

    public ResourceModifierLevelDefinition {
        modifiers = modifiers == null ? List.of() : List.copyOf(modifiers);
    }
}
