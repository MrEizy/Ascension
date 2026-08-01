package net.zic.ascension.impl.core.path.foundation.foundation_change;

import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.RegistryObjectData;
import net.zic.ascension.api.ascension.core.path.Path;
import net.zic.ascension.api.ascension.core.progression.ProgressAction;
import net.zic.ascension.api.ascension.core.progression.ProgressDirection;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.impl.core.path.foundation.FoundationPath;
import net.zic.ascension.impl.core.path.foundation.FoundationPathData;

import java.util.UUID;

public interface FoundationChangeAction extends ProgressAction {
    @Override
    default void run(UUID holderId, OriginSource source, Identifier contextIdentifier, RegistryObjectData contextData, ProgressDirection direction){
        if(!(contextData instanceof FoundationPathData foundationPathData)) return;
        Path path  = CoreRegistries.safeAccess(CoreRegistries.PATH_REGISTRY,contextIdentifier,source.getRegistryAccess());
        if(!(path instanceof FoundationPath foundationPath)) return;

        int majorRealm = foundationPathData.getMajorRealm();;
        int foundationRealm = foundationPathData.getFoundationRealm(majorRealm);
        run(
                holderId,
                source,
                foundationPath,
                foundationPathData,
                majorRealm,
                direction == ProgressDirection.DOWN ? foundationRealm+1: foundationRealm,
                direction);
    }
    void run(UUID holderId, OriginSource source, FoundationPath path, FoundationPathData foundationPathData, int majorRealm, int foundationRealm, ProgressDirection direction);

}
