package net.zic.ascension.impl.core.skill.castable;

import net.zic.ascension.api.ascension.core.targeting.TargetingDefinition;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.skill.castable.SkillExecutionDefinition;
import net.zic.ascension.api.ascension.core.skill.castable.action.SkillActionContext;
import net.zic.ascension.api.ascension.core.skill.castable.action.SkillAction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SkillExecutions {
    public static final Identifier TARGET_COUNT = AscensionCraft.prefix("execution/target_count");
    public static final Identifier TARGET_DISTANCE = AscensionCraft.prefix("execution/target_distance");
    public static final Identifier CAST_PROGRESS = AscensionCraft.prefix("cast/progress");
    public static final Identifier CAST_TICKS = AscensionCraft.prefix("cast/ticks");
    public static final Identifier MAXIMUM_CAST_TICKS = AscensionCraft.prefix("cast/maximum_ticks");
    public static final Identifier PROJECTILE_TRAVELLED = AscensionCraft.prefix("execution/projectile_travelled");
    public static final Identifier PROJECTILE_SPEED = AscensionCraft.prefix("execution/projectile_speed");
    public static final Identifier PROJECTILE_PIERCE_INDEX = AscensionCraft.prefix("execution/projectile_pierce_index");
    public static final Identifier PROJECTILE_RANGE_FRACTION = AscensionCraft.prefix("execution/projectile_range_fraction");
    public static final Identifier PROJECTILE_TICKS_LIVED = AscensionCraft.prefix("execution/projectile_ticks_lived");

    private SkillExecutions() {
    }

    public static Resolution resolve(
            ServerLevel level,
            LivingEntity caster,
            Identifier skill,
            int effectiveProgression,
            double charge,
            Map<Identifier, Double> variables,
            SkillExecutionDefinition definition
    ) {
        Map<Identifier, Double> resolvedVariables = new HashMap<>(variables == null ? Map.of() : variables);
        resolvedVariables.put(TargetingDefinition.Context.EFFECTIVE_PROGRESSION, (double) effectiveProgression);
        resolvedVariables.put(CAST_PROGRESS, charge);
        TargetingDefinition.Result targeting = definition.targeting().resolve(new TargetingDefinition.Context(
                level,
                caster,
                skill,
                effectiveProgression,
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
        TargetingDefinition.Target primary = targeting.primaryTarget();
        if (primary != null) {
            resolvedVariables.put(TARGET_DISTANCE, caster.getEyePosition().distanceTo(primary.position()));
        }
        return new Resolution(targeting.targets(), Map.copyOf(resolvedVariables), null);
    }

    public static void apply(
            ServerLevel level,
            LivingEntity caster,
            Identifier skill,
            double charge,
            SkillExecutionDefinition definition,
            Resolution resolution
    ) {
        Vec3 origin = caster.position().add(0.0D, caster.getBbHeight() * 0.5D, 0.0D);
        LivingEntity primary = primaryEntity(resolution);
        for (SkillAction action : definition.actions()) {
            applyAction(level, caster, skill, charge, resolution, origin, primary, action);
        }
    }

    private static void applyAction(
            ServerLevel level,
            LivingEntity caster,
            Identifier skill,
            double charge,
            Resolution resolution,
            Vec3 origin,
            LivingEntity primary,
            SkillAction action
    ) {
        if (action instanceof SkillActions.MasteryGate gate) {
            double progression = resolution.variables().getOrDefault(TargetingDefinition.Context.EFFECTIVE_PROGRESSION, 0.0D);
            if (progression >= gate.minimum().progression()) {
                for (SkillAction nested : gate.actions()) {
                    applyAction(level, caster, skill, charge, resolution, origin, primary, nested);
                }
            }
            return;
        }

        switch (action.subject()) {
            case CASTER, ORIGIN -> action.apply(new SkillActionContext(
                    level,
                    caster,
                    skill,
                    primary,
                    origin,
                    charge,
                    resolution.variables()
            ));
            case TARGET, POSITION -> {
                for (TargetingDefinition.Target target : resolution.targets()) {
                    Map<Identifier, Double> targetVariables = new HashMap<>(resolution.variables());
                    targetVariables.put(TARGET_DISTANCE, caster.getEyePosition().distanceTo(target.position()));
                    action.apply(new SkillActionContext(
                            level,
                            caster,
                            skill,
                            target.entity(),
                            target.position(),
                            charge,
                            targetVariables
                    ));
                }
            }
        }
    }

    public static LivingEntity primaryEntity(Resolution resolution) {
        return resolution.targets().isEmpty() ? null : resolution.targets().getFirst().entity();
    }

    public record Resolution(
            List<TargetingDefinition.Target> targets,
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
