package net.zic.ascension.impl.core.technique;

import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.path.PathInstance;
import net.zic.ascension.api.ascension.core.path.realm.Realm;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.ascension.core.technique.TechniqueInstance;
import net.zic.ascension.api.rpg_engine.source.OriginSource;

import java.util.Set;

public final class TechniqueCultivationLimits {
    private TechniqueCultivationLimits() {}

    public static boolean canCultivate(OriginSource source, Identifier skillId, Identifier path, PathInstance<?> pathInstance) {
        if (source == null || skillId == null || pathInstance == null) return true;
        Set<Identifier> owners = AscensionOriginSourceHelper.getSkillOwners(source, skillId);
        if (owners.isEmpty()) return true;

        for (Identifier owner : owners) {
            TechniqueInstance<?> instance = AscensionOriginSourceHelper.getTechnique(source, owner);
            if (instance == null || !(instance.getTechnique() instanceof SimpleTechnique technique)) return true;
            if (!technique.getCultivationPaths().contains(path) || technique.getMaxRealm().isEmpty()) return true;
            Realm limit = technique.getCultivationLimit(pathInstance);
            if (limit == null || pathInstance.getCurrentMajorRealm() < 0
                    || pathInstance.getCurrentRealm().compareTo(limit) < 0) return true;
        }
        return false;
    }
}
