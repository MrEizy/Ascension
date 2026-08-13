package net.zic.ascension.configuration.mobs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import net.zic.ascension.impl.core.physique.SimplePhysique;
import net.zic.ascension.mob_cultivation.generation.MobCultivationEliteTier;
import net.zic.zenithlib.value_containers.ValueContainer;
import net.zic.zenithlib.value_containers.ValueContainerModifier;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record MobTraitDefinition(Component prefix,
                                 List<ValueContainer.BaseModifier> baseStats,
                                 Map<Identifier, List<ValueContainerModifier>> statModifiers,
                                 List<ValueContainer.BaseModifier> baseAttributes,
                                 Map<Identifier, List<ValueContainerModifier>> attributeModifiers
                                ,List<Identifier> skills){
    public static record PotentialTrait(MobTraitDefinition trait,double chance){
        public static final Codec<PotentialTrait> CODEC = RecordCodecBuilder.create(
                instance->instance.group(
                        MobTraitDefinition.CODEC.fieldOf("trait").forGetter(PotentialTrait::trait),
                        Codec.doubleRange(0,1).fieldOf("chance").forGetter(PotentialTrait::chance)
                ).apply(instance,PotentialTrait::new)
        );
    }
    public static final Codec<MobTraitDefinition> CODEC = RecordCodecBuilder.create(
            instance->instance.group(
                    ComponentSerialization.CODEC.optionalFieldOf("prefix",Component.empty()).forGetter(MobTraitDefinition::prefix),
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
                    ).optionalFieldOf("base_stats", List.of()).forGetter(MobTraitDefinition::baseStats),
                    ValueContainerModifier.MAP_CODEC.optionalFieldOf("stat_modifiers", Map.of()).forGetter(MobTraitDefinition::statModifiers),
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
                    ).optionalFieldOf("base_attributes", List.of()).forGetter(MobTraitDefinition::baseAttributes),
                    ValueContainerModifier.MAP_CODEC.optionalFieldOf("attribute_modifiers", Map.of()).forGetter(MobTraitDefinition::attributeModifiers),
                    Identifier.CODEC.listOf().optionalFieldOf("skills", List.of()).forGetter(MobTraitDefinition::skills)
                    ).apply(instance,MobTraitDefinition::new)
    );
}
