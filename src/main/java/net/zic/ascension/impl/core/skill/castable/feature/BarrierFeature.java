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
import net.zic.ascension.impl.datapack.skill.AscensionSkillExecutionFeatureTypes;
import net.zic.ascension.impl.runtime.barrier.Barriers;

import java.util.Locale;

public record BarrierFeature(
        Action action,
        Target target,
        Identifier definition,
        ScaledValue amount
) implements SkillExecutionFeature {
    public static final MapCodec<BarrierFeature> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Action.CODEC.optionalFieldOf("action", Action.APPLY).forGetter(BarrierFeature::action),
            Target.CODEC.optionalFieldOf("target", Target.TARGET).forGetter(BarrierFeature::target),
            Identifier.CODEC.fieldOf("definition").forGetter(BarrierFeature::definition),
            ScaledValue.CODEC.codec().optionalFieldOf("amount", ScaledValue.constant(0.0D))
                    .forGetter(BarrierFeature::amount)
    ).apply(instance, BarrierFeature::new));

    @Override
    public CodecType<SkillExecutionFeature> getType() {
        return AscensionSkillExecutionFeatureTypes.BARRIER.get();
    }

    @Override
    public void apply(SkillExecutionContext context) {
        LivingEntity entity = target == Target.CASTER ? context.caster() : context.target();
        if (entity == null) {
            return;
        }
        switch (action) {
            case APPLY -> Barriers.apply(context, entity, definition);
            case REPAIR -> {
                double resolved = amount.resolve(context.scaledValueContext());
                Barriers.repair(context.level(), context.caster().getUUID(), entity.getUUID(), definition, resolved);
            }
            case REMOVE -> Barriers.removeMatching(
                    context.level(),
                    context.caster().getUUID(),
                    entity.getUUID(),
                    definition,
                    Barriers.Removal.EXPLICIT
            );
        }
    }

    public enum Action {
        APPLY,
        REPAIR,
        REMOVE;

        public static final Codec<Action> CODEC = Codec.STRING.comapFlatMap(
                value -> {
                    try {
                        return DataResult.success(valueOf(value.toUpperCase(Locale.ROOT)));
                    } catch (IllegalArgumentException exception) {
                        return DataResult.error(() -> "Unknown barrier action: " + value);
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
                        return DataResult.error(() -> "Unknown barrier target: " + value);
                    }
                },
                value -> value.name().toLowerCase(Locale.ROOT)
        );
    }
}
