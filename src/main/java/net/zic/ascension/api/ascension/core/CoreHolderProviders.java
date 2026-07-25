package net.zic.ascension.api.ascension.core;

import net.minecraft.core.Registry;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.bloodline.BloodlineHolderProvider;
import net.zic.ascension.api.ascension.core.path.PathHolderProvider;
import net.zic.ascension.api.ascension.core.physique.PhysiqueHolderProvider;
import net.zic.ascension.api.ascension.datapack.TypeRegistries;
import net.zic.ascension.api.ascension.datapack.path.PathType;
import net.zic.ascension.api.ascension.datapack.physique.PhysiqueType;
import net.zic.ascension.api.rpg_engine.RPGEngineRegistries;
import net.zic.ascension.api.rpg_engine.source.data_source.DataSource;
import net.zic.ascension.impl.datapack.path.FoundationPathType;
import net.zic.ascension.impl.datapack.path.SimplePathType;
import net.zic.zenithlib.registry.RegistryHelper;

public class CoreHolderProviders {

    public static final DeferredRegister<DataSource> DATA_SOURCES =
            DeferredRegister.create(RPGEngineRegistries.DATA_SOURCE_REGISTRY, AscensionCraft.MOD_ID);

    public static final DeferredHolder<DataSource,DataSource> PHYSIQUE_HOLDER_PROVIDER = DATA_SOURCES.register(
            "physique_holder_provider",
            PhysiqueHolderProvider::new
    );
    public static final DeferredHolder<DataSource,DataSource> BLOODLINE_HOLDER_PROVIDER = DATA_SOURCES.register(
            "bloodline_holder_provider",
            BloodlineHolderProvider::new
    );
    public static final DeferredHolder<DataSource,DataSource> PATH_HOLDER_PROVIDER = DATA_SOURCES.register(
            "path_holder_provider",
            PathHolderProvider::new
    );

    public static void register(IEventBus eventBus){

        DATA_SOURCES.register(eventBus);
    }


}
