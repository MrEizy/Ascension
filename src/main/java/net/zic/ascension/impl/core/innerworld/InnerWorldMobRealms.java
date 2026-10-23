package net.zic.ascension.impl.core.innerworld;

import net.minecraft.world.entity.Mob;
import net.zic.ascension.AscensionCraft;

/**
 * THE UNWIRED STEP of the mob cultivation hookup. The inner world decides the allowed realm range for each mob it spawns
 * (see InnerWorldMobs.realmRange). This class hands that range to your Mob Cultivation system.
 *
 * I haven't seen your mob cultivation code, so for now the range is written onto the mob as two plain string tags:
 *   ascension_inner_world_realm_min:<n>   and   ascension_inner_world_realm_max:<n>
 * and your MobCultivationEvents / roller needs to read them when it rolls the mob's realm (before it assigns one) and clamp
 * its roll into [min, max]. Send me the roller, the profile record and the attachment type and I'll wire it directly instead.
 */
public final class InnerWorldMobRealms {
    private InnerWorldMobRealms() {
    }

    public static final String MIN_TAG = "ascension_inner_world_realm_min:";
    public static final String MAX_TAG = "ascension_inner_world_realm_max:";

    private static boolean warned;

    public static void assign(Mob mob, int minRealm, int maxRealm) {
        mob.addTag(MIN_TAG + minRealm);
        mob.addTag(MAX_TAG + maxRealm);
        if (!warned) {
            warned = true;
            AscensionCraft.LOGGER.warn("Inner World mobs: realm range [{}..{}] is written as entity tags only. MobCultivationEvents must read '{}' / '{}' tags to apply it (InnerWorldMobRealms).",
                    minRealm, maxRealm, MIN_TAG, MAX_TAG);
        }
    }

    /** Reads the range back, for your roller: returns {min, max} or null if the mob wasn't spawned by the inner world. */
    public static int[] read(Mob mob) {
        Integer min = null;
        Integer max = null;
        for (String tag : mob.getTags()) {
            if (tag.startsWith(MIN_TAG)) {
                min = Integer.parseInt(tag.substring(MIN_TAG.length()));
            } else if (tag.startsWith(MAX_TAG)) {
                max = Integer.parseInt(tag.substring(MAX_TAG.length()));
            }
        }
        return min == null || max == null ? null : new int[]{min, max};
    }
}