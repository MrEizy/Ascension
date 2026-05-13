package net.thejadeproject.ascension.refactor_packages.skills.custom.passive.weapon;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.Tags;
import net.thejadeproject.ascension.refactor_packages.paths.ModPaths;


public class BowMasterySkill extends GenericWeaponMasterySkill {

    @Override
    protected ResourceLocation getPathId() {
        return ModPaths.BOW.getId();
    }

    @Override
    protected TagKey<Item> getWeaponTag() {
        return Tags.Items.TOOLS_BOW;
    }

    @Override
    protected String getTitleKey() {
        return "ascension.skill.bow_mastery_skill";
    }

    @Override
    protected String getDescriptionKey() {
        return "ascension.skill.bow_mastery.description_skill";
    }


    @Override
    public boolean matchesDamage(ServerPlayer player, DamageSource source) {
        Entity direct = source.getDirectEntity();

        return direct instanceof AbstractArrow
                && !(direct instanceof ThrownTrident);
    }

    @Override
    protected double getBaseBonus() {
        return 0.04D;
    }

    @Override
    protected double getBonusPerMajorRealm() {
        return 0.10D;
    }

    @Override
    protected double getBonusPerMinorRealm() {
        return 0.0125D;
    }

    @Override
    protected double getMaxBonus() {
        return 1.75D;
    }
}