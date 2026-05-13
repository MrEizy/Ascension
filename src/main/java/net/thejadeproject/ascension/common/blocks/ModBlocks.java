package net.thejadeproject.ascension.common.blocks;


import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.common.blocks.custom.*;

import net.thejadeproject.ascension.common.blocks.custom.crops.GenericSlowCropBlock;
import net.thejadeproject.ascension.common.blocks.custom.crops.StemSlowCropBlock;
import net.thejadeproject.ascension.common.blocks.custom.crops.jadedew.JadeDewGrassCropBlock;
import net.thejadeproject.ascension.common.blocks.custom.fires.CrimsonLotusFire;
import net.thejadeproject.ascension.common.blocks.entity.ModBlockEntities;
import net.thejadeproject.ascension.common.items.ModItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.thejadeproject.ascension.worldgen.tree.ModTreeGrowers;


import java.util.Set;
import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCK =
            DeferredRegister.createBlocks(AscensionCraft.MOD_ID);




    public static final DeferredBlock<TechniqueStandBlock> TECHNIQUE_STAND =
            registerBlock("technique_stand",
                    () -> new TechniqueStandBlock(
                            BlockBehaviour.Properties.of()
                                    .mapColor(MapColor.STONE)
                                    .sound(SoundType.STONE)
                                    .strength(2.0f, 4.0f)
                                    .requiresCorrectToolForDrops()
                                    .noOcclusion()
                    ));

    public static final DeferredBlock<FlameStandBlock> FLAME_STAND_BLOCK =
            registerBlock("flame_stand",
                    () -> new FlameStandBlock(BlockBehaviour.Properties.of()
                            .mapColor(MapColor.STONE)
                            .sound(SoundType.STONE)
                            .strength(2.5f, 4.0f)
                            .requiresCorrectToolForDrops()
                            .lightLevel(state -> state.getValue(FlameStandBlock.LIT) ? 13 : 0)
                    ));

    public static final DeferredBlock<SpiritCondenserBlock> SPIRIT_CONDENSER_BLOCK =
            registerBlock("spirit_condenser",
                    () -> new SpiritCondenserBlock(BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_PURPLE)
                            .sound(SoundType.AMETHYST)
                            .strength(3.0f, 4.0f)
                            .requiresCorrectToolForDrops()
                            .noOcclusion()
                    ));


    public static final DeferredBlock<CauldronPedestalBlock> CAULDRON_PEDESTAL_BLOCK =
            registerBlock("cauldron_pedestal",
                    () -> new CauldronPedestalBlock(BlockBehaviour.Properties.of()
                            .mapColor(MapColor.STONE)
                            .sound(SoundType.STONE)
                            .strength(2.5f, 4.0f)
                            .requiresCorrectToolForDrops()
                    ));











    private static Block makeBlockFrom(Block other) {
        return new Block(BlockBehaviour.Properties.ofFullCopy(other));
    }
    private static StairBlock makeStairsFrom(Supplier<Block> baseBlock) {
        return new StairBlock(
                baseBlock.get().defaultBlockState(),
                BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE_STAIRS)
        );
    }
    private static SlabBlock makeSlabFrom() {
        return new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE_SLAB));
    }
    private static WallBlock makeWallFrom() {
        return new WallBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.ANDESITE_WALL));
    }

    /** Marble Blocks */
    public static final DeferredBlock<Block> RAW_MARBLE = registerBlock("raw_marble", () -> makeBlockFrom(Blocks.ANDESITE));
    public static final DeferredBlock<Block> POLISHED_MARBLE = registerBlock("polished_marble", () -> makeBlockFrom(Blocks.POLISHED_ANDESITE));
    public static final DeferredBlock<Block> MARBLE_BRICKS = registerBlock("marble_bricks", () -> makeBlockFrom(Blocks.POLISHED_ANDESITE));
    public static final DeferredBlock<Block> MARBLE_CHISELED = registerBlock("marble_chiseled", () -> makeBlockFrom(Blocks.POLISHED_ANDESITE));
    public static final DeferredBlock<Block> MARBLE_TILES = registerBlock("marble_tiles", () -> makeBlockFrom(Blocks.POLISHED_ANDESITE));

    public static final DeferredBlock<Block> POLISHED_BURNED_MARBLE = registerBlock("polished_burned_marble", () -> makeBlockFrom(Blocks.POLISHED_ANDESITE));
    public static final DeferredBlock<Block> CHARRED_MARBLE = registerBlock("marble_burned", () -> makeBlockFrom(Blocks.ANDESITE));
    public static final DeferredBlock<Block> CHARRED_MARBLE_BRICKS = registerBlock("marble_burned_bricks", () -> makeBlockFrom(Blocks.POLISHED_ANDESITE));
    public static final DeferredBlock<Block> CHARRED_MARBLE_CHISELED = registerBlock("marble_burned_chiseled", () -> makeBlockFrom(Blocks.POLISHED_ANDESITE));
    public static final DeferredBlock<Block> CHARRED_MARBLE_TILES = registerBlock("marble_burned_tiles", () -> makeBlockFrom(Blocks.POLISHED_ANDESITE));

    public static final DeferredBlock<Block> BLUE_MARBLE = registerBlock("blue_marble", () -> makeBlockFrom(Blocks.ANDESITE));
    public static final DeferredBlock<Block> BLUE_MARBLE_BRICKS = registerBlock("blue_marble_bricks", () -> makeBlockFrom(Blocks.POLISHED_ANDESITE));
    public static final DeferredBlock<Block> BLUE_MARBLE_CHISELED = registerBlock("blue_marble_chiseled", () -> makeBlockFrom(Blocks.POLISHED_ANDESITE));
    public static final DeferredBlock<Block> BLUE_MARBLE_TILES = registerBlock("blue_marble_tiles", () -> makeBlockFrom(Blocks.POLISHED_ANDESITE));
    public static final DeferredBlock<Block> BLUE_POLISHED_MARBLE = registerBlock("blue_polished_marble", () -> makeBlockFrom(Blocks.POLISHED_ANDESITE));
    public static final DeferredBlock<Block> BROWN_MARBLE = registerBlock("brown_marble", () -> makeBlockFrom(Blocks.ANDESITE));
    public static final DeferredBlock<Block> BROWN_MARBLE_BRICKS = registerBlock("brown_marble_bricks", () -> makeBlockFrom(Blocks.POLISHED_ANDESITE));
    public static final DeferredBlock<Block> BROWN_MARBLE_CHISELED = registerBlock("brown_marble_chiseled", () -> makeBlockFrom(Blocks.POLISHED_ANDESITE));
    public static final DeferredBlock<Block> BROWN_MARBLE_TILES = registerBlock("brown_marble_tiles", () -> makeBlockFrom(Blocks.POLISHED_ANDESITE));
    public static final DeferredBlock<Block> BROWN_POLISHED_MARBLE = registerBlock("brown_polished_marble", () -> makeBlockFrom(Blocks.POLISHED_ANDESITE));
    public static final DeferredBlock<Block> CYAN_MARBLE = registerBlock("cyan_marble", () -> makeBlockFrom(Blocks.ANDESITE));
    public static final DeferredBlock<Block> CYAN_MARBLE_BRICKS = registerBlock("cyan_marble_bricks", () -> makeBlockFrom(Blocks.POLISHED_ANDESITE));
    public static final DeferredBlock<Block> CYAN_MARBLE_CHISELED = registerBlock("cyan_marble_chiseled", () -> makeBlockFrom(Blocks.POLISHED_ANDESITE));
    public static final DeferredBlock<Block> CYAN_MARBLE_TILES = registerBlock("cyan_marble_tiles", () -> makeBlockFrom(Blocks.POLISHED_ANDESITE));
    public static final DeferredBlock<Block> CYAN_POLISHED_MARBLE = registerBlock("cyan_polished_marble", () -> makeBlockFrom(Blocks.POLISHED_ANDESITE));
    public static final DeferredBlock<Block> GRAY_MARBLE = registerBlock("gray_marble", () -> makeBlockFrom(Blocks.ANDESITE));
    public static final DeferredBlock<Block> GRAY_MARBLE_BRICKS = registerBlock("gray_marble_bricks", () -> makeBlockFrom(Blocks.POLISHED_ANDESITE));
    public static final DeferredBlock<Block> GRAY_MARBLE_CHISELED = registerBlock("gray_marble_chiseled", () -> makeBlockFrom(Blocks.POLISHED_ANDESITE));
    public static final DeferredBlock<Block> GRAY_MARBLE_TILES = registerBlock("gray_marble_tiles", () -> makeBlockFrom(Blocks.POLISHED_ANDESITE));
    public static final DeferredBlock<Block> GRAY_POLISHED_MARBLE = registerBlock("gray_polished_marble", () -> makeBlockFrom(Blocks.POLISHED_ANDESITE));
    public static final DeferredBlock<Block> GREEN_MARBLE = registerBlock("green_marble", () -> makeBlockFrom(Blocks.ANDESITE));
    public static final DeferredBlock<Block> GREEN_MARBLE_BRICKS = registerBlock("green_marble_bricks", () -> makeBlockFrom(Blocks.POLISHED_ANDESITE));
    public static final DeferredBlock<Block> GREEN_MARBLE_CHISELED = registerBlock("green_marble_chiseled", () -> makeBlockFrom(Blocks.POLISHED_ANDESITE));
    public static final DeferredBlock<Block> GREEN_MARBLE_TILES = registerBlock("green_marble_tiles", () -> makeBlockFrom(Blocks.POLISHED_ANDESITE));
    public static final DeferredBlock<Block> GREEN_POLISHED_MARBLE = registerBlock("green_polished_marble", () -> makeBlockFrom(Blocks.POLISHED_ANDESITE));
    public static final DeferredBlock<Block> LIGHT_BLUE_MARBLE = registerBlock("light_blue_marble", () -> makeBlockFrom(Blocks.ANDESITE));
    public static final DeferredBlock<Block> LIGHT_BLUE_MARBLE_BRICKS = registerBlock("light_blue_marble_bricks", () -> makeBlockFrom(Blocks.POLISHED_ANDESITE));
    public static final DeferredBlock<Block> LIGHT_BLUE_MARBLE_CHISELED = registerBlock("light_blue_marble_chiseled", () -> makeBlockFrom(Blocks.POLISHED_ANDESITE));
    public static final DeferredBlock<Block> LIGHT_BLUE_MARBLE_TILES = registerBlock("light_blue_marble_tiles", () -> makeBlockFrom(Blocks.POLISHED_ANDESITE));
    public static final DeferredBlock<Block> LIGHT_BLUE_POLISHED_MARBLE = registerBlock("light_blue_polished_marble", () -> makeBlockFrom(Blocks.POLISHED_ANDESITE));
    public static final DeferredBlock<Block> LIGHT_GRAY_MARBLE = registerBlock("light_gray_marble", () -> makeBlockFrom(Blocks.ANDESITE));
    public static final DeferredBlock<Block> LIGHT_GRAY_MARBLE_BRICKS = registerBlock("light_gray_marble_bricks", () -> makeBlockFrom(Blocks.POLISHED_ANDESITE));
    public static final DeferredBlock<Block> LIGHT_GRAY_MARBLE_CHISELED = registerBlock("light_gray_marble_chiseled", () -> makeBlockFrom(Blocks.POLISHED_ANDESITE));
    public static final DeferredBlock<Block> LIGHT_GRAY_MARBLE_TILES = registerBlock("light_gray_marble_tiles", () -> makeBlockFrom(Blocks.POLISHED_ANDESITE));
    public static final DeferredBlock<Block> POLISHED_LIGHT_GRAY_MARBLE = registerBlock("polished_light_gray_marble", () -> makeBlockFrom(Blocks.POLISHED_ANDESITE));
    public static final DeferredBlock<Block> LIME_MARBLE = registerBlock("lime_marble", () -> makeBlockFrom(Blocks.ANDESITE));
    public static final DeferredBlock<Block> LIME_MARBLE_BRICKS = registerBlock("lime_marble_bricks", () -> makeBlockFrom(Blocks.POLISHED_ANDESITE));
    public static final DeferredBlock<Block> LIME_MARBLE_CHISELED = registerBlock("lime_marble_chiseled", () -> makeBlockFrom(Blocks.POLISHED_ANDESITE));
    public static final DeferredBlock<Block> LIME_MARBLE_TILES = registerBlock("lime_marble_tiles", () -> makeBlockFrom(Blocks.POLISHED_ANDESITE));
    public static final DeferredBlock<Block> LIME_POLISHED_MARBLE = registerBlock("lime_polished_marble", () -> makeBlockFrom(Blocks.POLISHED_ANDESITE));
    public static final DeferredBlock<Block> MAGENTA_MARBLE = registerBlock("magenta_marble", () -> makeBlockFrom(Blocks.ANDESITE));
    public static final DeferredBlock<Block> MAGENTA_MARBLE_BRICKS = registerBlock("magenta_marble_bricks", () -> makeBlockFrom(Blocks.POLISHED_ANDESITE));
    public static final DeferredBlock<Block> MAGENTA_MARBLE_CHISELED = registerBlock("magenta_marble_chiseled", () -> makeBlockFrom(Blocks.POLISHED_ANDESITE));
    public static final DeferredBlock<Block> MAGENTA_MARBLE_TILES = registerBlock("magenta_marble_tiles", () -> makeBlockFrom(Blocks.POLISHED_ANDESITE));
    public static final DeferredBlock<Block> MAGENTA_POLISHED_MARBLE = registerBlock("magenta_polished_marble", () -> makeBlockFrom(Blocks.POLISHED_ANDESITE));
    public static final DeferredBlock<Block> ORANGE_MARBLE = registerBlock("orange_marble", () -> makeBlockFrom(Blocks.ANDESITE));
    public static final DeferredBlock<Block> ORANGE_MARBLE_BRICKS = registerBlock("orange_marble_bricks", () -> makeBlockFrom(Blocks.POLISHED_ANDESITE));
    public static final DeferredBlock<Block> ORANGE_MARBLE_CHISELED = registerBlock("orange_marble_chiseled", () -> makeBlockFrom(Blocks.POLISHED_ANDESITE));
    public static final DeferredBlock<Block> ORANGE_MARBLE_TILES = registerBlock("orange_marble_tiles", () -> makeBlockFrom(Blocks.POLISHED_ANDESITE));
    public static final DeferredBlock<Block> ORANGE_POLISHED_MARBLE = registerBlock("orange_polished_marble", () -> makeBlockFrom(Blocks.POLISHED_ANDESITE));
    public static final DeferredBlock<Block> PINK_MARBLE = registerBlock("pink_marble", () -> makeBlockFrom(Blocks.ANDESITE));
    public static final DeferredBlock<Block> PINK_MARBLE_BRICKS = registerBlock("pink_marble_bricks", () -> makeBlockFrom(Blocks.POLISHED_ANDESITE));
    public static final DeferredBlock<Block> PINK_MARBLE_CHISELED = registerBlock("pink_marble_chiseled", () -> makeBlockFrom(Blocks.POLISHED_ANDESITE));
    public static final DeferredBlock<Block> PINK_MARBLE_TILES = registerBlock("pink_marble_tiles", () -> makeBlockFrom(Blocks.POLISHED_ANDESITE));
    public static final DeferredBlock<Block> PINK_POLISHED_MARBLE = registerBlock("pink_polished_marble", () -> makeBlockFrom(Blocks.POLISHED_ANDESITE));
    public static final DeferredBlock<Block> PURPLE_MARBLE = registerBlock("purple_marble", () -> makeBlockFrom(Blocks.ANDESITE));
    public static final DeferredBlock<Block> PURPLE_MARBLE_BRICKS = registerBlock("purple_marble_bricks", () -> makeBlockFrom(Blocks.POLISHED_ANDESITE));
    public static final DeferredBlock<Block> PURPLE_MARBLE_CHISELED = registerBlock("purple_marble_chiseled", () -> makeBlockFrom(Blocks.POLISHED_ANDESITE));
    public static final DeferredBlock<Block> PURPLE_MARBLE_TILES = registerBlock("purple_marble_tiles", () -> makeBlockFrom(Blocks.POLISHED_ANDESITE));
    public static final DeferredBlock<Block> PURPLE_POLISHED_MARBLE = registerBlock("purple_polished_marble", () -> makeBlockFrom(Blocks.POLISHED_ANDESITE));
    public static final DeferredBlock<Block> RED_MARBLE = registerBlock("red_marble", () -> makeBlockFrom(Blocks.ANDESITE));
    public static final DeferredBlock<Block> RED_MARBLE_BRICKS = registerBlock("red_marble_bricks", () -> makeBlockFrom(Blocks.POLISHED_ANDESITE));
    public static final DeferredBlock<Block> RED_MARBLE_CHISELED = registerBlock("red_marble_chiseled", () -> makeBlockFrom(Blocks.POLISHED_ANDESITE));
    public static final DeferredBlock<Block> RED_MARBLE_TILES = registerBlock("red_marble_tiles", () -> makeBlockFrom(Blocks.POLISHED_ANDESITE));
    public static final DeferredBlock<Block> RED_POLISHED_MARBLE = registerBlock("red_polished_marble", () -> makeBlockFrom(Blocks.POLISHED_ANDESITE));
    public static final DeferredBlock<Block> YELLOW_MARBLE = registerBlock("yellow_marble", () -> makeBlockFrom(Blocks.ANDESITE));
    public static final DeferredBlock<Block> YELLOW_MARBLE_BRICKS = registerBlock("yellow_marble_bricks", () -> makeBlockFrom(Blocks.POLISHED_ANDESITE));
    public static final DeferredBlock<Block> YELLOW_MARBLE_CHISELED = registerBlock("yellow_marble_chiseled", () -> makeBlockFrom(Blocks.POLISHED_ANDESITE));
    public static final DeferredBlock<Block> YELLOW_MARBLE_TILES = registerBlock("yellow_marble_tiles", () -> makeBlockFrom(Blocks.POLISHED_ANDESITE));
    public static final DeferredBlock<Block> YELLOW_POLISHED_MARBLE = registerBlock("yellow_polished_marble", () -> makeBlockFrom(Blocks.POLISHED_ANDESITE));




    /** Valueable Blocks */
    public static final DeferredBlock<Block> SPIRITUAL_STONE_BLOCK = registerBlock("spiritual_stone_block", () -> makeBlockFrom(Blocks.DIAMOND_BLOCK));


    /** Marble Stairs */

    public static final DeferredBlock<StairBlock> MARBLE_BRICK_STAIRS = registerBlock("marble_brick_stairs",
            () -> makeStairsFrom(ModBlocks.MARBLE_BRICKS));
    public static final DeferredBlock<StairBlock> MARBLE_TILE_STAIRS = registerBlock("marble_tile_stairs",
            () -> makeStairsFrom(ModBlocks.MARBLE_TILES));
    public static final DeferredBlock<StairBlock> CHARRED_MARBLE_BRICK_STAIRS = registerBlock("burned_marble_brick_stairs",
            () -> makeStairsFrom(ModBlocks.CHARRED_MARBLE_BRICKS));
    public static final DeferredBlock<StairBlock> CHARRED_MARBLE_TILE_STAIRS = registerBlock("burned_marble_tile_stairs",
            () -> makeStairsFrom(ModBlocks.CHARRED_MARBLE_TILES));
    public static final DeferredBlock<StairBlock> BLUE_MARBLE_BRICK_STAIRS = registerBlock("blue_marble_brick_stairs",
            () -> makeStairsFrom(ModBlocks.BLUE_MARBLE_BRICKS));
    public static final DeferredBlock<StairBlock> BLUE_MARBLE_TILE_STAIRS = registerBlock("blue_marble_tile_stairs",
            () -> makeStairsFrom(ModBlocks.BLUE_MARBLE_TILES));
    public static final DeferredBlock<StairBlock> BROWN_MARBLE_BRICK_STAIRS = registerBlock("brown_marble_brick_stairs",
            () -> makeStairsFrom(ModBlocks.BROWN_MARBLE_BRICKS));
    public static final DeferredBlock<StairBlock> BROWN_MARBLE_TILE_STAIRS = registerBlock("brown_marble_tile_stairs",
            () -> makeStairsFrom(ModBlocks.BROWN_MARBLE_TILES));
    public static final DeferredBlock<StairBlock> CYAN_MARBLE_BRICK_STAIRS = registerBlock("cyan_marble_brick_stairs",
            () -> makeStairsFrom(ModBlocks.CYAN_MARBLE_BRICKS));
    public static final DeferredBlock<StairBlock> CYAN_MARBLE_TILE_STAIRS = registerBlock("cyan_marble_tile_stairs",
            () -> makeStairsFrom(ModBlocks.CYAN_MARBLE_TILES));
    public static final DeferredBlock<StairBlock> GRAY_MARBLE_BRICK_STAIRS = registerBlock("gray_marble_brick_stairs",
            () -> makeStairsFrom(ModBlocks.GRAY_MARBLE_BRICKS));
    public static final DeferredBlock<StairBlock> GRAY_MARBLE_TILE_STAIRS = registerBlock("gray_marble_tile_stairs",
            () -> makeStairsFrom(ModBlocks.GRAY_MARBLE_TILES));
    public static final DeferredBlock<StairBlock> GREEN_MARBLE_BRICK_STAIRS = registerBlock("green_marble_brick_stairs",
            () -> makeStairsFrom(ModBlocks.GREEN_MARBLE_BRICKS));
    public static final DeferredBlock<StairBlock> GREEN_MARBLE_TILE_STAIRS = registerBlock("green_marble_tile_stairs",
            () -> makeStairsFrom(ModBlocks.GREEN_MARBLE_TILES));
    public static final DeferredBlock<StairBlock> LIGHT_BLUE_MARBLE_BRICK_STAIRS = registerBlock("light_blue_marble_brick_stairs",
            () -> makeStairsFrom(ModBlocks.LIGHT_BLUE_MARBLE_BRICKS));
    public static final DeferredBlock<StairBlock> LIGHT_BLUE_MARBLE_TILE_STAIRS = registerBlock("light_blue_marble_tile_stairs",
            () -> makeStairsFrom(ModBlocks.LIGHT_BLUE_MARBLE_TILES));
    public static final DeferredBlock<StairBlock> LIGHT_GRAY_MARBLE_BRICK_STAIRS = registerBlock("light_gray_marble_brick_stairs",
            () -> makeStairsFrom(ModBlocks.LIGHT_GRAY_MARBLE_BRICKS));
    public static final DeferredBlock<StairBlock> LIGHT_GRAY_MARBLE_TILE_STAIRS = registerBlock("light_gray_marble_tile_stairs",
            () -> makeStairsFrom(ModBlocks.LIGHT_GRAY_MARBLE_TILES));
    public static final DeferredBlock<StairBlock> LIME_MARBLE_BRICK_STAIRS = registerBlock("lime_marble_brick_stairs",
            () -> makeStairsFrom(ModBlocks.LIME_MARBLE_BRICKS));
    public static final DeferredBlock<StairBlock> LIME_MARBLE_TILE_STAIRS = registerBlock("lime_marble_tile_stairs",
            () -> makeStairsFrom(ModBlocks.LIME_MARBLE_TILES));
    public static final DeferredBlock<StairBlock> MAGENTA_MARBLE_BRICK_STAIRS = registerBlock("magenta_marble_brick_stairs",
            () -> makeStairsFrom(ModBlocks.MAGENTA_MARBLE_BRICKS));
    public static final DeferredBlock<StairBlock> MAGENTA_MARBLE_TILE_STAIRS = registerBlock("magenta_marble_tile_stairs",
            () -> makeStairsFrom(ModBlocks.MAGENTA_MARBLE_TILES));
    public static final DeferredBlock<StairBlock> ORANGE_MARBLE_BRICK_STAIRS = registerBlock("orange_marble_brick_stairs",
            () -> makeStairsFrom(ModBlocks.ORANGE_MARBLE_BRICKS));
    public static final DeferredBlock<StairBlock> ORANGE_MARBLE_TILE_STAIRS = registerBlock("orange_marble_tile_stairs",
            () -> makeStairsFrom(ModBlocks.ORANGE_MARBLE_TILES));
    public static final DeferredBlock<StairBlock> PINK_MARBLE_BRICK_STAIRS = registerBlock("pink_marble_brick_stairs",
            () -> makeStairsFrom(ModBlocks.PINK_MARBLE_BRICKS));
    public static final DeferredBlock<StairBlock> PINK_MARBLE_TILE_STAIRS = registerBlock("pink_marble_tile_stairs",
            () -> makeStairsFrom(ModBlocks.PINK_MARBLE_TILES));
    public static final DeferredBlock<StairBlock> PURPLE_MARBLE_BRICK_STAIRS = registerBlock("purple_marble_brick_stairs",
            () -> makeStairsFrom(ModBlocks.PURPLE_MARBLE_BRICKS));
    public static final DeferredBlock<StairBlock> PURPLE_MARBLE_TILE_STAIRS = registerBlock("purple_marble_tile_stairs",
            () -> makeStairsFrom(ModBlocks.PURPLE_MARBLE_TILES));
    public static final DeferredBlock<StairBlock> RED_MARBLE_BRICK_STAIRS = registerBlock("red_marble_brick_stairs",
            () -> makeStairsFrom(ModBlocks.RED_MARBLE_BRICKS));
    public static final DeferredBlock<StairBlock> RED_MARBLE_TILE_STAIRS = registerBlock("red_marble_tile_stairs",
            () -> makeStairsFrom(ModBlocks.RED_MARBLE_TILES));
    public static final DeferredBlock<StairBlock> YELLOW_MARBLE_BRICK_STAIRS = registerBlock("yellow_marble_brick_stairs",
            () -> makeStairsFrom(ModBlocks.YELLOW_MARBLE_BRICKS));
    public static final DeferredBlock<StairBlock> YELLOW_MARBLE_TILE_STAIRS = registerBlock("yellow_marble_tile_stairs",
            () -> makeStairsFrom(ModBlocks.YELLOW_MARBLE_TILES));

    /** Marble Slabs */

    public static final DeferredBlock<SlabBlock> MARBLE_BRICK_SLABS = registerBlock("marble_brick_slabs", () -> makeSlabFrom());
    public static final DeferredBlock<SlabBlock> MARBLE_TILE_SLABS = registerBlock("marble_tile_slabs", () -> makeSlabFrom());
    public static final DeferredBlock<SlabBlock> CHARRED_MARBLE_BRICK_SLABS = registerBlock("burned_marble_brick_slabs", () -> makeSlabFrom());
    public static final DeferredBlock<SlabBlock> CHARRED_MARBLE_TILE_SLABS = registerBlock("burned_marble_tile_slabs", () -> makeSlabFrom());
    public static final DeferredBlock<SlabBlock> BLUE_MARBLE_BRICK_SLABS = registerBlock("blue_marble_brick_slabs", () -> makeSlabFrom());
    public static final DeferredBlock<SlabBlock> BLUE_MARBLE_TILE_SLABS = registerBlock("blue_marble_tile_slabs", () -> makeSlabFrom());
    public static final DeferredBlock<SlabBlock> BROWN_MARBLE_BRICK_SLABS = registerBlock("brown_marble_brick_slabs", () -> makeSlabFrom());
    public static final DeferredBlock<SlabBlock> BROWN_MARBLE_TILE_SLABS = registerBlock("brown_marble_tile_slabs", () -> makeSlabFrom());
    public static final DeferredBlock<SlabBlock> CYAN_MARBLE_BRICK_SLABS = registerBlock("cyan_marble_brick_slabs", () -> makeSlabFrom());
    public static final DeferredBlock<SlabBlock> CYAN_MARBLE_TILE_SLABS = registerBlock("cyan_marble_tile_slabs", () -> makeSlabFrom());
    public static final DeferredBlock<SlabBlock> GRAY_MARBLE_BRICK_SLABS = registerBlock("gray_marble_brick_slabs", () -> makeSlabFrom());
    public static final DeferredBlock<SlabBlock> GRAY_MARBLE_TILE_SLABS = registerBlock("gray_marble_tile_slabs", () -> makeSlabFrom());
    public static final DeferredBlock<SlabBlock> GREEN_MARBLE_BRICK_SLABS = registerBlock("green_marble_brick_slabs", () -> makeSlabFrom());
    public static final DeferredBlock<SlabBlock> GREEN_MARBLE_TILE_SLABS = registerBlock("green_marble_tile_slabs", () -> makeSlabFrom());
    public static final DeferredBlock<SlabBlock> LIGHT_BLUE_MARBLE_BRICK_SLABS = registerBlock("light_blue_marble_brick_slabs", () -> makeSlabFrom());
    public static final DeferredBlock<SlabBlock> LIGHT_BLUE_MARBLE_TILE_SLABS = registerBlock("light_blue_marble_tile_slabs", () -> makeSlabFrom());
    public static final DeferredBlock<SlabBlock> LIGHT_GRAY_MARBLE_BRICK_SLABS = registerBlock("light_gray_marble_brick_slabs", () -> makeSlabFrom());
    public static final DeferredBlock<SlabBlock> LIGHT_GRAY_MARBLE_TILE_SLABS = registerBlock("light_gray_marble_tile_slabs", () -> makeSlabFrom());
    public static final DeferredBlock<SlabBlock> LIME_MARBLE_BRICK_SLABS = registerBlock("lime_marble_brick_slabs", () -> makeSlabFrom());
    public static final DeferredBlock<SlabBlock> LIME_MARBLE_TILE_SLABS = registerBlock("lime_marble_tile_slabs", () -> makeSlabFrom());
    public static final DeferredBlock<SlabBlock> MAGENTA_MARBLE_BRICK_SLABS = registerBlock("magenta_marble_brick_slabs", () -> makeSlabFrom());
    public static final DeferredBlock<SlabBlock> MAGENTA_MARBLE_TILE_SLABS = registerBlock("magenta_marble_tile_slabs", () -> makeSlabFrom());
    public static final DeferredBlock<SlabBlock> ORANGE_MARBLE_BRICK_SLABS = registerBlock("orange_marble_brick_slabs", () -> makeSlabFrom());
    public static final DeferredBlock<SlabBlock> ORANGE_MARBLE_TILE_SLABS = registerBlock("orange_marble_tile_slabs", () -> makeSlabFrom());
    public static final DeferredBlock<SlabBlock> PINK_MARBLE_BRICK_SLABS = registerBlock("pink_marble_brick_slabs", () -> makeSlabFrom());
    public static final DeferredBlock<SlabBlock> PINK_MARBLE_TILE_SLABS = registerBlock("pink_marble_tile_slabs", () -> makeSlabFrom());
    public static final DeferredBlock<SlabBlock> PURPLE_MARBLE_BRICK_SLABS = registerBlock("purple_marble_brick_slabs", () -> makeSlabFrom());
    public static final DeferredBlock<SlabBlock> PURPLE_MARBLE_TILE_SLABS = registerBlock("purple_marble_tile_slabs", () -> makeSlabFrom());
    public static final DeferredBlock<SlabBlock> RED_MARBLE_BRICK_SLABS = registerBlock("red_marble_brick_slabs", () -> makeSlabFrom());
    public static final DeferredBlock<SlabBlock> RED_MARBLE_TILE_SLABS = registerBlock("red_marble_tile_slabs", () -> makeSlabFrom());
    public static final DeferredBlock<SlabBlock> YELLOW_MARBLE_BRICK_SLABS = registerBlock("yellow_marble_brick_slabs", () -> makeSlabFrom());
    public static final DeferredBlock<SlabBlock> YELLOW_MARBLE_TILE_SLABS = registerBlock("yellow_marble_tile_slabs", () -> makeSlabFrom());



    /** Marble Walls */

    public static final DeferredBlock<WallBlock> MARBLE_BRICK_WALLS = registerBlock("marble_brick_wall", () -> makeWallFrom());
    public static final DeferredBlock<WallBlock> MARBLE_TILE_WALLS = registerBlock("marble_tile_wall", () -> makeWallFrom());
    public static final DeferredBlock<WallBlock> CHARRED_MARBLE_BRICK_WALLS = registerBlock("burned_marble_brick_wall", () -> makeWallFrom());
    public static final DeferredBlock<WallBlock> CHARRED_MARBLE_TILE_WALLS = registerBlock("burned_marble_tile_wall", () -> makeWallFrom());
    public static final DeferredBlock<WallBlock> BLUE_MARBLE_BRICK_WALLS = registerBlock("blue_marble_brick_wall", () -> makeWallFrom());
    public static final DeferredBlock<WallBlock> BLUE_MARBLE_TILE_WALLS = registerBlock("blue_marble_tile_wall", () -> makeWallFrom());
    public static final DeferredBlock<WallBlock> BROWN_MARBLE_BRICK_WALLS = registerBlock("brown_marble_brick_wall", () -> makeWallFrom());
    public static final DeferredBlock<WallBlock> BROWN_MARBLE_TILE_WALLS = registerBlock("brown_marble_tile_wall", () -> makeWallFrom());
    public static final DeferredBlock<WallBlock> CYAN_MARBLE_BRICK_WALLS = registerBlock("cyan_marble_brick_wall", () -> makeWallFrom());
    public static final DeferredBlock<WallBlock> CYAN_MARBLE_TILE_WALLS = registerBlock("cyan_marble_tile_wall", () -> makeWallFrom());
    public static final DeferredBlock<WallBlock> GRAY_MARBLE_BRICK_WALLS = registerBlock("gray_marble_brick_wall", () -> makeWallFrom());
    public static final DeferredBlock<WallBlock> GRAY_MARBLE_TILE_WALLS = registerBlock("gray_marble_tile_wall", () -> makeWallFrom());
    public static final DeferredBlock<WallBlock> GREEN_MARBLE_BRICK_WALLS = registerBlock("green_marble_brick_wall", () -> makeWallFrom());
    public static final DeferredBlock<WallBlock> GREEN_MARBLE_TILE_WALLS = registerBlock("green_marble_tile_wall", () -> makeWallFrom());
    public static final DeferredBlock<WallBlock> LIGHT_BLUE_MARBLE_BRICK_WALLS = registerBlock("light_blue_marble_brick_wall", () -> makeWallFrom());
    public static final DeferredBlock<WallBlock> LIGHT_BLUE_MARBLE_TILE_WALLS = registerBlock("light_blue_marble_tile_wall", () -> makeWallFrom());
    public static final DeferredBlock<WallBlock> LIGHT_GRAY_MARBLE_BRICK_WALLS = registerBlock("light_gray_marble_brick_wall", () -> makeWallFrom());
    public static final DeferredBlock<WallBlock> LIGHT_GRAY_MARBLE_TILE_WALLS = registerBlock("light_gray_marble_tile_wall", () -> makeWallFrom());
    public static final DeferredBlock<WallBlock> LIME_MARBLE_BRICK_WALLS = registerBlock("lime_marble_brick_wall", () -> makeWallFrom());
    public static final DeferredBlock<WallBlock> LIME_MARBLE_TILE_WALLS = registerBlock("lime_marble_tile_wall", () -> makeWallFrom());
    public static final DeferredBlock<WallBlock> MAGENTA_MARBLE_BRICK_WALLS = registerBlock("magenta_marble_brick_wall", () -> makeWallFrom());
    public static final DeferredBlock<WallBlock> MAGENTA_MARBLE_TILE_WALLS = registerBlock("magenta_marble_tile_wall", () -> makeWallFrom());
    public static final DeferredBlock<WallBlock> ORANGE_MARBLE_BRICK_WALLS = registerBlock("orange_marble_brick_wall", () -> makeWallFrom());
    public static final DeferredBlock<WallBlock> ORANGE_MARBLE_TILE_WALLS = registerBlock("orange_marble_tile_wall", () -> makeWallFrom());
    public static final DeferredBlock<WallBlock> PINK_MARBLE_BRICK_WALLS = registerBlock("pink_marble_brick_wall", () -> makeWallFrom());
    public static final DeferredBlock<WallBlock> PINK_MARBLE_TILE_WALLS = registerBlock("pink_marble_tile_wall", () -> makeWallFrom());
    public static final DeferredBlock<WallBlock> PURPLE_MARBLE_BRICK_WALLS = registerBlock("purple_marble_brick_wall", () -> makeWallFrom());
    public static final DeferredBlock<WallBlock> PURPLE_MARBLE_TILE_WALLS = registerBlock("purple_marble_tile_wall", () -> makeWallFrom());
    public static final DeferredBlock<WallBlock> RED_MARBLE_BRICK_WALLS = registerBlock("red_marble_brick_wall", () -> makeWallFrom());
    public static final DeferredBlock<WallBlock> RED_MARBLE_TILE_WALLS = registerBlock("red_marble_tile_wall", () -> makeWallFrom());
    public static final DeferredBlock<WallBlock> YELLOW_MARBLE_BRICK_WALLS = registerBlock("yellow_marble_brick_wall", () -> makeWallFrom());
    public static final DeferredBlock<WallBlock> YELLOW_MARBLE_TILE_WALLS = registerBlock("yellow_marble_tile_wall", () -> makeWallFrom());



    //Ores

    public static final DeferredBlock<Block> JADE_ORE = registerBlock("jade_ore",
            () -> new DropExperienceBlock(UniformInt.of(2, 4),
                    BlockBehaviour.Properties.of()
                .strength(4.5f, 3.5f).requiresCorrectToolForDrops().sound(SoundType.DEEPSLATE)));
    public static final DeferredBlock<Block> SPIRITUAL_STONE_CLUSTER = registerBlock("spiritual_stone_cluster",
            () -> new SpiritualStoneClusterBlock(BlockBehaviour.Properties.of()
                            .strength(6.5f, 5.5f).requiresCorrectToolForDrops().sound(SoundType.AMETHYST).noOcclusion(),
                    UniformInt.of(2, 4)));

    public static final DeferredBlock<Block> BLACK_IRON_ORE = registerBlock("black_iron_ore",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(4.5f, 6.0f).requiresCorrectToolForDrops().sound(SoundType.DEEPSLATE)));

    public static final DeferredBlock<Block> FROST_SILVER_ORE = registerBlock("frost_silver_ore",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(4.5f, 6.0f).requiresCorrectToolForDrops().sound(SoundType.DEEPSLATE)));

    //Other Blocks
    public static final DeferredBlock<Block> JADE_BLOCK = registerBlock("jade_block",
            () -> new Block(BlockBehaviour.Properties.of()
                .strength(5.5f, 4.5f).requiresCorrectToolForDrops().sound(SoundType.NETHERITE_BLOCK)));
    public static final DeferredBlock<Block> BLACK_IRON_BLOCK = registerBlock("black_iron_block",
            () -> new Block(BlockBehaviour.Properties.of()
                .strength(7.5f, 11.5f).requiresCorrectToolForDrops().sound(SoundType.METAL)));
    public static final DeferredBlock<Block> FROST_SILVER_BLOCK = registerBlock("frost_silver_block",
            () -> new Block(BlockBehaviour.Properties.of()
                .strength(7.5f, 11.5f).requiresCorrectToolForDrops().sound(SoundType.METAL)));


    //Decorational Blocks
    public static final DeferredBlock<Block> CUSHION_WHITE = registerBlock("cushion_white",
            () -> new CushionBlockBase(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).noOcclusion()));
    public static final DeferredBlock<Block> CUSHION_ORANGE = registerBlock("cushion_orange",
            () -> new CushionBlockBase(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).noOcclusion()));
    public static final DeferredBlock<Block> CUSHION_MAGENTA = registerBlock("cushion_magenta",
            () -> new CushionBlockBase(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).noOcclusion()));
    public static final DeferredBlock<Block> CUSHION_LIGHT_BLUE = registerBlock("cushion_light_blue",
            () -> new CushionBlockBase(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).noOcclusion()));
    public static final DeferredBlock<Block> CUSHION_YELLOW = registerBlock("cushion_yellow",
            () -> new CushionBlockBase(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).noOcclusion()));
    public static final DeferredBlock<Block> CUSHION_LIME = registerBlock("cushion_lime",
            () -> new CushionBlockBase(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).noOcclusion()));
    public static final DeferredBlock<Block> CUSHION_PINK = registerBlock("cushion_pink",
            () -> new CushionBlockBase(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).noOcclusion()));
    public static final DeferredBlock<Block> CUSHION_GRAY = registerBlock("cushion_gray",
            () -> new CushionBlockBase(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).noOcclusion()));
    public static final DeferredBlock<Block> CUSHION_LIGHT_GRAY = registerBlock("cushion_light_gray",
            () -> new CushionBlockBase(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).noOcclusion()));
    public static final DeferredBlock<Block> CUSHION_CYAN = registerBlock("cushion_cyan",
            () -> new CushionBlockBase(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).noOcclusion()));
    public static final DeferredBlock<Block> CUSHION_PURPLE = registerBlock("cushion_purple",
            () -> new CushionBlockBase(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).noOcclusion()));
    public static final DeferredBlock<Block> CUSHION_BLUE = registerBlock("cushion_blue",
            () -> new CushionBlockBase(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).noOcclusion()));
    public static final DeferredBlock<Block> CUSHION_BROWN = registerBlock("cushion_brown",
            () -> new CushionBlockBase(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).noOcclusion()));
    public static final DeferredBlock<Block> CUSHION_GREEN = registerBlock("cushion_green",
            () -> new CushionBlockBase(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).noOcclusion()));
    public static final DeferredBlock<Block> CUSHION_RED = registerBlock("cushion_red",
            () -> new CushionBlockBase(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).noOcclusion()));
    public static final DeferredBlock<Block> CUSHION_BLACK = registerBlock("cushion_black",
            () -> new CushionBlockBase(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).noOcclusion()));




    public static final DeferredBlock<Block> PILL_CAULDRON_HUMAN_LOW = registerBlock("pill_cauldron_low_human",
            () -> new PillCauldronLowHumanBlock(BlockBehaviour.Properties.of().noOcclusion()));

    //Spirit Vein
    public static final DeferredBlock<Block> SPIRIT_VEIN = registerBlock("spirit_vein",
            () -> new SpiritVeinBlock(BlockBehaviour.Properties.of()));



    //Wood
    public static final DeferredBlock<Block> GOLDEN_PALM_LOG = registerBlock("golden_palm_log",
            () -> new ModFlammableRotatePillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LOG)));
    public static final DeferredBlock<Block> STRIPPED_GOLDEN_PALM_LOG = registerBlock("stripped_golden_palm_log",
            () -> new ModFlammableRotatePillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_OAK_LOG)));
    public static final DeferredBlock<Block> GOLDEN_PALM_WOOD = registerBlock("golden_palm_wood",
            () -> new ModFlammableRotatePillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LOG)));
    public static final DeferredBlock<Block> STRIPPED_GOLDEN_PALM_WOOD = registerBlock("stripped_golden_palm_wood",
            () -> new ModFlammableRotatePillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_OAK_WOOD)));

    public static final DeferredBlock<Block> GOLDEN_PALM_PLANKS = registerBlock("golden_palm_planks",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)) {
                @Override
                public boolean isFlammable(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                    return true;
                }
                @Override
                public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                    return 20;
                }
                @Override
                public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                    return 5;
                }
            });


    public static final DeferredBlock<StairBlock> GOLDEN_PALM_STAIRS = registerBlock("golden_palm_stairs",
            () -> new StairBlock(ModBlocks.GOLDEN_PALM_PLANKS.get().defaultBlockState(),
                    BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_STAIRS)));
    public static final DeferredBlock<SlabBlock> GOLDEN_PALM_SLAB = registerBlock("golden_palm_slab",
            () -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SLAB)));

    public static final DeferredBlock<PressurePlateBlock> GOLDEN_PALM_PRESSURE_PLATE = registerBlock("golden_palm_pressure_plate",
            () -> new PressurePlateBlock(BlockSetType.OAK, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PRESSURE_PLATE)));
    public static final DeferredBlock<ButtonBlock> GOLDEN_PALM_BUTTON = registerBlock("golden_palm_button",
            () -> new ButtonBlock(BlockSetType.OAK, 20, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_BUTTON).noCollission()));

    public static final DeferredBlock<FenceBlock> GOLDEN_PALM_FENCE = registerBlock("golden_palm_fence",
            () -> new FenceBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_FENCE)));
    public static final DeferredBlock<FenceGateBlock> GOLDEN_PALM_FENCE_GATE = registerBlock("golden_palm_fence_gate",
            () -> new FenceGateBlock(WoodType.OAK, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_FENCE_GATE)));

    public static final DeferredBlock<DoorBlock> GOLDEN_PALM_DOOR = registerBlock("golden_palm_door",
            () -> new DoorBlock(BlockSetType.OAK, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_DOOR).noOcclusion()));
    public static final DeferredBlock<TrapDoorBlock> GOLDEN_PALM_TRAPDOOR = registerBlock("golden_palm_trapdoor",
            () -> new TrapDoorBlock(BlockSetType.OAK, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_TRAPDOOR).noOcclusion()));

    public static final DeferredBlock<Block> GOLDEN_PALM_LEAVES = registerBlock("golden_palm_leaves",
            () -> new LeavesBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES)) {
                @Override
                public boolean isFlammable(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                    return true;
                }
                @Override
                public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                    return 60;
                }
                @Override
                public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                    return 30;
                }
            });

    public static final DeferredBlock<Block> GOLDEN_PALM_SAPLING = registerBlock("golden_palm_sapling",
            () -> new GoldenPalmSapling(ModTreeGrowers.GOLDEN_PALM, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SAPLING), () -> Blocks.SAND));



    //IronWood
    public static final DeferredBlock<Block> IRONWOOD_LOG = registerBlock("ironwood_log",
            () -> new ModFlammableRotatePillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LOG)));
    public static final DeferredBlock<Block> STRIPPED_IRONWOOD_LOG = registerBlock("stripped_ironwood_log",
            () -> new ModFlammableRotatePillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_OAK_LOG)));
    public static final DeferredBlock<Block> IRONWOOD_WOOD = registerBlock("ironwood_wood",
            () -> new ModFlammableRotatePillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LOG)));
    public static final DeferredBlock<Block> STRIPPED_IRONWOOD_WOOD = registerBlock("stripped_ironwood_wood",
            () -> new ModFlammableRotatePillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STRIPPED_OAK_WOOD)));

    public static final DeferredBlock<DoorBlock> IRONWOOD_DOOR = registerBlock("ironwood_door",
            () -> new DoorBlock(BlockSetType.OAK, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_DOOR).noOcclusion()));
    public static final DeferredBlock<TrapDoorBlock> IRONWOOD_TRAPDOOR = registerBlock("ironwood_trapdoor",
            () -> new TrapDoorBlock(BlockSetType.OAK, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_TRAPDOOR).noOcclusion()));

    public static final DeferredBlock<Block> IRONWOOD_PLANKS = registerBlock("ironwood_planks",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)) {
                @Override
                public boolean isFlammable(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                    return true;
                }
                @Override
                public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                    return 20;
                }
                @Override
                public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                    return 5;
                }
            });

    public static final DeferredBlock<StairBlock> IRONWOOD_STAIRS = registerBlock("ironwood_stairs",
            () -> new StairBlock(ModBlocks.IRONWOOD_PLANKS.get().defaultBlockState(),
                    BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_STAIRS)));
    public static final DeferredBlock<SlabBlock> IRONWOOD_SLABS = registerBlock("ironwood_slabs",
            () -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SLAB)));

    public static final DeferredBlock<PressurePlateBlock> IRONWOOD_PRESSURE_PLATE = registerBlock("ironwood_pressure_plate",
            () -> new PressurePlateBlock(BlockSetType.OAK, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PRESSURE_PLATE)));
    public static final DeferredBlock<ButtonBlock> IRONWOOD_BUTTON = registerBlock("ironwood_button",
            () -> new ButtonBlock(BlockSetType.OAK, 20, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_BUTTON).noCollission()));

    public static final DeferredBlock<FenceBlock> IRONWOOD_FENCE = registerBlock("ironwood_fence",
            () -> new FenceBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_FENCE)));
    public static final DeferredBlock<FenceGateBlock> IRONWOOD_FENCE_GATE = registerBlock("ironwood_fence_gate",
            () -> new FenceGateBlock(WoodType.OAK, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_FENCE_GATE)));

    public static final DeferredBlock<Block> IRONWOOD_LEAVES = registerBlock("ironwood_leaves",
            () -> new LeavesBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES)) {
                @Override
                public boolean isFlammable(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                    return true;
                }
                @Override
                public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                    return 60;
                }
                @Override
                public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
                    return 30;
                }
            });

    public static final DeferredBlock<Block> IRONWOOD_SAPLING = registerBlock("ironwood_sapling",
            () -> new GoldenPalmSapling(ModTreeGrowers.IRONWOOD, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SAPLING), () -> Blocks.GRASS_BLOCK));



    // ── Herbs ──────────────────────────────────────────────────────────────────
    public static final DeferredBlock<Block> WHITE_JADE_ORCHID_CROP = registerHerbStemCrop(
            "white_jade_orchid_crop", ModItems.WHITE_JADE_ORCHID, StemSlowCropBlock.SPEED_SLOW);
    public static final DeferredBlock<Block> HUNDRED_YEAR_GINSENG_CROP = registerHerbCrop(
            "hundred_year_ginseng_crop", ModItems.HUNDRED_YEAR_GINSENG, GenericSlowCropBlock.SPEED_SLOW);
    public static final DeferredBlock<Block> HUNDRED_YEAR_SNOW_GINSENG_CROP = registerHerbCrop(
            "hundred_year_snow_ginseng_crop", ModItems.HUNDRED_YEAR_SNOW_GINSENG, GenericSlowCropBlock.SPEED_SLOW);
    public static final DeferredBlock<Block> HUNDRED_YEAR_FIRE_GINSENG_CROP = registerHerbCrop(
            "hundred_year_fire_ginseng_crop", ModItems.HUNDRED_YEAR_FIRE_GINSENG, GenericSlowCropBlock.SPEED_SLOW);
    public static final DeferredBlock<Block> JADE_DEW_GRASS_CROP = registerHerbJadeDew(
            "jade_dew_grass_crop", ModItems.JADE_DEW_GRASS_SEEDS, JadeDewGrassCropBlock.SPEED_SLOW);

    //Fires / Flames

    public static final DeferredBlock<Block> CRIMSON_LOTUS_FIRE = registerBlockNoItem("crimson_lotus_fire",
            () -> new CrimsonLotusFire(
                    BlockBehaviour.Properties.of()
                            .noLootTable()
                            .replaceable()
                            .noCollission()
                            .instabreak()
                            .lightLevel(state -> 15)
                            .sound(SoundType.WOOL)
                            .pushReaction(PushReaction.BLOCK),
                    8.0f,  // Custom damage
                    20,    // Custom spread delay (lower = faster spread)
                    10     // Custom extinguish chance (higher = less likely to extinguish)
            ));

    // ── Herb crop registration helpers ────────────────────────────────────────
    private static DeferredBlock<Block> registerHerbCrop(String name, Supplier<? extends net.minecraft.world.level.ItemLike> seedItem, float growthChance) {
        return registerBlockNoItem(name,
                () -> new GenericSlowCropBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT), () -> seedItem.get(), growthChance));
    }

    /** Registers a {@link StemSlowCropBlock} (thin stem shape) with no BlockItem. */
    private static DeferredBlock<Block> registerHerbStemCrop(String name, Supplier<? extends net.minecraft.world.level.ItemLike> seedItem, float growthChance) {
        return registerBlockNoItem(name,
                () -> new StemSlowCropBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT), () -> seedItem.get(), growthChance));
    }

    /** Registers a {@link JadeDewGrassCropBlock} (7-stage grass) with no BlockItem. */
    private static DeferredBlock<Block> registerHerbJadeDew(String name, Supplier<? extends net.minecraft.world.level.ItemLike> seedItem, float growthChance) {
        return registerBlockNoItem(name,
                () -> new JadeDewGrassCropBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT), () -> seedItem.get(), growthChance));
    }

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> toReturn = BLOCK.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    private static <T extends Block> DeferredBlock<T> registerBlockNoItem(String name, Supplier<T> block) {
        return BLOCK.register(name, block);
    }

    public static void register(IEventBus eventBus) {
        BLOCK.register(eventBus);
    }
}
