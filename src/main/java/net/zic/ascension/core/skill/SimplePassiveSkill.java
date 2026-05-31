package net.zic.ascension.core.skill;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.zic.ascension.api.core.skill.Skill;
import net.zic.ascension.api.core.skill.SkillData;
import net.zic.ascension.api.core.source.OriginSource;
import net.zic.ascension.api.datapack.skill.SkillType;
import net.zic.ascension.datapack.skill.AscensionSkillTypes;
import net.zic.zenithlib.common.ZenithRegistries;
import net.zic.zenithlib.value_containers.ValueContainer;
import net.zic.zenithlib.value_containers.ValueContainerModifier;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SimplePassiveSkill implements Skill {

    private final UUID skillModifierId = UUID.randomUUID();

    private final Component name;
    private final Component description;

    private final List<ValueContainer.BaseModifier> baseStats;
    private final Map<Identifier,List<ValueContainerModifier>> statModifiers;
    private final List<ValueContainer.BaseModifier> baseAffinities;
    private final Map<Identifier,List<ValueContainerModifier>> affinityModifiers;



    public SimplePassiveSkill(Component name, Component description, List<ValueContainer.BaseModifier> baseStats, Map<Identifier, List<ValueContainerModifier>> statModifiers, List<ValueContainer.BaseModifier> baseAffinities, Map<Identifier, List<ValueContainerModifier>> affinityModifiers){
        this.name = name;
        this.description = description;
        this.baseStats = baseStats;
        this.statModifiers = statModifiers;
        this.baseAffinities = baseAffinities;
        this.affinityModifiers = affinityModifiers;
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
        for(ValueContainer.BaseModifier baseModifier : baseStats){
            source.addStat(ZenithRegistries.STAT_REGISTRY.getValue(baseModifier.container()),baseModifier.val());
        }
        for(Identifier stat : statModifiers.keySet()){
            for(ValueContainerModifier modifier : statModifiers.get(stat)){
                source.addStatModifier(ZenithRegistries.STAT_REGISTRY.getValue(stat),modifier);
            }
        }
    }

    @Override
    public void onRemoved(OriginSource source, SkillData data) {
        for(ValueContainer.BaseModifier baseModifier : baseStats){
            source.removeStat(ZenithRegistries.STAT_REGISTRY.getValue(baseModifier.container()),baseModifier.val());
        }
        for(Identifier stat : statModifiers.keySet()){
            for(ValueContainerModifier modifier : statModifiers.get(stat)){

                source.removeStatModifier(ZenithRegistries.STAT_REGISTRY.getValue(stat),modifier.getIdentifier());
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
    public SkillData newData() {
        return null;
    }

    @Override
    public SkillData loadData(ValueInput input) {
        return null;
    }

    @Override
    public SkillData loadData(ByteBuf buf) {
        return null;
    }
}
