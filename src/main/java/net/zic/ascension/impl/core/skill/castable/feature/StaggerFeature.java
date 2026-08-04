package net.zic.ascension.impl.core.skill.castable.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.api.ascension.core.skill.castable.feature.SkillExecutionContext;
import net.zic.ascension.api.ascension.core.skill.castable.feature.SkillExecutionFeature;
import net.zic.ascension.api.ascension.datapack.CodecType;
import net.zic.ascension.api.ascension.value.ScaledValue;
import net.zic.ascension.impl.core.control.StaggerService;
import net.zic.ascension.impl.datapack.skill.AscensionSkillExecutionFeatureTypes;

import java.util.Locale;
import java.util.Optional;

public record StaggerFeature(
        Action action,
        Target target,
        Optional<Identifier> profile,
        ScaledValue amount,
        ScaledValue duration
) implements SkillExecutionFeature {
    public static final MapCodec<StaggerFeature> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Action.CODEC.optionalFieldOf("action", Action.APPLY).forGetter(StaggerFeature::action),
            Target.CODEC.optionalFieldOf("target", Target.TARGET).forGetter(StaggerFeature::target),
            Identifier.CODEC.optionalFieldOf("profile").forGetter(StaggerFeature::profile),
            ScaledValue.CODEC.codec().optionalFieldOf("amount", ScaledValue.constant(0.0D))
                    .forGetter(StaggerFeature::amount),
            ScaledValue.CODEC.codec().optionalFieldOf("duration", ScaledValue.constant(0.0D))
                    .forGetter(StaggerFeature::duration)
    ).apply(instance, StaggerFeature::new));

    public StaggerFeature {
        profile = profile == null ? Optional.empty() : profile;
    }

    @Override
    public CodecType<SkillExecutionFeature> getType() {
        return AscensionSkillExecutionFeatureTypes.STAGGER.get();
    }

    @Override
    public void apply(SkillExecutionContext context) {
        LivingEntity entity = target == Target.CASTER ? context.caster() : context.target();
        if (entity == null) {
            return;
        }
        switch (action) {
            case APPLY -> profile.ifPresent(value -> StaggerService.apply(
                    context,
                    entity,
                    value,
                    amount.resolve(context.scaledValueContext())
            ));
            case REDUCE -> StaggerService.reduce(entity, amount.resolve(context.scaledValueContext()));
            case CLEAR -> StaggerService.clear(entity);
            case IMMUNITY -> StaggerService.grantImmunity(
                    entity,
                    Math.max(0, (int) Math.round(duration.resolve(context.scaledValueContext())))
            );
            case BREAK -> profile.ifPresent(value -> StaggerService.forceGuardBreak(context, entity, value));
        }
    }

    public enum Action {
        APPLY,
        REDUCE,
        CLEAR,
        IMMUNITY,
        BREAK;

        public static final Codec<Action> CODEC = Codec.STRING.comapFlatMap(
                value -> {
                    try {
                        return DataResult.success(valueOf(value.toUpperCase(Locale.ROOT)));
                    } catch (IllegalArgumentException exception) {
                        return DataResult.error(() -> "Unknown stagger action: " + value);
                    }
                },
                value -> value.name().toLowerCase(Locale.ROOT)
        );
    }

    public enum Target {
        CASTER,
        TARGET;

        public static final Codec<Target> CODEC = Codec.STRING.comapFlatMap(
                value -> {
                    try {
                        return DataResult.success(valueOf(value.toUpperCase(Locale.ROOT)));
                    } catch (IllegalArgumentException exception) {
                        return DataResult.error(() -> "Unknown stagger target: " + value);
                    }
                },
                value -> value.name().toLowerCase(Locale.ROOT)
        );
    }
}
