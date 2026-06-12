package net.zic.ascension.datagen;

import net.minecraft.data.PackOutput;
import net.zic.zenithlib.tooltip.api.ZenithTooltipColor;
import net.zic.zenithlib.tooltip.api.ZenithTooltipTheme;
import net.zic.zenithlib.tooltip.datagen.ZenithTooltipDataProvider;

import static net.zic.zenithlib.tooltip.api.builder.ZenithTooltipBuilders.*;

/**
 * Generates ZenithLib tooltip documents for registered
 * Ascension items such as artifacts and consumables.
 *
 * <p>This provider should contain only information that is fixed for an item.</p>
 *
 * <p>Registry-backed items such as physiques, techniques, and bloodlines
 * should be defining item_tooltip inside their own JSONs.</p>
 */
public final class AscTooltipDataProvider extends ZenithTooltipDataProvider {

    public AscTooltipDataProvider(PackOutput output, String modId) {
        super(output, modId);
    }

    @Override
    protected void addTooltips() {
        addTabletOfDestructionTheme();
        addTabletOfDestructionTooltips();
        addTabletOfDestructionRules();
    }

    private void addTabletOfDestructionTooltips() {
        addHumanTabletTooltip();
        addEarthTabletTooltip();
        addHeavenTabletTooltip();
    }

    // Tooltips
    private void addHumanTabletTooltip() {
        template(id("tablet_of_destruction_human_document"))
                .page(page(literal("Tablet of Destruction: Human"))
                        .add(titleIcon(
                                literal("Tablet of Destruction"),
                                literal("Human Grade")
                        ))
                        .add(badge(
                                literal("HUMAN GRADE"),
                                ZenithTooltipColor.BACKGROUND,
                                ZenithTooltipColor.MUTED,
                                ZenithTooltipColor.ACCENT
                        ))
                        .add(divider())
                        .add(header(
                                literal("Excavation"),
                                ZenithTooltipColor.ACCENT
                        ))
                        .add(row(
                                literal("Tunnel Profile"),
                                literal("2 × 3 × 15"),
                                ZenithTooltipColor.TEXT,
                                ZenithTooltipColor.ACCENT
                        ))
                        .add(row(
                                literal("Cooldown"),
                                literal("20 seconds"),
                                ZenithTooltipColor.TEXT,
                                ZenithTooltipColor.WARNING
                        ))
                        .add(divider())
                        .add(spacer(2))
                        .add(text(
                                literal("Aim upward or downward to excavate vertically."),
                                ZenithTooltipColor.MUTED
                        )));
    }

    private void addEarthTabletTooltip() {
        template(id("tablet_of_destruction_earth_document"))
                .page(page(literal("Tablet of Destruction: Earth"))
                        .add(titleIcon(
                                literal("Tablet of Destruction"),
                                literal("Earth Grade")
                        ))
                        .add(badge(
                                literal("EARTH GRADE"),
                                ZenithTooltipColor.BACKGROUND,
                                ZenithTooltipColor.POSITIVE,
                                ZenithTooltipColor.POSITIVE
                        ))
                        .add(divider())
                        .add(header(
                                literal("Excavation"),
                                ZenithTooltipColor.POSITIVE
                        ))
                        .add(row(
                                literal("Tunnel Profile"),
                                literal("3 × 5 × 18"),
                                ZenithTooltipColor.TEXT,
                                ZenithTooltipColor.ACCENT
                        ))
                        .add(row(
                                literal("Cooldown"),
                                literal("10 seconds"),
                                ZenithTooltipColor.TEXT,
                                ZenithTooltipColor.WARNING
                        ))
                        .add(divider())
                        .add(header(
                                literal("Controls"),
                                ZenithTooltipColor.ACCENT
                        ))
                        .add(text(
                                translated("ascension.tablet.cycle_mode_info"),
                                ZenithTooltipColor.MUTED
                        )));
    }

    private void addHeavenTabletTooltip() {
        template(id("tablet_of_destruction_heaven_document"))
                .page(page(literal("Tablet of Destruction: Heaven"))
                        .add(titleIcon(
                                literal("Tablet of Destruction"),
                                literal("Heaven Grade")
                        ))
                        .add(badge(
                                literal("HEAVEN GRADE"),
                                ZenithTooltipColor.BACKGROUND,
                                ZenithTooltipColor.ACCENT,
                                ZenithTooltipColor.ACCENT
                        ))
                        .add(divider())
                        .add(header(
                                literal("Excavation"),
                                ZenithTooltipColor.ACCENT
                        ))
                        .add(row(
                                literal("Tunnel Profile"),
                                literal("4 × 7 × 22"),
                                ZenithTooltipColor.TEXT,
                                ZenithTooltipColor.ACCENT
                        ))
                        .add(row(
                                literal("Cooldown"),
                                literal("5 seconds"),
                                ZenithTooltipColor.TEXT,
                                ZenithTooltipColor.POSITIVE
                        )))
                .page(page(literal("Heavenly Functions"))
                        .add(header(
                                literal("Linked Storage"),
                                ZenithTooltipColor.ACCENT
                        ))
                        .add(text(
                                translated("item.ascension.tablet_of_destruction_heaven.link_info"),
                                ZenithTooltipColor.TEXT
                        ))
                        .add(spacer(3))
                        .add(text(
                                literal(
                                        "When block collection is enabled, excavated materials may be routed "
                                                + "toward a linked container."
                                ),
                                ZenithTooltipColor.MUTED
                        ))
                        .add(divider())
                        .add(header(
                                literal("Controls"),
                                ZenithTooltipColor.ACCENT
                        ))
                        .add(text(
                                translated("ascension.tablet.cycle_mode_info"),
                                ZenithTooltipColor.MUTED
                        ))
                        .add(text(
                                literal("Aim upward or downward to excavate vertically."),
                                ZenithTooltipColor.MUTED
                        )));
    }

    // Rules
    private void addTabletOfDestructionRules() {

        rule(id("tablet_of_destruction_human"))
                .priority(200)
                .items(id("tablet_of_destruction_human"))
                .document(id("tablet_of_destruction_human_document"))
                .theme(id("tablet_of_destruction"));
        rule(id("tablet_of_destruction_earth"))
                .priority(200)
                .items(id("tablet_of_destruction_earth"))
                .document(id("tablet_of_destruction_earth_document"))
                .theme(id("tablet_of_destruction"));
        rule(id("tablet_of_destruction_heaven"))
                .priority(200)
                .items(id("tablet_of_destruction_heaven"))
                .document(id("tablet_of_destruction_heaven_document"))
                .theme(id("tablet_of_destruction"));
    }

    // Themes
    private void addTabletOfDestructionTheme() {
        theme(id("tablet_of_destruction"))
                .colors(
                        "#100C0CDD",
                        "#D9A441FF",
                        "#681F1FFF",
                        "#F2E7D5FF",
                        "#E2B95FFF",
                        "#9B8C7DFF",
                        "#72C982FF",
                        "#E7B95CFF",
                        "#E05C5CFF"
                )
                .layout(6, 250, 220, 3, 1)
                .iconHolder(ZenithTooltipTheme.Shape.OCTAGON, 31, 2, 8, "accent", "background", 92)
                .barStyle(5, 2, "muted", 70,"border_bottom", 1, 255)
                .badgeStyle(6, 2, 1, 230)
                .dividerStyle(1, 2, 3, "accent", ZenithTooltipTheme.Decoration.CENTER_RUNE)
                .frameStyle(ZenithTooltipTheme.CornerDecoration.RUNE, 6, 2, "accent", true, 2, "border_bottom", 105)
                .headerStyle(ZenithTooltipTheme.Ornament.NONE, "accent")
                .backgroundStyle(ZenithTooltipTheme.Pattern.RUNES, "accent", 14, 12);
    }
}