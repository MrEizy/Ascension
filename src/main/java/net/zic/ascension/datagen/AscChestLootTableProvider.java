package net.zic.ascension.datagen;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.SetComponentsFunction;
import net.minecraft.world.level.storage.loot.functions.SetEnchantmentsFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.item.ModItems;
import net.zic.ascension.common.item.components.AscensionComponents;
import net.zic.ascension.datagen.loot.JadeMedicineLootFunction;

import java.util.List;

import java.util.function.BiConsumer;

public class AscChestLootTableProvider implements LootTableSubProvider {
    public static final ResourceKey<LootTable> ALCHEMIST_CACHE_BARREL = key("barrels/alchemist_cache");
    public static final ResourceKey<LootTable> ANCIENT_ALCHEMIST_CACHE_BARREL = key("barrels/ancient_alchemist_cache");
    public static final ResourceKey<LootTable> SPIRITUAL_CAVERN_BARREL = key("barrels/spiritual_cavern");
    // Referenced by the barrels in sword_tomb1/2/3.nbt
    public static final ResourceKey<LootTable> SWORD_TOMB_1_BARREL = key("barrels/sword_tomb1");
    public static final ResourceKey<LootTable> SWORD_TOMB_2_BARREL = key("barrels/sword_tomb2");
    public static final ResourceKey<LootTable> SWORD_TOMB_3_BARREL = key("barrels/sword_tomb3");
    // Referenced by the barrel in floating_ironwood_island.nbt
    public static final ResourceKey<LootTable> IRONWOOD_ISLAND_BARREL = key("barrels/ironwood_island");
    // Referenced by the 4 corner barrels (in the scaffolding stacks) in pagoda_large.nbt
    public static final ResourceKey<LootTable> PAGODA_CORNER_BARREL = key("barrels/corner_barrel");
    // Referenced by the barrel under the central pillar in pagoda_large.nbt
    public static final ResourceKey<LootTable> PAGODA_PILLAR_BARREL = key("barrels/pillar_under_barrel");
    // Referenced by the barrel on the top ceiling in pagoda_large.nbt
    public static final ResourceKey<LootTable> PAGODA_TOP_BARREL = key("barrels/barrel_ceiling");

    private final HolderLookup.Provider registries;

    public AscChestLootTableProvider(HolderLookup.Provider registries) {
        this.registries = registries;
    }

    private static ResourceKey<LootTable> key(String path) {
        return ResourceKey.create(Registries.LOOT_TABLE, Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, path));
    }

    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> consumer) {
        consumer.accept(ALCHEMIST_CACHE_BARREL, LootTable.lootTable()
                .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                        .add(LootItem.lootTableItem(ModItems.JADE_BOTTLE.get())
                                .apply(() -> new JadeMedicineLootFunction(List.of(
                                        AscensionCraft.prefix("jade_dew_grass"),
                                        AscensionCraft.prefix("frostglass_lotus")), 3)))));

        consumer.accept(ANCIENT_ALCHEMIST_CACHE_BARREL, LootTable.lootTable()
                .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                        .add(LootItem.lootTableItem(ModItems.JADE_BOTTLE.get())
                                .apply(() -> new JadeMedicineLootFunction(List.of(
                                        AscensionCraft.prefix("jade_dew_grass"),
                                        AscensionCraft.prefix("frostglass_lotus")), 1,
                                        java.util.Optional.empty(), java.util.Optional.of(0.03D))))));

        consumer.accept(SPIRITUAL_CAVERN_BARREL, LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .add(LootItem.lootTableItem(ModItems.JADE_BOTTLE.get())
                                .apply(() -> new JadeMedicineLootFunction(List.of(
                                        AscensionCraft.prefix("jade_dew_grass"),
                                        AscensionCraft.prefix("frostglass_lotus")), 3)))
                        .when(LootItemRandomChanceCondition.randomChance(0.12F)))
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .add(LootItem.lootTableItem(ModItems.JADE_BOTTLE.get())
                                .apply(() -> new JadeMedicineLootFunction(List.of(
                                        AscensionCraft.prefix("jade_dew_grass"),
                                        AscensionCraft.prefix("frostglass_lotus")), 1,
                                        java.util.Optional.empty(), java.util.Optional.of(0.03D))))
                        .when(LootItemRandomChanceCondition.randomChance(0.025F)))
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
                        .add(LootItem.lootTableItem(ModItems.JADE_BOTTLE.get())
                                .apply(() -> new JadeMedicineLootFunction(List.of(
                                        AscensionCraft.prefix("jade_dew_grass"),
                                        AscensionCraft.prefix("frostglass_lotus")), 2))
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

        consumer.accept(PAGODA_CORNER_BARREL, LootTable.lootTable()
                // 4 slots roll a plain book, each 70%
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(4))
                        .add(LootItem.lootTableItem(Items.BOOK)
                                .when(LootItemRandomChanceCondition.randomChance(0.7f))))
                // Two enchanted books always, a third with 50%
                .withPool(pagodaEnchantedBooks().setRolls(ConstantValue.exactly(2)))
                .withPool(pagodaEnchantedBooks().setRolls(ConstantValue.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.5f)))
                // A few paper stacks; the remaining slots stay empty
                .withPool(LootPool.lootPool()
                        .setRolls(UniformGenerator.between(1, 2))
                        .add(LootItem.lootTableItem(Items.PAPER)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 4)))))
                // One slot: each manual 12%, nothing 52%
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .add(techniqueManual("2_profound/rigid_stance_summation_technique").setWeight(12))
                        .add(techniqueManual("2_profound/white_lightning_ten_stage_technique").setWeight(12))
                        .add(techniqueManual("2_profound/sanguine_abyss_technique").setWeight(12))
                        .add(techniqueManual("2_profound/scholarly_soul_technique").setWeight(12))
                        .add(EmptyLootItem.emptyItem().setWeight(52))));

        consumer.accept(PAGODA_PILLAR_BARREL, LootTable.lootTable()
                .withPool(AscAddedLootTableProvider.bloodlinePool(AscensionCraft.prefix("3_heaven/golden_silkworm_bloodline"), 28, 63)
                        .when(LootItemRandomChanceCondition.randomChance(0.065f)))
                .withPool(chancePhysique("3_heaven/tyrant_body", 0.065f))
                // 8 slots of spirit stones, each 50%
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(8))
                        .add(LootItem.lootTableItem(ModItems.SPIRITUAL_STONE.get())
                                .when(LootItemRandomChanceCondition.randomChance(0.5f))
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 2)))))
                // One enchanted book always, a second with 25%
                .withPool(pagodaPillarEnchantedBooks().setRolls(ConstantValue.exactly(1)))
                .withPool(pagodaPillarEnchantedBooks().setRolls(ConstantValue.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.25f)))
                // Heaven tablets: 3 always, 25% for 2 more (5 in total), one per roll
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(3))
                        .add(LootItem.lootTableItem(ModItems.TABLET_OF_DESTRUCTION_HEAVEN.get())))
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(2))
                        .when(LootItemRandomChanceCondition.randomChance(0.25f))
                        .add(LootItem.lootTableItem(ModItems.TABLET_OF_DESTRUCTION_HEAVEN.get())))
                // A few books and paper; at most 25 of the 27 slots fill, usually far fewer
                .withPool(LootPool.lootPool()
                        .setRolls(UniformGenerator.between(2, 4))
                        .add(LootItem.lootTableItem(Items.BOOK)))
                .withPool(LootPool.lootPool()
                        .setRolls(UniformGenerator.between(2, 4))
                        .add(LootItem.lootTableItem(Items.PAPER)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 4))))));

        consumer.accept(PAGODA_TOP_BARREL, LootTable.lootTable()
                .withPool(AscAddedLootTableProvider.bloodlinePool(AscensionCraft.prefix("3_heaven/nine_tailed_fox_bloodline"), 28, 63)
                        .when(LootItemRandomChanceCondition.randomChance(0.065f)))
                .withPool(chancePhysique("3_heaven/nine_life_demon_cat_body_physique", 0.065f))
                // 4 slots of spirit stones, each 65%
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(4))
                        .add(LootItem.lootTableItem(ModItems.SPIRITUAL_STONE.get())
                                .when(LootItemRandomChanceCondition.randomChance(0.65f))
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 4)))))
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.05f))
                        .add(LootItem.lootTableItem(Items.ENCHANTED_GOLDEN_APPLE)))
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.1f))
                        .add(LootItem.lootTableItem(Items.GOLDEN_APPLE)
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 5)))))
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .add(LootItem.lootTableItem(ModItems.BLACK_IRON_SWORD.get())))
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.8f))
                        .add(techniqueManual("3_heaven/five_element_body_technique")))
                // A few plain books; at most 14 of the 27 slots fill
                .withPool(LootPool.lootPool()
                        .setRolls(UniformGenerator.between(2, 4))
                        .add(LootItem.lootTableItem(Items.BOOK))));
    }

    /** One enchanted book per roll, equally weighted: Mending, Efficiency V, Curse of Binding. */
    private LootPool.Builder pagodaPillarEnchantedBooks() {
        HolderLookup.RegistryLookup<Enchantment> enchantments = registries.lookupOrThrow(Registries.ENCHANTMENT);
        return LootPool.lootPool()
                .add(enchantedBook(enchantments.getOrThrow(Enchantments.MENDING), ConstantValue.exactly(1)))
                .add(enchantedBook(enchantments.getOrThrow(Enchantments.EFFICIENCY), ConstantValue.exactly(5)))
                .add(enchantedBook(enchantments.getOrThrow(Enchantments.BINDING_CURSE), ConstantValue.exactly(1)));
    }

    private static LootPoolSingletonContainer.Builder<?> techniqueManual(String technique) {
        return LootItem.lootTableItem(ModItems.TECHNIQUE_MANUAL.get())
                .apply(SetComponentsFunction.setComponent(AscensionComponents.REGISTRY_ID_HOLDER.get(),
                        AscensionCraft.prefix(technique)));
    }

    /** One enchanted book per roll, equally weighted: Unbreaking III, Silk Touch, Efficiency I-III, Sharpness I-II. */
    private LootPool.Builder pagodaEnchantedBooks() {
        HolderLookup.RegistryLookup<Enchantment> enchantments = registries.lookupOrThrow(Registries.ENCHANTMENT);
        return LootPool.lootPool()
                .add(enchantedBook(enchantments.getOrThrow(Enchantments.UNBREAKING), ConstantValue.exactly(3)))
                .add(enchantedBook(enchantments.getOrThrow(Enchantments.SILK_TOUCH), ConstantValue.exactly(1)))
                .add(enchantedBook(enchantments.getOrThrow(Enchantments.EFFICIENCY), UniformGenerator.between(1, 3)))
                .add(enchantedBook(enchantments.getOrThrow(Enchantments.SHARPNESS), UniformGenerator.between(1, 2)));
    }

    private static LootItem.Builder<?> enchantedBook(Holder<Enchantment> enchantment, NumberProvider level) {
        return LootItem.lootTableItem(Items.BOOK)
                .apply(new SetEnchantmentsFunction.Builder().withEnchantment(enchantment, level));
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
