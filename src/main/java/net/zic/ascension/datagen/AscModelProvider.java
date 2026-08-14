package net.zic.ascension.datagen;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiPartGenerator;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.*;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.blocks.ModBlocks;
import net.zic.ascension.common.blocks.crops.herbs.HerbCropBlock;
import net.zic.ascension.common.blocks.crops.herbs.PodHerbBlock;
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
        herbItemModel(itemModels, ModItems.NINE_SUN_FIRE_ROOT.get());
        herbItemModel(itemModels, ModItems.MOONWELL_JADE_LOTUS.get());
        herbItemModel(itemModels, ModItems.HEAVENLY_THUNDER_PEACH.get());
        herbItemModel(itemModels, ModItems.LINGZHI_MUSHROOM.get());
        herbItemModel(itemModels, ModItems.BLOOD_LINGZHI_MUSHROOM.get());
        herbItemModel(itemModels, ModItems.WHITE_JADE_ORCHID.get());
        herbItemModel(itemModels, ModItems.PEACH.get());


        //Herb Blocks
        herbBlockModelRotated(blockModels, ModBlocks.LINGZHI_MUSHROOM_B.get());
        herbBlockModelRotated(blockModels, ModBlocks.BLOOD_LINGZHI_MUSHROOM_B.get());
        cultivationSoilModel(blockModels);
        herbCropModel(blockModels, ModBlocks.JADE_DEW_GRASS_CROP.get());
        herbCropModel(blockModels, ModBlocks.GINSENG_CROP.get(), "ginseng");
        herbCropModel(blockModels, ModBlocks.FIRE_GINSENG_CROP.get(), "hundred_year_fire_ginseng");
        herbCropModel(blockModels, ModBlocks.SNOW_GINSENG_CROP.get(), "hundred_year_snow_ginseng");
        herbCropModel(blockModels, ModBlocks.NINE_SUN_FIRE_ROOT_CROP.get());
        herbCropModel(blockModels, ModBlocks.MOONWELL_JADE_LOTUS_CROP.get());
        herbCropModel(blockModels, ModBlocks.WHITE_JADE_ORCHID_CROP.get(), "white_jade_orchid");
        podHerbModel(blockModels, ModBlocks.PEACH_POD.get(), "peach");
        podHerbModel(blockModels, ModBlocks.HEAVENLY_THUNDER_PEACH_POD.get(), "heavenly_thunder_peach");


        //Ore Models
        itemModels.generateFlatItem(ModItems.JADE.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.RAW_FROST_SILVER.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.FROST_SILVER_NUGGET.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.FROST_SILVER_INGOT.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.RAW_BLACK_IRON.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.BLACK_IRON_INGOT.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(ModItems.BLACK_IRON_NUGGET.get(), ModelTemplates.FLAT_ITEM);



        //Fluids
        itemModels.generateFlatItem(ModItems.LIQUIFIED_SPIRITUAL_QI_BUCKET.get(), ModelTemplates.FLAT_ITEM);




        //Trees
        blockModels.createTrivialCube(ModBlocks.PEACH_PLANKS.get());
        blockModels.woodProvider(ModBlocks.PEACH_LOG.get()).logWithHorizontal(ModBlocks.PEACH_LOG.get()).wood(ModBlocks.PEACH_WOOD.get());
        blockModels.woodProvider(ModBlocks.STRIPPED_PEACH_LOG.get()).logWithHorizontal(ModBlocks.STRIPPED_PEACH_LOG.get()).wood(ModBlocks.STRIPPED_PEACH_WOOD.get());

        blockModels.createTintedLeaves(ModBlocks.PEACH_LEAVES.get(), TexturedModel.LEAVES, -12012255);
        blockModels.createPlantWithDefaultItem(ModBlocks.PEACH_SAPLING.get(), ModBlocks.POTTED_PEACH_SAPLING.get(), BlockModelGenerators.PlantType.TINTED);






        //Blocks
        blockModels.createTrivialCube(ModBlocks.JADE_ORE.get());
        blockModels.createTrivialCube(ModBlocks.JADE_BLOCK.get());
        blockModels.createTrivialCube(ModBlocks.FROST_SILVER_ORE.get());
        blockModels.createTrivialCube(ModBlocks.FROST_SILVER_BLOCK.get());
        blockModels.createTrivialCube(ModBlocks.BLACK_IRON_ORE.get());
        blockModels.createTrivialCube(ModBlocks.BLACK_IRON_BLOCK.get());


        //Block Entities
        fermentingBarrelModel(blockModels);

        //Fluids
        blockModels.createNonTemplateModelBlock(ModBlocks.LIQUIFIED_SPIRITUAL_QI_BLOCK.get());
    }

    private void podHerbModel(BlockModelGenerators blockModels, PodHerbBlock block, String texturePath) {
        int growthStages = block.definition().growthStages();
        Identifier[] stageModels = new Identifier[growthStages];

        for (int visualStage = 0; visualStage < growthStages; visualStage++) {
            stageModels[visualStage] = Identifier.fromNamespaceAndPath(
                    AscensionCraft.MOD_ID,
                    "block/herbs/" + texturePath + "_stage_" + visualStage
            );
        }

        MultiPartGenerator blockState = MultiPartGenerator.multiPart(block);

        for (int stage = 0; stage <= PodHerbBlock.MAX_STAGE; stage++) {
            int visualStage = Math.min(stage, block.definition().maxGrowthStage());
            MultiVariant base = BlockModelGenerators.plainVariant(stageModels[visualStage]);

            blockState.with(
                    BlockModelGenerators.condition()
                            .term(PodHerbBlock.STAGE, stage)
                            .term(PodHerbBlock.FACING, Direction.NORTH),
                    base);
            blockState.with(
                    BlockModelGenerators.condition()
                            .term(PodHerbBlock.STAGE, stage)
                            .term(PodHerbBlock.FACING, Direction.EAST),
                    base.with(BlockModelGenerators.Y_ROT_90));
            blockState.with(
                    BlockModelGenerators.condition()
                            .term(PodHerbBlock.STAGE, stage)
                            .term(PodHerbBlock.FACING, Direction.SOUTH),
                    base.with(BlockModelGenerators.Y_ROT_180));
            blockState.with(
                    BlockModelGenerators.condition()
                            .term(PodHerbBlock.STAGE, stage)
                            .term(PodHerbBlock.FACING, Direction.WEST),
                    base.with(BlockModelGenerators.Y_ROT_270));
        }

        blockModels.blockStateOutput.accept(blockState);

        blockModels.registerSimpleItemModel(block, stageModels[3]);
    }

    private void fermentingBarrelModel(BlockModelGenerators blockModels) {
        Block block = ModBlocks.FERMENTING_BARREL.get();
        String path = "block/fermenting_barrel";

        Material top = new Material(Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, path + "_top"));
        Material bottom = new Material(Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, path + "_bottom"));
        Material side = new Material(Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, path + "_side"));

        TextureMapping textures = new TextureMapping()
                .put(TextureSlot.SIDE, side)
                .put(TextureSlot.TOP, top)
                .put(TextureSlot.BOTTOM, bottom);

        Identifier model = ModelTemplates.CUBE_BOTTOM_TOP.create(
                ModelLocationUtils.getModelLocation(block),
                textures,
                blockModels.modelOutput);

        MultiVariant baseVariant = BlockModelGenerators.plainVariant(model);

        var xRot180 = BlockModelGenerators.X_ROT_90.then(BlockModelGenerators.X_ROT_90);
        var xRot270 = xRot180.then(BlockModelGenerators.X_ROT_90);
        var yRot270 = BlockModelGenerators.Y_ROT_90.then(BlockModelGenerators.Y_ROT_90).then(BlockModelGenerators.Y_ROT_90);

        blockModels.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(block, baseVariant)
                        .with(PropertyDispatch.modify(DirectionalBlock.FACING)
                                .select(Direction.UP, BlockModelGenerators.NOP)
                                .select(Direction.DOWN, xRot180)
                                .select(Direction.NORTH, xRot270)
                                .select(Direction.SOUTH, BlockModelGenerators.X_ROT_90)
                                .select(Direction.EAST, BlockModelGenerators.X_ROT_90.then(yRot270))
                                .select(Direction.WEST, BlockModelGenerators.X_ROT_90.then(BlockModelGenerators.Y_ROT_90))));

        blockModels.registerSimpleItemModel(block, model);
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
            stageModels[visualStage] = ModelTemplates.CROP.create(
                    model,
                    TextureMapping.crop(texture),
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