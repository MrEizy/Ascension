package net.zic.ascension.impl.datapack.technique;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.ComponentSerialization;
import net.zic.ascension.api.ascension.core.progression.ProgressActionList;
import net.zic.ascension.api.ascension.core.requirement.RequirementHolder;
import net.zic.ascension.api.ascension.core.technique.Technique;
import net.zic.ascension.api.ascension.datapack.technique.TechniqueType;
import net.zic.ascension.api.tooltip.AscensionItemTooltipDefinition;
import net.zic.ascension.impl.core.technique.SimpleTechnique;

public class SimpleTechniqueType extends TechniqueType {
    @Override
    public MapCodec<? extends Technique<?>> codec() {
        return RecordCodecBuilder.<SimpleTechnique>mapCodec(instance -> instance.group(
                ComponentSerialization.CODEC.fieldOf("name").forGetter(technique -> technique.getName(null)),
                ComponentSerialization.CODEC.fieldOf("description").forGetter(technique -> technique.getDescription(null)),
                ProgressActionList.CODEC.optionalFieldOf("on_added", ProgressActionList.EMPTY).forGetter(SimpleTechnique::getOnAddedActions),
                AscensionItemTooltipDefinition.CODEC.optionalFieldOf("item_tooltip").forGetter(Technique::itemTooltip),
                RequirementHolder.CODEC.optionalFieldOf("requirements", RequirementHolder.EMPTY).forGetter(Technique::requirements)
        ).apply(instance, SimpleTechnique::new));
    }
}
