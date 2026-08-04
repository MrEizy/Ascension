package net.zic.ascension.impl.core.control;

import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.capabilities.CoreCapabilities;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.control.StaggerDefinition;
import net.zic.ascension.api.ascension.core.skill.castable.feature.SkillExecutionContext;
import net.zic.ascension.api.ascension.core.skill.castable.feature.SkillExecutionFeature;
import net.zic.ascension.api.ascension.value.ScaledValueContext;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.common.data_attachements.AscensionAttachments;
import net.zic.ascension.common.util.ModTags;
import net.zic.ascension.impl.core.skill.passive.PassiveDefenseService;

import java.util.LinkedHashMap;
import java.util.Map;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class StaggerService {
    public static final Identifier APPLIED = AscensionCraft.prefix("stagger_applied");
    public static final Identifier BUILDUP = AscensionCraft.prefix("stagger_buildup");
    public static final Identifier THRESHOLD = AscensionCraft.prefix("stagger_threshold");
    public static final Identifier RESISTANCE = AscensionCraft.prefix("stagger_resistance");
    public static final Identifier GUARD_BREAK_TICKS = AscensionCraft.prefix("guard_break_ticks");
    public static final TagKey<EntityType<?>> IMMUNE = ModTags.EntityTypes.STAGGER_IMMUNE;
    public static final TagKey<EntityType<?>> RESISTANT = ModTags.EntityTypes.STAGGER_RESISTANT;
    public static final TagKey<EntityType<?>> BOSS_PROFILE = ModTags.EntityTypes.STAGGER_BOSS_PROFILE;

    private StaggerService() {
    }

    public static double apply(
            SkillExecutionContext context,
            LivingEntity target,
            Identifier profileId,
            double amount
    ) {
        if (context == null
                || target == null
                || profileId == null
                || target.level().isClientSide()
                || !target.isAlive()
                || !Double.isFinite(amount)
                || amount <= 0.0D
                || is(target, IMMUNE)) {
            return 0.0D;
        }

        StaggerDefinition definition = CoreRegistries.safeAccess(
                CoreRegistries.STAGGER_REGISTRY,
                profileId,
                target.registryAccess()
        );
        if (definition == null) {
            return 0.0D;
        }

        StaggerStateData state = data(target);
        if (state.isImmune()) {
            return 0.0D;
        }

        ScaledValueContext targetContext = targetContext(context, target);
        double threshold = definition.threshold().resolve(targetContext);
        double resistance = definition.resistance().resolve(targetContext);
        if (!Double.isFinite(threshold) || threshold <= 0.0D) {
            return 0.0D;
        }
        if (!Double.isFinite(resistance)) {
            resistance = 0.0D;
        }
        if (is(target, BOSS_PROFILE)) {
            resistance = Math.max(resistance, 0.75D);
        } else if (is(target, RESISTANT)) {
            resistance = Math.max(resistance, 0.5D);
        }
        double passiveResistance = PassiveDefenseService.staggerResistance(target, context);
        resistance = 1.0D - (1.0D - resistance) * (1.0D - passiveResistance);
        resistance = Math.clamp(resistance, 0.0D, 0.95D);

        double applied = amount * (1.0D - resistance);
        if (!Double.isFinite(applied) || applied <= 0.0D) {
            return 0.0D;
        }

        state.setProfile(profileId);
        state.setBuildup(state.buildup() + applied);
        state.setDecayDelay(Math.max(state.decayDelay(), definition.decayDelay()));

        if (state.buildup() >= threshold) {
            guardBreak(context, target, profileId, definition, targetContext, applied, threshold, resistance);
        }
        return applied;
    }

    public static double reduce(LivingEntity target, double amount) {
        if (target == null || target.level().isClientSide() || !Double.isFinite(amount) || amount <= 0.0D) {
            return 0.0D;
        }
        StaggerStateData state = data(target);
        double previous = state.buildup();
        state.setBuildup(previous - amount);
        return previous - state.buildup();
    }

    public static void clear(LivingEntity target) {
        if (target == null || target.level().isClientSide()) {
            return;
        }
        data(target).clear();
    }

    public static void grantImmunity(LivingEntity target, int duration) {
        if (target == null || target.level().isClientSide() || duration <= 0) {
            return;
        }
        data(target).grantImmunity(duration);
    }

    public static boolean forceGuardBreak(
            SkillExecutionContext context,
            LivingEntity target,
            Identifier profileId
    ) {
        if (context == null
                || target == null
                || profileId == null
                || target.level().isClientSide()
                || !target.isAlive()
                || is(target, IMMUNE)) {
            return false;
        }
        StaggerDefinition definition = CoreRegistries.safeAccess(
                CoreRegistries.STAGGER_REGISTRY,
                profileId,
                target.registryAccess()
        );
        if (definition == null || data(target).isImmune()) {
            return false;
        }
        ScaledValueContext targetContext = targetContext(context, target);
        double threshold = definition.threshold().resolve(targetContext);
        double resistance = definition.resistance().resolve(targetContext);
        if (!Double.isFinite(threshold) || threshold <= 0.0D) {
            return false;
        }
        if (!Double.isFinite(resistance)) {
            resistance = 0.0D;
        }
        if (is(target, BOSS_PROFILE)) {
            resistance = Math.max(resistance, 0.75D);
        } else if (is(target, RESISTANT)) {
            resistance = Math.max(resistance, 0.5D);
        }
        double passiveResistance = PassiveDefenseService.staggerResistance(target, context);
        resistance = 1.0D - (1.0D - resistance) * (1.0D - passiveResistance);
        resistance = Math.clamp(resistance, 0.0D, 0.95D);
        guardBreak(context, target, profileId, definition, targetContext, 0.0D, threshold, resistance);
        return true;
    }

    public static boolean isGuardBroken(LivingEntity target) {
        return target != null && data(target).isGuardBroken();
    }

    public static boolean isImmune(LivingEntity target) {
        return target != null && data(target).isImmune();
    }

    public static double buildup(LivingEntity target) {
        return target == null ? 0.0D : data(target).buildup();
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Pre event) {
        if (!(event.getEntity() instanceof LivingEntity entity) || entity.level().isClientSide()) {
            return;
        }
        tick(entity);
    }

    private static void tick(LivingEntity entity) {
        StaggerStateData state = data(entity);
        if (!state.isActive()) {
            return;
        }

        StaggerDefinition definition = state.profile() == null ? null : CoreRegistries.safeAccess(
                CoreRegistries.STAGGER_REGISTRY,
                state.profile(),
                entity.registryAccess()
        );
        if (state.profile() != null && definition == null) {
            state.clear();
            return;
        }

        if (state.isGuardBroken()) {
            double movementMultiplier = definition == null ? 0.2D : definition.movementMultiplier();
            entity.setSprinting(false);
            Vec3 movement = entity.getDeltaMovement();
            entity.setDeltaMovement(
                    movement.x * movementMultiplier,
                    movement.y,
                    movement.z * movementMultiplier
            );
            if (entity instanceof Mob mob) {
                mob.getNavigation().stop();
            }
            state.tickGuardBreak();
            return;
        }

        if (state.immunityTicks() > 0) {
            state.tickImmunity();
            if (!state.isActive()) {
                state.clear();
            }
            return;
        }

        if (state.decayDelay() > 0) {
            state.tickDecayDelay();
            return;
        }

        if (state.buildup() > 0.0D && definition != null) {
            double decay = definition.decayPerSecond().resolve(targetContext(entity, state.profile())) / 20.0D;
            if (Double.isFinite(decay) && decay > 0.0D) {
                state.setBuildup(state.buildup() - decay);
            }
        }
        if (state.buildup() <= 0.0D) {
            state.clear();
        }
    }

    private static void guardBreak(
            SkillExecutionContext context,
            LivingEntity target,
            Identifier profileId,
            StaggerDefinition definition,
            ScaledValueContext targetContext,
            double applied,
            double threshold,
            double resistance
    ) {
        int duration = resolveTicks(definition.guardBreakDuration().resolve(targetContext));
        int immunityDuration = resolveTicks(definition.immunityDuration().resolve(targetContext));
        StaggerStateData state = data(target);
        state.setProfile(profileId);
        state.beginGuardBreak(duration, immunityDuration);

        if (definition.interruptHeldCasts() && target instanceof Player player) {
            player.getData(AscensionAttachments.ASCENSION_SKILL_CAST_HANDLER).interruptCast();
        }

        if (definition.onGuardBreak().isEmpty()) {
            return;
        }
        Map<Identifier, Double> variables = new LinkedHashMap<>(context.variables());
        variables.put(APPLIED, applied);
        variables.put(BUILDUP, threshold);
        variables.put(THRESHOLD, threshold);
        variables.put(RESISTANCE, resistance);
        variables.put(GUARD_BREAK_TICKS, (double) duration);
        SkillExecutionContext breakContext = new SkillExecutionContext(
                context.level(),
                context.caster(),
                context.skill(),
                target,
                target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D),
                context.charge(),
                variables,
                context.attribution()
        );
        for (SkillExecutionFeature feature : definition.onGuardBreak()) {
            feature.apply(breakContext);
        }
    }

    private static ScaledValueContext targetContext(SkillExecutionContext context, LivingEntity target) {
        return new ScaledValueContext(
                originSource(target),
                context.skill(),
                target,
                context.caster(),
                context.charge(),
                context.variables()
        );
    }

    private static ScaledValueContext targetContext(LivingEntity target, Identifier profileId) {
        return new ScaledValueContext(
                originSource(target),
                profileId,
                target,
                null,
                0.0D,
                Map.of()
        );
    }

    private static OriginSource originSource(LivingEntity entity) {
        var provider = entity.getCapability(CoreCapabilities.ASCENSION_ENTITY_DATA_PROVIDER_CAPABILITY);
        return provider == null ? null : provider.getData(entity).getSource();
    }

    private static StaggerStateData data(LivingEntity entity) {
        return entity.getData(AscensionAttachments.STAGGER_STATE);
    }

    private static int resolveTicks(double value) {
        return !Double.isFinite(value) ? 0 : Math.max(0, (int) Math.round(value));
    }

    private static boolean is(LivingEntity entity, TagKey<EntityType<?>> tag) {
        return entity.getType().builtInRegistryHolder().is(tag);
    }
}
