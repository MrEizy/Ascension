package net.zic.ascension.impl.datapack.requirement;

import com.mojang.serialization.MapCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.requirement.Requirement;
import net.zic.ascension.api.ascension.datapack.TypeRegistries;
import net.zic.ascension.api.ascension.datapack.requirement.RequirementType;
import net.zic.ascension.impl.core.requirement.Requirements;

public final class AscensionRequirementTypes {
    public static final DeferredRegister<RequirementType> REQUIREMENT_TYPES =
            DeferredRegister.create(TypeRegistries.REQUIREMENT_TYPE_REGISTRY, AscensionCraft.MOD_ID);

    public static final DeferredHolder<RequirementType, RequirementType> ALL_OF = register("all_of", Requirements.AllOf.CODEC);
    public static final DeferredHolder<RequirementType, RequirementType> ANY_OF = register("any_of", Requirements.AnyOf.CODEC);
    public static final DeferredHolder<RequirementType, RequirementType> NOT = register("not", Requirements.Not.CODEC);
    public static final DeferredHolder<RequirementType, RequirementType> HAS_PATH = register("has_path", Requirements.HasPath.CODEC);
    public static final DeferredHolder<RequirementType, RequirementType> PATH_REALM = register("path_realm", Requirements.PathRealm.CODEC);
    public static final DeferredHolder<RequirementType, RequirementType> HAS_TECHNIQUE = register("has_technique", Requirements.HasTechnique.CODEC);
    public static final DeferredHolder<RequirementType, RequirementType> HAS_BLOODLINE = register("has_bloodline", Requirements.HasBloodline.CODEC);
    public static final DeferredHolder<RequirementType, RequirementType> BLOODLINE_PURITY = register("bloodline_purity", Requirements.BloodlinePurity.CODEC);
    public static final DeferredHolder<RequirementType, RequirementType> HAS_PHYSIQUE = register("has_physique", Requirements.HasPhysique.CODEC);
    public static final DeferredHolder<RequirementType, RequirementType> HAS_SKILL = register("has_skill", Requirements.HasSkill.CODEC);
    public static final DeferredHolder<RequirementType, RequirementType> SKILL_MASTERY = register("skill_mastery", Requirements.SkillMastery.CODEC);
    public static final DeferredHolder<RequirementType, RequirementType> AFFINITY = register("affinity", Requirements.Affinity.CODEC);
    public static final DeferredHolder<RequirementType, RequirementType> STAT = register("stat", Requirements.StatRequirement.CODEC);

    private AscensionRequirementTypes() {
    }

    private static DeferredHolder<RequirementType, RequirementType> register(
            String name,
            MapCodec<? extends Requirement> codec
    ) {
        return REQUIREMENT_TYPES.register(name, () -> new RequirementType() {
            @Override
            public MapCodec<? extends Requirement> codec() {
                return codec;
            }
        });
    }

    public static void register(IEventBus eventBus) {
        REQUIREMENT_TYPES.register(eventBus);
    }
}
