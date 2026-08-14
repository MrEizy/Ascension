package net.zic.ascension.configuration.mob_traits.traits;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import net.zic.ascension.configuration.mob_traits.MobTraitDefinition;
import net.zic.ascension.configuration.mob_traits.MobTraitDefinitionType;
import net.zic.ascension.configuration.mob_traits.traits.cultivation_traits.PathDefinition;
import net.zic.ascension.configuration.mobs.condition.MobConfigurationConditionType;
import net.zic.zenithlib.value_containers.ValueContainer;
import net.zic.zenithlib.value_containers.ValueContainerModifier;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SimpleTraitDefinitionType extends MobTraitDefinitionType {

    @Override
    public MapCodec<? extends MobTraitDefinition> codec() {
        return RecordCodecBuilder.<SimpleTraitDefinition>mapCodec(
                instance->instance.group(
                        ComponentSerialization.CODEC.fieldOf("name").forGetter(SimpleTraitDefinition::name),
                        ComponentSerialization.CODEC.optionalFieldOf("prefix", Component.empty()).forGetter(SimpleTraitDefinition::prefix),
                        PathDefinition.CODEC.optionalFieldOf("paths",List.of()).forGetter(SimpleTraitDefinition::paths),
                        Codec.unboundedMap(Identifier.CODEC,Codec.DOUBLE).xmap(
                                rawMap->
                                        rawMap.entrySet().stream()
                                                .map(entry->new ValueContainer.BaseModifier(entry.getKey(),entry.getValue()))
                                                .toList(),
                                array->
                                        array.stream()
                                                .collect(Collectors.toMap(
                                                        ValueContainer.BaseModifier::container,
                                                        ValueContainer.BaseModifier::val
                                                ))
                        ).optionalFieldOf("base_stats", List.of()).forGetter(SimpleTraitDefinition::baseStats),
                        ValueContainerModifier.MAP_CODEC.optionalFieldOf("stat_modifiers", Map.of()).forGetter(SimpleTraitDefinition::statModifiers),
                        ValueContainerModifier.MAP_CODEC.optionalFieldOf("attribute_modifiers", Map.of()).forGetter(SimpleTraitDefinition::attributeModifiers),
                        Identifier.CODEC.listOf().optionalFieldOf("skills", List.of()).forGetter(SimpleTraitDefinition::skills),
                        MobConfigurationConditionType.MOB_CONFIGURATION_CONDITION_CODEC.listOf().optionalFieldOf("conditions",List.of()).forGetter(SimpleTraitDefinition::conditions)
                ).apply(instance, SimpleTraitDefinition::new)
        );
    }
}
