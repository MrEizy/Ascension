package net.zic.ascension.impl.datapack.bloodline;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.bloodline.Bloodline;
import net.zic.ascension.api.ascension.core.progression.ProgressActionHolder;
import net.zic.ascension.api.ascension.core.requirement.RequirementHolder;
import net.zic.ascension.api.ascension.datapack.bloodline.BloodlineType;
import net.zic.ascension.api.tooltip.AscensionItemTooltipDefinition;
import net.zic.ascension.impl.core.bloodline.SimpleBloodline;

public class SimpleBloodlineType extends BloodlineType {


    @Override
    public MapCodec<? extends Bloodline<?>> codec() {
        return RecordCodecBuilder.<SimpleBloodline>mapCodec(instance ->
                instance.group(
                        ComponentSerialization.CODEC.fieldOf("name").forGetter(bloodline -> bloodline.getName(null)),
                        ComponentSerialization.CODEC.fieldOf("description").forGetter(bloodline -> bloodline.getDescription(null)),
                        Identifier.CODEC.listOf().optionalFieldOf("paths", java.util.List.of()).forGetter(SimpleBloodline::unlockedPaths),
                        ProgressActionHolder.PROGRESS_HOLDER_CODEC.fieldOf("purity_handler").forGetter(SimpleBloodline::getHolder),
                        AscensionItemTooltipDefinition.CODEC.optionalFieldOf("item_tooltip").forGetter(SimpleBloodline::itemTooltip),
                        RequirementHolder.CODEC.optionalFieldOf("requirements", RequirementHolder.EMPTY).forGetter(SimpleBloodline::requirements)
                ).apply(instance, SimpleBloodline::new)
        );
    }
}
