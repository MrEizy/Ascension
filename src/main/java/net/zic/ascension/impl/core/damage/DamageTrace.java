package net.zic.ascension.impl.core.damage;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.rpg_engine.damage.RPGEngineEntityDamagedEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class DamageTrace {
    private static final boolean ENABLED = Boolean.getBoolean("ascension.damageTrace");
    private static final DamageTrace DISABLED = new DamageTrace();
    private final List<String> lines;

    private DamageTrace() {
        this.lines = null;
    }

    private DamageTrace(boolean enabled) {
        this.lines = enabled ? new ArrayList<>() : null;
    }

    public static DamageTrace begin() {
        return ENABLED ? new DamageTrace(true) : DISABLED;
    }

    public boolean enabled() {
        return lines != null;
    }

    public void value(String name, double value) {
        if (lines == null) {
            return;
        }
        lines.add(formatValue(name, value));
    }

    public void add(String name, double amount) {
        if (lines == null || amount == 0.0D) {
            return;
        }
        lines.add(formatValue(name, amount >= 0.0D ? "+" + formatNumber(amount) : formatNumber(amount)));
    }

    public void multiply(String name, double multiplier) {
        if (lines == null || !Double.isFinite(multiplier) || multiplier == 1.0D) {
            return;
        }
        lines.add(formatValue(name, "x" + formatNumber(multiplier)));
    }

    public void transition(String name, double before, double after) {
        if (lines == null || !Double.isFinite(before) || !Double.isFinite(after) || before == after) {
            return;
        }
        if (before != 0.0D) {
            double multiplier = after / before;
            if (Double.isFinite(multiplier) && multiplier >= 0.0D) {
                multiply(name, multiplier);
                return;
            }
        }
        add(name, after - before);
    }

    public void finish(RPGEngineEntityDamagedEvent.Pre event) {
        if (lines == null) {
            return;
        }
        lines.add(formatValue("Final", event.getDamage()));
        Entity attacker = event.getSource().getEntity();
        LivingEntity target = event.getEntity();
        AscensionCraft.LOGGER.info(
                "Damage trace {} -> {}\n{}",
                attacker == null ? "environment" : attacker.getScoreboardName(),
                target.getScoreboardName(),
                String.join("\n", lines)
        );
    }

    public static void post(RPGEngineEntityDamagedEvent.Post event) {
        if (!ENABLED) return;
        Entity attacker = event.getSource().getEntity();
        LivingEntity target = event.getEntity();
        AscensionCraft.LOGGER.info("Damage post {} -> {}\n{}\n{}",
                attacker == null ? "environment" : attacker.getScoreboardName(), target.getScoreboardName(),
                formatValue("Reported health damage", event.getDamage()),
                formatValue("Current target health", target.getHealth()));
    }

    private static String formatValue(String name, double value) {
        return formatValue(name, formatNumber(value));
    }

    private static String formatValue(String name, String value) {
        return String.format(Locale.ROOT, "%-36s %s", name, value);
    }

    static String formatNumber(double value) {
        double magnitude = Math.abs(value);
        if (magnitude != 0.0D && (magnitude < 0.001D || magnitude >= 1.0E6D)) {
            return String.format(Locale.ROOT, "%.6e", value);
        }
        return String.format(Locale.ROOT, "%.3f", value);
    }
}
