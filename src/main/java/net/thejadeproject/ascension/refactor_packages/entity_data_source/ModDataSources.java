package net.thejadeproject.ascension.refactor_packages.entity_data_source;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.refactor_packages.entity_data_source.custom.SkillSource;
import net.thejadeproject.ascension.refactor_packages.entity_data_source.custom.TemporarySkill;
import net.thejadeproject.ascension.refactor_packages.paths.IPath;
import net.thejadeproject.ascension.refactor_packages.paths.custom.GenericPath;
import net.thejadeproject.ascension.refactor_packages.registries.AscensionRegistries;
import net.thejadeproject.ascension.refactor_packages.skills.ISkill;

public class ModDataSources {
    public static final DeferredRegister<IEntityDataSource> DATA_SOURCES =DeferredRegister.create(AscensionRegistries.EntityDataSources.ENTITY_DATA_SOURCES_REGISTRY, AscensionCraft.MOD_ID);

    public static final DeferredHolder<IEntityDataSource, ? extends TemporarySkill> TEMP_SKILL_SOURCE = DATA_SOURCES.register("temp_skill_source",TemporarySkill::new);

    public static final DeferredHolder<IEntityDataSource, ? extends SkillSource> SKILL_SOURCE = DATA_SOURCES.register("skill_source",SkillSource::new);


    public static void register(IEventBus event){
        DATA_SOURCES.register(event);
    }
}
