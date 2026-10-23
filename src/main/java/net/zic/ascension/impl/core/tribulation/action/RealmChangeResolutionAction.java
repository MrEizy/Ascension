package net.zic.ascension.impl.core.tribulation.action;

import net.zic.ascension.impl.core.path.simple.SimplePathData;
import net.zic.ascension.impl.core.path.simple.SimplePath;
import com.mojang.serialization.Codec;
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
import net.zic.ascension.impl.datapack.tribulation.AscensionTribulationResolutionActionTypes;

/**
 * Moves a path realmChange realms up from realm, used to breakthrough after a successful tribulation.
 * Verifies the target is still at realm, and that realm still has this tribulation before progressing
 * @param path the path to progress
 * @param realm the realm the tribulation was started at
 * @param realmChange how many realms to move up
 */
public record RealmChangeResolutionAction(Identifier path, Realm realm, int realmChange) implements TribulationResolutionAction {
    private static final Codec<Realm> REALM_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("major").forGetter(Realm::majorRealm),
            Codec.INT.fieldOf("minor").forGetter(Realm::minorRealm)
    ).apply(instance, Realm::of));

    public static final MapCodec<RealmChangeResolutionAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("path").forGetter(RealmChangeResolutionAction::path),
            REALM_CODEC.fieldOf("realm").forGetter(RealmChangeResolutionAction::realm),
            Codec.INT.optionalFieldOf("realm_change", 1).forGetter(RealmChangeResolutionAction::realmChange)
    ).apply(instance, RealmChangeResolutionAction::new));

    @Override
    public TribulationResolutionActionType getType() {
        return AscensionTribulationResolutionActionTypes.REALM_CHANGE.get();
    }

    @Override
    public void resolve(TribulationResolutionContext context) {
        OriginSource source = context.targetSource();
        if (source == null || !AscensionOriginSourceHelper.hasPath(source, path)) return;
        PathInstance<?> pathInstance = AscensionOriginSourceHelper.getPathInstance(source, path);
        PathInstance<SimplePathData> simplePathInstance = pathInstance == null ? null : pathInstance.as(SimplePath.class);
        if (simplePathInstance == null) return;
        SimplePath simplePath = (SimplePath) simplePathInstance.getPath();

        simplePath.completeBreakthrough(source, simplePathInstance, realm, context.instance(), realmChange);
        AscensionOriginSourceHelper.markPathDirty(source, path);
    }
}
