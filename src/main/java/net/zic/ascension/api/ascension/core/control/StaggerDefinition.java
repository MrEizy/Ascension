package net.zic.ascension.api.ascension.core.control;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.zic.ascension.api.ascension.core.skill.castable.action.SkillAction;
import net.zic.ascension.api.ascension.value.ScaledValue;

import java.util.List;

public record StaggerDefinition(
        ScaledValue threshold,
        ScaledValue resistance,
        ScaledValue decayPerSecond,
        int decayDelay,
        ScaledValue guardBreakDuration,
        ScaledValue immunityDuration,
        double movementMultiplier,
        boolean interruptHeldCasts,
        List<SkillAction> onGuardBreak
) {
    public static final Codec<StaggerDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ScaledValue.COMPACT_CODEC.optionalFieldOf("threshold", ScaledValue.constant(100.0D))
                    .forGetter(StaggerDefinition::threshold),
            ScaledValue.COMPACT_CODEC.optionalFieldOf("resistance", ScaledValue.constant(0.0D))
                    .forGetter(StaggerDefinition::resistance),
            ScaledValue.COMPACT_CODEC.optionalFieldOf("decay_per_second", ScaledValue.constant(10.0D))
                    .forGetter(StaggerDefinition::decayPerSecond),
            Codec.intRange(0, 12000).optionalFieldOf("decay_delay", 40)
                    .forGetter(StaggerDefinition::decayDelay),
            ScaledValue.COMPACT_CODEC.optionalFieldOf("guard_break_duration", ScaledValue.constant(30.0D))
                    .forGetter(StaggerDefinition::guardBreakDuration),
            ScaledValue.COMPACT_CODEC.optionalFieldOf("immunity_duration", ScaledValue.constant(40.0D))
                    .forGetter(StaggerDefinition::immunityDuration),
            Codec.doubleRange(0.0D, 1.0D).optionalFieldOf("movement_multiplier", 0.2D)
                    .forGetter(StaggerDefinition::movementMultiplier),
            Codec.BOOL.optionalFieldOf("interrupt_held_casts", true)
                    .forGetter(StaggerDefinition::interruptHeldCasts),
            SkillAction.CODEC.listOf().optionalFieldOf("on_guard_break", List.of())
                    .forGetter(StaggerDefinition::onGuardBreak)
    ).apply(instance, StaggerDefinition::new));

    public StaggerDefinition {
        onGuardBreak = onGuardBreak == null ? List.of() : List.copyOf(onGuardBreak);
    }
}
