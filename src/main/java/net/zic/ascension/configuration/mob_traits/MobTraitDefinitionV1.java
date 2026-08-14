package net.zic.ascension.configuration.mob_traits;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.path.bonus.PathBonus;
import net.zic.ascension.api.ascension.core.path.bonus.PathBonusHolder;

import net.zic.zenithlib.value_containers.typed.ModifierHolder;
import net.zic.zenithlib.value_containers.typed.ValueContainerCodecHelper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record MobTraitDefinitionV1(Component prefix,
                                 Map<Identifier, ModifierHolder<Double>> statModifiers,
                                 Map<Identifier,ModifierHolder<Double>> attributeModifiers,
                                 Map<PathBonus,ModifierHolder<Double>> pathBonusModifiers,
                                 List<Identifier> skills){
    public static record PotentialTrait(MobTraitDefinitionV1 trait,double chance){
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
                    ValueContainerCodecHelper.containersCodec(Codec.DOUBLE).optionalFieldOf("stats", Map.of()).forGetter(MobTraitDefinitionV1::statModifiers),
                    ValueContainerCodecHelper.containersCodec(Codec.DOUBLE).optionalFieldOf("attributes", Map.of()).forGetter(MobTraitDefinitionV1::attributeModifiers),
                    PathBonusHolder.MODIFIER_CODEC.optionalFieldOf("stats",Map.of()).forGetter(MobTraitDefinitionV1::pathBonusModifiers),
                    Identifier.CODEC.listOf().optionalFieldOf("skills", List.of()).forGetter(MobTraitDefinitionV1::skills)
            ).apply(instance,MobTraitDefinitionV1::new)
    );
}
