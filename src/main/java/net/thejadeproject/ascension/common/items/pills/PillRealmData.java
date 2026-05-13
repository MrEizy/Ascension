package net.thejadeproject.ascension.common.items.pills;

import net.minecraft.ChatFormatting;

/**
 * Central data class for pill realm names, purity grades, and bonus display text.
 *
 * ── Purity Grade system ───────────────────────────────────────────────────
 * The numeric purity (1-100) maps to a named grade shown in tooltips.
 * The number is kept for all game logic; only the tooltip shows the name.
 *
 *   1  – 30   Basic      (dark red)
 *   31 – 70   Average    (gold)
 *   71 – 89   Advanced   (green)
 *   90 – 100  Peak       (aqua)
 *
 * PILL_MINOR_REALM data component is removed. All code that previously
 * wrote or read minor realm should use getPurityGrade() instead.
 *
 * ── Major Realm names (1-9) ───────────────────────────────────────────────
 * Rename here freely.
 */
public class PillRealmData {

    // ── Major realm names ─────────────────────────────────────────
    private static final String[] MAJOR_REALM_NAMES = {
            "Mortal",       // 1
            "Spirit",       // 2
            "Earth",        // 3
            "Sky",          // 4
            "Heaven",       // 5
            "Profound",     // 6
            "Divine",       // 7
            "Immortal",     // 8
            "Transcendent"  // 9
    };

    // ── Purity grade names ────────────────────────────────────────
    public static final String GRADE_BASIC    = "Basic";
    public static final String GRADE_AVERAGE  = "Average";
    public static final String GRADE_ADVANCED = "Advanced";
    public static final String GRADE_PEAK     = "Peak";

    /**
     * Returns the purity grade name for a given numeric purity value.
     *
     *   1  – 30  → Basic
     *   31 – 70  → Average
     *   71 – 89  → Advanced
     *   90 – 100 → Peak
     */
    public static String getPurityGrade(int purity) {
        if (purity >= 90) return GRADE_PEAK;
        if (purity >= 71) return GRADE_ADVANCED;
        if (purity >= 31) return GRADE_AVERAGE;
        return GRADE_BASIC;
    }

    /**
     * Returns the ChatFormatting colour for a purity grade name.
     */
    public static ChatFormatting getPurityGradeColor(int purity) {
        if (purity >= 90) return ChatFormatting.AQUA;
        if (purity >= 71) return ChatFormatting.GREEN;
        if (purity >= 31) return ChatFormatting.GOLD;
        return ChatFormatting.DARK_RED;
    }

    public static String getMajorRealmName(int majorRealm) {
        if (majorRealm < 1 || majorRealm > 9) return "Unknown";
        return MAJOR_REALM_NAMES[majorRealm - 1];
    }

    public static String getFullRealmDisplay(int majorRealm, int purity) {
        return getMajorRealmName(majorRealm) + " — " + getPurityGrade(purity);
    }

    //TODO setup a registry for pill effects...

    // ── Bonus effect display strings ──────────────────────────────
    public static String getBonusEffectDisplay(String id) {
        if (id == null || id.isEmpty()) return "";
        return switch (id) {
            case "ascension:fire_imperviousness"        -> "[Fire] Impervious to fire and lava";
            case "ascension:ice_imperviousness"         -> "[Ice] Impervious to freezing; walk on powder snow";
            case "ascension:temperature_imperviousness" -> "[Fire & Ice] Impervious to temperature; walk on water";
            case "ascension:qi_nourishing"              -> "Qi Nourishing: Increased Qi regeneration";
            case "ascension:body_strengthening"         -> "Body Strengthening: Enhanced physical resilience";
            case "ascension:spirit_clarity"             -> "Spirit Clarity: Improved skill cooldown recovery";
            default -> "Unknown Bonus: " + id;
        };
    }
}