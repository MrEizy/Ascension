package net.zic.ascension.impl.core.skill.castable.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.api.core.movement.MovementContext;
import net.zic.ascension.api.core.movement.MovementDefinition;
import net.zic.ascension.api.core.movement.MovementSubject;
import net.zic.ascension.api.core.skill.castable.feature.SkillExecutionContext;
import net.zic.ascension.api.core.skill.castable.feature.SkillExecutionFeature;
import net.zic.ascension.api.core.skill.castable.feature.SkillExecutionFeatureType;
import net.zic.ascension.impl.datapack.skill.castable.feature.AscensionSkillExecutionFeatureTypes;

public record MovementFeature(
        MovementSubject subject,
        MovementDefinition movement
) implements SkillExecutionFeature {
    public static final MapCodec<MovementFeature> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            MovementSubject.CODEC.optionalFieldOf("subject", MovementSubject.CASTER)
                    .forGetter(MovementFeature::subject),
            MovementDefinition.CODEC.fieldOf("movement").forGetter(MovementFeature::movement)
    ).apply(instance, MovementFeature::new));

    @Override
    public SkillExecutionFeatureType getType() {
        return AscensionSkillExecutionFeatureTypes.MOVEMENT.get();
    }

    @Override
    public void apply(SkillExecutionContext context) {
        LivingEntity mover = subject == MovementSubject.CASTER ? context.caster() : context.target();
        if (mover != null) {
            movement.execute(new MovementContext(context, mover));
        }
    }
}
