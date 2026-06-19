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
                .literalLabel("Bloodline Essence")
                .color(ZenithTooltipColor.NEGATIVE);

        category("physique_essence")
                .literalLabel("Physique Essence")
                .color(ZenithTooltipColor.POSITIVE);

        category("technique_manual")
                .literalLabel("Technique Manual")
                .color(ZenithTooltipColor.ACCENT);

        category("artifact")
                .literalLabel("Artifact")
                .color("#E8B85FFF");
    }

    private void addRanks() {
        rank("human")
                .literalLabel("Human") // change to translatable at some point
                .color("#A99678FF");

        rank("earth")
                .literalLabel("Earth")
                .color("#66D487FF");

        rank("sky")
                .literalLabel("Sky") // a placeholder because I think five ranks is cool, but names are hard...
                .color("#74C8FFFF");

        rank("heaven")
                .literalLabel("Heaven")
                .color("#F0C45CFF");

        rank("ascendant")
                .literalLabel("Ascendant")
                .color("#D58CFFFF");
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
