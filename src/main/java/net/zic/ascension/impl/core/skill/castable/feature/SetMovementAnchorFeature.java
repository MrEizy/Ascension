package net.zic.ascension.impl.core.skill.castable.feature;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.core.skill.castable.feature.SkillExecutionContext;
import net.zic.ascension.api.core.skill.castable.feature.SkillExecutionFeature;
import net.zic.ascension.api.core.skill.castable.feature.SkillExecutionFeatureType;
import net.zic.ascension.api.value.ScaledValue;
import net.zic.ascension.common.movement.MovementService;
import net.zic.ascension.impl.datapack.skill.castable.feature.AscensionSkillExecutionFeatureTypes;

public record SetMovementAnchorFeature(
        Identifier anchor,
        ScaledValue duration
) implements SkillExecutionFeature {
    public static final MapCodec<SetMovementAnchorFeature> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("anchor").forGetter(SetMovementAnchorFeature::anchor),
            ScaledValue.CODEC.codec().optionalFieldOf("duration", ScaledValue.constant(0.0D))
                    .forGetter(SetMovementAnchorFeature::duration)
    ).apply(instance, SetMovementAnchorFeature::new));

    @Override
    public SkillExecutionFeatureType getType() {
        return AscensionSkillExecutionFeatureTypes.SET_MOVEMENT_ANCHOR.get();
    }

    @Override
    public void apply(SkillExecutionContext context) {
        double value = duration.resolve(context.scaledValueContext());
        long ticks = !Double.isFinite(value) || value <= 0.0D ? 0L : Math.round(value);
        MovementService.setAnchor(context.caster(), anchor, ticks);
    }
}
