package net.zic.ascension.impl.core.skill.castable.held.execution;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.api.core.skill.castable.held.execution.HeldCastExecution;
import net.zic.ascension.api.core.skill.castable.held.execution.HeldCastExecutionContext;
import net.zic.ascension.api.core.skill.castable.held.execution.HeldCastExecutionType;
import net.zic.ascension.api.core.skill.castable.held.execution.RadialTargetingDefinition;
import net.zic.ascension.api.core.skill.castable.held.feature.HeldCastReleaseContext;
import net.zic.ascension.api.core.skill.castable.held.feature.HeldCastReleaseFeature;
import net.zic.ascension.impl.datapack.skill.castable.held.AscensionHeldCastExecutionTypes;

import java.util.Comparator;
import java.util.List;

public record RadialReleaseExecution(
        RadialTargetingDefinition targeting,
        List<HeldCastReleaseFeature> originFeatures,
        List<HeldCastReleaseFeature> features
) implements HeldCastExecution {
    public static final MapCodec<RadialReleaseExecution> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            RadialTargetingDefinition.CODEC.fieldOf("targeting").forGetter(RadialReleaseExecution::targeting),
            HeldCastReleaseFeature.CODEC.listOf().optionalFieldOf("origin_features", List.of())
                    .forGetter(RadialReleaseExecution::originFeatures),
            HeldCastReleaseFeature.CODEC.listOf().optionalFieldOf("features", List.of())
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
        HeldCastReleaseContext origin = new HeldCastReleaseContext(
                context,
                context.caster(),
                context.caster().position().add(0.0D, context.caster().getBbHeight() * 0.5D, 0.0D)
        );
        for (HeldCastReleaseFeature feature : originFeatures) {
            feature.apply(origin);
        }

        double radius = targeting.radius().resolve(context.scaledValueContext(null));
        if (!Double.isFinite(radius) || radius <= 0.0D) {
            return;
        }

        List<LivingEntity> targets = context.level().getEntitiesOfClass(
                LivingEntity.class,
                context.caster().getBoundingBox().inflate(radius),
                target -> !target.isRemoved()
                        && targeting.matches(context.caster(), target)
                        && (!targeting.requireLineOfSight() || context.caster().hasLineOfSight(target))
        );
        targets.sort(Comparator.comparingDouble(context.caster()::distanceToSqr));

        int limit = targeting.maximumTargets() <= 0
                ? targets.size()
                : Math.min(targeting.maximumTargets(), targets.size());
        for (int index = 0; index < limit; index++) {
            LivingEntity target = targets.get(index);
            HeldCastReleaseContext release = new HeldCastReleaseContext(
                    context,
                    target,
                    target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D)
            );
            for (HeldCastReleaseFeature feature : features) {
                feature.apply(release);
            }
        }
    }
}
