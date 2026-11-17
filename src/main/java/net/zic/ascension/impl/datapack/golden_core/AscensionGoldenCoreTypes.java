package net.zic.ascension.impl.datapack.golden_core;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.datapack.TypeRegistries;
import net.zic.ascension.api.ascension.datapack.golden_core.GoldenCoreType;

public class AscensionGoldenCoreTypes {
    public static final DeferredRegister<GoldenCoreType> GOLDEN_CORE_TYPES =
            DeferredRegister.create(TypeRegistries.GOLDEN_CORE_TYPE_REGISTRY, AscensionCraft.MOD_ID);


    public static final DeferredHolder<GoldenCoreType,GoldenCoreType> SIMPLE_GOLDEN_CORE_TYPE = GOLDEN_CORE_TYPES.register(
            "simple",
            SimpleGoldenCoreType::new
    );


    public static void register(IEventBus eventBus){

        GOLDEN_CORE_TYPES.register(eventBus);
    }
}
