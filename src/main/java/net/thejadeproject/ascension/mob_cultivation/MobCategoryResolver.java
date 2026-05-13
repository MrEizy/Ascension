package net.thejadeproject.ascension.mob_cultivation;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.warden.Warden;

public class MobCategoryResolver {

    private MobCategoryResolver() {}

    public static MobCultivationCategory resolve(LivingEntity entity) {
        if (isBoss(entity)) return MobCultivationCategory.BOSS;
        if (isHostile(entity)) return MobCultivationCategory.HOSTILE;
        return MobCultivationCategory.PASSIVE;
    }

    private static boolean isHostile(LivingEntity entity) {
        return entity instanceof Monster;
    }

    private static boolean isBoss(LivingEntity entity) {
        return entity instanceof EnderDragon
                || entity instanceof WitherBoss
                || entity instanceof Warden;
    }

}
