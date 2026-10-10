package net.zic.ascension.api.ascension.core;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.bloodline.BloodlineDataSource;
import net.zic.ascension.api.ascension.core.path.bonus.data_source.PathBonusHolderProvider;
import net.zic.ascension.api.ascension.core.path.PathDataSource;
import net.zic.ascension.api.ascension.core.physique.PhysiqueDataSource;
import net.zic.ascension.api.ascension.core.skill.data_source.SkillDataSource;
import net.zic.ascension.api.ascension.core.technique.TechniqueDataSource;
import net.zic.ascension.api.rpg_engine.RPGEngineRegistries;
import net.zic.ascension.api.rpg_engine.source.data_source.DataSource;

public class CoreDataSources {

    public static final DeferredRegister<DataSource<?>> DATA_SOURCES =
            DeferredRegister.create(RPGEngineRegistries.DATA_SOURCE_REGISTRY, AscensionCraft.MOD_ID);

    public static final DeferredHolder<DataSource<?>, PhysiqueDataSource> PHYSIQUE_DATA_SOURCE = DATA_SOURCES.register(
            "physique_data_source",
            PhysiqueDataSource::new
    );
    public static final DeferredHolder<DataSource<?>,BloodlineDataSource> BLOODLINE_DATA_SOURCE = DATA_SOURCES.register(
            "bloodline_data_source",
            BloodlineDataSource::new
    );
    public static final DeferredHolder<DataSource<?>, TechniqueDataSource> TECHNIQUE_DATA_SOURCE = DATA_SOURCES.register(
            "technique_data_source",
            TechniqueDataSource::new
    );
    public static final DeferredHolder<DataSource<?>, PathDataSource> PATH_DATA_SOURCE = DATA_SOURCES.register(
            "path_data_source",
            PathDataSource::new
    );
    public static final DeferredHolder<DataSource<?>, SkillDataSource> SKILL_DATA_SOURCE = DATA_SOURCES.register(
            "skill_data_source",
            SkillDataSource::new
    );
    public static final DeferredHolder<DataSource<?>,DataSource> PATH_BONUS_HOLDER_PROVIDER = DATA_SOURCES.register(
            "path_bonus_data_source",
            PathBonusHolderProvider::new
    );
    public static void register(IEventBus eventBus){

        DATA_SOURCES.register(eventBus);
    }


}
