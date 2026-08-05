package net.zic.ascension.impl.core.skill;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.api.ascension.core.runtime.BarrierDefinition;
import net.zic.ascension.api.ascension.core.skill.Skill;
import net.zic.ascension.api.ascension.core.skill.SkillData;
import net.zic.ascension.api.ascension.datapack.skill.SkillType;
import net.zic.ascension.api.ascension.value.ScaledValue;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.impl.datapack.skill.AscensionSkillTypes;
import net.zic.zenithlib.common.ZenithRegistries;
import net.zic.zenithlib.value_containers.ValueContainer;
import net.zic.zenithlib.value_containers.ValueContainerModifier;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class SimplePassiveSkill implements Skill {

    private final UUID skillModifierId = UUID.randomUUID();

    private final Component name;
    private final Component description;
    private final List<ValueContainer.BaseModifier> baseStats;
    private final Map<Identifier, List<ValueContainerModifier>> statModifiers;
    private final List<ValueContainer.BaseModifier> baseAffinities;
    private final Map<Identifier, List<ValueContainerModifier>> affinityModifiers;
    private final Optional<Defense> defense;

    public SimplePassiveSkill(
            Component name,
            Component description,
            List<ValueContainer.BaseModifier> baseStats,
            Map<Identifier, List<ValueContainerModifier>> statModifiers,
            List<ValueContainer.BaseModifier> baseAffinities,
            Map<Identifier, List<ValueContainerModifier>> affinityModifiers,
            Optional<Defense> defense
    ) {
        this.name = name;
        this.description = description;
        this.baseStats = baseStats;
        this.statModifiers = statModifiers;
        this.baseAffinities = baseAffinities;
        this.affinityModifiers = affinityModifiers;
        this.defense = defense == null ? Optional.empty() : defense;
    }

    public List<ValueContainer.BaseModifier> getBaseAffinities() {
        return baseAffinities;
    }

    public List<ValueContainer.BaseModifier> getBaseStats() {
        return baseStats;
    }

    public Map<Identifier, List<ValueContainerModifier>> getAffinityModifiers() {
        return affinityModifiers;
    }

    public Map<Identifier, List<ValueContainerModifier>> getStatModifiers() {
        return statModifiers;
    }

    public Optional<Defense> getDefense() {
        return defense;
    }

    @Override
    public SkillType getType() {
        return AscensionSkillTypes.SIMPLE_PASSIVE_SKILL_TYPE.get();
    }

    @Override
    public Component getName() {
        return name;
    }

    @Override
    public Component getDescription() {
        return description;
    }

    @Override
    public void onAdded(OriginSource source, SkillData data) {
        for (ValueContainer.BaseModifier baseModifier : baseStats) {
            source.addStat(ZenithRegistries.STAT_REGISTRY.getValue(baseModifier.container()), baseModifier.val());
        }
        for (Identifier stat : statModifiers.keySet()) {
            for (ValueContainerModifier modifier : statModifiers.get(stat)) {
                source.addStatModifier(ZenithRegistries.STAT_REGISTRY.getValue(stat), modifier);
            }
        }
    }

    @Override
    public void onRemoved(OriginSource source, SkillData data) {
        for (ValueContainer.BaseModifier baseModifier : baseStats) {
            source.removeStat(ZenithRegistries.STAT_REGISTRY.getValue(baseModifier.container()), baseModifier.val());
        }
        for (Identifier stat : statModifiers.keySet()) {
            for (ValueContainerModifier modifier : statModifiers.get(stat)) {
                source.removeStatModifier(ZenithRegistries.STAT_REGISTRY.getValue(stat), modifier.getIdentifier());
            }
        }
    }

    @Override
    public void applyToEntity(LivingEntity entity, SkillData data) {
    }

    @Override
    public void removeFromEntity(LivingEntity entity, SkillData data) {
    }

    @Override
    public SkillData newData(RegistryAccess access) {
        return new EmptyData();
    }

    @Override
    public SkillData loadData(ValueInput input, RegistryAccess access) {
        return new EmptyData();
    }

    @Override
    public SkillData loadData(ByteBuf buf) {
        return new EmptyData();
    }

    public record Defense(
            ScaledValue flatReduction,
            ScaledValue percentageReduction,
            ScaledValue staggerResistance,
            BarrierDefinition.DamageFilter filter
    ) {
        public static final Codec<Defense> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ScaledValue.CODEC.codec().optionalFieldOf("flat_reduction", ScaledValue.constant(0.0D))
                        .forGetter(Defense::flatReduction),
                ScaledValue.CODEC.codec().optionalFieldOf("percentage_reduction", ScaledValue.constant(0.0D))
                        .forGetter(Defense::percentageReduction),
                ScaledValue.CODEC.codec().optionalFieldOf("stagger_resistance", ScaledValue.constant(0.0D))
                        .forGetter(Defense::staggerResistance),
                BarrierDefinition.DamageFilter.CODEC.optionalFieldOf(
                        "filter",
                        BarrierDefinition.DamageFilter.EMPTY
                ).forGetter(Defense::filter)
        ).apply(instance, Defense::new));

        public Defense {
            flatReduction = flatReduction == null ? ScaledValue.constant(0.0D) : flatReduction;
            percentageReduction = percentageReduction == null
                    ? ScaledValue.constant(0.0D)
                    : percentageReduction;
            staggerResistance = staggerResistance == null
                    ? ScaledValue.constant(0.0D)
                    : staggerResistance;
            filter = filter == null ? BarrierDefinition.DamageFilter.EMPTY : filter;
        }
    }

    public static final class EmptyData implements SkillData {
        @Override
        public void write(ValueOutput output) {
        }

        @Override
        public void encode(ByteBuf buf) {
        }

        @Override
        public SkillType getType() {
            return AscensionSkillTypes.SIMPLE_PASSIVE_SKILL_TYPE.get();
        }
    }
}
