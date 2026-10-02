package net.zic.ascension.api.ascension.core.requirement;

import com.mojang.serialization.Codec;
import net.zic.ascension.api.ascension.datapack.requirement.RequirementType;
import net.zic.ascension.api.rpg_engine.source.OriginSource;

import java.util.List;

public record RequirementHolder(List<Requirement> requirements) {
    public static final RequirementHolder EMPTY = new RequirementHolder(List.of());
    public static final Codec<RequirementHolder> CODEC = RequirementType.REQUIREMENT_CODEC.listOf().xmap(RequirementHolder::new, RequirementHolder::requirements);

    public RequirementHolder {
        requirements = requirements == null ? List.of() : List.copyOf(requirements);
    }

    public boolean test(OriginSource source) {
        if (source == null) return false;
        for (Requirement requirement : requirements) {
            if (requirement == null || !requirement.test(source)) return false;
        }
        return true;
    }
}
