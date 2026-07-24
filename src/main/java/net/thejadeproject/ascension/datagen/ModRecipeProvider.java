package net.thejadeproject.ascension.datagen;


import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.common.blocks.ModBlocks;
import net.thejadeproject.ascension.common.items.data_components.ModDataComponents;
import net.thejadeproject.ascension.datagen.builders.ComponentShapedRecipeBuilder;
import net.thejadeproject.ascension.datagen.builders.PillCauldronRecipeBuilder;
import net.thejadeproject.ascension.common.items.ModItems;

import net.thejadeproject.ascension.util.ModTags;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class ModRecipeProvider extends RecipeProvider implements IConditionBuilder {
    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }


    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {

        SmithingTransformRecipeBuilder.smithing(Ingredient.of(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE), Ingredient.of(ModItems.DIAMOND_BLADE.get()), Ingredient.of(Items.NETHERITE_INGOT), RecipeCategory.MISC, ModItems.NETHERITE_BLADE.get());
        SmithingTransformRecipeBuilder.smithing(Ingredient.of(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE), Ingredient.of(ModItems.DIAMOND_SPEAR.get()), Ingredient.of(Items.NETHERITE_INGOT), RecipeCategory.MISC, ModItems.NETHERITE_SPEAR.get());


        record CushionRecipeData(String color, Supplier<Block> cushion, Block wool) {
        }

        List<CushionRecipeData> cushions = List.of(
                new CushionRecipeData("white", ModBlocks.CUSHION_WHITE::get, Blocks.WHITE_WOOL),
                new CushionRecipeData("light_gray", ModBlocks.CUSHION_LIGHT_GRAY::get, Blocks.LIGHT_GRAY_WOOL),
                new CushionRecipeData("gray", ModBlocks.CUSHION_GRAY::get, Blocks.GRAY_WOOL),
                new CushionRecipeData("black", ModBlocks.CUSHION_BLACK::get, Blocks.BLACK_WOOL),
                new CushionRecipeData("brown", ModBlocks.CUSHION_BROWN::get, Blocks.BROWN_WOOL),
                new CushionRecipeData("red", ModBlocks.CUSHION_RED::get, Blocks.RED_WOOL),
                new CushionRecipeData("orange", ModBlocks.CUSHION_ORANGE::get, Blocks.ORANGE_WOOL),
                new CushionRecipeData("yellow", ModBlocks.CUSHION_YELLOW::get, Blocks.YELLOW_WOOL),
                new CushionRecipeData("lime", ModBlocks.CUSHION_LIME::get, Blocks.LIME_WOOL),
                new CushionRecipeData("green", ModBlocks.CUSHION_GREEN::get, Blocks.GREEN_WOOL),
                new CushionRecipeData("cyan", ModBlocks.CUSHION_CYAN::get, Blocks.CYAN_WOOL),
                new CushionRecipeData("light_blue", ModBlocks.CUSHION_LIGHT_BLUE::get, Blocks.LIGHT_BLUE_WOOL),
                new CushionRecipeData("blue", ModBlocks.CUSHION_BLUE::get, Blocks.BLUE_WOOL),
                new CushionRecipeData("purple", ModBlocks.CUSHION_PURPLE::get, Blocks.PURPLE_WOOL),
                new CushionRecipeData("magenta", ModBlocks.CUSHION_MAGENTA::get, Blocks.MAGENTA_WOOL),
                new CushionRecipeData("pink", ModBlocks.CUSHION_PINK::get, Blocks.PINK_WOOL)
        );

        for (CushionRecipeData data : cushions) {
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, data.cushion().get())
                    .pattern("   ")
                    .pattern("EEE")
                    .pattern("EWE")
                    .define('W', ItemTags.PLANKS)
                    .define('E', data.wool())
                    .unlockedBy("has_" + data.color() + "_wool", has(data.wool()))
                    .save(recipeOutput, "ascension:shaped/" + data.color() + "_cushion");
        }



        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.UNSTABLE_5_ELEMENT_ESSENCE.get(), 1)
                .requires(ModItems.FIRE_CORE)
                .requires(ModItems.WATER_CORE)
                .requires(ModItems.WOOD_CORE)
                .requires(ModItems.EARTH_CORE)
                .requires(ModItems.METAL_CORE)
                .unlockedBy("has_fire_core", has(ModItems.FIRE_CORE))
                .save(recipeOutput, "ascension:shapeless/unstable_5_element_essence");

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.BLINDED_SENSES_POWDER.get(), 2)
                .requires(ModItems.MORTAR_PESTLE)
                .requires(ModItems.BLINDED_SENSES_POISON_PILL)
                .unlockedBy("has_mortar_and_pestle", has(ModItems.MORTAR_PESTLE))
                .save(recipeOutput, "ascension:shapeless/blinded_senses_powder");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.CORROSIVE_POISON_POWDER.get(), 2)
                .requires(ModItems.MORTAR_PESTLE)
                .requires(ModItems.CORROSIVE_POISON_PILL)
                .unlockedBy("has_mortar_and_pestle", has(ModItems.MORTAR_PESTLE))
                .save(recipeOutput, "ascension:shapeless/corrosive_poison_powder");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.CRACKED_MERIDIANS_POWDER.get(), 2)
                .requires(ModItems.MORTAR_PESTLE)
                .requires(ModItems.CRACKED_MERIDIANS_POISON_PILL)
                .unlockedBy("has_mortar_and_pestle", has(ModItems.MORTAR_PESTLE))
                .save(recipeOutput, "ascension:shapeless/cracked_meridians_powder");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.FROST_SILKWORM_POWDER.get(), 2)
                .requires(ModItems.MORTAR_PESTLE)
                .requires(ModItems.FROST_SILKWORM_POISON_PILL)
                .unlockedBy("has_mortar_and_pestle", has(ModItems.MORTAR_PESTLE))
                .save(recipeOutput, "ascension:shapeless/frost_silkworm_powder");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.PARALYZED_BODY_POWDER.get(), 2)
                .requires(ModItems.MORTAR_PESTLE)
                .requires(ModItems.PARALYZED_BODY_POISON_PILL)
                .unlockedBy("has_mortar_and_pestle", has(ModItems.MORTAR_PESTLE))
                .save(recipeOutput, "ascension:shapeless/paralyzed_body_powder");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.QI_DEVOURING_POWDER.get(), 2)
                .requires(ModItems.MORTAR_PESTLE)
                .requires(ModItems.QI_DEVOURING_PARASITE_PILL)
                .unlockedBy("has_mortar_and_pestle", has(ModItems.MORTAR_PESTLE))
                .save(recipeOutput, "ascension:shapeless/qi_devouring_powder");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.SCORCHING_YANG_POWDER.get(), 2)
                .requires(ModItems.MORTAR_PESTLE)
                .requires(ModItems.SCORCHING_YANG_POISON_PILL)
                .unlockedBy("has_mortar_and_pestle", has(ModItems.MORTAR_PESTLE))
                .save(recipeOutput, "ascension:shapeless/scorching_yang_powder");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.VENOMOUS_MERIDIAN_POWDER.get(), 2)
                .requires(ModItems.MORTAR_PESTLE)
                .requires(ModItems.VENOMOUS_MERIDIAN_POISON_PILL)
                .unlockedBy("has_mortar_and_pestle", has(ModItems.MORTAR_PESTLE))
                .save(recipeOutput, "ascension:shapeless/venomous_meridian_powder");

        ItemStack crackedMeridiansResult = new ItemStack(ModItems.SILVER_NEEDLE.get(), 8);
        crackedMeridiansResult.set(ModDataComponents.NEEDLE_EFFECT.get(), "ascension:needle_cracked_meridians");
        ComponentShapedRecipeBuilder.shaped(RecipeCategory.MISC, crackedMeridiansResult, "PPP", "PSP", "PPP")
                .define('S', ModItems.SILVER_NEEDLE.get())
                .define('P', ModItems.CRACKED_MERIDIANS_POWDER.get())
                .unlockedBy("has_silver_needle", has(ModItems.SILVER_NEEDLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "shaped/cracked_meridians_silver_needle"));

        ItemStack blindedSensesResult = new ItemStack(ModItems.SILVER_NEEDLE.get(), 8);
        blindedSensesResult.set(ModDataComponents.NEEDLE_EFFECT.get(), "ascension:needle_blinded_senses");
        ComponentShapedRecipeBuilder.shaped(RecipeCategory.MISC, blindedSensesResult, "PPP", "PSP", "PPP")
                .define('S', ModItems.SILVER_NEEDLE.get())
                .define('P', ModItems.BLINDED_SENSES_POWDER.get())
                .unlockedBy("has_silver_needle", has(ModItems.SILVER_NEEDLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "shaped/blinded_senses_silver_needle"));

        ItemStack paralyzedBodyResult = new ItemStack(ModItems.SILVER_NEEDLE.get(), 8);
        paralyzedBodyResult.set(ModDataComponents.NEEDLE_EFFECT.get(), "ascension:needle_paralyzed_body");
        ComponentShapedRecipeBuilder.shaped(RecipeCategory.MISC, paralyzedBodyResult, "PPP", "PSP", "PPP")
                .define('S', ModItems.SILVER_NEEDLE.get())
                .define('P', ModItems.PARALYZED_BODY_POWDER.get())
                .unlockedBy("has_silver_needle", has(ModItems.SILVER_NEEDLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "shaped/paralyzed_body_silver_needle"));

        ItemStack venomousMeridiansResult = new ItemStack(ModItems.SILVER_NEEDLE.get(), 8);
        venomousMeridiansResult.set(ModDataComponents.NEEDLE_EFFECT.get(), "ascension:needle_venomous_meridians");
        ComponentShapedRecipeBuilder.shaped(RecipeCategory.MISC, venomousMeridiansResult, "PPP", "PSP", "PPP")
                .define('S', ModItems.SILVER_NEEDLE.get())
                .define('P', ModItems.VENOMOUS_MERIDIAN_POWDER.get())
                .unlockedBy("has_silver_needle", has(ModItems.SILVER_NEEDLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "shaped/venomous_meridians_silver_needle"));

        ItemStack scorchingYangResult = new ItemStack(ModItems.SILVER_NEEDLE.get(), 8);
        scorchingYangResult.set(ModDataComponents.NEEDLE_EFFECT.get(), "ascension:needle_scorching_yang_poison");
        ComponentShapedRecipeBuilder.shaped(RecipeCategory.MISC, scorchingYangResult, "PPP", "PSP", "PPP")
                .define('S', ModItems.SILVER_NEEDLE.get())
                .define('P', ModItems.SCORCHING_YANG_POWDER.get())
                .unlockedBy("has_silver_needle", has(ModItems.SILVER_NEEDLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "shaped/scorching_yang_silver_needle"));

        ItemStack qiDevouringResult = new ItemStack(ModItems.SILVER_NEEDLE.get(), 8);
        qiDevouringResult.set(ModDataComponents.NEEDLE_EFFECT.get(), "ascension:needle_qi_devouring_poison");
        ComponentShapedRecipeBuilder.shaped(RecipeCategory.MISC, qiDevouringResult, "PPP", "PSP", "PPP")
                .define('S', ModItems.SILVER_NEEDLE.get())
                .define('P', ModItems.QI_DEVOURING_POWDER.get())
                .unlockedBy("has_silver_needle", has(ModItems.SILVER_NEEDLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "shaped/qi_devouring_silver_needle"));

        ItemStack corrosiveResult = new ItemStack(ModItems.SILVER_NEEDLE.get(), 8);
        corrosiveResult.set(ModDataComponents.NEEDLE_EFFECT.get(), "ascension:needle_corrosive_poison");
        ComponentShapedRecipeBuilder.shaped(RecipeCategory.MISC, corrosiveResult, "PPP", "PSP", "PPP")
                .define('S', ModItems.SILVER_NEEDLE.get())
                .define('P', ModItems.CORROSIVE_POISON_POWDER.get())
                .unlockedBy("has_silver_needle", has(ModItems.SILVER_NEEDLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "shaped/corrosive_poison_silver_needle"));

        ItemStack frostSilkwormResult = new ItemStack(ModItems.SILVER_NEEDLE.get(), 8);
        frostSilkwormResult.set(ModDataComponents.NEEDLE_EFFECT.get(), "ascension:needle_frost_silkworm_poison");
        ComponentShapedRecipeBuilder.shaped(RecipeCategory.MISC, frostSilkwormResult, "PPP", "PSP", "PPP")
                .define('S', ModItems.SILVER_NEEDLE.get())
                .define('P', ModItems.FROST_SILKWORM_POWDER.get())
                .unlockedBy("has_silver_needle", has(ModItems.SILVER_NEEDLE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "shaped/frost_silkworm_silver_needle"));



        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.SPIRITUAL_MEAL.get(), 1)
                .requires(ModItems.MORTAR_PESTLE)
                .requires(ModItems.SPIRITUAL_STONE, 2)
                .requires(Items.BONE_MEAL)
                .unlockedBy("has_mortar_and_pestle", has(ModItems.MORTAR_PESTLE)).save(recipeOutput, "ascension:shapeless/spiritual_meal");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.MORTAR_PESTLE.get())
                .pattern("   ")
                .pattern("AFA")
                .pattern(" A ")
                .define('F', ModItems.FROST_SILVER_INGOT.get())
                .define('A', ItemTags.STONE_CRAFTING_MATERIALS)
                .unlockedBy("has_frost_silver", has(ModItems.FROST_SILVER_INGOT)).save(recipeOutput, "ascension:shaped/mortar_and_pestle");


        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.CULTIVATORS_SWORD_IRON.get())
                .pattern(" B ")
                .pattern(" B ")
                .pattern(" S ")
                .define('B', ModItems.BLACK_IRON_INGOT.get())
                .define('S', Items.STICK)
                .unlockedBy("has_black_iron", has(ModItems.BLACK_IRON_INGOT)).save(recipeOutput, "ascension:shaped/cultivators_sword_iron_from_black_iron");


        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.TECHNIQUE_BINDER.get())
                .pattern("PPP")
                .pattern("PSP")
                .pattern("PPP")
                .define('S', ModItems.TECHNIQUE_PAGE.get())
                .define('P', Items.BOOK)
                .unlockedBy("has_technique_page", has(ModItems.TECHNIQUE_PAGE)).save(recipeOutput, "ascension:shaped/technique_binder");

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.TECHNIQUE_STAND.get())
                .pattern("DDD")
                .pattern(" S ")
                .pattern("SSS")
                .define('S', Items.SPRUCE_PLANKS)
                .define('D', Items.DEEPSLATE)
                .unlockedBy("has_spruce_wood", has(Items.SPRUCE_PLANKS)).save(recipeOutput, "ascension:shaped/technique_stand");


        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SPIRITUAL_STONE_PICKAXE.get())
                .pattern("SSS")
                .pattern(" T ")
                .pattern(" T ")
                .define('S', ModItems.SPIRITUAL_STONE.get())
                .define('T', Items.STICK)
                .unlockedBy("has_spiritual_stone", has(ModItems.SPIRITUAL_STONE)).save(recipeOutput, "ascension:shaped/spiritual_stone_pickaxe");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SPIRITUAL_STONE_HOE.get())
                .pattern("SS ")
                .pattern(" T ")
                .pattern(" T ")
                .define('S', ModItems.SPIRITUAL_STONE.get())
                .define('T', Items.STICK)
                .unlockedBy("has_spiritual_stone", has(ModItems.SPIRITUAL_STONE)).save(recipeOutput, "ascension:shaped/spiritual_stone_hoe");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SPIRITUAL_STONE_AXE.get())
                .pattern("SS ")
                .pattern("ST ")
                .pattern(" T ")
                .define('S', ModItems.SPIRITUAL_STONE.get())
                .define('T', Items.STICK)
                .unlockedBy("has_spiritual_stone", has(ModItems.SPIRITUAL_STONE)).save(recipeOutput, "ascension:shaped/spiritual_stone_axe");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SPIRITUAL_STONE_SHOVEL.get())
                .pattern(" S ")
                .pattern(" T ")
                .pattern(" T ")
                .define('S', ModItems.SPIRITUAL_STONE.get())
                .define('T', Items.STICK)
                .unlockedBy("has_spiritual_stone", has(ModItems.SPIRITUAL_STONE)).save(recipeOutput, "ascension:shaped/spiritual_stone_shovel");

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SOULBOUND_PACT_TALISMAN.get())
                .pattern("EHE")
                .pattern("WTW")
                .pattern("EHE")
                .define('T', ModItems.TALISMAN_PAPER.get())
                .define('E', Items.ENDER_EYE)
                .define('H', Items.ENDER_PEARL)
                .define('W', ItemTags.WOOL)
                .unlockedBy("has_talisman_paper", has(ModItems.TALISMAN_PAPER)).save(recipeOutput, "ascension:shaped/soul_pact_talisman");

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SOUL_ANCHOR_TALISMAN.get())
                .pattern("HEH")
                .pattern("ETE")
                .pattern("HEH")
                .define('T', ModItems.TALISMAN_PAPER.get())
                .define('E', Items.ENDER_EYE)
                .define('H', Items.HOPPER)
                .unlockedBy("has_talisman_paper", has(ModItems.TALISMAN_PAPER)).save(recipeOutput, "ascension:shaped/soul_anchor_talisman");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.DEATH_RECALL_TALISMAN.get())
                .pattern("TUT")
                .pattern("TET")
                .pattern("TTT")
                .define('T', ModItems.TALISMAN_PAPER.get())
                .define('E', Items.ENDER_EYE)
                .define('U', Items.AMETHYST_SHARD)
                .unlockedBy("has_talisman_paper", has(ModItems.TALISMAN_PAPER)).save(recipeOutput, "ascension:shaped/death_recall_talisman");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SOULSTEAD_RETURN_TALISMAN.get())
                .pattern("DED")
                .pattern("ETE")
                .pattern("DED")
                .define('T', ModItems.TALISMAN_PAPER.get())
                .define('E', Items.ENDER_PEARL)
                .define('D', ModTags.Items.WOOLABLE)
                .unlockedBy("has_talisman_paper", has(ModItems.TALISMAN_PAPER)).save(recipeOutput, "ascension:shaped/soulstead_return_talisman");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.WORLD_AXIS_TALISMAN.get())
                .pattern("DED")
                .pattern("ETE")
                .pattern("DED")
                .define('T', ModItems.TALISMAN_PAPER.get())
                .define('E', Items.ENDER_PEARL)
                .define('D', Items.FEATHER)
                .unlockedBy("has_talisman_paper", has(ModItems.TALISMAN_PAPER)).save(recipeOutput, "ascension:shaped/world_axis_talisman");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.VOID_MARKING_TALISMAN.get())
                .pattern("DED")
                .pattern("ETE")
                .pattern("DED")
                .define('T', ModItems.SPATIAL_RUPTURE_TALISMAN_T3.get())
                .define('E', Items.REDSTONE_TORCH)
                .define('D', Items.FEATHER)
                .unlockedBy("has_talisman_paper", has(ModItems.TALISMAN_PAPER)).save(recipeOutput, "ascension:shaped/void_marking_talisman");

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SPATIAL_RUPTURE_TALISMAN_T1.get())
                .pattern("DED")
                .pattern("ETE")
                .pattern("DED")
                .define('T', ModItems.TALISMAN_PAPER.get())
                .define('D', Items.ENDER_PEARL)
                .define('E', Items.ENDER_EYE)
                .unlockedBy("has_talisman_paper", has(ModItems.TALISMAN_PAPER)).save(recipeOutput, "ascension:shaped/spatial_rupture_talisman_t1");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SPATIAL_RUPTURE_TALISMAN_T2.get())
                .pattern("DED")
                .pattern("ETE")
                .pattern("DED")
                .define('T', ModItems.SPATIAL_RUPTURE_TALISMAN_T1.get())
                .define('D', Items.ENDER_PEARL)
                .define('E', Items.ENDER_EYE)
                .unlockedBy("has_spatial_rupture_talisman_t1", has(ModItems.SPATIAL_RUPTURE_TALISMAN_T1)).save(recipeOutput, "ascension:shaped/spatial_rupture_talisman_t2");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SPATIAL_RUPTURE_TALISMAN_T3.get())
                .pattern("DED")
                .pattern("ETE")
                .pattern("DED")
                .define('T', ModItems.SPATIAL_RUPTURE_TALISMAN_T2.get())
                .define('D', Items.ENDER_PEARL)
                .define('E', Items.ENDER_EYE)
                .unlockedBy("has_spatial_rupture_talisman_t2", has(ModItems.SPATIAL_RUPTURE_TALISMAN_T2)).save(recipeOutput, "ascension:shaped/spatial_rupture_talisman_t3");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.FIRE_GOURD.get())
                .pattern("   ")
                .pattern("MFM")
                .pattern(" M ")
                .define('F', ModItems.HUNDRED_YEAR_FIRE_GINSENG.get())
                .define('M', ModBlocks.RAW_MARBLE.get())
                .unlockedBy("has_marble", has(ModBlocks.RAW_MARBLE)).save(recipeOutput, "ascension:shaped/fire_gourd");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SPATIAL_RING.get())
                .pattern("FIF")
                .pattern("BCB")
                .pattern("FBF")
                .define('I', Items.IRON_INGOT)
                .define('C', Blocks.CHEST)
                .define('F', ModItems.FROST_SILVER_INGOT.get())
                .define('B', ModItems.BLACK_IRON_INGOT.get())
                .unlockedBy("has_frost_silver_ingot", has(ModItems.FROST_SILVER_INGOT)).save(recipeOutput, "ascension:shaped/spatial_ring");




        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.TABLET_OF_DESTRUCTION_HUMAN.get(), 2)
                .pattern("GBG")
                .pattern("BGB")
                .pattern("GBG")
                .define('G', Items.IRON_INGOT)
                .define('B', ModBlocks.RAW_MARBLE.get())
                .unlockedBy("has_gunpowder", has(Items.GUNPOWDER)).save(recipeOutput, "ascension:shaped/tablet_of_destruction_human");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.TABLET_OF_DESTRUCTION_EARTH.get(), 2)
                .pattern("TTT")
                .pattern("THT")
                .pattern("TTT")
                .define('T', Items.GUNPOWDER)
                .define('H', ModItems.TABLET_OF_DESTRUCTION_HUMAN.get())
                .unlockedBy("has_tablet_of_destruction_human", has(ModItems.TABLET_OF_DESTRUCTION_HUMAN)).save(recipeOutput, "ascension:shaped/tablet_of_destruction_earth");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.TABLET_OF_DESTRUCTION_HEAVEN.get(), 2)
                .pattern("TTT")
                .pattern("THT")
                .pattern("TTT")
                .define('T', Items.GUNPOWDER)
                .define('H', ModItems.TABLET_OF_DESTRUCTION_EARTH.get())
                .unlockedBy("has_tablet_of_destruction_eart", has(ModItems.TABLET_OF_DESTRUCTION_EARTH)).save(recipeOutput, "ascension:shaped/tablet_of_destruction_heaven");

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.ENDER_POUCH.get())
                .pattern("LJL")
                .pattern("LEL")
                .pattern("LLL")
                .define('E', Items.ENDER_CHEST)
                .define('L', Items.LEATHER)
                .define('J', ModItems.JADE.get())
                .unlockedBy("has_ender_chest", has(Items.ENDER_CHEST)).save(recipeOutput, "ascension:shaped/ender_pouch");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.REPAIR_SLIP.get())
                .pattern(" SS")
                .pattern("BB ")
                .pattern("BB ")
                .define('S', Items.STRING)
                .define('B', ModItems.BLACK_IRON_NUGGET.get())
                .unlockedBy("has_black_iron_nugget", has(ModItems.REPAIR_SLIP)).save(recipeOutput, "ascension:shaped/repair_slip");





        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.TALISMAN_PAPER.get(), 6)
                .pattern("   ")
                .pattern("SPS")
                .pattern("   ")
                .define('S', Items.SUGAR_CANE)
                .define('P', ModItems.JADE_BAMBOO_OF_SERENITY.get())
                .unlockedBy("has_jade_bamboo_of_serenity", has(ModItems.JADE_BAMBOO_OF_SERENITY)).save(recipeOutput, "ascension:shaped/talisman_paper");

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.ANCESTOR_JOURNAL.get())
                .pattern("TTT")
                .pattern("TJT")
                .pattern("TTT")
                .define('T', ModItems.TALISMAN_PAPER.get())
                .define('J', ModItems.JADE.get())
                .unlockedBy("has_talisman_paper", has(ModItems.TALISMAN_PAPER)).save(recipeOutput, "ascension:shaped/ancestor_journal");


        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.WOODEN_BLADE.get())
                .pattern("JJ ")
                .pattern(" J ")
                .pattern(" S ")
                .define('J', ItemTags.PLANKS)
                .define('S', Items.STICK)
                .unlockedBy("has_wood", has(ItemTags.PLANKS)).save(recipeOutput, "ascension:shaped/wooden_blade");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.STONE_BLADE.get())
                .pattern("JJ ")
                .pattern(" J ")
                .pattern(" S ")
                .define('J', Items.COBBLESTONE)
                .define('S', Items.STICK)
                .unlockedBy("has_cobble", has(Items.COBBLESTONE)).save(recipeOutput, "ascension:shaped/cobblestone_blade");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.IRON_BLADE.get())
                .pattern("JJ ")
                .pattern(" J ")
                .pattern(" S ")
                .define('J', Items.IRON_INGOT)
                .define('S', Items.STICK)
                .unlockedBy("has_iron", has(Items.IRON_INGOT)).save(recipeOutput, "ascension:shaped/iron_blade");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.GOLD_BLADE.get())
                .pattern("JJ ")
                .pattern(" J ")
                .pattern(" S ")
                .define('J', Items.GOLD_INGOT)
                .define('S', Items.STICK)
                .unlockedBy("has_gold", has(Items.GOLD_INGOT)).save(recipeOutput, "ascension:shaped/gold_blade");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.DIAMOND_BLADE.get())
                .pattern("JJ ")
                .pattern(" J ")
                .pattern(" S ")
                .define('J', Items.DIAMOND)
                .define('S', Items.STICK)
                .unlockedBy("has_diamond", has(Items.DIAMOND)).save(recipeOutput, "ascension:shaped/diamond_blade");
        SmithingTransformRecipeBuilder.smithing(Ingredient.of(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE), Ingredient.of(ModItems.DIAMOND_BLADE.get()), Ingredient.of(Items.NETHERITE_INGOT), RecipeCategory.MISC, ModItems.NETHERITE_BLADE.get()).unlocks(getHasName(Items.NETHERITE_INGOT), has(Items.NETHERITE_INGOT)).save(recipeOutput, "ascension:smithing/netherite_blade");


        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.WOODEN_SPEAR.get())
                .pattern("  J")
                .pattern(" J ")
                .pattern("S  ")
                .define('J', ItemTags.PLANKS)
                .define('S', Items.STICK)
                .unlockedBy("has_wood", has(ItemTags.PLANKS)).save(recipeOutput, "ascension:shaped/wooden_spear");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.STONE_SPEAR.get())
                .pattern("  J")
                .pattern(" J ")
                .pattern("S  ")
                .define('J', Items.COBBLESTONE)
                .define('S', Items.STICK)
                .unlockedBy("has_cobble", has(Items.COBBLESTONE)).save(recipeOutput, "ascension:shaped/stone_spear");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.IRON_SPEAR.get())
                .pattern("  J")
                .pattern(" J ")
                .pattern("S  ")
                .define('J', Items.IRON_INGOT)
                .define('S', Items.STICK)
                .unlockedBy("has_iron", has(Items.IRON_INGOT)).save(recipeOutput, "ascension:shaped/iron_spear");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.GOLD_SPEAR.get())
                .pattern("  J")
                .pattern(" J ")
                .pattern("S  ")
                .define('J', Items.GOLD_INGOT)
                .define('S', Items.STICK)
                .unlockedBy("has_gold", has(Items.GOLD_INGOT)).save(recipeOutput, "ascension:shaped/gold_spear");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.DIAMOND_SPEAR.get())
                .pattern("  J")
                .pattern(" J ")
                .pattern("S  ")
                .define('J', Items.DIAMOND)
                .define('S', Items.STICK)
                .unlockedBy("has_diamond", has(Items.DIAMOND)).save(recipeOutput, "ascension:shaped/diamond_spear");
        SmithingTransformRecipeBuilder.smithing(Ingredient.of(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE), Ingredient.of(ModItems.DIAMOND_SPEAR.get()), Ingredient.of(Items.NETHERITE_INGOT), RecipeCategory.MISC, ModItems.NETHERITE_SPEAR.get()).unlocks(getHasName(Items.NETHERITE_INGOT), has(Items.NETHERITE_INGOT)).save(recipeOutput, "ascension:smithing/netherite_spear");


        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.WOODEN_GAUNTLET.get())
                .pattern("J J")
                .pattern("JJJ")
                .define('J', ItemTags.PLANKS)
                .unlockedBy("has_wood", has(ItemTags.PLANKS)).save(recipeOutput, "ascension:shaped/wooden_gauntlet");

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.STONE_GAUNTLET.get())
                .pattern("J J")
                .pattern("JJJ")
                .define('J', Items.COBBLESTONE)
                .unlockedBy("has_cobble", has(Items.COBBLESTONE)).save(recipeOutput, "ascension:shaped/stone_gauntlet");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.IRON_GAUNTLET.get())
                .pattern("J J")
                .pattern("JJJ")
                .define('J', Items.IRON_INGOT)
                .unlockedBy("has_iron", has(Items.IRON_INGOT)).save(recipeOutput, "ascension:shaped/iron_gauntlet");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.GOLD_GAUNTLET.get())
                .pattern("J J")
                .pattern("JJJ")
                .define('J', Items.GOLD_INGOT)
                .unlockedBy("has_gold", has(Items.GOLD_INGOT)).save(recipeOutput, "ascension:shaped/gold_gauntlet");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.DIAMOND_GAUNTLET.get())
                .pattern("J J")
                .pattern("JJJ")
                .define('J', Items.DIAMOND)
                .unlockedBy("has_diamond", has(Items.DIAMOND)).save(recipeOutput, "ascension:shaped/diamond_gauntlet");
        SmithingTransformRecipeBuilder.smithing(
                Ingredient.of(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE),
                Ingredient.of(ModItems.DIAMOND_GAUNTLET.get()),
                Ingredient.of(Items.NETHERITE_INGOT),
                RecipeCategory.MISC,
                ModItems.NETHERITE_GAUNTLET.get()
        ).unlocks(getHasName(Items.NETHERITE_INGOT), has(Items.NETHERITE_INGOT)).save(recipeOutput, "ascension:smithing/netherite_gauntlet");


        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.SPIRITUAL_STONE_BLOCK.get())
                .pattern("JJJ")
                .pattern("JJJ")
                .pattern("JJJ")
                .define('J', ModItems.SPIRITUAL_STONE.get())
                .unlockedBy("has_spiritual_stone", has(ModItems.SPIRITUAL_STONE)).save(recipeOutput, "ascension:shaped/ssb_from_spiritual_stone");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.JADE_BLOCK.get())
                .pattern("JJJ")
                .pattern("JJJ")
                .pattern("JJJ")
                .define('J', ModItems.JADE.get())
                .unlockedBy("has_jade_ingot1", has(ModItems.JADE)).save(recipeOutput, "ascension:shaped/jade_block_from_jade");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.JADE.get())
                .pattern("JJJ")
                .pattern("JJJ")
                .pattern("JJJ")
                .define('J', ModItems.JADE_NUGGET.get())
                .unlockedBy("has_jade_ingot", has(ModItems.JADE)).save(recipeOutput, "ascension:shaped/jade_ingot_from_nugget");

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.BLACK_IRON_BLOCK.get())
                .pattern("BBB")
                .pattern("BBB")
                .pattern("BBB")
                .define('B', ModItems.BLACK_IRON_INGOT.get())
                .unlockedBy("has_black_iron_ingot", has(ModItems.BLACK_IRON_INGOT)).save(recipeOutput, "ascension:shaped/black_iron_block_from_ingot");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.BLACK_IRON_INGOT.get())
                .pattern("BBB")
                .pattern("BBB")
                .pattern("BBB")
                .define('B', ModItems.BLACK_IRON_NUGGET.get())
                .unlockedBy("has_black_iron_nugget", has(ModItems.BLACK_IRON_NUGGET)).save(recipeOutput, "ascension:shaped/black_iron_ingot_from_nugget");

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SILVER_NEEDLE.get(), 6)
                .pattern("  N")
                .pattern(" B ")
                .pattern("N  ")
                .define('B', ModItems.FROST_SILVER_INGOT.get())
                .define('N', ModItems.FROST_SILVER_NUGGET.get())
                .unlockedBy("has_frost_silver_ingot", has(ModItems.FROST_SILVER_INGOT)).save(recipeOutput, "ascension:shaped/silver_needle");

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.FROST_SILVER_BLOCK.get())
                .pattern("BBB")
                .pattern("BBB")
                .pattern("BBB")
                .define('B', ModItems.FROST_SILVER_INGOT.get())
                .unlockedBy("has_frost_silver_ingot", has(ModItems.FROST_SILVER_INGOT)).save(recipeOutput, "ascension:shaped/frost_silver_block_from_ingot");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.FROST_SILVER_INGOT.get())
                .pattern("BBB")
                .pattern("BBB")
                .pattern("BBB")
                .define('B', ModItems.FROST_SILVER_NUGGET.get())
                .unlockedBy("has_frost_silver_nugget", has(ModItems.FROST_SILVER_NUGGET)).save(recipeOutput, "ascension:shaped/frost_silver_ingot_from_nugget");

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.PILL_CAULDRON_HUMAN_LOW.get())
                .pattern("B B")
                .pattern("B B")
                .pattern("BBB")
                .define('B', ModItems.BLACK_IRON_INGOT.get())
                .unlockedBy("has_black_iron_ingot", has(ModItems.BLACK_IRON_INGOT)).save(recipeOutput, "ascension:shaped/pill_cauldron_from_black_iron");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.FLAME_STAND_BLOCK.get())
                .pattern("B B")
                .pattern("BFB")
                .pattern("BBB")
                .define('B', ModItems.BLACK_IRON_INGOT.get())
                .define('F', ModTags.Items.FLAMES)
                .unlockedBy("has_black_iron_ingot", has(ModItems.BLACK_IRON_INGOT)).save(recipeOutput, "ascension:shaped/flamestand_from_black_iron");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.CAULDRON_PEDESTAL_BLOCK.get())
                .pattern("BBB")
                .pattern("B B")
                .pattern("B B")
                .define('B', ModItems.BLACK_IRON_INGOT.get())
                .unlockedBy("has_black_iron_ingot", has(ModItems.BLACK_IRON_INGOT)).save(recipeOutput, "ascension:shaped/pedestal_from_black_iron");
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.FAN.get())
                .pattern(" WW")
                .pattern(" WW")
                .pattern("S  ")
                .define('S', Items.STICK)
                .define('W', ItemTags.WOOL)
                .unlockedBy("has_black_iron_ingot", has(ModItems.BLACK_IRON_INGOT)).save(recipeOutput, "ascension:shaped/fan");

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.HERB_POUCH.get())
                .pattern("BCB")
                .pattern("ADA")
                .pattern(" A ")
                .define('A', Items.LEATHER)
                .define('B', Items.STRING)
                .define('C', ModItems.SPIRITUAL_STONE)
                .define('D', ModTags.Items.HERBS)
                .unlockedBy("has_spiritual_stone", has(ModItems.SPIRITUAL_STONE)).save(recipeOutput, "ascension:shaped/herb_pouch");

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocks.GOLDEN_PALM_PLANKS.get(), 4)
                .requires(ModBlocks.GOLDEN_PALM_LOG)
                .unlockedBy("has_golden_palm_log", has(ModBlocks.GOLDEN_PALM_LOG)).save(recipeOutput, "ascension:shapeless/palm_planks");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocks.GOLDEN_PALM_PLANKS.get(), 4)
                .requires(ModBlocks.GOLDEN_PALM_WOOD)
                .unlockedBy("has_golden_palm_wood", has(ModBlocks.GOLDEN_PALM_WOOD)).save(recipeOutput, "ascension:shapeless/palm_planks_from_wood");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocks.GOLDEN_PALM_PLANKS.get(), 4)
                .requires(ModBlocks.STRIPPED_GOLDEN_PALM_LOG)
                .unlockedBy("has_golden_palm_log_stripped", has(ModBlocks.STRIPPED_GOLDEN_PALM_LOG)).save(recipeOutput, "ascension:shapeless/palm_planks_stripped");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocks.GOLDEN_PALM_PLANKS.get(), 4)
                .requires(ModBlocks.STRIPPED_GOLDEN_PALM_WOOD)
                .unlockedBy("has_golden_palm_wood_stripped", has(ModBlocks.STRIPPED_GOLDEN_PALM_WOOD)).save(recipeOutput, "ascension:shapeless/palm_planks_from_wood_stripped");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocks.IRONWOOD_PLANKS.get(), 4)
                .requires(ModBlocks.IRONWOOD_LOG)
                .unlockedBy("has_ironwood_log", has(ModBlocks.IRONWOOD_LOG)).save(recipeOutput, "ascension:shapeless/ironwood_planks");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocks.IRONWOOD_PLANKS.get(), 4)
                .requires(ModBlocks.IRONWOOD_WOOD)
                .unlockedBy("has_ironwood_wood", has(ModBlocks.IRONWOOD_WOOD)).save(recipeOutput, "ascension:shapeless/ironwood_from_wood");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocks.IRONWOOD_PLANKS.get(), 4)
                .requires(ModBlocks.STRIPPED_IRONWOOD_LOG)
                .unlockedBy("has_ironwood_stripped", has(ModBlocks.STRIPPED_IRONWOOD_LOG)).save(recipeOutput, "ascension:shapeless/ironwood_planks_stripped");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocks.IRONWOOD_PLANKS.get(), 4)
                .requires(ModBlocks.STRIPPED_IRONWOOD_WOOD)
                .unlockedBy("has_ironwood_stripped", has(ModBlocks.STRIPPED_IRONWOOD_WOOD)).save(recipeOutput, "ascension:shapeless/ironwood_planks_from_wood_stripped");

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.SPIRITUAL_STONE.get(), 9)
                .requires(ModBlocks.SPIRITUAL_STONE_BLOCK)
                .unlockedBy("has_ssb", has(ModBlocks.SPIRITUAL_STONE_BLOCK)).save(recipeOutput, "ascension:shapeless/ss_from_ssb");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.JADE.get(), 9)
                .requires(ModBlocks.JADE_BLOCK)
                .unlockedBy("has_block_of_jade", has(ModBlocks.JADE_BLOCK)).save(recipeOutput, "ascension:shapeless/jade_from_jade_block");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.JADE_NUGGET.get(), 9)
                .requires(ModItems.JADE)
                .unlockedBy("has_jade", has(ModItems.JADE)).save(recipeOutput, "ascension:shapeless/jade_nugget_from_jade");

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.BLACK_IRON_INGOT.get(), 9)
                .requires(ModBlocks.BLACK_IRON_BLOCK)
                .unlockedBy("has_block_of_black_iron", has(ModBlocks.BLACK_IRON_BLOCK)).save(recipeOutput, "ascension:shapeless/black_iron_ingot_from_block");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.BLACK_IRON_NUGGET.get(), 9)
                .requires(ModItems.BLACK_IRON_INGOT)
                .unlockedBy("has_black_iron_ingot", has(ModItems.BLACK_IRON_INGOT)).save(recipeOutput, "ascension:shapeless/black_iron_nugget_from_ingot");

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.FROST_SILVER_INGOT.get(), 9)
                .requires(ModBlocks.FROST_SILVER_BLOCK)
                .unlockedBy("has_block_of_frost_silver", has(ModBlocks.FROST_SILVER_BLOCK)).save(recipeOutput, "ascension:shapeless/frost_silver_ingot_from_block");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.FROST_SILVER_NUGGET.get(), 9)
                .requires(ModItems.FROST_SILVER_INGOT)
                .unlockedBy("has_frost_silver_ingot", has(ModItems.FROST_SILVER_INGOT)).save(recipeOutput, "ascension:shapeless/frost_silver_nugget_from_ingot");


        /** Marble Recipes */
        //Polished
        polished(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.IRONWOOD_WOOD.get(), ModBlocks.IRONWOOD_LOG.get());
        polished(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.STRIPPED_IRONWOOD_WOOD.get(), ModBlocks.STRIPPED_IRONWOOD_LOG.get());
        polished(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.GOLDEN_PALM_WOOD.get(), ModBlocks.GOLDEN_PALM_LOG.get());
        polished(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.STRIPPED_GOLDEN_PALM_WOOD.get(), ModBlocks.STRIPPED_GOLDEN_PALM_LOG.get());

        polished(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.POLISHED_MARBLE.get(), ModBlocks.RAW_MARBLE.get());
        polished(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.POLISHED_LIGHT_GRAY_MARBLE.get(), ModBlocks.LIGHT_GRAY_MARBLE.get());
        polished(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.GRAY_POLISHED_MARBLE.get(), ModBlocks.GRAY_MARBLE.get());
        polished(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.POLISHED_BURNED_MARBLE.get(), ModBlocks.CHARRED_MARBLE.get());
        polished(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.BROWN_POLISHED_MARBLE.get(), ModBlocks.BROWN_MARBLE.get());
        polished(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.RED_POLISHED_MARBLE.get(), ModBlocks.RED_MARBLE.get());
        polished(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.ORANGE_POLISHED_MARBLE.get(), ModBlocks.ORANGE_MARBLE.get());
        polished(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.YELLOW_POLISHED_MARBLE.get(), ModBlocks.YELLOW_MARBLE.get());
        polished(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.LIME_POLISHED_MARBLE.get(), ModBlocks.LIME_MARBLE.get());
        polished(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.GREEN_POLISHED_MARBLE.get(), ModBlocks.GREEN_MARBLE.get());
        polished(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.CYAN_POLISHED_MARBLE.get(), ModBlocks.CYAN_MARBLE.get());
        polished(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.LIGHT_BLUE_POLISHED_MARBLE.get(), ModBlocks.LIGHT_BLUE_MARBLE.get());
        polished(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.BLUE_POLISHED_MARBLE.get(), ModBlocks.BLUE_MARBLE.get());
        polished(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.PURPLE_POLISHED_MARBLE.get(), ModBlocks.PURPLE_MARBLE.get());
        polished(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.MAGENTA_POLISHED_MARBLE.get(), ModBlocks.MAGENTA_MARBLE.get());
        polished(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.PINK_POLISHED_MARBLE.get(), ModBlocks.PINK_MARBLE.get());
        //Bricks
        polished(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.MARBLE_BRICKS.get(), ModBlocks.POLISHED_MARBLE.get());
        polished(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.LIGHT_GRAY_MARBLE_BRICKS.get(), ModBlocks.POLISHED_LIGHT_GRAY_MARBLE.get());
        polished(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.GRAY_MARBLE_BRICKS.get(), ModBlocks.GRAY_POLISHED_MARBLE.get());
        polished(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.CHARRED_MARBLE_BRICKS.get(), ModBlocks.POLISHED_BURNED_MARBLE.get());
        polished(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.BROWN_MARBLE_BRICKS.get(), ModBlocks.BROWN_POLISHED_MARBLE.get());
        polished(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.RED_MARBLE_BRICKS.get(), ModBlocks.RED_POLISHED_MARBLE.get());
        polished(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.ORANGE_MARBLE_BRICKS.get(), ModBlocks.ORANGE_POLISHED_MARBLE.get());
        polished(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.YELLOW_MARBLE_BRICKS.get(), ModBlocks.YELLOW_POLISHED_MARBLE.get());
        polished(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.LIME_MARBLE_BRICKS.get(), ModBlocks.LIME_POLISHED_MARBLE.get());
        polished(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.GREEN_MARBLE_BRICKS.get(), ModBlocks.GREEN_POLISHED_MARBLE.get());
        polished(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.CYAN_MARBLE_BRICKS.get(), ModBlocks.CYAN_POLISHED_MARBLE.get());
        polished(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.LIGHT_BLUE_MARBLE_BRICKS.get(), ModBlocks.LIGHT_BLUE_POLISHED_MARBLE.get());
        polished(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.BLUE_MARBLE_BRICKS.get(), ModBlocks.BLUE_POLISHED_MARBLE.get());
        polished(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.PURPLE_MARBLE_BRICKS.get(), ModBlocks.PURPLE_POLISHED_MARBLE.get());
        polished(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.MAGENTA_MARBLE_BRICKS.get(), ModBlocks.MAGENTA_POLISHED_MARBLE.get());
        polished(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.PINK_MARBLE_BRICKS.get(), ModBlocks.PINK_POLISHED_MARBLE.get());
        //Tiles
        polished(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.MARBLE_TILES.get(), ModBlocks.MARBLE_BRICKS.get());
        polished(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.LIGHT_GRAY_MARBLE_TILES.get(), ModBlocks.LIGHT_GRAY_MARBLE_BRICKS.get());
        polished(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.GRAY_MARBLE_TILES.get(), ModBlocks.GRAY_MARBLE_BRICKS.get());
        polished(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.CHARRED_MARBLE_TILES.get(), ModBlocks.CHARRED_MARBLE_BRICKS.get());
        polished(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.BROWN_MARBLE_TILES.get(), ModBlocks.BROWN_MARBLE_BRICKS.get());
        polished(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.RED_MARBLE_TILES.get(), ModBlocks.RED_MARBLE_BRICKS.get());
        polished(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.ORANGE_MARBLE_TILES.get(), ModBlocks.ORANGE_MARBLE_BRICKS.get());
        polished(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.YELLOW_MARBLE_TILES.get(), ModBlocks.YELLOW_MARBLE_BRICKS.get());
        polished(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.LIME_MARBLE_TILES.get(), ModBlocks.LIME_MARBLE_BRICKS.get());
        polished(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.GREEN_MARBLE_TILES.get(), ModBlocks.GREEN_MARBLE_BRICKS.get());
        polished(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.CYAN_MARBLE_TILES.get(), ModBlocks.CYAN_MARBLE_BRICKS.get());
        polished(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.LIGHT_BLUE_MARBLE_TILES.get(), ModBlocks.LIGHT_BLUE_MARBLE_BRICKS.get());
        polished(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.BLUE_MARBLE_TILES.get(), ModBlocks.BLUE_MARBLE_BRICKS.get());
        polished(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.PURPLE_MARBLE_TILES.get(), ModBlocks.PURPLE_MARBLE_BRICKS.get());
        polished(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.MAGENTA_MARBLE_TILES.get(), ModBlocks.MAGENTA_MARBLE_BRICKS.get());
        polished(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.PINK_MARBLE_TILES.get(), ModBlocks.PINK_MARBLE_BRICKS.get());


// Define color mappings
        Map<DyeColor, Supplier<Block>> COLORED_MARBLES = Map.ofEntries(
                Map.entry(DyeColor.LIGHT_GRAY, ModBlocks.LIGHT_GRAY_MARBLE),
                Map.entry(DyeColor.GRAY, ModBlocks.GRAY_MARBLE),
                Map.entry(DyeColor.BLACK, ModBlocks.CHARRED_MARBLE), // Using charred for black
                Map.entry(DyeColor.BROWN, ModBlocks.BROWN_MARBLE),
                Map.entry(DyeColor.RED, ModBlocks.RED_MARBLE),
                Map.entry(DyeColor.ORANGE, ModBlocks.ORANGE_MARBLE),
                Map.entry(DyeColor.YELLOW, ModBlocks.YELLOW_MARBLE),
                Map.entry(DyeColor.LIME, ModBlocks.LIME_MARBLE),
                Map.entry(DyeColor.GREEN, ModBlocks.GREEN_MARBLE),
                Map.entry(DyeColor.CYAN, ModBlocks.CYAN_MARBLE),
                Map.entry(DyeColor.LIGHT_BLUE, ModBlocks.LIGHT_BLUE_MARBLE),
                Map.entry(DyeColor.BLUE, ModBlocks.BLUE_MARBLE),
                Map.entry(DyeColor.PURPLE, ModBlocks.PURPLE_MARBLE),
                Map.entry(DyeColor.MAGENTA, ModBlocks.MAGENTA_MARBLE),
                Map.entry(DyeColor.PINK, ModBlocks.PINK_MARBLE)
        );

// Generate recipes for all colors
        COLORED_MARBLES.forEach((dyeColor, coloredMarble) -> {
            ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, coloredMarble.get(), 8)
                    .pattern("MMM")
                    .pattern("MDM")
                    .pattern("MMM")
                    .define('D', DyeItem.byColor(dyeColor))
                    .define('M', ModBlocks.RAW_MARBLE.get())
                    .unlockedBy("has_marble", has(ModBlocks.RAW_MARBLE.get()))
                    .save(recipeOutput, "ascension:marble_coloring/" + dyeColor.getName());
        });


        oreSmelting(recipeOutput, ModBlocks.JADE_ORE.asItem(), RecipeCategory.MISC, ModItems.JADE.get(), 0.25f, 200, "jade");
        oreBlasting(recipeOutput, ModBlocks.JADE_ORE.asItem(), RecipeCategory.MISC, ModItems.JADE.get(), 0.30f, 100, "jade");
        oreSmelting(recipeOutput, ModItems.RAW_BLACK_IRON.get(), RecipeCategory.MISC, ModItems.BLACK_IRON_INGOT.get(), 0.25f, 200, "black_iron_ingot");
        oreBlasting(recipeOutput, ModItems.RAW_BLACK_IRON.get(), RecipeCategory.MISC, ModItems.BLACK_IRON_INGOT.get(), 0.30f, 100, "black_iron_ingot");

        oreSmelting(recipeOutput, ModItems.RAW_FROST_SILVER.get(), RecipeCategory.MISC, ModItems.FROST_SILVER_INGOT.get(), 0.25f, 200, "frost_silver_ingot");
        oreBlasting(recipeOutput, ModItems.RAW_FROST_SILVER.get(), RecipeCategory.MISC, ModItems.FROST_SILVER_INGOT.get(), 0.30f, 100, "frost_silver_ingot");


        //Stairs
        stairBuilder(ModBlocks.GOLDEN_PALM_STAIRS.get(), Ingredient.of(ModBlocks.GOLDEN_PALM_PLANKS)).group("golden_palm_planks")
                .unlockedBy("has_golden_palm_planks", has(ModBlocks.GOLDEN_PALM_PLANKS)).save(recipeOutput);
        stairBuilder(ModBlocks.IRONWOOD_STAIRS.get(), Ingredient.of(ModBlocks.IRONWOOD_PLANKS)).group("ironwood_planks")
                .unlockedBy("has_ironwood_planks", has(ModBlocks.IRONWOOD_PLANKS)).save(recipeOutput);

        stairBuilder(ModBlocks.MARBLE_BRICK_STAIRS.get(), Ingredient.of(ModBlocks.MARBLE_BRICKS)).group("marble_bricks")
                .unlockedBy("has_marble_bricks", has(ModBlocks.MARBLE_BRICKS)).save(recipeOutput);
        stairBuilder(ModBlocks.MARBLE_TILE_STAIRS.get(), Ingredient.of(ModBlocks.MARBLE_TILES)).group("marble_tiles")
                .unlockedBy("has_marble_tiles", has(ModBlocks.MARBLE_TILES)).save(recipeOutput);
        stairBuilder(ModBlocks.LIGHT_GRAY_MARBLE_BRICK_STAIRS.get(), Ingredient.of(ModBlocks.LIGHT_GRAY_MARBLE_BRICKS)).group("light_gray_marble_bricks")
                .unlockedBy("has_marble_bricks", has(ModBlocks.LIGHT_GRAY_MARBLE_BRICKS)).save(recipeOutput);
        stairBuilder(ModBlocks.LIGHT_GRAY_MARBLE_TILE_STAIRS.get(), Ingredient.of(ModBlocks.LIGHT_GRAY_MARBLE_TILES)).group("light_gray_marble_tiles")
                .unlockedBy("has_marble_tiles", has(ModBlocks.LIGHT_GRAY_MARBLE_TILES)).save(recipeOutput);
        stairBuilder(ModBlocks.GRAY_MARBLE_BRICK_STAIRS.get(), Ingredient.of(ModBlocks.GRAY_MARBLE_BRICKS)).group("gray_marble_bricks")
                .unlockedBy("has_marble_bricks", has(ModBlocks.GRAY_MARBLE_BRICKS)).save(recipeOutput);
        stairBuilder(ModBlocks.GRAY_MARBLE_TILE_STAIRS.get(), Ingredient.of(ModBlocks.GRAY_MARBLE_TILES)).group("gray_marble_tiles")
                .unlockedBy("has_marble_tiles", has(ModBlocks.GRAY_MARBLE_TILES)).save(recipeOutput);
        stairBuilder(ModBlocks.CHARRED_MARBLE_BRICK_STAIRS.get(), Ingredient.of(ModBlocks.CHARRED_MARBLE_BRICKS)).group("burned_marble_bricks")
                .unlockedBy("has_marble_bricks", has(ModBlocks.CHARRED_MARBLE_BRICKS)).save(recipeOutput);
        stairBuilder(ModBlocks.CHARRED_MARBLE_TILE_STAIRS.get(), Ingredient.of(ModBlocks.CHARRED_MARBLE_TILES)).group("burned_marble_tiles")
                .unlockedBy("has_marble_tiles", has(ModBlocks.CHARRED_MARBLE_TILES)).save(recipeOutput);
        stairBuilder(ModBlocks.BROWN_MARBLE_BRICK_STAIRS.get(), Ingredient.of(ModBlocks.BROWN_MARBLE_BRICKS)).group("brown_marble_bricks")
                .unlockedBy("has_marble_bricks", has(ModBlocks.BROWN_MARBLE_BRICKS)).save(recipeOutput);
        stairBuilder(ModBlocks.BROWN_MARBLE_TILE_STAIRS.get(), Ingredient.of(ModBlocks.BROWN_MARBLE_TILES)).group("brown_marble_tiles")
                .unlockedBy("has_marble_tiles", has(ModBlocks.BROWN_MARBLE_TILES)).save(recipeOutput);
        stairBuilder(ModBlocks.RED_MARBLE_BRICK_STAIRS.get(), Ingredient.of(ModBlocks.RED_MARBLE_BRICKS)).group("red_marble_bricks")
                .unlockedBy("has_marble_bricks", has(ModBlocks.RED_MARBLE_BRICKS)).save(recipeOutput);
        stairBuilder(ModBlocks.RED_MARBLE_TILE_STAIRS.get(), Ingredient.of(ModBlocks.RED_MARBLE_TILES)).group("red_marble_tiles")
                .unlockedBy("has_marble_tiles", has(ModBlocks.RED_MARBLE_TILES)).save(recipeOutput);
        stairBuilder(ModBlocks.ORANGE_MARBLE_BRICK_STAIRS.get(), Ingredient.of(ModBlocks.ORANGE_MARBLE_BRICKS)).group("orange_marble_bricks")
                .unlockedBy("has_marble_bricks", has(ModBlocks.ORANGE_MARBLE_BRICKS)).save(recipeOutput);
        stairBuilder(ModBlocks.ORANGE_MARBLE_TILE_STAIRS.get(), Ingredient.of(ModBlocks.ORANGE_MARBLE_TILES)).group("orange_marble_tiles")
                .unlockedBy("has_marble_tiles", has(ModBlocks.ORANGE_MARBLE_TILES)).save(recipeOutput);
        stairBuilder(ModBlocks.YELLOW_MARBLE_BRICK_STAIRS.get(), Ingredient.of(ModBlocks.YELLOW_MARBLE_BRICKS)).group("yellow_marble_bricks")
                .unlockedBy("has_marble_bricks", has(ModBlocks.YELLOW_MARBLE_BRICKS)).save(recipeOutput);
        stairBuilder(ModBlocks.YELLOW_MARBLE_TILE_STAIRS.get(), Ingredient.of(ModBlocks.YELLOW_MARBLE_TILES)).group("yellow_marble_tiles")
                .unlockedBy("has_marble_tiles", has(ModBlocks.YELLOW_MARBLE_TILES)).save(recipeOutput);
        stairBuilder(ModBlocks.LIME_MARBLE_BRICK_STAIRS.get(), Ingredient.of(ModBlocks.LIME_MARBLE_BRICKS)).group("lime_marble_bricks")
                .unlockedBy("has_marble_bricks", has(ModBlocks.LIME_MARBLE_BRICKS)).save(recipeOutput);
        stairBuilder(ModBlocks.LIME_MARBLE_TILE_STAIRS.get(), Ingredient.of(ModBlocks.LIME_MARBLE_TILES)).group("lime_marble_tiles")
                .unlockedBy("has_marble_tiles", has(ModBlocks.LIME_MARBLE_TILES)).save(recipeOutput);
        stairBuilder(ModBlocks.GREEN_MARBLE_BRICK_STAIRS.get(), Ingredient.of(ModBlocks.GREEN_MARBLE_BRICKS)).group("green_marble_bricks")
                .unlockedBy("has_marble_bricks", has(ModBlocks.GREEN_MARBLE_BRICKS)).save(recipeOutput);
        stairBuilder(ModBlocks.GREEN_MARBLE_TILE_STAIRS.get(), Ingredient.of(ModBlocks.GREEN_MARBLE_TILES)).group("green_marble_tiles")
                .unlockedBy("has_marble_tiles", has(ModBlocks.GREEN_MARBLE_TILES)).save(recipeOutput);
        stairBuilder(ModBlocks.CYAN_MARBLE_BRICK_STAIRS.get(), Ingredient.of(ModBlocks.CYAN_MARBLE_BRICKS)).group("cyan_marble_bricks")
                .unlockedBy("has_marble_bricks", has(ModBlocks.CYAN_MARBLE_BRICKS)).save(recipeOutput);
        stairBuilder(ModBlocks.CYAN_MARBLE_TILE_STAIRS.get(), Ingredient.of(ModBlocks.CYAN_MARBLE_TILES)).group("cyan_marble_tiles")
                .unlockedBy("has_marble_tiles", has(ModBlocks.CYAN_MARBLE_TILES)).save(recipeOutput);
        stairBuilder(ModBlocks.LIGHT_BLUE_MARBLE_BRICK_STAIRS.get(), Ingredient.of(ModBlocks.LIGHT_BLUE_MARBLE_BRICKS)).group("light_blue_marble_bricks")
                .unlockedBy("has_marble_bricks", has(ModBlocks.LIGHT_BLUE_MARBLE_BRICKS)).save(recipeOutput);
        stairBuilder(ModBlocks.LIGHT_BLUE_MARBLE_TILE_STAIRS.get(), Ingredient.of(ModBlocks.LIGHT_BLUE_MARBLE_TILES)).group("light_blue_marble_tiles")
                .unlockedBy("has_marble_tiles", has(ModBlocks.LIGHT_BLUE_MARBLE_TILES)).save(recipeOutput);
        stairBuilder(ModBlocks.BLUE_MARBLE_BRICK_STAIRS.get(), Ingredient.of(ModBlocks.BLUE_MARBLE_BRICKS)).group("blue_marble_bricks")
                .unlockedBy("has_marble_bricks", has(ModBlocks.BLUE_MARBLE_BRICKS)).save(recipeOutput);
        stairBuilder(ModBlocks.BLUE_MARBLE_TILE_STAIRS.get(), Ingredient.of(ModBlocks.BLUE_MARBLE_TILES)).group("blue_marble_tiles")
                .unlockedBy("has_marble_tiles", has(ModBlocks.BLUE_MARBLE_TILES)).save(recipeOutput);
        stairBuilder(ModBlocks.PURPLE_MARBLE_BRICK_STAIRS.get(), Ingredient.of(ModBlocks.PURPLE_MARBLE_BRICKS)).group("purple_marble_bricks")
                .unlockedBy("has_marble_bricks", has(ModBlocks.PURPLE_MARBLE_BRICKS)).save(recipeOutput);
        stairBuilder(ModBlocks.PURPLE_MARBLE_TILE_STAIRS.get(), Ingredient.of(ModBlocks.PURPLE_MARBLE_TILES)).group("purple_marble_tiles")
                .unlockedBy("has_marble_tiles", has(ModBlocks.PURPLE_MARBLE_TILES)).save(recipeOutput);
        stairBuilder(ModBlocks.MAGENTA_MARBLE_BRICK_STAIRS.get(), Ingredient.of(ModBlocks.MAGENTA_MARBLE_BRICKS)).group("magenta_marble_bricks")
                .unlockedBy("has_marble_bricks", has(ModBlocks.MAGENTA_MARBLE_BRICKS)).save(recipeOutput);
        stairBuilder(ModBlocks.MAGENTA_MARBLE_TILE_STAIRS.get(), Ingredient.of(ModBlocks.MAGENTA_MARBLE_TILES)).group("magenta_marble_tiles")
                .unlockedBy("has_marble_tiles", has(ModBlocks.MAGENTA_MARBLE_TILES)).save(recipeOutput);
        stairBuilder(ModBlocks.PINK_MARBLE_BRICK_STAIRS.get(), Ingredient.of(ModBlocks.PINK_MARBLE_BRICKS)).group("pink_marble_bricks")
                .unlockedBy("has_marble_bricks", has(ModBlocks.PINK_MARBLE_BRICKS)).save(recipeOutput);
        stairBuilder(ModBlocks.PINK_MARBLE_TILE_STAIRS.get(), Ingredient.of(ModBlocks.PINK_MARBLE_TILES)).group("pink_marble_tiles")
                .unlockedBy("has_marble_tiles", has(ModBlocks.PINK_MARBLE_TILES)).save(recipeOutput);

        //Slabs
        slab(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.GOLDEN_PALM_SLAB.get(), ModBlocks.GOLDEN_PALM_PLANKS.get());
        slab(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.IRONWOOD_SLABS.get(), ModBlocks.IRONWOOD_PLANKS.get());

        slab(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.MARBLE_BRICK_SLABS.get(), ModBlocks.MARBLE_BRICKS.get());
        slab(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.MARBLE_TILE_SLABS.get(), ModBlocks.MARBLE_TILES.get());
        slab(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.LIGHT_GRAY_MARBLE_BRICK_SLABS.get(), ModBlocks.LIGHT_GRAY_MARBLE_BRICKS.get());
        slab(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.LIGHT_GRAY_MARBLE_TILE_SLABS.get(), ModBlocks.LIGHT_GRAY_MARBLE_TILES.get());
        slab(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.GRAY_MARBLE_BRICK_SLABS.get(), ModBlocks.GRAY_MARBLE_BRICKS.get());
        slab(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.GRAY_MARBLE_TILE_SLABS.get(), ModBlocks.GRAY_MARBLE_TILES.get());
        slab(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.CHARRED_MARBLE_BRICK_SLABS.get(), ModBlocks.CHARRED_MARBLE_BRICKS.get());
        slab(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.CHARRED_MARBLE_TILE_SLABS.get(), ModBlocks.CHARRED_MARBLE_TILES.get());
        slab(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.BROWN_MARBLE_BRICK_SLABS.get(), ModBlocks.BROWN_MARBLE_BRICKS.get());
        slab(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.BROWN_MARBLE_TILE_SLABS.get(), ModBlocks.BROWN_MARBLE_TILES.get());
        slab(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.RED_MARBLE_BRICK_SLABS.get(), ModBlocks.RED_MARBLE_BRICKS.get());
        slab(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.RED_MARBLE_TILE_SLABS.get(), ModBlocks.RED_MARBLE_TILES.get());
        slab(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.ORANGE_MARBLE_BRICK_SLABS.get(), ModBlocks.ORANGE_MARBLE_BRICKS.get());
        slab(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.ORANGE_MARBLE_TILE_SLABS.get(), ModBlocks.ORANGE_MARBLE_TILES.get());
        slab(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.YELLOW_MARBLE_BRICK_SLABS.get(), ModBlocks.YELLOW_MARBLE_BRICKS.get());
        slab(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.YELLOW_MARBLE_TILE_SLABS.get(), ModBlocks.YELLOW_MARBLE_TILES.get());
        slab(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.LIME_MARBLE_BRICK_SLABS.get(), ModBlocks.LIME_MARBLE_BRICKS.get());
        slab(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.LIME_MARBLE_TILE_SLABS.get(), ModBlocks.LIME_MARBLE_TILES.get());
        slab(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.GREEN_MARBLE_BRICK_SLABS.get(), ModBlocks.GREEN_MARBLE_BRICKS.get());
        slab(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.GREEN_MARBLE_TILE_SLABS.get(), ModBlocks.GREEN_MARBLE_TILES.get());
        slab(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.CYAN_MARBLE_BRICK_SLABS.get(), ModBlocks.CYAN_MARBLE_BRICKS.get());
        slab(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.CYAN_MARBLE_TILE_SLABS.get(), ModBlocks.CYAN_MARBLE_TILES.get());
        slab(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.LIGHT_BLUE_MARBLE_BRICK_SLABS.get(), ModBlocks.LIGHT_BLUE_MARBLE_BRICKS.get());
        slab(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.LIGHT_BLUE_MARBLE_TILE_SLABS.get(), ModBlocks.LIGHT_BLUE_MARBLE_TILES.get());
        slab(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.BLUE_MARBLE_BRICK_SLABS.get(), ModBlocks.BLUE_MARBLE_BRICKS.get());
        slab(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.BLUE_MARBLE_TILE_SLABS.get(), ModBlocks.BLUE_MARBLE_TILES.get());
        slab(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.PURPLE_MARBLE_BRICK_SLABS.get(), ModBlocks.PURPLE_MARBLE_BRICKS.get());
        slab(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.PURPLE_MARBLE_TILE_SLABS.get(), ModBlocks.PURPLE_MARBLE_TILES.get());
        slab(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.MAGENTA_MARBLE_BRICK_SLABS.get(), ModBlocks.MAGENTA_MARBLE_BRICKS.get());
        slab(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.MAGENTA_MARBLE_TILE_SLABS.get(), ModBlocks.MAGENTA_MARBLE_TILES.get());
        slab(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.PINK_MARBLE_BRICK_SLABS.get(), ModBlocks.PINK_MARBLE_BRICKS.get());
        slab(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.PINK_MARBLE_TILE_SLABS.get(), ModBlocks.PINK_MARBLE_TILES.get());

        //Walls
        wall(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.MARBLE_BRICK_WALLS.get(), ModBlocks.MARBLE_BRICKS.get());
        wall(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.MARBLE_TILE_WALLS.get(), ModBlocks.MARBLE_TILES.get());
        wall(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.LIGHT_GRAY_MARBLE_BRICK_WALLS.get(), ModBlocks.LIGHT_GRAY_MARBLE_BRICKS.get());
        wall(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.LIGHT_GRAY_MARBLE_TILE_WALLS.get(), ModBlocks.LIGHT_GRAY_MARBLE_TILES.get());
        wall(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.GRAY_MARBLE_BRICK_WALLS.get(), ModBlocks.GRAY_MARBLE_BRICKS.get());
        wall(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.GRAY_MARBLE_TILE_WALLS.get(), ModBlocks.GRAY_MARBLE_TILES.get());
        wall(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.CHARRED_MARBLE_BRICK_WALLS.get(), ModBlocks.CHARRED_MARBLE_BRICKS.get());
        wall(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.CHARRED_MARBLE_TILE_WALLS.get(), ModBlocks.CHARRED_MARBLE_TILES.get());
        wall(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.BROWN_MARBLE_BRICK_WALLS.get(), ModBlocks.BROWN_MARBLE_BRICKS.get());
        wall(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.BROWN_MARBLE_TILE_WALLS.get(), ModBlocks.BROWN_MARBLE_TILES.get());
        wall(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.RED_MARBLE_BRICK_WALLS.get(), ModBlocks.RED_MARBLE_BRICKS.get());
        wall(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.RED_MARBLE_TILE_WALLS.get(), ModBlocks.RED_MARBLE_TILES.get());
        wall(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.ORANGE_MARBLE_BRICK_WALLS.get(), ModBlocks.ORANGE_MARBLE_BRICKS.get());
        wall(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.ORANGE_MARBLE_TILE_WALLS.get(), ModBlocks.ORANGE_MARBLE_TILES.get());
        wall(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.YELLOW_MARBLE_BRICK_WALLS.get(), ModBlocks.YELLOW_MARBLE_BRICKS.get());
        wall(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.YELLOW_MARBLE_TILE_WALLS.get(), ModBlocks.YELLOW_MARBLE_TILES.get());
        wall(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.LIME_MARBLE_BRICK_WALLS.get(), ModBlocks.LIME_MARBLE_BRICKS.get());
        wall(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.LIME_MARBLE_TILE_WALLS.get(), ModBlocks.LIME_MARBLE_TILES.get());
        wall(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.GREEN_MARBLE_BRICK_WALLS.get(), ModBlocks.GREEN_MARBLE_BRICKS.get());
        wall(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.GREEN_MARBLE_TILE_WALLS.get(), ModBlocks.GREEN_MARBLE_TILES.get());
        wall(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.CYAN_MARBLE_BRICK_WALLS.get(), ModBlocks.CYAN_MARBLE_BRICKS.get());
        wall(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.CYAN_MARBLE_TILE_WALLS.get(), ModBlocks.CYAN_MARBLE_TILES.get());
        wall(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.LIGHT_BLUE_MARBLE_BRICK_WALLS.get(), ModBlocks.LIGHT_BLUE_MARBLE_BRICKS.get());
        wall(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.LIGHT_BLUE_MARBLE_TILE_WALLS.get(), ModBlocks.LIGHT_BLUE_MARBLE_TILES.get());
        wall(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.BLUE_MARBLE_BRICK_WALLS.get(), ModBlocks.BLUE_MARBLE_BRICKS.get());
        wall(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.BLUE_MARBLE_TILE_WALLS.get(), ModBlocks.BLUE_MARBLE_TILES.get());
        wall(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.PURPLE_MARBLE_BRICK_WALLS.get(), ModBlocks.PURPLE_MARBLE_BRICKS.get());
        wall(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.PURPLE_MARBLE_TILE_WALLS.get(), ModBlocks.PURPLE_MARBLE_TILES.get());
        wall(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.MAGENTA_MARBLE_BRICK_WALLS.get(), ModBlocks.MAGENTA_MARBLE_BRICKS.get());
        wall(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.MAGENTA_MARBLE_TILE_WALLS.get(), ModBlocks.MAGENTA_MARBLE_TILES.get());
        wall(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.PINK_MARBLE_BRICK_WALLS.get(), ModBlocks.PINK_MARBLE_BRICKS.get());
        wall(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.PINK_MARBLE_TILE_WALLS.get(), ModBlocks.PINK_MARBLE_TILES.get());

        //Chiseled Blocks
        chiseled(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.MARBLE_CHISELED.get(), ModBlocks.MARBLE_BRICK_SLABS.get());
        chiseled(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.LIGHT_GRAY_MARBLE_CHISELED.get(), ModBlocks.LIGHT_GRAY_MARBLE_BRICK_SLABS.get());
        chiseled(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.GRAY_MARBLE_CHISELED.get(), ModBlocks.GRAY_MARBLE_BRICK_SLABS.get());
        chiseled(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.CHARRED_MARBLE_CHISELED.get(), ModBlocks.CHARRED_MARBLE_BRICK_SLABS.get());
        chiseled(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.BROWN_MARBLE_CHISELED.get(), ModBlocks.BROWN_MARBLE_BRICK_SLABS.get());
        chiseled(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.RED_MARBLE_CHISELED.get(), ModBlocks.RED_MARBLE_BRICK_SLABS.get());
        chiseled(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.ORANGE_MARBLE_CHISELED.get(), ModBlocks.ORANGE_MARBLE_BRICK_SLABS.get());
        chiseled(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.YELLOW_MARBLE_CHISELED.get(), ModBlocks.YELLOW_MARBLE_BRICK_SLABS.get());
        chiseled(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.LIME_MARBLE_CHISELED.get(), ModBlocks.LIME_MARBLE_BRICK_SLABS.get());
        chiseled(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.GREEN_MARBLE_CHISELED.get(), ModBlocks.GREEN_MARBLE_BRICK_SLABS.get());
        chiseled(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.CYAN_MARBLE_CHISELED.get(), ModBlocks.CYAN_MARBLE_BRICK_SLABS.get());
        chiseled(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.LIGHT_BLUE_MARBLE_CHISELED.get(), ModBlocks.LIGHT_BLUE_MARBLE_BRICK_SLABS.get());
        chiseled(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.BLUE_MARBLE_CHISELED.get(), ModBlocks.BLUE_MARBLE_BRICK_SLABS.get());
        chiseled(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.PURPLE_MARBLE_CHISELED.get(), ModBlocks.PURPLE_MARBLE_BRICK_SLABS.get());
        chiseled(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.MAGENTA_MARBLE_CHISELED.get(), ModBlocks.MAGENTA_MARBLE_BRICK_SLABS.get());
        chiseled(recipeOutput, RecipeCategory.BUILDING_BLOCKS, ModBlocks.PINK_MARBLE_CHISELED.get(), ModBlocks.PINK_MARBLE_BRICK_SLABS.get());

        buttonBuilder(ModBlocks.GOLDEN_PALM_BUTTON.get(), Ingredient.of(ModBlocks.GOLDEN_PALM_PLANKS.get())).group("golden_palm_planks")
                .unlockedBy("has_golden_palm_planks", has(ModBlocks.GOLDEN_PALM_PLANKS.get())).save(recipeOutput);
        pressurePlate(recipeOutput, ModBlocks.GOLDEN_PALM_PRESSURE_PLATE.get(), ModBlocks.GOLDEN_PALM_PLANKS.get());

        fenceBuilder(ModBlocks.GOLDEN_PALM_FENCE.get(), Ingredient.of(ModBlocks.GOLDEN_PALM_PLANKS.get())).group("golden_palm_planks")
                .unlockedBy("has_golden_palm_planks", has(ModBlocks.GOLDEN_PALM_PLANKS.get())).save(recipeOutput);
        fenceGateBuilder(ModBlocks.GOLDEN_PALM_FENCE_GATE.get(), Ingredient.of(ModBlocks.GOLDEN_PALM_PLANKS.get())).group("golden_palm_planks")
                .unlockedBy("has_golden_palm_planks", has(ModBlocks.GOLDEN_PALM_PLANKS.get())).save(recipeOutput);

        doorBuilder(ModBlocks.GOLDEN_PALM_DOOR.get(), Ingredient.of(ModBlocks.GOLDEN_PALM_PLANKS.get())).group("golden_palm_planks")
                .unlockedBy("has_golden_palm_planks", has(ModBlocks.GOLDEN_PALM_PLANKS.get())).save(recipeOutput);
        trapdoorBuilder(ModBlocks.GOLDEN_PALM_TRAPDOOR.get(), Ingredient.of(ModBlocks.GOLDEN_PALM_PLANKS.get())).group("golden_palm_planks")
                .unlockedBy("has_golden_palm_planks", has(ModBlocks.GOLDEN_PALM_PLANKS.get())).save(recipeOutput);

        buttonBuilder(ModBlocks.IRONWOOD_BUTTON.get(), Ingredient.of(ModBlocks.IRONWOOD_PLANKS.get())).group("ironwood_planks")
                .unlockedBy("has_golden_palm_planks", has(ModBlocks.IRONWOOD_PLANKS.get())).save(recipeOutput);
        pressurePlate(recipeOutput, ModBlocks.IRONWOOD_PRESSURE_PLATE.get(), ModBlocks.IRONWOOD_PLANKS.get());

        fenceBuilder(ModBlocks.IRONWOOD_FENCE.get(), Ingredient.of(ModBlocks.IRONWOOD_PLANKS.get())).group("ironwood_planks")
                .unlockedBy("has_ironwood_planks", has(ModBlocks.IRONWOOD_PLANKS.get())).save(recipeOutput);
        fenceGateBuilder(ModBlocks.IRONWOOD_FENCE_GATE.get(), Ingredient.of(ModBlocks.IRONWOOD_PLANKS.get())).group("ironwood_planks")
                .unlockedBy("has_ironwood_planks", has(ModBlocks.IRONWOOD_PLANKS.get())).save(recipeOutput);

        doorBuilder(ModBlocks.IRONWOOD_DOOR.get(), Ingredient.of(ModBlocks.IRONWOOD_PLANKS.get())).group("ironwood_planks")
                .unlockedBy("has_ironwood_planks", has(ModBlocks.IRONWOOD_PLANKS.get())).save(recipeOutput);
        trapdoorBuilder(ModBlocks.IRONWOOD_TRAPDOOR.get(), Ingredient.of(ModBlocks.IRONWOOD_PLANKS.get())).group("ironwood_planks")
                .unlockedBy("has_ironwood_planks", has(ModBlocks.IRONWOOD_PLANKS.get())).save(recipeOutput);

        // Pill Recipes

        // ── Cultivation Pills ─────────────────────────────────────────

        // Essence Gathering Pill
        PillCauldronRecipeBuilder.lowHuman(
                        ModItems.ESSENCE_GATHERING_PILL.get(),
                        ModItems.PILL_RESIDUE.get()
                )
                .ingredient(ModItems.HUNDRED_YEAR_FIRE_GINSENG.get(), 1)
                .ingredient(ModItems.WHITE_JADE_ORCHID.get(), 1)
                .ingredient(ModItems.HUNDRED_YEAR_SNOW_GINSENG.get(), 1)
                .chance(0.75D)
                .temperature(478, 1248, 677)
                .timeSeconds(10)
                .realm(1, "lower")
                .purity(10, 100)
                .bonusChance(0.08D)
                .unlockedBy("has_hundred_year_fire_ginseng", has(ModItems.HUNDRED_YEAR_FIRE_GINSENG.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "cauldron/essence_gathering_pill"));

        // Inner Reinforcement Pill
        PillCauldronRecipeBuilder.lowHuman(
                        ModItems.INNER_REINFORCEMENT_PILL.get(),
                        ModItems.PILL_RESIDUE.get()
                )
                .ingredient(ModItems.HUNDRED_YEAR_GINSENG.get(), 1)
                .ingredient(ModItems.IRONWOOD_SPROUT.get(), 1)
                .ingredient(ModItems.GOLDEN_SUN_LEAF.get(), 1)
                .chance(0.70D)
                .temperature(325, 986, 543)
                .timeSeconds(10)
                .realm(1, "lower")
                .purity(15, 90)
                .bonusChance(0.06D)
                .unlockedBy("has_hundred_year_ginseng", has(ModItems.HUNDRED_YEAR_GINSENG.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "cauldron/inner_reinforcement_pill"));

        // Soul Focus Pill
        PillCauldronRecipeBuilder.lowHuman(
                        ModItems.SOUL_FOCUS_PILL.get(),
                        ModItems.PILL_RESIDUE.get()
                )
                .ingredient(ModItems.JADE_BAMBOO_OF_SERENITY.get(), 1)
                .ingredient(ModItems.WHITE_JADE_ORCHID.get(), 1)
                .ingredient(ModItems.HUNDRED_YEAR_SNOW_GINSENG.get(), 1)
                .chance(0.70D)
                .temperature(146, 752, 322)
                .timeSeconds(10)
                .realm(1, "lower")
                .purity(15, 90)
                .bonusChance(0.06D)
                .unlockedBy("has_jade_bamboo_of_serenity", has(ModItems.JADE_BAMBOO_OF_SERENITY.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "cauldron/soul_focus_pill"));


        PillCauldronRecipeBuilder.lowHuman(
                        ModItems.QI_REPLENISHING_PILL.get(),
                        ModItems.PILL_RESIDUE.get()
                )
                .ingredient(ModItems.JADE_DEW_GRASS.get(), 3)
                .ingredient(Items.GOLDEN_APPLE, 1)
                .ingredient(ModItems.JADE_DEW_GRASS.get(), 3)
                .chance(0.50D)
                .temperature(325, 785, 546)
                .timeSeconds(5)
                .realm(1, "lower")
                .purity(15, 100)
                .bonusChance(0.06D)
                .unlockedBy("has_jade_dew_grass", has(ModItems.JADE_DEW_GRASS.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "cauldron/qi_replenishing_pill"));


        // ── Poison Pills ──────────────────────────────────────────────

        // Cracked Meridians Poison Pill
        PillCauldronRecipeBuilder.lowHuman(
                        ModItems.CRACKED_MERIDIANS_POISON_PILL.get(),
                        ModItems.PILL_RESIDUE.get()
                )
                .ingredient(ModItems.HUNDRED_YEAR_GINSENG.get(), 1)
                .ingredient(Items.BONE, 4)
                .ingredient(Items.PUFFERFISH, 2)
                .chance(0.55D)
                .temperature(520, 1380, 610)
                .timeSeconds(6)
                .realm(1, "lower")
                .purity(15, 75)
                .bonusChance(0.04D)
                .unlockedBy("has_poisonous_potato", has(Items.POISONOUS_POTATO))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "cauldron/cracked_meridians_poison_pill"));

        // Blinded Senses Poison Pill
        PillCauldronRecipeBuilder.lowHuman(
                        ModItems.BLINDED_SENSES_POISON_PILL.get(),
                        ModItems.PILL_RESIDUE.get()
                )
                .ingredient(Items.SPIDER_EYE, 12)
                .ingredient(Items.INK_SAC, 8)
                .ingredient(ModItems.WHITE_JADE_ORCHID.get(), 1)
                .chance(0.50D)
                .temperature(480, 1250, 580)
                .timeSeconds(7)
                .realm(1, "lower")
                .purity(10, 70)
                .bonusChance(0.03D)
                .unlockedBy("has_spider_eye", has(Items.SPIDER_EYE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "cauldron/blinded_senses_poison_pill"));

        // Paralyzed Body Poison Pill
        PillCauldronRecipeBuilder.lowHuman(
                        ModItems.PARALYZED_BODY_POISON_PILL.get(),
                        ModItems.PILL_RESIDUE.get()
                )
                .ingredient(Items.HONEYCOMB, 6)
                .ingredient(ModItems.HUNDRED_YEAR_GINSENG.get(), 1)
                .ingredient(Items.RABBIT_FOOT, 2)
                .chance(0.48D)
                .temperature(620, 1420, 720)
                .timeSeconds(8)
                .realm(1, "lower")
                .purity(12, 65)
                .bonusChance(0.03D)
                .unlockedBy("has_rabbit_foot", has(Items.RABBIT_FOOT))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "cauldron/paralyzed_body_poison_pill"));

        // Venomous Meridian Poison Pill
        PillCauldronRecipeBuilder.lowHuman(
                        ModItems.VENOMOUS_MERIDIAN_POISON_PILL.get(),
                        ModItems.PILL_RESIDUE.get()
                )
                .ingredient(Items.POISONOUS_POTATO, 5)
                .ingredient(Items.FERMENTED_SPIDER_EYE, 10)
                .ingredient(ModItems.JADE_DEW_GRASS.get(), 1)
                .chance(0.52D)
                .temperature(550, 1320, 640)
                .timeSeconds(6)
                .realm(1, "lower")
                .purity(18, 72)
                .bonusChance(0.04D)
                .unlockedBy("has_fermented_spider_eye", has(Items.FERMENTED_SPIDER_EYE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "cauldron/venomous_meridian_poison_pill"));

        // Scorching Yang Poison Pill
        PillCauldronRecipeBuilder.lowHuman(
                        ModItems.SCORCHING_YANG_POISON_PILL.get(),
                        ModItems.PILL_RESIDUE.get()
                )
                .ingredient(Items.BLAZE_POWDER, 6)
                .ingredient(Items.MAGMA_CREAM, 4)
                .ingredient(ModItems.HUNDRED_YEAR_FIRE_GINSENG.get(), 1)
                .chance(0.58D)
                .temperature(890, 1650, 980)
                .timeSeconds(5)
                .realm(1, "lower")
                .purity(22, 78)
                .bonusChance(0.05D)
                .unlockedBy("has_blaze_powder", has(Items.BLAZE_POWDER))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "cauldron/scorching_yang_poison_pill"));

        // Qi Devouring Parasite Pill
        PillCauldronRecipeBuilder.lowHuman(
                        ModItems.QI_DEVOURING_PARASITE_PILL.get(),
                        ModItems.PILL_RESIDUE.get()
                )
                .ingredient(Items.ROTTEN_FLESH, 16)
                .ingredient(ModItems.JADE_DEW_GRASS.get(), 1)
                .ingredient(Items.FERMENTED_SPIDER_EYE, 16)
                .chance(0.60D)
                .temperature(587, 1479, 670)
                .timeSeconds(5)
                .realm(1, "lower")
                .purity(20, 80)
                .bonusChance(0.05D)
                .unlockedBy("has_hundred_year_fire_ginseng", has(ModItems.HUNDRED_YEAR_FIRE_GINSENG.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "cauldron/qi_devouring_parasite_pill"));

        // Corrosive Poison Pill
        PillCauldronRecipeBuilder.lowHuman(
                        ModItems.CORROSIVE_POISON_PILL.get(),
                        ModItems.PILL_RESIDUE.get()
                )
                .ingredient(Items.SLIME_BALL, 8)
                .ingredient(Items.GUNPOWDER, 6)
                .ingredient(ModItems.HUNDRED_YEAR_FIRE_GINSENG.get(), 1)
                .chance(0.45D)
                .temperature(710, 1520, 820)
                .timeSeconds(9)
                .realm(1, "lower")
                .purity(8, 60)
                .bonusChance(0.02D)
                .unlockedBy("has_gunpowder", has(Items.GUNPOWDER))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "cauldron/corrosive_poison_pill"));

        // Frost Silkworm Poison Pill
        PillCauldronRecipeBuilder.lowHuman(
                        ModItems.FROST_SILKWORM_POISON_PILL.get(),
                        ModItems.PILL_RESIDUE.get()
                )
                .ingredient(Items.POWDER_SNOW_BUCKET, 1)
                .ingredient(ModItems.HUNDRED_YEAR_SNOW_GINSENG.get(), 1)
                .ingredient(Items.BLUE_ICE, 4)
                .chance(0.53D)
                .temperature(310, 980, 420)
                .timeSeconds(10)
                .realm(1, "lower")
                .purity(16, 68)
                .bonusChance(0.04D)
                .unlockedBy("has_blue_ice", has(Items.BLUE_ICE))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "cauldron/frost_silkworm_poison_pill"));


        // ── Positive / Medicinal Pills ────────────────────────────────

        // Qi Enhanced Regeneration Pill
        PillCauldronRecipeBuilder.lowHuman(
                        ModItems.QI_ENHANCED_REGEN_PILL.get(),
                        ModItems.PILL_RESIDUE.get()
                )
                .ingredient(Items.GOLDEN_APPLE, 2)
                .ingredient(ModItems.WHITE_JADE_ORCHID.get(), 1)
                .ingredient(Items.GOLDEN_CARROT, 2)
                .chance(0.65D)
                .temperature(345, 974, 666)
                .timeSeconds(7)
                .realm(1, "lower")
                .purity(15, 100)
                .bonusChance(0.07D)
                .unlockedBy("has_hundred_year_ginseng", has(ModItems.HUNDRED_YEAR_GINSENG.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "cauldron/regeneration_pill"));


        // ── Antidote Pills ────────────────────────────────────────────

        // Antidote for Qi Devouring Parasite
        PillCauldronRecipeBuilder.lowHuman(
                        ModItems.ANTIDOTE_PILL_QDP.get(),
                        ModItems.PILL_RESIDUE.get()
                )
                .ingredient(ModItems.JADE_BAMBOO_OF_SERENITY.get(), 1)
                .ingredient(ModItems.HUNDRED_YEAR_SNOW_GINSENG.get(), 1)
                .ingredient(ModItems.GOLDEN_SUN_LEAF.get(), 1)
                .chance(0.70D)
                .temperature(327, 895, 535)
                .timeSeconds(6)
                .realm(1, "lower")
                .purity(20, 90)
                .bonusChance(0.06D)
                .unlockedBy("has_jade_bamboo_of_serenity", has(ModItems.JADE_BAMBOO_OF_SERENITY.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "cauldron/antidote_qdp_pill"));


        // ── Physique Changing Pills ───────────────────────────────────

        // Five Element Harmony Pill
        PillCauldronRecipeBuilder.lowHuman(
                        ModItems.FIVE_ELEMENT_HARMONY_PILL.get(),
                        ModItems.PILL_RESIDUE.get()
                )
                .ingredient(ModItems.UNSTABLE_5_ELEMENT_ESSENCE.get(), 1)
                .ingredient(ModItems.JADE_BAMBOO_OF_SERENITY.get(), 1)
                .ingredient(Items.WITHER_ROSE, 1)
                .chance(0.50D)
                .temperature(600, 950, 775)
                .timeSeconds(30)
                .realm(3, "lower")
                .purity(10, 100)
                .bonusChance(0.04D)
                .unlockedBy("has_unstable_5_element_essence", has(ModItems.UNSTABLE_5_ELEMENT_ESSENCE.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "cauldron/five_element_harmony_pill"));

        // Marrow Cleanse Pill
        PillCauldronRecipeBuilder.lowHuman(
                        ModItems.MARROW_CLEANSE_PILL.get(),
                        ModItems.PILL_RESIDUE.get()
                )
                .ingredient(ModItems.HUNDRED_YEAR_GINSENG.get(), 1)
                .ingredient(ModItems.HUNDRED_YEAR_FIRE_GINSENG.get(), 1)
                .ingredient(ModItems.HUNDRED_YEAR_SNOW_GINSENG.get(), 1)
                .chance(0.55D)
                .temperature(175, 1247, 649)
                .timeSeconds(10)
                .realm(2, "lower")
                .purity(25, 100)
                .bonusChance(0.04D)
                .unlockedBy("has_hundred_year_ginseng", has(ModItems.HUNDRED_YEAR_GINSENG.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "cauldron/marrow_cleanse_pill"));


        // ── Utility Pills ─────────────────────────────────────────────

        // Neutrality Pill
        PillCauldronRecipeBuilder.lowHuman(
                        ModItems.NEUTRALITY_PILL.get(),
                        ModItems.PILL_RESIDUE.get()
                )
                .ingredient(Items.GOLD_INGOT, 2)
                .ingredient(ModItems.GOLDEN_SUN_LEAF.get(), 1)
                .ingredient(Items.BONE, 8)
                .chance(0.80D)
                .temperature(365, 975, 592)
                .timeSeconds(4)
                .realm(1, "lower")
                .purity(10, 80)
                .bonusChance(0.10D)
                .unlockedBy("has_jade_bamboo_of_serenity", has(ModItems.JADE_BAMBOO_OF_SERENITY.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "cauldron/neutrality_pill"));

        // Cleansing Pill T1
        PillCauldronRecipeBuilder.lowHuman(
                        ModItems.CLEANSING_PILL_T1.get(),
                        ModItems.PILL_RESIDUE.get()
                )
                .ingredient(ModItems.GOLDEN_SUN_LEAF.get(), 1)
                .ingredient(Items.MILK_BUCKET, 1)
                .ingredient(ModItems.WHITE_JADE_ORCHID.get(), 1)
                .chance(0.85D)
                .temperature(212, 975, 460)
                .timeSeconds(3)
                .realm(1, "lower")
                .purity(10, 60)
                .bonusChance(0.10D)
                .unlockedBy("has_golden_sun_leaf", has(ModItems.GOLDEN_SUN_LEAF.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "cauldron/cleansing_pill_t1"));

        // Cleansing Pill T2
        PillCauldronRecipeBuilder.lowHuman(
                        ModItems.CLEANSING_PILL_T2.get(),
                        ModItems.PILL_RESIDUE.get()
                )
                .ingredient(ModItems.GOLDEN_SUN_LEAF.get(), 1)
                .ingredient(ModItems.CLEANSING_PILL_T1, 1)
                .ingredient(ModItems.WHITE_JADE_ORCHID.get(), 1)
                .chance(0.80D)
                .temperature(212, 975, 460)
                .timeSeconds(4)
                .realm(1, "lower")
                .purity(15, 70)
                .bonusChance(0.08D)
                .unlockedBy("has_jade_bamboo_of_serenity", has(ModItems.JADE_BAMBOO_OF_SERENITY.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "cauldron/cleansing_pill_t2"));

        // Cleansing Pill T3
        PillCauldronRecipeBuilder.lowHuman(
                        ModItems.CLEANSING_PILL_T3.get(),
                        ModItems.PILL_RESIDUE.get()
                )
                .ingredient(ModItems.GOLDEN_SUN_LEAF.get(), 1)
                .ingredient(ModItems.CLEANSING_PILL_T2, 1)
                .ingredient(ModItems.WHITE_JADE_ORCHID.get(), 1)
                .chance(0.75D)
                .temperature(212, 975, 460)
                .timeSeconds(5)
                .realm(2, "lower")
                .purity(20, 80)
                .bonusChance(0.07D)
                .unlockedBy("has_ironwood_sprout", has(ModItems.IRONWOOD_SPROUT.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "cauldron/cleansing_pill_t3"));

        // Cleansing Pill T4
        PillCauldronRecipeBuilder.lowHuman(
                        ModItems.CLEANSING_PILL_T4.get(),
                        ModItems.PILL_RESIDUE.get()
                )
                .ingredient(ModItems.HUNDRED_YEAR_FIRE_GINSENG.get(), 1)
                .ingredient(ModItems.CLEANSING_PILL_T3, 1)
                .ingredient(ModItems.WHITE_JADE_ORCHID.get(), 1)
                .chance(0.70D)
                .temperature(212, 975, 460)
                .timeSeconds(6)
                .realm(2, "lower")
                .purity(25, 90)
                .bonusChance(0.06D)
                .unlockedBy("has_white_jade_orchid", has(ModItems.WHITE_JADE_ORCHID.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "cauldron/cleansing_pill_t4"));

        // Fasting Pill T1
        PillCauldronRecipeBuilder.lowHuman(
                        ModItems.FASTING_PILL_T1.get(),
                        ModItems.PILL_RESIDUE.get()
                )
                .ingredient(Items.CARROT, 4)
                .ingredient(Items.APPLE, 4)
                .ingredient(Items.BEETROOT, 4)
                .chance(0.80D)
                .temperature(100, 1000, 500)
                .timeSeconds(4)
                .realm(1, "lower")
                .purity(10, 70)
                .bonusChance(0.08D)
                .unlockedBy("has_ironwood_sprout", has(ModItems.IRONWOOD_SPROUT.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "cauldron/fasting_pill_t1"));

        // Fasting Pill T2
        PillCauldronRecipeBuilder.lowHuman(
                        ModItems.FASTING_PILL_T2.get(),
                        ModItems.PILL_RESIDUE.get()
                )
                .ingredient(Items.CARROT, 4)
                .ingredient(ModItems.FASTING_PILL_T1.get(), 1)
                .ingredient(Items.BEETROOT, 4)
                .chance(0.75D)
                .temperature(100, 1000, 500)
                .timeSeconds(5)
                .realm(1, "lower")
                .purity(15, 75)
                .bonusChance(0.07D)
                .unlockedBy("has_hundred_year_ginseng", has(ModItems.HUNDRED_YEAR_GINSENG.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "cauldron/fasting_pill_t2"));

        // Fasting Pill T3
        PillCauldronRecipeBuilder.lowHuman(
                        ModItems.FASTING_PILL_T3.get(),
                        ModItems.PILL_RESIDUE.get()
                )
                .ingredient(Items.CARROT, 4)
                .ingredient(ModItems.FASTING_PILL_T2.get(), 1)
                .ingredient(Items.BEETROOT, 4)
                .chance(0.70D)
                .temperature(100, 1000, 500)
                .timeSeconds(6)
                .realm(2, "lower")
                .purity(20, 85)
                .bonusChance(0.06D)
                .unlockedBy("has_hundred_year_snow_ginseng", has(ModItems.HUNDRED_YEAR_SNOW_GINSENG.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "cauldron/fasting_pill_t3"));

        // Crimson Lotus Bone Pill
        PillCauldronRecipeBuilder.lowHuman(
                        ModItems.CRIMSON_LOTUS_BONE_PILL.get(),
                        ModItems.PILL_RESIDUE.get()
                )
                .ingredient(ModItems.HUNDRED_YEAR_FIRE_GINSENG.get(), 2)
                .ingredient(ModItems.WHITE_JADE_ORCHID.get(), 2)
                .ingredient(Items.BONE_BLOCK, 4)
                .chance(0.35D)
                .temperature(700, 1000, 900)
                .timeSeconds(20)
                .realm(4, "lower")
                .purity(60, 100)
                .bonusChance(0.03D)
                .unlockedBy("has_hundred_year_fire_ginseng", has(ModItems.HUNDRED_YEAR_FIRE_GINSENG.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "cauldron/crimson_lotus_bone_pill"));



    }





    protected static void oreSmelting(RecipeOutput recipeOutput, Item pIngredient, RecipeCategory pCategory, ItemLike pResult,
                                      float pExperience, int pCookingTime, String pGroup) {
        oreCooking(recipeOutput, RecipeSerializer.SMELTING_RECIPE, SmeltingRecipe::new, List.of(pIngredient), pCategory, pResult,
                pExperience, pCookingTime, pGroup, "_from_smelting");
    }

    protected static void oreBlasting(RecipeOutput recipeOutput, Item pIngredient, RecipeCategory pCategory, ItemLike pResult,
                                      float pExperience, int pCookingTime, String pGroup) {
        oreCooking(recipeOutput, RecipeSerializer.BLASTING_RECIPE, BlastingRecipe::new, List.of(pIngredient), pCategory, pResult,
                pExperience, pCookingTime, pGroup, "_from_blasting");
    }


    protected static <T extends AbstractCookingRecipe> void oreCooking(RecipeOutput recipeOutput, RecipeSerializer<T> pCookingSerializer, AbstractCookingRecipe.Factory<T> factory,
                                                                       List<ItemLike> pIngredients, RecipeCategory pCategory, ItemLike pResult, float pExperience, int pCookingTime, String pGroup, String pRecipeName) {
        for (ItemLike itemlike : pIngredients) {
            SimpleCookingRecipeBuilder.generic(Ingredient.of(itemlike), pCategory, pResult, pExperience, pCookingTime, pCookingSerializer, factory).group(pGroup).unlockedBy(getHasName(itemlike), has(itemlike))
                    .save(recipeOutput, AscensionCraft.MOD_ID + ":" + getItemName(pResult) + pRecipeName + "_" + getItemName(itemlike));
        }
    }
}
