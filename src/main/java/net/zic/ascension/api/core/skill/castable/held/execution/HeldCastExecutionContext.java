package net.zic.ascension.api.core.skill.castable.held.execution;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.capabilities.AscensionEntityDataHolder;
import net.zic.ascension.api.capabilities.CoreCapabilities;
import net.zic.ascension.api.core.skill.castable.feature.SkillExecutionContext;
import net.zic.ascension.api.core.skill.castable.held.HeldCastData;
import net.zic.ascension.api.core.source.OriginSource;
import net.zic.ascension.api.value.ScaledValueContext;

import java.util.Map;

public record HeldCastExecutionContext(
        ServerLevel level,
        ServerPlayer caster,
        Identifier skill,
        HeldCastData data,
        int chargeTicks,
        int maximumChargeTicks,
        double charge
) {
    public static final Identifier CHARGE_TICKS = AscensionCraft.prefix("cast/charge_ticks");
    public static final Identifier MAXIMUM_CHARGE_TICKS = AscensionCraft.prefix("cast/maximum_charge_ticks");

    public HeldCastExecutionContext {
        chargeTicks = Math.max(0, chargeTicks);
        maximumChargeTicks = Math.max(1, maximumChargeTicks);
        charge = Double.isFinite(charge) ? Math.clamp(charge, 0.0D, 1.0D) : 0.0D;
    }

    public ScaledValueContext scaledValueContext(LivingEntity target) {
        return new ScaledValueContext(
                originSource(),
                skill,
                caster,
                target,
                charge,
                variables()
        );
    }

    public SkillExecutionContext featureContext(LivingEntity target, Vec3 position) {
        return new SkillExecutionContext(
                level,
                caster,
                skill,
                target,
                position,
                charge,
                variables()
        );
    }

    public Map<Identifier, Double> variables() {
        return Map.of(
                CHARGE_TICKS, (double) chargeTicks,
                MAXIMUM_CHARGE_TICKS, (double) maximumChargeTicks
        );
    }

    public OriginSource originSource() {
        AscensionEntityDataHolder holder = caster.getCapability(
                CoreCapabilities.ASCENSION_ENTITY_DATA_HOLDER_CAPABILITY
        );
        return holder == null ? null : holder.getData(caster).getSource();
    }
}
