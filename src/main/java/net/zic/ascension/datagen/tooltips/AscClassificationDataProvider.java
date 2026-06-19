package net.zic.ascension.datagen.tooltips;

import net.minecraft.data.PackOutput;
import net.zic.zenithlib.classification.datagen.ZenithClassificationDataProvider;
import net.zic.zenithlib.tooltip.api.ZenithTooltipColor;

/** Generates Ascension tooltip categories and ranks used by stack-aware transfer item classifications. */
public final class AscClassificationDataProvider extends ZenithClassificationDataProvider {
    public AscClassificationDataProvider(PackOutput output, String modId) {
        super(output, modId);
    }

    @Override
    protected void addClassifications() {
        addCategories();
        addRanks();
        addItemClassifications();
    }

    private void addCategories() {
        category("bloodline_essence")
                .label("zenith.ascension.category.bloodline")
                .color(ZenithTooltipColor.NEGATIVE);

        category("physique_essence")
                .label("zenith.ascension.category.physique")
                .color(ZenithTooltipColor.POSITIVE);

        category("technique_manual")
                .label("zenith.ascension.category.technique")
                .color(ZenithTooltipColor.ACCENT);

        category("artifact")
                .literalLabel("Artifact")
                .color("#E8B85FFF");
    }

    private void addRanks() {
        rank("human")
                .label("zenith.ascension.tier.human")
                .color(ZenithTooltipColor.MUTED);

        rank("earth")
                .label("zenith.ascension.tier.earth")
                .color(ZenithTooltipColor.POSITIVE);

        rank("heaven")
                .label("zenith.ascension.tier.heaven")
                .color(ZenithTooltipColor.WARNING);

        rank("ascendant")
                .label("zenith.ascension.tier.ascendant")
                .color(ZenithTooltipColor.ACCENT);
    }

    private void addItemClassifications() {
        classification("human_items")
                .priority(210)
                .tags(id("human_items"))
                .category(id("artifact"))
                .rank(id("human"));

        classification("earth_items")
                .priority(220)
                .tags(id("earth_items"))
                .category(id("artifact"))
                .rank(id("earth"));

        classification("heaven_items")
                .priority(230)
                .tags(id("heaven_items"))
                .category(id("artifact"))
                .rank(id("heaven"));

        classification("ascendant_items")
                .priority(240)
                .tags(id("ascendant_items"))
                .category(id("artifact"))
                .rank(id("ascendant"));
    }

}
