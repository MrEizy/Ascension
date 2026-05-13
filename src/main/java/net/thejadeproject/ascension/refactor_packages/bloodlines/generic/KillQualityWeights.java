package net.thejadeproject.ascension.refactor_packages.bloodlines.generic;


import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.warden.Warden;
import net.thejadeproject.ascension.data_attachments.ModAttachments;
import net.thejadeproject.ascension.mob_cultivation.MobCultivationData;
import net.thejadeproject.ascension.mob_cultivation.MobCultivationList;
import net.thejadeproject.ascension.mob_cultivation.MobCultivationResolver;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Defines how much purity a kill is worth based on configurable rules.
 *
 * Rules are evaluated in registration order — the first matching rule wins.
 * If no rule matches, {@link #defaultGain} is returned.
 *
 * Build via {@link KillQualityWeights#builder()}.
 *
 * Example:
 * <pre>{@code
 * KillQualityWeights.builder()
 *     .defaultGain(0.01)
 *     .ifBoss(0.5)
 *     .ifPassive(0.0)
 *     .ifRealmAtLeast("golden_core", 1, 0.05)
 *     .ifHealthAtLeast(200, 0.08)
 *     .ifEntity(Zombie.class, 0.015)
 *     .scaledByCombatPower(15, 0.1, 0.5)
 *     .build();
 * }</pre>
 */
public class KillQualityWeights {

    // ─────────────────────────────── rule type ────────────────────────────────

    /**
     * A single evaluation rule. Gain is computed dynamically from the entity
     * so both fixed and scaled rules share the same type with no inheritance tricks.
     */
    private static class WeightRule {
        private final Predicate<LivingEntity> predicate;
        private final Function<LivingEntity, Double> gainFn;

        WeightRule(Predicate<LivingEntity> predicate, double fixedGain) {
            this.predicate = predicate;
            this.gainFn    = e -> fixedGain;
        }

        WeightRule(Predicate<LivingEntity> predicate, Function<LivingEntity, Double> gainFn) {
            this.predicate = predicate;
            this.gainFn    = gainFn;
        }

        boolean matches(LivingEntity e)  { return predicate.test(e); }
        double  gain(LivingEntity e)     { return gainFn.apply(e); }
    }

    // ─────────────────────────────── core ─────────────────────────────────────

    private final double defaultGain;
    private final List<WeightRule> rules;

    private KillQualityWeights(double defaultGain, List<WeightRule> rules) {
        this.defaultGain = defaultGain;
        this.rules       = List.copyOf(rules);
    }

    /**
     * Evaluates purity gain for a kill.
     * Returns the gain of the first matching rule, or defaultGain if none match.
     */
    public double evaluate(LivingEntity killed) {
        for (WeightRule rule : rules) {
            if (rule.matches(killed)) return rule.gain(killed);
        }
        return defaultGain;
    }

    public double getDefaultGain() { return defaultGain; }

    public static Builder builder() { return new Builder(); }

    // ─────────────────────────────── builder ──────────────────────────────────

    public static class Builder {

        private double defaultGain = 0.01;
        private final List<WeightRule> rules = new ArrayList<>();

        private Builder() {}

        /** Fallback gain used when no rule matches. */
        public Builder defaultGain(double gain) {
            this.defaultGain = gain;
            return this;
        }

        /** Fully custom predicate with a fixed gain value. */
        public Builder ifMatches(Predicate<LivingEntity> predicate, double gain) {
            rules.add(new WeightRule(predicate, gain));
            return this;
        }

        /** Fully custom predicate with a dynamic gain function. */
        public Builder ifMatchesDynamic(Predicate<LivingEntity> predicate,
                                        Function<LivingEntity, Double> gainFn) {
            rules.add(new WeightRule(predicate, gainFn));
            return this;
        }

        // ── Category shortcuts ─────────────────────────────────────────────

        /** Vanilla bosses — Ender Dragon, Wither, Warden. */
        public Builder ifBoss(double gain) {
            return ifMatches(e -> e instanceof EnderDragon
                    || e instanceof WitherBoss
                    || e instanceof Warden, gain);
        }

        /** Any hostile mob (Monster). */
        public Builder ifHostile(double gain) {
            return ifMatches(e -> e instanceof Monster, gain);
        }

        /** Any passive mob (Animal). */
        public Builder ifPassive(double gain) {
            return ifMatches(e -> e instanceof Animal, gain);
        }

        /** Exact entity class or subclass. */
        public Builder ifEntity(Class<? extends LivingEntity> type, double gain) {
            return ifMatches(type::isInstance, gain);
        }

        // ── Max health thresholds ──────────────────────────────────────────

        /** Fixed gain when killed entity's max health >= threshold. */
        public Builder ifHealthAtLeast(double threshold, double gain) {
            return ifMatches(e -> e.getMaxHealth() >= threshold, gain);
        }

        /** Fixed gain when killed entity's max health < threshold. */
        public Builder ifHealthBelow(double threshold, double gain) {
            return ifMatches(e -> e.getMaxHealth() < threshold, gain);
        }

        /**
         * Scales gain linearly with max health.
         * gain = clamp((maxHealth / referenceHealth) * baseGain, 0, maxGain)
         * Always matches — put this after more specific rules.
         */
        public Builder scaledByHealth(double referenceHealth, double baseGain, double maxGain) {
            return ifMatchesDynamic(
                    e -> true,
                    e -> Math.min(maxGain, Math.max(0,
                            (e.getMaxHealth() / referenceHealth) * baseGain))
            );
        }

        // ── Mob rank / cultivation realm ───────────────────────────────────

        /**
         * Fixed gain if the mob's rank is at least the given realm and stage.
         * Mobs without a MobRankData attachment or an uninitialized rank do not match.
         */
        public Builder ifRealmAtLeast(String minRealmId, int minStage, double gain) {
            return ifMatches(e -> {
                if (!e.hasData(ModAttachments.MOB_RANK)) return false;
                MobCultivationData data = e.getData(ModAttachments.MOB_RANK);
                if (data == null || !data.isInitialized()) return false;
                return MobCultivationList.isAtLeast(data.getRealmId(), data.getStage(),
                        minRealmId, minStage);
            }, gain);
        }

        /**
         * Fixed gain if the mob's combat power >= minPower.
         * Combat power = realmIndex * 3 + (stage - 1).
         */
        public Builder ifCombatPowerAtLeast(int minPower, double gain) {
            return ifMatches(
                    e -> MobCultivationResolver.resolveCombatPower(e) >= minPower, gain);
        }

        /**
         * Scales gain linearly with combat power.
         * gain = clamp((combatPower / referencePower) * baseGain, 0, maxGain)
         * Mobs with power <= 0 do not match (returns defaultGain instead).
         */
        public Builder scaledByCombatPower(int referencePower, double baseGain, double maxGain) {
            return ifMatchesDynamic(
                    e -> MobCultivationResolver.resolveCombatPower(e) > 0,
                    e -> {
                        int power = MobCultivationResolver.resolveCombatPower(e);
                        return Math.min(maxGain, Math.max(0,
                                ((double) power / referencePower) * baseGain));
                    }
            );
        }

        public KillQualityWeights build() {
            return new KillQualityWeights(defaultGain, rules);
        }
    }
}