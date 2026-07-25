package net.zic.ascension.impl.core.technique.realm_change.condition;

import net.zic.ascension.api.ascension.core.path.PathData;
import net.zic.ascension.api.ascension.core.progression.ProgressDirection;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSource;
import net.zic.ascension.api.ascension.core.technique.Technique;
import net.zic.ascension.api.ascension.core.technique.TechniqueData;
import net.zic.ascension.api.ascension.core.technique.realm_change.RealmChangeActionCondition;
import net.zic.ascension.api.ascension.datapack.progresison.ProgressActionConditionType;
import net.zic.ascension.impl.datapack.progression.AscensionProgressActionConditionTypes;

public class EveryRealmCondition implements RealmChangeActionCondition {
    @Override
    public boolean test(AscensionOriginSource source, PathData pathData, Technique technique, TechniqueData techniqueData, int majorRealm, int minorRealm, ProgressDirection direction) {
        return true;
    }

    @Override
    public ProgressActionConditionType getType() {
        return AscensionProgressActionConditionTypes.EVERY_REALM.get();
    }
}
