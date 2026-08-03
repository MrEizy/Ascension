package net.zic.ascension.impl.datapack.effect;

import net.zic.ascension.api.core.effect.SkillEffectModule;
import net.zic.ascension.api.ascension.datapack.CodecType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.datapack.TypeRegistries;
import net.zic.ascension.impl.effect.module.FrozenFormEffectModule;
import net.zic.ascension.impl.effect.module.ResourceModifierEffectModule;

public final class AscensionSkillEffectModuleTypes {
    public static final DeferredRegister<CodecType<SkillEffectModule>> TYPES = DeferredRegister.create(TypeRegistries.SKILL_EFFECT_MODULE_TYPE_REGISTRY, AscensionCraft.MOD_ID);
    public static final DeferredHolder<CodecType<SkillEffectModule>, CodecType<SkillEffectModule>> FROZEN_FORM = TYPES.register(
            "frozen_form",
            () -> new CodecType<>(FrozenFormEffectModule.CODEC)
    );
    public static final DeferredHolder<CodecType<SkillEffectModule>, CodecType<SkillEffectModule>> RESOURCE_MODIFIER = TYPES.register(
            "resource_modifier",
            () -> new CodecType<>(ResourceModifierEffectModule.CODEC)
    );

    private AscensionSkillEffectModuleTypes() {
    }

    public static void register(IEventBus bus) {
        TYPES.register(bus);
    }
}
