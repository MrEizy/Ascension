package net.zic.ascension.configuration.mobs;

import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.monster.Enemy;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.util.ModTags;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class MobFallbackConfigurations {
    private static final Identifier RESOURCE = AscensionCraft.prefix("ascension/config/mob_traits/fallback.json");
    private static volatile Profiles profiles = Profiles.DISABLED;

    public record Profiles(boolean enabled, MobConfiguration passive, MobConfiguration hostile, MobConfiguration boss) {
        private static final Profiles DISABLED = new Profiles(false, null, null, null);
        public static final Codec<Profiles> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.BOOL.optionalFieldOf("enabled", true).forGetter(Profiles::enabled),
                MobConfiguration.CODEC.fieldOf("passive").forGetter(Profiles::passive),
                MobConfiguration.CODEC.fieldOf("hostile").forGetter(Profiles::hostile),
                MobConfiguration.CODEC.fieldOf("boss").forGetter(Profiles::boss)
        ).apply(instance, Profiles::new));
    }

    private MobFallbackConfigurations() {
    }

    @SubscribeEvent
    public static void registerReloadListener(AddServerReloadListenersEvent event) {
        event.addListener(AscensionCraft.prefix("mob_cultivation_fallback"),
                (ResourceManagerReloadListener) MobFallbackConfigurations::reload);
    }

    private static void reload(ResourceManager manager) {
        Profiles loaded = Profiles.DISABLED;
        try {
            var resource = manager.getResource(RESOURCE);
            if (resource.isPresent()) {
                try (var reader = new InputStreamReader(resource.get().open(), StandardCharsets.UTF_8)) {
                    loaded = Profiles.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(reader)).getOrThrow();
                }
            } else {
                AscensionCraft.LOGGER.warn("Missing mob cultivation fallback configuration {}", RESOURCE);
            }
        } catch (Exception exception) {
            AscensionCraft.LOGGER.error("Unable to load mob cultivation fallback configuration {}", RESOURCE, exception);
        }
        profiles = loaded;
    }

    public static MobConfiguration get(Mob mob) {
        Profiles current = profiles;
        if (!current.enabled() || mob.getType().builtInRegistryHolder().is(ModTags.EntityTypes.MOB_CULTIVATION_EXCLUDED)) return null;
        if (mob.getType().builtInRegistryHolder().is(ModTags.EntityTypes.MOB_CULTIVATION_BOSSES)) return current.boss();
        if (mob.getType().builtInRegistryHolder().is(ModTags.EntityTypes.MOB_CULTIVATION_HOSTILE)) return current.hostile();
        if (mob.getType().builtInRegistryHolder().is(ModTags.EntityTypes.MOB_CULTIVATION_PASSIVE)) return current.passive();
        if (mob instanceof Enemy || mob.getType().getCategory() == MobCategory.MONSTER) return current.hostile();
        return current.passive();
    }
}
