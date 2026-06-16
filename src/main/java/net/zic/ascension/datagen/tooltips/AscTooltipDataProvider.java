package net.zic.ascension.datagen.tooltips;

import net.minecraft.data.PackOutput;
import net.zic.ascension.datagen.AscTooltipThemes;
import net.zic.zenithlib.tooltip.api.ZenithTooltipColor;
import net.zic.zenithlib.tooltip.api.animation.ZenithTooltipPresets;
import net.zic.zenithlib.tooltip.api.element.ClassificationElement;
import net.zic.zenithlib.tooltip.datagen.ZenithTooltipDataProvider;

import static net.zic.zenithlib.tooltip.api.builder.ZenithTooltipBuilders.*;

/**
 * Generates ZenithLib tooltip templates and definitions for registered Ascension items.
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
        addDefaultTransferItemTooltips();
        addTabletOfDestructionTooltips();
        addTabletOfDestructionRules();
    }


    private void addDefaultTransferItemTooltips() {
        addDefaultBloodlineEssenceTooltip();
        addDefaultPhysiqueEssenceTooltip();
        addDefaultTechniqueManualTooltip();
    }

    private void addDefaultBloodlineEssenceTooltip() {
        template(id("default_bloodline_essence"))
                .animationPreset(ZenithTooltipPresets.CORRUPTED)
                .page(page(sourced("zenithlib:subject_name"))
                        .add(titleIcon(
                                sourced("zenithlib:subject_name"),
                                literal("Bloodline Essence")
                        ).withOnAllPages(true))
                        .add(classification(false, true, ClassificationElement.Style.BADGE))
                        .add(divider())
                        .add(dynamicBar(
                                literal("Purity"),
                                id("bloodline_purity"),
                                ZenithTooltipColor.NEGATIVE
                        ))
                        .add(text(
                                sourced("zenithlib:subject_description"),
                                ZenithTooltipColor.TEXT,
                                shimmer(2400, 0.14F, 0.45F)
                        )))
                .page(page(literal("Purity Growth"))
                        .add(header(literal("Purity Growth"), ZenithTooltipColor.ACCENT).withEffect(shimmer()))
                        .add(dynamic(id("bloodline_purity_gains"))));
    }

    private void addDefaultPhysiqueEssenceTooltip() {
        template(id("default_physique_essence"))
                .animationPreset(ZenithTooltipPresets.LIVING)
                .page(page(sourced("zenithlib:subject_name"))
                        .add(titleIcon(
                                sourced("zenithlib:subject_name"),
                                literal("Physique Essence")
                        ).withOnAllPages(true))
                        .add(classification(false, true, ClassificationElement.Style.BADGE))
                        .add(dynamic(id("physique_paths")))
                        .add(divider())
                        .add(text(
                                sourced("zenithlib:subject_description"),
                                ZenithTooltipColor.TEXT
                        )))
                .page(page(literal("Attributes"))
                        .add(header(literal("Statistics"), ZenithTooltipColor.POSITIVE))
                        .add(dynamic(id("physique_stats")))
                        .add(divider())
                        .add(header(literal("Affinities"), ZenithTooltipColor.ACCENT))
                        .add(dynamic(id("physique_affinities"))));
    }

    private void addDefaultTechniqueManualTooltip() {
        template(id("default_technique_manual"))
                .animationPreset(ZenithTooltipPresets.RUNIC)
                .animationPreset(ZenithTooltipPresets.MECHANICAL)
                .page(page(sourced("zenithlib:subject_name"))
                        .add(titleIcon(
                                sourced("zenithlib:subject_name"),
                                literal("Technique Manual")
                        ).withOnAllPages(true))
                        .add(classification(false, true, ClassificationElement.Style.BADGE))
                        .add(badge(
                                sourced("ascension:technique_path"),
                                ZenithTooltipColor.BACKGROUND,
                                ZenithTooltipColor.ACCENT,
                                ZenithTooltipColor.ACCENT
                        ))
                        .add(divider())
                        .add(text(
                                sourced("zenithlib:subject_description"),
                                ZenithTooltipColor.TEXT,
                                typewriter(760, 80)
                        ))
                        .add(row(
                                literal("Max Realm"),
                                sourced("ascension:technique_max_realm"),
                                ZenithTooltipColor.TEXT,
                                ZenithTooltipColor.ACCENT
                        )))
                .page(page(literal("Realm Growth"))
                        .add(header(literal("Realm Growth"), ZenithTooltipColor.ACCENT))
                        .add(dynamic(id("technique_progression_gains"))));
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
        template(id("tablet_of_destruction_human"))
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
        template(id("tablet_of_destruction_earth"))
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
        template(id("tablet_of_destruction_heaven"))
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
        template(id("tablet_of_destruction_ascendant"))
                .animationPreset(ZenithTooltipPresets.CELESTIAL)
                .animationPreset(ZenithTooltipPresets.CORRUPTED)
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
                                literal("Cooldown"),
                                literal("3 seconds"),
                                ZenithTooltipColor.TEXT,
                                ZenithTooltipColor.POSITIVE
                        ))
                        .add(row(
                                literal("Shapeless / Dome Max"),
                                literal("128 blocks"),
                                ZenithTooltipColor.TEXT,
                                ZenithTooltipColor.ACCENT
                        ))
                        .add(row(
                                literal("Tunnel / Escape Max"),
                                literal("256 blocks"),
                                ZenithTooltipColor.TEXT,
                                ZenithTooltipColor.ACCENT
                        )))
                .page(page(literal("Mining Shapes"))
                        .add(header(literal("Shapes"), ZenithTooltipColor.ACCENT))
                        .add(row(
                                translated("ascension.tablet.ascendant.shape.shapeless"),
                                literal("Sphere shell around target"),
                                ZenithTooltipColor.TEXT,
                                ZenithTooltipColor.MUTED
                        ))
                        .add(row(
                                translated("ascension.tablet.ascendant.shape.tunnel"),
                                literal("3×4 descending staircase"),
                                ZenithTooltipColor.TEXT,
                                ZenithTooltipColor.MUTED
                        ))
                        .add(row(
                                translated("ascension.tablet.ascendant.shape.escape"),
                                literal("3×4 ascending staircase"),
                                ZenithTooltipColor.TEXT,
                                ZenithTooltipColor.MUTED
                        ))
                        .add(row(
                                translated("ascension.tablet.ascendant.shape.dome"),
                                literal("Hemisphere around target"),
                                ZenithTooltipColor.TEXT,
                                ZenithTooltipColor.MUTED
                        )))
                .page(page(literal("Controls"))
                        .add(divider())
                        .add(header(literal("Controls"), ZenithTooltipColor.ACCENT))
                        .add(text(
                                translated("ascension.tablet.cycle_mode_info"),
                                ZenithTooltipColor.MUTED
                        ))
                        .add(text(
                                translated("ascension.tablet.ascendant.shape_info"),
                                ZenithTooltipColor.MUTED,
                                shimmer(2200, 0.15F, 0.45F)
                        ))
                        .add(spacer(3))
                        .add(text(
                                literal("Tunnels and Escape tunnels place torches every 5 steps along the staircase wall."),
                                ZenithTooltipColor.MUTED
                        )));
    }

    private void addTabletOfDestructionRules() {
        rule(id("tablet_of_destruction_human"))
                .priority(200)
                .items(id("tablet_of_destruction_human"))
                .template(id("tablet_of_destruction_human"))
                .theme(id("tablet_of_destruction"));

        rule(id("tablet_of_destruction_earth"))
                .priority(200)
                .items(id("tablet_of_destruction_earth"))
                .template(id("tablet_of_destruction_earth"))
                .theme(id("tablet_of_destruction"));

        rule(id("tablet_of_destruction_heaven"))
                .priority(200)
                .items(id("tablet_of_destruction_heaven"))
                .template(id("tablet_of_destruction_heaven"))
                .theme(id("tablet_of_destruction"));

        rule(id("tablet_of_destruction_ascendant"))
                .priority(200)
                .items(id("tablet_of_destruction_ascendant"))
                .template(id("tablet_of_destruction_ascendant"))
                .theme(id("tablet_of_destruction"));
    }
}
