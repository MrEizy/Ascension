package net.zic.ascension.datagen.villager;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.VillagerTradesTagsProvider;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.VillagerTradeTags;

import java.util.concurrent.CompletableFuture;

public class AscVillagerTradeTags extends VillagerTradesTagsProvider {
    public AscVillagerTradeTags(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        var builder = getOrCreateRawBuilder(VillagerTradeTags.LIBRARIAN_LEVEL_3);

        builder.add(TagEntry.element(AscVillagerTrades.LIBRARIAN_3_EAGLE_CLAW_PALM_TECHNIQUE.identifier()));
        builder.add(TagEntry.element(AscVillagerTrades.LIBRARIAN_3_GRASPING_SAND_TECHNIQUE.identifier()));
        builder.add(TagEntry.element(AscVillagerTrades.LIBRARIAN_3_IMPERIAL_SEVEN_STANCES.identifier()));
        builder.add(TagEntry.element(AscVillagerTrades.LIBRARIAN_3_IRON_FIST_TEMPERING_MANUAL.identifier()));
        builder.add(TagEntry.element(AscVillagerTrades.LIBRARIAN_3_LIGHTNESS_TECHNIQUE.identifier()));
        builder.add(TagEntry.element(AscVillagerTrades.LIBRARIAN_3_MARTIAL_TRANSCENDENCE_VOL_1.identifier()));
        builder.add(TagEntry.element(AscVillagerTrades.LIBRARIAN_3_NINE_PATHS_OF_TRUTH.identifier()));
        builder.add(TagEntry.element(AscVillagerTrades.LIBRARIAN_3_SHADOWLESS_ART.identifier()));
        builder.add(TagEntry.element(AscVillagerTrades.LIBRARIAN_3_SUSTAINED_SPIRIT_ART.identifier()));
        builder.add(TagEntry.element(AscVillagerTrades.LIBRARIAN_3_SWORD_DRAW_MANUAL.identifier()));
    }
}