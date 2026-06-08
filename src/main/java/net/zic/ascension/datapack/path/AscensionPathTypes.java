package net.zic.ascension.datapack.path;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.datapack.TypeRegistries;
import net.zic.ascension.api.datapack.path.PathType;
import net.zic.ascension.api.datapack.physique.PhysiqueType;
import net.zic.ascension.datapack.physique.SimplePhysiqueType;

public class AscensionPathTypes {
    public static final DeferredRegister<PathType> PATH_TYPES =
            DeferredRegister.create(TypeRegistries.PATH_TYPE_REGISTRY, AscensionCraft.MOD_ID);

    public static final DeferredHolder<PathType,PathType> SIMPLE_PATH_TYPE = PATH_TYPES.register(
            "simple_path",
            SimplePathType::new
    );

    public static void register(IEventBus eventBus){

        PATH_TYPES.register(eventBus);
    }
}
