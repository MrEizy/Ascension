package net.zic.ascension.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.blocks.ModBlocks;
import net.zic.ascension.common.util.ModTags;

import java.util.concurrent.CompletableFuture;

public class AscBlockTagProvider extends BlockTagsProvider {
    public AscBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, AscensionCraft.MOD_ID);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {



        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(ModBlocks.JADE_BLOCK.get())
                .add(ModBlocks.JADE_ORE.get())
                .add(ModBlocks.FROST_SILVER_BLOCK.get())
                .add(ModBlocks.FROST_SILVER_ORE.get())
                .add(ModBlocks.BLACK_IRON_BLOCK.get())
                .add(ModBlocks.BLACK_IRON_ORE.get());


        tag(BlockTags.MINEABLE_WITH_HOE)
                .add(ModBlocks.LINGZHI_MUSHROOM_B.get());


        tag(BlockTags.NEEDS_IRON_TOOL)
                .add(ModBlocks.JADE_BLOCK.get())
                .add(ModBlocks.JADE_ORE.get())
                .add(ModBlocks.FROST_SILVER_BLOCK.get())
                .add(ModBlocks.FROST_SILVER_ORE.get())
                .add(ModBlocks.BLACK_IRON_BLOCK.get())
                .add(ModBlocks.BLACK_IRON_ORE.get());



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

        tag(ModTags.Blocks.PROJECTION_UNBREAKABLE)
                .addOptionalTag(BlockTags.DRAGON_IMMUNE)
                .addOptionalTag(BlockTags.WITHER_IMMUNE)
                .add(
                        Blocks.BEDROCK,
                        Blocks.BARRIER,
                        Blocks.END_PORTAL,
                        Blocks.END_PORTAL_FRAME,
                        Blocks.NETHER_PORTAL,
                        Blocks.COMMAND_BLOCK,
                        Blocks.CHAIN_COMMAND_BLOCK,
                        Blocks.REPEATING_COMMAND_BLOCK,
                        Blocks.STRUCTURE_BLOCK,
                        Blocks.JIGSAW
                );

    }
}
