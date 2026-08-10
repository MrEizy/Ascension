package net.zic.ascension.datagen;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiPartGenerator;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.blocks.ModBlocks;
import net.zic.ascension.common.blocks.crops.herbs.HerbCropBlock;
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

        itemModels.generateFlatItem(ModItems.JADE_BOTTLE.get(), ModelTemplates.FLAT_ITEM);


        //Key Items
        itemModels.generateFlatItem(ModItems.BLOODLINE_ESSENCE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.PHYSIQUE_ESSENCE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.TECHNIQUE_MANUAL.get(), ModelTemplates.FLAT_ITEM);


        //Herb Items
        herbItemModel(itemModels, ModItems.JADE_DEW_GRASS.get());
        itemModels.generateFlatItem(ModItems.JADE_DEW_GRASS_SEEDS.get(), ModelTemplates.FLAT_ITEM);
        herbItemModel(itemModels, ModItems.GINSENG.get(), "ginseng");
        herbItemModel(itemModels, ModItems.FIRE_GINSENG.get(), "fire_ginseng");
        herbItemModel(itemModels, ModItems.SNOW_GINSENG.get(), "snow_ginseng");
        herbItemModel(itemModels, ModItems.LINGZHI_MUSHROOM.get());
        herbItemModel(itemModels, ModItems.BLOOD_LINGZHI_MUSHROOM.get());


        //Herb Blocks
        herbBlockModelRotated(blockModels, ModBlocks.LINGZHI_MUSHROOM_B.get());
        herbBlockModelRotated(blockModels, ModBlocks.BLOOD_LINGZHI_MUSHROOM_B.get());
        cultivationSoilModel(blockModels);
        herbCropModel(blockModels, ModBlocks.JADE_DEW_GRASS_CROP.get());
        herbCropModel(blockModels, ModBlocks.GINSENG_CROP.get(), "hundred_year_ginseng");
        herbCropModel(blockModels, ModBlocks.FIRE_GINSENG_CROP.get(), "hundred_year_fire_ginseng");
        herbCropModel(blockModels, ModBlocks.SNOW_GINSENG_CROP.get(), "hundred_year_snow_ginseng");


        //Ore Models
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


    private void cultivationSoilModel(BlockModelGenerators blockModels) {
        Block block = ModBlocks.CULTIVATION_SOIL.get();
        Identifier farmlandModel = Identifier.fromNamespaceAndPath("minecraft", "block/farmland");
        blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(block, BlockModelGenerators.plainVariant(farmlandModel)));
        blockModels.registerSimpleItemModel(block, farmlandModel);
    }


    private void herbCropModel(BlockModelGenerators blockModels, HerbCropBlock block) {
        herbCropModel(blockModels, block, block.definition().id().getPath());
    }

    private void herbCropModel(BlockModelGenerators blockModels, HerbCropBlock block, String texturePath) {
        int growthStages = block.definition().growthStages();
        Identifier[] stageModels = new Identifier[growthStages];
        String herbPath = block.definition().id().getPath();

        for (int visualStage = 0; visualStage < growthStages; visualStage++) {
            Identifier model = Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "block/herbs/" + herbPath + "_stage" + visualStage);
            Material texture = new Material(Identifier.fromNamespaceAndPath(
                    AscensionCraft.MOD_ID,
                    "block/herbs/" + texturePath + "_stage" + visualStage
            ));
            stageModels[visualStage] = ModelTemplates.CROSS.create(
                    model,
                    TextureMapping.cross(texture),
                    blockModels.modelOutput
            );
        }

        MultiPartGenerator blockState = MultiPartGenerator.multiPart(block);

        for (int stage = 0; stage <= HerbCropBlock.MAX_STAGE; stage++) {
            int visualStage = Math.min(stage, block.definition().maxGrowthStage());
            blockState.with(
                    BlockModelGenerators.condition().term(HerbCropBlock.STAGE, stage),
                    BlockModelGenerators.plainVariant(stageModels[visualStage])
            );
        }

        blockModels.blockStateOutput.accept(blockState);
    }

    private Material herbItemTexture(Item item) {
        String name = BuiltInRegistries.ITEM.getKey(item).getPath();
        return new Material(Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "item/herbs/" + name));
    }

    private Material herbBlockTexture(Block block) {
        String name = BuiltInRegistries.BLOCK.getKey(block).getPath();
        return new Material(Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "block/herbs/" + name));
    }

    private void herbItemModel(ItemModelGenerators itemModels, Item item) {
        herbItemModel(itemModels, item, BuiltInRegistries.ITEM.getKey(item).getPath());
    }

    private void herbItemModel(ItemModelGenerators itemModels, Item item, String texturePath) {
        Identifier model = ModelTemplates.FLAT_ITEM.create(
                ModelLocationUtils.getModelLocation(item),
                TextureMapping.layer0(new Material(Identifier.fromNamespaceAndPath(
                        AscensionCraft.MOD_ID,
                        "item/herbs/" + texturePath
                ))),
                itemModels.modelOutput);
        itemModels.itemModelOutput.accept(item, ItemModelUtils.plainModel(model));
    }

    private void herbBlockModel(BlockModelGenerators blockModels, Block block) {
        Identifier model = ModelTemplates.CUBE_ALL.create(
                ModelLocationUtils.getModelLocation(block),
                TextureMapping.cube(herbBlockTexture(block)),
                blockModels.modelOutput);
        blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(block, BlockModelGenerators.plainVariant(model)));
        blockModels.registerSimpleItemModel(block, model);
    }

    private void herbBlockModelRotated(BlockModelGenerators blockModels, Block block) {
        Identifier model = herbBlockLocation(block);
        blockModels.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(block, BlockModelGenerators.plainVariant(model))
                        .with(BlockModelGenerators.ROTATION_HORIZONTAL_FACING));
        blockModels.registerSimpleItemModel(block, model);
    }

    private Identifier herbBlockLocation(Block block) {
        String name = BuiltInRegistries.BLOCK.getKey(block).getPath();
        return Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "block/herbs/" + name);
    }
}