package net.zic.ascension.configuration.mobs;

import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.zic.ascension.AscensionCraft;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class MobSkillCastingConfigurations {
    private static final Identifier RESOURCE = AscensionCraft.prefix("ascension/config/mob_configurations/skill_casting.json");
    private static volatile Settings settings = Settings.DISABLED;

    public record Rule(String behavior, double minimumDistance, double maximumDistance,
                       double healthBelow, int weight, boolean requireLineOfSight) {
        public static final Codec<Rule> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("behavior").forGetter(Rule::behavior),
                Codec.DOUBLE.optionalFieldOf("minimum_distance", 0.0D).forGetter(Rule::minimumDistance),
                Codec.DOUBLE.optionalFieldOf("maximum_distance", 12.0D).forGetter(Rule::maximumDistance),
                Codec.DOUBLE.optionalFieldOf("health_below", 1.0D).forGetter(Rule::healthBelow),
                Codec.intRange(1, 10000).optionalFieldOf("weight", 1).forGetter(Rule::weight),
                Codec.BOOL.optionalFieldOf("require_line_of_sight", true).forGetter(Rule::requireLineOfSight)
        ).apply(instance, Rule::new));
    }

    public record Settings(boolean enabled, int interval, double attemptChance, Map<Identifier, Rule> skills) {
        private static final Settings DISABLED = new Settings(false, 40, 0.0D, Map.of());
        public static final Codec<Settings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.BOOL.optionalFieldOf("enabled", true).forGetter(Settings::enabled),
                Codec.intRange(5, 1200).optionalFieldOf("interval_ticks", 40).forGetter(Settings::interval),
                Codec.doubleRange(0.0D, 1.0D).optionalFieldOf("attempt_chance", 0.5D).forGetter(Settings::attemptChance),
                Codec.unboundedMap(Identifier.CODEC, Rule.CODEC).fieldOf("skills").forGetter(Settings::skills)
        ).apply(instance, Settings::new));
    }

    private MobSkillCastingConfigurations() {
    }

    public static Settings current() {
        return settings;
    }

    @SubscribeEvent
    public static void registerReloadListener(AddServerReloadListenersEvent event) {
        event.addListener(AscensionCraft.prefix("mob_skill_casting"),
                (ResourceManagerReloadListener) MobSkillCastingConfigurations::reload);
    }

    private static void reload(ResourceManager manager) {
        Settings loaded = Settings.DISABLED;
        try {
            var resource = manager.getResource(RESOURCE);
            if (resource.isPresent()) {
                try (var reader = new InputStreamReader(resource.get().open(), StandardCharsets.UTF_8)) {
                    loaded = Settings.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(reader)).getOrThrow();
                }
            } else {
                AscensionCraft.LOGGER.warn("Missing mob skill casting configuration {}", RESOURCE);
            }
        } catch (Exception exception) {
            AscensionCraft.LOGGER.error("Unable to load mob skill casting configuration {}", RESOURCE, exception);
        }
        settings = loaded;
    }
}
