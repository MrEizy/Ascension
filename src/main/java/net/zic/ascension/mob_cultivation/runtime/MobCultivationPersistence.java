package net.zic.ascension.mob_cultivation.runtime;

import net.minecraft.world.entity.Mob;
import net.zic.ascension.Config;
import net.zic.ascension.mob_cultivation.MobCultivationCategory;
import net.zic.ascension.mob_cultivation.MobCultivationData;
import net.zic.ascension.mob_cultivation.MobCultivationManager;
import net.zic.ascension.mob_cultivation.generation.MobCultivationEliteTier;

public final class MobCultivationPersistence {
    private MobCultivationPersistence() {
    }

    public static void refresh(Mob mob) {
        MobCultivationData data = MobCultivationManager.getCultivationData(mob);
        if (!data.isCultivated()) {
            return;
        }

        if (data.isCultivationPersistenceGranted()) {
            mob.setPersistenceRequired();
            return;
        }

        if (!Config.MOB_CULTIVATION.PERSISTENCE_ENABLED.get()) {
            return;
        }

        boolean shouldPersist = false;

        if (Config.MOB_CULTIVATION.BOSSES_ARE_PERSISTENT.get() && data.getCategory() == MobCultivationCategory.BOSS) {
            shouldPersist = true;
        }

        if (Config.MOB_CULTIVATION.ANCIENTS_ARE_PERSISTENT.get() && data.getEliteTier() == MobCultivationEliteTier.ANCIENT) {
            shouldPersist = true;
        }

        if (Config.MOB_CULTIVATION.ELITES_ARE_PERSISTENT.get() && data.getEliteTier() == MobCultivationEliteTier.ELITE) {
            shouldPersist = true;
        }

        int minimumRealmScore = Config.MOB_CULTIVATION.MINIMUM_PERSISTENT_REALM_SCORE.get();
        if (minimumRealmScore >= 0 && MobCultivationManager.getRealmScore(mob) >= minimumRealmScore) {
            shouldPersist = true;
        }

        if (!shouldPersist) {
            return;
        }

        mob.setPersistenceRequired();
        data.setCultivationPersistenceGranted(true);
    }
}
