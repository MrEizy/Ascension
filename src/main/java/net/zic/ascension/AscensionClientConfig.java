package net.zic.ascension;

import net.neoforged.neoforge.common.ModConfigSpec;

public class AscensionClientConfig {
    public final ModConfigSpec.BooleanValue SHOW_EXACT_HUD_VALUES;

    public AscensionClientConfig(ModConfigSpec.Builder builder) {
        builder.push("HUD");

        SHOW_EXACT_HUD_VALUES = builder
                .comment("Show current and maximum values on the health and qi bars. [Default: true]")
                .define("showExactValues", true);

        builder.pop();
    }
}