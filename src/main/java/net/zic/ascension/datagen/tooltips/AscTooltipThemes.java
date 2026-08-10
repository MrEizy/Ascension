package net.zic.ascension.datagen.tooltips;

import net.zic.zenithlib.tooltip.api.ZenithTooltipTheme;
import net.zic.zenithlib.tooltip.api.builder.ZenithTooltipThemeBuilder;

/** Ascension-owned tooltip themes. */
public final class AscTooltipThemes {
    private AscTooltipThemes() {}

    public static void artifactThemes(ZenithTooltipThemeBuilder theme) {
        theme.colors(
                        "#0F0907E6",
                        "#F0B85BFF",
                        "#5B1B18FF",
                        "#F7E9D2FF",
                        "#E8B85FFF",
                        "#A8937BFF",
                        "#7EE08EFF",
                        "#F2C15DFF",
                        "#F06B5FFF"
                )
                .layout(7, 258, 226, 3, 1)
                .iconHolder(ZenithTooltipTheme.Shape.GEM, 31, 2, 8, "accent", "background", 96)
                .barStyle(5, 2, "border_bottom", 74, "accent", 1, 238)
                .badgeStyle(6, 1, 1, 224)
                .dividerStyle(1, 2, 3, "accent", ZenithTooltipTheme.Decoration.CENTER_RUNE)
                .frameStyle(ZenithTooltipTheme.CornerDecoration.RUNE, 6, 2, "accent", true, 2, "border_bottom", 118)
                .headerStyle(ZenithTooltipTheme.Ornament.SMALL_DIAMONDS, "accent")
                .backgroundStyle(ZenithTooltipTheme.Pattern.RUNES, "accent", 16, 12);
    }

    public static void physiqueEssence(ZenithTooltipThemeBuilder theme) {
        theme.colors(
                        "#100A07E6",
                        "#D99A55FF",
                        "#5A2418FF",
                        "#F8EAD7FF",
                        "#DFA45FFF",
                        "#A98A76FF",
                        "#7DDE91FF",
                        "#F2C15DFF",
                        "#F06B5FFF"
                )
                .layout(7, 254, 222, 3, 1)
                .iconHolder(ZenithTooltipTheme.Shape.GEM, 30, 2, 8, "accent", "background", 92)
                .barStyle(5, 2, "border_bottom", 72, "positive", 1, 232)
                .badgeStyle(6, 2, 1, 220)
                .dividerStyle(1, 2, 3, "accent", ZenithTooltipTheme.Decoration.CENTER_RUNE)
                .frameStyle(ZenithTooltipTheme.CornerDecoration.RUNE, 6, 2, "accent", true, 2, "border_bottom", 112)
                .headerStyle(ZenithTooltipTheme.Ornament.SMALL_DIAMONDS, "accent")
                .backgroundStyle(ZenithTooltipTheme.Pattern.RUNES, "accent", 13, 9);
    }

    public static void bloodlineEssence(ZenithTooltipThemeBuilder theme) {
        theme.colors(
                        "#110507E8",
                        "#F06B6BFF",
                        "#64131AFF",
                        "#FFE4E0FF",
                        "#F28B7EFF",
                        "#B88984FF",
                        "#89E88EFF",
                        "#F2C15DFF",
                        "#FF6F6FFF"
                )
                .layout(7, 256, 224, 3, 1)
                .iconHolder(ZenithTooltipTheme.Shape.GEM, 31, 2, 8, "accent", "background", 104)
                .barStyle(5, 2, "border_bottom", 82, "accent", 1, 238)
                .badgeStyle(6, 1, 1, 224)
                .dividerStyle(1, 2, 3, "accent", ZenithTooltipTheme.Decoration.CENTER_RUNE)
                .frameStyle(ZenithTooltipTheme.CornerDecoration.RUNE, 6, 2, "accent", true, 2, "border_bottom", 132)
                .headerStyle(ZenithTooltipTheme.Ornament.SMALL_DIAMONDS, "accent")
                .backgroundStyle(ZenithTooltipTheme.Pattern.RUNES, "accent", 15, 11);
    }

    public static void techniqueManual(ZenithTooltipThemeBuilder theme) {
        theme.colors(
                        "#0B0A0FE6",
                        "#8FC7FFFF",
                        "#24355AFF",
                        "#EAF4FFFF",
                        "#91CFFFFF",
                        "#8A97AAFF",
                        "#7EE08EFF",
                        "#E8C86BFF",
                        "#F06B5FFF"
                )
                .layout(7, 258, 226, 3, 1)
                .iconHolder(ZenithTooltipTheme.Shape.GEM, 30, 2, 8, "accent", "background", 88)
                .barStyle(5, 2, "border_bottom", 68, "accent", 1, 234)
                .badgeStyle(6, 1, 1, 224)
                .dividerStyle(1, 2, 3, "accent", ZenithTooltipTheme.Decoration.CENTER_RUNE)
                .frameStyle(ZenithTooltipTheme.CornerDecoration.RUNE, 6, 2, "accent", true, 2, "border_bottom", 108)
                .headerStyle(ZenithTooltipTheme.Ornament.SMALL_DIAMONDS, "accent")
                .backgroundStyle(ZenithTooltipTheme.Pattern.RUNES, "accent", 12, 10);
    }
}
