package net.zic.ascension.impl.core.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.effect.SkillEffectContext;

import java.util.Optional;
import java.util.UUID;

public final class SkillEffectInstance implements SkillEffectContext {
    public static final Codec<SkillEffectInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("definition").forGetter(SkillEffectInstance::definition),
            Codec.STRING.xmap(UUID::fromString, UUID::toString).fieldOf("instance_id").forGetter(SkillEffectInstance::instanceId),
            Codec.STRING.xmap(UUID::fromString, UUID::toString).optionalFieldOf("source_entity")
                    .forGetter(value -> Optional.ofNullable(value.sourceEntity)),
            Identifier.CODEC.optionalFieldOf("source_skill")
                    .forGetter(value -> Optional.ofNullable(value.sourceSkill)),
            Codec.INT.fieldOf("remaining_duration").forGetter(SkillEffectInstance::remainingDuration),
            Codec.DOUBLE.fieldOf("potency").forGetter(SkillEffectInstance::potency),
            Codec.INT.optionalFieldOf("stacks", 1).forGetter(SkillEffectInstance::stacks)
    ).apply(instance, (definition, id, sourceEntity, sourceSkill, duration, potency, stacks) ->
            new SkillEffectInstance(
                    definition,
                    id,
                    sourceEntity.orElse(null),
                    sourceSkill.orElse(null),
                    duration,
                    potency,
                    stacks
            )));

    private final Identifier definition;
    private final UUID instanceId;
    private final UUID sourceEntity;
    private final Identifier sourceSkill;
    private int remainingDuration;
    private double potency;
    private int stacks;

    public SkillEffectInstance(
            Identifier definition,
            UUID sourceEntity,
            Identifier sourceSkill,
            int duration,
            double potency
    ) {
        this(definition, UUID.randomUUID(), sourceEntity, sourceSkill, duration, potency, 1);
    }

    private SkillEffectInstance(
            Identifier definition,
            UUID instanceId,
            UUID sourceEntity,
            Identifier sourceSkill,
            int duration,
            double potency,
            int stacks
    ) {
        this.definition = definition;
        this.instanceId = instanceId;
        this.sourceEntity = sourceEntity;
        this.sourceSkill = sourceSkill;
        this.remainingDuration = Math.max(0, duration);
        this.potency = Math.max(0.0D, potency);
        this.stacks = Math.max(1, stacks);
    }

    @Override
    public Identifier definition() {
        return definition;
    }

    @Override
    public UUID instanceId() {
        return instanceId;
    }

    @Override
    public UUID sourceEntity() {
        return sourceEntity;
    }

    @Override
    public Identifier sourceSkill() {
        return sourceSkill;
    }

    @Override
    public int remainingDuration() {
        return remainingDuration;
    }

    @Override
    public double potency() {
        return potency;
    }

    @Override
    public int stacks() {
        return stacks;
    }

    void setRemainingDuration(int value) {
        remainingDuration = Math.max(0, value);
    }

    void setPotency(double value) {
        potency = Math.max(0.0D, value);
    }

    void setStacks(int value) {
        stacks = Math.max(1, value);
    }

    boolean tickDuration() {
        if (remainingDuration <= 0) {
            return true;
        }
        remainingDuration--;
        return remainingDuration <= 0;
    }
}
