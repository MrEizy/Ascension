package net.zic.ascension.impl.core.innerworld;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import net.zic.ascension.AscensionCraft;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Persistent plot registry, stored as JSON in the world folder (ascension_inner_worlds.json) so it
 * doesn't depend on the SavedData API. Server thread only.
 */
public final class InnerWorldPlots {
    private InnerWorldPlots() {
    }

    public static final class Origin {
        public String dimension = "minecraft:overworld";
        public double x;
        public double y;
        public double z;
        public float yaw;
        public float pitch;

        public Origin() {
        }

        public Origin(String dimension, double x, double y, double z, float yaw, float pitch) {
            this.dimension = dimension;
            this.x = x;
            this.y = y;
            this.z = z;
            this.yaw = yaw;
            this.pitch = pitch;
        }
    }

    public static final class Session {
        public Origin origin;
        public String ghost = "";
    }

    public static final class Plot {
        public int index;
        public int tier;
        /** Largest fully generated ring (in chunks); the invisible border sits here. -1 = nothing yet. */
        public int generatedRadius = -1;
        public Session session;
        /** Set when the ghost died while the owner was offline; applied on next login. */
        public Origin pendingReturn;
    }

    private static final class Data {
        int next;
        Map<String, Plot> players = new LinkedHashMap<>();
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Data data = new Data();
    private static Path file;
    private static boolean loaded;

    public static void load(MinecraftServer server) {
        file = server.getWorldPath(LevelResource.ROOT).resolve("ascension_inner_worlds.json");
        data = new Data();
        if (Files.exists(file)) {
            try (var reader = Files.newBufferedReader(file)) {
                Data parsed = GSON.fromJson(reader, Data.class);
                if (parsed != null) {
                    data = parsed;
                    if (data.players == null) {
                        data.players = new LinkedHashMap<>();
                    }
                }
            } catch (IOException | JsonParseException e) {
                AscensionCraft.LOGGER.error("Cannot read inner world plots; starting empty", e);
            }
        }
        loaded = true;
    }

    public static void save() {
        if (!loaded || file == null) {
            return;
        }
        try (var writer = Files.newBufferedWriter(file)) {
            GSON.toJson(data, writer);
        } catch (IOException e) {
            AscensionCraft.LOGGER.error("Cannot save inner world plots", e);
        }
    }

    public static void unload() {
        save();
        loaded = false;
        data = new Data();
        file = null;
    }

    public static boolean isLoaded() {
        return loaded;
    }

    public static Plot get(UUID player) {
        return data.players.get(player.toString());
    }

    public static Plot getOrCreate(UUID player) {
        Plot existing = data.players.get(player.toString());
        if (existing != null) {
            return existing;
        }
        Plot plot = new Plot();
        plot.index = data.next++;
        data.players.put(player.toString(), plot);
        save();
        return plot;
    }

    public static Map<String, Plot> entries() {
        return Collections.unmodifiableMap(data.players);
    }
}