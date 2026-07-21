package net.zic.ascension.impl.core.path.foundation.foundation_change.condition;

import net.zic.ascension.api.ascension.core.progression.ProgressDirection;
import net.zic.ascension.api.ascension.core.source.OriginSource;
import net.zic.ascension.api.ascension.datapack.progresison.ProgressActionConditionType;
import net.zic.ascension.impl.core.path.foundation.FoundationPath;
import net.zic.ascension.impl.core.path.foundation.FoundationPathData;
import net.zic.ascension.impl.core.path.foundation.foundation_change.FoundationChangeCondition;
import net.zic.ascension.impl.datapack.progression.AscensionProgressActionConditionTypes;

public class EveryFoundationRealmCondition implements FoundationChangeCondition {
    @Override
    public boolean test(OriginSource source, FoundationPath path, FoundationPathData foundationPathData, int majorRealm, int foundationRealm, ProgressDirection direction) {
        return true;
    }

    @Override
    public ProgressActionConditionType getType() {
        return AscensionProgressActionConditionTypes.EVERY_FOUNDATION_REALM.get();
    }
}
