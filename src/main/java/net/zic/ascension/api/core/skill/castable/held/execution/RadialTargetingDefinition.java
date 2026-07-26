package net.zic.ascension.api.core.skill.castable.held.execution;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.api.core.targeting.TargetFilterDefinition;
import net.zic.ascension.api.value.ScaledValue;

public record RadialTargetingDefinition(
        ScaledValue radius,
        int maximumTargets,
        boolean includeSelf,
        boolean includeAllies,
        boolean includeNeutral,
        boolean includeHostile,
        boolean includePlayers,
        boolean requireLineOfSight
) {
    public static final MapCodec<RadialTargetingDefinition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ScaledValue.CODEC.codec().fieldOf("radius").forGetter(RadialTargetingDefinition::radius),
            Codec.intRange(0, 1024).optionalFieldOf("maximum_targets", 0)
                    .forGetter(RadialTargetingDefinition::maximumTargets),
            Codec.BOOL.optionalFieldOf("include_self", false).forGetter(RadialTargetingDefinition::includeSelf),
            Codec.BOOL.optionalFieldOf("include_allies", false).forGetter(RadialTargetingDefinition::includeAllies),
            Codec.BOOL.optionalFieldOf("include_neutral", true).forGetter(RadialTargetingDefinition::includeNeutral),
            Codec.BOOL.optionalFieldOf("include_hostile", true).forGetter(RadialTargetingDefinition::includeHostile),
            Codec.BOOL.optionalFieldOf("include_players", true).forGetter(RadialTargetingDefinition::includePlayers),
            Codec.BOOL.optionalFieldOf("require_line_of_sight", true).forGetter(RadialTargetingDefinition::requireLineOfSight)
    ).apply(instance, RadialTargetingDefinition::new));

    public TargetFilterDefinition filter() {
        return new TargetFilterDefinition(
                includeSelf,
                includeAllies,
                includeNeutral,
                includeHostile,
                includePlayers,
                requireLineOfSight
        );
    }

    public boolean matches(LivingEntity caster, LivingEntity target) {
        return filter().matches(caster, target);
    }
}
