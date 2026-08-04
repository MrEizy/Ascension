package net.zic.ascension.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.blocks.ModBlocks;
import net.zic.ascension.common.item.ModItems;

import java.util.List;
import java.util.concurrent.CompletableFuture;


public class AscRecipeProvider extends RecipeProvider {
    public AscRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    public static class Runner extends RecipeProvider.Runner {
        public Runner(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registries) {
            super(packOutput, registries);
        }

        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
            return new AscRecipeProvider(registries, output);
        }

        @Override
        public String getName() {
            return "Ascension Recipes";
        }
    }

    @Override
    protected void buildRecipes() {



        //Ores
        List<ItemLike> JADE_SMELTABLES = List.of(ModBlocks.JADE_ORE.get());
        oreSmelting(JADE_SMELTABLES, RecipeCategory.MISC, CookingBookCategory.MISC, ModItems.JADE.get(), 0.25f, 200, "jade");
        oreBlasting(JADE_SMELTABLES, RecipeCategory.MISC, CookingBookCategory.MISC, ModItems.JADE.get(), 0.25f, 100, "jade");
        shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.JADE_BLOCK.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItems.JADE.get())
                .unlockedBy(getHasName(ModItems.JADE.get()), has(ModItems.JADE))
                .group("jade_block")
                .save(output, "ascension:shaped/jade_block_from_jade");
        shapeless(RecipeCategory.MISC, ModItems.JADE.get(), 9)
                .requires(ModBlocks.JADE_BLOCK)
                .unlockedBy(getHasName(ModBlocks.JADE_BLOCK.get()), has(ModBlocks.JADE_BLOCK))
                .group("jade")
                .save(output, "ascension:shapeless/jade_from_block");
        List<ItemLike> FROST_SILVER_SMELTABLES = List.of(ModBlocks.FROST_SILVER_ORE.get(), ModItems.RAW_FROST_SILVER.get());
        oreSmelting(FROST_SILVER_SMELTABLES, RecipeCategory.MISC, CookingBookCategory.MISC, ModItems.FROST_SILVER_INGOT.get(), 0.25f, 200, "frost_silver");
        oreBlasting(FROST_SILVER_SMELTABLES, RecipeCategory.MISC, CookingBookCategory.MISC, ModItems.FROST_SILVER_INGOT.get(), 0.25f, 100, "frost_silver");
        shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.FROST_SILVER_BLOCK.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItems.FROST_SILVER_INGOT.get())
                .unlockedBy(getHasName(ModItems.FROST_SILVER_INGOT.get()), has(ModItems.FROST_SILVER_INGOT))
                .group("frost_silver_block")
                .save(output, "ascension:shaped/frost_silver_block_from_ingot");
        shapeless(RecipeCategory.MISC, ModItems.FROST_SILVER_INGOT.get(), 9)
                .requires(ModBlocks.FROST_SILVER_BLOCK)
                .unlockedBy(getHasName(ModBlocks.FROST_SILVER_BLOCK.get()), has(ModBlocks.FROST_SILVER_BLOCK))
                .group("frost_silver_ingot")
                .save(output, "ascension:shapeless/frost_silver_ingot_from_block");
        shaped(RecipeCategory.BUILDING_BLOCKS, ModItems.FROST_SILVER_INGOT.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItems.FROST_SILVER_NUGGET.get())
                .unlockedBy(getHasName(ModItems.FROST_SILVER_NUGGET.get()), has(ModItems.FROST_SILVER_NUGGET))
                .group("frost_silver_ingot")
                .save(output, "ascension:shaped/frost_silver_ingot_from_nugget");
        shapeless(RecipeCategory.MISC, ModItems.FROST_SILVER_NUGGET.get(), 9)
                .requires(ModItems.FROST_SILVER_INGOT)
                .unlockedBy(getHasName(ModItems.FROST_SILVER_INGOT.get()), has(ModItems.FROST_SILVER_INGOT))
                .group("frost_silver_nugget")
                .save(output, "ascension:shapeless/frost_silver_nugget_from_ingot");
        List<ItemLike> BLACK_IRON_SMELTABLES = List.of(ModBlocks.BLACK_IRON_ORE.get(), ModItems.RAW_BLACK_IRON.get());
        oreSmelting(BLACK_IRON_SMELTABLES, RecipeCategory.MISC, CookingBookCategory.MISC, ModItems.BLACK_IRON_INGOT.get(), 0.25f, 200, "black_iron");
        oreBlasting(BLACK_IRON_SMELTABLES, RecipeCategory.MISC, CookingBookCategory.MISC, ModItems.BLACK_IRON_INGOT.get(), 0.25f, 100, "black_iron");
        shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.BLACK_IRON_BLOCK.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItems.BLACK_IRON_INGOT.get())
                .unlockedBy(getHasName(ModItems.BLACK_IRON_INGOT.get()), has(ModItems.BLACK_IRON_INGOT))
                .group("black_iron_block")
                .save(output, "ascension:shaped/black_iron_block_from_ingot");
        shapeless(RecipeCategory.MISC, ModItems.BLACK_IRON_INGOT.get(), 9)
                .requires(ModBlocks.BLACK_IRON_BLOCK)
                .unlockedBy(getHasName(ModBlocks.BLACK_IRON_BLOCK.get()), has(ModBlocks.BLACK_IRON_BLOCK))
                .group("black_iron_ingot")
                .save(output, "ascension:shapeless/black_iron_ingot_from_block");
        shaped(RecipeCategory.BUILDING_BLOCKS, ModItems.BLACK_IRON_INGOT.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItems.BLACK_IRON_NUGGET.get())
                .unlockedBy(getHasName(ModItems.BLACK_IRON_NUGGET.get()), has(ModItems.BLACK_IRON_NUGGET))
                .group("black_iron_ingot")
                .save(output, "ascension:shaped/black_iron_ingot_from_nugget");
        shapeless(RecipeCategory.MISC, ModItems.BLACK_IRON_NUGGET.get(), 9)
                .requires(ModItems.BLACK_IRON_INGOT)
                .unlockedBy(getHasName(ModItems.BLACK_IRON_INGOT.get()), has(ModItems.BLACK_IRON_INGOT))
                .group("black_iron_nugget")
                .save(output, "ascension:shapeless/black_iron_nugget_from_ingot");

    }
    @Override
    protected <T extends AbstractCookingRecipe> void oreCooking(AbstractCookingRecipe.Factory<T> factory, List<ItemLike> smeltables,
                                                                RecipeCategory craftingCategory, CookingBookCategory cookingCategory, ItemLike result,
                                                                float experience, int cookingTime, String group, String fromDesc) {
        String folder = fromDesc.contains("blasting") ? "blasting" : "smelting";
        for(ItemLike itemlike : smeltables) {
            SimpleCookingRecipeBuilder.generic(Ingredient.of(itemlike), craftingCategory, cookingCategory, result, experience, cookingTime, factory).group(group).unlockedBy(getHasName(itemlike), has(itemlike))
                    .save(output, AscensionCraft.MOD_ID + ":" + folder + "/" + getItemName(result) + "_from_" + getItemName(itemlike));
        }
    }
}