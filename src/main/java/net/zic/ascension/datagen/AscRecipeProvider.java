package net.zic.ascension.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
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


        shaped(RecipeCategory.MISC, ModBlocks.ALCHEMY_FURNACE.get())
                .pattern("B B")
                .pattern("B B")
                .pattern("BBB")
                .define('B', ModItems.BLACK_IRON_INGOT.get())
                .unlockedBy(getHasName(ModItems.BLACK_IRON_INGOT.get()), has(ModItems.BLACK_IRON_INGOT))
                .group("black_iron")
                .save(output, "ascension:shaped/alchemy_furnace");
        shapeless(RecipeCategory.MISC, ModBlocks.CULTIVATION_SOIL.get(), 2)
                .requires(ModItems.SPIRITUAL_STONE, 2)
                .requires(Items.DIRT, 2)
                .unlockedBy(getHasName(ModItems.SPIRITUAL_STONE.get()), has(ModItems.SPIRITUAL_STONE))
                .group("cultivation_soil")
                .save(output, "ascension:shapeless/cultivation_soil");


        //Artifacts
        shaped(RecipeCategory.MISC, ModItems.TABLET_OF_DESTRUCTION_HUMAN.get(), 8)
                .pattern("GBG")
                .pattern("BGB")
                .pattern("GBG")
                .define('G', Items.IRON_INGOT)
                .define('B', ModItems.BLACK_IRON_INGOT.get())
                .unlockedBy(getHasName(ModItems.BLACK_IRON_INGOT.get()), has(ModItems.BLACK_IRON_INGOT))
                .group("TODH")
                .save(output, "ascension:shapeless/tablet_of_destruction_human");
        shaped(RecipeCategory.MISC, ModItems.TABLET_OF_DESTRUCTION_EARTH.get(), 4)
                .pattern("TTT")
                .pattern("THT")
                .pattern("TTT")
                .define('T', Items.GUNPOWDER)
                .define('H', ModItems.TABLET_OF_DESTRUCTION_HUMAN.get())
                .unlockedBy(getHasName(ModItems.TABLET_OF_DESTRUCTION_HUMAN.get()), has(ModItems.TABLET_OF_DESTRUCTION_HUMAN))
                .group("TODE")
                .save(output, "ascension:shapeless/tablet_of_destruction_earth");
        shaped(RecipeCategory.MISC, ModItems.TABLET_OF_DESTRUCTION_HEAVEN.get(), 4)
                .pattern("TTT")
                .pattern("THT")
                .pattern("TTT")
                .define('T', Items.GUNPOWDER)
                .define('H', ModItems.TABLET_OF_DESTRUCTION_EARTH.get())
                .unlockedBy(getHasName(ModItems.TABLET_OF_DESTRUCTION_EARTH.get()), has(ModItems.TABLET_OF_DESTRUCTION_EARTH))
                .group("TODHE")
                .save(output, "ascension:shapeless/tablet_of_destruction_heaven");
        shaped(RecipeCategory.MISC, ModItems.TABLET_OF_DESTRUCTION_ASCENDANT.get(), 4)
                .pattern("TTT")
                .pattern("THT")
                .pattern("TTT")
                .define('T', Items.GUNPOWDER)
                .define('H', ModItems.TABLET_OF_DESTRUCTION_HEAVEN.get())
                .unlockedBy(getHasName(ModItems.TABLET_OF_DESTRUCTION_HEAVEN.get()), has(ModItems.TABLET_OF_DESTRUCTION_HEAVEN))
                .group("TODA")
                .save(output, "ascension:shapeless/tablet_of_destruction_ascendant");


        //Items
        smithingTransform(
                ModItems.SPIRITUAL_STONE_UPGRADE_SMITHING_TEMPLATE.asItem(),
                Items.NETHERITE_AXE,
                Ingredient.of(ModItems.SPIRITUAL_STONE),
                RecipeCategory.COMBAT,
                ModItems.SPIRITUAL_STONE_AXE.get()
        );
        smithingTransform(
                ModItems.SPIRITUAL_STONE_UPGRADE_SMITHING_TEMPLATE.asItem(),
                Items.NETHERITE_SWORD,
                Ingredient.of(ModItems.SPIRITUAL_STONE),
                RecipeCategory.COMBAT,
                ModItems.SPIRITUAL_STONE_SWORD.get()
        );
        smithingTransform(
                ModItems.SPIRITUAL_STONE_UPGRADE_SMITHING_TEMPLATE.asItem(),
                Items.NETHERITE_PICKAXE,
                Ingredient.of(ModItems.SPIRITUAL_STONE),
                RecipeCategory.COMBAT,
                ModItems.SPIRITUAL_STONE_PICKAXE.get()
        );
        smithingTransform(
                ModItems.SPIRITUAL_STONE_UPGRADE_SMITHING_TEMPLATE.asItem(),
                Items.NETHERITE_HOE,
                Ingredient.of(ModItems.SPIRITUAL_STONE),
                RecipeCategory.COMBAT,
                ModItems.SPIRITUAL_STONE_HOE.get()
        );
        smithingTransform(
                ModItems.SPIRITUAL_STONE_UPGRADE_SMITHING_TEMPLATE.asItem(),
                Items.NETHERITE_SHOVEL,
                Ingredient.of(ModItems.SPIRITUAL_STONE),
                RecipeCategory.COMBAT,
                ModItems.SPIRITUAL_STONE_SHOVEL.get()
        );
        smithingTransform(
                ModItems.SPIRITUAL_STONE_UPGRADE_SMITHING_TEMPLATE.asItem(),
                Items.NETHERITE_SPEAR,
                Ingredient.of(ModItems.SPIRITUAL_STONE),
                RecipeCategory.COMBAT,
                ModItems.SPIRITUAL_STONE_SPEAR.get()
        );
        smithingTransform(
                ModItems.SPIRITUAL_STONE_UPGRADE_SMITHING_TEMPLATE.asItem(),
                ModItems.NETHERITE_BLADE.get(),
                Ingredient.of(ModItems.SPIRITUAL_STONE),
                RecipeCategory.COMBAT,
                ModItems.SPIRITUAL_STONE_BLADE.get()
        );
        smithingTransform(
                Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE,
                ModItems.DIAMOND_BLADE.get(),
                Ingredient.of(Items.NETHERITE_INGOT),
                RecipeCategory.COMBAT,
                ModItems.NETHERITE_BLADE.get()
        );


        shaped(RecipeCategory.COMBAT, ModItems.BLACK_IRON_SWORD.get())
                .pattern("T")
                .pattern("T")
                .pattern("S")
                .define('T', ModItems.BLACK_IRON_INGOT.get())
                .define('S', Items.STICK)
                .unlockedBy(getHasName(ModItems.BLACK_IRON_INGOT.get()), has(ModItems.BLACK_IRON_INGOT))
                .group("black_iron")
                .save(output, "ascension:shaped/black_iron_sword");
        shaped(RecipeCategory.COMBAT, ModItems.BLACK_IRON_BLADE.get())
                .pattern(" T ")
                .pattern("TT ")
                .pattern(" S ")
                .define('T', ModItems.BLACK_IRON_INGOT.get())
                .define('S', Items.STICK)
                .unlockedBy(getHasName(ModItems.BLACK_IRON_INGOT.get()), has(ModItems.BLACK_IRON_INGOT))
                .group("black_iron")
                .save(output, "ascension:shaped/black_iron_blade");
        shaped(RecipeCategory.COMBAT, ModItems.BLACK_IRON_AXE.get())
                .pattern("TT ")
                .pattern("TS ")
                .pattern(" S ")
                .define('T', ModItems.BLACK_IRON_INGOT.get())
                .define('S', Items.STICK)
                .unlockedBy(getHasName(ModItems.BLACK_IRON_INGOT.get()), has(ModItems.BLACK_IRON_INGOT))
                .group("black_iron")
                .save(output, "ascension:shaped/black_iron_axe");
        shaped(RecipeCategory.TOOLS, ModItems.BLACK_IRON_PICKAXE.get())
                .pattern("TTT")
                .pattern(" S ")
                .pattern(" S ")
                .define('T', ModItems.BLACK_IRON_INGOT.get())
                .define('S', Items.STICK)
                .unlockedBy(getHasName(ModItems.BLACK_IRON_INGOT.get()), has(ModItems.BLACK_IRON_INGOT))
                .group("black_iron")
                .save(output, "ascension:shaped/black_iron_pickaxe");
        shaped(RecipeCategory.TOOLS, ModItems.BLACK_IRON_SHOVEL.get())
                .pattern("T")
                .pattern("S")
                .pattern("S")
                .define('T', ModItems.BLACK_IRON_INGOT.get())
                .define('S', Items.STICK)
                .unlockedBy(getHasName(ModItems.BLACK_IRON_INGOT.get()), has(ModItems.BLACK_IRON_INGOT))
                .group("black_iron")
                .save(output, "ascension:shaped/black_iron_shovel");
        shaped(RecipeCategory.TOOLS, ModItems.BLACK_IRON_HOE.get())
                .pattern("TT")
                .pattern(" S")
                .pattern(" S")
                .define('T', ModItems.BLACK_IRON_INGOT.get())
                .define('S', Items.STICK)
                .unlockedBy(getHasName(ModItems.BLACK_IRON_INGOT.get()), has(ModItems.BLACK_IRON_INGOT))
                .group("black_iron")
                .save(output, "ascension:shaped/black_iron_hoe");
        shaped(RecipeCategory.COMBAT, ModItems.BLACK_IRON_SPEAR.get())
                .pattern("  T")
                .pattern(" S ")
                .pattern("S  ")
                .define('T', ModItems.BLACK_IRON_INGOT.get())
                .define('S', Items.STICK)
                .unlockedBy(getHasName(ModItems.BLACK_IRON_INGOT.get()), has(ModItems.BLACK_IRON_INGOT))
                .group("black_iron")
                .save(output, "ascension:shaped/black_iron_spear");
        shaped(RecipeCategory.COMBAT, ModItems.FROST_SILVER_SWORD.get())
                .pattern("T")
                .pattern("T")
                .pattern("S")
                .define('T', ModItems.FROST_SILVER_INGOT.get())
                .define('S', Items.STICK)
                .unlockedBy(getHasName(ModItems.FROST_SILVER_INGOT.get()), has(ModItems.FROST_SILVER_INGOT))
                .group("frost_silver")
                .save(output, "ascension:shaped/frost_silver_sword");
        shaped(RecipeCategory.COMBAT, ModItems.FROST_SILVER_BLADE.get())
                .pattern(" T ")
                .pattern("TT ")
                .pattern(" S ")
                .define('T', ModItems.FROST_SILVER_INGOT.get())
                .define('S', Items.STICK)
                .unlockedBy(getHasName(ModItems.FROST_SILVER_INGOT.get()), has(ModItems.FROST_SILVER_INGOT))
                .group("frost_silver")
                .save(output, "ascension:shaped/frost_silver_blade");
        shaped(RecipeCategory.COMBAT, ModItems.FROST_SILVER_AXE.get())
                .pattern("TT ")
                .pattern("TS ")
                .pattern(" S ")
                .define('T', ModItems.FROST_SILVER_INGOT.get())
                .define('S', Items.STICK)
                .unlockedBy(getHasName(ModItems.FROST_SILVER_INGOT.get()), has(ModItems.FROST_SILVER_INGOT))
                .group("frost_silver")
                .save(output, "ascension:shaped/frost_silver_axe");
        shaped(RecipeCategory.TOOLS, ModItems.FROST_SILVER_PICKAXE.get())
                .pattern("TTT")
                .pattern(" S ")
                .pattern(" S ")
                .define('T', ModItems.FROST_SILVER_INGOT.get())
                .define('S', Items.STICK)
                .unlockedBy(getHasName(ModItems.FROST_SILVER_INGOT.get()), has(ModItems.FROST_SILVER_INGOT))
                .group("frost_silver")
                .save(output, "ascension:shaped/frost_silver_pickaxe");
        shaped(RecipeCategory.TOOLS, ModItems.FROST_SILVER_SHOVEL.get())
                .pattern("T")
                .pattern("S")
                .pattern("S")
                .define('T', ModItems.FROST_SILVER_INGOT.get())
                .define('S', Items.STICK)
                .unlockedBy(getHasName(ModItems.FROST_SILVER_INGOT.get()), has(ModItems.FROST_SILVER_INGOT))
                .group("frost_silver")
                .save(output, "ascension:shaped/frost_silver_shovel");
        shaped(RecipeCategory.TOOLS, ModItems.FROST_SILVER_HOE.get())
                .pattern("TT")
                .pattern(" S")
                .pattern(" S")
                .define('T', ModItems.FROST_SILVER_INGOT.get())
                .define('S', Items.STICK)
                .unlockedBy(getHasName(ModItems.FROST_SILVER_INGOT.get()), has(ModItems.FROST_SILVER_INGOT))
                .group("frost_silver")
                .save(output, "ascension:shaped/frost_silver_hoe");
        shaped(RecipeCategory.COMBAT, ModItems.FROST_SILVER_SPEAR.get())
                .pattern("  T")
                .pattern(" S ")
                .pattern("S  ")
                .define('T', ModItems.FROST_SILVER_INGOT.get())
                .define('S', Items.STICK)
                .unlockedBy(getHasName(ModItems.FROST_SILVER_INGOT.get()), has(ModItems.FROST_SILVER_INGOT))
                .group("frost_silver")
                .save(output, "ascension:shaped/frost_silver_spear");
        shaped(RecipeCategory.COMBAT, ModItems.WOODEN_BLADE.get())
                .pattern(" T ")
                .pattern("TT ")
                .pattern(" S ")
                .define('T', ItemTags.PLANKS)
                .define('S', Items.STICK)
                .unlockedBy("has_planks", has(ItemTags.PLANKS))
                .group("blade")
                .save(output, "ascension:shaped/wooden_blade");
        shaped(RecipeCategory.COMBAT, ModItems.STONE_BLADE.get())
                .pattern(" T ")
                .pattern("TT ")
                .pattern(" S ")
                .define('T', Items.COBBLESTONE)
                .define('S', Items.STICK)
                .unlockedBy(getHasName(Items.COBBLESTONE), has(Items.COBBLESTONE))
                .group("blade")
                .save(output, "ascension:shaped/stone_blade");
        shaped(RecipeCategory.COMBAT, ModItems.COPPER_BLADE.get())
                .pattern(" T ")
                .pattern("TT ")
                .pattern(" S ")
                .define('T', Items.COPPER_INGOT)
                .define('S', Items.STICK)
                .unlockedBy(getHasName(Items.COPPER_INGOT), has(Items.COPPER_INGOT))
                .group("blade")
                .save(output, "ascension:shaped/copper_blade");
        shaped(RecipeCategory.COMBAT, ModItems.IRON_BLADE.get())
                .pattern(" T ")
                .pattern("TT ")
                .pattern(" S ")
                .define('T', Items.IRON_INGOT)
                .define('S', Items.STICK)
                .unlockedBy(getHasName(Items.IRON_INGOT), has(Items.IRON_INGOT))
                .group("blade")
                .save(output, "ascension:shaped/iron_blade");
        shaped(RecipeCategory.COMBAT, ModItems.GOLD_BLADE.get())
                .pattern(" T ")
                .pattern("TT ")
                .pattern(" S ")
                .define('T', Items.GOLD_INGOT)
                .define('S', Items.STICK)
                .unlockedBy(getHasName(Items.GOLD_INGOT), has(Items.GOLD_INGOT))
                .group("blade")
                .save(output, "ascension:shaped/gold_blade");
        shaped(RecipeCategory.COMBAT, ModItems.DIAMOND_BLADE.get())
                .pattern(" T ")
                .pattern("TT ")
                .pattern(" S ")
                .define('T', Items.DIAMOND)
                .define('S', Items.STICK)
                .unlockedBy(getHasName(Items.DIAMOND), has(Items.DIAMOND))
                .group("blade")
                .save(output, "ascension:shaped/diamond_blade");

    }


    protected void smithingTransform(Item template, Item base, Ingredient addition,
                                     RecipeCategory category, Item result) {
        SmithingTransformRecipeBuilder.smithing(
                Ingredient.of(template),
                Ingredient.of(base),
                addition,
                category,
                result
        ).unlocks(getHasName(base), this.has(base)).save(this.output, getItemName(result) + "_smithing");
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