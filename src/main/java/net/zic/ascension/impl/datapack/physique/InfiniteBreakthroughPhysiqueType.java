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
import net.zic.ascension.impl.core.physique.InfiniteBreakthroughPhysique;
import net.zic.ascension.impl.core.physique.SimplePhysique;
import net.zic.zenithlib.value_containers.ValueContainer;
import net.zic.zenithlib.value_containers.ValueContainerModifier;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InfiniteBreakthroughPhysiqueType extends PhysiqueType {
    @Override
    public MapCodec<? extends Physique> codec() {
        return RecordCodecBuilder.<InfiniteBreakthroughPhysique>mapCodec(instance ->
                instance.group(
                        ComponentSerialization.CODEC.fieldOf("name").forGetter(InfiniteBreakthroughPhysique::name),
                        ComponentSerialization.CODEC.fieldOf("description").forGetter(InfiniteBreakthroughPhysique::description),
                        Identifier.CODEC.listOf().fieldOf("paths").forGetter(InfiniteBreakthroughPhysique::unlockedPaths),
                        Identifier.CODEC.listOf().optionalFieldOf("skills", List.of()).forGetter(InfiniteBreakthroughPhysique::skills),
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
                        ).optionalFieldOf("base_stats",List.of()).forGetter(InfiniteBreakthroughPhysique::baseStats),
                        ValueContainerModifier.MAP_CODEC.optionalFieldOf("stat_modifiers", Map.of()).forGetter(InfiniteBreakthroughPhysique::statModifiers),
                        PathBonusBase.CODEC.optionalFieldOf("base_path_bonuses",List.of()).forGetter(InfiniteBreakthroughPhysique::basePathBonuses),
                        PathBonusModifier.CODEC.optionalFieldOf("path_bonuse_modifiers",List.of()).forGetter(InfiniteBreakthroughPhysique::pathBonusModifiers),
                        Identifier.CODEC.fieldOf("infinite_path").forGetter(InfiniteBreakthroughPhysique::path),
                        Codec.INT.fieldOf("infinite_realm").forGetter(InfiniteBreakthroughPhysique::infiniteRealm),
                        AscensionItemTooltipDefinition.CODEC.optionalFieldOf("item_tooltip").forGetter(InfiniteBreakthroughPhysique::itemTooltip)
                ).apply(instance, InfiniteBreakthroughPhysique::new)
        );
    }


}