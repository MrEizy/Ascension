package net.zic.ascension.datagen.tooltips;

import net.minecraft.data.PackOutput;
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
        addArtifactsTooltips();
        addArtifactsRules();
        addDefaultHerbTooltip();
        addDefaultHerbRelatedTooltip();
        addDefaultPillTooltip();
    }

    private void addDefaultPillTooltip() {
        template(id("default_pill"))
                .animationPreset(ZenithTooltipPresets.LIVING)
                .page(page(sourced("zenithlib:subject_name"))
                        .add(titleIcon(
                                sourced("zenithlib:subject_name"),
                                translated("ascension.pill.tooltip.type")
                        ).withOnAllPages(true))
                        .add(classification(false, true, ClassificationElement.Style.BADGE))
                        .add(divider())
                        .add(text(
                                sourced("zenithlib:subject_description"),
                                ZenithTooltipColor.TEXT
                        ))
                        .add(divider())
                        .add(dynamicBar(
                                translated("ascension.pill.tooltip.purity"),
                                id("pill_purity"),
                                ZenithTooltipColor.POSITIVE
                        ))
                        .add(divider())
                        .add(header(translated("ascension.pill.tooltip.effect"), ZenithTooltipColor.ACCENT))
                        .add(text(
                                sourced("ascension:pill_effect"),
                                ZenithTooltipColor.POSITIVE
                        )));
    }

    private void addDefaultHerbRelatedTooltip() {
        template(id("default_herb_related"))
                .animationPreset(ZenithTooltipPresets.LIVING)
                .page(page(sourced("zenithlib:subject_name"))
                        .add(titleIcon(
                                sourced("zenithlib:subject_name")
                        ).withOnAllPages(true))
                        .add(dynamic(id("herb_related_type_badge")))
                        .add(divider())
                        .add(text(
                                sourced("zenithlib:subject_description"),
                                ZenithTooltipColor.TEXT
                        ))
                        .add(divider())
                        .add(dynamic(id("herb_related_target_row"))));
    }

    private void addDefaultHerbTooltip() {
        template(id("default_herb"))
                .animationPreset(ZenithTooltipPresets.LIVING)
                .page(page(sourced("zenithlib:subject_name"))
                        .add(titleIcon(
                                sourced("zenithlib:subject_name"),
                                translated("ascension.herb.tooltip.type")
                        ).withOnAllPages(true))
                        .add(dynamic(id("herb_quality_badge")))
                        .add(divider())
                        .add(text(
                                sourced("zenithlib:subject_description"),
                                ZenithTooltipColor.TEXT
                        ))
                        .add(divider())
                        .add(row(
                                translated("ascension.herb.tooltip.age"),
                                sourced("ascension:herb_age"),
                                ZenithTooltipColor.TEXT,
                                ZenithTooltipColor.ACCENT
                        ))
                        .add(row(
                                translated("ascension.herb.tooltip.origin"),
                                sourced("ascension:herb_origin"),
                                ZenithTooltipColor.TEXT,
                                ZenithTooltipColor.MUTED
                        )));
    }

    private void addDefaultTransferItemTooltips() {
        addDefaultBloodlineEssenceTooltip();
        addDefaultPhysiqueEssenceTooltip();
        addDefaultCultivationTechniqueTooltip();
        addDefaultBattleStyleTooltip();
    }

    private void addDefaultBloodlineEssenceTooltip() {
        template(id("default_bloodline_essence"))
                .animationPreset(ZenithTooltipPresets.CORRUPTED)
                .page(page(sourced("zenithlib:subject_name"))
                        .add(titleIcon(
                                sourced("zenithlib:subject_name"),
                                translated("item.ascension.bloodline_essence")
                        ).withOnAllPages(true))
                        .add(classification(false, true, ClassificationElement.Style.BADGE))
                        .add(divider())
                        .add(dynamicBar(
                                translated("ascension.tooltip.bloodline.purity"),
                                id("bloodline_purity"),
                                ZenithTooltipColor.NEGATIVE
                        ))
                        .add(text(
                                sourced("zenithlib:subject_description"),
                                ZenithTooltipColor.TEXT,
                                shimmer(2400, 0.14F, 0.45F)
                        )))
                .page(page(translated("ascension.tooltip.bloodline.purity_growth"))
                        .add(header(translated("ascension.tooltip.bloodline.purity_growth"), ZenithTooltipColor.ACCENT).withEffect(shimmer()))
                        .add(dynamic(id("bloodline_purity_gains"))));
    }

    private void addDefaultPhysiqueEssenceTooltip() {
        template(id("default_physique_essence"))
                .animationPreset(ZenithTooltipPresets.LIVING)
                .page(page(sourced("zenithlib:subject_name"))
                        .add(titleIcon(
                                sourced("zenithlib:subject_name"),
                                translated("item.ascension.physique_essence")
                        ).withOnAllPages(true))
                        .add(classification(false, true, ClassificationElement.Style.BADGE))
                        .add(divider())
                        .add(dynamic(id("physique_paths")))
                        .add(divider())
                        .add(text(
                                sourced("zenithlib:subject_description"),
                                ZenithTooltipColor.TEXT
                        )))
                .page(page(translated("ascension.tooltip.physique.stats_affinities"))
                        .add(header(translated("ascension.tooltip.physique.stats"), ZenithTooltipColor.POSITIVE))
                        .add(dynamic(id("physique_stats")))
                        .add(divider())
                        .add(header(translated("ascension.tooltip.physique.affinities"), ZenithTooltipColor.ACCENT))
                        .add(dynamic(id("physique_affinities"))));
    }

    private void addDefaultCultivationTechniqueTooltip() {
        template(id("default_cultivation_technique"))
                .animationPreset(ZenithTooltipPresets.RUNIC)
                .animationPreset(ZenithTooltipPresets.MECHANICAL)
                .page(page(sourced("zenithlib:subject_name"))
                        .add(titleIcon(
                                sourced("zenithlib:subject_name"),
                                translated("item.ascension.technique_manual.cultivation_technique")
                        ).withOnAllPages(true))
                        .add(classification(false, true, ClassificationElement.Style.BADGE))
                        .add(divider())
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
                        .add(divider())
                        .add(row(
                                translated("ascension.tooltip.technique.max_realm"),
                                sourced("ascension:technique_max_realm"),
                                ZenithTooltipColor.TEXT,
                                ZenithTooltipColor.ACCENT
                        )))
                .page(page(translated("ascension.tooltip.technique.realm_growth"))
                        .add(header(translated("ascension.tooltip.technique.realm_growth"), ZenithTooltipColor.ACCENT))
                        .add(dynamic(id("technique_progression_gains"))));
    }

    private void addDefaultBattleStyleTooltip() {
        template(id("default_battle_style"))
                .animationPreset(ZenithTooltipPresets.RUNIC)
                .animationPreset(ZenithTooltipPresets.MECHANICAL)
                .page(page(sourced("zenithlib:subject_name"))
                        .add(titleIcon(
                                sourced("zenithlib:subject_name"),
                                translated("item.ascension.technique_manual.battle_style")
                        ).withOnAllPages(true))
                        .add(classification(false, true, ClassificationElement.Style.BADGE))
                        .add(divider())
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
                        .add(divider())
                        .add(row(
                                translated("ascension.tooltip.technique.max_realm"),
                                sourced("ascension:technique_max_realm"),
                                ZenithTooltipColor.TEXT,
                                ZenithTooltipColor.ACCENT
                        )))
                .page(page(translated("ascension.tooltip.technique.realm_growth"))
                        .add(header(translated("ascension.tooltip.technique.realm_growth"), ZenithTooltipColor.ACCENT))
                        .add(dynamic(id("technique_progression_gains"))));
    }

    private void addThemes() {
        AscTooltipThemes.artifactThemes(theme("artifact_themes"));
        AscTooltipThemes.physiqueEssence(theme("physique_essence"));
        AscTooltipThemes.bloodlineEssence(theme("bloodline_essence"));
        AscTooltipThemes.techniqueManual(theme("technique_manual"));
        AscTooltipThemes.herb(theme("herb"));
    }

    private void addArtifactsTooltips() {
        addHumanTabletTooltip();
        addEarthTabletTooltip();
        addHeavenTabletTooltip();
        addAscendantTabletTooltip();
        addJadeBottleTooltip();
    }


    private void addJadeBottleTooltip() {
        template(id("jade_bottle"))
                .page(page(translated("ascension.tooltip.jade_bottle.page"))
                        .add(titleIcon(
                                translated("ascension.tooltip.jade_bottle")
                        ))
                        .add(classificationRankBadge())
                        .add(divider())
                        .add(header(translated("ascension.tooltip.jade_bottle.contained"), ZenithTooltipColor.ACCENT))
                        .add(row(
                                translated("ascension.tooltip.jade_bottle.contained_pill"),
                                literal("null"),
                                ZenithTooltipColor.TEXT,
                                ZenithTooltipColor.ACCENT
                        ))
                        .add(row(
                                translated("ascension.tooltip.jade_bottle.expiration"),
                                literal("20 seconds"),
                                ZenithTooltipColor.TEXT,
                                ZenithTooltipColor.WARNING
                        )));
    }

    private void addHumanTabletTooltip() {
        template(id("tablet_of_destruction_human"))
                .page(page(literal("Tablet of Destruction: Ordinary"))
                        .add(titleIcon(
                                literal("Tablet of Destruction")
                        ))
                        .add(classificationRankBadge())
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
                .page(page(literal("Tablet of Destruction: Profound"))
                        .add(titleIcon(
                                literal("Tablet of Destruction")
                        ))
                        .add(classificationRankBadge())
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
                                literal("Tablet of Destruction")
                        ))
                        .add(classificationRankBadge())
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
                .page(page(literal("Tablet of Destruction: Saint"))
                        .add(titleIcon(
                                literal("Tablet of Destruction")
                        ))
                        .add(classificationRankBadge())
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

    private void addArtifactsRules() {
        rule(id("tablet_of_destruction_human"))
                .priority(200)
                .items(id("tablet_of_destruction_human"))
                .template(id("tablet_of_destruction_human"))
                .theme(id("artifact_themes"));

        rule(id("tablet_of_destruction_earth"))
                .priority(200)
                .items(id("tablet_of_destruction_earth"))
                .template(id("tablet_of_destruction_earth"))
                .theme(id("artifact_themes"));

        rule(id("tablet_of_destruction_heaven"))
                .priority(200)
                .items(id("tablet_of_destruction_heaven"))
                .template(id("tablet_of_destruction_heaven"))
                .theme(id("artifact_themes"));

        rule(id("tablet_of_destruction_ascendant"))
                .priority(200)
                .items(id("tablet_of_destruction_ascendant"))
                .template(id("tablet_of_destruction_ascendant"))
                .theme(id("artifact_themes"));

        rule(id("jade_bottle"))
                .priority(200)
                .items(id("jade_bottle"))
                .template(id("jade_bottle"))
                .theme(id("artifact_themes"));
    }
}
