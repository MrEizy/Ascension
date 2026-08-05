package net.zic.ascension;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.zic.ascension.mob_cultivation.MobCultivationConfig;

public class Config {
    public static AscensionCommonConfig COMMON;
    public static AscensionClientConfig CLIENT;
    public static MobCultivationConfig MOB_CULTIVATION;

    public static ModConfigSpec CULTIVATION_SPEC;
    public static ModConfigSpec COMMON_SPEC;
    public static ModConfigSpec CLIENT_SPEC;
    public static ModConfigSpec MOB_CULTIVATION_SPEC;

    static {
        ModConfigSpec.Builder cultivationBuilder = new ModConfigSpec.Builder();
        CULTIVATION_SPEC = cultivationBuilder.build();

        ModConfigSpec.Builder commonBuilder = new ModConfigSpec.Builder();
        COMMON = new AscensionCommonConfig(commonBuilder);
        COMMON_SPEC = commonBuilder.build();


        ModConfigSpec.Builder mobCultivationBuilder = new ModConfigSpec.Builder();
        MOB_CULTIVATION = new MobCultivationConfig(mobCultivationBuilder);
        MOB_CULTIVATION_SPEC = mobCultivationBuilder.build();

        ModConfigSpec.Builder clientBuilder = new ModConfigSpec.Builder();
        CLIENT = new AscensionClientConfig(clientBuilder);
        CLIENT_SPEC = clientBuilder.build();
    }
}