package net.thejadeproject.ascension.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetComponentsFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.common.blocks.ModBlocks;
import net.thejadeproject.ascension.common.items.ModItems;
import net.thejadeproject.ascension.common.items.data_components.ModDataComponents;
import net.thejadeproject.ascension.datagen.loot.functions.SetRandomIntComponentFunction;
import net.thejadeproject.ascension.datagen.loot.functions.SetTechniquePageFunction;

import java.util.function.BiConsumer;

public class ModChestLootTables implements LootTableSubProvider {

    private final HolderLookup.Provider provider;

    public ModChestLootTables(HolderLookup.Provider provider) {
        this.provider = provider;
    }

    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output) {

        output.accept(
                        ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "barrels/sword_tomb1")),
                        LootTable.lootTable()
                                .withPool(LootPool.lootPool()
                                        .setRolls(UniformGenerator.between(3, 5))
                                        .add(LootItem.lootTableItem(Items.EMERALD)
                                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 4)))
                                                .setWeight(35))
                                        .add(LootItem.lootTableItem(ModItems.TALISMAN_PAPER.get())
                                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 6)))
                                                .setWeight(30))
                                        .add(LootItem.lootTableItem(Items.IRON_SWORD)
                                                .apply(SetItemCountFunction.setCount(ConstantValue.exactly(1)))
                                                .setWeight(25))
                                        .add(LootItem.lootTableItem(Items.IRON_INGOT)
                                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(3, 7)))
                                                .setWeight(20))
                                        .add(LootItem.lootTableItem(ModItems.FASTING_PILL_T1.get())
                                                .setWeight(15))

                                        .add(LootItem.lootTableItem(ModItems.GOLDEN_SUN_LEAF.get())
                                                .setWeight(10))
                                        .add(LootItem.lootTableItem(ModItems.IRONWOOD_SPROUT.get())
                                                .setWeight(8)))

                                .withPool(LootPool.lootPool()
                                        .setRolls(ConstantValue.exactly(1))
                                        .add(LootItem.lootTableItem(ModItems.SPATIAL_RUPTURE_TALISMAN_T1.get())
                                                .apply(SetComponentsFunction.setComponent(ModDataComponents.PERMANENT.get(), true))
                                                .setWeight(60))
                                        .add(LootItem.lootTableItem(ModItems.SOULSTEAD_RETURN_TALISMAN.get())
                                                .apply(SetComponentsFunction.setComponent(ModDataComponents.PERMANENT.get(), true))
                                                .setWeight(30))
                                        .add(EmptyLootItem.emptyItem()
                                                .setWeight(10))));
                output.accept(
                        ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "barrels/sword_tomb2")),
                        LootTable.lootTable()
                                .withPool(LootPool.lootPool()
                                        .setRolls(UniformGenerator.between(3, 6))
                                        .add(LootItem.lootTableItem(Items.DIAMOND)
                                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 3)))
                                                .setWeight(25))
                                        .add(LootItem.lootTableItem(Items.GOLD_INGOT)
                                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(4, 10)))
                                                .setWeight(30))
                                        .add(LootItem.lootTableItem(Items.DIAMOND_SWORD)
                                                .setWeight(15))
                                        .add(LootItem.lootTableItem(ModItems.JADE.get())
                                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 4)))
                                                .setWeight(20))
                                        .add(LootItem.lootTableItem(ModItems.CLEANSING_PILL_T1.get())
                                                .setWeight(15))

                                        .add(LootItem.lootTableItem(ModItems.SPATIAL_STONE_TIER_1.get())
                                                .setWeight(10))
                                        .add(LootItem.lootTableItem(ModItems.LIVING_CORE.get())
                                                .setWeight(8))
                                        .add(LootItem.lootTableItem(Items.ENDER_PEARL)
                                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 5)))
                                                .setWeight(15))
                                        .add(LootItem.lootTableItem(ModItems.ENDER_POUCH.get())
                                                .setWeight(5)))

                                .withPool(LootPool.lootPool()
                                        .setRolls(ConstantValue.exactly(1))
                                        .add(LootItem.lootTableItem(ModItems.SPATIAL_RUPTURE_TALISMAN_T2.get())
                                                .apply(SetComponentsFunction.setComponent(ModDataComponents.PERMANENT.get(), true))
                                                .setWeight(40))
                                        .add(LootItem.lootTableItem(ModItems.WORLD_AXIS_TALISMAN.get())
                                                .apply(SetComponentsFunction.setComponent(ModDataComponents.PERMANENT.get(), true))
                                                .setWeight(30))
                                        .add(LootItem.lootTableItem(ModItems.VOID_MARKING_TALISMAN.get())
                                                .apply(SetComponentsFunction.setComponent(ModDataComponents.PERMANENT.get(), true))
                                                .setWeight(20))
                                        .add(EmptyLootItem.emptyItem()
                                                .setWeight(10))));

        output.accept(
                        ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "barrels/sword_tomb3")),
                        LootTable.lootTable()
                                .withPool(LootPool.lootPool()
                                        .setRolls(UniformGenerator.between(4, 7))
                                        .add(LootItem.lootTableItem(Items.NETHERITE_SCRAP)
                                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 4)))
                                                .setWeight(6))
                                        .add(LootItem.lootTableItem(Items.NETHERITE_SWORD)
                                                .setWeight(10))
                                        .add(LootItem.lootTableItem(Items.NETHERITE_INGOT)
                                                .setWeight(1))
                                        .add(LootItem.lootTableItem(ModItems.CLEANSING_PILL_T3.get())
                                                .setWeight(15))
                                        .add(LootItem.lootTableItem(ModItems.TABLET_OF_DESTRUCTION_HUMAN.get())
                                                .setWeight(8))
                                        .add(LootItem.lootTableItem(ModItems.TABLET_OF_DESTRUCTION_EARTH.get())
                                                .setWeight(5))
                                        .add(LootItem.lootTableItem(ModItems.UNDEAD_CORE.get())
                                                .setWeight(10))
                                        .add(LootItem.lootTableItem(ModItems.FIRE_GOURD.get())
                                                .setWeight(6))
                                        .add(LootItem.lootTableItem(ModItems.REPAIR_SLIP.get())
                                                .setWeight(8)))

                                .withPool(LootPool.lootPool()
                                        .setRolls(ConstantValue.exactly(1))
                                        .add(LootItem.lootTableItem(ModItems.SPATIAL_RUPTURE_TALISMAN_T3.get())
                                                .apply(SetComponentsFunction.setComponent(ModDataComponents.PERMANENT.get(), true))
                                                .setWeight(35))
                                        .add(LootItem.lootTableItem(ModItems.TABLET_OF_DESTRUCTION_HEAVEN.get())
                                                .setWeight(25))
                                        .add(LootItem.lootTableItem(ModItems.WORLD_AXIS_TALISMAN.get())
                                                .apply(SetComponentsFunction.setComponent(ModDataComponents.PERMANENT.get(), true))
                                                .setWeight(20))
                                        .add(LootItem.lootTableItem(ModItems.SPATIAL_RUPTURE_TALISMAN_T2.get())
                                                .apply(SetComponentsFunction.setComponent(ModDataComponents.PERMANENT.get(), true))
                                                .setWeight(10))
                                        .add(EmptyLootItem.emptyItem()
                                                .setWeight(10))));


        output.accept(
                ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "barrel/ironwood_island")),
                LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .setRolls(UniformGenerator.between(1, 3))
                                .add(LootItem.lootTableItem(ModItems.SPIRITUAL_MEAL)
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 1)))
                                        .setWeight(9))
                                .add(LootItem.lootTableItem(ModItems.QI_REPLENISHING_PILL.get())
                                        .apply(SetRandomIntComponentFunction.builder(ModDataComponents.PILL_MAJOR_REALM.get(), 1, 3))
                                        .apply(SetRandomIntComponentFunction.builder(ModDataComponents.PILL_PURITY.get(), 10, 100))
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 3)))
                                        .setWeight(21))
                                .add(LootItem.lootTableItem(ModItems.NETHERITE_BLADE)
                                        .apply(SetItemCountFunction.setCount(ConstantValue.exactly(1)))
                                        .setWeight(15))
                                .add(LootItem.lootTableItem(ModItems.SILVER_NEEDLE)
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(3, 7)))
                                        .setWeight(17))
                                .add(LootItem.lootTableItem(ModItems.JADE_DEW_GRASS.get())
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 6)))
                                        .setWeight(10))
                                .add(LootItem.lootTableItem(ModItems.CRIMSON_LOTUS_FLAME.get())
                                        .setWeight(8))
                                .add(LootItem.lootTableItem(Items.ENCHANTED_GOLDEN_APPLE)
                                        .setWeight(13))
                        )
                        .withPool(LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1))
                                .add(LootItem.lootTableItem(ModItems.TECHNIQUE_PAGE.get())
                                        .apply(SetTechniquePageFunction.builder(ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "bloodfeast_soul_refining_scripture"), 0, 4))
                                        .setWeight(7))
                                .add(LootItem.lootTableItem(Items.EXPERIENCE_BOTTLE)
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 6)))
                                        .setWeight(32))
                                .add(LootItem.lootTableItem(Items.DIAMOND)
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 6)))
                                        .setWeight(21))
                                .add(LootItem.lootTableItem(ModItems.FROST_SILVER_INGOT)
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 6)))
                                        .setWeight(32))));
    }
}