package net.zic.ascension.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.datagen.tooltips.AscClassificationDataProvider;
import net.zic.ascension.datagen.tooltips.AscTooltipDataProvider;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public class AscDataGen {
    @SubscribeEvent
    public static void gatherClientData(GatherDataEvent.Client event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        var lookupProvider = event.getLookupProvider();

        generator.addProvider(true, new AscLangProvider(packOutput));
        generator.addProvider(true, new AscModelProvider(packOutput));
        generator.addProvider(true, new AscBlockTagProvider(packOutput, lookupProvider));
        generator.addProvider(true, new AscItemTagProvider(packOutput, lookupProvider));
        generator.addProvider(true, new AscEntityTypeTagProvider(packOutput, lookupProvider));
        generator.addProvider(true, new AscTooltipDataProvider(packOutput, AscensionCraft.MOD_ID));
        generator.addProvider(true, new AscClassificationDataProvider(packOutput, AscensionCraft.MOD_ID));

    }



}
