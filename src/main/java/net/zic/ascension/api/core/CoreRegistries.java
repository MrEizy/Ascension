package net.zic.ascension.api.core;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.zic.ascension.api.core.physique.Physique;
import net.zic.ascension.api.datapack.physique.PhysiqueType;
import net.zic.zenithlib.ZenithLib;
import net.zic.zenithlib.registry.RegistryHelper;

public class CoreRegistries {

    public static final RegistryHelper.DataPackRegistry<Physique> PHYSIQUE_REGISTRY = RegistryHelper.dataPackRegistry(
            "physiques",
            ZenithLib.MOD_ID,
            ()->PhysiqueType.PHYSIQUE_CODEC
    );


    @SubscribeEvent
    public static void registerDatapackRegistries(DataPackRegistryEvent.NewRegistry event) {
        ZenithLib.LOGGER.info("creating core registries");
        event.dataPackRegistry(
                PHYSIQUE_REGISTRY.key(),
                PHYSIQUE_REGISTRY.codec().get(),
                PHYSIQUE_REGISTRY.codec().get()
        );
    }
}
