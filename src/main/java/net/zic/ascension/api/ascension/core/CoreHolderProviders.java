package net.zic.ascension.api.ascension.core;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.bloodline.BloodlineHolderProvider;
import net.zic.ascension.api.ascension.core.path.PathHolderProvider;
import net.zic.ascension.api.ascension.core.path.bonus.PathBonusHolderProvider;
import net.zic.ascension.api.ascension.core.physique.PhysiqueHolderProvider;
import net.zic.ascension.api.ascension.core.skill.SkillHolderProvider;
import net.zic.ascension.api.rpg_engine.RPGEngineRegistries;
import net.zic.ascension.api.rpg_engine.source.data_source.DataSource;

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
    public static final DeferredHolder<DataSource,DataSource> SKILL_HOLDER_PROVIDER = DATA_SOURCES.register(
            "skill_holder_provider",
            SkillHolderProvider::new
    );
    public static final DeferredHolder<DataSource,DataSource> PATH_BONUS_HOLDER_PROVIDER = DATA_SOURCES.register(
            "path_bonus_holder_provider",
            PathBonusHolderProvider::new
    );
    public static void register(IEventBus eventBus){

        DATA_SOURCES.register(eventBus);
    }


}
