package net.thejadeproject.ascension.refactor_packages.skills.custom.passive.debuff;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
import net.thejadeproject.ascension.refactor_packages.skills.ITickingSkill;
import net.thejadeproject.ascension.refactor_packages.skills.custom.ModSkills;
import net.thejadeproject.ascension.refactor_packages.skills.custom.passive.SimpleDebuffSkill;
import net.thejadeproject.ascension.refactor_packages.skills.custom.passive.debuff.skill_data.DebuffSkillHelper;

public class ParalyzedBodyDebuff extends SimpleDebuffSkill implements ITickingSkill {

    @Override
    public void onPlayerTick(ServerPlayer player, IEntityData entityData) {
        if (DebuffSkillHelper.removeIfExpired(player, entityData, ModSkills.PARALYZED_BODY.getId())) {
            return;
        }

        player.setSprinting(false);

        Vec3 movement = player.getDeltaMovement();

        double x = movement.x * 0.15D;
        double y = movement.y;
        double z = movement.z * 0.15D;

        if (y > 0.0D) {
            y = 0.0D;
        }

        player.setDeltaMovement(x, y, z);
    }

    @Override
    protected String getTitleKey() {
        return "ascension.skill.paralyzed_body_debuff";
    }

    @Override
    protected String getDescriptionKey() {
        return "ascension.skill.paralyzed_body_debuff.description";
    }
}