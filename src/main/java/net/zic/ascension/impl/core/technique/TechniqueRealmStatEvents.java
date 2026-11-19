package net.zic.ascension.impl.core.technique;

import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.path.realm.Realm;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.ascension.core.technique.TechniqueInstance;
import net.zic.ascension.api.ascension.datapack.path.realm.PathRealmChangeEvent;
import net.zic.ascension.api.ascension.event.path.PathAddedEvent;
import net.zic.ascension.api.ascension.event.path.PathRemovedEvent;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class TechniqueRealmStatEvents {
    private TechniqueRealmStatEvents() {
    }

    @SubscribeEvent
    public static void onRealmUp(PathRealmChangeEvent.PathRealmUpEvent event) {
        update(event.getSource(), event.getPathId(), event.getOldRealm(), event.getRealm());
    }

    @SubscribeEvent
    public static void onRealmDown(PathRealmChangeEvent.PathRealmDownEvent event) {
        update(event.getSource(), event.getPathId(), event.getOldRealm(), event.getRealm());
    }

    @SubscribeEvent
    public static void onPathAdded(PathAddedEvent.Post event) {
        Realm realm = event.getPathInstance().getCurrentMajorRealm() < 0
                ? null : event.getPathInstance().getCurrentRealm();
        update(event.getSource(), event.getPathIdentifier(), null, realm);
    }

    @SubscribeEvent
    public static void onPathRemoved(PathRemovedEvent.Post event) {
        Realm realm = event.getPathInstance().getCurrentMajorRealm() < 0
                ? null : event.getPathInstance().getCurrentRealm();
        update(event.getSource(), event.getPathIdentifier(), realm, null);
    }

    private static void update(OriginSource source, Identifier path, @Nullable Realm before, @Nullable Realm after) {
        for (TechniqueInstance<?> instance : AscensionOriginSourceHelper.getTechniqueInstances(source)) {
            if (instance.getTechnique() instanceof SimpleTechnique technique) {
                technique.updateRealmStatGrowth(source, instance.getTechniqueId(), path, before, after);
            }
        }
    }
}
