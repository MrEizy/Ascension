package net.zic.ascension.api.ascension.core.skill.castable.feature;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.zic.ascension.api.ascension.capabilities.AscensionEntityDataProvider;
import net.zic.ascension.api.ascension.capabilities.CoreCapabilities;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.api.ascension.value.ScaledValueContext;

import java.util.Map;

public record SkillExecutionContext(
        ServerLevel level,
        ServerPlayer caster,
        Identifier skill,
        LivingEntity target,
        Vec3 position,
        double charge,
        Map<Identifier, Double> variables
) {
    public SkillExecutionContext {
        position = position == null
                ? caster.position().add(0.0D, caster.getBbHeight() * 0.5D, 0.0D)
                : position;
        charge = Double.isFinite(charge) ? Math.clamp(charge, 0.0D, 1.0D) : 0.0D;
        variables = variables == null ? Map.of() : Map.copyOf(variables);
    }

    public ScaledValueContext scaledValueContext() {
        return new ScaledValueContext(
                originSource(),
                skill,
                caster,
                target,
                charge,
                variables
        );
    }

    public OriginSource originSource() {
        AscensionEntityDataProvider provider = caster.getCapability(
                CoreCapabilities.ASCENSION_ENTITY_DATA_PROVIDER_CAPABILITY
        );
        return provider == null ? null : provider.getData(caster).getSource();
    }
}
