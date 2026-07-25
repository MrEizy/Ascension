package net.zic.ascension.datagen;

import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.world.damagesource.DamageEffects;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DeathMessageType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.datagen.tooltips.AscClassificationDataProvider;
import net.zic.ascension.datagen.tooltips.AscTooltipDataProvider;
import net.zic.ascension.handler.AscensionDamageHandler;

import java.util.Set;

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
        event.createDatapackRegistryObjects(
                buildDamageTypes()

        );
    }


    public static RegistrySetBuilder buildDamageTypes(){
        return new RegistrySetBuilder()
                .add(Registries.DAMAGE_TYPE, bootstrap->{

                    bootstrap.register(AscensionDamageHandler.PATH_TYPE,
                            new DamageType(
                                    AscensionDamageHandler.PATH_TYPE.identifier().toString(),
                                    DamageScaling.NEVER,
                                    0.1f,
                                    DamageEffects.HURT,
                                    DeathMessageType.DEFAULT)
                    );
                });
    }
}
