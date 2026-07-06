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
        rank("ordinary")
                .label("zenith.ascension.tier.ordinary")
                .color("#A99678FF");

        rank("profound")
                .label("zenith.ascension.tier.profound")
                .color("#66D487FF");

        rank("heaven")
                .label("zenith.ascension.tier.heaven")
                .color("#F0C45CFF");

        rank("saint")
                .label("zenith.ascension.tier.saint")
                .color("#E8E8E8FF");

        rank("god")
                .label("zenith.ascension.tier.god")
                .color("#B388FFFF");

        rank("heavens_path")
                .label("zenith.ascension.tier.heavens_path")
                .color("#00E5FFFF");
    }

    private void addItemClassifications() {
        classification("ordinary_items")
                .priority(210)
                .tags(id("ordinary_items"))
                .category(id("artifact"))
                .rank(id("ordinary"));

        classification("profound_items")
                .priority(220)
                .tags(id("profound_items"))
                .category(id("artifact"))
                .rank(id("profound"));

        classification("heaven_items")
                .priority(230)
                .tags(id("heaven_items"))
                .category(id("artifact"))
                .rank(id("heaven"));

        classification("saint_items")
                .priority(240)
                .tags(id("saint_items"))
                .category(id("artifact"))
                .rank(id("saint"));

        classification("god_items")
                .priority(250)
                .tags(id("god_items"))
                .category(id("artifact"))
                .rank(id("god"));

        classification("heavens_path_items")
                .priority(260)
                .tags(id("heavens_path_items"))
                .category(id("artifact"))
                .rank(id("heavens_path"));
    }

}
