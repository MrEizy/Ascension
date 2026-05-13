package net.thejadeproject.ascension.common.items.herbs;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Resolves herb bonus effects and calculates quality/age purity contributions.
 *
 * ── Herb quality purity bonus ─────────────────────────────────────────────
 * calcHerbPurityBonus(inputs) sums the purity bonus from all input stacks.
 * Each pedestal slot contributes its herb's quality+age bonus independently,
 * so using multiple high-quality herbs compounds the bonus.
 *
 * ── Realm upgrade from herbs ──────────────────────────────────────────────
 * calcHerbRealmUpgradeChance(inputs) returns the highest realm-upgrade chance
 * across all input stacks (doesn't stack — only the best herb counts).
 *
 * ── Fire + Ice combo logic ────────────────────────────────────────────────
 * If both a [Fire] and [Ice] herb are in the same craft, the bonus slot is
 * contested: 10% → [Fire & Ice], 45% → [Fire], 45% → [Ice].
 */
public class HerbBonusEffects {

    // ── Bonus IDs ─────────────────────────────────────────────────
    public static final String FIRE_IMPERVIOUSNESS        = "ascension:fire_imperviousness";
    public static final String ICE_IMPERVIOUSNESS         = "ascension:ice_imperviousness";
    public static final String TEMPERATURE_IMPERVIOUSNESS = "ascension:temperature_imperviousness";
    public static final String QI_NOURISHING              = "ascension:qi_nourishing";
    public static final String BODY_STRENGTHENING         = "ascension:body_strengthening";
    public static final String SPIRIT_CLARITY             = "ascension:spirit_clarity";

    // ── Tags ──────────────────────────────────────────────────────
    private static final TagKey<net.minecraft.world.item.Item> TAG_FIRE =
            TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("ascension", "herb_fire_affinity"));
    private static final TagKey<net.minecraft.world.item.Item> TAG_ICE =
            TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("ascension", "herb_ice_affinity"));
    private static final TagKey<net.minecraft.world.item.Item> TAG_QI =
            TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("ascension", "herb_qi_nourishing"));
    private static final TagKey<net.minecraft.world.item.Item> TAG_BODY =
            TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("ascension", "herb_body_strengthening"));
    private static final TagKey<net.minecraft.world.item.Item> TAG_SPIRIT =
            TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("ascension", "herb_spirit_clarity"));

    // ── Quality / Age purity bonus ────────────────────────────────

    /**
     * Sums the flat purity bonus from all input herb stacks.
     * Each stack contributes independently based on its Quality + Age data.
     */
    public static int calcHerbPurityBonus(List<ItemStack> inputs) {
        int total = 0;
        for (ItemStack stack : inputs) {
            total += HerbQuality.getPurityBonus(stack);
        }
        return total;
    }

    /**
     * Returns the highest realm-upgrade chance from any single input herb.
     * Only Peak-quality herbs contribute; the best one wins (no stacking).
     */
    public static double calcHerbRealmUpgradeChance(List<ItemStack> inputs) {
        double best = 0.0;
        for (ItemStack stack : inputs) {
            best = Math.max(best, HerbQuality.getRealmUpgradeChance(stack));
        }
        return best;
    }

    // ── Bonus effect roll ─────────────────────────────────────────

    /**
     * Rolls the bonus effect for the craft considering fire/ice combo logic.
     */
    public static String rollBonusEffect(List<ItemStack> inputs, double bonusChance) {
        if (ThreadLocalRandom.current().nextDouble() > bonusChance) return "";

        boolean hasFire = false, hasIce = false;
        List<String> eligible = new ArrayList<>();

        for (ItemStack stack : inputs) {
            if (stack.isEmpty()) continue;
            if (stack.is(TAG_FIRE)) hasFire = true;
            if (stack.is(TAG_ICE))  hasIce  = true;
            if (stack.is(TAG_QI)    && !eligible.contains(QI_NOURISHING))    eligible.add(QI_NOURISHING);
            if (stack.is(TAG_BODY)  && !eligible.contains(BODY_STRENGTHENING)) eligible.add(BODY_STRENGTHENING);
            if (stack.is(TAG_SPIRIT)&& !eligible.contains(SPIRIT_CLARITY))   eligible.add(SPIRIT_CLARITY);
        }

        if (hasFire && hasIce) {
            double roll = ThreadLocalRandom.current().nextDouble();
            if      (roll < 0.10) eligible.add(TEMPERATURE_IMPERVIOUSNESS);
            else if (roll < 0.55) eligible.add(FIRE_IMPERVIOUSNESS);
            else                  eligible.add(ICE_IMPERVIOUSNESS);
        } else {
            if (hasFire) eligible.add(FIRE_IMPERVIOUSNESS);
            if (hasIce)  eligible.add(ICE_IMPERVIOUSNESS);
        }

        if (eligible.isEmpty()) return "";
        return eligible.get(ThreadLocalRandom.current().nextInt(eligible.size()));
    }
}