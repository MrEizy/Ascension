package net.thejadeproject.ascension.datagen.builders;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.NonNullList;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.thejadeproject.ascension.recipe.LowHumanPillCauldronRecipe;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

public class PillCauldronRecipeBuilder implements RecipeBuilder {

    private static final int MAX_INPUT_ITEMS = 3;

    private final NonNullList<SizedIngredient> ingredients = NonNullList.create();
    private final ItemStack success;
    private final ItemStack fail;

    private double chance = 0.75D;
    private int minTemp = 200;
    private int maxTemp = 800;
    private int idealTemp = 500;
    private int recipeTime = 10;
    private int pillRealmMajor = 1;
    private String pillRealmMinor = "lower";
    private int purityMin = 10;
    private int purityMax = 40;
    private double bonusChance = 0.08D;

    private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();

    @Nullable
    private String group;

    private PillCauldronRecipeBuilder(ItemStack success, ItemStack fail) {
        this.success = success;
        this.fail = fail;
    }

    public static PillCauldronRecipeBuilder lowHuman(ItemLike success, ItemLike fail) {
        return lowHuman(success, 1, fail, 1);
    }

    public static PillCauldronRecipeBuilder lowHuman(ItemLike success, int successCount, ItemLike fail, int failCount) {
        return new PillCauldronRecipeBuilder(
                new ItemStack(success, successCount),
                new ItemStack(fail, failCount)
        );
    }

    public PillCauldronRecipeBuilder ingredient(ItemLike item, int count) {
        return ingredient(Ingredient.of(item), count);
    }

    public PillCauldronRecipeBuilder ingredient(Ingredient ingredient, int count) {
        if (this.ingredients.size() >= MAX_INPUT_ITEMS) {
            throw new IllegalStateException("Low Human Pill Cauldron recipes can only have up to 3 ingredients.");
        }

        if (count <= 0) {
            throw new IllegalArgumentException("Ingredient count must be greater than 0.");
        }

        this.ingredients.add(new SizedIngredient(ingredient, count));
        return this;
    }

    public PillCauldronRecipeBuilder chance(double chance) {
        this.chance = chance;
        return this;
    }

    public PillCauldronRecipeBuilder temperature(int minTemp, int maxTemp, int idealTemp) {
        this.minTemp = minTemp;
        this.maxTemp = maxTemp;
        this.idealTemp = idealTemp;
        return this;
    }

    public PillCauldronRecipeBuilder timeSeconds(int recipeTime) {
        this.recipeTime = recipeTime;
        return this;
    }

    public PillCauldronRecipeBuilder realm(int major, String minor) {
        this.pillRealmMajor = major;
        this.pillRealmMinor = minor;
        return this;
    }

    public PillCauldronRecipeBuilder purity(int min, int max) {
        this.purityMin = min;
        this.purityMax = max;
        return this;
    }

    public PillCauldronRecipeBuilder bonusChance(double bonusChance) {
        this.bonusChance = bonusChance;
        return this;
    }

    @Override
    public PillCauldronRecipeBuilder unlockedBy(String name, Criterion<?> criterion) {
        this.criteria.put(name, criterion);
        return this;
    }

    @Override
    public PillCauldronRecipeBuilder group(@Nullable String group) {
        this.group = group;
        return this;
    }

    @Override
    public Item getResult() {
        return this.success.getItem();
    }

    @Override
    public void save(RecipeOutput output, ResourceLocation id) {
        validate(id);

        Advancement.Builder advancement = output.advancement()
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
                .rewards(AdvancementRewards.Builder.recipe(id))
                .requirements(AdvancementRequirements.Strategy.OR);

        this.criteria.forEach(advancement::addCriterion);

        NonNullList<SizedIngredient> copiedIngredients = NonNullList.create();
        copiedIngredients.addAll(this.ingredients);

        LowHumanPillCauldronRecipe recipe = new LowHumanPillCauldronRecipe(
                copiedIngredients,
                this.success.copy(),
                this.fail.copy(),
                this.chance,
                this.minTemp,
                this.maxTemp,
                this.idealTemp,
                this.recipeTime,
                this.pillRealmMajor,
                this.pillRealmMinor,
                this.purityMin,
                this.purityMax,
                this.bonusChance
        );

        output.accept(id, recipe, advancement.build(id.withPrefix("recipes/")));
    }

    private void validate(ResourceLocation id) {
        if (this.ingredients.isEmpty()) {
            throw new IllegalStateException("No ingredients defined for Pill Cauldron recipe: " + id);
        }

        if (this.minTemp > this.maxTemp) {
            throw new IllegalStateException("min_temp cannot be greater than max_temp for recipe: " + id);
        }

        if (this.idealTemp < this.minTemp || this.idealTemp > this.maxTemp) {
            throw new IllegalStateException("ideal_temp must be inside [min_temp, max_temp] for recipe: " + id);
        }

        if (this.recipeTime <= 0) {
            throw new IllegalStateException("recipe_time must be greater than 0 for recipe: " + id);
        }

        if (this.purityMin > this.purityMax) {
            throw new IllegalStateException("purity_min cannot be greater than purity_max for recipe: " + id);
        }

        if (this.chance < 0.0D || this.chance > 1.0D) {
            throw new IllegalStateException("chance must be between 0.0 and 1.0 for recipe: " + id);
        }

        if (this.bonusChance < 0.0D || this.bonusChance > 1.0D) {
            throw new IllegalStateException("bonus_chance must be between 0.0 and 1.0 for recipe: " + id);
        }
    }
}