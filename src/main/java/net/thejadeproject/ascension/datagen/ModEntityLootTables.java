package net.thejadeproject.ascension.datagen;

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
import net.neoforged.neoforge.registries.DeferredHolder;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.common.items.ModItems;
import net.thejadeproject.ascension.common.items.data_components.ModDataComponents;
import net.thejadeproject.ascension.entity.ModEntities;

import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

public class ModEntityLootTables extends EntityLootSubProvider {
    public ModEntityLootTables(HolderLookup.Provider provider) {
        super(FeatureFlags.REGISTRY.allFlags(), provider);
    }

    private static final String WHITE_LIGHTNING = AscensionCraft.MOD_ID + ":white_lightning_ten_stage_technique";

    @Override
    public void generate() {
        this.add(ModEntities.RAT.get(), LootTable.lootTable()

                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0f))
                        .when(LootItemRandomChanceCondition.randomChance(0.04f))
                        .add(LootItem.lootTableItem(ModItems.TECHNIQUE_PAGE.get())
                                .apply(SetComponentsFunction.setComponent(
                                        ModDataComponents.TECHNIQUE_ID.get(),
                                        WHITE_LIGHTNING
                                ))
                                .apply(SetComponentsFunction.setComponent(
                                        ModDataComponents.PAGE_INDEX.get(),
                                        2
                                ))
                        )
                )
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0f))
                        .when(LootItemRandomChanceCondition.randomChance(0.04f))
                        .add(LootItem.lootTableItem(ModItems.TECHNIQUE_PAGE.get())
                                .apply(SetComponentsFunction.setComponent(
                                        ModDataComponents.TECHNIQUE_ID.get(),
                                        WHITE_LIGHTNING
                                ))
                                .apply(SetComponentsFunction.setComponent(
                                        ModDataComponents.PAGE_INDEX.get(),
                                        5
                                ))
                        )
                )
        );





        this.add(ModEntities.FAKE_PLAYER.get(), LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0f))

                )
        );
    }

    @Override
    protected @NotNull Stream<EntityType<?>> getKnownEntityTypes() {
        return ModEntities.ENTITY_TYPES.getEntries().stream().map(DeferredHolder::value);
    }


}
