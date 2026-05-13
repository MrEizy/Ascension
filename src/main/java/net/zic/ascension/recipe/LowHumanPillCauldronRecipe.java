package net.zic.ascension.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Recipe for the Mortal-tier Pill Cauldron multiblock.
 *
 * ── JSON fields ─────────────────────────────────────────────────────────────
 * {
 *   "ingredients":      [ { "item": "...", "count": 1 }, ... ],  // up to 3
 *   "success":          { "id": "...", "count": 1 },
 *   "fail":             { "id": "...", "count": 1 },
 *   "chance":           0.75,
 *   "min_temp":         300,     // temp must be >= this to progress
 *   "max_temp":         700,     // temp must be <= this to progress
 *   "ideal_temp":       500,     // closest to this = highest purity + best minor realm
 *   "recipe_time":      10,      // seconds temp must stay in [min, max]
 *   "pill_realm_major": 1,
 *   "pill_realm_minor": "lower", // baseline minor realm (overridden by ideal_temp precision)
 *   "purity_min":       10,
 *   "purity_max":       40,
 *   "bonus_chance":     0.08
 * }
 *
 * ── Ideal Temperature & Minor Realm ─────────────────────────────────────────
 * ideal_temp sits inside [min_temp, max_temp].
 * The closer the final temperature is to ideal_temp, the higher the purity
 * and the better the minor realm:
 *
 *   precision 0.00–0.33 (far from ideal)  → minor realm stays at pill_realm_minor (base)
 *   precision 0.33–0.66 (moderate)        → one tier above base (lower→middle, middle→peak)
 *   precision 0.66–1.00 (near ideal)      → two tiers above base (lower→peak)
 *
 * "base" is the recipe-defined pill_realm_minor and acts as the floor — you can
 * never go BELOW it, only above it with good temperature control.
 */
public class LowHumanPillCauldronRecipe implements Recipe<PillCauldronInput> {

    private static final int MAX_INPUT_ITEMS = 3;

    final NonNullList<SizedIngredient> ingredients;
    final ItemStack success;
    final ItemStack fail;
    final double chance;
    final int    minTemp;
    final int    maxTemp;
    final int    idealTemp;
    final int    recipeTime;
    final int    pillRealmMajor;
    final String pillRealmMinor;
    final int    purityMin;
    final int    purityMax;
    final double bonusChance;

    public LowHumanPillCauldronRecipe(NonNullList<SizedIngredient> ingredients,
                                      ItemStack success, ItemStack fail,
                                      double chance,
                                      int minTemp, int maxTemp, int idealTemp, int recipeTime,
                                      int pillRealmMajor, String pillRealmMinor,
                                      int purityMin, int purityMax,
                                      double bonusChance) {
        this.ingredients    = ingredients;
        this.success        = success;
        this.fail           = fail;
        this.chance         = chance;
        this.minTemp        = minTemp;
        this.maxTemp        = maxTemp;
        this.idealTemp      = idealTemp;
        this.recipeTime     = recipeTime;
        this.pillRealmMajor = pillRealmMajor;
        this.pillRealmMinor = pillRealmMinor;
        this.purityMin      = purityMin;
        this.purityMax      = purityMax;
        this.bonusChance    = bonusChance;
    }

    // ── Getters ──────────────────────────────────────────────────
    public int    getMinTemp()         { return minTemp; }
    public int    getMaxTemp()         { return maxTemp; }
    public int    getIdealTemp()       { return idealTemp; }
    public int    getRecipeTime()      { return recipeTime; }
    public int    getRecipeTimeTicks() { return recipeTime * 20; }
    public ItemStack getSuccess()      { return success; }
    public ItemStack getFail()         { return fail; }
    public double getChance()          { return chance; }
    public int    getPillRealmMajor()  { return pillRealmMajor; }
    public String getPillRealmMinor()  { return pillRealmMinor; }
    public int    getPurityMin()       { return purityMin; }
    public int    getPurityMax()       { return purityMax; }
    public double getBonusChance()     { return bonusChance; }
    public NonNullList<SizedIngredient> getSizedIngredients() { return ingredients; }

    /**
     * Returns 0.0–1.0 representing how close {@code temp} is to {@code idealTemp}
     * within the valid range. 1.0 = exactly ideal, 0.0 = at min or max edge.
     */
    public float getIdealPrecision(int temp) {
        if (temp < minTemp || temp > maxTemp) return 0f;
        int halfRange = Math.max(1, Math.max(idealTemp - minTemp, maxTemp - idealTemp));
        return 1f - Math.min(1f, Math.abs(temp - idealTemp) / (float) halfRange);
    }


    /**
     * Calculates purity from the final-second temperature.
     *
     * @param temp           Current flame stand temperature when craft finishes.
     * @param condenserBonus Flat bonus from an active Spirit Condenser.
     */
    public int calcPurity(int temp, int condenserBonus) {
        float precision = getIdealPrecision(temp);
        int range       = purityMax - purityMin;
        int base        = purityMin + (int)(range * precision);
        int jitter      = ThreadLocalRandom.current().nextInt(-3, 4);
        return Math.min(100, Math.max(1, base + jitter + condenserBonus));
    }

    // ── Recipe interface ──────────────────────────────────────────

    @Override
    public boolean matches(@NotNull PillCauldronInput input, Level level) {
        if (level.isClientSide()) return false;
        int temp = input.getCurrentHeat();
        if (temp < minTemp || temp > maxTemp) return false;
        if (input.items.isEmpty()) return false;

        for (int i = 0; i < MAX_INPUT_ITEMS; i++) {
            ItemStack item = input.getItem(i);
            if (i < ingredients.size()) {
                SizedIngredient req = ingredients.get(i);
                if (item == null || !req.test(item) || item.getCount() < req.count()) return false;
            } else {
                if (item != null && !item.isEmpty()) return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack assemble(PillCauldronInput input, HolderLookup.Provider provider) {
        return ThreadLocalRandom.current().nextDouble() < chance ? success.copy() : fail.copy();
    }

    @Override public boolean canCraftInDimensions(int i, int j) { return false; }
    @Override public ItemStack getResultItem(HolderLookup.Provider p) { return success; }
    @Override public RecipeSerializer<?> getSerializer() { return ModRecipes.CAULDRON_LOW_HUMAN_SERIALIZER.get(); }
    @Override public RecipeType<?> getType()             { return ModRecipes.CAULDRON_LOW_HUMAN_TYPE.get(); }


    // ── Serializer ────────────────────────────────────────────────

    public static class Serializer implements RecipeSerializer<LowHumanPillCauldronRecipe> {

        public static final MapCodec<LowHumanPillCauldronRecipe> CODEC =
                RecordCodecBuilder.mapCodec(inst -> inst.group(
                        SizedIngredient.FLAT_CODEC.listOf()
                                .fieldOf("ingredients")
                                .flatXmap(
                                        inputs -> {
                                            if (inputs.isEmpty()) return DataResult.error(() -> "No ingredients");
                                            if (inputs.size() > MAX_INPUT_ITEMS) return DataResult.error(() -> "Too many ingredients");
                                            NonNullList<SizedIngredient> list = NonNullList.create();
                                            list.addAll(inputs);
                                            return DataResult.success(list);
                                        },
                                        DataResult::success
                                ).forGetter(r -> r.ingredients),
                        ItemStack.CODEC.fieldOf("success").forGetter(r -> r.success),
                        ItemStack.CODEC.fieldOf("fail").forGetter(r -> r.fail),
                        Codec.DOUBLE.fieldOf("chance").forGetter(r -> r.chance),
                        Codec.INT.optionalFieldOf("min_temp",   200).forGetter(r -> r.minTemp),
                        Codec.INT.optionalFieldOf("max_temp",   800).forGetter(r -> r.maxTemp),
                        Codec.INT.optionalFieldOf("ideal_temp", 500).forGetter(r -> r.idealTemp),
                        Codec.INT.optionalFieldOf("recipe_time", 10).forGetter(r -> r.recipeTime),
                        Codec.INT.optionalFieldOf("pill_realm_major", 1).forGetter(r -> r.pillRealmMajor),
                        Codec.STRING.optionalFieldOf("pill_realm_minor", "lower").forGetter(r -> r.pillRealmMinor),
                        Codec.INT.optionalFieldOf("purity_min", 10).forGetter(r -> r.purityMin),
                        Codec.INT.optionalFieldOf("purity_max", 40).forGetter(r -> r.purityMax),
                        Codec.DOUBLE.optionalFieldOf("bonus_chance", 0.08).forGetter(r -> r.bonusChance)
                ).apply(inst, LowHumanPillCauldronRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, LowHumanPillCauldronRecipe> STREAM_CODEC =
                StreamCodec.of(Serializer::toNetwork, Serializer::fromNetwork);

        @Override public MapCodec<LowHumanPillCauldronRecipe> codec() { return CODEC; }
        @Override public StreamCodec<RegistryFriendlyByteBuf, LowHumanPillCauldronRecipe> streamCodec() { return STREAM_CODEC; }

        private static LowHumanPillCauldronRecipe fromNetwork(RegistryFriendlyByteBuf buf) {
            int size = buf.readVarInt();
            NonNullList<SizedIngredient> ings = NonNullList.withSize(size, new SizedIngredient(Ingredient.EMPTY, 1));
            ings.replaceAll(i -> SizedIngredient.STREAM_CODEC.decode(buf));
            ItemStack success   = ItemStack.STREAM_CODEC.decode(buf);
            ItemStack fail      = ItemStack.STREAM_CODEC.decode(buf);
            double  chance      = buf.readDouble();
            int     minTemp     = buf.readInt();
            int     maxTemp     = buf.readInt();
            int     idealTemp   = buf.readInt();
            int     recipeTime  = buf.readInt();
            int     major       = buf.readInt();
            String  minor       = buf.readUtf();
            int     purityMin   = buf.readInt();
            int     purityMax   = buf.readInt();
            double  bonusChance = buf.readDouble();
            return new LowHumanPillCauldronRecipe(ings, success, fail, chance,
                    minTemp, maxTemp, idealTemp, recipeTime,
                    major, minor, purityMin, purityMax, bonusChance);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buf, LowHumanPillCauldronRecipe r) {
            buf.writeVarInt(r.ingredients.size());
            for (SizedIngredient ing : r.ingredients) SizedIngredient.STREAM_CODEC.encode(buf, ing);
            ItemStack.STREAM_CODEC.encode(buf, r.success);
            ItemStack.STREAM_CODEC.encode(buf, r.fail);
            buf.writeDouble(r.chance);
            buf.writeInt(r.minTemp);
            buf.writeInt(r.maxTemp);
            buf.writeInt(r.idealTemp);
            buf.writeInt(r.recipeTime);
            buf.writeInt(r.pillRealmMajor);
            buf.writeUtf(r.pillRealmMinor);
            buf.writeInt(r.purityMin);
            buf.writeInt(r.purityMax);
            buf.writeDouble(r.bonusChance);
        }
    }
}