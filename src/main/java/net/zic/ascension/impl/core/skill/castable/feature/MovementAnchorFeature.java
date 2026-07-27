package net.zic.ascension.impl.core.skill.castable.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;
import net.zic.ascension.api.core.skill.castable.feature.SkillExecutionContext;
import net.zic.ascension.api.core.skill.castable.feature.SkillExecutionFeature;
import net.zic.ascension.api.datapack.CodecType;
import net.zic.ascension.api.value.ScaledValue;
import net.zic.ascension.common.movement.MovementService;
import net.zic.ascension.impl.datapack.skill.castable.feature.AscensionSkillExecutionFeatureTypes;

public record MovementAnchorFeature(Action action, Identifier anchor, ScaledValue duration)
        implements SkillExecutionFeature {
    public static final MapCodec<MovementAnchorFeature> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Action.CODEC.fieldOf("action").forGetter(MovementAnchorFeature::action),
            Identifier.CODEC.fieldOf("anchor").forGetter(MovementAnchorFeature::anchor),
            ScaledValue.CODEC.codec().optionalFieldOf("duration", ScaledValue.constant(0.0D))
                    .forGetter(MovementAnchorFeature::duration)
    ).apply(instance, MovementAnchorFeature::new));

    @Override
    public CodecType<SkillExecutionFeature> getType() {
        return AscensionSkillExecutionFeatureTypes.MOVEMENT_ANCHOR.get();
    }

    @Override
    public void apply(SkillExecutionContext context) {
        if (action == Action.CLEAR) {
            MovementService.removeAnchor(context.caster(), anchor);
            return;
        }
        double value = duration.resolve(context.scaledValueContext());
        long ticks = !Double.isFinite(value) || value <= 0.0D ? 0L : Math.round(value);
        MovementService.setAnchor(context.caster(), anchor, ticks);
    }

    public enum Action implements StringRepresentable {
        SET("set"),
        CLEAR("clear");

        public static final Codec<Action> CODEC = StringRepresentable.fromEnum(Action::values);

        private final String name;

        Action(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
