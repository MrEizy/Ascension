package net.zic.ascension.impl.core.tribulation.action;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.path.PathInstance;
import net.zic.ascension.api.ascension.core.path.realm.Realm;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.ascension.core.tribulation.TribulationResolutionAction;
import net.zic.ascension.api.ascension.core.tribulation.TribulationResolutionContext;
import net.zic.ascension.api.ascension.datapack.tribulation.TribulationResolutionActionType;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.impl.core.path.simple.SimplePath;
import net.zic.ascension.impl.core.path.simple.SimplePathData;
import net.zic.ascension.impl.datapack.tribulation.AscensionTribulationResolutionActionTypes;

/**
 * clears a path's progress in the realm a failed breakthrough tribulation was started at.
 * does nothing if the path is no longer at that realm
 * @param path the path to clear
 * @param realm the realm the tribulation was started at
 */
public record ClearRealmProgressResolutionAction(Identifier path, Realm realm) implements TribulationResolutionAction {
    public static final MapCodec<ClearRealmProgressResolutionAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("path").forGetter(ClearRealmProgressResolutionAction::path),
            Realm.CODEC.fieldOf("realm").forGetter(ClearRealmProgressResolutionAction::realm)
    ).apply(instance, ClearRealmProgressResolutionAction::new));

    @Override
    public TribulationResolutionActionType getType() {
        return AscensionTribulationResolutionActionTypes.CLEAR_REALM_PROGRESS.get();
    }

    @Override
    public void resolve(TribulationResolutionContext context) {
        OriginSource source = context.targetSource();
        if (source == null || !AscensionOriginSourceHelper.hasPath(source, path)) return;
        PathInstance<?> pathInstance = AscensionOriginSourceHelper.getPathInstance(source, path);
        PathInstance<SimplePathData> simplePathInstance = pathInstance == null ? null : pathInstance.as(SimplePath.class);
        if (simplePathInstance == null) return;
        SimplePath simplePath = (SimplePath) simplePathInstance.getPath();

        simplePath.clearRealmProgress(simplePathInstance, realm);
        AscensionOriginSourceHelper.markPathDirty(source, path);
    }
}
