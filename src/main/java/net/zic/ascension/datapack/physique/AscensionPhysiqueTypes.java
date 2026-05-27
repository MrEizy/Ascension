package net.zic.ascension.datapack.physique;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.datapack.TypeRegistries;
import net.zic.ascension.api.datapack.physique.PhysiqueType;

public class AscensionPhysiqueTypes {
    public static final DeferredRegister<PhysiqueType> PHYSIQUE_TYPES =
            DeferredRegister.create(TypeRegistries.PHYSIQUE_TYPE_REGISTRY, AscensionCraft.MOD_ID);

    public static final DeferredHolder<PhysiqueType,PhysiqueType> SIMPLE_PHYSIQUE_TYPE = PHYSIQUE_TYPES.register(
            "simple",
            SimplePhysiqueType::new
    );

    public static void register(IEventBus eventBus){

        PHYSIQUE_TYPES.register(eventBus);
    }
}
