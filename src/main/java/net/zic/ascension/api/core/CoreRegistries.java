package net.zic.ascension.api.core;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.zic.ascension.api.core.bloodline.Bloodline;
import net.zic.ascension.api.core.data_source.DataSource;
import net.zic.ascension.api.core.path.Path;
import net.zic.ascension.api.core.physique.Physique;
import net.zic.ascension.api.core.skill.Skill;
import net.zic.ascension.api.core.technique.Technique;
import net.zic.ascension.api.datapack.bloodline.BloodlineType;
import net.zic.ascension.api.datapack.data_source.DataSourceType;
import net.zic.ascension.api.datapack.path.PathType;
import net.zic.ascension.api.datapack.physique.PhysiqueType;
import net.zic.ascension.api.datapack.skill.SkillType;
import net.zic.ascension.api.datapack.technique.TechniqueType;
import net.zic.zenithlib.ZenithLib;
import net.zic.zenithlib.registry.RegistryHelper;

public class CoreRegistries {

    public static final RegistryHelper.DataPackRegistry<Physique> PHYSIQUE_REGISTRY = RegistryHelper.dataPackRegistry(
            "physiques",
            ZenithLib.MOD_ID,
            ()->PhysiqueType.PHYSIQUE_CODEC
    );
    public static final RegistryHelper.DataPackRegistry<Bloodline> BLOODLINE_REGISTRY = RegistryHelper.dataPackRegistry(
            "bloodlines",
            ZenithLib.MOD_ID,
            ()-> BloodlineType.BLOODLINE_CODEC
    );
    public static final RegistryHelper.DataPackRegistry<Technique> TECHNIQUE_REGISTRY = RegistryHelper.dataPackRegistry(
            "technique",
            ZenithLib.MOD_ID,
            ()-> TechniqueType.TECHNIQUE_CODEC
    );
    public static final RegistryHelper.DataPackRegistry<Path> PATH_REGISTRY = RegistryHelper.dataPackRegistry(
            "paths",
            ZenithLib.MOD_ID,
            ()-> PathType.PATH_CODEC
    );
    public static final RegistryHelper.DataPackRegistry<Skill> SKILL_REGISTRY = RegistryHelper.dataPackRegistry(
            "skills",
            ZenithLib.MOD_ID,
            ()-> SkillType.SKILL_CODEC
    );
    public static final RegistryHelper.DataPackRegistry<DataSource> DATA_SOURCE_REGISTRY = RegistryHelper.dataPackRegistry(
            "data_sources",
            ZenithLib.MOD_ID,
            ()-> DataSourceType.DATA_SOURCE_CODEC
    );

    //TODO think about if tribulations need their own registry or not. aka do we want them to make them in place
    //TODO like realm change, or do we want them to be able to define them elsewhere then reuse them using the registry (prob 2)


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
