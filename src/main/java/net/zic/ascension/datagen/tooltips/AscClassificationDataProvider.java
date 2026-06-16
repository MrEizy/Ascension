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
    }

    private void addRanks() {
        rank("human")
                .literalLabel("Human")
                .color(ZenithTooltipColor.MUTED);

        rank("earth")
                .literalLabel("Earth")
                .color(ZenithTooltipColor.POSITIVE);

        rank("sky")
                .literalLabel("Sky")
                .color(ZenithTooltipColor.ACCENT);

        rank("heaven")
                .literalLabel("Heaven")
                .color(ZenithTooltipColor.WARNING);

        rank("ascendant")
                .literalLabel("Ascendant")
                .color(ZenithTooltipColor.ACCENT);
    }
}
