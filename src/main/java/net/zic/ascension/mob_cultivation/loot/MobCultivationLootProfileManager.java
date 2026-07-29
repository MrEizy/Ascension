package net.zic.ascension.mob_cultivation.loot;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.zic.ascension.AscensionCraft;

import java.io.Reader;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class MobCultivationLootProfileManager {
    private static final Identifier RELOAD_ID = AscensionCraft.prefix("mob_cultivation_loot_profiles");
    private static final String DIRECTORY = "mob_cultivation/loot_profiles";
    private static volatile Map<Identifier, MobCultivationLootProfile> profiles = Map.of();

    private MobCultivationLootProfileManager() {
    }

    public static MobCultivationLootProfile get(Identifier id) {
        return id == null ? null : profiles.get(id);
    }

    public static Set<Identifier> ids() {
        return profiles.keySet();
    }

    @SubscribeEvent
    public static void addReloadListener(AddServerReloadListenersEvent event) {
        event.addListener(RELOAD_ID, new ReloadListener());
    }

    private static final class ReloadListener extends SimplePreparableReloadListener<Map<Identifier, MobCultivationLootProfile>> {
        @Override
        protected Map<Identifier, MobCultivationLootProfile> prepare(ResourceManager manager, ProfilerFiller profiler) {
            Map<Identifier, MobCultivationLootProfile> loaded = new LinkedHashMap<>();
            Map<Identifier, Resource> resources = new HashMap<>(manager.listResources(
                    DIRECTORY,
                    id -> id.getPath().endsWith(".json")
            ));
            for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
                try (Reader reader = entry.getValue().openAsReader()) {
                    Identifier id = normalizeId(entry.getKey());
                    JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                    loaded.put(id, MobCultivationLootProfile.parse(id, json));
                } catch (Exception exception) {
                    AscensionCraft.LOGGER.error("Failed to load mob cultivation loot profile {}", entry.getKey(), exception);
                }
            }
            return Map.copyOf(loaded);
        }

        @Override
        protected void apply(Map<Identifier, MobCultivationLootProfile> loaded, ResourceManager manager, ProfilerFiller profiler) {
            profiles = loaded;
            AscensionCraft.LOGGER.info("Loaded {} mob cultivation loot profile(s)", loaded.size());
        }

        private static Identifier normalizeId(Identifier resourceId) {
            String path = resourceId.getPath();
            String prefix = DIRECTORY + "/";
            if (path.startsWith(prefix)) path = path.substring(prefix.length());
            if (path.endsWith(".json")) path = path.substring(0, path.length() - 5);
            return Identifier.fromNamespaceAndPath(resourceId.getNamespace(), path);
        }
    }
}
