package net.zic.ascension.mob_cultivation.profile;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.mob_cultivation.MobCultivationCategory;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class MobCultivationProfileManager {
    private static final Identifier RELOAD_ID = AscensionCraft.prefix("mob_cultivation_profiles");
    private static final String DIRECTORY = "mob_cultivation/profiles";

    private static volatile List<MobCultivationProfile> profiles = List.of();
    private static volatile long revision;
    private static final Map<CacheKey, ResolvedMobCultivationProfile> CACHE = new ConcurrentHashMap<>();

    private MobCultivationProfileManager() {
    }

    public static long revision() {
        return revision;
    }

    public static ResolvedMobCultivationProfile resolve(Mob mob, MobCultivationCategory category) {
        Identifier entityId = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType());
        CacheKey key = new CacheKey(entityId, category);
        return CACHE.computeIfAbsent(key, ignored -> resolveUncached(mob, entityId, category));
    }

    private static ResolvedMobCultivationProfile resolveUncached(
            Mob mob,
            Identifier entityId,
            MobCultivationCategory category
    ) {
        ResolvedMobCultivationProfile.Builder builder = new ResolvedMobCultivationProfile.Builder(category);
        for (MobCultivationProfile profile : profiles) {
            if (profile.target().matches(mob, entityId, category)) {
                builder.apply(profile);
            }
        }
        return builder.build();
    }

    @SubscribeEvent
    public static void addReloadListener(AddServerReloadListenersEvent event) {
        event.addListener(RELOAD_ID, new ReloadListener());
    }

    private record CacheKey(Identifier entityId, MobCultivationCategory category) {
    }

    private static final class ReloadListener extends SimplePreparableReloadListener<List<MobCultivationProfile>> {
        @Override
        protected List<MobCultivationProfile> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
            List<MobCultivationProfile> loaded = new ArrayList<>();
            Map<Identifier, Resource> resources = new HashMap<>(resourceManager.listResources(
                    DIRECTORY,
                    id -> id.getPath().endsWith(".json")
            ));

            for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
                try (Reader reader = entry.getValue().openAsReader()) {
                    JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                    loaded.add(MobCultivationProfile.parse(entry.getKey(), json));
                } catch (Exception exception) {
                    AscensionCraft.LOGGER.error(
                            "Failed to load mob cultivation profile {}",
                            entry.getKey(),
                            exception
                    );
                }
            }

            loaded.sort(Comparator
                    .comparingInt((MobCultivationProfile profile) -> profile.target().type().specificity())
                    .thenComparingInt(MobCultivationProfile::priority)
                    .thenComparing(profile -> profile.id().toString()));
            return List.copyOf(loaded);
        }

        @Override
        protected void apply(
                List<MobCultivationProfile> loaded,
                ResourceManager resourceManager,
                ProfilerFiller profiler
        ) {
            profiles = loaded;
            revision++;
            CACHE.clear();
            AscensionCraft.LOGGER.info("Loaded {} mob cultivation profile(s)", loaded.size());
        }
    }
}
