package net.zic.ascension.impl.core.skill.castable;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.skill.castable.SkillExecutionDefinition;
import net.zic.ascension.api.ascension.core.skill.castable.feature.SkillExecutionContext;
import net.zic.ascension.api.ascension.core.skill.castable.feature.SkillExecutionFeature;
import net.zic.ascension.api.ascension.core.targeting.SkillTarget;
import net.zic.ascension.api.ascension.core.targeting.TargetingContext;
import net.zic.ascension.api.ascension.core.targeting.TargetingResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SkillExecutions {
    public static final Identifier TARGET_COUNT = AscensionCraft.prefix("execution/target_count");
    public static final Identifier TARGET_DISTANCE = AscensionCraft.prefix("execution/target_distance");
    public static final Identifier CHARGE_TICKS = AscensionCraft.prefix("execution/charge_ticks");
    public static final Identifier MAXIMUM_CHARGE_TICKS = AscensionCraft.prefix("execution/maximum_charge_ticks");

    private SkillExecutions() {
    }

    public static Resolution resolve(
            ServerLevel level,
            ServerPlayer caster,
            Identifier skill,
            int effectiveLevel,
            double charge,
            Map<Identifier, Double> variables,
            SkillExecutionDefinition definition
    ) {
        Map<Identifier, Double> resolvedVariables = new HashMap<>(variables == null ? Map.of() : variables);
        resolvedVariables.put(TargetingContext.EFFECTIVE_LEVEL, (double) effectiveLevel);
        TargetingResult targeting = definition.targeting().resolve(new TargetingContext(
                level,
                caster,
                skill,
                effectiveLevel,
                charge,
                resolvedVariables
        ));
        if (!targeting.succeeded()) {
            return Resolution.failure(targeting.failureMessage());
        }
        if (definition.requireTargets() && targeting.targets().isEmpty()) {
            return Resolution.failure(Component.literal("No valid target"));
        }
        resolvedVariables.put(TARGET_COUNT, (double) targeting.targets().size());
        SkillTarget primary = targeting.primaryTarget();
        if (primary != null) {
            resolvedVariables.put(TARGET_DISTANCE, caster.getEyePosition().distanceTo(primary.position()));
        }
        return new Resolution(targeting.targets(), Map.copyOf(resolvedVariables), null);
    }

    public static void apply(
            ServerLevel level,
            ServerPlayer caster,
            Identifier skill,
            double charge,
            SkillExecutionDefinition definition,
            Resolution resolution
    ) {
        Vec3 origin = caster.position().add(0.0D, caster.getBbHeight() * 0.5D, 0.0D);
        SkillExecutionContext casterContext = new SkillExecutionContext(
                level,
                caster,
                skill,
                caster,
                origin,
                charge,
                resolution.variables()
        );
        for (SkillExecutionFeature feature : definition.casterFeatures()) {
            feature.apply(casterContext);
        }
        for (SkillTarget target : resolution.targets()) {
            Map<Identifier, Double> targetVariables = new HashMap<>(resolution.variables());
            targetVariables.put(TARGET_DISTANCE, caster.getEyePosition().distanceTo(target.position()));
            SkillExecutionContext targetContext = new SkillExecutionContext(
                    level,
                    caster,
                    skill,
                    target.entity(),
                    target.position(),
                    charge,
                    targetVariables
            );
            for (SkillExecutionFeature feature : definition.targetFeatures()) {
                feature.apply(targetContext);
            }
        }
    }

    public static LivingEntity primaryEntity(Resolution resolution) {
        return resolution.targets().isEmpty() ? null : resolution.targets().getFirst().entity();
    }

    public record Resolution(
            List<SkillTarget> targets,
            Map<Identifier, Double> variables,
            Component failureMessage
    ) {
        public Resolution {
            targets = targets == null ? List.of() : List.copyOf(targets);
            variables = variables == null ? Map.of() : Map.copyOf(variables);
        }

        public boolean succeeded() {
            return failureMessage == null;
        }

        private static Resolution failure(Component message) {
            return new Resolution(List.of(), Map.of(), message);
        }
    }
}
