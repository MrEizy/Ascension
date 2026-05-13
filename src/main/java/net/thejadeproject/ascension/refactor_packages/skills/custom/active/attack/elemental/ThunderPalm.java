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

public class ThunderPalm extends SimpleInstantCastSkill {
    private static final double QI_COST = 32.0D;
    private static final double RANGE = 4.5D;
    private static final float BASE_DAMAGE = 12.0F;
    private static final int COOLDOWN = 90;

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

        LivingEntity target = SkillTargetingHelper.findLookTarget(player, RANGE, 1.0D);
        if (target == null) return;

        PathData lightning = entityData.getPathData(ModPaths.LIGHTNING.getId());
        int major = lightning != null ? lightning.getMajorRealm() : 0;
        int minor = lightning != null ? lightning.getMinorRealm() : 0;

        float damage = BASE_DAMAGE + major * 5.0F + minor * 0.6F;

        AscensionDamageHandler.AscensionDamageSource source =
                new AscensionDamageHandler.AscensionDamageSource(
                        new HashSet<>() {{ add(ModPaths.LIGHTNING.getId()); }},
                        player.damageSources().magic()
                );

        target.hurt(source, damage);
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 50, 2, true, false, false));

        player.serverLevel().sendParticles(
                ParticleTypes.ELECTRIC_SPARK,
                target.getX(), target.getY() + target.getBbHeight() * 0.5D, target.getZ(),
                24,
                0.35D, 0.45D, 0.35D,
                0.08D
        );
    }

    @Override public int getCooldown(CastEndData castEndData) { return COOLDOWN; }
    @Override protected String getTitleKey() { return "ascension.skill.thunder_palm"; }
    @Override protected String getDescriptionKey() { return "ascension.skill.thunder_palm.description"; }
}