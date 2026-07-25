package net.zic.ascension.impl.datapack.effect;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.core.effect.SkillEffectModuleType;
import net.zic.ascension.api.datapack.TypeRegistries;
import net.zic.ascension.impl.effect.module.FrozenFormEffectModule;

public final class AscensionSkillEffectModuleTypes {
    public static final DeferredRegister<SkillEffectModuleType> TYPES = DeferredRegister.create(TypeRegistries.SKILL_EFFECT_MODULE_TYPE_REGISTRY, AscensionCraft.MOD_ID);
    public static final DeferredHolder<SkillEffectModuleType, SkillEffectModuleType> FROZEN_FORM = TYPES.register(
            "frozen_form",
            () -> new SkillEffectModuleType(FrozenFormEffectModule.CODEC)
    );

    private AscensionSkillEffectModuleTypes() {
    }

    public static void register(IEventBus bus) {
        TYPES.register(bus);
    }
}
