package net.zic.ascension.impl.datapack.skill;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.datapack.TypeRegistries;
import net.zic.ascension.api.ascension.datapack.skill.SkillType;
import net.zic.ascension.impl.datapack.skill.active.ActiveSkillType;
import net.zic.ascension.impl.datapack.skill.body.BodyCultivationSkillType;
import net.zic.ascension.impl.datapack.skill.passive.PassiveSkillType;

public final class AscensionSkillTypes {
    public static final DeferredRegister<SkillType> SKILL_TYPES =
            DeferredRegister.create(TypeRegistries.SKILL_TYPE_REGISTRY, AscensionCraft.MOD_ID);

    public static final DeferredHolder<SkillType, SkillType> PASSIVE_SKILL_TYPE = SKILL_TYPES.register(
            "passive",
            PassiveSkillType::new
    );
    public static final DeferredHolder<SkillType, SkillType> ACTIVE_SKILL_TYPE = SKILL_TYPES.register(
            "active",
            ActiveSkillType::new
    );
    public static final DeferredHolder<SkillType, SkillType> BODY_CULTIVATION_SKILL_TYPE = SKILL_TYPES.register(
            "body_cultivation",
            BodyCultivationSkillType::new
    );

    private AscensionSkillTypes() {
    }

    public static void register(IEventBus eventBus) {
        SKILL_TYPES.register(eventBus);
    }
}
