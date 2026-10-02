package net.zic.ascension.impl.datapack.bloodline;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.bloodline.Bloodline;
import net.zic.ascension.api.ascension.core.bloodline.BloodlineData;
import net.zic.ascension.api.ascension.core.progression.ProgressActionHolder;
import net.zic.ascension.api.ascension.core.requirement.RequirementHolder;
import net.zic.ascension.api.ascension.datapack.bloodline.BloodlineType;
import net.zic.ascension.api.tooltip.AscensionItemTooltipDefinition;
import net.zic.ascension.impl.core.bloodline.SimpleBloodline;
import net.zic.ascension.impl.core.bloodline.SimpleBloodlineData;

public class SimpleBloodlineType extends BloodlineType {


    @Override
    public MapCodec<? extends Bloodline> codec() {
        return RecordCodecBuilder.<SimpleBloodline>mapCodec(instance ->
                instance.group(
                        ComponentSerialization.CODEC.fieldOf("name").forGetter(SimpleBloodline::getName),
                        ComponentSerialization.CODEC.fieldOf("description").forGetter(SimpleBloodline::getDescription),
                        Identifier.CODEC.listOf().optionalFieldOf("paths", java.util.List.of()).forGetter(SimpleBloodline::unlockedPaths),
                        ProgressActionHolder.PROGRESS_HOLDER_CODEC.fieldOf("purity_handler").forGetter(SimpleBloodline::getHolder),
                        AscensionItemTooltipDefinition.CODEC.optionalFieldOf("item_tooltip").forGetter(SimpleBloodline::itemTooltip),
                        RequirementHolder.CODEC.optionalFieldOf("requirements", RequirementHolder.EMPTY).forGetter(SimpleBloodline::requirements)
                ).apply(instance, SimpleBloodline::new)
        );
    }

    @Override
    public MapCodec<? extends BloodlineData> dataCodec() {
        return MapCodec.unit(new SimpleBloodlineData());
    }
}
