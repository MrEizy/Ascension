package net.zic.ascension.datagen;

import net.minecraft.advancements.criterion.StatePropertiesPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.AnyOfCondition;
import net.minecraft.world.level.storage.loot.predicates.InvertedLootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.zic.ascension.common.blocks.ModBlocks;
import net.zic.ascension.common.blocks.crops.herbs.HerbCropBlock;
import net.zic.ascension.common.blocks.crops.herbs.PodHerbBlock;
import net.zic.ascension.common.item.ModItems;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AscBlockLootTableProvider extends BlockLootSubProvider {
    public AscBlockLootTableProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        // Drop Self Blocks
        dropSelf(ModBlocks.CULTIVATION_SOIL.get());
        dropSelf(ModBlocks.JADE_BLOCK.get());
        dropSelf(ModBlocks.FROST_SILVER_BLOCK.get());
        dropSelf(ModBlocks.BLACK_IRON_BLOCK.get());
        dropSelf(ModBlocks.FERMENTING_BARREL.get());

        // Ore Drop Blocks
        add(ModBlocks.BLACK_IRON_ORE.get(),
                createOreDrop(ModBlocks.BLACK_IRON_ORE.get(), ModItems.RAW_BLACK_IRON.get()));
        add(ModBlocks.FROST_SILVER_ORE.get(),
                createOreDrop(ModBlocks.FROST_SILVER_ORE.get(), ModItems.RAW_FROST_SILVER.get()));
        add(ModBlocks.JADE_ORE.get(),
                createMultipleOreDrops(ModBlocks.JADE_ORE.get(), ModItems.JADE.get(), 1, 4));


        add(ModBlocks.JADE_DEW_GRASS_CROP.get(), createSeedHerbDrops(
                ModBlocks.JADE_DEW_GRASS_CROP.get(),
                ModItems.JADE_DEW_GRASS.get(),
                ModItems.JADE_DEW_GRASS_SEEDS.get(),
                1, 2,
                1,
                0.35F
        ));

        add(ModBlocks.GINSENG_CROP.get(), createDirectHerbDrops(
                ModBlocks.GINSENG_CROP.get(),
                ModItems.GINSENG.get(),
                1, 2,
                0.0F
        ));

        add(ModBlocks.FIRE_GINSENG_CROP.get(), createDirectHerbDrops(
                ModBlocks.FIRE_GINSENG_CROP.get(),
                ModItems.FIRE_GINSENG.get(),
                1, 1,
                0.25F
        ));

        add(ModBlocks.SNOW_GINSENG_CROP.get(), createDirectHerbDrops(
                ModBlocks.SNOW_GINSENG_CROP.get(),
                ModItems.SNOW_GINSENG.get(),
                1, 1,
                0.25F
        ));
        add(ModBlocks.NINE_SUN_FIRE_ROOT_CROP.get(), createDirectHerbDrops(
                ModBlocks.NINE_SUN_FIRE_ROOT_CROP.get(),
                ModItems.NINE_SUN_FIRE_ROOT.get(),
                1, 1,
                0.15F
        ));
        add(ModBlocks.MOONWELL_JADE_LOTUS_CROP.get(), createDirectHerbDrops(
                ModBlocks.MOONWELL_JADE_LOTUS_CROP.get(),
                ModItems.MOONWELL_JADE_LOTUS.get(),
                1, 1,
                0.10F
        ));
        add(ModBlocks.WHITE_JADE_ORCHID_CROP.get(), createDirectHerbDrops(
                ModBlocks.WHITE_JADE_ORCHID_CROP.get(),
                ModItems.WHITE_JADE_ORCHID.get(),
                1, 1,
                0.25F
        ));

        add(ModBlocks.LINGZHI_MUSHROOM_B.get(),
                createSingleItemTableWithSilkTouch(ModBlocks.LINGZHI_MUSHROOM_B.get(), ModItems.LINGZHI_MUSHROOM.get()));
        add(ModBlocks.BLOOD_LINGZHI_MUSHROOM_B.get(),
                createSingleItemTableWithSilkTouch(ModBlocks.BLOOD_LINGZHI_MUSHROOM_B.get(), ModItems.BLOOD_LINGZHI_MUSHROOM.get()));

        add(ModBlocks.PEACH_POD.get(), createPodHerbDrops(ModBlocks.PEACH_POD.get(), 1, 3));

        add(ModBlocks.HEAVENLY_THUNDER_PEACH_POD.get(),
                createPodHerbDrops(ModBlocks.HEAVENLY_THUNDER_PEACH_POD.get(), 1, 2));




        //Trees
        dropSelf(ModBlocks.PEACH_LOG.get());
        dropSelf(ModBlocks.PEACH_WOOD.get());
        dropSelf(ModBlocks.STRIPPED_PEACH_LOG.get());
        dropSelf(ModBlocks.STRIPPED_PEACH_WOOD.get());

        dropSelf(ModBlocks.PEACH_PLANKS.get());
        dropSelf(ModBlocks.PEACH_SAPLING.get());

        add(ModBlocks.POTTED_PEACH_SAPLING.get(), createPotFlowerItemTable(ModBlocks.PEACH_SAPLING.get()));
        add(ModBlocks.PEACH_LEAVES.get(), block -> createLeavesDrops(block, ModBlocks.PEACH_SAPLING.get(), NORMAL_LEAVES_SAPLING_CHANCES));
    }

    protected LootTable.Builder createPodHerbDrops(PodHerbBlock block, float minCount, float maxCount) {
        return LootTable.lootTable().withPool(
                LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0F))
                        .when(podMatureCondition(block))
                        .add(applyExplosionDecay(block,
                                LootItem.lootTableItem(block.harvestItem())
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(minCount, maxCount))))));
    }

    private LootItemCondition.Builder podMatureCondition(PodHerbBlock block) {
        List<LootItemCondition.Builder> matureStages = new ArrayList<>();
        int firstMatureStage = block.definition().maxGrowthStage();
        int lastMatureStage = firstMatureStage + block.definition().maxAgeTier();

        for (int stage = firstMatureStage; stage <= lastMatureStage; stage++) {
            matureStages.add(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                    .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(PodHerbBlock.STAGE, stage))
            );
        }

        return AnyOfCondition.anyOf(matureStages.toArray(LootItemCondition.Builder[]::new));
    }

    protected LootTable.Builder createSeedHerbDrops(HerbCropBlock block, Item herb, Item seeds, float minHerbs, float maxHerbs, int baseSeeds, float bonusSeedChance) {
        LootTable.Builder table = LootTable.lootTable()
                .withPool(immaturePlantingPool(block, seeds))
                .withPool(matureCountPool(block, herb, minHerbs, maxHerbs));

        if (baseSeeds > 0) {
            table.withPool(matureCountPool(block, seeds, baseSeeds, baseSeeds));
        }
        if (bonusSeedChance > 0.0F) {
            table.withPool(matureChancePool(block, seeds, bonusSeedChance));
        }

        return table;
    }

    protected LootTable.Builder createDirectHerbDrops(HerbCropBlock block, Item herb, float minHerbs, float maxHerbs, float bonusHerbChance) {
        LootTable.Builder table = LootTable.lootTable()
                .withPool(immaturePlantingPool(block, herb))
                .withPool(matureCountPool(block, herb, minHerbs, maxHerbs));

        if (bonusHerbChance > 0.0F) {
            table.withPool(matureChancePool(block, herb, bonusHerbChance));
        }

        return table;
    }

    private LootPool.Builder immaturePlantingPool(HerbCropBlock block, Item plantingItem) {
        return LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1.0F))
                .when(InvertedLootItemCondition.invert(matureCondition(block)))
                .add(applyExplosionDecay(block, LootItem.lootTableItem(plantingItem)));
    }

    private LootPool.Builder matureCountPool(HerbCropBlock block, Item item, float minCount, float maxCount) {
        return LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1.0F))
                .when(matureCondition(block))
                .add(applyExplosionDecay(block,
                        LootItem.lootTableItem(item)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(minCount, maxCount)))));
    }

    private LootPool.Builder matureChancePool(HerbCropBlock block, Item item, float chance) {
        return LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1.0F))
                .when(matureCondition(block))
                .when(LootItemRandomChanceCondition.randomChance(chance))
                .add(applyExplosionDecay(block, LootItem.lootTableItem(item)));
    }

    private LootItemCondition.Builder matureCondition(HerbCropBlock block) {
        List<LootItemCondition.Builder> matureStages = new ArrayList<>();
        int firstMatureStage = block.definition().maxGrowthStage();
        int lastMatureStage = firstMatureStage + block.definition().maxAgeTier();

        for (int stage = firstMatureStage; stage <= lastMatureStage; stage++) {
            matureStages.add(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                    .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(HerbCropBlock.STAGE, stage))
            );
        }

        return AnyOfCondition.anyOf(matureStages.toArray(LootItemCondition.Builder[]::new));
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
