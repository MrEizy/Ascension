package net.thejadeproject.ascension.refactor_packages.skills.custom.passive.weapon;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
import net.thejadeproject.ascension.refactor_packages.paths.PathData;
import net.thejadeproject.ascension.refactor_packages.skills.custom.passive.SimplePassiveSkill;

public abstract class GenericWeaponMasterySkill extends SimplePassiveSkill {

    protected abstract ResourceLocation getPathId();

    protected abstract TagKey<Item> getWeaponTag();

    protected double getBaseBonus() {
        return 0.10D;
    }

    protected double getBonusPerMajorRealm() {
        return 0.22D;
    }

    protected double getBonusPerMinorRealm() {
        return 0.025D;
    }

    protected double getMaxBonus() {
        return 5.0D;
    }

    public boolean matchesWeapon(ItemStack stack) {
        return !stack.isEmpty() && stack.is(getWeaponTag());
    }

    public boolean matchesDamage(ServerPlayer player, DamageSource source) {
        return matchesWeapon(player.getMainHandItem());
    }

    public double getDamageMultiplier(IEntityData entityData) {
        if (entityData == null) return 1.0D;
        if (!entityData.hasPath(getPathId())) return 1.0D;

        PathData pathData = entityData.getPathData(getPathId());
        if (pathData == null) return 1.0D;

        int majorRealm = pathData.getMajorRealm();
        int minorRealm = pathData.getMinorRealm();

        double bonus = getBaseBonus()
                + majorRealm * getBonusPerMajorRealm()
                + minorRealm * getBonusPerMinorRealm();

        bonus = Mth.clamp(bonus, 0.0D, getMaxBonus());

        return 1.0D + bonus;
    }
}