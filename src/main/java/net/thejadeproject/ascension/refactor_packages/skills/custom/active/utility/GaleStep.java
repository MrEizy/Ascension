package net.thejadeproject.ascension.refactor_packages.skills.custom.active.utility;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.thejadeproject.ascension.data_attachments.ModAttachments;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
import net.thejadeproject.ascension.refactor_packages.paths.ModPaths;
import net.thejadeproject.ascension.refactor_packages.paths.PathData;
import net.thejadeproject.ascension.refactor_packages.skill_casting.casting.CastEndData;
import net.thejadeproject.ascension.refactor_packages.skill_casting.casting.CastResult;
import net.thejadeproject.ascension.refactor_packages.skills.custom.active.SimpleInstantCastSkill;

import java.util.List;

public class GaleStep extends SimpleInstantCastSkill {
    private static final double QI_COST = 24.0D;
    private static final double BASE_SPEED = 1.8D;
    private static final int COOLDOWN = 70;

    @Override
    public CastResult canCast(Entity caster, net.thejadeproject.ascension.refactor_packages.skills.castable.IPreCastData preCastData) {
        if (!(caster instanceof ServerPlayer player)) return new CastResult(CastResult.Type.FAILURE);
        if (!player.hasData(ModAttachments.ENTITY_DATA)) return new CastResult(CastResult.Type.FAILURE);

        IEntityData entityData = player.getData(ModAttachments.ENTITY_DATA);
        return entityData.getQiContainer().hasQi(QI_COST)
                ? new CastResult(CastResult.Type.SUCCESS)
                : new CastResult(CastResult.Type.FAILURE);
    }

    @Override
    public void initialCast(Entity caster, net.thejadeproject.ascension.refactor_packages.skills.castable.IPreCastData preCastData) {
        if (!(caster instanceof ServerPlayer player)) return;
        if (!player.hasData(ModAttachments.ENTITY_DATA)) return;

        IEntityData entityData = player.getData(ModAttachments.ENTITY_DATA);
        if (!entityData.getQiContainer().tryConsumeQi(QI_COST)) return;

        PathData wind = entityData.getPathData(ModPaths.WIND.getId());
        int major = wind != null ? wind.getMajorRealm() : 0;

        Vec3 dir = player.getLookAngle().normalize();
        double speed = BASE_SPEED + major * 0.18D;

        player.setDeltaMovement(dir.scale(speed).add(0.0D, 0.12D, 0.0D));
        player.hurtMarked = true;
        player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 60, 0, true, false, false));

        pushNearby(player, dir);

        player.serverLevel().sendParticles(
                ParticleTypes.CLOUD,
                player.getX(), player.getY() + 0.5D, player.getZ(),
                28,
                0.45D, 0.25D, 0.45D,
                0.08D
        );
    }

    private void pushNearby(ServerPlayer player, Vec3 dir) {
        List<LivingEntity> targets = player.serverLevel().getEntitiesOfClass(
                LivingEntity.class,
                new AABB(player.blockPosition()).inflate(2.0D),
                e -> e != player && e.isAlive() && !e.isSpectator()
        );

        for (LivingEntity target : targets) {
            Vec3 push = dir.scale(0.65D);
            target.push(push.x, 0.15D, push.z);
            target.hurtMarked = true;
        }
    }

    @Override public int getCooldown(CastEndData castEndData) { return COOLDOWN; }
    @Override protected String getTitleKey() { return "ascension.skill.gale_step"; }
    @Override protected String getDescriptionKey() { return "ascension.skill.gale_step.description"; }
}