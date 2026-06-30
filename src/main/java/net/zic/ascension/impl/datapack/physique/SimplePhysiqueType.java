package net.zic.ascension.impl.datapack.physique;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.core.physique.Physique;
import net.zic.ascension.api.core.physique.PhysiqueData;
import net.zic.ascension.api.datapack.physique.PhysiqueType;
import net.zic.ascension.api.tooltip.AscensionItemTooltipDefinition;
import net.zic.ascension.impl.core.physique.EmptyPhysiqueData;
import net.zic.ascension.impl.core.physique.SimplePhysique;
import net.zic.ascension.impl.datapack.util.AffinityModifier;
import net.zic.ascension.impl.datapack.util.BaseAffinity;
import net.zic.zenithlib.value_containers.ValueContainer;
import net.zic.zenithlib.value_containers.ValueContainerModifier;

import java.util.List;
import java.util.Map;

public class SimplePhysiqueType extends PhysiqueType {
    @Override
    public MapCodec<? extends Physique> codec() {
        return RecordCodecBuilder.<SimplePhysique>mapCodec(instance ->
                instance.group(
                        ComponentSerialization.CODEC.fieldOf("name").forGetter(SimplePhysique::name),
                        ComponentSerialization.CODEC.fieldOf("description").forGetter(SimplePhysique::description),
                        Identifier.CODEC.listOf().fieldOf("paths").forGetter(SimplePhysique::unlockedPaths),
                        Identifier.CODEC.listOf().optionalFieldOf("skills", List.of()).forGetter(SimplePhysique::skills),
                        ValueContainer.BASE_MODIFIER_CODEC.listOf().optionalFieldOf("base_stats",List.of()).forGetter(SimplePhysique::baseStats),
                        ValueContainerModifier.MAP_CODEC.optionalFieldOf("stat_modifiers", Map.of()).forGetter(SimplePhysique::statModifiers),
                        BaseAffinity.CODEC.listOf().optionalFieldOf("base_affinity",List.of()).forGetter(SimplePhysique::baseAffinities),
                        Codec.unboundedMap(Identifier.CODEC,AffinityModifier.CODEC.listOf()).optionalFieldOf("affinity_modifiers",Map.of()).forGetter(SimplePhysique::affinityModifiers),
                        AscensionItemTooltipDefinition.CODEC.optionalFieldOf("item_tooltip").forGetter(SimplePhysique::itemTooltip)
                ).apply(instance, SimplePhysique::new)
        );
    }


}
