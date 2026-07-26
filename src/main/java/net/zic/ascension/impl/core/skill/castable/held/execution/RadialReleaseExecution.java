package net.zic.ascension.impl.core.skill.castable.held.execution;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.zic.ascension.api.core.skill.castable.feature.SkillExecutionContext;
import net.zic.ascension.api.core.skill.castable.feature.SkillExecutionFeature;
import net.zic.ascension.api.core.skill.castable.held.execution.HeldCastExecution;
import net.zic.ascension.api.core.skill.castable.held.execution.HeldCastExecutionContext;
import net.zic.ascension.api.core.skill.castable.held.execution.HeldCastExecutionType;
import net.zic.ascension.api.core.skill.castable.held.execution.RadialTargetingDefinition;
import net.zic.ascension.api.core.targeting.SkillTarget;
import net.zic.ascension.api.core.targeting.TargetFilterDefinition;
import net.zic.ascension.api.core.targeting.TargetSort;
import net.zic.ascension.common.targeting.TargetingService;
import net.zic.ascension.impl.datapack.skill.castable.held.AscensionHeldCastExecutionTypes;

import java.util.List;

public record RadialReleaseExecution(
        RadialTargetingDefinition targeting,
        List<SkillExecutionFeature> originFeatures,
        List<SkillExecutionFeature> features
) implements HeldCastExecution {
    public static final MapCodec<RadialReleaseExecution> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            RadialTargetingDefinition.CODEC.fieldOf("targeting").forGetter(RadialReleaseExecution::targeting),
            SkillExecutionFeature.CODEC.listOf().optionalFieldOf("origin_features", List.of())
                    .forGetter(RadialReleaseExecution::originFeatures),
            SkillExecutionFeature.CODEC.listOf().optionalFieldOf("features", List.of())
                    .forGetter(RadialReleaseExecution::features)
    ).apply(instance, RadialReleaseExecution::new));

    public RadialReleaseExecution {
        originFeatures = originFeatures == null ? List.of() : List.copyOf(originFeatures);
        features = features == null ? List.of() : List.copyOf(features);
    }

    @Override
    public HeldCastExecutionType getType() {
        return AscensionHeldCastExecutionTypes.RADIAL_RELEASE.get();
    }

    @Override
    public void execute(HeldCastExecutionContext context) {
        SkillExecutionContext origin = context.featureContext(
                context.caster(),
                context.caster().position().add(0.0D, context.caster().getBbHeight() * 0.5D, 0.0D)
        );
        for (SkillExecutionFeature feature : originFeatures) {
            feature.apply(origin);
        }

        double radius = targeting.radius().resolve(context.scaledValueContext(null));
        TargetFilterDefinition filter = targeting.filter();
        List<SkillTarget> targets = TargetingService.radial(
                context.level(),
                context.caster(),
                context.caster().position().add(0.0D, context.caster().getBbHeight() * 0.5D, 0.0D),
                radius,
                filter,
                TargetSort.NEAREST,
                targeting.maximumTargets()
        );
        for (SkillTarget target : targets) {
            SkillExecutionContext release = context.featureContext(target.entity(), target.position());
            for (SkillExecutionFeature feature : features) {
                feature.apply(release);
            }
        }
    }
}
