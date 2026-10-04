package net.zic.ascension.datagen.loot;

import com.mojang.serialization.MapCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.zic.ascension.AscensionCraft;

import java.util.function.Supplier;

public class ModLootModifiers {
    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> LOOT_MODIFIERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, AscensionCraft.MOD_ID);

    public static final Supplier<MapCodec<AddItemStackModifier>> ADD_ITEMSTACK =
            LOOT_MODIFIERS.register("add_itemstack", () -> AddItemStackModifier.CODEC);


    public static void register(IEventBus eventBus) {
        LOOT_MODIFIERS.register(eventBus);
    }
}