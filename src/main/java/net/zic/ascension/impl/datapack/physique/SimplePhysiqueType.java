package net.zic.ascension.impl.datapack.physique;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.path.bonus.PathBonus;
import net.zic.ascension.api.ascension.core.path.bonus.PathBonusHolder;
import net.zic.ascension.api.ascension.core.physique.Physique;
import net.zic.ascension.api.ascension.core.requirement.RequirementHolder;
import net.zic.ascension.api.ascension.datapack.path.PathBonusBase;
import net.zic.ascension.api.ascension.datapack.path.PathBonusModifier;
import net.zic.ascension.api.ascension.datapack.physique.PhysiqueType;
import net.zic.ascension.api.tooltip.AscensionItemTooltipDefinition;
import net.zic.ascension.impl.core.physique.SimplePhysique;
import net.zic.zenithlib.value_containers.typed.ModifierHolder;
import net.zic.zenithlib.value_containers.typed.ValueContainerCodecHelper;

import java.util.HashMap;
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
                        ValueContainerCodecHelper.containersCodec(Codec.DOUBLE).optionalFieldOf("stats",Map.of()).forGetter(SimplePhysique::statModifiers),
                        PathBonusHolder.MODIFIER_CODEC.optionalFieldOf("path_bonus",Map.of()).forGetter(SimplePhysique::pathBonusModifiers),
                        AscensionItemTooltipDefinition.CODEC.optionalFieldOf("item_tooltip").forGetter(SimplePhysique::itemTooltip),
                        RequirementHolder.CODEC.optionalFieldOf("requirements", RequirementHolder.EMPTY).forGetter(SimplePhysique::requirements)
                ).apply(instance, SimplePhysique::new)
        );
    }


}
