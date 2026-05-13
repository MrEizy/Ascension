package net.thejadeproject.ascension.mob_cultivation;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.Zombie;

public final class MobBiasList {
    private MobBiasList() {
    }

    public static final MobBodyStatBias ZOMBIE =
            new MobBodyStatBias(8, 4, -1);

    public static final MobBodyStatBias SKELETON =
            new MobBodyStatBias(2, 2, 2);

    public static final MobBodyStatBias SPIDER =
            new MobBodyStatBias(3, 2, 6);

    public static final MobBodyStatBias GENERIC_MONSTER =
            new MobBodyStatBias(5, 3, 1);

    public static final MobBodyStatBias GENERIC_ANIMAL =
            new MobBodyStatBias(2, 1, 0);

    public static MobBodyStatBias getFor(LivingEntity entity) {
        if (entity instanceof Zombie) {
            return ZOMBIE;
        }

        if (entity instanceof Skeleton) {
            return SKELETON;
        }

        if (entity instanceof Spider) {
            return SPIDER;
        }

        if (entity instanceof Monster) {
            return GENERIC_MONSTER;
        }

        if (entity instanceof Animal) {
            return GENERIC_ANIMAL;
        }

        return MobBodyStatBias.NONE;
    }

}