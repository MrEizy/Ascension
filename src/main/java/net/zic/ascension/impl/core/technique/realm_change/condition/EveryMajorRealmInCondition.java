package net.zic.ascension.impl.core.technique.realm_change.condition;

import net.zic.ascension.api.ascension.core.path.PathData;
import net.zic.ascension.api.ascension.core.progression.ProgressDirection;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSource;
import net.zic.ascension.api.ascension.core.technique.Technique;
import net.zic.ascension.api.ascension.core.technique.TechniqueData;
import net.zic.ascension.api.ascension.core.technique.realm_change.RealmChangeActionCondition;
import net.zic.ascension.api.ascension.datapack.progresison.ProgressActionConditionType;
import net.zic.ascension.impl.datapack.progression.AscensionProgressActionConditionTypes;

import java.util.List;

public record EveryMajorRealmInCondition(List<Integer> majorRealms) implements RealmChangeActionCondition {
    @Override
    public boolean test(AscensionOriginSource source, PathData pathData, Technique technique, TechniqueData techniqueData, int majorRealm, int minorRealm, ProgressDirection direction) {

        System.out.println(minorRealm);
        System.out.println(majorRealm);
        System.out.println(minorRealm == 0 && majorRealms.contains(majorRealm));
        return minorRealm == 0 && majorRealms.contains(majorRealm);
    }

    @Override
    public ProgressActionConditionType getType() {
        return AscensionProgressActionConditionTypes.EVERY_MAJOR_REALM_IN.get();
    }
}
