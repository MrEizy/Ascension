package net.zic.ascension.api.ascension.core.skill.castable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.zic.ascension.api.ascension.core.skill.castable.feature.SkillExecutionFeature;
import net.zic.ascension.api.ascension.core.targeting.TargetingDefinition;
import net.zic.ascension.api.ascension.value.ScaledValue;

import java.util.List;
import java.util.Optional;

public record ActiveSkillLevelDefinition(
        SkillExecutionDefinition execution,
        List<ActiveSkillCostDefinition> costs,
        ScaledValue cooldown
) {
    public ActiveSkillLevelDefinition {
        costs = costs == null ? List.of() : List.copyOf(costs);
        cooldown = cooldown == null ? ScaledValue.constant(0.0D) : cooldown;
    }

    public record Template(
            Optional<TargetingDefinition> targeting,
            Optional<Boolean> requireTargets,
            Optional<List<SkillExecutionFeature>> features,
            Optional<List<ActiveSkillCostDefinition>> costs,
            Optional<ScaledValue> cooldown
    ) {
        public static final MapCodec<Template> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                TargetingDefinition.CODEC.optionalFieldOf("targeting").forGetter(Template::targeting),
                Codec.BOOL.optionalFieldOf("require_targets").forGetter(Template::requireTargets),
                SkillExecutionFeature.CODEC.listOf().optionalFieldOf("features").forGetter(Template::features),
                ActiveSkillCostDefinition.CODEC.codec().listOf().optionalFieldOf("costs").forGetter(Template::costs),
                ScaledValue.COMPACT_CODEC.optionalFieldOf("cooldown").forGetter(Template::cooldown)
        ).apply(instance, Template::new));

        public Template {
            targeting = targeting == null ? Optional.empty() : targeting;
            requireTargets = requireTargets == null ? Optional.empty() : requireTargets;
            features = features == null ? Optional.empty() : features.map(List::copyOf);
            costs = costs == null ? Optional.empty() : costs.map(List::copyOf);
            cooldown = cooldown == null ? Optional.empty() : cooldown;
        }

        public ActiveSkillLevelDefinition resolve(ActiveSkillLevelDefinition previous) {
            TargetingDefinition resolvedTargeting = targeting.orElseGet(() -> previous == null ? null : previous.execution().targeting());
            if (resolvedTargeting == null) {
                throw new IllegalArgumentException("An active skill level requires targeting");
            }
            boolean resolvedRequireTargets = requireTargets.orElse(previous == null || previous.execution().requireTargets());
            List<SkillExecutionFeature> resolvedFeatures = features.orElseGet(
                    () -> previous == null ? List.of() : previous.execution().features()
            );
            List<ActiveSkillCostDefinition> resolvedCosts = costs.orElseGet(
                    () -> previous == null ? List.of() : previous.costs()
            );
            ScaledValue resolvedCooldown = cooldown.orElseGet(
                    () -> previous == null ? ScaledValue.constant(0.0D) : previous.cooldown()
            );
            return new ActiveSkillLevelDefinition(
                    new SkillExecutionDefinition(resolvedTargeting, resolvedRequireTargets, resolvedFeatures),
                    resolvedCosts,
                    resolvedCooldown
            );
        }

        public static Template from(ActiveSkillLevelDefinition definition) {
            return new Template(
                    Optional.of(definition.execution().targeting()),
                    Optional.of(definition.execution().requireTargets()),
                    Optional.of(definition.execution().features()),
                    Optional.of(definition.costs()),
                    Optional.of(definition.cooldown())
            );
        }
    }
}
