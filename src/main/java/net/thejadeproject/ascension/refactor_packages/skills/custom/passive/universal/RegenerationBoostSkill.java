package net.thejadeproject.ascension.refactor_packages.skills.custom.passive.universal;

import net.minecraft.server.level.ServerPlayer;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
import net.thejadeproject.ascension.refactor_packages.paths.PathData;
import net.thejadeproject.ascension.refactor_packages.skills.ITickingSkill;
import net.thejadeproject.ascension.refactor_packages.skills.custom.passive.SimplePassiveSkill;

public class RegenerationBoostSkill extends SimplePassiveSkill implements ITickingSkill {

    //TODO: Completely random healing rate that need to be tuned later

    private static final int HEAL_INTERVAL_TICKS = 30;

    private static final float BASE_HEAL_AMOUNT = 1.0F;
    private static final float HEAL_AMOUNT_PER_MAJOR_REALM = 3.0F;

    @Override
    protected String getTitleKey() {
        return "ascension.skill.regeneration_boost";
    }

    @Override
    protected String getDescriptionKey() {
        return "ascension.skill.regeneration_boost.description";
    }

    @Override
    public void onPlayerTick(ServerPlayer player, IEntityData entityData) {
        if (player.tickCount % HEAL_INTERVAL_TICKS != 0) return;
        if (player.getHealth() >= player.getMaxHealth()) return;

        int highestMajorRealm = getHighestMajorRealm(entityData);
        float healAmount = getHealAmount(highestMajorRealm);

        player.heal(healAmount);
    }

    private int getHighestMajorRealm(IEntityData entityData) {
        if (entityData == null) return 0;

        int highestMajorRealm = 0;

        for (PathData pathData : entityData.getAllPathData()) {
            if (pathData == null) continue;

            highestMajorRealm = Math.max(
                    highestMajorRealm,
                    pathData.getMajorRealm()
            );
        }

        return Math.max(0, highestMajorRealm);
    }

    private float getHealAmount(int majorRealm) {
        return BASE_HEAL_AMOUNT + majorRealm * HEAL_AMOUNT_PER_MAJOR_REALM;
    }
}