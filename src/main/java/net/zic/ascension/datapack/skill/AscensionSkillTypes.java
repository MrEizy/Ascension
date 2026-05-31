package net.zic.ascension.datapack.skill;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.datapack.TypeRegistries;
import net.zic.ascension.api.datapack.physique.PhysiqueType;
import net.zic.ascension.api.datapack.skill.SkillType;
import net.zic.ascension.core.skill.SimplePassiveSkill;
import net.zic.ascension.datapack.physique.SimplePhysiqueType;
import net.zic.ascension.datapack.skill.passive.SimplePassiveSkillType;

public class AscensionSkillTypes {
    public static final DeferredRegister<SkillType> SKILL_TYPES =
            DeferredRegister.create(TypeRegistries.SKILL_TYPE_REGISTRY, AscensionCraft.MOD_ID);

    public static final DeferredHolder<SkillType,SkillType> SIMPLE_PASSIVE_SKILL_TYPE = SKILL_TYPES.register(
            "simple_passive",
            SimplePassiveSkillType::new
    );
    public static void register(IEventBus eventBus){

        SKILL_TYPES.register(eventBus);
    }
}
