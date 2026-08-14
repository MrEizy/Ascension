package net.zic.ascension.configuration.mob_traits.traits;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import net.zic.ascension.configuration.mob_traits.MobTraitCondition;
import net.zic.ascension.configuration.mob_traits.MobTraitDefinition;
import net.zic.ascension.configuration.mob_traits.MobTraitDefinitionType;
import net.zic.ascension.configuration.mob_traits.MobTraitDefinitionV1;
import net.zic.zenithlib.value_containers.ValueContainer;
import net.zic.zenithlib.value_containers.ValueContainerModifier;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record SimpleTraitDefinition (Component name,
                                     Component prefix,
                                     List<ValueContainer.BaseModifier> baseStats,
                                     Map<Identifier, List<ValueContainerModifier>> statModifiers,
                                     List<ValueContainer.BaseModifier> baseAttributes,
                                     Map<Identifier, List<ValueContainerModifier>> attributeModifiers
                                    , List<Identifier> skills,
                                     Collection<MobTraitCondition> conditions) implements MobTraitDefinition {
    @Override
    public MobTraitDefinitionType getType() {
        return null;
    }


}
