package net.thejadeproject.ascension;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.thejadeproject.ascension.common.blocks.ModBlocks;
import net.thejadeproject.ascension.common.items.ModItems;


import java.util.function.Supplier;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AscensionCraft.MOD_ID);

    public static final Supplier<CreativeModeTab> ASCENSION_ITEMS_TAB = CREATIVE_MODE_TAB.register("ascension_items_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.JADE.get()))
                    .withTabsBefore(ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "ascension_blocks_tab"))
                    .title(Component.translatable("creativetab.ascension.items"))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(ModItems.RAW_BLACK_IRON);
                        output.accept(ModItems.BLACK_IRON_INGOT);
                        output.accept(ModItems.BLACK_IRON_NUGGET);
                        output.accept(ModItems.RAW_FROST_SILVER);
                        output.accept(ModItems.FROST_SILVER_INGOT);
                        output.accept(ModItems.FROST_SILVER_NUGGET);
                        output.accept(ModItems.SPIRITUAL_STONE);
                        output.accept(ModItems.SPATIAL_STONE_TIER_1);
                        output.accept(ModItems.JADE_NUGGET);
                        output.accept(ModItems.JADE);
                        output.accept(ModItems.UNDEAD_CORE);
                        output.accept(ModItems.LIVING_CORE);
                        output.accept(ModItems.TALISMAN_PAPER);

                        output.accept(ModItems.ANCESTOR_JOURNAL);

                        output.accept(ModItems.SCHOLARLY_SOUL_RECTIFICATION_OF_NAMES);
                        output.accept(ModItems.SCHOLARLY_SOUL_GREAT_LEARNING);
                        output.accept(ModItems.SCHOLARLY_SOUL_THOUSAND_COMMENTARIES);
                        output.accept(ModItems.SCHOLARLY_SOUL_SAGE_MANDATE);


                    }).build());

    public static final Supplier<CreativeModeTab> ASCENSION_ARTIFACTS_TAB = CREATIVE_MODE_TAB.register("ascension_artifacts_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.SOULSTEAD_RETURN_TALISMAN.get()))
                    .withTabsBefore(ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "ascension_pills_tab"))
                    .title(Component.translatable("creativetab.ascension.artifacts"))
                    .displayItems((itemDisplayParameters, output) -> {

                        output.accept(ModItems.SPATIAL_RING);
                        output.accept(ModItems.REPAIR_SLIP);
                        output.accept(ModItems.ENDER_POUCH);
                        output.accept(ModItems.TABLET_OF_DESTRUCTION_HUMAN);
                        output.accept(ModItems.TABLET_OF_DESTRUCTION_EARTH);
                        output.accept(ModItems.TABLET_OF_DESTRUCTION_HEAVEN);


                        output.accept(ModItems.FIRE_GOURD);

                        output.accept(ModItems.SPATIAL_RUPTURE_TALISMAN_T1);
                        output.accept(ModItems.SPATIAL_RUPTURE_TALISMAN_T2);
                        output.accept(ModItems.SPATIAL_RUPTURE_TALISMAN_T3);
                        output.accept(ModItems.SOULSTEAD_RETURN_TALISMAN);
                        output.accept(ModItems.WORLD_AXIS_TALISMAN);
                        output.accept(ModItems.VOID_MARKING_TALISMAN);
                        output.accept(ModItems.DEATH_RECALL_TALISMAN);
                        output.accept(ModItems.SOUL_ANCHOR_TALISMAN);
                    }).build());

    public static final Supplier<CreativeModeTab> ASCENSION_TOOLS_TAB = CREATIVE_MODE_TAB.register("ascension_tools_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.DIAMOND_BLADE.get()))
                    .withTabsBefore(ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "ascension_items_tab"))
                    .title(Component.translatable("creativetab.ascension.tools"))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(ModItems.WOODEN_BLADE);
                        output.accept(ModItems.STONE_BLADE);
                        output.accept(ModItems.IRON_BLADE);
                        output.accept(ModItems.GOLD_BLADE);
                        output.accept(ModItems.DIAMOND_BLADE);
                        output.accept(ModItems.NETHERITE_BLADE);

                        output.accept(ModItems.WOODEN_SPEAR);
                        output.accept(ModItems.STONE_SPEAR);
                        output.accept(ModItems.IRON_SPEAR);
                        output.accept(ModItems.GOLD_SPEAR);
                        output.accept(ModItems.DIAMOND_SPEAR);
                        output.accept(ModItems.NETHERITE_SPEAR);

                        output.accept(ModItems.SILVER_NEEDLE);

                        output.accept(ModItems.SPIRITUAL_STONE_PICKAXE);
                        output.accept(ModItems.SPIRITUAL_STONE_AXE);
                        output.accept(ModItems.SPIRITUAL_STONE_SHOVEL);
                        output.accept(ModItems.SPIRITUAL_STONE_HOE);
                    }).build());

    public static final Supplier<CreativeModeTab> ASCENSION_BLOCKS_TAB = CREATIVE_MODE_TAB.register("ascension_blocks_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModBlocks.JADE_ORE.get()))
                    .title(Component.translatable("creativetab.ascension.blocks"))
                    .displayItems((itemDisplayParameters, output) -> {


                        output.accept(ModBlocks.TECHNIQUE_STAND);

                        output.accept(ModBlocks.CUSHION_WHITE);
                        output.accept(ModBlocks.CUSHION_LIGHT_GRAY);
                        output.accept(ModBlocks.CUSHION_GRAY);
                        output.accept(ModBlocks.CUSHION_BLACK);
                        output.accept(ModBlocks.CUSHION_BROWN);
                        output.accept(ModBlocks.CUSHION_RED);
                        output.accept(ModBlocks.CUSHION_ORANGE);
                        output.accept(ModBlocks.CUSHION_YELLOW);
                        output.accept(ModBlocks.CUSHION_LIME);
                        output.accept(ModBlocks.CUSHION_GREEN);
                        output.accept(ModBlocks.CUSHION_CYAN);
                        output.accept(ModBlocks.CUSHION_LIGHT_BLUE);
                        output.accept(ModBlocks.CUSHION_BLUE);
                        output.accept(ModBlocks.CUSHION_PURPLE);
                        output.accept(ModBlocks.CUSHION_MAGENTA);
                        output.accept(ModBlocks.CUSHION_PINK);

                        output.accept(ModBlocks.BLACK_IRON_ORE);
                        output.accept(ModBlocks.BLACK_IRON_BLOCK);
                        output.accept(ModBlocks.FROST_SILVER_ORE);
                        output.accept(ModBlocks.FROST_SILVER_BLOCK);
                        output.accept(ModBlocks.JADE_ORE);
                        output.accept(ModBlocks.JADE_BLOCK);
                        output.accept(ModBlocks.SPIRIT_VEIN);
                        output.accept(ModBlocks.SPIRITUAL_STONE_CLUSTER);
                        output.accept(ModBlocks.SPIRITUAL_STONE_BLOCK);


                        output.accept(ModBlocks.GOLDEN_PALM_LOG);
                        output.accept(ModBlocks.GOLDEN_PALM_WOOD);
                        output.accept(ModBlocks.STRIPPED_GOLDEN_PALM_LOG);
                        output.accept(ModBlocks.STRIPPED_GOLDEN_PALM_WOOD);
                        output.accept(ModBlocks.GOLDEN_PALM_PLANKS);
                        output.accept(ModBlocks.GOLDEN_PALM_SAPLING);
                        output.accept(ModBlocks.GOLDEN_PALM_LEAVES);
                        output.accept(ModBlocks.GOLDEN_PALM_STAIRS);
                        output.accept(ModBlocks.GOLDEN_PALM_SLAB);
                        output.accept(ModBlocks.GOLDEN_PALM_PRESSURE_PLATE);
                        output.accept(ModBlocks.GOLDEN_PALM_BUTTON);
                        output.accept(ModBlocks.GOLDEN_PALM_FENCE);
                        output.accept(ModBlocks.GOLDEN_PALM_FENCE_GATE);
                        output.accept(ModBlocks.GOLDEN_PALM_DOOR);
                        output.accept(ModBlocks.GOLDEN_PALM_TRAPDOOR);

                        output.accept(ModBlocks.IRONWOOD_LOG);
                        output.accept(ModBlocks.IRONWOOD_WOOD);
                        output.accept(ModBlocks.STRIPPED_IRONWOOD_LOG);
                        output.accept(ModBlocks.STRIPPED_IRONWOOD_WOOD);
                        output.accept(ModBlocks.IRONWOOD_PLANKS);
                        output.accept(ModBlocks.IRONWOOD_SAPLING);
                        output.accept(ModBlocks.IRONWOOD_LEAVES);
                        output.accept(ModBlocks.IRONWOOD_STAIRS);
                        output.accept(ModBlocks.IRONWOOD_SLABS);
                        output.accept(ModBlocks.IRONWOOD_PRESSURE_PLATE);
                        output.accept(ModBlocks.IRONWOOD_BUTTON);
                        output.accept(ModBlocks.IRONWOOD_FENCE);
                        output.accept(ModBlocks.IRONWOOD_FENCE_GATE);
                        output.accept(ModBlocks.IRONWOOD_DOOR);
                        output.accept(ModBlocks.IRONWOOD_TRAPDOOR);

                        /** Marble */
                        output.accept(ModBlocks.RAW_MARBLE);
                        output.accept(ModBlocks.POLISHED_MARBLE);
                        output.accept(ModBlocks.MARBLE_BRICKS);
                        output.accept(ModBlocks.MARBLE_CHISELED);
                        output.accept(ModBlocks.MARBLE_TILES);
                        output.accept(ModBlocks.MARBLE_BRICK_STAIRS);
                        output.accept(ModBlocks.MARBLE_TILE_STAIRS);
                        output.accept(ModBlocks.MARBLE_BRICK_SLABS);
                        output.accept(ModBlocks.MARBLE_TILE_SLABS);
                        output.accept(ModBlocks.MARBLE_BRICK_WALLS);
                        output.accept(ModBlocks.MARBLE_TILE_WALLS);
                        output.accept(ModBlocks.LIGHT_GRAY_MARBLE);
                        output.accept(ModBlocks.POLISHED_LIGHT_GRAY_MARBLE);
                        output.accept(ModBlocks.LIGHT_GRAY_MARBLE_BRICKS);
                        output.accept(ModBlocks.LIGHT_GRAY_MARBLE_CHISELED);
                        output.accept(ModBlocks.LIGHT_GRAY_MARBLE_TILES);
                        output.accept(ModBlocks.LIGHT_GRAY_MARBLE_BRICK_STAIRS);
                        output.accept(ModBlocks.LIGHT_GRAY_MARBLE_TILE_STAIRS);
                        output.accept(ModBlocks.LIGHT_GRAY_MARBLE_BRICK_SLABS);
                        output.accept(ModBlocks.LIGHT_GRAY_MARBLE_TILE_SLABS);
                        output.accept(ModBlocks.LIGHT_GRAY_MARBLE_BRICK_WALLS);
                        output.accept(ModBlocks.LIGHT_GRAY_MARBLE_TILE_WALLS);
                        output.accept(ModBlocks.GRAY_MARBLE);
                        output.accept(ModBlocks.GRAY_POLISHED_MARBLE);
                        output.accept(ModBlocks.GRAY_MARBLE_BRICKS);
                        output.accept(ModBlocks.GRAY_MARBLE_CHISELED);
                        output.accept(ModBlocks.GRAY_MARBLE_TILES);
                        output.accept(ModBlocks.GRAY_MARBLE_BRICK_STAIRS);
                        output.accept(ModBlocks.GRAY_MARBLE_TILE_STAIRS);
                        output.accept(ModBlocks.GRAY_MARBLE_BRICK_SLABS);
                        output.accept(ModBlocks.GRAY_MARBLE_TILE_SLABS);
                        output.accept(ModBlocks.GRAY_MARBLE_BRICK_WALLS);
                        output.accept(ModBlocks.GRAY_MARBLE_TILE_WALLS);
                        output.accept(ModBlocks.CHARRED_MARBLE);
                        output.accept(ModBlocks.POLISHED_BURNED_MARBLE);
                        output.accept(ModBlocks.CHARRED_MARBLE_BRICKS);
                        output.accept(ModBlocks.CHARRED_MARBLE_CHISELED);
                        output.accept(ModBlocks.CHARRED_MARBLE_TILES);
                        output.accept(ModBlocks.CHARRED_MARBLE_BRICK_STAIRS);
                        output.accept(ModBlocks.CHARRED_MARBLE_TILE_STAIRS);
                        output.accept(ModBlocks.CHARRED_MARBLE_BRICK_SLABS);
                        output.accept(ModBlocks.CHARRED_MARBLE_TILE_SLABS);
                        output.accept(ModBlocks.CHARRED_MARBLE_BRICK_WALLS);
                        output.accept(ModBlocks.CHARRED_MARBLE_TILE_WALLS);
                        output.accept(ModBlocks.BROWN_MARBLE);
                        output.accept(ModBlocks.BROWN_POLISHED_MARBLE);
                        output.accept(ModBlocks.BROWN_MARBLE_BRICKS);
                        output.accept(ModBlocks.BROWN_MARBLE_CHISELED);
                        output.accept(ModBlocks.BROWN_MARBLE_TILES);
                        output.accept(ModBlocks.BROWN_MARBLE_BRICK_STAIRS);
                        output.accept(ModBlocks.BROWN_MARBLE_TILE_STAIRS);
                        output.accept(ModBlocks.BROWN_MARBLE_BRICK_SLABS);
                        output.accept(ModBlocks.BROWN_MARBLE_TILE_SLABS);
                        output.accept(ModBlocks.BROWN_MARBLE_BRICK_WALLS);
                        output.accept(ModBlocks.BROWN_MARBLE_TILE_WALLS);
                        output.accept(ModBlocks.RED_MARBLE);
                        output.accept(ModBlocks.RED_POLISHED_MARBLE);
                        output.accept(ModBlocks.RED_MARBLE_BRICKS);
                        output.accept(ModBlocks.RED_MARBLE_CHISELED);
                        output.accept(ModBlocks.RED_MARBLE_TILES);
                        output.accept(ModBlocks.RED_MARBLE_BRICK_STAIRS);
                        output.accept(ModBlocks.RED_MARBLE_TILE_STAIRS);
                        output.accept(ModBlocks.RED_MARBLE_BRICK_SLABS);
                        output.accept(ModBlocks.RED_MARBLE_TILE_SLABS);
                        output.accept(ModBlocks.RED_MARBLE_BRICK_WALLS);
                        output.accept(ModBlocks.RED_MARBLE_TILE_WALLS);
                        output.accept(ModBlocks.ORANGE_MARBLE);
                        output.accept(ModBlocks.ORANGE_POLISHED_MARBLE);
                        output.accept(ModBlocks.ORANGE_MARBLE_BRICKS);
                        output.accept(ModBlocks.ORANGE_MARBLE_CHISELED);
                        output.accept(ModBlocks.ORANGE_MARBLE_TILES);
                        output.accept(ModBlocks.ORANGE_MARBLE_BRICK_STAIRS);
                        output.accept(ModBlocks.ORANGE_MARBLE_TILE_STAIRS);
                        output.accept(ModBlocks.ORANGE_MARBLE_BRICK_SLABS);
                        output.accept(ModBlocks.ORANGE_MARBLE_TILE_SLABS);
                        output.accept(ModBlocks.ORANGE_MARBLE_BRICK_WALLS);
                        output.accept(ModBlocks.ORANGE_MARBLE_TILE_WALLS);
                        output.accept(ModBlocks.YELLOW_MARBLE);
                        output.accept(ModBlocks.YELLOW_POLISHED_MARBLE);
                        output.accept(ModBlocks.YELLOW_MARBLE_BRICKS);
                        output.accept(ModBlocks.YELLOW_MARBLE_CHISELED);
                        output.accept(ModBlocks.YELLOW_MARBLE_TILES);
                        output.accept(ModBlocks.YELLOW_MARBLE_BRICK_STAIRS);
                        output.accept(ModBlocks.YELLOW_MARBLE_TILE_STAIRS);
                        output.accept(ModBlocks.YELLOW_MARBLE_BRICK_SLABS);
                        output.accept(ModBlocks.YELLOW_MARBLE_TILE_SLABS);
                        output.accept(ModBlocks.YELLOW_MARBLE_BRICK_WALLS);
                        output.accept(ModBlocks.YELLOW_MARBLE_TILE_WALLS);
                        output.accept(ModBlocks.LIME_MARBLE);
                        output.accept(ModBlocks.LIME_POLISHED_MARBLE);
                        output.accept(ModBlocks.LIME_MARBLE_BRICKS);
                        output.accept(ModBlocks.LIME_MARBLE_CHISELED);
                        output.accept(ModBlocks.LIME_MARBLE_TILES);
                        output.accept(ModBlocks.LIME_MARBLE_BRICK_STAIRS);
                        output.accept(ModBlocks.LIME_MARBLE_TILE_STAIRS);
                        output.accept(ModBlocks.LIME_MARBLE_BRICK_SLABS);
                        output.accept(ModBlocks.LIME_MARBLE_TILE_SLABS);
                        output.accept(ModBlocks.LIME_MARBLE_BRICK_WALLS);
                        output.accept(ModBlocks.LIME_MARBLE_TILE_WALLS);
                        output.accept(ModBlocks.GREEN_MARBLE);
                        output.accept(ModBlocks.GREEN_POLISHED_MARBLE);
                        output.accept(ModBlocks.GREEN_MARBLE_BRICKS);
                        output.accept(ModBlocks.GREEN_MARBLE_CHISELED);
                        output.accept(ModBlocks.GREEN_MARBLE_TILES);
                        output.accept(ModBlocks.GREEN_MARBLE_BRICK_STAIRS);
                        output.accept(ModBlocks.GREEN_MARBLE_TILE_STAIRS);
                        output.accept(ModBlocks.GREEN_MARBLE_BRICK_SLABS);
                        output.accept(ModBlocks.GREEN_MARBLE_TILE_SLABS);
                        output.accept(ModBlocks.GREEN_MARBLE_BRICK_WALLS);
                        output.accept(ModBlocks.GREEN_MARBLE_TILE_WALLS);
                        output.accept(ModBlocks.CYAN_MARBLE);
                        output.accept(ModBlocks.CYAN_POLISHED_MARBLE);
                        output.accept(ModBlocks.CYAN_MARBLE_BRICKS);
                        output.accept(ModBlocks.CYAN_MARBLE_CHISELED);
                        output.accept(ModBlocks.CYAN_MARBLE_TILES);
                        output.accept(ModBlocks.CYAN_MARBLE_BRICK_STAIRS);
                        output.accept(ModBlocks.CYAN_MARBLE_TILE_STAIRS);
                        output.accept(ModBlocks.CYAN_MARBLE_BRICK_SLABS);
                        output.accept(ModBlocks.CYAN_MARBLE_TILE_SLABS);
                        output.accept(ModBlocks.CYAN_MARBLE_BRICK_WALLS);
                        output.accept(ModBlocks.CYAN_MARBLE_TILE_WALLS);
                        output.accept(ModBlocks.LIGHT_BLUE_MARBLE);
                        output.accept(ModBlocks.LIGHT_BLUE_POLISHED_MARBLE);
                        output.accept(ModBlocks.LIGHT_BLUE_MARBLE_BRICKS);
                        output.accept(ModBlocks.LIGHT_BLUE_MARBLE_CHISELED);
                        output.accept(ModBlocks.LIGHT_BLUE_MARBLE_TILES);
                        output.accept(ModBlocks.LIGHT_BLUE_MARBLE_BRICK_STAIRS);
                        output.accept(ModBlocks.LIGHT_BLUE_MARBLE_TILE_STAIRS);
                        output.accept(ModBlocks.LIGHT_BLUE_MARBLE_BRICK_SLABS);
                        output.accept(ModBlocks.LIGHT_BLUE_MARBLE_TILE_SLABS);
                        output.accept(ModBlocks.LIGHT_BLUE_MARBLE_BRICK_WALLS);
                        output.accept(ModBlocks.LIGHT_BLUE_MARBLE_TILE_WALLS);
                        output.accept(ModBlocks.BLUE_MARBLE);
                        output.accept(ModBlocks.BLUE_POLISHED_MARBLE);
                        output.accept(ModBlocks.BLUE_MARBLE_BRICKS);
                        output.accept(ModBlocks.BLUE_MARBLE_CHISELED);
                        output.accept(ModBlocks.BLUE_MARBLE_TILES);
                        output.accept(ModBlocks.BLUE_MARBLE_BRICK_STAIRS);
                        output.accept(ModBlocks.BLUE_MARBLE_TILE_STAIRS);
                        output.accept(ModBlocks.BLUE_MARBLE_BRICK_SLABS);
                        output.accept(ModBlocks.BLUE_MARBLE_TILE_SLABS);
                        output.accept(ModBlocks.BLUE_MARBLE_BRICK_WALLS);
                        output.accept(ModBlocks.BLUE_MARBLE_TILE_WALLS);
                        output.accept(ModBlocks.PURPLE_MARBLE);
                        output.accept(ModBlocks.PURPLE_POLISHED_MARBLE);
                        output.accept(ModBlocks.PURPLE_MARBLE_BRICKS);
                        output.accept(ModBlocks.PURPLE_MARBLE_CHISELED);
                        output.accept(ModBlocks.PURPLE_MARBLE_TILES);
                        output.accept(ModBlocks.PURPLE_MARBLE_BRICK_STAIRS);
                        output.accept(ModBlocks.PURPLE_MARBLE_TILE_STAIRS);
                        output.accept(ModBlocks.PURPLE_MARBLE_BRICK_SLABS);
                        output.accept(ModBlocks.PURPLE_MARBLE_TILE_SLABS);
                        output.accept(ModBlocks.PURPLE_MARBLE_BRICK_WALLS);
                        output.accept(ModBlocks.PURPLE_MARBLE_TILE_WALLS);
                        output.accept(ModBlocks.MAGENTA_MARBLE);
                        output.accept(ModBlocks.MAGENTA_POLISHED_MARBLE);
                        output.accept(ModBlocks.MAGENTA_MARBLE_BRICKS);
                        output.accept(ModBlocks.MAGENTA_MARBLE_CHISELED);
                        output.accept(ModBlocks.MAGENTA_MARBLE_TILES);
                        output.accept(ModBlocks.MAGENTA_MARBLE_BRICK_STAIRS);
                        output.accept(ModBlocks.MAGENTA_MARBLE_TILE_STAIRS);
                        output.accept(ModBlocks.MAGENTA_MARBLE_BRICK_SLABS);
                        output.accept(ModBlocks.MAGENTA_MARBLE_TILE_SLABS);
                        output.accept(ModBlocks.MAGENTA_MARBLE_BRICK_WALLS);
                        output.accept(ModBlocks.MAGENTA_MARBLE_TILE_WALLS);
                        output.accept(ModBlocks.PINK_MARBLE);
                        output.accept(ModBlocks.PINK_POLISHED_MARBLE);
                        output.accept(ModBlocks.PINK_MARBLE_BRICKS);
                        output.accept(ModBlocks.PINK_MARBLE_CHISELED);
                        output.accept(ModBlocks.PINK_MARBLE_TILES);
                        output.accept(ModBlocks.PINK_MARBLE_BRICK_STAIRS);
                        output.accept(ModBlocks.PINK_MARBLE_TILE_STAIRS);
                        output.accept(ModBlocks.PINK_MARBLE_BRICK_SLABS);
                        output.accept(ModBlocks.PINK_MARBLE_TILE_SLABS);
                        output.accept(ModBlocks.PINK_MARBLE_BRICK_WALLS);
                        output.accept(ModBlocks.PINK_MARBLE_TILE_WALLS);




                    }).build());

    public static final Supplier<CreativeModeTab> ASCENSION_HERBS_TAB = CREATIVE_MODE_TAB.register("ascension_herbs_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.PEACH.get()))
                    .withTabsBefore(ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "ascension_items_tab"))
                    .title(Component.translatable("creativetab.ascension.herbs"))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(ModBlocks.PILL_CAULDRON_HUMAN_LOW);
                        output.accept(ModBlocks.CAULDRON_PEDESTAL_BLOCK);
                        output.accept(ModBlocks.FLAME_STAND_BLOCK);
                        output.accept(ModBlocks.SPIRIT_CONDENSER_BLOCK);
                        output.accept(ModItems.FAN);
                        output.accept(ModItems.MORTAR_PESTLE);
                        output.accept(ModItems.SPIRITUAL_MEAL);

                        output.accept(ModItems.FLAME);
                        output.accept(ModItems.SOUL_FLAME);
                        output.accept(ModItems.CRIMSON_LOTUS_FLAME);

                        output.accept(ModItems.PEACH);
                        output.accept(ModItems.GOLDEN_SUN_LEAF);
                        output.accept(ModItems.JADE_BAMBOO_OF_SERENITY);
                        output.accept(ModItems.JADE_DEW_GRASS_SEEDS);
                        output.accept(ModItems.JADE_DEW_GRASS);
                        output.accept(ModItems.HUNDRED_YEAR_GINSENG);
                        output.accept(ModItems.HUNDRED_YEAR_SNOW_GINSENG);
                        output.accept(ModItems.HUNDRED_YEAR_FIRE_GINSENG);
                        output.accept(ModItems.IRONWOOD_SPROUT);
                        output.accept(ModItems.WHITE_JADE_ORCHID);


                        output.accept(ModItems.HERB_POUCH);


                    }).build());

    public static final Supplier<CreativeModeTab> ASCENSION_PILLS_TAB = CREATIVE_MODE_TAB.register("ascension_pills_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.PILL_RESIDUE.get()))
                    .withTabsBefore(ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "ascension_herbs_tab"))
                    .title(Component.translatable("creativetab.ascension.pills"))
                    .displayItems((itemDisplayParameters, output) -> {

                        //Waste
                        output.accept(ModItems.PILL_RESIDUE);

                        //Cultivation
                        output.accept(ModItems.INNER_REINFORCEMENT_PILL);
                        output.accept(ModItems.SOUL_FOCUS_PILL);
                        output.accept(ModItems.ESSENCE_GATHERING_PILL);

                        //Qi
                        output.accept(ModItems.QI_REPLENISHING_PILL);

                        //Medicinal
                        output.accept(ModItems.FASTING_PILL_T1);
                        output.accept(ModItems.FASTING_PILL_T2);
                        output.accept(ModItems.FASTING_PILL_T3);
                        output.accept(ModItems.CLEANSING_PILL_T1);
                        output.accept(ModItems.CLEANSING_PILL_T2);
                        output.accept(ModItems.CLEANSING_PILL_T3);
                        output.accept(ModItems.CLEANSING_PILL_T4);
                        output.accept(ModItems.ANTIDOTE_PILL_QDP);
                        output.accept(ModItems.QI_ENHANCED_REGEN_PILL);

                        //Poison
                        output.accept(ModItems.QI_DEVOURING_PARASITE_PILL);
                        output.accept(ModItems.QI_DEVOURING_POWDER);


                        //Misc
                        output.accept(ModItems.NEUTRALITY_PILL);
                        output.accept(ModItems.MARROW_CLEANSE_PILL);
                        output.accept(ModItems.CRIMSON_LOTUS_BONE_PILL);


                        //Important Pills


                    }).build());


    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TAB.register(eventBus);
    }
}
