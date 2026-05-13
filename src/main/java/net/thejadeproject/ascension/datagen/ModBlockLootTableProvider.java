package net.thejadeproject.ascension.datagen;

import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyExplosionDecay;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.thejadeproject.ascension.common.blocks.ModBlocks;
import net.thejadeproject.ascension.common.blocks.custom.crops.GenericSlowCropBlock;
import net.thejadeproject.ascension.common.blocks.custom.crops.StemSlowCropBlock;
import net.thejadeproject.ascension.common.blocks.custom.crops.jadedew.JadeDewGrassCropBlock;
import net.thejadeproject.ascension.common.items.ModItems;

import java.util.Set;

public class ModBlockLootTableProvider extends BlockLootSubProvider {

    protected ModBlockLootTableProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {


        dropSelf(ModBlocks.JADE_BLOCK.get());
        dropSelf(ModBlocks.BLACK_IRON_BLOCK.get());
        dropSelf(ModBlocks.FROST_SILVER_BLOCK.get());
        dropSelf(ModBlocks.SPIRITUAL_STONE_BLOCK.get());
        dropSelf(ModBlocks.CUSHION_WHITE.get());
        dropSelf(ModBlocks.CUSHION_LIGHT_GRAY.get());
        dropSelf(ModBlocks.CUSHION_GRAY.get());
        dropSelf(ModBlocks.CUSHION_BLACK.get());
        dropSelf(ModBlocks.CUSHION_BROWN.get());
        dropSelf(ModBlocks.CUSHION_RED.get());
        dropSelf(ModBlocks.CUSHION_ORANGE.get());
        dropSelf(ModBlocks.CUSHION_YELLOW.get());
        dropSelf(ModBlocks.CUSHION_LIME.get());
        dropSelf(ModBlocks.CUSHION_GREEN.get());
        dropSelf(ModBlocks.CUSHION_CYAN.get());
        dropSelf(ModBlocks.CUSHION_LIGHT_BLUE.get());
        dropSelf(ModBlocks.CUSHION_BLUE.get());
        dropSelf(ModBlocks.CUSHION_PURPLE.get());
        dropSelf(ModBlocks.CUSHION_MAGENTA.get());
        dropSelf(ModBlocks.CUSHION_PINK.get());
        dropSelf(ModBlocks.TECHNIQUE_STAND.get());


        /** Marble */

        dropSelf(ModBlocks.RAW_MARBLE.get());
        dropSelf(ModBlocks.POLISHED_MARBLE.get());
        dropSelf(ModBlocks.MARBLE_TILES.get());
        dropSelf(ModBlocks.MARBLE_BRICKS.get());
        dropSelf(ModBlocks.MARBLE_CHISELED.get());
        dropSelf(ModBlocks.CHARRED_MARBLE.get());
        dropSelf(ModBlocks.POLISHED_BURNED_MARBLE.get());
        dropSelf(ModBlocks.CHARRED_MARBLE_CHISELED.get());
        dropSelf(ModBlocks.CHARRED_MARBLE_BRICKS.get());
        dropSelf(ModBlocks.CHARRED_MARBLE_TILES.get());
        dropSelf(ModBlocks.BLUE_MARBLE.get());
        dropSelf(ModBlocks.BLUE_MARBLE_BRICKS.get());
        dropSelf(ModBlocks.BLUE_MARBLE_CHISELED.get());
        dropSelf(ModBlocks.BLUE_MARBLE_TILES.get());
        dropSelf(ModBlocks.BLUE_POLISHED_MARBLE.get());
        dropSelf(ModBlocks.BROWN_MARBLE.get());
        dropSelf(ModBlocks.BROWN_MARBLE_BRICKS.get());
        dropSelf(ModBlocks.BROWN_MARBLE_CHISELED.get());
        dropSelf(ModBlocks.BROWN_MARBLE_TILES.get());
        dropSelf(ModBlocks.BROWN_POLISHED_MARBLE.get());
        dropSelf(ModBlocks.CYAN_MARBLE.get());
        dropSelf(ModBlocks.CYAN_MARBLE_BRICKS.get());
        dropSelf(ModBlocks.CYAN_MARBLE_CHISELED.get());
        dropSelf(ModBlocks.CYAN_MARBLE_TILES.get());
        dropSelf(ModBlocks.CYAN_POLISHED_MARBLE.get());
        dropSelf(ModBlocks.GRAY_MARBLE.get());
        dropSelf(ModBlocks.GRAY_MARBLE_BRICKS.get());
        dropSelf(ModBlocks.GRAY_MARBLE_CHISELED.get());
        dropSelf(ModBlocks.GRAY_MARBLE_TILES.get());
        dropSelf(ModBlocks.GRAY_POLISHED_MARBLE.get());
        dropSelf(ModBlocks.GREEN_MARBLE.get());
        dropSelf(ModBlocks.GREEN_MARBLE_BRICKS.get());
        dropSelf(ModBlocks.GREEN_MARBLE_CHISELED.get());
        dropSelf(ModBlocks.GREEN_MARBLE_TILES.get());
        dropSelf(ModBlocks.GREEN_POLISHED_MARBLE.get());
        dropSelf(ModBlocks.LIGHT_BLUE_MARBLE.get());
        dropSelf(ModBlocks.LIGHT_BLUE_MARBLE_BRICKS.get());
        dropSelf(ModBlocks.LIGHT_BLUE_MARBLE_CHISELED.get());
        dropSelf(ModBlocks.LIGHT_BLUE_MARBLE_TILES.get());
        dropSelf(ModBlocks.LIGHT_BLUE_POLISHED_MARBLE.get());
        dropSelf(ModBlocks.LIGHT_GRAY_MARBLE.get());
        dropSelf(ModBlocks.LIGHT_GRAY_MARBLE_BRICKS.get());
        dropSelf(ModBlocks.LIGHT_GRAY_MARBLE_CHISELED.get());
        dropSelf(ModBlocks.LIGHT_GRAY_MARBLE_TILES.get());
        dropSelf(ModBlocks.POLISHED_LIGHT_GRAY_MARBLE.get());
        dropSelf(ModBlocks.LIME_MARBLE.get());
        dropSelf(ModBlocks.LIME_MARBLE_BRICKS.get());
        dropSelf(ModBlocks.LIME_MARBLE_CHISELED.get());
        dropSelf(ModBlocks.LIME_MARBLE_TILES.get());
        dropSelf(ModBlocks.LIME_POLISHED_MARBLE.get());
        dropSelf(ModBlocks.MAGENTA_MARBLE.get());
        dropSelf(ModBlocks.MAGENTA_MARBLE_BRICKS.get());
        dropSelf(ModBlocks.MAGENTA_MARBLE_CHISELED.get());
        dropSelf(ModBlocks.MAGENTA_MARBLE_TILES.get());
        dropSelf(ModBlocks.MAGENTA_POLISHED_MARBLE.get());
        dropSelf(ModBlocks.ORANGE_MARBLE.get());
        dropSelf(ModBlocks.ORANGE_MARBLE_BRICKS.get());
        dropSelf(ModBlocks.ORANGE_MARBLE_CHISELED.get());
        dropSelf(ModBlocks.ORANGE_MARBLE_TILES.get());
        dropSelf(ModBlocks.ORANGE_POLISHED_MARBLE.get());
        dropSelf(ModBlocks.PINK_MARBLE.get());
        dropSelf(ModBlocks.PINK_MARBLE_BRICKS.get());
        dropSelf(ModBlocks.PINK_MARBLE_CHISELED.get());
        dropSelf(ModBlocks.PINK_MARBLE_TILES.get());
        dropSelf(ModBlocks.PINK_POLISHED_MARBLE.get());
        dropSelf(ModBlocks.PURPLE_MARBLE.get());
        dropSelf(ModBlocks.PURPLE_MARBLE_BRICKS.get());
        dropSelf(ModBlocks.PURPLE_MARBLE_CHISELED.get());
        dropSelf(ModBlocks.PURPLE_MARBLE_TILES.get());
        dropSelf(ModBlocks.PURPLE_POLISHED_MARBLE.get());
        dropSelf(ModBlocks.RED_MARBLE.get());
        dropSelf(ModBlocks.RED_MARBLE_BRICKS.get());
        dropSelf(ModBlocks.RED_MARBLE_CHISELED.get());
        dropSelf(ModBlocks.RED_MARBLE_TILES.get());
        dropSelf(ModBlocks.RED_POLISHED_MARBLE.get());
        dropSelf(ModBlocks.YELLOW_MARBLE.get());
        dropSelf(ModBlocks.YELLOW_MARBLE_BRICKS.get());
        dropSelf(ModBlocks.YELLOW_MARBLE_CHISELED.get());
        dropSelf(ModBlocks.YELLOW_MARBLE_TILES.get());
        dropSelf(ModBlocks.YELLOW_POLISHED_MARBLE.get());


        dropSelf(ModBlocks.PILL_CAULDRON_HUMAN_LOW.get());
        dropSelf(ModBlocks.FLAME_STAND_BLOCK.get());
        dropSelf(ModBlocks.CAULDRON_PEDESTAL_BLOCK.get());
        dropSelf(ModBlocks.SPIRIT_CONDENSER_BLOCK.get());



        add(ModBlocks.SPIRITUAL_STONE_CLUSTER.get(),
                block -> createOreDrop(ModBlocks.SPIRITUAL_STONE_CLUSTER.get(), ModItems.SPIRITUAL_STONE.get()));



        //Herbs
        add(ModBlocks.WHITE_JADE_ORCHID_CROP.get(),
                block -> createSingleItemTable(ModItems.WHITE_JADE_ORCHID.get()));
        add(ModBlocks.SPIRIT_VEIN.get(),
                block -> createSingleItemTable(Blocks.AIR));


        //add(ModBlocks.HUNDRED_YEAR_GINSENG_CROP.get(),
                //block -> createSingleItemTable(ModItems.HUNDRED_YEAR_GINSENG.get()));


        add(ModBlocks.HUNDRED_YEAR_FIRE_GINSENG_CROP.get(),
                block -> createSingleItemTable(ModItems.HUNDRED_YEAR_FIRE_GINSENG.get()));
        add(ModBlocks.HUNDRED_YEAR_SNOW_GINSENG_CROP.get(),
                block -> createSingleItemTable(ModItems.HUNDRED_YEAR_SNOW_GINSENG.get()));



        //Ores
        add(ModBlocks.JADE_ORE.get(),
                block -> createOreDrop(ModBlocks.JADE_ORE.get(), ModItems.JADE.get()));
        add(ModBlocks.BLACK_IRON_ORE.get(),
                block -> createOreDrop(ModBlocks.BLACK_IRON_ORE.get(), ModItems.RAW_BLACK_IRON.get()));
        add(ModBlocks.FROST_SILVER_ORE.get(),
                block -> createOreDrop(ModBlocks.FROST_SILVER_ORE.get(), ModItems.RAW_FROST_SILVER.get()));

        this.dropSelf(ModBlocks.GOLDEN_PALM_LOG.get());
        this.dropSelf(ModBlocks.GOLDEN_PALM_WOOD.get());
        this.dropSelf(ModBlocks.STRIPPED_GOLDEN_PALM_WOOD.get());
        this.dropSelf(ModBlocks.STRIPPED_GOLDEN_PALM_LOG.get());
        this.dropSelf(ModBlocks.GOLDEN_PALM_PLANKS.get());
        this.dropSelf(ModBlocks.GOLDEN_PALM_SAPLING.get());

        this.dropSelf(ModBlocks.IRONWOOD_LOG.get());
        this.dropSelf(ModBlocks.IRONWOOD_WOOD.get());
        this.dropSelf(ModBlocks.STRIPPED_IRONWOOD_WOOD.get());
        this.dropSelf(ModBlocks.STRIPPED_IRONWOOD_LOG.get());
        this.dropSelf(ModBlocks.IRONWOOD_PLANKS.get());
        this.dropSelf(ModBlocks.IRONWOOD_SAPLING.get());

        /** Stairs */
        dropSelf(ModBlocks.GOLDEN_PALM_STAIRS.get());
        dropSelf(ModBlocks.IRONWOOD_STAIRS.get());

        dropSelf(ModBlocks.CHARRED_MARBLE_BRICK_STAIRS.get());
        dropSelf(ModBlocks.CHARRED_MARBLE_TILE_STAIRS.get());
        dropSelf(ModBlocks.MARBLE_BRICK_STAIRS.get());
        dropSelf(ModBlocks.MARBLE_TILE_STAIRS.get());
        dropSelf(ModBlocks.BLUE_MARBLE_BRICK_STAIRS.get());
        dropSelf(ModBlocks.BLUE_MARBLE_TILE_STAIRS.get());
        dropSelf(ModBlocks.BROWN_MARBLE_BRICK_STAIRS.get());
        dropSelf(ModBlocks.BROWN_MARBLE_TILE_STAIRS.get());
        dropSelf(ModBlocks.CYAN_MARBLE_BRICK_STAIRS.get());
        dropSelf(ModBlocks.CYAN_MARBLE_TILE_STAIRS.get());
        dropSelf(ModBlocks.GRAY_MARBLE_BRICK_STAIRS.get());
        dropSelf(ModBlocks.GRAY_MARBLE_TILE_STAIRS.get());
        dropSelf(ModBlocks.GREEN_MARBLE_BRICK_STAIRS.get());
        dropSelf(ModBlocks.GREEN_MARBLE_TILE_STAIRS.get());
        dropSelf(ModBlocks.LIGHT_BLUE_MARBLE_BRICK_STAIRS.get());
        dropSelf(ModBlocks.LIGHT_BLUE_MARBLE_TILE_STAIRS.get());
        dropSelf(ModBlocks.LIGHT_GRAY_MARBLE_BRICK_STAIRS.get());
        dropSelf(ModBlocks.LIGHT_GRAY_MARBLE_TILE_STAIRS.get());
        dropSelf(ModBlocks.LIME_MARBLE_BRICK_STAIRS.get());
        dropSelf(ModBlocks.LIME_MARBLE_TILE_STAIRS.get());
        dropSelf(ModBlocks.MAGENTA_MARBLE_BRICK_STAIRS.get());
        dropSelf(ModBlocks.MAGENTA_MARBLE_TILE_STAIRS.get());
        dropSelf(ModBlocks.ORANGE_MARBLE_BRICK_STAIRS.get());
        dropSelf(ModBlocks.ORANGE_MARBLE_TILE_STAIRS.get());
        dropSelf(ModBlocks.PINK_MARBLE_BRICK_STAIRS.get());
        dropSelf(ModBlocks.PINK_MARBLE_TILE_STAIRS.get());
        dropSelf(ModBlocks.PURPLE_MARBLE_BRICK_STAIRS.get());
        dropSelf(ModBlocks.PURPLE_MARBLE_TILE_STAIRS.get());
        dropSelf(ModBlocks.RED_MARBLE_BRICK_STAIRS.get());
        dropSelf(ModBlocks.RED_MARBLE_TILE_STAIRS.get());
        dropSelf(ModBlocks.YELLOW_MARBLE_BRICK_STAIRS.get());
        dropSelf(ModBlocks.YELLOW_MARBLE_TILE_STAIRS.get());

        /** Walls */
        dropSelf(ModBlocks.MARBLE_BRICK_WALLS.get());
        dropSelf(ModBlocks.MARBLE_TILE_WALLS.get());
        dropSelf(ModBlocks.CHARRED_MARBLE_BRICK_WALLS.get());
        dropSelf(ModBlocks.CHARRED_MARBLE_TILE_WALLS.get());
        dropSelf(ModBlocks.BLUE_MARBLE_BRICK_WALLS.get());
        dropSelf(ModBlocks.BLUE_MARBLE_TILE_WALLS.get());
        dropSelf(ModBlocks.BROWN_MARBLE_BRICK_WALLS.get());
        dropSelf(ModBlocks.BROWN_MARBLE_TILE_WALLS.get());
        dropSelf(ModBlocks.CYAN_MARBLE_BRICK_WALLS.get());
        dropSelf(ModBlocks.CYAN_MARBLE_TILE_WALLS.get());
        dropSelf(ModBlocks.GRAY_MARBLE_BRICK_WALLS.get());
        dropSelf(ModBlocks.GRAY_MARBLE_TILE_WALLS.get());
        dropSelf(ModBlocks.GREEN_MARBLE_BRICK_WALLS.get());
        dropSelf(ModBlocks.GREEN_MARBLE_TILE_WALLS.get());
        dropSelf(ModBlocks.LIGHT_BLUE_MARBLE_BRICK_WALLS.get());
        dropSelf(ModBlocks.LIGHT_BLUE_MARBLE_TILE_WALLS.get());
        dropSelf(ModBlocks.LIGHT_GRAY_MARBLE_BRICK_WALLS.get());
        dropSelf(ModBlocks.LIGHT_GRAY_MARBLE_TILE_WALLS.get());
        dropSelf(ModBlocks.LIME_MARBLE_BRICK_WALLS.get());
        dropSelf(ModBlocks.LIME_MARBLE_TILE_WALLS.get());
        dropSelf(ModBlocks.MAGENTA_MARBLE_BRICK_WALLS.get());
        dropSelf(ModBlocks.MAGENTA_MARBLE_TILE_WALLS.get());
        dropSelf(ModBlocks.ORANGE_MARBLE_BRICK_WALLS.get());
        dropSelf(ModBlocks.ORANGE_MARBLE_TILE_WALLS.get());
        dropSelf(ModBlocks.PINK_MARBLE_BRICK_WALLS.get());
        dropSelf(ModBlocks.PINK_MARBLE_TILE_WALLS.get());
        dropSelf(ModBlocks.PURPLE_MARBLE_BRICK_WALLS.get());
        dropSelf(ModBlocks.PURPLE_MARBLE_TILE_WALLS.get());
        dropSelf(ModBlocks.RED_MARBLE_BRICK_WALLS.get());
        dropSelf(ModBlocks.RED_MARBLE_TILE_WALLS.get());
        dropSelf(ModBlocks.YELLOW_MARBLE_BRICK_WALLS.get());
        dropSelf(ModBlocks.YELLOW_MARBLE_TILE_WALLS.get());


        /** Slabs */
        add(ModBlocks.GOLDEN_PALM_SLAB.get(),
                block -> createSlabItemTable(ModBlocks.GOLDEN_PALM_SLAB.get()));
        add(ModBlocks.IRONWOOD_SLABS.get(),
                block -> createSlabItemTable(ModBlocks.IRONWOOD_SLABS.get()));

        add(ModBlocks.MARBLE_TILE_SLABS.get(),
                block -> createSlabItemTable(ModBlocks.MARBLE_TILE_SLABS.get()));
        add(ModBlocks.MARBLE_BRICK_SLABS.get(),
                block -> createSlabItemTable(ModBlocks.MARBLE_BRICK_SLABS.get()));
        add(ModBlocks.CHARRED_MARBLE_TILE_SLABS.get(),
                block -> createSlabItemTable(ModBlocks.MARBLE_TILE_SLABS.get()));
        add(ModBlocks.CHARRED_MARBLE_BRICK_SLABS.get(),
                block -> createSlabItemTable(ModBlocks.MARBLE_BRICK_SLABS.get()));
        add(ModBlocks.BLUE_MARBLE_BRICK_SLABS.get(),
                block -> createSlabItemTable(ModBlocks.BLUE_MARBLE_BRICK_SLABS.get()));
        add(ModBlocks.BLUE_MARBLE_TILE_SLABS.get(),
                block -> createSlabItemTable(ModBlocks.BLUE_MARBLE_TILE_SLABS.get()));
        add(ModBlocks.BROWN_MARBLE_BRICK_SLABS.get(),
                block -> createSlabItemTable(ModBlocks.BROWN_MARBLE_BRICK_SLABS.get()));
        add(ModBlocks.BROWN_MARBLE_TILE_SLABS.get(),
                block -> createSlabItemTable(ModBlocks.BROWN_MARBLE_TILE_SLABS.get()));
        add(ModBlocks.CYAN_MARBLE_BRICK_SLABS.get(),
                block -> createSlabItemTable(ModBlocks.CYAN_MARBLE_BRICK_SLABS.get()));
        add(ModBlocks.CYAN_MARBLE_TILE_SLABS.get(),
                block -> createSlabItemTable(ModBlocks.CYAN_MARBLE_TILE_SLABS.get()));
        add(ModBlocks.GRAY_MARBLE_BRICK_SLABS.get(),
                block -> createSlabItemTable(ModBlocks.GRAY_MARBLE_BRICK_SLABS.get()));
        add(ModBlocks.GRAY_MARBLE_TILE_SLABS.get(),
                block -> createSlabItemTable(ModBlocks.GRAY_MARBLE_TILE_SLABS.get()));
        add(ModBlocks.GREEN_MARBLE_BRICK_SLABS.get(),
                block -> createSlabItemTable(ModBlocks.GREEN_MARBLE_BRICK_SLABS.get()));
        add(ModBlocks.GREEN_MARBLE_TILE_SLABS.get(),
                block -> createSlabItemTable(ModBlocks.GREEN_MARBLE_TILE_SLABS.get()));
        add(ModBlocks.LIGHT_BLUE_MARBLE_BRICK_SLABS.get(),
                block -> createSlabItemTable(ModBlocks.LIGHT_BLUE_MARBLE_BRICK_SLABS.get()));
        add(ModBlocks.LIGHT_BLUE_MARBLE_TILE_SLABS.get(),
                block -> createSlabItemTable(ModBlocks.LIGHT_BLUE_MARBLE_TILE_SLABS.get()));
        add(ModBlocks.LIGHT_GRAY_MARBLE_BRICK_SLABS.get(),
                block -> createSlabItemTable(ModBlocks.LIGHT_GRAY_MARBLE_BRICK_SLABS.get()));
        add(ModBlocks.LIGHT_GRAY_MARBLE_TILE_SLABS.get(),
                block -> createSlabItemTable(ModBlocks.LIGHT_GRAY_MARBLE_TILE_SLABS.get()));
        add(ModBlocks.LIME_MARBLE_BRICK_SLABS.get(),
                block -> createSlabItemTable(ModBlocks.LIME_MARBLE_BRICK_SLABS.get()));
        add(ModBlocks.LIME_MARBLE_TILE_SLABS.get(),
                block -> createSlabItemTable(ModBlocks.LIME_MARBLE_TILE_SLABS.get()));
        add(ModBlocks.MAGENTA_MARBLE_BRICK_SLABS.get(),
                block -> createSlabItemTable(ModBlocks.MAGENTA_MARBLE_BRICK_SLABS.get()));
        add(ModBlocks.MAGENTA_MARBLE_TILE_SLABS.get(),
                block -> createSlabItemTable(ModBlocks.MAGENTA_MARBLE_TILE_SLABS.get()));
        add(ModBlocks.ORANGE_MARBLE_BRICK_SLABS.get(),
                block -> createSlabItemTable(ModBlocks.ORANGE_MARBLE_BRICK_SLABS.get()));
        add(ModBlocks.ORANGE_MARBLE_TILE_SLABS.get(),
                block -> createSlabItemTable(ModBlocks.ORANGE_MARBLE_TILE_SLABS.get()));
        add(ModBlocks.PINK_MARBLE_BRICK_SLABS.get(),
                block -> createSlabItemTable(ModBlocks.PINK_MARBLE_BRICK_SLABS.get()));
        add(ModBlocks.PINK_MARBLE_TILE_SLABS.get(),
                block -> createSlabItemTable(ModBlocks.PINK_MARBLE_TILE_SLABS.get()));
        add(ModBlocks.PURPLE_MARBLE_BRICK_SLABS.get(),
                block -> createSlabItemTable(ModBlocks.PURPLE_MARBLE_BRICK_SLABS.get()));
        add(ModBlocks.PURPLE_MARBLE_TILE_SLABS.get(),
                block -> createSlabItemTable(ModBlocks.PURPLE_MARBLE_TILE_SLABS.get()));
        add(ModBlocks.RED_MARBLE_BRICK_SLABS.get(),
                block -> createSlabItemTable(ModBlocks.RED_MARBLE_BRICK_SLABS.get()));
        add(ModBlocks.RED_MARBLE_TILE_SLABS.get(),
                block -> createSlabItemTable(ModBlocks.RED_MARBLE_TILE_SLABS.get()));
        add(ModBlocks.YELLOW_MARBLE_BRICK_SLABS.get(),
                block -> createSlabItemTable(ModBlocks.YELLOW_MARBLE_BRICK_SLABS.get()));
        add(ModBlocks.YELLOW_MARBLE_TILE_SLABS.get(),
                block -> createSlabItemTable(ModBlocks.YELLOW_MARBLE_TILE_SLABS.get()));

        dropSelf(ModBlocks.GOLDEN_PALM_PRESSURE_PLATE.get());
        dropSelf(ModBlocks.GOLDEN_PALM_BUTTON.get());
        dropSelf(ModBlocks.GOLDEN_PALM_FENCE.get());
        dropSelf(ModBlocks.GOLDEN_PALM_FENCE_GATE.get());
        dropSelf(ModBlocks.GOLDEN_PALM_TRAPDOOR.get());

        dropSelf(ModBlocks.IRONWOOD_PRESSURE_PLATE.get());
        dropSelf(ModBlocks.IRONWOOD_BUTTON.get());
        dropSelf(ModBlocks.IRONWOOD_FENCE.get());
        dropSelf(ModBlocks.IRONWOOD_FENCE_GATE.get());
        dropSelf(ModBlocks.IRONWOOD_TRAPDOOR.get());

        add(ModBlocks.GOLDEN_PALM_DOOR.get(),
                block -> createDoorTable(ModBlocks.GOLDEN_PALM_DOOR.get()));
        add(ModBlocks.IRONWOOD_DOOR.get(),
                block -> createDoorTable(ModBlocks.IRONWOOD_DOOR.get()));






        this.add(ModBlocks.GOLDEN_PALM_LEAVES.get(), block ->
                createLeavesDropsWithSecondary(
                        block,
                        ModBlocks.GOLDEN_PALM_SAPLING.get(),
                        ModItems.GOLDEN_SUN_LEAF.get(),
                        NORMAL_LEAVES_SAPLING_CHANCES,
                        0.005F
                ));
        this.add(ModBlocks.IRONWOOD_LEAVES.get(), block ->
                createLeavesDropsWithSecondary(
                        block,
                        ModBlocks.IRONWOOD_SAPLING.get(),
                        ModItems.IRONWOOD_SPROUT.get(),
                        NORMAL_LEAVES_SAPLING_CHANCES,
                        0.005F
                ));


        LootItemCondition.Builder lootItemConditionBuilder = LootItemBlockStatePropertyCondition.hasBlockStateProperties(ModBlocks.HUNDRED_YEAR_GINSENG_CROP.get())
                .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(GenericSlowCropBlock.AGE, 3));
        this.add(ModBlocks.HUNDRED_YEAR_GINSENG_CROP.get(), this.createCropDrops(ModBlocks.HUNDRED_YEAR_GINSENG_CROP.get(),
                ModItems.HUNDRED_YEAR_GINSENG.get(), ModItems.HUNDRED_YEAR_GINSENG.get(), lootItemConditionBuilder));
        LootItemCondition.Builder lootItemConditionBuilder2 = LootItemBlockStatePropertyCondition.hasBlockStateProperties(ModBlocks.HUNDRED_YEAR_SNOW_GINSENG_CROP.get())
                .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(GenericSlowCropBlock.AGE, 3));
        this.add(ModBlocks.HUNDRED_YEAR_SNOW_GINSENG_CROP.get(), this.createCropDrops(ModBlocks.HUNDRED_YEAR_SNOW_GINSENG_CROP.get(),
                ModItems.HUNDRED_YEAR_SNOW_GINSENG.get(), ModItems.HUNDRED_YEAR_SNOW_GINSENG.get(), lootItemConditionBuilder2));
        LootItemCondition.Builder lootItemConditionBuilder3 = LootItemBlockStatePropertyCondition.hasBlockStateProperties(ModBlocks.HUNDRED_YEAR_FIRE_GINSENG_CROP.get())
                .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(GenericSlowCropBlock.AGE, 3));
        this.add(ModBlocks.HUNDRED_YEAR_FIRE_GINSENG_CROP.get(), this.createCropDrops(ModBlocks.HUNDRED_YEAR_FIRE_GINSENG_CROP.get(),
                ModItems.HUNDRED_YEAR_FIRE_GINSENG.get(), ModItems.HUNDRED_YEAR_FIRE_GINSENG.get(), lootItemConditionBuilder3));
        LootItemCondition.Builder lootItemConditionBuilder4 = LootItemBlockStatePropertyCondition.hasBlockStateProperties(ModBlocks.WHITE_JADE_ORCHID_CROP.get())
                .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(StemSlowCropBlock.AGE, 3));
        this.add(ModBlocks.WHITE_JADE_ORCHID_CROP.get(), this.createCropDrops(ModBlocks.WHITE_JADE_ORCHID_CROP.get(),
                ModItems.WHITE_JADE_ORCHID.get(), ModItems.WHITE_JADE_ORCHID.get(), lootItemConditionBuilder4));

        LootItemCondition.Builder jadeDewGrassMature =
                LootItemBlockStatePropertyCondition
                        .hasBlockStateProperties(ModBlocks.JADE_DEW_GRASS_CROP.get())
                        .setProperties(StatePropertiesPredicate.Builder.properties()
                                .hasProperty(JadeDewGrassCropBlock.AGE, 7));

        this.add(ModBlocks.JADE_DEW_GRASS_CROP.get(),
                LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1))
                                .add(LootItem.lootTableItem(ModItems.JADE_DEW_GRASS.get())
                                        .when(jadeDewGrassMature))
                                .apply(ApplyExplosionDecay.explosionDecay()))
                        .withPool(LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1))
                                .add(LootItem.lootTableItem(ModItems.JADE_DEW_GRASS_SEEDS.get())
                                        .apply(SetItemCountFunction.setCount(ConstantValue.exactly(1))))
                                .apply(ApplyExplosionDecay.explosionDecay()))
                        .withPool(LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1))
                                .add(LootItem.lootTableItem(ModItems.JADE_DEW_GRASS_SEEDS.get())
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(0.0f, 2.0f)))
                                        .when(jadeDewGrassMature))
                                .apply(ApplyExplosionDecay.explosionDecay())));


    }




    protected LootTable.Builder createLeavesDropsWithSecondary(Block leaves, Block sapling, Item secondaryItem, float[] saplingChances, float secondaryChance) {
        return LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(sapling)
                                .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(leaves)
                                        .setProperties(StatePropertiesPredicate.Builder.properties()
                                                .hasProperty(LeavesBlock.PERSISTENT, false)))
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 2.0F)))
                                .when(LootItemRandomChanceCondition.randomChance(saplingChances[0])))
                        .add(LootItem.lootTableItem(secondaryItem)
                                .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(leaves)
                                        .setProperties(StatePropertiesPredicate.Builder.properties()
                                                .hasProperty(LeavesBlock.PERSISTENT, false)))
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 1.0F)))
                                .when(LootItemRandomChanceCondition.randomChance(secondaryChance))))
                .apply(ApplyExplosionDecay.explosionDecay());
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ModBlocks.BLOCK.getEntries().stream().map(Holder::value)::iterator;
    }
}