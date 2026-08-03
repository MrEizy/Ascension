package net.zic.ascension.api.core.targeting;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.capabilities.AscensionEntityDataProvider;
import net.zic.ascension.api.ascension.capabilities.CoreCapabilities;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.api.value.ScaledValueContext;

import java.util.HashMap;
import java.util.Map;

public record TargetingContext(
        ServerLevel level,
        ServerPlayer caster,
        Identifier skill,
        int effectiveLevel,
        double charge,
        Map<Identifier, Double> variables
) {
    public static final Identifier EFFECTIVE_LEVEL = AscensionCraft.prefix("skill/effective_level");

    public TargetingContext {
        effectiveLevel = Math.max(0, effectiveLevel);
        charge = Double.isFinite(charge) ? Math.clamp(charge, 0.0D, 1.0D) : 0.0D;
        variables = variables == null ? Map.of() : Map.copyOf(variables);
    }

    public ScaledValueContext scaledValueContext(LivingEntity target) {
        Map<Identifier, Double> resolvedVariables = new HashMap<>(variables);
        resolvedVariables.put(EFFECTIVE_LEVEL, (double) effectiveLevel);
        return new ScaledValueContext(
                originSource(),
                skill,
                caster,
                target,
                charge,
                resolvedVariables
        );
    }

    public OriginSource originSource() {
        AscensionEntityDataProvider provider = caster.getCapability(
                CoreCapabilities.ASCENSION_ENTITY_DATA_PROVIDER_CAPABILITY
        );
        return provider == null ? null : provider.getData(caster).getSource();
    }
}
