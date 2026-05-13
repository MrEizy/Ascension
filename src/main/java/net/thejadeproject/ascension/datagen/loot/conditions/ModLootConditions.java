package net.thejadeproject.ascension.datagen.loot.conditions;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.thejadeproject.ascension.AscensionCraft;

public final class ModLootConditions {
    private ModLootConditions() {
    }

    public static final DeferredRegister<LootItemConditionType> LOOT_CONDITION_TYPES =
            DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, AscensionCraft.MOD_ID);

    public static final DeferredHolder<LootItemConditionType, LootItemConditionType> MOB_RANK =
            LOOT_CONDITION_TYPES.register("mob_rank",
                    () -> new LootItemConditionType(MobRankLootCondition.CODEC));

    public static void register(IEventBus eventBus) {
        LOOT_CONDITION_TYPES.register(eventBus);
    }
}