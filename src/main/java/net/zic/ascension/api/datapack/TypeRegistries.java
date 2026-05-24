package net.zic.ascension.api.datapack;

import net.minecraft.core.Registry;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.zic.ascension.api.datapack.physique.PhysiqueType;
import net.zic.zenithlib.ZenithLib;
import net.zic.zenithlib.common.ZenithRegistries;
import net.zic.zenithlib.registry.RegistryHelper;


public class TypeRegistries {

    public static final Registry<PhysiqueType> PHYSIQUE_TYPE_REGISTRY = RegistryHelper.registry("physique_type");



    @SubscribeEvent
    public static void registerRegistries(NewRegistryEvent event){
        ZenithLib.LOGGER.info("Creating type registries");
        event.register(PHYSIQUE_TYPE_REGISTRY);
    }
}
