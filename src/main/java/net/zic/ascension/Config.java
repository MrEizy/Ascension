package net.zic.ascension;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    public static AscensionCommonConfig COMMON;
    public static ModConfigSpec CULTIVATION_SPEC;
    public static ModConfigSpec COMMON_SPEC;

    static {
        ModConfigSpec.Builder cultivationBuilder = new ModConfigSpec.Builder();
        CULTIVATION_SPEC = cultivationBuilder.build();

        ModConfigSpec.Builder commonBuilder = new ModConfigSpec.Builder();
        COMMON = new AscensionCommonConfig(commonBuilder);
        COMMON_SPEC = commonBuilder.build();
    }
}