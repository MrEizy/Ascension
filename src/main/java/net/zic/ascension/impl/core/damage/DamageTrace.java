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

    public void value(String name, double value) {
        if (lines == null || !Double.isFinite(value)) {
            return;
        }
        lines.add(formatValue(name, value));
    }

    public void add(String name, double amount) {
        if (lines == null || Math.abs(amount) <= 1.0E-10D) {
            return;
        }
        lines.add(String.format(Locale.ROOT, "%-24s %+.3f", name, amount));
    }

    public void multiply(String name, double multiplier) {
        if (lines == null || !Double.isFinite(multiplier) || Math.abs(multiplier - 1.0D) <= 1.0E-10D) {
            return;
        }
        lines.add(String.format(Locale.ROOT, "%-24s x%.3f", name, multiplier));
    }

    public void transition(String name, double before, double after) {
        if (lines == null || !Double.isFinite(before) || !Double.isFinite(after) || Math.abs(before - after) <= 1.0E-10D) {
            return;
        }
        if (Math.abs(before) > 1.0E-10D) {
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

    private static String formatValue(String name, double value) {
        return String.format(Locale.ROOT, "%-24s %.3f", name, value);
    }
}
