package net.zic.ascension.impl.core.skill.castable.feature;

import com.mojang.serialization.MapCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.skill.castable.feature.SkillExecutionFeature;
import net.zic.ascension.api.ascension.core.skill.castable.feature.impl.DivineSenseFeature;
import net.zic.ascension.api.ascension.datapack.CodecType;
import net.zic.ascension.api.ascension.datapack.ExtensionTypeRegistry;
import net.zic.ascension.api.ascension.datapack.TypeRegistries;

/**
 * I don't have your TypeRegistries/CodecType source. If you already have a
 * class registering "ascension:weapon_swing", "ascension:visual", etc. — add
 * the DIVINE_SENSE field there instead, with your real registration call.
 * Don't compile this file as-is.
 */
public final class SkillExecutionFeatureTypes {

    private static final ExtensionTypeRegistry<SkillExecutionFeature> REGISTRY =
            new ExtensionTypeRegistry<>(TypeRegistries.SKILL_EXECUTION_FEATURE_TYPE_REGISTRY, AscensionCraft.MOD_ID);

    public static final DeferredHolder<CodecType<SkillExecutionFeature>, CodecType<SkillExecutionFeature>> DIVINE_SENSE =
            REGISTRY.add("divine_sense", DivineSenseFeature.CODEC);

    /** Call from your mod constructor: SkillExecutionFeatureTypes.register(modEventBus); */
    public static void register(IEventBus modEventBus) {
        REGISTRY.register(modEventBus);
    }

    private SkillExecutionFeatureTypes() {
    }
}