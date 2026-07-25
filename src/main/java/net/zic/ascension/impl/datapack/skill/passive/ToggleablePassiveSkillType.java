package net.zic.ascension.impl.datapack.skill.passive;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.ComponentSerialization;
import net.zic.ascension.api.core.skill.Skill;
import net.zic.ascension.api.core.skill.SkillData;
import net.zic.ascension.api.datapack.skill.SkillType;
import net.zic.ascension.impl.core.skill.toggleable.QiUpkeep;
import net.zic.ascension.impl.core.skill.toggleable.ToggleablePassiveSkill;
import net.zic.ascension.impl.core.skill.toggleable.ToggleablePassiveSkillData;
import net.zic.zenithlib.value_containers.ValueContainer;
import net.zic.zenithlib.value_containers.ValueContainerModifier;

import java.util.List;
import java.util.Map;

public class ToggleablePassiveSkillType extends SkillType {
    private static final MapCodec<ToggleablePassiveSkillData> DATA_CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.BOOL.optionalFieldOf("enabled", false)
                            .forGetter(ToggleablePassiveSkillData::isEnabled)
            ).apply(instance, ToggleablePassiveSkillData::new));

    @Override
    public MapCodec<? extends Skill> codec() {
        return RecordCodecBuilder.<ToggleablePassiveSkill>mapCodec(instance ->
                instance.group(
                        ComponentSerialization.CODEC.fieldOf("name")
                                .forGetter(ToggleablePassiveSkill::getName),
                        ComponentSerialization.CODEC.fieldOf("description")
                                .forGetter(ToggleablePassiveSkill::getDescription),
                        Codec.BOOL.optionalFieldOf("enabled_by_default", false)
                                .forGetter(ToggleablePassiveSkill::isEnabledByDefault),
                        QiUpkeep.CODEC.optionalFieldOf("qi_upkeep")
                                .forGetter(ToggleablePassiveSkill::getQiUpkeep),
                        ValueContainer.BASE_MODIFIER_CODEC.listOf()
                                .optionalFieldOf("base_stats", List.of())
                                .forGetter(ToggleablePassiveSkill::getBaseStats),
                        ValueContainerModifier.MAP_CODEC
                                .optionalFieldOf("stat_modifiers", Map.of())
                                .forGetter(ToggleablePassiveSkill::getStatModifiers),
                        ValueContainer.BASE_MODIFIER_CODEC.listOf()
                                .optionalFieldOf("base_affinity", List.of())
                                .forGetter(ToggleablePassiveSkill::getBaseAffinities),
                        ValueContainerModifier.MAP_CODEC
                                .optionalFieldOf("affinity_modifiers", Map.of())
                                .forGetter(ToggleablePassiveSkill::getAffinityModifiers)
                ).apply(instance, ToggleablePassiveSkill::new)
        );
    }

    @Override
    public MapCodec<? extends SkillData> dataCodec() {
        return DATA_CODEC;
    }
}
