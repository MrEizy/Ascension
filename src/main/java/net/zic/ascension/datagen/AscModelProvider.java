package net.zic.ascension.datagen;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.data.PackOutput;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.item.ModItems;

public class AscModelProvider extends ModelProvider {
    public AscModelProvider(PackOutput output) {
        super(output, AscensionCraft.MOD_ID);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        itemModels.generateFlatItem(ModItems.TABLET_OF_DESTRUCTION_HUMAN.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.TABLET_OF_DESTRUCTION_EARTH.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.TABLET_OF_DESTRUCTION_HEAVEN.get(), ModelTemplates.FLAT_ITEM);

        itemModels.generateFlatItem(ModItems.BLOODLINE_ESSENCE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.PHYSIQUE_ESSENCE.get(), ModelTemplates.FLAT_ITEM);

        itemModels.generateFlatItem(ModItems.TECHNIQUE_MANUAL.get(), ModelTemplates.FLAT_ITEM);
    }
}
