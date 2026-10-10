package net.zic.ascension.impl.core.innerworld;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.api.ascension.core.path.PathInstance;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.rpg_engine.source.OriginSource;

import java.util.Collection;


final class InnerWorldRealm {
    private InnerWorldRealm() {
    }

    static int highestRealm(LivingEntity entity) {
        OriginSource source = AscensionOriginSourceHelper.getEntitySource(entity);
        if (source == null) {
            return 0;
        }
        Collection<Identifier> paths = AscensionOriginSourceHelper.getPaths(source);
        int highest = 0;
        for (Identifier path : paths) {
            PathInstance instance = AscensionOriginSourceHelper.getPathInstance(source, path);
            if (instance == null) {
                continue;
            }
            int realm = instance.getCurrentMajorRealm();
            if (realm > highest) {
                highest = realm;
            }
        }
        return highest;
    }
}