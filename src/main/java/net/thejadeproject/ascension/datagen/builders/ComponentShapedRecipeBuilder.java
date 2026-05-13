package net.thejadeproject.ascension.datagen.builders;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ComponentShapedRecipeBuilder implements RecipeBuilder {
    private final RecipeCategory category;
    private final ItemStack result;
    private final Map<Character, Ingredient> key = new LinkedHashMap<>();
    private final List<String> pattern;
    private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();
    private String group = "";

    private ComponentShapedRecipeBuilder(RecipeCategory category, ItemStack result, List<String> pattern) {
        this.category = category;
        this.result = result;
        this.pattern = pattern;
    }

    public static ComponentShapedRecipeBuilder shaped(RecipeCategory category, ItemStack result, String... pattern) {
        return new ComponentShapedRecipeBuilder(category, result, List.of(pattern));
    }

    public ComponentShapedRecipeBuilder define(char symbol, ItemLike item) {
        return define(symbol, Ingredient.of(item));
    }

    public ComponentShapedRecipeBuilder define(char symbol, TagKey<Item> tag) {
        return define(symbol, Ingredient.of(tag));
    }

    public ComponentShapedRecipeBuilder define(char symbol, Ingredient ingredient) {
        key.put(symbol, ingredient);
        return this;
    }

    @Override
    public RecipeBuilder unlockedBy(String name, Criterion<?> criterion) {
        criteria.put(name, criterion);
        return this;
    }

    @Override
    public RecipeBuilder group(@Nullable String group) {
        this.group = group;
        return this;
    }

    @Override
    public Item getResult() {
        return result.getItem();
    }

    @Override
    public void save(RecipeOutput output, ResourceLocation id) {
        ShapedRecipePattern shapedPattern = ShapedRecipePattern.of(key, pattern.toArray(new String[0]));
        ShapedRecipe recipe = new ShapedRecipe(group, CraftingBookCategory.MISC, shapedPattern, result, true);

        Advancement.Builder advancement = output.advancement()
                .parent(ROOT_RECIPE_ADVANCEMENT)
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
                .rewards(AdvancementRewards.Builder.recipe(id))
                .requirements(AdvancementRequirements.Strategy.OR);

        criteria.forEach(advancement::addCriterion);

        output.accept(id, recipe, advancement.build(id.withPrefix("recipes/" + category.getFolderName() + "/")));
    }
}