package net.zic.ascension.api.ascension.core.alchemy.medicinal;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.zic.ascension.Config;
import net.zic.ascension.common.data_attachements.AscensionAttachments;


public final class MedicinalImpurities {
    private static final double MAX = 100.0D;
    private static final double TICKS_PER_DAY = 24000.0D;

    private MedicinalImpurities() {
    }

    public record State(double amount, long checkedAt) {
        public static final Codec<State> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.DOUBLE.fieldOf("amount").forGetter(State::amount),
                Codec.LONG.fieldOf("checked_at").forGetter(State::checkedAt)
        ).apply(instance, State::new));

        public State {
            amount = Double.isFinite(amount) ? Mth.clamp(amount, 0.0D, MAX) : 0.0D;
            checkedAt = Math.max(0L, checkedAt);
        }
    }

    public static double get(LivingEntity entity) {
        if (!(entity instanceof Player)) return 0.0D;
        long now = currentTime(entity.level());
        State stored = entity.getData(AscensionAttachments.MEDICINAL_IMPURITIES);
        return reconcile(stored, now).amount();
    }

    public static double efficiency(LivingEntity entity) {
        return Math.max(0.15D, 1.0D - get(entity) / MAX);
    }

    public static double add(LivingEntity entity, double amount) {
        if (!(entity instanceof Player) || !(entity.level() instanceof ServerLevel server)
                || !Double.isFinite(amount) || amount <= 0.0D) return get(entity);
        long now = server.getServer().overworld().getGameTime();
        double next = Mth.clamp(get(entity) + amount * Config.ALCHEMY_SERVER.IMPURITY_GAIN_MULTIPLIER.get(), 0.0D, MAX);
        entity.setData(AscensionAttachments.MEDICINAL_IMPURITIES, new State(next, now));
        return next;
    }

    public static double purify(LivingEntity entity, double amount) {
        if (!(entity instanceof Player) || !(entity.level() instanceof ServerLevel server)
                || !Double.isFinite(amount) || amount <= 0.0D) return 0.0D;
        long now = server.getServer().overworld().getGameTime();
        double before = get(entity);
        double removed = Math.min(before, amount);
        if (removed > 0.0D) {
            entity.setData(AscensionAttachments.MEDICINAL_IMPURITIES, new State(before - removed, now));
        }
        return removed;
    }

    public static double contaminants(MedicinalProfile profile, boolean refined) {
        var composition = profile.composition();
        var essence = composition.substance();
        double impure = 1.0D - Mth.clamp(essence.purity(), 0.0D, 1.0D);
        double strength = Math.sqrt(Mth.clamp(essence.amplifier(), 0.25D, 16.0D));
        double instability = Math.max(0.0D, essence.instability());
        double toxicity = Math.max(0.0D, composition.toxicity());
        return Mth.clamp((impure * (refined ? 6.0D : 14.0D) * strength)
                + instability * (refined ? 0.75D : 2.5D)
                + toxicity * (refined ? 2.0D : 4.0D), 0.0D, 25.0D);
    }

    private static long currentTime(Level level) {
        return level instanceof ServerLevel server
                ? server.getServer().overworld().getGameTime() : level.getGameTime();
    }

    private static State reconcile(State state, long now) {
        if (state.checkedAt() == 0L || now < state.checkedAt()) {
            return new State(state.amount(), now);
        }
        long elapsed = now - state.checkedAt();
        if (elapsed <= 0 || state.amount() == 0.0D) return state;
        double rate = Config.ALCHEMY_SERVER.IMPURITY_DECAY_PER_DAY.get();
        if (rate <= 0.0D) return new State(state.amount(), now);
        return new State(Math.max(0.0D, state.amount() - elapsed * rate / TICKS_PER_DAY), now);
    }
}
