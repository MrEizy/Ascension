package net.thejadeproject.ascension.refactor_packages.skills.custom.passive.debuff;

import net.minecraft.server.level.ServerPlayer;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
import net.thejadeproject.ascension.refactor_packages.skills.ITickingSkill;
import net.thejadeproject.ascension.refactor_packages.skills.custom.ModSkills;
import net.thejadeproject.ascension.refactor_packages.skills.custom.passive.SimpleDebuffSkill;
import net.thejadeproject.ascension.refactor_packages.skills.custom.passive.debuff.skill_data.DebuffSkillHelper;

public class VenomousMeridiansDebuff extends SimpleDebuffSkill implements ITickingSkill {

    @Override
    protected String getTitleKey() {
        return "ascension.skill.venomous_meridians_debuff";
    }

    @Override
    protected String getDescriptionKey() {
        return "ascension.skill.venomous_meridians_debuff.description";
    }

    @Override
    public void onPlayerTick(ServerPlayer player, IEntityData entityData) {
        if (DebuffSkillHelper.removeIfExpired(player, entityData, ModSkills.VENOMOUS_MERIDIANS.getId())) {
            return;
        }

        if (player.tickCount % 40 != 0) return;

        if (player.getHealth() <= 1.0F) return;

        float damage = Math.max(1.0F, player.getMaxHealth() * 0.02F);
        damage = Math.min(damage, player.getHealth() - 1.0F);

        player.hurt(player.damageSources().magic(), damage);
    }
}