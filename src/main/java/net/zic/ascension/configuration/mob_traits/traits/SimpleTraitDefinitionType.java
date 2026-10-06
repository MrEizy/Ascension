package net.zic.ascension.configuration.mob_traits.traits;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.path.bonus.PathBonusHolder;
import net.zic.ascension.configuration.mob_traits.MobTraitDefinition;
import net.zic.ascension.configuration.mob_traits.MobTraitDefinitionType;
import net.zic.ascension.configuration.mob_traits.traits.cultivation_traits.PathDefinition;
import net.zic.ascension.configuration.mobs.condition.MobConfigurationConditionType;
import net.zic.ascension.impl.core.physique.SimplePhysique;
import net.zic.zenithlib.value_containers.ValueContainer;
import net.zic.zenithlib.value_containers.ValueContainerModifier;
import net.zic.zenithlib.value_containers.typed.ValueContainerCodecHelper;

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
                        ValueContainerCodecHelper.containersCodec(Codec.DOUBLE).optionalFieldOf("stats",Map.of()).forGetter(SimpleTraitDefinition::statModifiers),
                        ValueContainerCodecHelper.containersCodec(Codec.DOUBLE).optionalFieldOf("attributes",Map.of()).forGetter(SimpleTraitDefinition::attributeModifiers),
                        PathBonusHolder.MODIFIER_CODEC.optionalFieldOf("path_bonus",Map.of()).forGetter(SimpleTraitDefinition::pathBonusModifiers),
                        Identifier.CODEC.listOf().optionalFieldOf("skills", List.of()).forGetter(SimpleTraitDefinition::skills)
                ).apply(instance, SimpleTraitDefinition::new)
        );
    }
}
