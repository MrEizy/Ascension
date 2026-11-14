package net.zic.ascension.impl.core.innerworld;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.path.PathInstance;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.rpg_engine.source.OriginSource;

/**
 * The Life Dao rule for the inner world: a player "has the Life Dao" when their life path is at major realm 1 or higher.
 * Having it (a) raises the starting mob cultivation realm through a multiplier, and (b) unlocks the structures that
 * are marked requires_life in the profile. Without it, mobs stay at or below each theme's own realm cap.
 */
public final class InnerWorldLife {
    private InnerWorldLife() {
    }

    public static final Identifier LIFE_PATH = AscensionCraft.prefix("elemental/life");
    public static final int REQUIRED_MAJOR_REALM = 1;

    /** The player's life major realm, or -1 if they have no life path at all. */
    public static int lifeRealm(LivingEntity entity) {
        OriginSource source = AscensionOriginSourceHelper.getEntitySource(entity);
        if (source == null) {
            return -1;
        }
        PathInstance instance = AscensionOriginSourceHelper.getPathInstance(source, LIFE_PATH);
        return instance == null ? -1 : instance.getCurrentMajorRealm();
    }

    public static boolean hasLifeDao(LivingEntity entity) {
        return lifeRealm(entity) >= REQUIRED_MAJOR_REALM;
    }
}