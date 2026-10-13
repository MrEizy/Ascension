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
        add(AscEntities.TREASURE_RAT.get(), LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1))
                        .when(LootItemRandomChanceCondition.randomChance(0.08f))
                        .add(LootItem.lootTableItem(ModItems.TECHNIQUE_MANUAL.get())
                                .apply(SetComponentsFunction.setComponent(
                                        AscensionComponents.REGISTRY_ID_HOLDER.get(),
                                        AscensionCraft.prefix("2_profound/white_lightning_ten_stage_technique"))))));
    }

    @Override
    protected Stream<EntityType<?>> getKnownEntityTypes() {
        return Stream.of(AscEntities.TREASURE_RAT.get());
    }
}
