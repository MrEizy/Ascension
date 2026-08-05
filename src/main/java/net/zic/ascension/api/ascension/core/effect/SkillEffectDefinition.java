package net.zic.ascension.api.ascension.core.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.StringRepresentable;

import java.util.List;

public record SkillEffectDefinition(
        Stacking stacking,
        Scope stackingScope,
        int maxStacks,
        List<SkillEffectModule> modules
) {
    public static final Codec<SkillEffectDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Stacking.CODEC.optionalFieldOf("stacking", Stacking.STRONGER_REPLACES)
                    .forGetter(SkillEffectDefinition::stacking),
            Scope.CODEC.optionalFieldOf("stacking_scope", Scope.DEFINITION)
                    .forGetter(SkillEffectDefinition::stackingScope),
            Codec.INT.optionalFieldOf("max_stacks", 1).forGetter(SkillEffectDefinition::maxStacks),
            SkillEffectModule.CODEC.listOf().optionalFieldOf("modules", List.of())
                    .forGetter(SkillEffectDefinition::modules)
    ).apply(instance, SkillEffectDefinition::new));

    public SkillEffectDefinition {
        stacking = stacking == null ? Stacking.STRONGER_REPLACES : stacking;
        stackingScope = stackingScope == null ? Scope.DEFINITION : stackingScope;
        maxStacks = Math.max(1, maxStacks);
        modules = modules == null ? List.of() : List.copyOf(modules);
    }
    public enum Stacking implements StringRepresentable {
        REFRESH("refresh"),
        STRONGER_REPLACES("stronger_replaces"),
        STACK("stack");

        public static final Codec<Stacking> CODEC = StringRepresentable.fromEnum(Stacking::values);
        private final String name;

        Stacking(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public enum Scope implements StringRepresentable {
        DEFINITION("definition"),
        SOURCE_ENTITY("source_entity"),
        SOURCE_SKILL("source_skill"),
        SOURCE_ENTITY_AND_SKILL("source_entity_and_skill"),
        INDEPENDENT("independent");

        public static final Codec<Scope> CODEC = StringRepresentable.fromEnum(Scope::values);
        private final String name;

        Scope(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public enum RemovalReason {
        EXPIRED,
        MANUAL,
        CLEARED,
        MISSING_DEFINITION,
        CONDITION,
        ENTITY_REMOVED
    }

}
