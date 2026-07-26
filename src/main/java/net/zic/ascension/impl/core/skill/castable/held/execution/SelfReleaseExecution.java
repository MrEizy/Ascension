package net.zic.ascension.impl.core.skill.castable.held.execution;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.zic.ascension.api.core.skill.castable.feature.SkillExecutionContext;
import net.zic.ascension.api.core.skill.castable.feature.SkillExecutionFeature;
import net.zic.ascension.api.core.skill.castable.held.execution.HeldCastExecution;
import net.zic.ascension.api.core.skill.castable.held.execution.HeldCastExecutionContext;
import net.zic.ascension.api.core.skill.castable.held.execution.HeldCastExecutionType;
import net.zic.ascension.impl.datapack.skill.castable.held.AscensionHeldCastExecutionTypes;

import java.util.List;

public record SelfReleaseExecution(List<SkillExecutionFeature> features) implements HeldCastExecution {
    public static final MapCodec<SelfReleaseExecution> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            SkillExecutionFeature.CODEC.listOf().optionalFieldOf("features", List.of())
                    .forGetter(SelfReleaseExecution::features)
    ).apply(instance, SelfReleaseExecution::new));

    public SelfReleaseExecution {
        features = features == null ? List.of() : List.copyOf(features);
    }

    @Override
    public HeldCastExecutionType getType() {
        return AscensionHeldCastExecutionTypes.SELF_RELEASE.get();
    }

    @Override
    public void execute(HeldCastExecutionContext context) {
        SkillExecutionContext release = context.featureContext(
                context.caster(),
                context.caster().position().add(0.0D, context.caster().getBbHeight() * 0.5D, 0.0D)
        );
        for (SkillExecutionFeature feature : features) {
            feature.apply(release);
        }
    }
}
