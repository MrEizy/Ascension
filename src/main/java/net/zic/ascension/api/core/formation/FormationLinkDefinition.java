package net.zic.ascension.api.core.formation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

public record FormationLinkDefinition(
        Identifier from,
        Identifier to
) {
    public static final Codec<FormationLinkDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("from").forGetter(FormationLinkDefinition::from),
            Identifier.CODEC.fieldOf("to").forGetter(FormationLinkDefinition::to)
    ).apply(instance, FormationLinkDefinition::new));
}
