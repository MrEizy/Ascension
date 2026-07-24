package net.zic.ascension.impl.core.skill.toggleable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record QiUpkeep(
        double flat,
        double maxQiFraction,
        int interval
) {
    public static final Codec<QiUpkeep> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.DOUBLE.optionalFieldOf("flat", 0.0D).forGetter(QiUpkeep::flat),
                    Codec.DOUBLE.optionalFieldOf("max_qi_fraction", 0.0D).forGetter(QiUpkeep::maxQiFraction),
                    Codec.INT.optionalFieldOf("interval", 20).forGetter(QiUpkeep::interval)
            ).apply(instance, QiUpkeep::new)
    );

    public QiUpkeep {
        flat = Math.max(0.0D, flat);
        maxQiFraction = Math.max(0.0D, maxQiFraction);
        interval = Math.max(1, interval);
    }

    public double cost(double maxQi) {
        return flat + Math.max(0.0D, maxQi) * maxQiFraction;
    }
}
