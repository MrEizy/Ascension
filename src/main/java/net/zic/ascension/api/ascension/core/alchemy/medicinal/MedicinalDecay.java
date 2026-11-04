package net.zic.ascension.api.ascension.core.alchemy.medicinal;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.zic.ascension.Config;
import net.zic.ascension.common.item.ModItems;
import net.zic.ascension.common.item.components.AscensionComponents;

public final class MedicinalDecay {
    public static final double EXPIRE_AT = 0.01D;
    public static final double OPEN_AIR_RATE = 1.0D;

    private MedicinalDecay() {
    }

    public record State(double ageTicks, long checkedAt, double preservationRate) {
        public static final Codec<State> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.DOUBLE.fieldOf("age_ticks").forGetter(State::ageTicks),
                Codec.LONG.fieldOf("checked_at").forGetter(State::checkedAt),
                Codec.DOUBLE.fieldOf("preservation_rate").forGetter(State::preservationRate)
        ).apply(instance, State::new));

        public State {
            ageTicks = Double.isFinite(ageTicks) ? Mth.clamp(ageTicks, 0.0D, 1.0E15D) : 0.0D;
            checkedAt = Math.max(0L, checkedAt);
            preservationRate = Double.isFinite(preservationRate)
                    ? Mth.clamp(preservationRate, 0.0D, 100.0D) : OPEN_AIR_RATE;
        }
    }

    public static long now(Level level) {
        if (level instanceof ServerLevel server) return server.getServer().overworld().getGameTime();
        return level.getGameTime();
    }

    public static double agingSpeed() {
        return Config.ALCHEMY_SERVER.DECAY_SPEED.get();
    }

    public static double jadeRate() {
        return Config.ALCHEMY_SERVER.JADE_BOTTLE_RATE.get();
    }

    public static double lifetime(MedicinalProfile profile) {
        var substance = profile.composition().substance();
        double stability = Mth.clamp(0.35D + substance.purity() * 1.6D
                - substance.instability() * 0.20D - profile.composition().toxicity() * 0.12D,
                0.15D, 3.0D);
        return Math.max(1.0D, Config.ALCHEMY_SERVER.BASE_LIFESPAN_DAYS.get() * 24000.0D * stability);
    }

    public static State fresh(long now) {
        return new State(0.0D, now, OPEN_AIR_RATE);
    }

    public static double effectiveAge(State state, long now) {
        double elapsed = Math.max(0L, now - state.checkedAt());
        return Math.min(1.0E15D, state.ageTicks() + elapsed * state.preservationRate() * agingSpeed());
    }

    public static double vitality(MedicinalProfile profile, State state, long now) {
        double ratio = effectiveAge(state, now) / lifetime(profile);
        return ratio > 64.0D ? 0.0D : Math.exp(-ratio * ratio);
    }

    public static double vitality(ItemStack stack, long now) {
        MedicinalProfile profile = stack.get(AscensionComponents.MEDICINAL_PILL.get());
        if (profile == null) return 1.0D;
        State state = stack.get(AscensionComponents.MEDICINAL_DECAY.get());
        return vitality(profile, state == null ? fresh(now) : state, now);
    }

    public static boolean expired(ItemStack stack, long now) {
        return stack.has(AscensionComponents.MEDICINAL_PILL.get()) && vitality(stack, now) <= EXPIRE_AT;
    }

    public static void initialize(ItemStack stack, long now) {
        if (stack.has(AscensionComponents.MEDICINAL_PILL.get())
                && !stack.has(AscensionComponents.MEDICINAL_DECAY.get())) {
            stack.set(AscensionComponents.MEDICINAL_DECAY.get(), fresh(now));
        }
    }

    public static void transition(ItemStack stack, long now, double nextRate) {
        if (!stack.has(AscensionComponents.MEDICINAL_PILL.get())) return;
        State previous = stack.get(AscensionComponents.MEDICINAL_DECAY.get());
        if (previous == null) previous = fresh(now);
        double rate = Double.isFinite(nextRate) ? Mth.clamp(nextRate, 0.0D, 100.0D) : OPEN_AIR_RATE;
        if (previous.preservationRate() != rate || now < previous.checkedAt()) {
            stack.set(AscensionComponents.MEDICINAL_DECAY.get(),
                    new State(effectiveAge(previous, now), now, rate));
        }
    }

    public static void setInitialAge(ItemStack stack, long now, double ageTicks) {
        if (!stack.has(AscensionComponents.MEDICINAL_PILL.get())) return;
        stack.set(AscensionComponents.MEDICINAL_DECAY.get(), new State(ageTicks, now, OPEN_AIR_RATE));
    }

    public static void setInitialVitality(ItemStack stack, long now, double vitality) {
        MedicinalProfile profile = stack.get(AscensionComponents.MEDICINAL_PILL.get());
        if (profile == null) return;
        double retained = Mth.clamp(vitality, 0.0D, 1.0D);
        double age = retained == 0.0D ? lifetime(profile) * 64.0D
                : lifetime(profile) * Math.sqrt(-Math.log(retained));
        setInitialAge(stack, now, age);
    }

    public static long ticksUntilExpiry(MedicinalProfile profile, State state, long now) {
        double rate = state.preservationRate() * agingSpeed();
        if (rate <= 0.0D) return Long.MAX_VALUE;
        double limit = lifetime(profile) * Math.sqrt(-Math.log(EXPIRE_AT));
        return (long) Math.max(0.0D, Math.ceil((limit - effectiveAge(state, now)) / rate));
    }

    public static MedicinalProfile diminished(MedicinalProfile profile, double vitality) {
        double safe = Mth.clamp(vitality, 0.0D, 1.0D);
        return new MedicinalProfile(profile.composition(), profile.effects().stream()
                .map(effect -> effect.adverse() ? effect : new MedicinalProfile.Effect(
                        effect.id(), effect.potency() * safe,
                        (int) Math.round(effect.durationTicks() * safe), false))
                .toList(), profile.schemaVersion());
    }

    public static ItemStack toResidue(ItemStack stack) {
        return new ItemStack(ModItems.PILL_RESIDUE.get(), stack.getCount());
    }
}
