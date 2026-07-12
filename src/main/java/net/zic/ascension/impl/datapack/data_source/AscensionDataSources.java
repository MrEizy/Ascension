package net.zic.ascension.impl.datapack.data_source;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.datapack.TypeRegistries;
import net.zic.ascension.api.datapack.data_source.DataSourceType;
import net.zic.ascension.api.datapack.technique.TechniqueType;
import net.zic.ascension.impl.datapack.technique.SimpleTechniqueType;
import net.zic.ascension.skill_manual.SkillManualSource;

public class AscensionDataSources {
    public static final DeferredRegister<DataSourceType> DATA_SOURCE_TYPES =
            DeferredRegister.create(TypeRegistries.DATA_SOURCE_TYPE_REGISTRY, AscensionCraft.MOD_ID);
    public static final DeferredHolder<DataSourceType,DataSourceType> SKILL_MANUAL_HOLDER_SOURCE_TYPE = DATA_SOURCE_TYPES.register(
            "skill_manual_holder_source",
            SkillManualSource.Type::new
    );

    public static void register(IEventBus eventBus){

        DATA_SOURCE_TYPES.register(eventBus);
    }
}
