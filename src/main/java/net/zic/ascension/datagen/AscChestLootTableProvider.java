package net.zic.ascension.datagen;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.item.ModItems;

import java.util.function.BiConsumer;

public class AscChestLootTableProvider implements LootTableSubProvider {
    public static final ResourceKey<LootTable> SPIRITUAL_CAVERN_BARREL = key("barrels/spiritual_cavern");
    // Referenced by the barrels in sword_tomb1/2/3.nbt
    public static final ResourceKey<LootTable> SWORD_TOMB_1_BARREL = key("barrels/sword_tomb1");
    public static final ResourceKey<LootTable> SWORD_TOMB_2_BARREL = key("barrels/sword_tomb2");
    public static final ResourceKey<LootTable> SWORD_TOMB_3_BARREL = key("barrels/sword_tomb3");
    // Referenced by the barrel in floating_ironwood_island.nbt
    public static final ResourceKey<LootTable> IRONWOOD_ISLAND_BARREL = key("barrels/ironwood_island");

    private static ResourceKey<LootTable> key(String path) {
        return ResourceKey.create(Registries.LOOT_TABLE, Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, path));
    }

    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> consumer) {
        consumer.accept(SPIRITUAL_CAVERN_BARREL, LootTable.lootTable()
                .withPool(AscAddedLootTableProvider.physiquePool(AscensionCraft.prefix("2_profound/cold_spring_dantian_physique")))

                .withPool(LootPool.lootPool()
                        .setRolls(UniformGenerator.between(5, 11))
                        .add(LootItem.lootTableItem(ModItems.SPIRITUAL_STONE.get())))
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .add(LootItem.lootTableItem(Items.BONE)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 5)))))
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .add(LootItem.lootTableItem(Items.ROTTEN_FLESH)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 3))))));

        consumer.accept(SWORD_TOMB_1_BARREL, LootTable.lootTable()
                .withPool(chancePhysique("1_ordinary/sword_bone_physique", 0.45f))
                .withPool(chancePhysique("2_profound/sword_body_flawed_physique", 0.4f))
                .withPool(swordTombSpiritStone())
                .withPool(swordTombJadeSlots())
                .withPool(swordTombDiamonds())
                .withPool(swordTombBlade())
                .withPool(swordTombJade()));
        consumer.accept(SWORD_TOMB_2_BARREL, LootTable.lootTable()
                .withPool(chancePhysique("2_profound/moonjade_marrow_physique", 0.15f))
                .withPool(chancePhysique("2_profound/sword_body_flawed_physique", 0.4f))
                .withPool(swordTombSpiritStone())
                .withPool(swordTombJadeSlots())
                .withPool(swordTombDiamonds())
                .withPool(swordTombBlade())
                .withPool(swordTombJade()));
        consumer.accept(SWORD_TOMB_3_BARREL, LootTable.lootTable()
                .withPool(chancePhysique("2_profound/thousand_edge_meridians_physique", 0.15f))
                .withPool(chancePhysique("2_profound/sword_body_flawed_physique", 0.4f))
                .withPool(swordTombSpiritStone())
                .withPool(swordTombJadeSlots())
                .withPool(swordTombDiamonds())
                .withPool(swordTombBlade())
                .withPool(swordTombJade()));

        consumer.accept(IRONWOOD_ISLAND_BARREL, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(UniformGenerator.between(1, 3))
                        .add(LootItem.lootTableItem(ModItems.QI_REPLENISHING_PILL.get())
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 3)))
                                .setWeight(21))
                        .add(LootItem.lootTableItem(ModItems.NETHERITE_BLADE.get()).setWeight(15))
                        .add(LootItem.lootTableItem(ModItems.JADE_DEW_GRASS.get())
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 6)))
                                .setWeight(10))
                        .add(LootItem.lootTableItem(Items.ENCHANTED_GOLDEN_APPLE).setWeight(13)))
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .add(LootItem.lootTableItem(Items.EXPERIENCE_BOTTLE)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 6)))
                                .setWeight(32))
                        .add(LootItem.lootTableItem(Items.DIAMOND)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 6)))
                                .setWeight(21))
                        .add(LootItem.lootTableItem(ModItems.FROST_SILVER_INGOT.get())
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 6)))
                                .setWeight(32))));
    }

    /** A single spirit stone with a 15% chance. */
    private static LootPool.Builder swordTombSpiritStone() {
        return LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(ModItems.SPIRITUAL_STONE.get()))
                .when(LootItemRandomChanceCondition.randomChance(0.15f));
    }

    /** 3-7 jade, one per roll so each lands in its own slot. */
    private static LootPool.Builder swordTombJadeSlots() {
        return LootPool.lootPool()
                .setRolls(UniformGenerator.between(3, 7))
                .add(LootItem.lootTableItem(ModItems.JADE.get()));
    }

    private static LootPool.Builder swordTombDiamonds() {
        return LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(Items.DIAMOND)
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 2))));
    }

    /** At most one blade: iron 50%, diamond 10%, netherite 1%, nothing 39%. */
    private static LootPool.Builder swordTombBlade() {
        return LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(ModItems.IRON_BLADE.get()).setWeight(50))
                .add(LootItem.lootTableItem(ModItems.DIAMOND_BLADE.get()).setWeight(10))
                .add(LootItem.lootTableItem(ModItems.NETHERITE_BLADE.get()).setWeight(1))
                .add(EmptyLootItem.emptyItem().setWeight(39));
    }

    private static LootPool.Builder swordTombJade() {
        return LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(ModItems.JADE.get())
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(0, 3))));
    }

    /** A physique essence that appears with the given chance, rolled independently of other pools. */
    private static LootPool.Builder chancePhysique(String physique, float chance) {
        return AscAddedLootTableProvider.physiquePool(AscensionCraft.prefix(physique))
                .when(LootItemRandomChanceCondition.randomChance(chance));
    }
}
