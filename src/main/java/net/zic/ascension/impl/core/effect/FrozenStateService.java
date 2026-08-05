package net.zic.ascension.impl.core.effect;

import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.common.data_attachements.AscensionAttachments;
import net.zic.ascension.common.util.ModTags;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public final class FrozenStateService {
    public static final TagKey<EntityType<?>> IMMUNE = ModTags.EntityTypes.FROZEN_IMMUNE;
    public static final TagKey<EntityType<?>> RESISTANT = ModTags.EntityTypes.FROZEN_RESISTANT;
    public static final TagKey<EntityType<?>> BOSS_PROFILE = ModTags.EntityTypes.FROZEN_BOSS_PROFILE;

    private static final double NORMAL_MULTIPLIER = 1.0D;
    private static final double RESISTANT_MULTIPLIER = 0.5D;
    private static final double BOSS_MULTIPLIER = 0.25D;
    private static final double NORMAL_DECAY = 0.005D;
    private static final double FIRE_DECAY = 0.04D;

    private FrozenStateService() {
    }

    public static double apply(LivingEntity entity, double amount, int decayDelay) {
        if (entity == null
                || entity.level().isClientSide()
                || !Double.isFinite(amount)
                || amount <= 0.0D
                || is(entity, IMMUNE)) {
            return get(entity);
        }

        State data = data(entity);
        data.setBuildup(data.buildup() + amount * resistanceMultiplier(entity));
        data.setDecayDelay(Math.max(data.decayDelay(), decayDelay));
        syncVanillaVisual(entity, data);
        return data.buildup();
    }

    public static double reduce(LivingEntity entity, double amount) {
        if (entity == null
                || entity.level().isClientSide()
                || !Double.isFinite(amount)
                || amount <= 0.0D) {
            return get(entity);
        }

        State data = data(entity);
        data.setBuildup(data.buildup() - amount);

        if (data.buildup() <= 0.0D) {
            data.setDecayDelay(0);
        }

        syncVanillaVisual(entity, data);
        return data.buildup();
    }

    public static void clear(LivingEntity entity) {
        if (entity == null || entity.level().isClientSide()) {
            return;
        }

        State data = data(entity);
        data.setBuildup(0.0D);
        data.setDecayDelay(0);

        syncVanillaVisual(entity, data);
    }

    public static void maintainMinimum(LivingEntity entity, double minimum) {
        if (entity == null || entity.level().isClientSide() || !Double.isFinite(minimum)) {
            return;
        }

        State data = data(entity);
        if (data.buildup() < minimum) {
            data.setBuildup(minimum);
            syncVanillaVisual(entity, data);
        }
    }

    public static double get(LivingEntity entity) {
        return entity == null ? 0.0D : data(entity).buildup();
    }

    public static boolean isActive(LivingEntity entity) {
        return entity != null && data(entity).isActive();
    }

    public static boolean isResistant(LivingEntity entity) {
        return entity != null && is(entity, RESISTANT);
    }

    public static boolean isBossProfile(LivingEntity entity) {
        return entity != null && is(entity, BOSS_PROFILE);
    }

    public static void tick(LivingEntity entity) {
        if (entity == null || entity.level().isClientSide()) {
            return;
        }

        State data = data(entity);
        if (!data.isActive()) {
            return;
        }

        if (entity.isOnFire()) {
            data.setBuildup(data.buildup() - FIRE_DECAY);
            data.setDecayDelay(0);
        } else if (data.decayDelay() > 0) {
            data.setDecayDelay(data.decayDelay() - 1);
        } else if (data.buildup() > 0.0D) {
            data.setBuildup(data.buildup() - NORMAL_DECAY);
        }

        syncVanillaVisual(entity, data);
    }

    private static State data(LivingEntity entity) {
        return entity.getData(AscensionAttachments.FROZEN_STATE);
    }

    private static double resistanceMultiplier(LivingEntity entity) {
        if (is(entity, BOSS_PROFILE)) {
            return BOSS_MULTIPLIER;
        }
        if (is(entity, RESISTANT)) {
            return RESISTANT_MULTIPLIER;
        }
        return NORMAL_MULTIPLIER;
    }

    private static boolean is(LivingEntity entity, TagKey<EntityType<?>> tag) {
        return entity.getType().builtInRegistryHolder().is(tag);
    }

    private static void syncVanillaVisual(LivingEntity entity, State data) {
        int desired = (int) Math.round(
                entity.getTicksRequiredToFreeze() * Math.clamp(data.buildup(), 0.0D, 1.0D)
        );
        int current = entity.getTicksFrozen();
        int previousCustom = data.visualTicks();

        if (current <= previousCustom || desired > current) {
            entity.setTicksFrozen(desired);
        }
        data.setVisualTicks(desired);
    }

    public static final class State {
        public static final Codec<State> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.DOUBLE.optionalFieldOf("buildup", 0.0D).forGetter(State::buildup),
                Codec.INT.optionalFieldOf("decay_delay", 0).forGetter(State::decayDelay),
                Codec.INT.optionalFieldOf("visual_ticks", 0).forGetter(State::visualTicks)
        ).apply(instance, State::new));

        private double buildup;
        private int decayDelay;
        private int visualTicks;

        public State() {
            this(0.0D, 0, 0);
        }

        private State(double buildup, int decayDelay, int visualTicks) {
            this.buildup = Math.clamp(buildup, 0.0D, 1.0D);
            this.decayDelay = Math.max(0, decayDelay);
            this.visualTicks = Math.max(0, visualTicks);
        }

        public double buildup() {
            return buildup;
        }

        public int decayDelay() {
            return decayDelay;
        }

        public int visualTicks() {
            return visualTicks;
        }

        public void setBuildup(double value) {
            buildup = Math.clamp(value, 0.0D, 1.0D);
        }

        public void setDecayDelay(int value) {
            decayDelay = Math.max(0, value);
        }

        public void setVisualTicks(int value) {
            visualTicks = Math.max(0, value);
        }

        public boolean isActive() {
            return buildup > 0.0D || decayDelay > 0 || visualTicks > 0;
        }
    }
}
