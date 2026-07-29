package net.zic.ascension.mob_cultivation;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Enemy;
import net.zic.ascension.common.util.ModTags;

public final class MobCultivationClassifier {
    private MobCultivationClassifier() {
    }

    public static MobCultivationCategory classify(Mob mob) {
        if (mob.getType().builtInRegistryHolder().is(ModTags.EntityTypes.MOB_CULTIVATION_BOSSES)) {
            return MobCultivationCategory.BOSS;
        }
        if (mob.getType().builtInRegistryHolder().is(ModTags.EntityTypes.MOB_CULTIVATION_PASSIVE)) {
            return MobCultivationCategory.PASSIVE;
        }
        if (mob.getType().builtInRegistryHolder().is(ModTags.EntityTypes.MOB_CULTIVATION_HOSTILE)) {
            return MobCultivationCategory.HOSTILE;
        }

        if (mob instanceof EnderDragon || mob instanceof WitherBoss) {
            return MobCultivationCategory.BOSS;
        }
        if (mob instanceof Enemy) {
            return MobCultivationCategory.HOSTILE;
        }
        return MobCultivationCategory.PASSIVE;
    }
}
