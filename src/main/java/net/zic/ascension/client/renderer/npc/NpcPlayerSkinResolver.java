package net.zic.ascension.client.renderer.npc;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.client.innerworld.InnerWorldSkinCache;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public final class NpcPlayerSkinResolver {
    private static ResourceManager lastResources;
    private static Map<String, UUID> configured = Map.of();

    private NpcPlayerSkinResolver() { }

    public static Identifier resolve(Component customName) {
        if (customName == null) return null;
        String name = customName.getString().trim();
        if (!name.matches("[A-Za-z0-9_]{3,16}")) return null;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            for (var player : mc.level.players()) {
                if (player instanceof AbstractClientPlayer client && client.getName().getString().equalsIgnoreCase(name)) {
                    return client.getSkin().body().texturePath();
                }
            }
        }
        loadMappings(mc.getResourceManager());
        UUID id = configured.get(name.toLowerCase(Locale.ROOT));
        if (id == null) return null;
        return InnerWorldSkinCache.get(id, name).body().texturePath();
    }

    private static void loadMappings(ResourceManager resources) {
        if (resources == lastResources) return;
        lastResources = resources;
        Map<String, UUID> mappings = new HashMap<>();
        resources.getResource(AscensionCraft.prefix("npc_player_skins.json")).ifPresent(resource -> {
            try (var reader = new InputStreamReader(resource.open(), StandardCharsets.UTF_8)) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                for (var entry : json.entrySet()) {
                    try {
                        String name = entry.getKey().toLowerCase(Locale.ROOT);
                        if (name.matches("[a-z0-9_]{3,16}")) {
                            mappings.put(name, UUID.fromString(entry.getValue().getAsString()));
                        }
                    } catch (IllegalArgumentException ignored) { }
                }
            } catch (Exception e) {
                AscensionCraft.LOGGER.warn("Unable to read NPC player skin mappings", e);
            }
        });
        configured = Map.copyOf(mappings);
    }
}
