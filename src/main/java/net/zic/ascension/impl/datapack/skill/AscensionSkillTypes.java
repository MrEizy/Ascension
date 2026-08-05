package net.zic.ascension.impl.datapack.skill;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.datapack.TypeRegistries;
import net.zic.ascension.api.ascension.datapack.skill.SkillType;
import net.zic.ascension.impl.datapack.skill.castable.DebugCastableType;
import net.zic.ascension.impl.datapack.skill.castable.cultivation.SimpleCultivationSkillType;
import net.zic.ascension.impl.datapack.skill.passive.SimplePassiveSkillType;
import net.zic.ascension.impl.datapack.skill.passive.ToggleablePassiveSkillType;

public class AscensionSkillTypes {
    public static final DeferredRegister<SkillType> SKILL_TYPES =
            DeferredRegister.create(TypeRegistries.SKILL_TYPE_REGISTRY, AscensionCraft.MOD_ID);

    public static final DeferredHolder<SkillType,SkillType> SIMPLE_PASSIVE_SKILL_TYPE = SKILL_TYPES.register(
            "simple_passive",
            SimplePassiveSkillType::new
    );
    public static final DeferredHolder<SkillType,SkillType> TOGGLEABLE_PASSIVE_SKILL_TYPE = SKILL_TYPES.register(
            "toggleable_passive",
            ToggleablePassiveSkillType::new
    );
    public static final DeferredHolder<SkillType,SkillType> RESOURCE_MODIFIER_PASSIVE_SKILL_TYPE = SKILL_TYPES.register(
            "resource_modifier_passive",
            ResourceModifierPassiveSkillType::new
    );



    //──Castable Skills────────────────────────────────────────────────────────
    public static final DeferredHolder<SkillType,SkillType> ACTIVE_SKILL_TYPE = SKILL_TYPES.register(
            "active_skill",
            ActiveSkillType::new
    );
    public static final DeferredHolder<SkillType,SkillType> HELD_CAST_SKILL_TYPE = SKILL_TYPES.register(
            "held_cast",
            HeldCastSkillType::new
    );
    public static final DeferredHolder<SkillType,SkillType> DEBUG_CASTABLE_TYPE = SKILL_TYPES.register(
            "debug_castable",
            DebugCastableType::new
    );
    //──Cultivation────────────────────────────────────
    public static final DeferredHolder<SkillType,SkillType> SIMPLE_CULTIVATION_SKILL_TYPE = SKILL_TYPES.register(
            "simple_cultivation_skill",
            SimpleCultivationSkillType::new
    );

    public static void register(IEventBus eventBus){

        SKILL_TYPES.register(eventBus);
    }
}
