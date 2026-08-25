package net.zic.ascension.api.ascension.core.skill.castable.action;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

/** Selects the caster, target, origin, or position used by an action */
public enum ActionSubject implements StringRepresentable {
    CASTER("caster"),
    TARGET("target"),
    ORIGIN("origin"),
    POSITION("position");

    public static final Codec<ActionSubject> CODEC = StringRepresentable.fromEnum(ActionSubject::values);
    private final String name;

    ActionSubject(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
