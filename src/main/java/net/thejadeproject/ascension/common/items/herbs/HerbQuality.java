package net.thejadeproject.ascension.common.items.herbs;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.thejadeproject.ascension.common.items.data_components.ModDataComponents;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Herb Quality and Age system.
 *
 * ── Quality ───────────────────────────────────────────────────────────────
 * Rolled randomly when the crop first reaches max age. Four tiers:
 *
 *   0  Basic     (50% chance)   — minor purity bonus
 *   1  Average   (30% chance)   — moderate purity bonus
 *   2  Advanced  (15% chance)   — large purity bonus
 *   3  Peak      ( 5% chance)   — maximum purity bonus + major realm upgrade chance
 *
 * ── Age ───────────────────────────────────────────────────────────────────
 * "Age ticks" = how long (in game ticks) the crop spent at max age before harvest.
 * The crop records the world game-time when it first reaches max age.
 * At harvest, the item receives (currentTime - grownSince) as HERB_AGE_TICKS.
 * Nothing ticks — the number is computed lazily at harvest. Zero server overhead.
 *
 *   Reference: 100,000 ticks ≈ 100 in-game years at the default day cycle.
 *   (1 in-game day = 24,000 ticks → 100,000 / 240 ≈ 416 real hours at 1:1)
 *   Practically, "age" just rewards players who leave crops standing after maturity.
 *
 * ── Purity contribution ───────────────────────────────────────────────────
 * When the cauldron reads input herbs it sums their quality/age bonuses and
 * adds them to the purity roll. See HerbBonusEffects.calcHerbPurityBonus().
 *
 * Multiple herbs of the same type contribute independently — using 3× Average
 * ironwood sprouts each adds their bonus, so stacking herbs of the same quality
 * is worth more than one high-quality herb.
 */
public class HerbQuality {

    // ── Quality tier constants ────────────────────────────────────
    public static final int BASIC    = 0;
    public static final int AVERAGE  = 1;
    public static final int ADVANCED = 2;
    public static final int PEAK     = 3;

    // ── Age thresholds (in ticks at max age) ─────────────────────
    /** Young: just reached max age — no age bonus */
    public static final long AGE_YOUNG    = 0L;
    /** Mature: ~100 in-game years at max age */
    public static final long AGE_MATURE   = 100_000L;
    /** Elder: ~500 in-game years at max age */
    public static final long AGE_ELDER    = 500_000L;
    /** Ancient: ~1000 in-game years at max age */
    public static final long AGE_ANCIENT  = 1_000_000L;

    // ── Quality names and colors ──────────────────────────────────
    public static String getQualityName(int quality) {
        return switch (quality) {
            case AVERAGE  -> "Average";
            case ADVANCED -> "Advanced";
            case PEAK     -> "Peak";
            default       -> "Basic";
        };
    }

    public static ChatFormatting getQualityColor(int quality) {
        return switch (quality) {
            case AVERAGE  -> ChatFormatting.GOLD;
            case ADVANCED -> ChatFormatting.GREEN;
            case PEAK     -> ChatFormatting.AQUA;
            default       -> ChatFormatting.DARK_RED;
        };
    }

    // ── Age display ───────────────────────────────────────────────

    /**
     * Converts raw ticks at max age into a human-readable "years" number.
     * Scale: 100,000 ticks = 100 years, so 1 tick = 0.001 years.
     * Result is always shown as a whole number (rounded down).
     *
     * Examples:
     *   0 ticks         →   0 years
     *   100,000 ticks   → 100 years
     *   1,000,000 ticks → 1000 years
     */
    public static long ticksToYears(long ageTicks) {
        // 100,000 ticks = 100 years → 1,000 ticks = 1 year
        return ageTicks / 1_000L;
    }

    public static ChatFormatting getAgeColor(long ageTicks) {
        if (ageTicks >= AGE_ANCIENT) return ChatFormatting.LIGHT_PURPLE;
        if (ageTicks >= AGE_ELDER)   return ChatFormatting.AQUA;
        if (ageTicks >= AGE_MATURE)  return ChatFormatting.YELLOW;
        return ChatFormatting.GRAY;
    }

    // ── Random quality roll ───────────────────────────────────────
    /**
     * Rolls a quality tier when a crop first reaches max age.
     * Basic 50%, Average 30%, Advanced 15%, Peak 5%.
     */
    public static int rollQuality() {
        double roll = ThreadLocalRandom.current().nextDouble();
        if (roll < 0.05) return PEAK;
        if (roll < 0.20) return ADVANCED;
        if (roll < 0.50) return AVERAGE;
        return BASIC;
    }

    // ── Purity bonus per herb stack ───────────────────────────────
    /**
     * Returns the flat purity bonus that one herb ItemStack contributes to a craft.
     *
     * Quality bonus:
     *   Basic    +0
     *   Average  +3
     *   Advanced +8
     *   Peak     +15
     *
     * Age multiplier on top of quality bonus:
     *   Young    ×1.0
     *   Mature   ×1.3
     *   Elder    ×1.6
     *   Ancient  ×2.0
     *
     * The result is the bonus for ONE stack regardless of count — the recipe
     * ingredient amount is handled by the recipe system, not here. Each
     * distinct pedestal slot contributes its herb's bonus independently.
     */
    public static int getPurityBonus(ItemStack stack) {
        if (stack.isEmpty()) return 0;

        Integer qualityComp = stack.get(ModDataComponents.HERB_QUALITY.get());
        Long    ageTicks    = stack.get(ModDataComponents.HERB_AGE_TICKS.get());

        int quality  = (qualityComp != null) ? qualityComp : BASIC;
        long age     = (ageTicks    != null) ? ageTicks    : 0L;

        int baseBonus = switch (quality) {
            case PEAK     -> 15;
            case ADVANCED -> 8;
            case AVERAGE  -> 3;
            default       -> 0;
        };

        float ageMultiplier;
        if      (age >= AGE_ANCIENT) ageMultiplier = 2.0f;
        else if (age >= AGE_ELDER)   ageMultiplier = 1.6f;
        else if (age >= AGE_MATURE)  ageMultiplier = 1.3f;
        else                          ageMultiplier = 1.0f;

        return (int)(baseBonus * ageMultiplier);
    }

    /**
     * Returns the chance (0.0–1.0) that a Peak-quality herb contributes
     * a major realm upgrade bonus during crafting.
     * Only Peak quality + Ancient age has a non-zero chance.
     *
     *   Peak + Young   → 0%
     *   Peak + Mature  → 5%
     *   Peak + Elder   → 12%
     *   Peak + Ancient → 20%
     */
    public static double getRealmUpgradeChance(ItemStack stack) {
        if (stack.isEmpty()) return 0.0;

        Integer qualityComp = stack.get(ModDataComponents.HERB_QUALITY.get());
        Long    ageTicks    = stack.get(ModDataComponents.HERB_AGE_TICKS.get());

        int  quality = (qualityComp != null) ? qualityComp : BASIC;
        long age     = (ageTicks    != null) ? ageTicks    : 0L;

        if (quality < PEAK) return 0.0;

        if (age >= AGE_ANCIENT) return 0.20;
        if (age >= AGE_ELDER)   return 0.12;
        if (age >= AGE_MATURE)  return 0.05;
        return 0.0;
    }

    // ── Tooltip lines ─────────────────────────────────────────────
    /**
     * Appends Quality and Age tooltip lines to the given list.
     * Age is shown as a numerical year value derived from ticks.
     */
    public static void appendHerbTooltip(ItemStack stack, List<Component> tooltip) {
        Integer qualityComp = stack.get(ModDataComponents.HERB_QUALITY.get());
        Long    ageTicks    = stack.get(ModDataComponents.HERB_AGE_TICKS.get());

        if (qualityComp == null && ageTicks == null) return;

        int  quality = (qualityComp != null) ? qualityComp : BASIC;
        long age     = (ageTicks    != null) ? ageTicks    : 0L;
        long years   = ticksToYears(age);

        tooltip.add(
                Component.literal("Quality: ")
                        .withStyle(ChatFormatting.YELLOW)
                        .append(Component.literal(getQualityName(quality))
                                .withStyle(getQualityColor(quality)))
        );
        tooltip.add(
                Component.literal("Age: ")
                        .withStyle(ChatFormatting.YELLOW)
                        .append(Component.literal(years + " years")
                                .withStyle(getAgeColor(age)))
        );
    }
}