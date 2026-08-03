package net.zic.ascension.impl.core.damage;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.damage.AscensionDamageTypeHolders;
import net.zic.ascension.api.ascension.core.skill.castable.feature.SkillExecutionAttribution;
import net.zic.ascension.api.ascension.core.skill.castable.feature.SkillExecutionContext;
import net.zic.ascension.api.rpg_engine.damage.RPGEngineDamageSource;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class AscensionDamageService {
    private static final Set<Identifier> WARNED_MISSING_DAMAGE_TYPES = ConcurrentHashMap.newKeySet();

    private AscensionDamageService() {
    }

    public static boolean apply(
            SkillExecutionContext context,
            double amount,
            Identifier damageType,
            Set<Identifier> classifications,
            Optional<Identifier> path,
            Optional<Identifier> technique
    ) {
        LivingEntity target = context.target();
        if (target == null || target.isRemoved() || target.level().isClientSide()) {
            return false;
        }
        if (!Double.isFinite(amount) || amount <= 0.0D || damageType == null) {
            return false;
        }

        SkillExecutionAttribution executionAttribution = context.attribution();
        Entity owner = context.level().getEntity(executionAttribution.ownerId());
        if (owner == null || owner.isRemoved()) {
            owner = context.caster();
        }

        Entity direct = executionAttribution.directEntity();
        if (direct != null && (direct.isRemoved() || direct.level() != context.level())) {
            direct = null;
        }

        DamageSource vanillaSource;
        try {
            ResourceKey<DamageType> damageTypeKey = ResourceKey.create(Registries.DAMAGE_TYPE, damageType);
            vanillaSource = context.level().damageSources().source(damageTypeKey, direct, owner);
        } catch (RuntimeException exception) {
            if (WARNED_MISSING_DAMAGE_TYPES.add(damageType)) {
                AscensionCraft.LOGGER.warn(
                        "Unable to create damage source for datapack damage type {}",
                        damageType,
                        exception
                );
            }
            return false;
        }

        RPGEngineDamageSource source = new RPGEngineDamageSource(vanillaSource);
        path.ifPresent(value -> AscensionDamageTypeHolders.attachPath(source, value));

        LinkedHashSet<Identifier> resolvedClassifications = new LinkedHashSet<>();
        if (classifications != null) {
            resolvedClassifications.addAll(classifications);
        }
        AscensionDamageTypeHolders.attachClassifications(source, resolvedClassifications);
        AscensionDamageTypeHolders.attachAttribution(
                source,
                new AscensionDamageTypeHolders.Attribution(
                        executionAttribution.ownerId(),
                        executionAttribution.casterId(),
                        context.skill(),
                        technique,
                        executionAttribution.projectileDefinition(),
                        executionAttribution.projectileRuntime()
                )
        );

        float resolvedAmount = (float) Math.min(amount, Float.MAX_VALUE);
        return target.hurtServer(context.level(), source, resolvedAmount);
    }
}
