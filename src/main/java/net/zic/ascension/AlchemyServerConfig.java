package net.zic.ascension;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class AlchemyServerConfig {
    public final ModConfigSpec.DoubleValue DECAY_SPEED;
    public final ModConfigSpec.DoubleValue BASE_LIFESPAN_DAYS;
    public final ModConfigSpec.DoubleValue JADE_BOTTLE_RATE;
    public final ModConfigSpec.DoubleValue IMPURITY_DECAY_PER_DAY;
    public final ModConfigSpec.DoubleValue IMPURITY_GAIN_MULTIPLIER;

    public AlchemyServerConfig(ModConfigSpec.Builder builder) {
        builder.push("medicinal_decay");
        DECAY_SPEED = builder.comment("Effective aging multiplier. 0 pauses all medicinal decay; 1 is normal speed.")
                .defineInRange("decay_speed", 1.0D, 0.0D, 100.0D);
        BASE_LIFESPAN_DAYS = builder.comment("Baseline medicinal lifespan in 24000-tick Minecraft days. Stability modifies this per pill.")
                .defineInRange("base_lifespan_days", 7.0D, 0.01D, 365.0D);
        JADE_BOTTLE_RATE = builder.comment("Effective aging rate inside jade pill bottles. 0 pauses aging, 0.05 is 20x slower.")
                .defineInRange("jade_bottle_rate", 0.05D, 0.0D, 1.0D);
        builder.pop();
        builder.push("medicinal_impurities");
        IMPURITY_DECAY_PER_DAY = builder.comment("Impurity percentage points naturally purified per 24000 server ticks. Zero disables natural purification.")
                .defineInRange("decay_per_day", 2.0D, 0.0D, 100.0D);
        IMPURITY_GAIN_MULTIPLIER = builder.comment("Multiplier applied to impurities absorbed from herbs and pills.")
                .defineInRange("gain_multiplier", 1.0D, 0.0D, 20.0D);
        builder.pop();
    }
}
