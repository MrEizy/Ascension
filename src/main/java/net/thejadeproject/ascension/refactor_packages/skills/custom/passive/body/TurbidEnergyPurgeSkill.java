package net.thejadeproject.ascension.refactor_packages.skills.custom.passive.body;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
import net.thejadeproject.ascension.refactor_packages.skills.ITickingSkill;
import net.thejadeproject.ascension.refactor_packages.skills.custom.passive.SimplePassiveSkill;

import java.util.ArrayList;
import java.util.List;

public class TurbidEnergyPurgeSkill extends SimplePassiveSkill implements ITickingSkill {

    private static final int PURGE_INTERVAL_TICKS = 100;
    private static final int PURGE_AMOUNT_TICKS = 200;


    @Override
    protected String getTitleKey() {
        return "ascension.skill.turbid_energy_purge";
    }

    @Override
    protected String getDescriptionKey() {
        return "ascension.skill.turbid_energy_purge.description.";
    }

    @Override
    protected String getIconPath() {
        return "textures/spells/icon/placeholder_white.png";
    }

    @Override
    public void onPlayerTick(ServerPlayer player, IEntityData entityData) {
        if (player.tickCount % PURGE_INTERVAL_TICKS != 0) return;

        purgeOneHarmfulEffect(player);
    }

    private static void purgeOneHarmfulEffect(ServerPlayer player) {
        List<MobEffectInstance> effects = new ArrayList<>(player.getActiveEffects());

        for (MobEffectInstance effect : effects) {
            if (effect.getEffect().value().isBeneficial()) continue;

            int newDuration = effect.getDuration() - PURGE_AMOUNT_TICKS;

            player.removeEffect(effect.getEffect());

            if (newDuration > 0) {
                player.addEffect(new MobEffectInstance(
                        effect.getEffect(),
                        newDuration,
                        effect.getAmplifier(),
                        effect.isAmbient(),
                        effect.isVisible(),
                        effect.showIcon()
                ));
            }

            return;
        }
    }
}