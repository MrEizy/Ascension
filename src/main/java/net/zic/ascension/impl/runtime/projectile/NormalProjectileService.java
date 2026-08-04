package net.zic.ascension.impl.runtime.projectile;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.capabilities.CoreCapabilities;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.damage.AscensionDamageTypeHolders;
import net.zic.ascension.api.ascension.core.projectile.NormalProjectileDefinition;
import net.zic.ascension.api.ascension.core.skill.castable.feature.SkillExecutionAttribution;
import net.zic.ascension.api.ascension.core.skill.castable.feature.SkillExecutionContext;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.ascension.value.ScaledValueContext;
import net.zic.ascension.api.rpg_engine.damage.RPGEngineDamageSource;
import net.zic.ascension.api.rpg_engine.damage.RPGEngineEntityDamagedEvent;
import net.zic.ascension.api.rpg_engine.damage.RPGEngineGatherDamageTypesEvent;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.common.data_attachements.AscensionAttachments;
import net.zic.ascension.impl.core.control.StaggerService;
import net.zic.ascension.impl.core.effect.SkillEffectService;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class NormalProjectileService {
    public static final Identifier INITIAL_SPEED = AscensionCraft.prefix("projectile_initial_speed");
    public static final Identifier CURRENT_SPEED = AscensionCraft.prefix("projectile_current_speed");
    public static final Identifier DISTANCE = AscensionCraft.prefix("projectile_distance");
    public static final Identifier AGE = AscensionCraft.prefix("projectile_age");
    public static final Identifier CHARGE = AscensionCraft.prefix("projectile_charge");
    public static final Identifier IMPACT_DAMAGE = AscensionCraft.prefix("projectile_impact_damage");

    private NormalProjectileService() {
    }

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof Projectile projectile)
                || !(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        initialize(level, projectile);
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Pre event) {
        if (!(event.getEntity() instanceof Projectile projectile)
                || !(projectile.level() instanceof ServerLevel level)) {
            return;
        }
        NormalProjectileData data = projectile.getData(AscensionAttachments.NORMAL_PROJECTILE_DATA);
        if (!data.initialized()) {
            initialize(level, projectile);
        }
        if (!data.active()) {
            return;
        }
        data.tick();
        steer(level, projectile, data);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void gatherDamageTypes(RPGEngineGatherDamageTypesEvent event) {
        if (!(event.getSource() instanceof RPGEngineDamageSource source)
                || !(source.getDirectEntity() instanceof Projectile projectile)) {
            return;
        }
        NormalProjectileData data = projectile.getData(AscensionAttachments.NORMAL_PROJECTILE_DATA);
        if (!data.active()) {
            return;
        }
        Entity ownerEntity = projectile.getOwner();
        if (!(ownerEntity instanceof LivingEntity owner)) {
            return;
        }

        LinkedHashSet<Identifier> classifications = new LinkedHashSet<>();
        classifications.add(AscensionCraft.prefix("projectile"));
        classifications.add(AscensionCraft.prefix("ranged"));
        Identifier primaryProfile = null;
        NormalProjectileDefinition primaryDefinition = null;

        for (Identifier profileId : data.profiles()) {
            NormalProjectileDefinition definition = definition(projectile, profileId);
            if (definition == null) {
                continue;
            }
            if (primaryDefinition == null
                    || primaryDefinition.requiredSkill().isEmpty() && definition.requiredSkill().isPresent()) {
                primaryProfile = profileId;
                primaryDefinition = definition;
            }
            classifications.addAll(definition.classifications());
            if (!source.hasDamageTypeHolder(AscensionDamageTypeHolders.PATH)) {
                definition.path().ifPresent(value -> AscensionDamageTypeHolders.attachPath(source, value));
            }
        }

        AscensionDamageTypeHolders.attachClassifications(source, classifications);
        if (primaryDefinition != null && primaryProfile != null) {
            Identifier skill = primaryDefinition.requiredSkill().orElse(primaryProfile);
            AscensionDamageTypeHolders.attachAttribution(
                    source,
                    new AscensionDamageTypeHolders.Attribution(
                            owner.getUUID(),
                            owner.getUUID(),
                            skill,
                            primaryDefinition.technique(),
                            Optional.of(primaryProfile),
                            Optional.of(projectile.getUUID())
                    )
            );
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onDamage(RPGEngineEntityDamagedEvent.Pre event) {
        if (!(event.getSource().getDirectEntity() instanceof Projectile projectile)
                || !(projectile.getOwner() instanceof LivingEntity owner)) {
            return;
        }
        NormalProjectileData data = projectile.getData(AscensionAttachments.NORMAL_PROJECTILE_DATA);
        if (!data.active()) {
            return;
        }

        double damage = event.getDamage();
        for (Identifier profileId : data.profiles()) {
            NormalProjectileDefinition definition = definition(projectile, profileId);
            if (definition == null) {
                continue;
            }
            ScaledValueContext context = valueContext(
                    projectile,
                    data,
                    owner,
                    event.getEntity(),
                    profileId,
                    definition,
                    damage
            );
            double multiplier = definition.damageMultiplier().resolve(context);
            double bonus = definition.bonusDamage().resolve(context);
            if (!Double.isFinite(multiplier)) {
                multiplier = 1.0D;
            }
            if (!Double.isFinite(bonus)) {
                bonus = 0.0D;
            }
            damage = Math.max(0.0D, damage * Math.max(0.0D, multiplier) + bonus);
        }
        event.setDamage(damage);
    }

    @SubscribeEvent
    public static void onDamagePost(RPGEngineEntityDamagedEvent.Post event) {
        if (event.getDamage() <= 0.0D
                || !(event.getSource().getDirectEntity() instanceof Projectile projectile)
                || !(projectile.level() instanceof ServerLevel level)
                || !(projectile.getOwner() instanceof ServerPlayer owner)) {
            return;
        }
        NormalProjectileData data = projectile.getData(AscensionAttachments.NORMAL_PROJECTILE_DATA);
        if (!data.active()) {
            return;
        }

        for (Identifier profileId : data.profiles()) {
            NormalProjectileDefinition definition = definition(projectile, profileId);
            if (definition == null || definition.stagger().isEmpty()) {
                continue;
            }
            NormalProjectileDefinition.Stagger stagger = definition.stagger().get();
            ScaledValueContext valueContext = valueContext(
                    projectile,
                    data,
                    owner,
                    event.getEntity(),
                    profileId,
                    definition,
                    event.getDamage()
            );
            double amount = stagger.amount().resolve(valueContext);
            if (stagger.scaleWithDamage()) {
                amount *= event.getDamage();
            }
            if (!Double.isFinite(amount) || amount <= 0.0D) {
                continue;
            }
            Map<Identifier, Double> variables = variables(projectile, data, definition, event.getDamage());
            Identifier skill = definition.requiredSkill().orElse(profileId);
            SkillExecutionContext context = new SkillExecutionContext(
                    level,
                    owner,
                    skill,
                    event.getEntity(),
                    event.getEntity().position().add(0.0D, event.getEntity().getBbHeight() * 0.5D, 0.0D),
                    charge(data, definition),
                    variables,
                    new SkillExecutionAttribution(
                            owner.getUUID(),
                            owner.getUUID(),
                            projectile,
                            Optional.of(profileId),
                            Optional.of(projectile.getUUID())
                    )
            );
            StaggerService.apply(context, event.getEntity(), stagger.profile(), amount);
        }
    }

    private static void initialize(ServerLevel level, Projectile projectile) {
        NormalProjectileData data = projectile.getData(AscensionAttachments.NORMAL_PROJECTILE_DATA);
        if (data.initialized()) {
            return;
        }
        Entity ownerEntity = projectile.getOwner();
        if (!(ownerEntity instanceof LivingEntity owner)) {
            return;
        }
        OriginSource source = originSource(owner);
        List<Identifier> profiles = new ArrayList<>();
        for (Map.Entry<net.minecraft.resources.ResourceKey<NormalProjectileDefinition>, NormalProjectileDefinition> entry
                : CoreRegistries.NORMAL_PROJECTILE_REGISTRY.get(level.registryAccess()).entrySet()) {
            Identifier profileId = entry.getKey().identifier();
            NormalProjectileDefinition definition = entry.getValue();
            if (matches(projectile, definition)
                    && definition.requiredSkill().map(skill -> source != null
                    && AscensionOriginSourceHelper.hasSkill(source, skill)).orElse(true)) {
                profiles.add(profileId);
            }
        }
        data.initialize(profiles, projectile.position(), projectile.getDeltaMovement().length());
    }

    private static boolean matches(Projectile projectile, NormalProjectileDefinition definition) {
        boolean hasSelector = definition.projectileTag().isPresent() || !definition.projectileTypes().isEmpty();
        if (!hasSelector) {
            return true;
        }
        Identifier type = BuiltInRegistries.ENTITY_TYPE.getKey(projectile.getType());
        if (definition.projectileTypes().contains(type)) {
            return true;
        }
        return definition.projectileTag()
                .map(id -> projectile.getType().builtInRegistryHolder().is(TagKey.create(Registries.ENTITY_TYPE, id)))
                .orElse(false);
    }

    private static void steer(ServerLevel level, Projectile projectile, NormalProjectileData data) {
        if (projectile.getDeltaMovement().lengthSqr() < 0.0025D
                || !(projectile.getOwner() instanceof LivingEntity owner)) {
            return;
        }
        for (Identifier profileId : data.profiles()) {
            NormalProjectileDefinition definition = definition(projectile, profileId);
            if (definition == null || definition.steering().isEmpty()) {
                continue;
            }
            NormalProjectileDefinition.Steering steering = definition.steering().get();
            ScaledValueContext context = valueContext(
                    projectile,
                    data,
                    owner,
                    null,
                    profileId,
                    definition,
                    0.0D
            );
            double range = steering.range().resolve(context);
            double turnRate = steering.turnRate().resolve(context);
            if (!Double.isFinite(range) || range <= 0.0D || !Double.isFinite(turnRate) || turnRate <= 0.0D) {
                continue;
            }
            LivingEntity target = resolveTarget(level, projectile, data, owner, steering, range);
            if (target == null) {
                return;
            }
            Vec3 velocity = projectile.getDeltaMovement();
            double speed = velocity.length();
            Vec3 desired = target.position()
                    .add(0.0D, target.getBbHeight() * 0.55D, 0.0D)
                    .subtract(projectile.position());
            if (speed <= 0.0D || desired.lengthSqr() <= 1.0E-8D) {
                return;
            }
            Vec3 currentDirection = velocity.normalize();
            Vec3 desiredDirection = desired.normalize();
            double angle = Math.acos(Math.clamp(currentDirection.dot(desiredDirection), -1.0D, 1.0D));
            Vec3 resolvedDirection;
            if (angle <= turnRate) {
                resolvedDirection = desiredDirection;
            } else {
                double blend = Math.clamp(turnRate / angle, 0.0D, 1.0D);
                resolvedDirection = currentDirection.scale(1.0D - blend).add(desiredDirection.scale(blend)).normalize();
            }
            projectile.setDeltaMovement(resolvedDirection.scale(speed));
            projectile.hurtMarked = true;
            return;
        }
    }

    private static LivingEntity resolveTarget(
            ServerLevel level,
            Projectile projectile,
            NormalProjectileData data,
            LivingEntity owner,
            NormalProjectileDefinition.Steering steering,
            double range
    ) {
        UUID targetId = data.steeringTarget();
        if (targetId != null) {
            Entity entity = level.getEntity(targetId);
            if (entity instanceof LivingEntity living
                    && validTarget(projectile, owner, living, steering, range)) {
                return living;
            }
            data.setSteeringTarget(null);
        }
        if (data.age() % steering.reacquireInterval() != 0) {
            return null;
        }

        LivingEntity best = null;
        double bestScore = Double.MAX_VALUE;
        for (LivingEntity candidate : level.getEntitiesOfClass(
                LivingEntity.class,
                projectile.getBoundingBox().inflate(range),
                entity -> validTarget(projectile, owner, entity, steering, range)
        )) {
            Vec3 toTarget = candidate.position()
                    .add(0.0D, candidate.getBbHeight() * 0.5D, 0.0D)
                    .subtract(projectile.position());
            double distance = toTarget.length();
            Vec3 direction = projectile.getDeltaMovement().normalize();
            double alignment = direction.lengthSqr() <= 0.0D ? 0.0D : direction.dot(toTarget.normalize());
            double score = distance * (1.5D - Math.max(-0.5D, alignment));
            if (score < bestScore) {
                best = candidate;
                bestScore = score;
            }
        }
        data.setSteeringTarget(best == null ? null : best.getUUID());
        return best;
    }

    private static boolean validTarget(
            Projectile projectile,
            LivingEntity owner,
            LivingEntity target,
            NormalProjectileDefinition.Steering steering,
            double range
    ) {
        if (target == owner
                || !target.isAlive()
                || target.isRemoved()
                || target.distanceToSqr(projectile) > range * range) {
            return false;
        }
        UUID sourceEntity = steering.ownerScoped() ? owner.getUUID() : null;
        return SkillEffectService.has(
                target,
                steering.effect(),
                sourceEntity,
                steering.sourceSkill().orElse(null)
        );
    }

    private static NormalProjectileDefinition definition(Projectile projectile, Identifier profileId) {
        return projectile.level() instanceof ServerLevel level
                ? CoreRegistries.safeAccess(
                CoreRegistries.NORMAL_PROJECTILE_REGISTRY,
                profileId,
                level.registryAccess()
        )
                : null;
    }

    private static ScaledValueContext valueContext(
            Projectile projectile,
            NormalProjectileData data,
            LivingEntity owner,
            LivingEntity target,
            Identifier profileId,
            NormalProjectileDefinition definition,
            double impactDamage
    ) {
        Identifier skill = definition.requiredSkill().orElse(profileId);
        return new ScaledValueContext(
                originSource(owner),
                skill,
                owner,
                target,
                charge(data, definition),
                variables(projectile, data, definition, impactDamage)
        );
    }

    private static Map<Identifier, Double> variables(
            Projectile projectile,
            NormalProjectileData data,
            NormalProjectileDefinition definition,
            double impactDamage
    ) {
        Map<Identifier, Double> variables = new LinkedHashMap<>();
        variables.put(INITIAL_SPEED, data.initialSpeed());
        variables.put(CURRENT_SPEED, projectile.getDeltaMovement().length());
        variables.put(DISTANCE, projectile.position().distanceTo(data.launchPosition()));
        variables.put(AGE, (double) data.age());
        variables.put(CHARGE, charge(data, definition));
        variables.put(IMPACT_DAMAGE, Math.max(0.0D, impactDamage));
        return variables;
    }

    private static double charge(NormalProjectileData data, NormalProjectileDefinition definition) {
        return Math.clamp(data.initialSpeed() / definition.fullChargeSpeed(), 0.0D, 1.0D);
    }

    private static OriginSource originSource(LivingEntity entity) {
        var provider = entity.getCapability(CoreCapabilities.ASCENSION_ENTITY_DATA_PROVIDER_CAPABILITY);
        return provider == null ? null : provider.getData(entity).getSource();
    }
}
