package net.zic.ascension.worldgen.dimension;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.DimensionType;
import net.zic.ascension.AscensionCraft;

public final class AscDimensionTypes {

    public static final ResourceKey<DimensionType> OVERWORLD = ResourceKey.create(
            Registries.DIMENSION_TYPE,
            AscensionCraft.prefix("overworld")
    );

    private AscDimensionTypes() {
    }

    public static void bootstrap(BootstrapContext<DimensionType> context) {
        context.register(OVERWORLD, createOverworld());
    }

    private static DimensionType createOverworld() {
        JsonObject json = new JsonObject();

        json.addProperty("has_fixed_time", false);
        json.addProperty("has_skylight", true);
        json.addProperty("has_ceiling", false);
        json.addProperty("coordinate_scale", 1.0D);

        json.addProperty("min_y", -64);
        json.addProperty("height", 480);
        json.addProperty("logical_height", 480);

        json.addProperty(
                "infiniburn",
                "#minecraft:infiniburn_overworld"
        );

        json.addProperty("skybox", "overworld");
        json.addProperty("cardinal_light_type", "default");
        json.addProperty("ambient_light", 0.0F);

        json.addProperty(
                "monster_spawn_block_light_limit",
                0
        );

        JsonObject monsterSpawnLightLevel = new JsonObject();
        monsterSpawnLightLevel.addProperty(
                "type",
                "minecraft:uniform"
        );
        monsterSpawnLightLevel.addProperty(
                "min_inclusive",
                0
        );
        monsterSpawnLightLevel.addProperty(
                "max_inclusive",
                7
        );

        json.add(
                "monster_spawn_light_level",
                monsterSpawnLightLevel
        );

        json.addProperty(
                "has_ender_dragon_fight",
                false
        );

        return DimensionType.DIRECT_CODEC
                .parse(JsonOps.INSTANCE, json)
                .getOrThrow();
    }
}