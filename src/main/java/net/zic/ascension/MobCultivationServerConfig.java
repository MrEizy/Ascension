package net.zic.ascension;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class MobCultivationServerConfig {
    public final ModConfigSpec.BooleanValue ENABLED;
    public final ModConfigSpec.DoubleValue EFFECTIVENESS;
    public final ModConfigSpec.DoubleValue BASE_PROGRESS_PER_SECOND;
    public final ModConfigSpec.IntValue UPDATE_INTERVAL;

    public MobCultivationServerConfig(ModConfigSpec.Builder builder) {
        builder.push("mob_cultivation");
        ENABLED = builder.define("enabled", true);
        EFFECTIVENESS = builder.defineInRange("effectiveness", 1.0D, 0.0D, 1000.0D);
        BASE_PROGRESS_PER_SECOND = builder.defineInRange("base_progress_per_second", 12.0D, 0.0D, 1000000.0D);
        UPDATE_INTERVAL = builder.defineInRange("update_interval_ticks", 100, 20, 1200);
        builder.pop();
    }
}
