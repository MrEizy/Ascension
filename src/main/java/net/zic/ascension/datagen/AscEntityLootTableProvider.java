package net.zic.ascension.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.EntityLootSubProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetComponentsFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.entities.AscEntities;
import net.zic.ascension.common.item.ModItems;
import net.zic.ascension.common.item.components.AscensionComponents;

import java.util.stream.Stream;

public class AscEntityLootTableProvider extends EntityLootSubProvider {
    public AscEntityLootTableProvider(HolderLookup.Provider lookupProvider) {
        super(FeatureFlags.DEFAULT_FLAGS, lookupProvider);
    }

    @Override
    public void generate() {
        npcLoot(AscEntities.MALE_CULTIVATOR.get());
        npcLoot(AscEntities.FEMALE_CULTIVATOR.get());
        npcLoot(AscEntities.SECT_ELDER.get());
        npcLoot(AscEntities.SECT_MASTER.get());
        npcLoot(AscEntities.YOUNG_MASTER.get());
    }

    private void npcLoot(EntityType<?> type) {
        add(type, LootTable.lootTable().withPool(LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .when(LootItemRandomChanceCondition.randomChance(0.2f))
                .add(LootItem.lootTableItem(ModItems.JADE.get()))));
    }

    @Override
    protected Stream<EntityType<?>> getKnownEntityTypes() {
        return Stream.of(AscEntities.MALE_CULTIVATOR.get(), AscEntities.FEMALE_CULTIVATOR.get(),
                AscEntities.SECT_ELDER.get(), AscEntities.SECT_MASTER.get(), AscEntities.YOUNG_MASTER.get());
    }
}
