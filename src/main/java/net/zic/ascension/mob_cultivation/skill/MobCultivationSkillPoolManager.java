package net.zic.ascension.mob_cultivation.skill;

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
public final class MobCultivationSkillPoolManager {
    private static final Identifier RELOAD_ID = AscensionCraft.prefix("mob_cultivation_skill_pools");
    private static final String DIRECTORY = "mob_cultivation/skill_pools";
    private static volatile Map<Identifier, MobCultivationSkillPool> pools = Map.of();

    private MobCultivationSkillPoolManager() {
    }

    public static MobCultivationSkillPool get(Identifier id) {
        return id == null ? null : pools.get(id);
    }

    public static Set<Identifier> ids() {
        return pools.keySet();
    }

    @SubscribeEvent
    public static void addReloadListener(AddServerReloadListenersEvent event) {
        event.addListener(RELOAD_ID, new ReloadListener());
    }

    private static final class ReloadListener extends SimplePreparableReloadListener<Map<Identifier, MobCultivationSkillPool>> {
        @Override
        protected Map<Identifier, MobCultivationSkillPool> prepare(ResourceManager manager, ProfilerFiller profiler) {
            Map<Identifier, MobCultivationSkillPool> loaded = new LinkedHashMap<>();
            Map<Identifier, Resource> resources = new HashMap<>(manager.listResources(
                    DIRECTORY,
                    id -> id.getPath().endsWith(".json")
            ));
            for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
                try (Reader reader = entry.getValue().openAsReader()) {
                    Identifier id = normalizeId(entry.getKey());
                    JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                    loaded.put(id, MobCultivationSkillPool.parse(id, json));
                } catch (Exception exception) {
                    AscensionCraft.LOGGER.error("Failed to load mob cultivation skill pool {}", entry.getKey(), exception);
                }
            }
            return Map.copyOf(loaded);
        }

        @Override
        protected void apply(Map<Identifier, MobCultivationSkillPool> loaded, ResourceManager manager, ProfilerFiller profiler) {
            pools = loaded;
            AscensionCraft.LOGGER.info("Loaded {} mob cultivation skill pool(s)", loaded.size());
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
