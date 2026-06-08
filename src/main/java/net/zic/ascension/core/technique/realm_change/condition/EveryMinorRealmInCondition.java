package net.zic.ascension.core.technique.realm_change.condition;

import net.zic.ascension.api.core.path.PathData;
import net.zic.ascension.api.core.progression.ProgressDirection;
import net.zic.ascension.api.core.source.OriginSource;
import net.zic.ascension.api.core.technique.Technique;
import net.zic.ascension.api.core.technique.TechniqueData;
import net.zic.ascension.api.core.technique.realm_change.RealmChangeActionCondition;
import net.zic.ascension.api.datapack.progresison.ProgressActionConditionType;
import net.zic.ascension.datapack.progression.AscensionProgressActionConditionTypes;

import java.util.List;

public record EveryMinorRealmInCondition(List<Integer> minorRealms) implements RealmChangeActionCondition {
    @Override
    public boolean test(OriginSource source, PathData pathData, Technique technique, TechniqueData techniqueData, int majorRealm, int minorRealm, ProgressDirection direction) {
        return minorRealm != 0 && minorRealms.contains(minorRealm);
    }

    @Override
    public ProgressActionConditionType getType() {
        return AscensionProgressActionConditionTypes.EVERY_MINOR_REALM_IN.get();
    }
}
