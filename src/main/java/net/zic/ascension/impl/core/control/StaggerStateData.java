package net.zic.ascension.impl.core.control;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

import java.util.Optional;

public final class StaggerStateData {
    public static final Codec<StaggerStateData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.optionalFieldOf("profile").forGetter(data -> Optional.ofNullable(data.profile)),
            Codec.DOUBLE.optionalFieldOf("buildup", 0.0D).forGetter(StaggerStateData::buildup),
            Codec.INT.optionalFieldOf("decay_delay", 0).forGetter(StaggerStateData::decayDelay),
            Codec.INT.optionalFieldOf("guard_break_ticks", 0).forGetter(StaggerStateData::guardBreakTicks),
            Codec.INT.optionalFieldOf("immunity_ticks", 0).forGetter(StaggerStateData::immunityTicks),
            Codec.INT.optionalFieldOf("pending_immunity_ticks", 0).forGetter(StaggerStateData::pendingImmunityTicks)
    ).apply(instance, (profile, buildup, decayDelay, guardBreakTicks, immunityTicks, pendingImmunityTicks) ->
            new StaggerStateData(
                    profile.orElse(null),
                    buildup,
                    decayDelay,
                    guardBreakTicks,
                    immunityTicks,
                    pendingImmunityTicks
            )));

    private Identifier profile;
    private double buildup;
    private int decayDelay;
    private int guardBreakTicks;
    private int immunityTicks;
    private int pendingImmunityTicks;

    public StaggerStateData() {
        this(null, 0.0D, 0, 0, 0, 0);
    }

    private StaggerStateData(
            Identifier profile,
            double buildup,
            int decayDelay,
            int guardBreakTicks,
            int immunityTicks,
            int pendingImmunityTicks
    ) {
        this.profile = profile;
        this.buildup = Math.max(0.0D, buildup);
        this.decayDelay = Math.max(0, decayDelay);
        this.guardBreakTicks = Math.max(0, guardBreakTicks);
        this.immunityTicks = Math.max(0, immunityTicks);
        this.pendingImmunityTicks = Math.max(0, pendingImmunityTicks);
    }

    public Identifier profile() {
        return profile;
    }

    public double buildup() {
        return buildup;
    }

    public int decayDelay() {
        return decayDelay;
    }

    public int guardBreakTicks() {
        return guardBreakTicks;
    }

    public int immunityTicks() {
        return immunityTicks;
    }

    public int pendingImmunityTicks() {
        return pendingImmunityTicks;
    }

    public void setProfile(Identifier profile) {
        this.profile = profile;
    }

    public void setBuildup(double buildup) {
        this.buildup = Math.max(0.0D, buildup);
    }

    public void setDecayDelay(int decayDelay) {
        this.decayDelay = Math.max(0, decayDelay);
    }

    public void beginGuardBreak(int duration, int immunityDuration) {
        buildup = 0.0D;
        decayDelay = 0;
        guardBreakTicks = Math.max(0, duration);
        immunityTicks = 0;
        pendingImmunityTicks = Math.max(0, immunityDuration);
        if (guardBreakTicks == 0) {
            immunityTicks = pendingImmunityTicks;
            pendingImmunityTicks = 0;
        }
    }

    public void grantImmunity(int duration) {
        buildup = 0.0D;
        decayDelay = 0;
        guardBreakTicks = 0;
        pendingImmunityTicks = 0;
        immunityTicks = Math.max(immunityTicks, Math.max(0, duration));
    }

    public void tickGuardBreak() {
        if (guardBreakTicks <= 0) {
            return;
        }
        guardBreakTicks--;
        if (guardBreakTicks == 0) {
            immunityTicks = Math.max(immunityTicks, pendingImmunityTicks);
            pendingImmunityTicks = 0;
        }
    }

    public void tickImmunity() {
        if (immunityTicks > 0) {
            immunityTicks--;
        }
    }

    public void tickDecayDelay() {
        if (decayDelay > 0) {
            decayDelay--;
        }
    }

    public void clear() {
        profile = null;
        buildup = 0.0D;
        decayDelay = 0;
        guardBreakTicks = 0;
        immunityTicks = 0;
        pendingImmunityTicks = 0;
    }

    public boolean isGuardBroken() {
        return guardBreakTicks > 0;
    }

    public boolean isImmune() {
        return guardBreakTicks > 0 || immunityTicks > 0;
    }

    public boolean isActive() {
        return profile != null || buildup > 0.0D || decayDelay > 0 || guardBreakTicks > 0 || immunityTicks > 0;
    }
}
