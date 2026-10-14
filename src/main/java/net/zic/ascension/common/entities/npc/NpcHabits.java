package net.zic.ascension.common.entities.npc;

import net.minecraft.util.RandomSource;

import java.util.List;

public record NpcHabits(Period preferredPeriod, int sociability, int discipline) {
    public static NpcHabits roll(RandomSource random, NpcProfile profile, NpcProfile.Personality personality) {
        List<Period> periods = profile.dailyLife().preferredPeriods();
        Period chosen = periods.get(random.nextInt(periods.size()));
        int sociability = Math.clamp(personality.social().interest() + random.nextInt(61) - 30, 25, 175);
        int discipline = Math.clamp(personality.social().focus() + random.nextInt(61) - 30, 25, 175);
        return new NpcHabits(chosen, sociability, discipline);
    }

    public int cultivationCooldown(int ticks, long dayTime) {
        double factor = 100.0 / discipline;
        if (!preferredPeriod.matches(dayTime)) factor *= 1.5;
        return Math.clamp((int) Math.round(ticks * factor), 60, 72000);
    }

    public int socialCooldown(int ticks, long dayTime) {
        double factor = 100.0 / sociability;
        if (!preferredPeriod.matches(dayTime)) factor *= 1.4;
        return Math.clamp((int) Math.round(ticks * factor), 100, 72000);
    }

    public enum Period {
        DAWN,
        DAY,
        DUSK,
        NIGHT;

        public boolean matches(long dayTime) {
            long time = Math.floorMod(dayTime, 24000);
            return switch (this) {
                case DAWN -> time < 6000;
                case DAY -> time >= 6000 && time < 12000;
                case DUSK -> time >= 12000 && time < 14000;
                case NIGHT -> time >= 14000;
            };
        }

        public static Period parse(String id) {
            if (id == null) return null;
            try {
                return valueOf(id.toUpperCase(java.util.Locale.ROOT));
            } catch (IllegalArgumentException ignored) {
                return null;
            }
        }
    }
}
