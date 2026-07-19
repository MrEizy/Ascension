package net.zic.ascension.api.core.api_rewrite;

import net.minecraft.core.Registry;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.datapack.physique.PhysiqueType;
import net.zic.zenithlib.registry.RegistryHelper;


@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public class APIRegistries {
    public static final Registry<DataSource> DATA_SOURCE_REGISTRY = RegistryHelper.registry(AscensionCraft.MOD_ID,"data_source");
    @SubscribeEvent
    public static void registerRegistries(NewRegistryEvent event){
        event.register(DATA_SOURCE_REGISTRY);
    }
}
