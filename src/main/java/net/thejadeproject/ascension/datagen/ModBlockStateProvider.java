package net.thejadeproject.ascension.datagen;

import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.client.model.generators.MultiPartBlockStateBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.thejadeproject.ascension.AscensionCraft;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.thejadeproject.ascension.common.blocks.ModBlocks;
import net.thejadeproject.ascension.common.blocks.custom.crops.GenericSlowCropBlock;
import net.thejadeproject.ascension.common.blocks.custom.crops.StemSlowCropBlock;
import net.thejadeproject.ascension.common.blocks.custom.crops.jadedew.JadeDewGrassCropBlock;
import net.thejadeproject.ascension.common.blocks.custom.fires.CrimsonLotusFire;

import java.util.function.Function;


public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper){
        super(output, AscensionCraft.MOD_ID, exFileHelper);
    }
    @Override
    protected void registerStatesAndModels() {

        //Fires

        //fireBlock(ModBlocks.CRIMSON_LOTUS_FIRE, "crimson_lotus_fire");

        //Crops
        makeCrop(((CropBlock) ModBlocks.HUNDRED_YEAR_GINSENG_CROP.get()), "hundred_year_ginseng_stage", "hundred_year_ginseng_stage");
        makeCrop(((CropBlock) ModBlocks.HUNDRED_YEAR_SNOW_GINSENG_CROP.get()), "hundred_year_snow_ginseng_stage", "hundred_year_snow_ginseng_stage");
        makeCrop(((CropBlock) ModBlocks.HUNDRED_YEAR_FIRE_GINSENG_CROP.get()), "hundred_year_fire_ginseng_stage", "hundred_year_fire_ginseng_stage");
        makeStemCrop(((CropBlock) ModBlocks.WHITE_JADE_ORCHID_CROP.get()), "white_jade_orchid_stage", "white_jade_orchid_stage");

        makeJadeDewGrassCrop(((CropBlock) ModBlocks.JADE_DEW_GRASS_CROP.get()), "jade_dew_grass_stage", "jade_dew_grass_stage");

        //Herbs Blocks



        //Ores
        simpleBlockWithItem(ModBlocks.BLACK_IRON_ORE);
        simpleBlockWithItem(ModBlocks.BLACK_IRON_BLOCK);
        simpleBlockWithItem(ModBlocks.FROST_SILVER_ORE);
        simpleBlockWithItem(ModBlocks.FROST_SILVER_BLOCK);
        simpleBlockWithItem(ModBlocks.JADE_ORE);
        simpleBlockWithItem(ModBlocks.JADE_BLOCK);
        simpleBlockWithItem(ModBlocks.SPIRITUAL_STONE_BLOCK);


        simpleBlockWithItem(ModBlocks.SPIRIT_VEIN);



        /** Marble */
        simpleBlockWithItem(ModBlocks.MARBLE_BRICKS);
        simpleBlockWithItem(ModBlocks.MARBLE_TILES);
        simpleBlockWithItem(ModBlocks.MARBLE_CHISELED);
        simpleBlockWithItem(ModBlocks.RAW_MARBLE);
        simpleBlockWithItem(ModBlocks.POLISHED_MARBLE);
        simpleBlockWithItem(ModBlocks.POLISHED_BURNED_MARBLE);
        simpleBlockWithItem(ModBlocks.CHARRED_MARBLE);
        simpleBlockWithItem(ModBlocks.CHARRED_MARBLE_BRICKS);
        simpleBlockWithItem(ModBlocks.CHARRED_MARBLE_CHISELED);
        simpleBlockWithItem(ModBlocks.CHARRED_MARBLE_TILES);

        simpleBlockWithItem(ModBlocks.BLUE_MARBLE);
        simpleBlockWithItem(ModBlocks.BLUE_MARBLE_BRICKS);
        simpleBlockWithItem(ModBlocks.BLUE_MARBLE_CHISELED);
        simpleBlockWithItem(ModBlocks.BLUE_MARBLE_TILES);
        simpleBlockWithItem(ModBlocks.BLUE_POLISHED_MARBLE);
        simpleBlockWithItem(ModBlocks.BROWN_MARBLE);
        simpleBlockWithItem(ModBlocks.BROWN_MARBLE_BRICKS);
        simpleBlockWithItem(ModBlocks.BROWN_MARBLE_CHISELED);
        simpleBlockWithItem(ModBlocks.BROWN_MARBLE_TILES);
        simpleBlockWithItem(ModBlocks.BROWN_POLISHED_MARBLE);
        simpleBlockWithItem(ModBlocks.CYAN_MARBLE);
        simpleBlockWithItem(ModBlocks.CYAN_MARBLE_BRICKS);
        simpleBlockWithItem(ModBlocks.CYAN_MARBLE_CHISELED);
        simpleBlockWithItem(ModBlocks.CYAN_MARBLE_TILES);
        simpleBlockWithItem(ModBlocks.CYAN_POLISHED_MARBLE);
        simpleBlockWithItem(ModBlocks.GRAY_MARBLE);
        simpleBlockWithItem(ModBlocks.GRAY_MARBLE_BRICKS);
        simpleBlockWithItem(ModBlocks.GRAY_MARBLE_CHISELED);
        simpleBlockWithItem(ModBlocks.GRAY_MARBLE_TILES);
        simpleBlockWithItem(ModBlocks.GRAY_POLISHED_MARBLE);
        simpleBlockWithItem(ModBlocks.GREEN_MARBLE);
        simpleBlockWithItem(ModBlocks.GREEN_MARBLE_BRICKS);
        simpleBlockWithItem(ModBlocks.GREEN_MARBLE_CHISELED);
        simpleBlockWithItem(ModBlocks.GREEN_MARBLE_TILES);
        simpleBlockWithItem(ModBlocks.GREEN_POLISHED_MARBLE);
        simpleBlockWithItem(ModBlocks.LIGHT_BLUE_MARBLE);
        simpleBlockWithItem(ModBlocks.LIGHT_BLUE_MARBLE_BRICKS);
        simpleBlockWithItem(ModBlocks.LIGHT_BLUE_MARBLE_CHISELED);
        simpleBlockWithItem(ModBlocks.LIGHT_BLUE_MARBLE_TILES);
        simpleBlockWithItem(ModBlocks.LIGHT_BLUE_POLISHED_MARBLE);
        simpleBlockWithItem(ModBlocks.LIGHT_GRAY_MARBLE);
        simpleBlockWithItem(ModBlocks.LIGHT_GRAY_MARBLE_BRICKS);
        simpleBlockWithItem(ModBlocks.LIGHT_GRAY_MARBLE_CHISELED);
        simpleBlockWithItem(ModBlocks.LIGHT_GRAY_MARBLE_TILES);
        simpleBlockWithItem(ModBlocks.POLISHED_LIGHT_GRAY_MARBLE);
        simpleBlockWithItem(ModBlocks.LIME_MARBLE);
        simpleBlockWithItem(ModBlocks.LIME_MARBLE_BRICKS);
        simpleBlockWithItem(ModBlocks.LIME_MARBLE_CHISELED);
        simpleBlockWithItem(ModBlocks.LIME_MARBLE_TILES);
        simpleBlockWithItem(ModBlocks.LIME_POLISHED_MARBLE);
        simpleBlockWithItem(ModBlocks.MAGENTA_MARBLE);
        simpleBlockWithItem(ModBlocks.MAGENTA_MARBLE_BRICKS);
        simpleBlockWithItem(ModBlocks.MAGENTA_MARBLE_CHISELED);
        simpleBlockWithItem(ModBlocks.MAGENTA_MARBLE_TILES);
        simpleBlockWithItem(ModBlocks.MAGENTA_POLISHED_MARBLE);
        simpleBlockWithItem(ModBlocks.ORANGE_MARBLE);
        simpleBlockWithItem(ModBlocks.ORANGE_MARBLE_BRICKS);
        simpleBlockWithItem(ModBlocks.ORANGE_MARBLE_CHISELED);
        simpleBlockWithItem(ModBlocks.ORANGE_MARBLE_TILES);
        simpleBlockWithItem(ModBlocks.ORANGE_POLISHED_MARBLE);
        simpleBlockWithItem(ModBlocks.PINK_MARBLE);
        simpleBlockWithItem(ModBlocks.PINK_MARBLE_BRICKS);
        simpleBlockWithItem(ModBlocks.PINK_MARBLE_CHISELED);
        simpleBlockWithItem(ModBlocks.PINK_MARBLE_TILES);
        simpleBlockWithItem(ModBlocks.PINK_POLISHED_MARBLE);
        simpleBlockWithItem(ModBlocks.PURPLE_MARBLE);
        simpleBlockWithItem(ModBlocks.PURPLE_MARBLE_BRICKS);
        simpleBlockWithItem(ModBlocks.PURPLE_MARBLE_CHISELED);
        simpleBlockWithItem(ModBlocks.PURPLE_MARBLE_TILES);
        simpleBlockWithItem(ModBlocks.PURPLE_POLISHED_MARBLE);
        simpleBlockWithItem(ModBlocks.RED_MARBLE);
        simpleBlockWithItem(ModBlocks.RED_MARBLE_BRICKS);
        simpleBlockWithItem(ModBlocks.RED_MARBLE_CHISELED);
        simpleBlockWithItem(ModBlocks.RED_MARBLE_TILES);
        simpleBlockWithItem(ModBlocks.RED_POLISHED_MARBLE);
        simpleBlockWithItem(ModBlocks.YELLOW_MARBLE);
        simpleBlockWithItem(ModBlocks.YELLOW_MARBLE_BRICKS);
        simpleBlockWithItem(ModBlocks.YELLOW_MARBLE_CHISELED);
        simpleBlockWithItem(ModBlocks.YELLOW_MARBLE_TILES);
        simpleBlockWithItem(ModBlocks.YELLOW_POLISHED_MARBLE);


        logBlock(((RotatedPillarBlock) ModBlocks.GOLDEN_PALM_LOG.get()));
        axisBlock(((RotatedPillarBlock) ModBlocks.GOLDEN_PALM_WOOD.get()), blockTexture(ModBlocks.GOLDEN_PALM_LOG.get()), blockTexture(ModBlocks.GOLDEN_PALM_LOG.get()));
        logBlock(((RotatedPillarBlock) ModBlocks.STRIPPED_GOLDEN_PALM_LOG.get()));
        axisBlock(((RotatedPillarBlock) ModBlocks.STRIPPED_GOLDEN_PALM_WOOD.get()), blockTexture(ModBlocks.STRIPPED_GOLDEN_PALM_LOG.get()), blockTexture(ModBlocks.STRIPPED_GOLDEN_PALM_LOG.get()));

        logBlock(((RotatedPillarBlock) ModBlocks.IRONWOOD_LOG.get()));
        axisBlock(((RotatedPillarBlock) ModBlocks.IRONWOOD_WOOD.get()), blockTexture(ModBlocks.IRONWOOD_LOG.get()), blockTexture(ModBlocks.IRONWOOD_LOG.get()));
        logBlock(((RotatedPillarBlock) ModBlocks.STRIPPED_IRONWOOD_LOG.get()));
        axisBlock(((RotatedPillarBlock) ModBlocks.STRIPPED_IRONWOOD_WOOD.get()), blockTexture(ModBlocks.STRIPPED_IRONWOOD_LOG.get()), blockTexture(ModBlocks.STRIPPED_IRONWOOD_LOG.get()));

        blockItem(ModBlocks.GOLDEN_PALM_LOG);
        blockItem(ModBlocks.GOLDEN_PALM_WOOD);
        blockItem(ModBlocks.STRIPPED_GOLDEN_PALM_LOG);
        blockItem(ModBlocks.STRIPPED_GOLDEN_PALM_WOOD);

        blockItem(ModBlocks.IRONWOOD_LOG);
        blockItem(ModBlocks.IRONWOOD_WOOD);
        blockItem(ModBlocks.STRIPPED_IRONWOOD_LOG);
        blockItem(ModBlocks.STRIPPED_IRONWOOD_WOOD);

        blockWithItem(ModBlocks.GOLDEN_PALM_PLANKS);
        blockWithItem(ModBlocks.IRONWOOD_PLANKS);

        //Leaves
        leavesBlock(ModBlocks.GOLDEN_PALM_LEAVES);
        leavesBlock(ModBlocks.IRONWOOD_LEAVES);

        //Saplings
        saplingBlock(ModBlocks.GOLDEN_PALM_SAPLING);
        saplingBlock(ModBlocks.IRONWOOD_SAPLING);

        //Stairs
        stairsBlock(ModBlocks.GOLDEN_PALM_STAIRS.get(), blockTexture(ModBlocks.GOLDEN_PALM_PLANKS.get()));
        stairsBlock(ModBlocks.IRONWOOD_STAIRS.get(), blockTexture(ModBlocks.IRONWOOD_PLANKS.get()));

        stairsBlock(ModBlocks.MARBLE_BRICK_STAIRS.get(), blockTexture(ModBlocks.MARBLE_BRICKS.get()));
        stairsBlock(ModBlocks.MARBLE_TILE_STAIRS.get(), blockTexture(ModBlocks.MARBLE_TILES.get()));
        stairsBlock(ModBlocks.CHARRED_MARBLE_BRICK_STAIRS.get(), blockTexture(ModBlocks.CHARRED_MARBLE_BRICKS.get()));
        stairsBlock(ModBlocks.CHARRED_MARBLE_TILE_STAIRS.get(), blockTexture(ModBlocks.CHARRED_MARBLE_TILES.get()));
        stairsBlock(ModBlocks.BLUE_MARBLE_BRICK_STAIRS.get(), blockTexture(ModBlocks.BLUE_MARBLE_BRICKS.get()));
        stairsBlock(ModBlocks.BLUE_MARBLE_TILE_STAIRS.get(), blockTexture(ModBlocks.BLUE_MARBLE_TILES.get()));
        stairsBlock(ModBlocks.BROWN_MARBLE_BRICK_STAIRS.get(), blockTexture(ModBlocks.BROWN_MARBLE_BRICKS.get()));
        stairsBlock(ModBlocks.BROWN_MARBLE_TILE_STAIRS.get(), blockTexture(ModBlocks.BROWN_MARBLE_TILES.get()));
        stairsBlock(ModBlocks.CYAN_MARBLE_BRICK_STAIRS.get(), blockTexture(ModBlocks.CYAN_MARBLE_BRICKS.get()));
        stairsBlock(ModBlocks.CYAN_MARBLE_TILE_STAIRS.get(), blockTexture(ModBlocks.CYAN_MARBLE_TILES.get()));
        stairsBlock(ModBlocks.GRAY_MARBLE_BRICK_STAIRS.get(), blockTexture(ModBlocks.GRAY_MARBLE_BRICKS.get()));
        stairsBlock(ModBlocks.GRAY_MARBLE_TILE_STAIRS.get(), blockTexture(ModBlocks.GRAY_MARBLE_TILES.get()));
        stairsBlock(ModBlocks.GREEN_MARBLE_BRICK_STAIRS.get(), blockTexture(ModBlocks.GREEN_MARBLE_BRICKS.get()));
        stairsBlock(ModBlocks.GREEN_MARBLE_TILE_STAIRS.get(), blockTexture(ModBlocks.GREEN_MARBLE_TILES.get()));
        stairsBlock(ModBlocks.LIGHT_BLUE_MARBLE_BRICK_STAIRS.get(), blockTexture(ModBlocks.LIGHT_BLUE_MARBLE_BRICKS.get()));
        stairsBlock(ModBlocks.LIGHT_BLUE_MARBLE_TILE_STAIRS.get(), blockTexture(ModBlocks.LIGHT_BLUE_MARBLE_TILES.get()));
        stairsBlock(ModBlocks.LIGHT_GRAY_MARBLE_BRICK_STAIRS.get(), blockTexture(ModBlocks.LIGHT_GRAY_MARBLE_BRICKS.get()));
        stairsBlock(ModBlocks.LIGHT_GRAY_MARBLE_TILE_STAIRS.get(), blockTexture(ModBlocks.LIGHT_GRAY_MARBLE_TILES.get()));
        stairsBlock(ModBlocks.LIME_MARBLE_BRICK_STAIRS.get(), blockTexture(ModBlocks.LIME_MARBLE_BRICKS.get()));
        stairsBlock(ModBlocks.LIME_MARBLE_TILE_STAIRS.get(), blockTexture(ModBlocks.LIME_MARBLE_TILES.get()));
        stairsBlock(ModBlocks.MAGENTA_MARBLE_BRICK_STAIRS.get(), blockTexture(ModBlocks.MAGENTA_MARBLE_BRICKS.get()));
        stairsBlock(ModBlocks.MAGENTA_MARBLE_TILE_STAIRS.get(), blockTexture(ModBlocks.MAGENTA_MARBLE_TILES.get()));
        stairsBlock(ModBlocks.ORANGE_MARBLE_BRICK_STAIRS.get(), blockTexture(ModBlocks.ORANGE_MARBLE_BRICKS.get()));
        stairsBlock(ModBlocks.ORANGE_MARBLE_TILE_STAIRS.get(), blockTexture(ModBlocks.ORANGE_MARBLE_TILES.get()));
        stairsBlock(ModBlocks.PINK_MARBLE_BRICK_STAIRS.get(), blockTexture(ModBlocks.PINK_MARBLE_BRICKS.get()));
        stairsBlock(ModBlocks.PINK_MARBLE_TILE_STAIRS.get(), blockTexture(ModBlocks.PINK_MARBLE_TILES.get()));
        stairsBlock(ModBlocks.PURPLE_MARBLE_BRICK_STAIRS.get(), blockTexture(ModBlocks.PURPLE_MARBLE_BRICKS.get()));
        stairsBlock(ModBlocks.PURPLE_MARBLE_TILE_STAIRS.get(), blockTexture(ModBlocks.PURPLE_MARBLE_TILES.get()));
        stairsBlock(ModBlocks.RED_MARBLE_BRICK_STAIRS.get(), blockTexture(ModBlocks.RED_MARBLE_BRICKS.get()));
        stairsBlock(ModBlocks.RED_MARBLE_TILE_STAIRS.get(), blockTexture(ModBlocks.RED_MARBLE_TILES.get()));
        stairsBlock(ModBlocks.YELLOW_MARBLE_BRICK_STAIRS.get(), blockTexture(ModBlocks.YELLOW_MARBLE_BRICKS.get()));
        stairsBlock(ModBlocks.YELLOW_MARBLE_TILE_STAIRS.get(), blockTexture(ModBlocks.YELLOW_MARBLE_TILES.get()));

        //Slabs
        slabBlock(ModBlocks.GOLDEN_PALM_SLAB.get(), blockTexture(ModBlocks.GOLDEN_PALM_PLANKS.get()), blockTexture(ModBlocks.GOLDEN_PALM_PLANKS.get()));
        slabBlock(ModBlocks.IRONWOOD_SLABS.get(), blockTexture(ModBlocks.IRONWOOD_PLANKS.get()), blockTexture(ModBlocks.IRONWOOD_PLANKS.get()));

        slabBlock(ModBlocks.MARBLE_BRICK_SLABS.get(), blockTexture(ModBlocks.MARBLE_BRICKS.get()), blockTexture(ModBlocks.MARBLE_BRICKS.get()));
        slabBlock(ModBlocks.MARBLE_TILE_SLABS.get(), blockTexture(ModBlocks.MARBLE_TILES.get()), blockTexture(ModBlocks.MARBLE_TILES.get()));
        slabBlock(ModBlocks.CHARRED_MARBLE_BRICK_SLABS.get(), blockTexture(ModBlocks.CHARRED_MARBLE_BRICKS.get()), blockTexture(ModBlocks.CHARRED_MARBLE_BRICKS.get()));
        slabBlock(ModBlocks.CHARRED_MARBLE_TILE_SLABS.get(), blockTexture(ModBlocks.CHARRED_MARBLE_TILES.get()), blockTexture(ModBlocks.CHARRED_MARBLE_TILES.get()));
        slabBlock(ModBlocks.BLUE_MARBLE_BRICK_SLABS.get(), blockTexture(ModBlocks.BLUE_MARBLE_BRICKS.get()), blockTexture(ModBlocks.BLUE_MARBLE_BRICKS.get()));
        slabBlock(ModBlocks.BLUE_MARBLE_TILE_SLABS.get(), blockTexture(ModBlocks.BLUE_MARBLE_TILES.get()), blockTexture(ModBlocks.BLUE_MARBLE_TILES.get()));
        slabBlock(ModBlocks.BROWN_MARBLE_BRICK_SLABS.get(), blockTexture(ModBlocks.BROWN_MARBLE_BRICKS.get()), blockTexture(ModBlocks.BROWN_MARBLE_BRICKS.get()));
        slabBlock(ModBlocks.BROWN_MARBLE_TILE_SLABS.get(), blockTexture(ModBlocks.BROWN_MARBLE_TILES.get()), blockTexture(ModBlocks.BROWN_MARBLE_TILES.get()));
        slabBlock(ModBlocks.CYAN_MARBLE_BRICK_SLABS.get(), blockTexture(ModBlocks.CYAN_MARBLE_BRICKS.get()), blockTexture(ModBlocks.CYAN_MARBLE_BRICKS.get()));
        slabBlock(ModBlocks.CYAN_MARBLE_TILE_SLABS.get(), blockTexture(ModBlocks.CYAN_MARBLE_TILES.get()), blockTexture(ModBlocks.CYAN_MARBLE_TILES.get()));
        slabBlock(ModBlocks.GRAY_MARBLE_BRICK_SLABS.get(), blockTexture(ModBlocks.GRAY_MARBLE_BRICKS.get()), blockTexture(ModBlocks.GRAY_MARBLE_BRICKS.get()));
        slabBlock(ModBlocks.GRAY_MARBLE_TILE_SLABS.get(), blockTexture(ModBlocks.GRAY_MARBLE_TILES.get()), blockTexture(ModBlocks.GRAY_MARBLE_TILES.get()));
        slabBlock(ModBlocks.GREEN_MARBLE_BRICK_SLABS.get(), blockTexture(ModBlocks.GREEN_MARBLE_BRICKS.get()), blockTexture(ModBlocks.GREEN_MARBLE_BRICKS.get()));
        slabBlock(ModBlocks.GREEN_MARBLE_TILE_SLABS.get(), blockTexture(ModBlocks.GREEN_MARBLE_TILES.get()), blockTexture(ModBlocks.GREEN_MARBLE_TILES.get()));
        slabBlock(ModBlocks.LIGHT_BLUE_MARBLE_BRICK_SLABS.get(), blockTexture(ModBlocks.LIGHT_BLUE_MARBLE_BRICKS.get()), blockTexture(ModBlocks.LIGHT_BLUE_MARBLE_BRICKS.get()));
        slabBlock(ModBlocks.LIGHT_BLUE_MARBLE_TILE_SLABS.get(), blockTexture(ModBlocks.LIGHT_BLUE_MARBLE_TILES.get()), blockTexture(ModBlocks.LIGHT_BLUE_MARBLE_TILES.get()));
        slabBlock(ModBlocks.LIGHT_GRAY_MARBLE_BRICK_SLABS.get(), blockTexture(ModBlocks.LIGHT_GRAY_MARBLE_BRICKS.get()), blockTexture(ModBlocks.LIGHT_GRAY_MARBLE_BRICKS.get()));
        slabBlock(ModBlocks.LIGHT_GRAY_MARBLE_TILE_SLABS.get(), blockTexture(ModBlocks.LIGHT_GRAY_MARBLE_TILES.get()), blockTexture(ModBlocks.LIGHT_GRAY_MARBLE_TILES.get()));
        slabBlock(ModBlocks.LIME_MARBLE_BRICK_SLABS.get(), blockTexture(ModBlocks.LIME_MARBLE_BRICKS.get()), blockTexture(ModBlocks.LIME_MARBLE_BRICKS.get()));
        slabBlock(ModBlocks.LIME_MARBLE_TILE_SLABS.get(), blockTexture(ModBlocks.LIME_MARBLE_TILES.get()), blockTexture(ModBlocks.LIME_MARBLE_TILES.get()));
        slabBlock(ModBlocks.MAGENTA_MARBLE_BRICK_SLABS.get(), blockTexture(ModBlocks.MAGENTA_MARBLE_BRICKS.get()), blockTexture(ModBlocks.MAGENTA_MARBLE_BRICKS.get()));
        slabBlock(ModBlocks.MAGENTA_MARBLE_TILE_SLABS.get(), blockTexture(ModBlocks.MAGENTA_MARBLE_TILES.get()), blockTexture(ModBlocks.MAGENTA_MARBLE_TILES.get()));
        slabBlock(ModBlocks.ORANGE_MARBLE_BRICK_SLABS.get(), blockTexture(ModBlocks.ORANGE_MARBLE_BRICKS.get()), blockTexture(ModBlocks.ORANGE_MARBLE_BRICKS.get()));
        slabBlock(ModBlocks.ORANGE_MARBLE_TILE_SLABS.get(), blockTexture(ModBlocks.ORANGE_MARBLE_TILES.get()), blockTexture(ModBlocks.ORANGE_MARBLE_TILES.get()));
        slabBlock(ModBlocks.PINK_MARBLE_BRICK_SLABS.get(), blockTexture(ModBlocks.PINK_MARBLE_BRICKS.get()), blockTexture(ModBlocks.PINK_MARBLE_BRICKS.get()));
        slabBlock(ModBlocks.PINK_MARBLE_TILE_SLABS.get(), blockTexture(ModBlocks.PINK_MARBLE_TILES.get()), blockTexture(ModBlocks.PINK_MARBLE_TILES.get()));
        slabBlock(ModBlocks.PURPLE_MARBLE_BRICK_SLABS.get(), blockTexture(ModBlocks.PURPLE_MARBLE_BRICKS.get()), blockTexture(ModBlocks.PURPLE_MARBLE_BRICKS.get()));
        slabBlock(ModBlocks.PURPLE_MARBLE_TILE_SLABS.get(), blockTexture(ModBlocks.PURPLE_MARBLE_TILES.get()), blockTexture(ModBlocks.PURPLE_MARBLE_TILES.get()));
        slabBlock(ModBlocks.RED_MARBLE_BRICK_SLABS.get(), blockTexture(ModBlocks.RED_MARBLE_BRICKS.get()), blockTexture(ModBlocks.RED_MARBLE_BRICKS.get()));
        slabBlock(ModBlocks.RED_MARBLE_TILE_SLABS.get(), blockTexture(ModBlocks.RED_MARBLE_TILES.get()), blockTexture(ModBlocks.RED_MARBLE_TILES.get()));
        slabBlock(ModBlocks.YELLOW_MARBLE_BRICK_SLABS.get(), blockTexture(ModBlocks.YELLOW_MARBLE_BRICKS.get()), blockTexture(ModBlocks.YELLOW_MARBLE_BRICKS.get()));
        slabBlock(ModBlocks.YELLOW_MARBLE_TILE_SLABS.get(), blockTexture(ModBlocks.YELLOW_MARBLE_TILES.get()), blockTexture(ModBlocks.YELLOW_MARBLE_TILES.get()));

        //Walls
        wallBlock(ModBlocks.MARBLE_BRICK_WALLS.get(), blockTexture(ModBlocks.MARBLE_BRICKS.get()));
        wallBlock(ModBlocks.MARBLE_TILE_WALLS.get(), blockTexture(ModBlocks.MARBLE_TILES.get()));
        wallBlock(ModBlocks.CHARRED_MARBLE_BRICK_WALLS.get(), blockTexture(ModBlocks.CHARRED_MARBLE_BRICKS.get()));
        wallBlock(ModBlocks.CHARRED_MARBLE_TILE_WALLS.get(), blockTexture(ModBlocks.CHARRED_MARBLE_TILES.get()));
        wallBlock(ModBlocks.BLUE_MARBLE_BRICK_WALLS.get(), blockTexture(ModBlocks.BLUE_MARBLE_BRICKS.get()));
        wallBlock(ModBlocks.BLUE_MARBLE_TILE_WALLS.get(), blockTexture(ModBlocks.BLUE_MARBLE_TILES.get()));
        wallBlock(ModBlocks.BROWN_MARBLE_BRICK_WALLS.get(), blockTexture(ModBlocks.BROWN_MARBLE_BRICKS.get()));
        wallBlock(ModBlocks.BROWN_MARBLE_TILE_WALLS.get(), blockTexture(ModBlocks.BROWN_MARBLE_TILES.get()));
        wallBlock(ModBlocks.CYAN_MARBLE_BRICK_WALLS.get(), blockTexture(ModBlocks.CYAN_MARBLE_BRICKS.get()));
        wallBlock(ModBlocks.CYAN_MARBLE_TILE_WALLS.get(), blockTexture(ModBlocks.CYAN_MARBLE_TILES.get()));
        wallBlock(ModBlocks.GRAY_MARBLE_BRICK_WALLS.get(), blockTexture(ModBlocks.GRAY_MARBLE_BRICKS.get()));
        wallBlock(ModBlocks.GRAY_MARBLE_TILE_WALLS.get(), blockTexture(ModBlocks.GRAY_MARBLE_TILES.get()));
        wallBlock(ModBlocks.GREEN_MARBLE_BRICK_WALLS.get(), blockTexture(ModBlocks.GREEN_MARBLE_BRICKS.get()));
        wallBlock(ModBlocks.GREEN_MARBLE_TILE_WALLS.get(), blockTexture(ModBlocks.GREEN_MARBLE_TILES.get()));
        wallBlock(ModBlocks.LIGHT_BLUE_MARBLE_BRICK_WALLS.get(), blockTexture(ModBlocks.LIGHT_BLUE_MARBLE_BRICKS.get()));
        wallBlock(ModBlocks.LIGHT_BLUE_MARBLE_TILE_WALLS.get(), blockTexture(ModBlocks.LIGHT_BLUE_MARBLE_TILES.get()));
        wallBlock(ModBlocks.LIGHT_GRAY_MARBLE_BRICK_WALLS.get(), blockTexture(ModBlocks.LIGHT_GRAY_MARBLE_BRICKS.get()));
        wallBlock(ModBlocks.LIGHT_GRAY_MARBLE_TILE_WALLS.get(), blockTexture(ModBlocks.LIGHT_GRAY_MARBLE_TILES.get()));
        wallBlock(ModBlocks.LIME_MARBLE_BRICK_WALLS.get(), blockTexture(ModBlocks.LIME_MARBLE_BRICKS.get()));
        wallBlock(ModBlocks.LIME_MARBLE_TILE_WALLS.get(), blockTexture(ModBlocks.LIME_MARBLE_TILES.get()));
        wallBlock(ModBlocks.MAGENTA_MARBLE_BRICK_WALLS.get(), blockTexture(ModBlocks.MAGENTA_MARBLE_BRICKS.get()));
        wallBlock(ModBlocks.MAGENTA_MARBLE_TILE_WALLS.get(), blockTexture(ModBlocks.MAGENTA_MARBLE_TILES.get()));
        wallBlock(ModBlocks.ORANGE_MARBLE_BRICK_WALLS.get(), blockTexture(ModBlocks.ORANGE_MARBLE_BRICKS.get()));
        wallBlock(ModBlocks.ORANGE_MARBLE_TILE_WALLS.get(), blockTexture(ModBlocks.ORANGE_MARBLE_TILES.get()));
        wallBlock(ModBlocks.PINK_MARBLE_BRICK_WALLS.get(), blockTexture(ModBlocks.PINK_MARBLE_BRICKS.get()));
        wallBlock(ModBlocks.PINK_MARBLE_TILE_WALLS.get(), blockTexture(ModBlocks.PINK_MARBLE_TILES.get()));
        wallBlock(ModBlocks.PURPLE_MARBLE_BRICK_WALLS.get(), blockTexture(ModBlocks.PURPLE_MARBLE_BRICKS.get()));
        wallBlock(ModBlocks.PURPLE_MARBLE_TILE_WALLS.get(), blockTexture(ModBlocks.PURPLE_MARBLE_TILES.get()));
        wallBlock(ModBlocks.RED_MARBLE_BRICK_WALLS.get(), blockTexture(ModBlocks.RED_MARBLE_BRICKS.get()));
        wallBlock(ModBlocks.RED_MARBLE_TILE_WALLS.get(), blockTexture(ModBlocks.RED_MARBLE_TILES.get()));
        wallBlock(ModBlocks.YELLOW_MARBLE_BRICK_WALLS.get(), blockTexture(ModBlocks.YELLOW_MARBLE_BRICKS.get()));
        wallBlock(ModBlocks.YELLOW_MARBLE_TILE_WALLS.get(), blockTexture(ModBlocks.YELLOW_MARBLE_TILES.get()));

        //Buttons
        buttonBlock(ModBlocks.GOLDEN_PALM_BUTTON.get(), blockTexture(ModBlocks.GOLDEN_PALM_PLANKS.get()));
        buttonBlock(ModBlocks.IRONWOOD_BUTTON.get(), blockTexture(ModBlocks.IRONWOOD_PLANKS.get()));

        //PresurePlates
        pressurePlateBlock(ModBlocks.GOLDEN_PALM_PRESSURE_PLATE.get(), blockTexture(ModBlocks.GOLDEN_PALM_PLANKS.get()));
        pressurePlateBlock(ModBlocks.IRONWOOD_PRESSURE_PLATE.get(), blockTexture(ModBlocks.IRONWOOD_PLANKS.get()));

        //Fences
        fenceBlock(ModBlocks.GOLDEN_PALM_FENCE.get(), blockTexture(ModBlocks.GOLDEN_PALM_PLANKS.get()));
        fenceBlock(ModBlocks.IRONWOOD_FENCE.get(), blockTexture(ModBlocks.IRONWOOD_PLANKS.get()));

        //FenceGates
        fenceGateBlock(ModBlocks.GOLDEN_PALM_FENCE_GATE.get(), blockTexture(ModBlocks.GOLDEN_PALM_PLANKS.get()));
        fenceGateBlock(ModBlocks.IRONWOOD_FENCE_GATE.get(), blockTexture(ModBlocks.IRONWOOD_PLANKS.get()));

        //Doors
        doorBlockWithRenderType(ModBlocks.GOLDEN_PALM_DOOR.get(), modLoc("block/golden_palm_door_bottom"), modLoc("block/golden_palm_door_top"), "cutout");
        doorBlockWithRenderType(ModBlocks.IRONWOOD_DOOR.get(), modLoc("block/ironwood_door_bottom"), modLoc("block/ironwood_door_top"), "cutout");

        //TrapDoors
        trapdoorBlockWithRenderType(ModBlocks.GOLDEN_PALM_TRAPDOOR.get(), modLoc("block/golden_palm_trapdoor"), true, "cutout");
        trapdoorBlockWithRenderType(ModBlocks.IRONWOOD_TRAPDOOR.get(), modLoc("block/ironwood_trapdoor"), true, "cutout");


        //BlockItems Stairs
        blockItem(ModBlocks.GOLDEN_PALM_STAIRS);
        blockItem(ModBlocks.IRONWOOD_STAIRS);

        blockItem(ModBlocks.MARBLE_TILE_STAIRS);
        blockItem(ModBlocks.MARBLE_BRICK_STAIRS);
        blockItem(ModBlocks.CHARRED_MARBLE_TILE_STAIRS);
        blockItem(ModBlocks.CHARRED_MARBLE_BRICK_STAIRS);
        blockItem(ModBlocks.LIGHT_GRAY_MARBLE_BRICK_STAIRS);
        blockItem(ModBlocks.LIGHT_GRAY_MARBLE_TILE_STAIRS);
        blockItem(ModBlocks.GRAY_MARBLE_BRICK_STAIRS);
        blockItem(ModBlocks.GRAY_MARBLE_TILE_STAIRS);
        blockItem(ModBlocks.BROWN_MARBLE_BRICK_STAIRS);
        blockItem(ModBlocks.BROWN_MARBLE_TILE_STAIRS);
        blockItem(ModBlocks.RED_MARBLE_BRICK_STAIRS);
        blockItem(ModBlocks.RED_MARBLE_TILE_STAIRS);
        blockItem(ModBlocks.ORANGE_MARBLE_BRICK_STAIRS);
        blockItem(ModBlocks.ORANGE_MARBLE_TILE_STAIRS);
        blockItem(ModBlocks.YELLOW_MARBLE_BRICK_STAIRS);
        blockItem(ModBlocks.YELLOW_MARBLE_TILE_STAIRS);
        blockItem(ModBlocks.LIME_MARBLE_BRICK_STAIRS);
        blockItem(ModBlocks.LIME_MARBLE_TILE_STAIRS);
        blockItem(ModBlocks.GREEN_MARBLE_BRICK_STAIRS);
        blockItem(ModBlocks.GREEN_MARBLE_TILE_STAIRS);
        blockItem(ModBlocks.CYAN_MARBLE_BRICK_STAIRS);
        blockItem(ModBlocks.CYAN_MARBLE_TILE_STAIRS);
        blockItem(ModBlocks.LIGHT_BLUE_MARBLE_BRICK_STAIRS);
        blockItem(ModBlocks.LIGHT_BLUE_MARBLE_TILE_STAIRS);
        blockItem(ModBlocks.BLUE_MARBLE_BRICK_STAIRS);
        blockItem(ModBlocks.BLUE_MARBLE_TILE_STAIRS);
        blockItem(ModBlocks.PURPLE_MARBLE_BRICK_STAIRS);
        blockItem(ModBlocks.PURPLE_MARBLE_TILE_STAIRS);
        blockItem(ModBlocks.MAGENTA_MARBLE_BRICK_STAIRS);
        blockItem(ModBlocks.MAGENTA_MARBLE_TILE_STAIRS);
        blockItem(ModBlocks.PINK_MARBLE_BRICK_STAIRS);
        blockItem(ModBlocks.PINK_MARBLE_TILE_STAIRS);

        //BlockItems Slabs
        blockItem(ModBlocks.GOLDEN_PALM_SLAB);
        blockItem(ModBlocks.IRONWOOD_SLABS);

        blockItem(ModBlocks.MARBLE_BRICK_SLABS);
        blockItem(ModBlocks.MARBLE_TILE_SLABS);
        blockItem(ModBlocks.CHARRED_MARBLE_BRICK_SLABS);
        blockItem(ModBlocks.CHARRED_MARBLE_TILE_SLABS);
        blockItem(ModBlocks.LIGHT_GRAY_MARBLE_BRICK_SLABS);
        blockItem(ModBlocks.LIGHT_GRAY_MARBLE_TILE_SLABS);
        blockItem(ModBlocks.GRAY_MARBLE_BRICK_SLABS);
        blockItem(ModBlocks.GRAY_MARBLE_TILE_SLABS);
        blockItem(ModBlocks.BROWN_MARBLE_BRICK_SLABS);
        blockItem(ModBlocks.BROWN_MARBLE_TILE_SLABS);
        blockItem(ModBlocks.RED_MARBLE_BRICK_SLABS);
        blockItem(ModBlocks.RED_MARBLE_TILE_SLABS);
        blockItem(ModBlocks.ORANGE_MARBLE_BRICK_SLABS);
        blockItem(ModBlocks.ORANGE_MARBLE_TILE_SLABS);
        blockItem(ModBlocks.YELLOW_MARBLE_BRICK_SLABS);
        blockItem(ModBlocks.YELLOW_MARBLE_TILE_SLABS);
        blockItem(ModBlocks.LIME_MARBLE_BRICK_SLABS);
        blockItem(ModBlocks.LIME_MARBLE_TILE_SLABS);
        blockItem(ModBlocks.GREEN_MARBLE_BRICK_SLABS);
        blockItem(ModBlocks.GREEN_MARBLE_TILE_SLABS);
        blockItem(ModBlocks.CYAN_MARBLE_BRICK_SLABS);
        blockItem(ModBlocks.CYAN_MARBLE_TILE_SLABS);
        blockItem(ModBlocks.LIGHT_BLUE_MARBLE_BRICK_SLABS);
        blockItem(ModBlocks.LIGHT_BLUE_MARBLE_TILE_SLABS);
        blockItem(ModBlocks.BLUE_MARBLE_BRICK_SLABS);
        blockItem(ModBlocks.BLUE_MARBLE_TILE_SLABS);
        blockItem(ModBlocks.PURPLE_MARBLE_BRICK_SLABS);
        blockItem(ModBlocks.PURPLE_MARBLE_TILE_SLABS);
        blockItem(ModBlocks.MAGENTA_MARBLE_BRICK_SLABS);
        blockItem(ModBlocks.MAGENTA_MARBLE_TILE_SLABS);
        blockItem(ModBlocks.PINK_MARBLE_BRICK_SLABS);
        blockItem(ModBlocks.PINK_MARBLE_TILE_SLABS);



        //BlockItem Redstone Stuff for now
        blockItem(ModBlocks.GOLDEN_PALM_PRESSURE_PLATE);
        blockItem(ModBlocks.GOLDEN_PALM_FENCE_GATE);
        blockItem(ModBlocks.GOLDEN_PALM_TRAPDOOR, "_bottom");
        blockItem(ModBlocks.IRONWOOD_PRESSURE_PLATE);
        blockItem(ModBlocks.IRONWOOD_FENCE_GATE);
        blockItem(ModBlocks.IRONWOOD_TRAPDOOR, "_bottom");



        /*
        blockItem(ModBlocks.FLAME_STAND_BLOCK);
        blockItem(ModBlocks.CAULDRON_PEDESTAL_BLOCK);
        blockItem(ModBlocks.SPIRIT_CONDENSER_BLOCK);
         */
    }


    private void spiritualStoneClusterBlocks(DeferredHolder<Block, ?> block){
        directionalBlock(block.get(),models()
                .withExistingParent(block.getId().getPath(), AscensionCraft.MOD_ID+":block/spiritual_stone_cluster_template")
                .texture("palette", block.getId().toString().replace(":", ":block/")));
    }

    public void makeCrop(CropBlock block, String modelName, String textureName) {
        Function<BlockState, ConfiguredModel[]> function = state -> states(state, block, modelName, textureName);

        getVariantBuilder(block).forAllStates(function);
    }

    private ConfiguredModel[] states(BlockState state, CropBlock block, String modelName, String textureName) {
        ConfiguredModel[] models = new ConfiguredModel[1];
        models[0] = new ConfiguredModel(models().crop(modelName + state.getValue(((GenericSlowCropBlock) block).getAgeProperty()),
                ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "block/herbs/" + textureName + state.getValue(((GenericSlowCropBlock) block).getAgeProperty()))).renderType("cutout"));

        return models;
    }

    public void makeStemCrop(CropBlock block2, String modelName2, String textureName2) {
        Function<BlockState, ConfiguredModel[]> function = state2 -> states2(state2, block2, modelName2, textureName2);
        getVariantBuilder(block2).forAllStates(function);
    }

    private ConfiguredModel[] states2(BlockState state2, CropBlock block2, String modelName2, String textureName2) {
        ConfiguredModel[] models = new ConfiguredModel[1];
        int age = state2.getValue(((StemSlowCropBlock) block2).getAgeProperty());
        models[0] = new ConfiguredModel(models().cross(
                        modelName2 + age,
                        ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "block/herbs/" + textureName2 + age))
                .renderType("cutout"));

        return models;

    }

    public void makeJadeDewGrassCrop(CropBlock block, String modelName, String textureName) {
        getVariantBuilder(block).forAllStates(state -> {
            int age = state.getValue(JadeDewGrassCropBlock.AGE);
            return new ConfiguredModel[]{
                    new ConfiguredModel(
                            models().crop(modelName + age,
                                            ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID,
                                                    "block/herbs/" + textureName + age))
                                    .renderType("cutout"))
            };
        });
    }




    private void simpleBlockWithItem(DeferredBlock<?> deferredBlock) {
        simpleBlockWithItem(deferredBlock.get(), cubeAll(deferredBlock.get()));
    }

    private void saplingBlock(DeferredBlock<Block> blockRegistryObject) {
        simpleBlock(blockRegistryObject.get(),
                models().cross(BuiltInRegistries.BLOCK.getKey(blockRegistryObject.get()).getPath(), blockTexture(blockRegistryObject.get())).renderType("cutout"));
    }

    private void herbsBlockCutout(DeferredBlock<Block> blockRegistryObject) {
        Block block = blockRegistryObject.get();
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(block);
        String blockName = blockId.getPath();
        ResourceLocation textureLoc = ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "block/herbs/" + blockName);

        simpleBlock(block,
                models().crop(blockId.getPath(), textureLoc).renderType("cutout"));
    }
    private void herbsBlockCross(DeferredBlock<Block> blockRegistryObject) {
        Block block = blockRegistryObject.get();
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(block);
        String blockName = blockId.getPath();
        ResourceLocation textureLoc = ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "block/herbs/" + blockName);

        simpleBlock(block,
                models().singleTexture(blockId.getPath(),
                                ResourceLocation.parse("block/cross"),
                                "cross", textureLoc)
                        .renderType("cutout"));
    }

    private void leavesBlock(DeferredBlock<Block> blockRegistryObject) {
        simpleBlockWithItem(blockRegistryObject.get(),
                models().singleTexture(BuiltInRegistries.BLOCK.getKey(blockRegistryObject.get()).getPath(), ResourceLocation.parse("minecraft:block/leaves"),
                        "all", blockTexture(blockRegistryObject.get())).renderType("cutout"));
    }

    private void blockWithItem(DeferredBlock<?> deferredBlock) {
        simpleBlockWithItem(deferredBlock.get(), cubeAll(deferredBlock.get()));
    }

    private void blockItem(DeferredBlock<?> deferredBlock) {
        simpleBlockItem(deferredBlock.get(), new ModelFile.UncheckedModelFile("ascension:block/" + deferredBlock.getId().getPath()));
    }

    private void blockItem(DeferredBlock<?> deferredBlock, String appendix) {
        simpleBlockItem(deferredBlock.get(), new ModelFile.UncheckedModelFile("ascension:block/" + deferredBlock.getId().getPath() + appendix));
    }



    private void fireBlock(DeferredBlock<Block> deferredBlock, String fireTextureName) {
        Block block = deferredBlock.get();
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(block);
        String path = blockId.getPath();

        // Generate all the fire model variants
        for (int i = 0; i <= 1; i++) {
            // Floor fire models
            models().singleTexture(path + "_floor" + i,
                            ResourceLocation.parse("minecraft:block/template_fire_floor"),
                            "fire", ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "block/" + fireTextureName + "_" + i))
                    .renderType("cutout");

            // Up fire models
            models().singleTexture(path + "_up" + i,
                            ResourceLocation.parse("minecraft:block/template_fire_up"),
                            "fire", ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "block/" + fireTextureName + "_" + i))
                    .renderType("cutout");

            // Up alt fire models
            models().singleTexture(path + "_up_alt" + i,
                            ResourceLocation.parse("minecraft:block/template_fire_up_alt"),
                            "fire", ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "block/" + fireTextureName + "_" + i))
                    .renderType("cutout");

            // Side fire models
            models().singleTexture(path + "_side" + i,
                            ResourceLocation.parse("minecraft:block/template_fire_side"),
                            "fire", ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "block/" + fireTextureName + "_" + i))
                    .renderType("cutout");

            // Side alt fire models
            models().singleTexture(path + "_side_alt" + i,
                            ResourceLocation.parse("minecraft:block/template_fire_side_alt"),
                            "fire", ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "block/" + fireTextureName + "_" + i))
                    .renderType("cutout");
        }

        // Create multipart blockstate (similar to vanilla fire)
        MultiPartBlockStateBuilder builder = getMultipartBuilder(block);

        // Add all the fire parts with age conditions
        for (int ageGroup = 0; ageGroup <= 1; ageGroup++) {
            String textureSuffix = String.valueOf(ageGroup);
            int minAge = ageGroup * 8;
            int maxAge = minAge + 7;

            // Floor
            builder.part()
                    .modelFile(models().getExistingFile(ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "block/" + path + "_floor" + textureSuffix)))
                    .addModel()
                    .condition(CrimsonLotusFire.AGE, minAge, maxAge)
                    .end();

            // Up
            builder.part()
                    .modelFile(models().getExistingFile(ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "block/" + path + "_up" + textureSuffix)))
                    .addModel()
                    .condition(CrimsonLotusFire.AGE, minAge, maxAge)
                    .end();

            // Up Alt
            builder.part()
                    .modelFile(models().getExistingFile(ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "block/" + path + "_up_alt" + textureSuffix)))
                    .addModel()
                    .condition(CrimsonLotusFire.AGE, minAge, maxAge)
                    .end();

            // Side (all directions)
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                builder.part()
                        .modelFile(models().getExistingFile(ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "block/" + path + "_side" + textureSuffix)))
                        .rotationY((int) direction.toYRot())
                        .addModel()
                        .condition(CrimsonLotusFire.AGE, minAge, maxAge)
                        .end();

                builder.part()
                        .modelFile(models().getExistingFile(ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "block/" + path + "_side_alt" + textureSuffix)))
                        .rotationY((int) direction.toYRot())
                        .addModel()
                        .condition(CrimsonLotusFire.AGE, minAge, maxAge)
                        .end();
            }
        }
    }


}
