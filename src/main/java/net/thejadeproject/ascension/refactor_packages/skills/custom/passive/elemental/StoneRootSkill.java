package net.thejadeproject.ascension.refactor_packages.skills.custom.passive.elemental;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
import net.thejadeproject.ascension.refactor_packages.skills.ITickingSkill;
import net.thejadeproject.ascension.refactor_packages.skills.custom.passive.SimplePassiveSkill;

public class StoneRootSkill extends SimplePassiveSkill implements ITickingSkill {

    @Override
    public void onPlayerTick(ServerPlayer player, IEntityData entityData) {
        if (player.tickCount % 20 != 0) return;
        if (!player.onGround()) return;

        boolean rooted = player.isShiftKeyDown()
                || player.getDeltaMovement().horizontalDistanceSqr() < 0.0025D;

        if (!rooted) return;

        player.addEffect(new MobEffectInstance(
                MobEffects.DAMAGE_RESISTANCE,
                40,
                0,
                true,
                false,
                false
        ));
    }

    @Override protected String getTitleKey() {
        return "ascension.skill.stone_root";
    }

    @Override protected String getDescriptionKey() {
        return "ascension.skill.stone_root.description";
    }

    @Override protected String getIconPath() {
        return "textures/spells/icon/placeholder.png";
    }
}