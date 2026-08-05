package net.zic.ascension.mob_cultivation;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class MobCultivationConfig {
    public final ModConfigSpec.BooleanValue SKILLS_ENABLED;
    public final ModConfigSpec.IntValue SKILL_DECISION_INTERVAL;

    public final ModConfigSpec.BooleanValue DISTANCE_GROWTH_ENABLED;
    public final ModConfigSpec.BooleanValue DISTANCE_GROWTH_OTHER_DIMENSIONS;
    public final ModConfigSpec.DoubleValue DISTANCE_GROWTH_START;
    public final ModConfigSpec.DoubleValue DISTANCE_BLOCKS_PER_MULTIPLIER;
    public final ModConfigSpec.DoubleValue MAXIMUM_DISTANCE_GROWTH_MULTIPLIER;

    public MobCultivationConfig(ModConfigSpec.Builder builder) {
        builder.push("skills");

        SKILLS_ENABLED = builder
                .comment("Whether cultivated mobs may automatically cast skills.")
                .define("enabled", true);

        SKILL_DECISION_INTERVAL = builder
                .comment("How often each cultivated mob considers casting a skill, in ticks.")
                .defineInRange("decision_interval", 20, 5, 1200);

        builder.pop();
        builder.push("distance_growth");

        DISTANCE_GROWTH_ENABLED = builder
                .comment("Whether cultivation growth increases with horizontal distance from world spawn.")
                .define("enabled", true);

        DISTANCE_GROWTH_OTHER_DIMENSIONS = builder
                .comment("Whether the distance bonus also applies outside the Overworld. Other dimensions measure horizontal distance from coordinate 0, 0.")
                .define("apply_in_other_dimensions", true);

        DISTANCE_GROWTH_START = builder
                .comment("Horizontal distance from the dimension's cultivation origin before the growth bonus begins.")
                .defineInRange("start_distance", 256.0D, 0.0D, 30_000_000.0D);

        DISTANCE_BLOCKS_PER_MULTIPLIER = builder
                .comment(
                        "Blocks beyond the start distance required to add another +1.0x growth.",
                        "For example, 2000 means 2256 blocks from spawn gives 2.0x growth with the default start distance."
                )
                .defineInRange("blocks_per_multiplier", 2000.0D, 1.0D, 30_000_000.0D);

        MAXIMUM_DISTANCE_GROWTH_MULTIPLIER = builder
                .comment("Maximum total multiplier supplied by distance from spawn.")
                .defineInRange("maximum_multiplier", 5.0D, 1.0D, 10000.0D);

        builder.pop();
    }
}
