package net.thejadeproject.ascension.refactor_packages.skills.custom.active.attack.elemental;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.thejadeproject.ascension.data_attachments.ModAttachments;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
import net.thejadeproject.ascension.refactor_packages.handlers.AscensionDamageHandler;
import net.thejadeproject.ascension.refactor_packages.paths.ModPaths;
import net.thejadeproject.ascension.refactor_packages.paths.PathData;
import net.thejadeproject.ascension.refactor_packages.skill_casting.casting.CastEndData;
import net.thejadeproject.ascension.refactor_packages.skill_casting.casting.CastResult;
import net.thejadeproject.ascension.refactor_packages.skills.custom.SkillTargetingHelper;
import net.thejadeproject.ascension.refactor_packages.skills.custom.active.SimpleInstantCastSkill;

import java.util.HashSet;

public class ThornBind extends SimpleInstantCastSkill {
    private static final double QI_COST = 28.0D;
    private static final double RANGE = 16.0D;
    private static final float BASE_DAMAGE = 5.0F;
    private static final int COOLDOWN = 120;

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

        LivingEntity target = SkillTargetingHelper.findLookTarget(player, RANGE, 1.5D);
        if (target == null) return;

        PathData wood = entityData.getPathData(ModPaths.WOOD.getId());
        int major = wood != null ? wood.getMajorRealm() : 0;
        int minor = wood != null ? wood.getMinorRealm() : 0;

        float damage = BASE_DAMAGE + major * 2.5F + minor * 0.25F;
        int duration = 80 + major * 20;

        AscensionDamageHandler.AscensionDamageSource source =
                new AscensionDamageHandler.AscensionDamageSource(
                        new HashSet<>() {{ add(ModPaths.WOOD.getId()); }},
                        player.damageSources().magic()
                );

        target.hurt(source, damage);
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, 6, true, false, false));
        target.addEffect(new MobEffectInstance(MobEffects.POISON, duration, 0, true, false, false));

        player.serverLevel().sendParticles(
                ParticleTypes.SPORE_BLOSSOM_AIR,
                target.getX(), target.getY() + 0.25D, target.getZ(),
                32,
                0.45D, 0.3D, 0.45D,
                0.02D
        );
    }

    @Override public int getCooldown(CastEndData castEndData) { return COOLDOWN; }
    @Override protected String getTitleKey() { return "ascension.skill.thorn_bind"; }
    @Override protected String getDescriptionKey() { return "ascension.skill.thorn_bind.description"; }
}