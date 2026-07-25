package net.zic.ascension.api.core.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;

public record SkillEffectDefinition(
        SkillEffectStackingPolicy stacking,
        List<SkillEffectModule> modules
) {
    public static final Codec<SkillEffectDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            SkillEffectStackingPolicy.CODEC.optionalFieldOf("stacking", SkillEffectStackingPolicy.STRONGER_REPLACES)
                    .forGetter(SkillEffectDefinition::stacking),
            SkillEffectModule.CODEC.listOf().optionalFieldOf("modules", List.of())
                    .forGetter(SkillEffectDefinition::modules)
    ).apply(instance, SkillEffectDefinition::new));

    public SkillEffectDefinition {
        modules = modules == null ? List.of() : List.copyOf(modules);
    }
}
