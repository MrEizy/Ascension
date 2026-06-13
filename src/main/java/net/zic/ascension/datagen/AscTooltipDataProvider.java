package net.zic.ascension.datagen;

import net.minecraft.data.PackOutput;
import net.zic.zenithlib.tooltip.api.ZenithTooltipColor;
import net.zic.zenithlib.tooltip.api.animation.ZenithTooltipPresets;
import net.zic.zenithlib.tooltip.datagen.ZenithTooltipDataProvider;

import static net.zic.zenithlib.tooltip.api.builder.ZenithTooltipBuilders.*;

/**
 * Generates ZenithLib tooltip documents for registered Ascension items.
 *
 * <p>Registry-backed items such as physiques, techniques, and bloodlines keep
 * their item_tooltip blocks inside their own datapack JSONs.</p>
 */
public final class AscTooltipDataProvider extends ZenithTooltipDataProvider {

    public AscTooltipDataProvider(PackOutput output, String modId) {
        super(output, modId);
    }

    @Override
    protected void addTooltips() {
        addThemes();
        addTabletOfDestructionTooltips();
        addTabletOfDestructionRules();
    }

    private void addThemes() {
        AscTooltipThemes.tabletOfDestruction(theme("tablet_of_destruction"));
        AscTooltipThemes.physiqueEssence(theme("physique_essence"));
        AscTooltipThemes.bloodlineEssence(theme("bloodline_essence"));
        AscTooltipThemes.techniqueManual(theme("technique_manual"));
    }

    private void addTabletOfDestructionTooltips() {
        addHumanTabletTooltip();
        addEarthTabletTooltip();
        addHeavenTabletTooltip();
        addAscendantTabletTooltip();
    }

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
                        .add(header(literal("Excavation"), ZenithTooltipColor.ACCENT))
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
                        .add(text(
                                literal("Aim upward or downward to excavate vertically."),
                                ZenithTooltipColor.MUTED,
                                typewriter(700, 80)
                        )));
    }

    private void addEarthTabletTooltip() {
        template(id("tablet_of_destruction_earth_document"))
                .animationPreset(ZenithTooltipPresets.KINETIC)
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
                        .add(header(literal("Excavation"), ZenithTooltipColor.POSITIVE))
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
                        .add(header(literal("Controls"), ZenithTooltipColor.ACCENT))
                        .add(text(
                                translated("ascension.tablet.cycle_mode_info"),
                                ZenithTooltipColor.MUTED
                        )));
    }

    private void addHeavenTabletTooltip() {
        template(id("tablet_of_destruction_heaven_document"))
                .animationPreset(ZenithTooltipPresets.KINETIC)
                .animationPreset(ZenithTooltipPresets.NEBULA)
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
                        .add(header(literal("Excavation"), ZenithTooltipColor.ACCENT))
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
                        .add(header(literal("Linked Storage"), ZenithTooltipColor.ACCENT))
                        .add(text(
                                translated("item.ascension.tablet_of_destruction_heaven.link_info"),
                                ZenithTooltipColor.TEXT,
                                shimmer(2200, 0.15F, 0.45F)
                        ))
                        .add(spacer(3))
                        .add(text(
                                literal("When collection is enabled, excavated materials may be routed to a linked container."),
                                ZenithTooltipColor.MUTED
                        ))
                        .add(divider())
                        .add(header(literal("Controls"), ZenithTooltipColor.ACCENT))
                        .add(text(
                                translated("ascension.tablet.cycle_mode_info"),
                                ZenithTooltipColor.MUTED
                        ))
                        .add(text(
                                literal("Aim upward or downward to excavate vertically."),
                                ZenithTooltipColor.MUTED
                        )));
    }

    private void addAscendantTabletTooltip() {
        template(id("tablet_of_destruction_ascendant_document"))
                .animationPreset(ZenithTooltipPresets.KINETIC)
                .animationPreset(ZenithTooltipPresets.NEBULA)
                .page(page(literal("Tablet of Destruction: Ascendant"))
                        .add(titleIcon(
                                literal("Tablet of Destruction"),
                                literal("Ascendant Grade")
                        ))
                        .add(badge(
                                literal("ASCENDANT GRADE"),
                                ZenithTooltipColor.BACKGROUND,
                                ZenithTooltipColor.ACCENT,
                                ZenithTooltipColor.ACCENT
                        ))
                        .add(divider())
                        .add(header(literal("Excavation"), ZenithTooltipColor.ACCENT))
                        .add(row(
                                literal("Block Limit"),
                                literal("128 blocks"),
                                ZenithTooltipColor.TEXT,
                                ZenithTooltipColor.ACCENT
                        ))
                        .add(row(
                                literal("Cooldown"),
                                literal("3 seconds"),
                                ZenithTooltipColor.TEXT,
                                ZenithTooltipColor.POSITIVE
                        ))
                        .add(row(
                                literal("Modes"),
                                literal("4 shapes"),
                                ZenithTooltipColor.TEXT,
                                ZenithTooltipColor.ACCENT
                        ))
                        .add(divider())
                        .add(text(
                                literal("Cycle between shapeless excavation, mining tunnel, escape tunnel, and dome excavation."),
                                ZenithTooltipColor.MUTED,
                                shimmer(2400, 0.14F, 0.42F)
                        )))
                .page(page(literal("Shapes"))
                        .add(header(literal("Shape Modes"), ZenithTooltipColor.ACCENT))
                        .add(row(
                                literal("Shapeless"),
                                literal("Expanding sphere"),
                                ZenithTooltipColor.TEXT,
                                ZenithTooltipColor.ACCENT
                        ))
                        .add(row(
                                literal("Mining Tunnel"),
                                literal("Descending 3 × 3 tunnel"),
                                ZenithTooltipColor.TEXT,
                                ZenithTooltipColor.ACCENT
                        ))
                        .add(row(
                                literal("Escape Tunnel"),
                                literal("Ascending 3 × 3 tunnel"),
                                ZenithTooltipColor.TEXT,
                                ZenithTooltipColor.POSITIVE
                        ))
                        .add(row(
                                literal("Dome"),
                                literal("Hemisphere excavation"),
                                ZenithTooltipColor.TEXT,
                                ZenithTooltipColor.ACCENT
                        ))
                        .add(divider())
                        .add(header(literal("Controls"), ZenithTooltipColor.ACCENT))
                        .add(text(
                                translated("ascension.tablet.ascendant.shape_info"),
                                ZenithTooltipColor.MUTED
                        ))
                        .add(text(
                                translated("ascension.tablet.cycle_mode_info"),
                                ZenithTooltipColor.MUTED
                        )));
    }

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
        rule(id("tablet_of_destruction_ascendant"))
                .priority(200)
                .items(id("tablet_of_destruction_ascendant"))
                .document(id("tablet_of_destruction_ascendant_document"))
                .theme(id("tablet_of_destruction"));
    }
}
