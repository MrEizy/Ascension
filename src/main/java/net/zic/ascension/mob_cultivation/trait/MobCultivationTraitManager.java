package net.zic.ascension.mob_cultivation.trait;

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
public final class MobCultivationTraitManager {
    private static final Identifier RELOAD_ID = AscensionCraft.prefix("mob_cultivation_traits");
    private static final String DIRECTORY = "mob_cultivation/traits";
    private static volatile Map<Identifier, MobCultivationTraitDefinition> definitions = Map.of();

    private MobCultivationTraitManager() {
    }

    public static MobCultivationTraitDefinition get(Identifier id) {
        return id == null ? null : definitions.get(id);
    }

    public static Set<Identifier> ids() {
        return definitions.keySet();
    }

    @SubscribeEvent
    public static void addReloadListener(AddServerReloadListenersEvent event) {
        event.addListener(RELOAD_ID, new ReloadListener());
    }

    private static final class ReloadListener extends SimplePreparableReloadListener<Map<Identifier, MobCultivationTraitDefinition>> {
        @Override
        protected Map<Identifier, MobCultivationTraitDefinition> prepare(
                ResourceManager resourceManager,
                ProfilerFiller profiler
        ) {
            Map<Identifier, MobCultivationTraitDefinition> loaded = new LinkedHashMap<>();
            Map<Identifier, Resource> resources = new HashMap<>(resourceManager.listResources(
                    DIRECTORY,
                    id -> id.getPath().endsWith(".json")
            ));

            for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
                try (Reader reader = entry.getValue().openAsReader()) {
                    JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                    loaded.put(normalizeId(entry.getKey()), MobCultivationTraitDefinition.parse(normalizeId(entry.getKey()), json));
                } catch (Exception exception) {
                    AscensionCraft.LOGGER.error("Failed to load mob cultivation trait {}", entry.getKey(), exception);
                }
            }
            return Map.copyOf(loaded);
        }

        @Override
        protected void apply(
                Map<Identifier, MobCultivationTraitDefinition> loaded,
                ResourceManager resourceManager,
                ProfilerFiller profiler
        ) {
            definitions = loaded;
            AscensionCraft.LOGGER.info("Loaded {} mob cultivation trait definition(s)", loaded.size());
        }

        private static Identifier normalizeId(Identifier resourceId) {
            String path = resourceId.getPath();
            String prefix = DIRECTORY + "/";
            if (path.startsWith(prefix)) {
                path = path.substring(prefix.length());
            }
            if (path.endsWith(".json")) {
                path = path.substring(0, path.length() - 5);
            }
            return Identifier.fromNamespaceAndPath(resourceId.getNamespace(), path);
        }
    }
}
