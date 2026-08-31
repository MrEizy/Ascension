package net.zic.ascension.api.ascension.core.runtime;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.skill.castable.action.SkillAction;
import net.zic.ascension.api.ascension.core.targeting.TargetingDefinition;
import net.zic.ascension.api.ascension.value.ScaledValue;

import java.util.List;
import java.util.Optional;

public record BeamDefinition(
        ScaledValue range,
        ScaledValue width,
        ScaledValue duration,
        int tickInterval,
        int maximumTargets,
        boolean stopOnBlock,
        TargetingDefinition.Filter filter,
        ScaledValue knockback,
        List<SkillAction> onHit,
        List<SkillAction> onBlock,
        List<SkillAction> onExpire,
        Optional<Identifier> visual
) {
    public static final Codec<BeamDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ScaledValue.COMPACT_CODEC.fieldOf("range").forGetter(BeamDefinition::range),
            ScaledValue.COMPACT_CODEC.optionalFieldOf("width", ScaledValue.constant(0.5D)).forGetter(BeamDefinition::width),
            ScaledValue.COMPACT_CODEC.optionalFieldOf("duration", ScaledValue.constant(1.0D)).forGetter(BeamDefinition::duration),
            Codec.intRange(1, 1200).optionalFieldOf("tick_interval", 1).forGetter(BeamDefinition::tickInterval),
            Codec.intRange(0, 256).optionalFieldOf("maximum_targets", 0).forGetter(BeamDefinition::maximumTargets),
            Codec.BOOL.optionalFieldOf("stop_on_block", true).forGetter(BeamDefinition::stopOnBlock),
            TargetingDefinition.Filter.CODEC.codec().optionalFieldOf("filter", TargetingDefinition.Filter.hostile()).forGetter(BeamDefinition::filter),
            ScaledValue.COMPACT_CODEC.optionalFieldOf("knockback", ScaledValue.constant(0.0D)).forGetter(BeamDefinition::knockback),
            SkillAction.CODEC.listOf().optionalFieldOf("on_hit", List.of()).forGetter(BeamDefinition::onHit),
            SkillAction.CODEC.listOf().optionalFieldOf("on_block", List.of()).forGetter(BeamDefinition::onBlock),
            SkillAction.CODEC.listOf().optionalFieldOf("on_expire", List.of()).forGetter(BeamDefinition::onExpire),
            Identifier.CODEC.optionalFieldOf("visual").forGetter(BeamDefinition::visual)
    ).apply(instance, BeamDefinition::new));

    public BeamDefinition {
        filter = filter == null ? TargetingDefinition.Filter.hostile() : filter;
        onHit = onHit == null ? List.of() : List.copyOf(onHit);
        onBlock = onBlock == null ? List.of() : List.copyOf(onBlock);
        onExpire = onExpire == null ? List.of() : List.copyOf(onExpire);
        visual = visual == null ? Optional.empty() : visual;
    }
}
