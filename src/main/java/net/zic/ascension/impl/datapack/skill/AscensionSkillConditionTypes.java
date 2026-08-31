package net.zic.ascension.impl.datapack.skill;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.skill.castable.SkillCondition;
import net.zic.ascension.api.ascension.datapack.CodecType;
import net.zic.ascension.api.ascension.datapack.ExtensionTypeRegistry;
import net.zic.ascension.api.ascension.datapack.TypeRegistries;
import net.zic.ascension.impl.core.skill.castable.SkillConditions;

public final class AscensionSkillConditionTypes {
    private static final ExtensionTypeRegistry<SkillCondition> TYPES = new ExtensionTypeRegistry<>(
            TypeRegistries.SKILL_CONDITION_TYPE_REGISTRY,
            AscensionCraft.MOD_ID
    );

    public static final DeferredHolder<CodecType<SkillCondition>, CodecType<SkillCondition>> ALL_OF = TYPES.add("all_of", SkillConditions.AllOf.CODEC);
    public static final DeferredHolder<CodecType<SkillCondition>, CodecType<SkillCondition>> ANY_OF = TYPES.add("any_of", SkillConditions.AnyOf.CODEC);
    public static final DeferredHolder<CodecType<SkillCondition>, CodecType<SkillCondition>> NOT = TYPES.add("not", SkillConditions.Not.CODEC);
    public static final DeferredHolder<CodecType<SkillCondition>, CodecType<SkillCondition>> REQUIREMENT = TYPES.add("requirement", SkillConditions.Requirement.CODEC);
    public static final DeferredHolder<CodecType<SkillCondition>, CodecType<SkillCondition>> HEALTH = TYPES.add("health", SkillConditions.Health.CODEC);
    public static final DeferredHolder<CodecType<SkillCondition>, CodecType<SkillCondition>> RESOURCE = TYPES.add("resource", SkillConditions.Resource.CODEC);
    public static final DeferredHolder<CodecType<SkillCondition>, CodecType<SkillCondition>> ENTITY_STATE = TYPES.add("entity_state", SkillConditions.EntityState.CODEC);
    public static final DeferredHolder<CodecType<SkillCondition>, CodecType<SkillCondition>> DISTANCE = TYPES.add("distance", SkillConditions.Distance.CODEC);
    public static final DeferredHolder<CodecType<SkillCondition>, CodecType<SkillCondition>> VARIABLE = TYPES.add("variable", SkillConditions.Variable.CODEC);
    public static final DeferredHolder<CodecType<SkillCondition>, CodecType<SkillCondition>> RANDOM = TYPES.add("random", SkillConditions.Random.CODEC);

    private AscensionSkillConditionTypes() {
    }

    public static void register(IEventBus eventBus) {
        TYPES.register(eventBus);
    }
}
