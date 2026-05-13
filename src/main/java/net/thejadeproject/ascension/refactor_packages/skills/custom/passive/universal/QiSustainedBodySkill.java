package net.thejadeproject.ascension.refactor_packages.skills.custom.passive.universal;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodData;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
import net.thejadeproject.ascension.refactor_packages.skills.ITickingSkill;
import net.thejadeproject.ascension.refactor_packages.skills.custom.passive.SimplePassiveSkill;

public class QiSustainedBodySkill extends SimplePassiveSkill implements ITickingSkill {

    @Override
    protected String getTitleKey() {
        return "ascension.skill.qi_sustained_body";
    }

    @Override
    protected String getDescriptionKey() { return "ascension.skill.qi_sustained_body.description"; }

    @Override
    public void onPlayerTick(ServerPlayer player, IEntityData entityData) {
        if (player.tickCount % 20 != 0) return;

        FoodData foodData = player.getFoodData();

        if (foodData.getFoodLevel() < 20) {
            foodData.setFoodLevel(20);
        }

        if (foodData.getSaturationLevel() < 5.0F) {
            foodData.setSaturation(5.0F);
        }
    }
}