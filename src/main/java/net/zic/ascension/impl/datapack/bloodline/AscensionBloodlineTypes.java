package net.zic.ascension.impl.datapack.bloodline;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.datapack.TypeRegistries;
import net.zic.ascension.api.ascension.datapack.bloodline.BloodlineType;

public class AscensionBloodlineTypes {
    public static final DeferredRegister<BloodlineType> BLOODLINE_TYPES =
            DeferredRegister.create(TypeRegistries.BLOODLINE_TYPE_REGISTRY, AscensionCraft.MOD_ID);

    public static final DeferredHolder<BloodlineType,BloodlineType> SIMPLE_BLOODLINE_TYPE = BLOODLINE_TYPES.register(
            "simple",
            SimpleBloodlineType::new
    );


    public static final DeferredHolder<BloodlineType,BloodlineType> CONNECTED_BLOODLINE_TYPE = BLOODLINE_TYPES.register(
            "connected_bloodline",
            ConnectedBloodlineType::new
    );
    public static final DeferredHolder<BloodlineType,BloodlineType> EVOLUTION_BLOODLINE_TYPE = BLOODLINE_TYPES.register(
            "evolution_bloodline",
            EvolutionBloodlineType::new
    );

    public static void register(IEventBus eventBus){

        BLOODLINE_TYPES.register(eventBus);
    }
}
