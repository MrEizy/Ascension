package net.zic.ascension.mob_cultivation.runtime;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import net.zic.ascension.impl.core.entity.AscensionStats;
import net.zic.ascension.mob_cultivation.MobCultivationCategory;
import net.zic.ascension.mob_cultivation.MobCultivationData;
import net.zic.ascension.mob_cultivation.MobCultivationManager;
import net.zic.ascension.mob_cultivation.trait.MobCultivationTraitDefinition;
import net.zic.ascension.mob_cultivation.trait.MobCultivationTraitManager;

public final class MobCultivationAi {
    private static final int AI_INTERVAL = 10;
    private static final int FLEE_REALM_GAP = 3;
    private static final int QI_SEARCH_INTERVAL = 300;

    private MobCultivationAi() {
    }

    public static void tick(Mob mob, long gameTime) {
        if (mob.isNoAi() || (mob.getId() + gameTime) % AI_INTERVAL != 0L) return;
        MobCultivationData data = MobCultivationManager.getCultivationData(mob);
        tickTraitRegeneration(mob, data, gameTime);
        tickPackAlert(mob, data);
        if (data.getCategory() == MobCultivationCategory.PASSIVE) {
            tickPassiveRetaliation(mob, data);
        } else {
            tickCombatRetreat(mob, data);
        }
        tickHighQiSeeking(mob, data, gameTime);
    }

    private static void tickTraitRegeneration(Mob mob, MobCultivationData data, long gameTime) {
        if (mob.getHealth() >= mob.getMaxHealth() || gameTime < data.getNextRegenerationGameTime()) return;
        int nextInterval = 100;
        double totalFraction = 0.0D;
        for (var traitId : data.getTraits()) {
            MobCultivationTraitDefinition trait = MobCultivationTraitManager.get(traitId);
            if (trait == null || trait.regenerationFraction() <= 0.0D) continue;
            totalFraction += trait.regenerationFraction();
            nextInterval = Math.min(nextInterval, trait.regenerationInterval());
        }
        if (totalFraction > 0.0D) {
            int realmScore = Math.max(0, MobCultivationManager.getRealmScore(mob));
            float amount = (float) (mob.getMaxHealth() * totalFraction * (1.0D + realmScore * 0.04D));
            mob.heal(Math.max(0.1F, amount));
        }
        data.setNextRegenerationGameTime(gameTime + Math.max(20, nextInterval));
    }

    private static void tickPackAlert(Mob mob, MobCultivationData data) {
        if (!(mob.level() instanceof ServerLevel level)) return;
        LivingEntity attacker = mob.getLastHurtByMob();
        int timestamp = mob.getLastHurtByMobTimestamp();
        if (attacker == null || timestamp == data.getLastPackAlertHurtTimestamp()) return;
        data.setLastPackAlertHurtTimestamp(timestamp);

        double radius = 0.0D;
        for (var traitId : data.getTraits()) {
            MobCultivationTraitDefinition trait = MobCultivationTraitManager.get(traitId);
            if (trait != null) radius = Math.max(radius, trait.packAlertRadius());
        }
        if (radius <= 0.0D) return;

        for (Mob ally : level.getEntitiesOfClass(
                Mob.class,
                mob.getBoundingBox().inflate(radius),
                candidate -> candidate != mob && candidate.getType() == mob.getType() && candidate.isAlive()
        )) {
            MobCultivationData allyData = MobCultivationManager.getCultivationData(ally);
            if (!allyData.isCultivated()) continue;
            if (allyData.getCategory() == MobCultivationCategory.PASSIVE) {
                allyData.startRetaliating(attacker.getUUID(), 160);
            } else {
                ally.setTarget(attacker);
                ally.setAggressive(true);
            }
        }
    }

    private static void tickPassiveRetaliation(Mob mob, MobCultivationData data) {
        if (!(mob.level() instanceof ServerLevel level)) return;
        int hurtTimestamp = mob.getLastHurtByMobTimestamp();
        LivingEntity attacker = mob.getLastHurtByMob();
        int realmScore = Math.max(0, MobCultivationManager.getRealmScore(mob));
        double spirit = MobCultivationManager.getEntityData(mob).getSource().getValue(AscensionStats.SPIRIT.get());

        if (attacker != null && attacker.isAlive() && hurtTimestamp != data.getLastProcessedHurtTimestamp()) {
            data.setLastProcessedHurtTimestamp(hurtTimestamp);
            double retaliationChance = Math.min(0.95D, 0.10D + realmScore * 0.12D + spirit * 0.0125D);
            if (realmScore >= 1 && mob.getRandom().nextDouble() < retaliationChance) {
                data.startRetaliating(attacker.getUUID(), 200 + realmScore * 20);
            }
        }

        if (data.getRetaliationTarget() == null || data.getRetaliationTicks() <= 0) {
            data.stopRetaliating();
            mob.setAggressive(false);
            return;
        }

        Entity targetEntity = level.getEntityInAnyDimension(data.getRetaliationTarget());
        if (!(targetEntity instanceof LivingEntity target)
                || target.level() != mob.level()
                || !target.isAlive()
                || target.distanceToSqr(mob) > 32.0D * 32.0D) {
            if (mob.getTarget() == targetEntity) mob.setTarget(null);
            mob.setAggressive(false);
            data.stopRetaliating();
            return;
        }

        mob.setTarget(target);
        mob.setAggressive(true);
        mob.getNavigation().moveTo(target, 1.10D + Math.min(0.35D, realmScore * 0.025D));
        if (data.getRetaliationAttackCooldown() <= 0 && mob.isWithinMeleeAttackRange(target)) {
            if (mob.doHurtTarget(level, target)) {
                data.setRetaliationAttackCooldown(Math.max(10, 22 - realmScore));
            }
        }
        data.tickRetaliation(AI_INTERVAL);
        if (data.getRetaliationTicks() <= 0) {
            if (mob.getTarget() == target) mob.setTarget(null);
            mob.setAggressive(false);
            data.stopRetaliating();
        }
    }

    private static void tickCombatRetreat(Mob mob, MobCultivationData data) {
        if (!(mob.level() instanceof ServerLevel level)) return;
        if (data.getFleeFrom() == null) {
            LivingEntity currentTarget = mob.getTarget();
            if (currentTarget == null) return;

            int mobScore = MobCultivationManager.getRealmScore(mob);
            double spirit = MobCultivationManager.getEntityData(mob).getSource().getValue(AscensionStats.SPIRIT.get());
            double retreatThreshold = defaultRetreatThreshold(data, mobScore);
            boolean injured = mob.getHealth() / Math.max(1.0F, mob.getMaxHealth()) <= retreatThreshold;
            boolean overwhelmed = false;
            if (currentTarget instanceof ServerPlayer player) {
                int playerScore = MobCultivationManager.getHighestPlayerRealmScore(player);
                double sensingChance = Math.min(0.90D, 0.25D + spirit * 0.02D);
                overwhelmed = playerScore - mobScore >= FLEE_REALM_GAP
                        && mob.getRandom().nextDouble() < sensingChance;
            }
            if (injured || overwhelmed) {
                data.startFleeing(currentTarget.getUUID(), injured ? 120 : 80);
            }
        }

        if (data.getFleeFrom() == null || data.getFleeTicks() <= 0) {
            data.stopFleeing();
            return;
        }
        Entity sourceEntity = level.getEntityInAnyDimension(data.getFleeFrom());
        if (!(sourceEntity instanceof LivingEntity threat)
                || threat.level() != mob.level()
                || !threat.isAlive()
                || threat.distanceToSqr(mob) > 48.0D * 48.0D) {
            data.stopFleeing();
            return;
        }

        Vec3 away = mob.position().subtract(threat.position());
        if (away.lengthSqr() < 0.0001D) {
            away = new Vec3(mob.getRandom().nextDouble() - 0.5D, 0.0D, mob.getRandom().nextDouble() - 0.5D);
        }
        Vec3 destination = mob.position().add(away.normalize().scale(12.0D));
        mob.setTarget(null);
        mob.getNavigation().moveTo(destination.x, destination.y, destination.z, 1.25D);
        data.tickFleeing(AI_INTERVAL);
    }

    private static double defaultRetreatThreshold(MobCultivationData data, int realmScore) {
        double threshold = data.getCategory() == MobCultivationCategory.HOSTILE && realmScore >= 3
                ? 0.18D
                : 0.0D;
        for (var traitId : data.getTraits()) {
            MobCultivationTraitDefinition trait = MobCultivationTraitManager.get(traitId);
            if (trait != null) threshold = Math.max(threshold, trait.retreatHealthThreshold());
        }
        return threshold;
    }

    private static void tickHighQiSeeking(Mob mob, MobCultivationData data, long gameTime) {
        if (!(mob.level() instanceof ServerLevel level)
                || mob.getTarget() != null
                || !mob.getNavigation().isDone()
                || gameTime < data.getNextQiSeekGameTime()) {
            return;
        }
        boolean traitAllows = false;
        for (var traitId : data.getTraits()) {
            MobCultivationTraitDefinition trait = MobCultivationTraitManager.get(traitId);
            if (trait != null && trait.seekHighQi()) {
                traitAllows = true;
                break;
            }
        }
        double spirit = MobCultivationManager.getEntityData(mob).getSource().getValue(AscensionStats.SPIRIT.get());
        if (!traitAllows && spirit < 8.0D) {
            data.setNextQiSeekGameTime(gameTime + QI_SEARCH_INTERVAL);
            return;
        }

        BlockPos origin = mob.blockPosition();
        BlockPos best = origin;
        double bestRatio = MobCultivationGrowth.getAtmosphericQiRatio(mob, origin);
        for (int i = 0; i < 6; i++) {
            int x = mob.getRandom().nextIntBetweenInclusive(-20, 20);
            int z = mob.getRandom().nextIntBetweenInclusive(-20, 20);
            BlockPos candidate = origin.offset(x, 0, z);
            if (!level.hasChunkAt(candidate)) continue;
            double ratio = MobCultivationGrowth.getAtmosphericQiRatio(mob, candidate);
            if (ratio > bestRatio + 0.08D) {
                best = candidate;
                bestRatio = ratio;
            }
        }
        if (!best.equals(origin)) {
            mob.getNavigation().moveTo(best.getX() + 0.5D, best.getY(), best.getZ() + 0.5D, 0.9D);
        }
        data.setNextQiSeekGameTime(gameTime + QI_SEARCH_INTERVAL + mob.getRandom().nextInt(200));
    }
}
