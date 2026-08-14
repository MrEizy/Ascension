package net.zic.ascension.impl.datapack.physique;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.physique.Physique;
import net.zic.ascension.api.ascension.datapack.path.PathBonusBase;
import net.zic.ascension.api.ascension.datapack.path.PathBonusModifier;
import net.zic.ascension.api.ascension.datapack.physique.PhysiqueType;
import net.zic.ascension.api.tooltip.AscensionItemTooltipDefinition;
import net.zic.ascension.impl.core.physique.SimplePhysique;
import net.zic.zenithlib.value_containers.ValueContainer;
import net.zic.zenithlib.value_containers.ValueContainerModifier;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SimplePhysiqueType extends PhysiqueType {
    @Override
    public MapCodec<? extends Physique> codec() {
        return RecordCodecBuilder.<SimplePhysique>mapCodec(instance ->
                instance.group(
                        ComponentSerialization.CODEC.fieldOf("name").forGetter(SimplePhysique::name),
                        ComponentSerialization.CODEC.fieldOf("description").forGetter(SimplePhysique::description),
                        Identifier.CODEC.listOf().fieldOf("paths").forGetter(SimplePhysique::unlockedPaths),
                        Identifier.CODEC.listOf().optionalFieldOf("skills", List.of()).forGetter(SimplePhysique::skills),
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
                        ).optionalFieldOf("base_stats",List.of()).forGetter(SimplePhysique::baseStats),
                        ValueContainerModifier.MAP_CODEC.optionalFieldOf("stat_modifiers", Map.of()).forGetter(SimplePhysique::statModifiers),
                        PathBonusBase.CODEC.optionalFieldOf("base_path_bonuses",List.of()).forGetter(SimplePhysique::basePathBonuses),
                        PathBonusModifier.CODEC.optionalFieldOf("path_bonus_modifiers",List.of()).forGetter(SimplePhysique::pathBonusModifiers),
                        AscensionItemTooltipDefinition.CODEC.optionalFieldOf("item_tooltip").forGetter(SimplePhysique::itemTooltip)
                ).apply(instance, SimplePhysique::new)
        );
    }


}
