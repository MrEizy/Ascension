package net.zic.ascension.impl.datapack.skill.passive;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.ComponentSerialization;
import net.zic.ascension.api.ascension.core.skill.Skill;
import net.zic.ascension.api.ascension.core.skill.SkillData;
import net.zic.ascension.api.ascension.datapack.skill.SkillType;
import net.zic.ascension.impl.core.skill.EmptySkillData;
import net.zic.ascension.impl.core.skill.SimplePassiveSkill;
import net.zic.zenithlib.value_containers.ValueContainer;
import net.zic.zenithlib.value_containers.ValueContainerModifier;

import java.util.List;
import java.util.Map;

public class SimplePassiveSkillType extends SkillType {
    @Override
    public MapCodec<? extends Skill> codec() {
        return RecordCodecBuilder.<SimplePassiveSkill>mapCodec(instance ->
                instance.group(
                        ComponentSerialization.CODEC.fieldOf("name").forGetter(SimplePassiveSkill::getName),
                        ComponentSerialization.CODEC.fieldOf("description").forGetter(SimplePassiveSkill::getDescription),
                        ValueContainer.BASE_MODIFIER_CODEC.listOf().optionalFieldOf("base_stats", List.of())
                                .forGetter(SimplePassiveSkill::getBaseStats),
                        ValueContainerModifier.MAP_CODEC.optionalFieldOf("stat_modifiers", Map.of())
                                .forGetter(SimplePassiveSkill::getStatModifiers),
                        ValueContainer.BASE_MODIFIER_CODEC.listOf().optionalFieldOf("base_affinity", List.of())
                                .forGetter(SimplePassiveSkill::getBaseAffinities),
                        ValueContainerModifier.MAP_CODEC.optionalFieldOf("affinity_modifiers", Map.of())
                                .forGetter(SimplePassiveSkill::getAffinityModifiers),
                        SimplePassiveSkill.Defense.CODEC.optionalFieldOf("defense")
                                .forGetter(SimplePassiveSkill::getDefense)
                ).apply(instance, SimplePassiveSkill::new)
        );
    }

    @Override
    public MapCodec<? extends SkillData> dataCodec() {
        return MapCodec.unit(EmptySkillData::new);
    }
}
