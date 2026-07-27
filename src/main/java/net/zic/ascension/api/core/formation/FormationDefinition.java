package net.zic.ascension.api.core.formation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.value.ScaledValue;

import java.util.List;
import java.util.Optional;

public record FormationDefinition(
        ScaledValue duration,
        List<FormationAnchorDefinition> anchors,
        List<FormationLinkDefinition> links,
        boolean rotateWithCaster,
        Optional<Identifier> field,
        Optional<Identifier> visual
) {
    public static final Codec<FormationDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ScaledValue.CODEC.codec().fieldOf("duration").forGetter(FormationDefinition::duration),
            FormationAnchorDefinition.CODEC.listOf().fieldOf("anchors").forGetter(FormationDefinition::anchors),
            FormationLinkDefinition.CODEC.listOf().optionalFieldOf("links", List.of())
                    .forGetter(FormationDefinition::links),
            Codec.BOOL.optionalFieldOf("rotate_with_caster", true).forGetter(FormationDefinition::rotateWithCaster),
            Identifier.CODEC.optionalFieldOf("field").forGetter(FormationDefinition::field),
            Identifier.CODEC.optionalFieldOf("visual").forGetter(FormationDefinition::visual)
    ).apply(instance, FormationDefinition::new));

    public FormationDefinition {
        anchors = anchors == null ? List.of() : List.copyOf(anchors);
        links = links == null ? List.of() : List.copyOf(links);
        field = field == null ? Optional.empty() : field;
        visual = visual == null ? Optional.empty() : visual;
    }
}
