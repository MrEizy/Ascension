package net.thejadeproject.ascension.refactor_packages.skills.custom.passive.debuff;
import net.minecraft.server.level.ServerPlayer;

import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
import net.thejadeproject.ascension.refactor_packages.qi.EntityQiContainer;
import net.thejadeproject.ascension.refactor_packages.skills.ITickingSkill;
import net.thejadeproject.ascension.refactor_packages.skills.custom.ModSkills;
import net.thejadeproject.ascension.refactor_packages.skills.custom.passive.SimpleDebuffSkill;
import net.thejadeproject.ascension.refactor_packages.skills.custom.passive.debuff.skill_data.DebuffSkillHelper;

public class QiDevouringPoisonDebuff extends SimpleDebuffSkill implements ITickingSkill {

    private int amplifier = 0;

    @Override
    public void onPlayerTick(ServerPlayer player, IEntityData entityData) {
        if (DebuffSkillHelper.removeIfExpired(player, entityData, ModSkills.QI_DEVOURING_POISON.getId())) {
            return;
        }

        int interval = Math.max(10, 20 - (amplifier * 3));

        if (player.tickCount % interval != 0) {
            return;
        }

        int drainAmount = (amplifier + 1) * 25;

        EntityQiContainer qi = entityData.getQiContainer();
        double currentQi = qi.getCurrentQi();

        if (currentQi >= drainAmount) {
            qi.tryConsumeQi(drainAmount);
        } else {
            qi.tryConsumeQi(currentQi);
            float healthDamage = (float) (drainAmount - currentQi);
            player.hurt(player.damageSources().magic(), healthDamage);
        }
    }

    @Override
    protected String getTitleKey() {
        return "ascension.skill.qi_devouring_parasite_debuff";
    }

    @Override
    protected String getDescriptionKey() {
        return "ascension.skill.qi_devouring_parasite_debuff.description";
    }
}
