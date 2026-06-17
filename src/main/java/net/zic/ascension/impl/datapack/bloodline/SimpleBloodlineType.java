package net.zic.ascension.impl.datapack.bloodline;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.ComponentSerialization;
import net.zic.ascension.api.core.bloodline.Bloodline;
import net.zic.ascension.api.core.bloodline.BloodlineData;
import net.zic.ascension.api.core.progression.ProgressActionHolder;
import net.zic.ascension.api.datapack.bloodline.BloodlineType;
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
                        ProgressActionHolder.PROGRESS_HOLDER_CODEC.fieldOf("purity_handler").forGetter(SimpleBloodline::getHolder),
                        AscensionItemTooltipDefinition.CODEC.optionalFieldOf("item_tooltip").forGetter(SimpleBloodline::itemTooltip)
                ).apply(instance, SimpleBloodline::new)
        );
    }

    @Override
    public MapCodec<? extends BloodlineData> dataCodec() {
        return MapCodec.unit(new SimpleBloodlineData());
    }
}
