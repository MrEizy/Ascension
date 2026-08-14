package net.zic.ascension.configuration.mob_traits;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import net.zic.zenithlib.value_containers.ValueContainer;
import net.zic.zenithlib.value_containers.ValueContainerModifier;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record MobTraitDefinitionV1(Component prefix,
                                   List<ValueContainer.BaseModifier> baseStats,
                                   Map<Identifier, List<ValueContainerModifier>> statModifiers,
                                   List<ValueContainer.BaseModifier> baseAttributes,
                                   Map<Identifier, List<ValueContainerModifier>> attributeModifiers
                                , List<Identifier> skills){
    public static record PotentialTrait(MobTraitDefinitionV1 trait, double chance){
        public static final Codec<PotentialTrait> CODEC = RecordCodecBuilder.create(
                instance->instance.group(
                        MobTraitDefinitionV1.CODEC.fieldOf("trait").forGetter(PotentialTrait::trait),
                        Codec.doubleRange(0,1).fieldOf("chance").forGetter(PotentialTrait::chance)
                ).apply(instance,PotentialTrait::new)
        );
    }
    public static final Codec<MobTraitDefinitionV1> CODEC = RecordCodecBuilder.create(
            instance->instance.group(
                    ComponentSerialization.CODEC.optionalFieldOf("prefix",Component.empty()).forGetter(MobTraitDefinitionV1::prefix),
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
                    ).optionalFieldOf("base_stats", List.of()).forGetter(MobTraitDefinitionV1::baseStats),
                    ValueContainerModifier.MAP_CODEC.optionalFieldOf("stat_modifiers", Map.of()).forGetter(MobTraitDefinitionV1::statModifiers),
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
                    ).optionalFieldOf("base_attributes", List.of()).forGetter(MobTraitDefinitionV1::baseAttributes),
                    ValueContainerModifier.MAP_CODEC.optionalFieldOf("attribute_modifiers", Map.of()).forGetter(MobTraitDefinitionV1::attributeModifiers),
                    Identifier.CODEC.listOf().optionalFieldOf("skills", List.of()).forGetter(MobTraitDefinitionV1::skills)
                    ).apply(instance, MobTraitDefinitionV1::new)
    );
}
