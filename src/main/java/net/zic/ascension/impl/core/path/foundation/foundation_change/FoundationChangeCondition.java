package net.zic.ascension.impl.core.path.foundation.foundation_change;

import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.RegistryObjectData;
import net.zic.ascension.api.ascension.core.path.Path;
import net.zic.ascension.api.ascension.core.progression.ProgressActionCondition;
import net.zic.ascension.api.ascension.core.progression.ProgressDirection;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.impl.core.path.foundation.FoundationPath;
import net.zic.ascension.impl.core.path.foundation.FoundationPathData;

public interface FoundationChangeCondition extends ProgressActionCondition {


    @Override
    default boolean test(OriginSource source, Identifier contextIdentifier, RegistryObjectData contextData, ProgressDirection direction){
        if(!(contextData instanceof FoundationPathData foundationPathData)) return false;
        Path path  = CoreRegistries.safeAccess(CoreRegistries.PATH_REGISTRY,contextIdentifier,source.getRegistryAccess());
        if(!(path instanceof FoundationPath foundationPath)) return false;

        int majorRealm = foundationPathData.getMajorRealm();;
        int foundationRealm = foundationPathData.getFoundationRealm(majorRealm);
        return test(
                source,
                foundationPath,
                foundationPathData,
                majorRealm,
                direction == ProgressDirection.DOWN ? foundationRealm+1: foundationRealm,
                direction
        );

    }
    boolean test(OriginSource source, FoundationPath path, FoundationPathData foundationPathData, int majorRealm, int foundationRealm, ProgressDirection direction);
}
