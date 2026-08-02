package net.zic.ascension.datagen;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.blocks.ModBlocks;
import net.zic.ascension.common.item.ModItems;

public class AscModelProvider extends ModelProvider {
    public AscModelProvider(PackOutput output) {
        super(output, AscensionCraft.MOD_ID);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {



        //Items
        Item tabletTexture = ModItems.TABLET_OF_DESTRUCTION_HUMAN.get();
        itemModels.generateFlatItem(ModItems.TABLET_OF_DESTRUCTION_HUMAN.get(), tabletTexture, ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.TABLET_OF_DESTRUCTION_EARTH.get(), tabletTexture, ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.TABLET_OF_DESTRUCTION_HEAVEN.get(), tabletTexture, ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.TABLET_OF_DESTRUCTION_ASCENDANT.get(), tabletTexture, ModelTemplates.FLAT_ITEM);

        itemModels.generateFlatItem(ModItems.BLOODLINE_ESSENCE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.PHYSIQUE_ESSENCE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.TECHNIQUE_MANUAL.get(), ModelTemplates.FLAT_ITEM);


        itemModels.generateFlatItem(ModItems.JADE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.RAW_FROST_SILVER.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.FROST_SILVER_NUGGET.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.FROST_SILVER_INGOT.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.RAW_BLACK_IRON.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.BLACK_IRON_INGOT.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.BLACK_IRON_NUGGET.get(), ModelTemplates.FLAT_ITEM);







        //Blocks
        blockModels.createTrivialCube(ModBlocks.JADE_ORE.get());
        blockModels.createTrivialCube(ModBlocks.JADE_BLOCK.get());
        blockModels.createTrivialCube(ModBlocks.FROST_SILVER_ORE.get());
        blockModels.createTrivialCube(ModBlocks.FROST_SILVER_BLOCK.get());
        blockModels.createTrivialCube(ModBlocks.BLACK_IRON_ORE.get());
        blockModels.createTrivialCube(ModBlocks.BLACK_IRON_BLOCK.get());
    }
}
