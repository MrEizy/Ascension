package net.zic.ascension.api.ascension.value;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.api.rpg_engine.source.OriginSource;

import java.util.HashMap;
import java.util.Map;

public record ScaledValueContext(
        OriginSource source,
        Identifier skill,
        LivingEntity sourceEntity,
        LivingEntity targetEntity,
        double charge,
        Map<Identifier, Double> variables
) {
    public ScaledValueContext {
        variables = variables == null ? Map.of() : Map.copyOf(variables);
    }

    public static ScaledValueContext of(OriginSource source, Identifier skill) {
        return new ScaledValueContext(source, skill, null, null, 0.0D, Map.of());
    }

    public ScaledValueContext withCharge(double charge) {
        return new ScaledValueContext(source, skill, sourceEntity, targetEntity, charge, variables);
    }

    public ScaledValueContext withEntities(LivingEntity sourceEntity, LivingEntity targetEntity) {
        return new ScaledValueContext(source, skill, sourceEntity, targetEntity, charge, variables);
    }

    public ScaledValueContext withVariable(Identifier key, double value) {
        if (key == null) {
            return this;
        }
        Map<Identifier, Double> updatedVariables = new HashMap<>(variables);
        updatedVariables.put(key, value);
        return new ScaledValueContext(source, skill, sourceEntity, targetEntity, charge, updatedVariables);
    }

    public double getVariable(Identifier key, double fallback) {
        if (key == null) {
            return fallback;
        }
        return variables.getOrDefault(key, fallback);
    }
}
