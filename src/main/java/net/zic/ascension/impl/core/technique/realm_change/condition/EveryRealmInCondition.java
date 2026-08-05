package net.zic.ascension.impl.core.technique.realm_change.condition;

import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.path.PathInstance;
import net.zic.ascension.api.ascension.core.progression.ProgressDirection;
import net.zic.ascension.api.ascension.core.technique.Technique;
import net.zic.ascension.api.ascension.core.technique.TechniqueData;
import net.zic.ascension.api.ascension.core.path.realm.realm_change.RealmChangeActionCondition;
import net.zic.ascension.api.ascension.datapack.progresison.ProgressActionConditionType;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.impl.datapack.progression.AscensionProgressActionConditionTypes;

import java.util.List;
import java.util.Map;

public record EveryRealmInCondition(Map<Integer, List<Integer>> realms) implements RealmChangeActionCondition {
    @Override
    public boolean test(OriginSource source, Identifier path, PathInstance PathInstance, int majorRealm, int minorRealm, ProgressDirection direction) {
        return realms.containsKey(majorRealm) && realms.get(majorRealm).contains(minorRealm);
    }

    @Override
    public ProgressActionConditionType getType() {
        return AscensionProgressActionConditionTypes.EVERY_REALM_IN.get();
    }
}
