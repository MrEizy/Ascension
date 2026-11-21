package net.zic.ascension.impl.datapack.physique;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.path.bonus.PathBonusHolder;
import net.zic.ascension.api.ascension.core.physique.Physique;
import net.zic.ascension.api.ascension.core.requirement.RequirementHolder;
import net.zic.ascension.api.ascension.datapack.physique.PhysiqueType;
import net.zic.ascension.api.tooltip.AscensionItemTooltipDefinition;
import net.zic.ascension.impl.core.evolution.EvolutionRequirements;
import net.zic.ascension.impl.core.physique.EvolutionPhysique;
import net.zic.zenithlib.value_containers.typed.ValueContainerCodecHelper;

import java.util.List;
import java.util.Map;

/**
 * ascension:evolution_physique. Same fields as simple physique plus an "evolution" object, see EvolutionRequirements.
 */
public class EvolutionPhysiqueType extends PhysiqueType {
    @Override
    public MapCodec<? extends Physique<?>> codec() {
        return RecordCodecBuilder.<EvolutionPhysique>mapCodec(instance -> instance.group(
                ComponentSerialization.CODEC.fieldOf("name").forGetter(EvolutionPhysique::name),
                ComponentSerialization.CODEC.fieldOf("description").forGetter(EvolutionPhysique::description),
                Identifier.CODEC.listOf().fieldOf("paths").forGetter(EvolutionPhysique::unlockedPaths),
                Identifier.CODEC.listOf().optionalFieldOf("skills", List.of()).forGetter(EvolutionPhysique::skills),
                ValueContainerCodecHelper.containersCodec(Codec.DOUBLE).optionalFieldOf("stats", Map.of()).forGetter(EvolutionPhysique::statModifiers),
                PathBonusHolder.MODIFIER_CODEC.optionalFieldOf("path_bonus", Map.of()).forGetter(EvolutionPhysique::pathBonusModifiers),
                AscensionItemTooltipDefinition.CODEC.optionalFieldOf("item_tooltip").forGetter(EvolutionPhysique::itemTooltip),
                RequirementHolder.CODEC.optionalFieldOf("requirements", RequirementHolder.EMPTY).forGetter(EvolutionPhysique::requirements),
                EvolutionRequirements.PHYSIQUE_CODEC.fieldOf("evolution").forGetter(EvolutionPhysique::evolution)
        ).apply(instance, EvolutionPhysique::new));
    }
}