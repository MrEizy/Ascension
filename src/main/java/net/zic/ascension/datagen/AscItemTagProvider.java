package net.zic.ascension.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ItemTagsProvider;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.item.ModItems;
import net.zic.ascension.common.util.ModTags;

import java.util.concurrent.CompletableFuture;

public class AscItemTagProvider extends ItemTagsProvider {

    public AscItemTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, AscensionCraft.MOD_ID);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {



        tag(ModTags.Items.ORDINARY_ITEMS)
                .add(ModItems.TABLET_OF_DESTRUCTION_HUMAN.get());

        tag(ModTags.Items.PROFOUND_ITEMS)
                .add(ModItems.TABLET_OF_DESTRUCTION_EARTH.get());

        tag(ModTags.Items.HEAVEN_ITEMS)
                .add(ModItems.TABLET_OF_DESTRUCTION_HEAVEN.get());

        tag(ModTags.Items.SAINT_ITEMS)
                .add(ModItems.TABLET_OF_DESTRUCTION_ASCENDANT.get());

        tag(ModTags.Items.GOD_ITEMS);

        tag(ModTags.Items.HEAVENS_PATH_ITEMS);
        
    }
}
