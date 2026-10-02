package net.zic.ascension.api.ascension.core.requirement;

import net.zic.ascension.api.ascension.datapack.requirement.RequirementType;
import net.zic.ascension.api.rpg_engine.source.OriginSource;

public interface Requirement {
    RequirementType getType();
    boolean test(OriginSource source);
}
