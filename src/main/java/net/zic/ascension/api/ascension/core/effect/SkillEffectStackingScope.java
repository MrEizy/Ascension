package net.zic.ascension.api.ascension.core.effect;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum SkillEffectStackingScope implements StringRepresentable {
    DEFINITION("definition"),
    SOURCE_ENTITY("source_entity"),
    SOURCE_SKILL("source_skill"),
    SOURCE_ENTITY_AND_SKILL("source_entity_and_skill"),
    INDEPENDENT("independent");

    public static final Codec<SkillEffectStackingScope> CODEC =
            StringRepresentable.fromEnum(SkillEffectStackingScope::values);

    private final String name;

    SkillEffectStackingScope(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
