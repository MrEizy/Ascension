package net.zic.ascension.impl.core.progression;

import net.minecraft.resources.Identifier;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.zenithlib.stats.Stat;
import net.zic.zenithlib.stats.ZenithStatHelper;
import net.zic.zenithlib.value_containers.typed.Modifier;

final class ScaledStatModifiers {
    private ScaledStatModifiers() {}

    static Identifier id(String kind, Identifier context, Identifier action, Identifier stat, String suffix) {
        return Identifier.fromNamespaceAndPath("ascension", "scaled_stats/" + kind + "/"
                + context.getNamespace() + "/" + context.getPath() + "/"
                + action.getNamespace() + "/" + action.getPath() + "/"
                + stat.getNamespace() + "/" + stat.getPath() + "/" + suffix);
    }

    static void setFlat(OriginSource source, Identifier statId, Identifier id, double value) {
        Stat stat = ZenithStatHelper.stat(statId);
        if (stat == null) throw new IllegalArgumentException("Unknown scaled stat: " + statId);
        if (!Double.isFinite(value)) throw new IllegalArgumentException("Non-finite scaled stat: " + id);
        source.removeStatModifier(stat, id);
        if (value != 0) source.addFlatStatModifier(stat, Modifier.flat(id, 0, value));
    }

    static void setMultiplier(OriginSource source, Identifier statId, Identifier id, Identifier group, double value) {
        Stat stat = ZenithStatHelper.stat(statId);
        if (stat == null) throw new IllegalArgumentException("Unknown scaled stat: " + statId);
        if (!Double.isFinite(value)) throw new IllegalArgumentException("Non-finite scaled stat: " + id);
        source.removeStatModifier(stat, id);
        if (value != 0) source.addMultiplierStatModifier(stat, Modifier.multiplier(id, group, 0, value));
    }
}
