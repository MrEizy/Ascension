package net.zic.ascension.api.ascension.core.skill.castable.feature;

import net.zic.ascension.api.ascension.value.ScaledValue;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;

import java.util.Map;

public record SkillExecutionContext(
        ServerLevel level,
        LivingEntity caster,
        Identifier skill,
        LivingEntity target,
        Vec3 position,
        double charge,
        Map<Identifier, Double> variables,
        SkillExecutionAttribution attribution
) {
    public SkillExecutionContext(ServerLevel level, LivingEntity caster, Identifier skill, LivingEntity target, Vec3 position, double charge, Map<Identifier, Double> variables) {
        this(level, caster, skill, target, position, charge, variables, SkillExecutionAttribution.direct(caster));
    }

    public SkillExecutionContext {
        position = position == null
                ? caster.position().add(0.0D, caster.getBbHeight() * 0.5D, 0.0D)
                : position;
        charge = Double.isFinite(charge) ? Math.clamp(charge, 0.0D, 1.0D) : 0.0D;
        variables = variables == null ? Map.of() : Map.copyOf(variables);
        attribution = attribution == null ? SkillExecutionAttribution.direct(caster) : attribution;
    }


    public LivingEntity entity(ExecutionSubject subject) {
        return subject == ExecutionSubject.CASTER || subject == ExecutionSubject.ORIGIN ? caster : target;
    }

    public SkillExecutionContext retarget(LivingEntity entity, Vec3 resolvedPosition) {
        return new SkillExecutionContext(
                level,
                caster,
                skill,
                entity,
                resolvedPosition,
                charge,
                variables,
                attribution
        );
    }

    public ScaledValue.Context scaledValueContext() {
        return new ScaledValue.Context(
                originSource(),
                skill,
                caster,
                target,
                charge,
                variables
        );
    }

    public OriginSource originSource() {
        return AscensionOriginSourceHelper.getEntitySource(caster);
    }
}
