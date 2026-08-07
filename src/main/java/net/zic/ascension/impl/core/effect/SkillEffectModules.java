package net.zic.ascension.impl.core.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.effect.SkillEffectContext;
import net.zic.ascension.api.ascension.core.effect.SkillEffectModule;
import net.zic.ascension.api.ascension.core.resource.ResourceModifiers;
import net.zic.ascension.api.ascension.core.resource.ResourceTransactionRequest;
import net.zic.ascension.api.ascension.core.resource.ResourceTransactionService;
import net.zic.ascension.api.ascension.core.skill.castable.feature.SkillExecutionAttribution;
import net.zic.ascension.api.ascension.core.skill.castable.feature.SkillExecutionContext;
import net.zic.ascension.api.ascension.datapack.CodecType;
import net.zic.ascension.api.ascension.value.ScaledValue;
import net.zic.ascension.impl.core.damage.AscensionDamageService;
import net.zic.ascension.impl.datapack.effect.AscensionSkillEffectModuleTypes;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class SkillEffectModules {
    private static final Identifier EFFECT_POTENCY = AscensionCraft.prefix("effect_potency");
    private static final Identifier EFFECT_STACKS = AscensionCraft.prefix("effect_stacks");
    private static final Identifier DEFAULT_DAMAGE_TYPE = Identifier.fromNamespaceAndPath("minecraft", "magic");

    private SkillEffectModules() {
    }

    public record EntityProfile(
            Optional<Identifier> immuneTag,
            Optional<Identifier> resistantTag,
            Optional<Identifier> bossTag,
            double playerMultiplier,
            double resistantMultiplier,
            double bossMultiplier
    ) {
        public static final EntityProfile DEFAULT = new EntityProfile(
                Optional.empty(), Optional.empty(), Optional.empty(), 1.0D, 0.5D, 0.25D
        );
        public static final Codec<EntityProfile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Identifier.CODEC.optionalFieldOf("immune_tag").forGetter(EntityProfile::immuneTag),
                Identifier.CODEC.optionalFieldOf("resistant_tag").forGetter(EntityProfile::resistantTag),
                Identifier.CODEC.optionalFieldOf("boss_tag").forGetter(EntityProfile::bossTag),
                Codec.DOUBLE.optionalFieldOf("player_multiplier", 1.0D).forGetter(EntityProfile::playerMultiplier),
                Codec.DOUBLE.optionalFieldOf("resistant_multiplier", 0.5D).forGetter(EntityProfile::resistantMultiplier),
                Codec.DOUBLE.optionalFieldOf("boss_multiplier", 0.25D).forGetter(EntityProfile::bossMultiplier)
        ).apply(instance, EntityProfile::new));

        public EntityProfile {
            immuneTag = immuneTag == null ? Optional.empty() : immuneTag;
            resistantTag = resistantTag == null ? Optional.empty() : resistantTag;
            bossTag = bossTag == null ? Optional.empty() : bossTag;
            playerMultiplier = SkillEffectModules.sanitizeMultiplier(playerMultiplier);
            resistantMultiplier = SkillEffectModules.sanitizeMultiplier(resistantMultiplier);
            bossMultiplier = SkillEffectModules.sanitizeMultiplier(bossMultiplier);
        }

        public boolean immune(LivingEntity entity) {
            return matches(entity, immuneTag);
        }

        public double sanitizeMultiplier(LivingEntity entity) {
            if (matches(entity, bossTag)) {
                return bossMultiplier;
            }
            if (matches(entity, resistantTag)) {
                return resistantMultiplier;
            }
            return entity instanceof Player ? playerMultiplier : 1.0D;
        }

        private static boolean matches(LivingEntity entity, Optional<Identifier> tagId) {
            if (entity == null || tagId == null || tagId.isEmpty()) {
                return false;
            }
            TagKey<EntityType<?>> tag = TagKey.create(Registries.ENTITY_TYPE, tagId.get());
            return entity.getType().builtInRegistryHolder().is(tag);
        }
    }

    public record FrozenForm(
            ScaledValue frozenFloor,
            ScaledValue movementReduction,
            double playerMultiplier,
            double resistantMultiplier,
            double bossMultiplier,
            boolean removeWhenBurning
    ) implements SkillEffectModule {
        public static final MapCodec<FrozenForm> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ScaledValue.COMPACT_CODEC.fieldOf("frozen_floor").forGetter(FrozenForm::frozenFloor),
                ScaledValue.COMPACT_CODEC.fieldOf("movement_reduction").forGetter(FrozenForm::movementReduction),
                Codec.DOUBLE.optionalFieldOf("player_multiplier", 0.6D).forGetter(FrozenForm::playerMultiplier),
                Codec.DOUBLE.optionalFieldOf("resistant_multiplier", 0.5D).forGetter(FrozenForm::resistantMultiplier),
                Codec.DOUBLE.optionalFieldOf("boss_multiplier", 0.3D).forGetter(FrozenForm::bossMultiplier),
                Codec.BOOL.optionalFieldOf("remove_when_burning", true).forGetter(FrozenForm::removeWhenBurning)
        ).apply(instance, FrozenForm::new));

        public FrozenForm {
            playerMultiplier = sanitizeMultiplier(playerMultiplier);
            resistantMultiplier = sanitizeMultiplier(resistantMultiplier);
            bossMultiplier = sanitizeMultiplier(bossMultiplier);
        }

        @Override
        public CodecType<SkillEffectModule> getType() {
            return AscensionSkillEffectModuleTypes.FROZEN_FORM.get();
        }

        @Override
        public void tick(LivingEntity entity, SkillEffectContext effect) {
            double profile = profileMultiplier(entity);
            ScaledValue.Context context = effectContext(entity, effect);
            FrozenStateService.maintainMinimum(
                    entity,
                    Math.clamp(frozenFloor.resolve(context) * profile, 0.0D, 1.0D)
            );
            double reduction = Math.clamp(movementReduction.resolve(context) * profile, 0.0D, 0.9D);
            Vec3 velocity = entity.getDeltaMovement();
            entity.setDeltaMovement(
                    velocity.x * (1.0D - reduction),
                    velocity.y,
                    velocity.z * (1.0D - reduction)
            );
        }

        @Override
        public boolean shouldRemove(LivingEntity entity, SkillEffectContext context) {
            return removeWhenBurning && entity.isOnFire();
        }

        private double profileMultiplier(LivingEntity entity) {
            if (FrozenStateService.isBossProfile(entity)) {
                return bossMultiplier;
            }
            if (FrozenStateService.isResistant(entity)) {
                return resistantMultiplier;
            }
            return entity instanceof Player ? playerMultiplier : 1.0D;
        }
    }


    public record MovementRestriction(
            ScaledValue horizontalReduction,
            ScaledValue verticalReduction,
            boolean stopSprinting,
            EntityProfile profile
    ) implements SkillEffectModule {
        public static final MapCodec<MovementRestriction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ScaledValue.COMPACT_CODEC.optionalFieldOf("horizontal_reduction", ScaledValue.constant(0.0D))
                        .forGetter(MovementRestriction::horizontalReduction),
                ScaledValue.COMPACT_CODEC.optionalFieldOf("vertical_reduction", ScaledValue.constant(0.0D))
                        .forGetter(MovementRestriction::verticalReduction),
                Codec.BOOL.optionalFieldOf("stop_sprinting", true).forGetter(MovementRestriction::stopSprinting),
                EntityProfile.CODEC.optionalFieldOf("profile", EntityProfile.DEFAULT).forGetter(MovementRestriction::profile)
        ).apply(instance, MovementRestriction::new));

        public MovementRestriction {
            profile = profile == null ? EntityProfile.DEFAULT : profile;
        }

        @Override
        public CodecType<SkillEffectModule> getType() {
            return AscensionSkillEffectModuleTypes.MOVEMENT_RESTRICTION.get();
        }

        @Override
        public void tick(LivingEntity entity, SkillEffectContext effect) {
            if (profile.immune(entity)) {
                return;
            }
            double multiplier = profile.sanitizeMultiplier(entity);
            ScaledValue.Context context = effectContext(entity, effect);
            double horizontal = Math.clamp(horizontalReduction.resolve(context) * multiplier, 0.0D, 1.0D);
            double vertical = Math.clamp(verticalReduction.resolve(context) * multiplier, 0.0D, 1.0D);
            Vec3 velocity = entity.getDeltaMovement();
            entity.setDeltaMovement(
                    velocity.x * (1.0D - horizontal),
                    velocity.y * (1.0D - vertical),
                    velocity.z * (1.0D - horizontal)
            );
            if (stopSprinting && horizontal > 0.0D) {
                entity.setSprinting(false);
            }
        }
    }

    public record PeriodicDamage(
            ScaledValue amount,
            int interval,
            Identifier damageType,
            List<Identifier> classifications,
            Optional<Identifier> path,
            Optional<Identifier> technique,
            EntityProfile profile
    ) implements SkillEffectModule {
        public static final MapCodec<PeriodicDamage> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ScaledValue.COMPACT_CODEC.fieldOf("amount").forGetter(PeriodicDamage::amount),
                Codec.INT.optionalFieldOf("interval", 20).forGetter(PeriodicDamage::interval),
                Identifier.CODEC.optionalFieldOf("damage_type", DEFAULT_DAMAGE_TYPE).forGetter(PeriodicDamage::damageType),
                Identifier.CODEC.listOf().optionalFieldOf("classifications", List.of()).forGetter(PeriodicDamage::classifications),
                Identifier.CODEC.optionalFieldOf("path").forGetter(PeriodicDamage::path),
                Identifier.CODEC.optionalFieldOf("technique").forGetter(PeriodicDamage::technique),
                EntityProfile.CODEC.optionalFieldOf("profile", EntityProfile.DEFAULT).forGetter(PeriodicDamage::profile)
        ).apply(instance, PeriodicDamage::new));

        public PeriodicDamage {
            interval = Math.max(1, interval);
            classifications = classifications == null ? List.of() : List.copyOf(classifications);
            path = path == null ? Optional.empty() : path;
            technique = technique == null ? Optional.empty() : technique;
            profile = profile == null ? EntityProfile.DEFAULT : profile;
        }

        @Override
        public CodecType<SkillEffectModule> getType() {
            return AscensionSkillEffectModuleTypes.PERIODIC_DAMAGE.get();
        }

        @Override
        public void tick(LivingEntity entity, SkillEffectContext effect) {
            if (profile.immune(entity) || effect.remainingDuration() % interval != 0) {
                return;
            }
            SkillExecutionContext context = executionContext(entity, effect);
            if (context == null) {
                return;
            }
            double resolved = amount.resolve(effectContext(entity, effect)) * profile.sanitizeMultiplier(entity);
            AscensionDamageService.apply(
                    context,
                    resolved,
                    damageType,
                    new LinkedHashSet<>(classifications),
                    path,
                    technique
            );
        }
    }

    public record Spread(
            ScaledValue radius,
            double chance,
            int interval,
            int maximumTargets,
            int minimumRemainingDuration,
            double durationMultiplier,
            double potencyMultiplier,
            boolean onlyIfAbsent
    ) implements SkillEffectModule {
        public static final MapCodec<Spread> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ScaledValue.COMPACT_CODEC.optionalFieldOf("radius", ScaledValue.constant(3.0D)).forGetter(Spread::radius),
                Codec.DOUBLE.optionalFieldOf("chance", 0.02D).forGetter(Spread::chance),
                Codec.INT.optionalFieldOf("interval", 10).forGetter(Spread::interval),
                Codec.INT.optionalFieldOf("maximum_targets", 1).forGetter(Spread::maximumTargets),
                Codec.INT.optionalFieldOf("minimum_remaining_duration", 20).forGetter(Spread::minimumRemainingDuration),
                Codec.DOUBLE.optionalFieldOf("duration_multiplier", 0.5D).forGetter(Spread::durationMultiplier),
                Codec.DOUBLE.optionalFieldOf("potency_multiplier", 0.8D).forGetter(Spread::potencyMultiplier),
                Codec.BOOL.optionalFieldOf("only_if_absent", true).forGetter(Spread::onlyIfAbsent)
        ).apply(instance, Spread::new));

        public Spread {
            chance = Math.clamp(Double.isFinite(chance) ? chance : 0.0D, 0.0D, 1.0D);
            interval = Math.max(1, interval);
            maximumTargets = Math.clamp(maximumTargets, 1, 8);
            minimumRemainingDuration = Math.max(1, minimumRemainingDuration);
            durationMultiplier = Math.clamp(Double.isFinite(durationMultiplier) ? durationMultiplier : 0.5D, 0.05D, 1.0D);
            potencyMultiplier = Math.clamp(Double.isFinite(potencyMultiplier) ? potencyMultiplier : 0.8D, 0.0D, 1.0D);
        }

        @Override
        public CodecType<SkillEffectModule> getType() {
            return AscensionSkillEffectModuleTypes.SPREAD.get();
        }

        @Override
        public void tick(LivingEntity entity, SkillEffectContext effect) {
            if (!(entity.level() instanceof ServerLevel level)
                    || effect.remainingDuration() < minimumRemainingDuration
                    || effect.remainingDuration() % interval != 0
                    || level.getRandom().nextDouble() >= chance) {
                return;
            }

            double resolvedRadius = Math.clamp(radius.resolve(effectContext(entity, effect)), 0.0D, 16.0D);
            if (resolvedRadius <= 0.0D) {
                return;
            }

            UUID sourceId = effect.sourceEntity();
            AABB bounds = entity.getBoundingBox().inflate(resolvedRadius);
            List<LivingEntity> candidates = new ArrayList<>(level.getEntitiesOfClass(
                    LivingEntity.class,
                    bounds,
                    target -> target != entity
                            && target.isAlive()
                            && !target.isRemoved()
                            && !target.isSpectator()
                            && target.distanceToSqr(entity) <= resolvedRadius * resolvedRadius
                            && (sourceId == null || !sourceId.equals(target.getUUID()))
                            && (!onlyIfAbsent || !SkillEffectService.has(
                                    target,
                                    effect.definition(),
                                    null,
                                    null
                            ))
            ));

            int applied = 0;
            while (!candidates.isEmpty() && applied < maximumTargets) {
                LivingEntity target = candidates.remove(level.getRandom().nextInt(candidates.size()));
                int duration = Math.max(1, (int) Math.floor(effect.remainingDuration() * durationMultiplier));
                double potency = effect.potency() * potencyMultiplier;
                if (potency <= 0.0D) {
                    return;
                }
                if (SkillEffectService.apply(
                        target,
                        effect.definition(),
                        effect.sourceEntity(),
                        effect.sourceSkill(),
                        duration,
                        potency
                )) {
                    applied++;
                }
            }
        }
    }

    public record ParticleAura(
            Identifier particle,
            int count,
            double spread,
            double speed,
            int interval
    ) implements SkillEffectModule {
        public static final MapCodec<ParticleAura> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Identifier.CODEC.fieldOf("particle").forGetter(ParticleAura::particle),
                Codec.INT.optionalFieldOf("count", 2).forGetter(ParticleAura::count),
                Codec.DOUBLE.optionalFieldOf("spread", 0.35D).forGetter(ParticleAura::spread),
                Codec.DOUBLE.optionalFieldOf("speed", 0.02D).forGetter(ParticleAura::speed),
                Codec.INT.optionalFieldOf("interval", 5).forGetter(ParticleAura::interval)
        ).apply(instance, ParticleAura::new));

        public ParticleAura {
            count = Math.clamp(count, 0, 64);
            spread = Math.clamp(Double.isFinite(spread) ? spread : 0.0D, 0.0D, 8.0D);
            speed = Math.clamp(Double.isFinite(speed) ? speed : 0.0D, 0.0D, 2.0D);
            interval = Math.max(1, interval);
        }

        @Override
        public CodecType<SkillEffectModule> getType() {
            return AscensionSkillEffectModuleTypes.PARTICLE_AURA.get();
        }

        @Override
        public void tick(LivingEntity entity, SkillEffectContext effect) {
            if (!(entity.level() instanceof ServerLevel level)
                    || count <= 0
                    || effect.remainingDuration() % interval != 0) {
                return;
            }
            ParticleType<?> type = BuiltInRegistries.PARTICLE_TYPE.getValue(particle);
            if (!(type instanceof SimpleParticleType simple)) {
                return;
            }
            Vec3 position = entity.getBoundingBox().getCenter();
            level.sendParticles(
                    simple,
                    position.x,
                    position.y,
                    position.z,
                    count,
                    spread,
                    spread,
                    spread,
                    speed
            );
        }
    }

    public record ResourceModifierModule(
            Identifier id,
            ResourceTransactionRequest.Selector selector,
            ResourceModifiers.Operation operation,
            ScaledValue value,
            int priority
    ) implements SkillEffectModule {
        public static final MapCodec<ResourceModifierModule> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Identifier.CODEC.fieldOf("id").forGetter(ResourceModifierModule::id),
                ResourceTransactionRequest.Selector.CODEC.fieldOf("selector").forGetter(ResourceModifierModule::selector),
                ResourceModifiers.Operation.CODEC.fieldOf("operation").forGetter(ResourceModifierModule::operation),
                ScaledValue.COMPACT_CODEC.optionalFieldOf("value", ScaledValue.constant(0.0D)).forGetter(ResourceModifierModule::value),
                Codec.INT.optionalFieldOf("priority", 0).forGetter(ResourceModifierModule::priority)
        ).apply(instance, ResourceModifierModule::new));

        @Override
        public CodecType<SkillEffectModule> getType() {
            return AscensionSkillEffectModuleTypes.RESOURCE_MODIFIER.get();
        }

        public boolean matches(ResourceTransactionService.Context context) {
            return selector.matches(context);
        }

        public ResourceModifiers.Entry resolve(ResourceTransactionService.Context context, SkillEffectContext effect) {
            ScaledValue.Context scaledContext = new ScaledValue.Context(
                    null,
                    effect.sourceSkill(),
                    context.request().entity(),
                    context.request().target(),
                    effect.potency(),
                    Map.of(EFFECT_POTENCY, effect.potency(), EFFECT_STACKS, (double) effect.stacks())
            );
            Identifier resolvedId = Identifier.fromNamespaceAndPath(
                    id.getNamespace(),
                    "skill_effect/" + effect.definition().getNamespace() + "/" + effect.definition().getPath() + "/" + id.getPath()
            );
            return new ResourceModifiers.Entry(resolvedId, operation, value.resolve(scaledContext), priority);
        }
    }

    public record AirDrain(
            ScaledValue amount,
            int interval,
            ScaledValue emptyDamage,
            int damageInterval,
            EntityProfile profile
    ) implements SkillEffectModule {
        private static final Identifier DROWN_DAMAGE = Identifier.fromNamespaceAndPath("minecraft", "drown");

        public static final MapCodec<AirDrain> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ScaledValue.COMPACT_CODEC.optionalFieldOf("amount", ScaledValue.constant(4.0D)).forGetter(AirDrain::amount),
                Codec.INT.optionalFieldOf("interval", 1).forGetter(AirDrain::interval),
                ScaledValue.COMPACT_CODEC.optionalFieldOf("empty_damage", ScaledValue.constant(2.0D)).forGetter(AirDrain::emptyDamage),
                Codec.INT.optionalFieldOf("damage_interval", 20).forGetter(AirDrain::damageInterval),
                EntityProfile.CODEC.optionalFieldOf("profile", EntityProfile.DEFAULT).forGetter(AirDrain::profile)
        ).apply(instance, AirDrain::new));

        public AirDrain {
            interval = Math.max(1, interval);
            damageInterval = Math.max(1, damageInterval);
            profile = profile == null ? EntityProfile.DEFAULT : profile;
        }

        @Override
        public CodecType<SkillEffectModule> getType() {
            return AscensionSkillEffectModuleTypes.AIR_DRAIN.get();
        }

        @Override
        public void tick(LivingEntity entity, SkillEffectContext effect) {
            if (profile.immune(entity)) {
                return;
            }

            double profileMultiplier = profile.sanitizeMultiplier(entity);

            if (effect.remainingDuration() % interval == 0) {
                double resolved = amount.resolve(effectContext(entity, effect)) * profileMultiplier;
                int drain = Math.max(0, (int) Math.ceil(resolved));
                if (drain > 0) {
                    entity.setAirSupply(Math.max(0, entity.getAirSupply() - drain));
                }
            }

            if (entity.getAirSupply() > 0 || effect.remainingDuration() % damageInterval != 0) {
                return;
            }

            SkillExecutionContext context = executionContext(entity, effect);
            if (context == null) {
                return;
            }

            double damage = emptyDamage.resolve(effectContext(entity, effect)) * profileMultiplier;
            if (damage <= 0.0D) {
                return;
            }

            AscensionDamageService.apply(context, damage, DROWN_DAMAGE, new LinkedHashSet<>(), Optional.empty(), Optional.empty());
        }
    }


    private static ScaledValue.Context effectContext(LivingEntity entity, SkillEffectContext effect) {
        LivingEntity caster = sourceLivingEntity(entity, effect);
        return new ScaledValue.Context(
                null,
                effect.sourceSkill(),
                caster,
                entity,
                effect.potency(),
                Map.of(EFFECT_POTENCY, effect.potency(), EFFECT_STACKS, (double) effect.stacks())
        );
    }

    private static SkillExecutionContext executionContext(LivingEntity entity, SkillEffectContext effect) {
        if (!(entity.level() instanceof ServerLevel level)) {
            return null;
        }
        Entity source = effect.sourceEntity() == null ? null : level.getEntity(effect.sourceEntity());
        LivingEntity caster = source instanceof LivingEntity living ? living : entity;
        UUID ownerId = effect.sourceEntity() == null ? caster.getUUID() : effect.sourceEntity();
        Identifier sourceSkill = effect.sourceSkill() == null ? effect.definition() : effect.sourceSkill();
        return new SkillExecutionContext(
                level,
                caster,
                sourceSkill,
                entity,
                entity.getBoundingBox().getCenter(),
                effect.potency(),
                Map.of(EFFECT_POTENCY, effect.potency(), EFFECT_STACKS, (double) effect.stacks()),
                new SkillExecutionAttribution(ownerId, ownerId, source, Optional.empty(), Optional.empty())
        );
    }

    private static LivingEntity sourceLivingEntity(LivingEntity entity, SkillEffectContext effect) {
        if (entity.level() instanceof ServerLevel level && effect.sourceEntity() != null) {
            Entity source = level.getEntity(effect.sourceEntity());
            if (source instanceof LivingEntity living) {
                return living;
            }
        }
        return entity;
    }

    private static double sanitizeMultiplier(double value) {
        return Double.isFinite(value) ? Math.clamp(value, 0.0D, 1.0D) : 1.0D;
    }
}
