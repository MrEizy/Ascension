package net.zic.ascension.impl.core.skill.castable;

import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.skill.castable.action.SkillAction;
import net.zic.ascension.api.ascension.core.skill.castable.action.SkillActionContext;
import net.zic.ascension.api.ascension.core.targeting.TargetingDefinition;
import net.zic.ascension.impl.runtime.object.PersistentRuntimeVisuals;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class SkillActionRuntime {
    private SkillActionRuntime() {
    }

    public static void execute(SkillActionContext context, List<SkillAction> actions) {
        if (context == null || actions == null || actions.isEmpty()) {
            return;
        }
        Map<Identifier, Double> variables = new HashMap<>(context.variables());
        execute(context, actions, variables);
    }

    public static void restorePersistentVisuals(SkillActionContext context, List<SkillAction> actions) {
        if (context == null || actions == null || actions.isEmpty()) {
            return;
        }
        restorePersistentVisuals(context, actions, new HashMap<>(context.variables()));
    }

    public static void clearPersistentVisuals(SkillActionContext context, List<SkillAction> actions) {
        if (context == null || actions == null || actions.isEmpty()) {
            return;
        }
        clearPersistentVisuals(context, actions, new HashSet<>());
    }

    private static void restorePersistentVisuals(
            SkillActionContext base,
            List<SkillAction> actions,
            Map<Identifier, Double> variables
    ) {
        for (SkillAction action : actions) {
            if (action == null) {
                continue;
            }
            SkillActionContext context = base.withVariables(variables);
            if (action instanceof SkillActions.MasteryGate gate) {
                double progression = variables.getOrDefault(TargetingDefinition.Context.EFFECTIVE_PROGRESSION, 0.0D);
                if (progression >= gate.minimum().progression()) {
                    restorePersistentVisuals(base, gate.actions(), variables);
                }
            } else if (action instanceof SkillActions.Conditional conditional) {
                restorePersistentVisuals(
                        base,
                        conditional.condition() != null && conditional.condition().test(context)
                                ? conditional.ifTrue()
                                : conditional.ifFalse(),
                        variables
                );
            } else if (action instanceof SkillActions.Variable variable) {
                double value = variable.value().resolve(context.scaledValueContext());
                if (Double.isFinite(value)) {
                    double current = variables.getOrDefault(variable.variable(), 0.0D);
                    variables.put(variable.variable(), switch (variable.operation()) {
                        case SET -> value;
                        case ADD -> current + value;
                        case MULTIPLY -> current * value;
                    });
                }
            } else if (action instanceof SkillActions.PersistentVisual visual
                    && visual.action() == SkillActions.PersistentVisualAction.APPLY) {
                visual.apply(context);
            }
        }
    }

    private static void clearPersistentVisuals(
            SkillActionContext context,
            List<SkillAction> actions,
            Set<Identifier> cleared
    ) {
        for (SkillAction action : actions) {
            if (action instanceof SkillActions.PersistentVisual visual
                    && visual.action() == SkillActions.PersistentVisualAction.APPLY
                    && cleared.add(visual.key())) {
                PersistentRuntimeVisuals.remove(context, visual.key());
            } else if (action instanceof SkillActions.MasteryGate gate) {
                clearPersistentVisuals(context, gate.actions(), cleared);
            } else if (action instanceof SkillActions.Conditional conditional) {
                clearPersistentVisuals(context, conditional.ifTrue(), cleared);
                clearPersistentVisuals(context, conditional.ifFalse(), cleared);
            } else if (action instanceof SkillActions.Delay delay) {
                clearPersistentVisuals(context, delay.actions(), cleared);
            } else if (action instanceof SkillActions.Repeat repeat) {
                clearPersistentVisuals(context, repeat.actions(), cleared);
            }
        }
    }

    static void execute(SkillActionContext context, List<SkillAction> actions, Map<Identifier, Double> variables) {
        for (SkillAction action : actions) {
            execute(context, action, variables);
        }
    }

    static void execute(SkillActionContext base, SkillAction action, Map<Identifier, Double> variables) {
        if (action == null) {
            return;
        }
        SkillActionContext context = base.withVariables(variables);
        if (action instanceof SkillActions.MasteryGate gate) {
            double progression = variables.getOrDefault(TargetingDefinition.Context.EFFECTIVE_PROGRESSION, 0.0D);
            if (progression >= gate.minimum().progression()) {
                execute(base, gate.actions(), variables);
            }
            return;
        }
        if (action instanceof SkillActions.Conditional conditional) {
            execute(base, conditional.condition() != null && conditional.condition().test(context)
                    ? conditional.ifTrue()
                    : conditional.ifFalse(), variables);
            return;
        }
        if (action instanceof SkillActions.Variable variable) {
            double value = variable.value().resolve(context.scaledValueContext());
            if (!Double.isFinite(value)) {
                return;
            }
            double current = variables.getOrDefault(variable.variable(), 0.0D);
            variables.put(variable.variable(), switch (variable.operation()) {
                case SET -> value;
                case ADD -> current + value;
                case MULTIPLY -> current * value;
            });
            return;
        }
        if (action instanceof SkillActions.Delay delay) {
            SkillActionScheduler.schedule(context, delay.ticks(), delay.actions());
            return;
        }
        if (action instanceof SkillActions.Repeat repeat) {
            for (int i = 1; i <= repeat.times(); i++) {
                SkillActionScheduler.schedule(context, (long) repeat.interval() * i, repeat.actions());
            }
            return;
        }
        action.apply(context);
    }
}
