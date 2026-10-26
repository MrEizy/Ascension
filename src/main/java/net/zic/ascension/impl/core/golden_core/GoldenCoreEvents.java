package net.zic.ascension.impl.core.golden_core;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.path.PathInstance;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.ascension.datapack.path.realm.PathRealmChangeEvent;
import net.zic.ascension.api.ascension.event.path.PathAddedEvent;
import net.zic.ascension.api.ascension.event.path.PathRemovedEvent;

/**
 * keeps the realm actions of golden cores in line with the paths they track.
 * realm change events fire once per realm traversed, syncing is idempotent so duplicate events do nothing
 */
@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class GoldenCoreEvents {
    private GoldenCoreEvents() {
    }

    @SubscribeEvent
    public static void onRealmUp(PathRealmChangeEvent.PathRealmUpEvent event) {
        syncRealm(event);
    }

    @SubscribeEvent
    public static void onRealmDown(PathRealmChangeEvent.PathRealmDownEvent event) {
        syncRealm(event);
    }

    private static void syncRealm(PathRealmChangeEvent event) {
        AscensionOriginSourceHelper.syncGoldenCoreRealms(event.getSource(), event.getPathId(), event.getPathInstance(), event.getRealm());
    }

    @SubscribeEvent
    public static void onPathAdded(PathAddedEvent.Post event) {
        PathInstance<?> instance = event.getPathInstance();
        AscensionOriginSourceHelper.syncGoldenCoreRealms(event.getSource(), event.getPathIdentifier(), instance,
                instance.getCurrentMajorRealm() < 0 ? null : instance.getCurrentRealm());
    }

    @SubscribeEvent
    public static void onPathRemoved(PathRemovedEvent.Post event) {
        AscensionOriginSourceHelper.syncGoldenCoreRealms(event.getSource(), event.getPathIdentifier(), event.getPathInstance(), null);
    }
}
