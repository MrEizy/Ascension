package net.zic.ascension;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class AlchemyServerConfig {
    public final ModConfigSpec.DoubleValue DECAY_SPEED;
    public final ModConfigSpec.DoubleValue BASE_LIFESPAN_DAYS;
    public final ModConfigSpec.DoubleValue JADE_BOTTLE_RATE;
    public final ModConfigSpec.DoubleValue IMPURITY_DECAY_PER_DAY;
    public final ModConfigSpec.DoubleValue IMPURITY_GAIN_MULTIPLIER;
    public final ModConfigSpec.IntValue ORDINARY_PILL_MAX_REALM;
    public final ModConfigSpec.IntValue PROFOUND_PILL_MAX_REALM;
    public final ModConfigSpec.IntValue HEAVEN_PILL_MAX_REALM;
    public final ModConfigSpec.IntValue SAINT_PILL_MAX_REALM;
    public final ModConfigSpec.IntValue GOD_PILL_MAX_REALM;
    public final ModConfigSpec.IntValue HEAVENS_PATH_PILL_MAX_REALM;
    public final ModConfigSpec.DoubleValue CULTIVATION_PILL_REALM_MULTIPLIER;
    public final ModConfigSpec.DoubleValue CULTIVATION_PILL_MINIMUM_EFFECTIVENESS;

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
        builder.push("cultivation_pills");
        ORDINARY_PILL_MAX_REALM = builder.comment("Highest zero-indexed major realm where an Ordinary Rank pill gives its full cultivation bonus.")
                .defineInRange("ordinary_max_realm", 1, 0, 1000);
        PROFOUND_PILL_MAX_REALM = builder.comment("Highest zero-indexed major realm where a Profound Rank pill gives its full cultivation bonus.")
                .defineInRange("profound_max_realm", 3, 0, 1000);
        HEAVEN_PILL_MAX_REALM = builder.comment("Highest zero-indexed major realm where a Heaven Rank pill gives its full cultivation bonus.")
                .defineInRange("heaven_max_realm", 5, 0, 1000);
        SAINT_PILL_MAX_REALM = builder.comment("Highest zero-indexed major realm where a Saint Rank pill gives its full cultivation bonus.")
                .defineInRange("saint_max_realm", 7, 0, 1000);
        GOD_PILL_MAX_REALM = builder.comment("Highest zero-indexed major realm where a God Rank pill gives its full cultivation bonus.")
                .defineInRange("god_max_realm", 9, 0, 1000);
        HEAVENS_PATH_PILL_MAX_REALM = builder.comment("Highest zero-indexed major realm where a Heaven's Path Rank pill gives its full cultivation bonus.")
                .defineInRange("heavens_path_max_realm", 11, 0, 1000);
        CULTIVATION_PILL_REALM_MULTIPLIER = builder.comment("Multiplies cultivation gains once for each major realm above the pill's maximum effective realm.")
                .defineInRange("realm_multiplier", 0.15D, 0.0D, 1.0D);
        CULTIVATION_PILL_MINIMUM_EFFECTIVENESS = builder.comment("Minimum fraction of cultivation progress retained after realm penalties.")
                .defineInRange("minimum_effectiveness", 0.0D, 0.0D, 1.0D);
        builder.pop();
    }
}
