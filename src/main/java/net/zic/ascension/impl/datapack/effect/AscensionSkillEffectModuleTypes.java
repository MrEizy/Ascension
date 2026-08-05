package net.zic.ascension.impl.datapack.effect;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.effect.SkillEffectModule;
import net.zic.ascension.api.ascension.datapack.CodecType;
import net.zic.ascension.api.ascension.datapack.ExtensionTypeRegistry;
import net.zic.ascension.api.ascension.datapack.TypeRegistries;
import net.zic.ascension.impl.core.effect.SkillEffectModules;

public final class AscensionSkillEffectModuleTypes {
    private static final ExtensionTypeRegistry<SkillEffectModule> TYPES = new ExtensionTypeRegistry<>(
            TypeRegistries.SKILL_EFFECT_MODULE_TYPE_REGISTRY,
            AscensionCraft.MOD_ID
    );

    public static final DeferredHolder<CodecType<SkillEffectModule>, CodecType<SkillEffectModule>> FROZEN_FORM =
            TYPES.add("frozen_form", SkillEffectModules.FrozenForm.CODEC);
    public static final DeferredHolder<CodecType<SkillEffectModule>, CodecType<SkillEffectModule>> RESOURCE_MODIFIER =
            TYPES.add("resource_modifier", SkillEffectModules.ResourceModifierModule.CODEC);

    private AscensionSkillEffectModuleTypes() {
    }

    public static void register(IEventBus bus) {
        TYPES.register(bus);
    }
}
