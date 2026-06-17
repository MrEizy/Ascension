package net.zic.ascension.impl.datapack.tribulation;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.datapack.TypeRegistries;
import net.zic.ascension.api.datapack.tribulation.TribulationType;

public class AscensionTribulationTypes {
    public static final DeferredRegister<TribulationType> TRIBULATION_TYPES =
            DeferredRegister.create(TypeRegistries.TRIBULATION_TYPE_REGISTRY, AscensionCraft.MOD_ID);


    public static final DeferredHolder<TribulationType,TribulationType> LIGHTNING_TRIBULATION = TRIBULATION_TYPES.register(
            "lightning",
            LightningTribulationType::new
    );
    public static void register(IEventBus eventBus){

        TRIBULATION_TYPES.register(eventBus);
    }
}
