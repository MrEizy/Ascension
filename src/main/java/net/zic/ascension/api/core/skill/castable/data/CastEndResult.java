package net.zic.ascension.api.core.skill.castable.data;

import net.minecraft.resources.Identifier;
import net.zic.ascension.api.core.skill.castable.CastData;

public  record CastEndResult(Identifier skill, Reason reason, CastData castData, int ticksElapsed) {
    public enum Reason{
        NATURAL,
        CANCELLED
    }
}
