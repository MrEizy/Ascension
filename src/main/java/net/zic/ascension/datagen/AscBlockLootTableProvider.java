package net.zic.ascension.datagen;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.zic.ascension.common.blocks.ModBlocks;
import net.zic.ascension.common.item.ModItems;

import java.util.Set;

public class AscBlockLootTableProvider extends BlockLootSubProvider {
    public AscBlockLootTableProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {

        dropSelf(ModBlocks.JADE_BLOCK.get());
        dropSelf(ModBlocks.FROST_SILVER_BLOCK.get());
        dropSelf(ModBlocks.BLACK_IRON_BLOCK.get());

        //Ores
        add(ModBlocks.BLACK_IRON_ORE.get(),
                createMultipleOreDrops(ModBlocks.BLACK_IRON_ORE.get(), ModItems.RAW_BLACK_IRON.get(), 1, 4));
        add(ModBlocks.FROST_SILVER_ORE.get(),
                createMultipleOreDrops(ModBlocks.FROST_SILVER_ORE.get(), ModItems.RAW_FROST_SILVER.get(), 1, 4));
        add(ModBlocks.JADE_ORE.get(),
                createMultipleOreDrops(ModBlocks.JADE_ORE.get(), ModItems.JADE.get(), 1, 6));

    }



    protected LootTable.Builder createMultipleOreDrops(Block block, Item item, float minDrops, float maxDrops) {
        HolderLookup.RegistryLookup<Enchantment> enchantments = this.registries.lookupOrThrow(Registries.ENCHANTMENT);
        return this.createSilkTouchDispatchTable(block, this.applyExplosionDecay(block,
                LootItem.lootTableItem(item)
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(minDrops, maxDrops)))
                        .apply(ApplyBonusCount.addOreBonusCount(enchantments.getOrThrow(Enchantments.FORTUNE)))));
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ModBlocks.BLOCKS.getEntries().stream().map(Holder::value)::iterator;
    }
}
