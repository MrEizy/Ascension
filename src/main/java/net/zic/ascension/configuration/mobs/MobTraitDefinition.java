package net.zic.ascension.configuration.mobs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.path.bonus.PathBonus;
import net.zic.ascension.api.ascension.core.path.bonus.PathBonusHolder;
import net.zic.ascension.impl.core.physique.SimplePhysique;
import net.zic.zenithlib.value_containers.typed.ModifierHolder;
import net.zic.zenithlib.value_containers.typed.ValueContainerCodecHelper;


import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record MobTraitDefinition(Component prefix,
                                 Map<Identifier,ModifierHolder<Double>> statModifiers,
                                 Map<Identifier,ModifierHolder<Double>> attributeModifiers,
                                 Map<PathBonus,ModifierHolder<Double>> pathBonusModifiers,
                                List<Identifier> skills){
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
                    ValueContainerCodecHelper.containersCodec(Codec.DOUBLE).optionalFieldOf("stats", Map.of()).forGetter(MobTraitDefinition::statModifiers),
                    ValueContainerCodecHelper.containersCodec(Codec.DOUBLE).optionalFieldOf("attributes", Map.of()).forGetter(MobTraitDefinition::attributeModifiers),
                    PathBonusHolder.MODIFIER_CODEC.optionalFieldOf("stats",Map.of()).forGetter(MobTraitDefinition::pathBonusModifiers),
                    Identifier.CODEC.listOf().optionalFieldOf("skills", List.of()).forGetter(MobTraitDefinition::skills)
                    ).apply(instance,MobTraitDefinition::new)
    );
}
