package net.zic.ascension.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.util.ModTags;

import java.util.concurrent.CompletableFuture;

public class AscBlockTagProvider extends BlockTagsProvider {
    public AscBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, AscensionCraft.MOD_ID);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {



        tag(ModTags.Blocks.DESTRUCTIBLE_BLOCKS)
                .add(Blocks.STONE)
                .add(Blocks.GRANITE)
                .add(Blocks.ANDESITE)
                .add(Blocks.DIORITE)
                .add(Blocks.DEEPSLATE)
                .add(Blocks.GRAVEL)
                .add(Blocks.SAND)
                .add(Blocks.TUFF)
                .add(Blocks.DIRT)
                .add(Blocks.NETHERRACK)
                .add(Blocks.BLACKSTONE)
                .add(Blocks.BASALT);

        tag(ModTags.Blocks.LINKABLE_CONTAINERS)
                .add(Blocks.CHEST)
                .add(Blocks.BARREL);

    }
}
