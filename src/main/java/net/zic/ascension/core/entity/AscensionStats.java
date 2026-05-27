package net.zic.ascension.core.entity;

import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zic.ascension.AscensionCraft;
import net.zic.zenithlib.ZenithLib;
import net.zic.zenithlib.common.ZenithRegistries;
import net.zic.zenithlib.stats.Stat;

public class AscensionStats {
    public static final DeferredRegister<Stat> STATS =DeferredRegister.create(ZenithRegistries.STAT_REGISTRY, AscensionCraft.MOD_ID);


    public static final DeferredHolder<Stat,Stat> VITALITY = STATS.register("vitality",()->
            new Stat(Component.literal("Vitality")));
    public static final DeferredHolder<Stat,Stat> AGILITY = STATS.register("agility",()->
            new Stat(Component.literal("Agility")));
    public static final DeferredHolder<Stat,Stat> STRENGTH = STATS.register("strength",()->
            new Stat(Component.literal("Strength")));
    public static final DeferredHolder<Stat,Stat> INTELLIGENCE = STATS.register("intelligence",()->
            new Stat(Component.literal("Intelligence")));

    public static void register(IEventBus modEventBus){
        STATS.register(modEventBus);
    }
}
