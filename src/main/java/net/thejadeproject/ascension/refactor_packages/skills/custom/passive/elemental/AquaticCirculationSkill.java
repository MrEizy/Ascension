package net.thejadeproject.ascension.refactor_packages.skills.custom.passive.elemental;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
import net.thejadeproject.ascension.refactor_packages.skills.ITickingSkill;
import net.thejadeproject.ascension.refactor_packages.skills.custom.passive.SimplePassiveSkill;

public class AquaticCirculationSkill extends SimplePassiveSkill implements ITickingSkill {

    @Override
    protected String getTitleKey() {
        return "ascension.skill.aquatic_circulation";
    }

    @Override
    protected String getDescriptionKey() {
        return "ascension.skill.aquatic_circulation.description";
    }

    @Override
    protected String getIconPath() {
        return "textures/spells/icon/placeholder.png";
    }

    @Override
    public void onPlayerTick(ServerPlayer player, IEntityData entityData) {
        if (player.tickCount % 20 != 0) return;
        if (!player.isUnderWater()) return;

        player.addEffect(new MobEffectInstance(
                MobEffects.CONDUIT_POWER,
                60,
                0,
                true,
                false,
                false
        ));
    }
}