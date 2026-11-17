package net.zic.ascension.client.appearance;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.zic.ascension.AscensionCraft;
import org.jspecify.annotations.Nullable;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID, value = Dist.CLIENT)
public final class PlayerAppearanceModels {
    private static ResourceManager cachedManager;
    private static final Map<Identifier, Optional<Appearance>> MODELS = new HashMap<>();

    private PlayerAppearanceModels() {
    }

    @SubscribeEvent
    public static void onAddReloadListeners(AddClientReloadListenersEvent event) {
        event.addListener(AscensionCraft.prefix("player_appearances"), (ResourceManagerReloadListener) manager -> {
            MODELS.clear();
            PlayerAppearanceGeoRenderer.clear();
            cachedManager = manager;
        });
    }

    public static @Nullable Appearance get(Identifier id) {
        ResourceManager manager = Minecraft.getInstance().getResourceManager();
        if (manager != cachedManager) {
            MODELS.clear();
            PlayerAppearanceGeoRenderer.clear();
            cachedManager = manager;
        }
        return MODELS.computeIfAbsent(id, key -> Optional.ofNullable(load(manager, key))).orElse(null);
    }

    private static @Nullable Appearance load(ResourceManager manager, Identifier id) {
        Identifier path = Identifier.fromNamespaceAndPath(id.getNamespace(), "player_appearances/" + id.getPath() + ".json");
        try {
            var resource = manager.getResource(path);
            if (resource.isEmpty()) {
                AscensionCraft.LOGGER.warn("Missing player appearance model {}", path);
                return null;
            }
            try (var reader = new InputStreamReader(resource.get().open(), StandardCharsets.UTF_8)) {
                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                Identifier texture = Identifier.parse(root.get("texture").getAsString());
                if (root.has("renderer") && root.get("renderer").getAsString().equals("geckolib")) {
                    return loadGecko(root, texture);
                }
                if (root.has("renderer") && !root.get("renderer").getAsString().equals("cuboids")) {
                    throw new IllegalArgumentException("Unknown appearance renderer " + root.get("renderer").getAsString());
                }
                JsonArray dimensions = root.getAsJsonArray("texture_size");
                int width = dimensions == null ? 64 : dimensions.get(0).getAsInt();
                int height = dimensions == null ? 64 : dimensions.get(1).getAsInt();
                if (width < 1 || height < 1 || width > 1024 || height > 1024) {
                    throw new IllegalArgumentException("Invalid texture dimensions");
                }
                JsonArray parts = root.getAsJsonArray("parts");
                if (parts == null || parts.size() == 0 || parts.size() > 48) {
                    throw new IllegalArgumentException("Expected 1 to 48 model parts");
                }
                List<Attachment> attachments = new ArrayList<>();
                for (var entry : parts) {
                    JsonObject part = entry.getAsJsonObject();
                    String anchor = part.get("anchor").getAsString();
                    if (!List.of("head", "body", "left_arm", "right_arm", "left_leg", "right_leg").contains(anchor)) {
                        throw new IllegalArgumentException("Unknown model anchor " + anchor);
                    }
                    float[] offset = vector(part, "offset", new float[]{0, 0, 0});
                    float[] rotation = vector(part, "rotation", new float[]{0, 0, 0});
                    JsonObject sway = part.has("sway") ? part.getAsJsonObject("sway") : null;
                    String axis = sway == null ? "y" : sway.get("axis").getAsString();
                    float degrees = sway == null ? 0 : sway.get("degrees").getAsFloat();
                    float speed = sway == null ? 0 : sway.get("speed").getAsFloat();
                    if (!List.of("x", "y", "z").contains(axis)) {
                        throw new IllegalArgumentException("Unknown sway axis " + axis);
                    }
                    JsonArray cubes = part.getAsJsonArray("cubes");
                    if (cubes == null || cubes.size() == 0 || cubes.size() > 64) {
                        throw new IllegalArgumentException("Expected 1 to 64 cubes per part");
                    }
                    CubeListBuilder builder = CubeListBuilder.create();
                    for (var cubeEntry : cubes) {
                        JsonObject cube = cubeEntry.getAsJsonObject();
                        float[] origin = vector(cube, "origin", null);
                        float[] size = vector(cube, "size", null);
                        JsonArray uv = cube.getAsJsonArray("uv");
                        if (size[0] <= 0 || size[1] <= 0 || size[2] <= 0 || uv == null || uv.size() != 2) {
                            throw new IllegalArgumentException("Invalid cube dimensions or UV");
                        }
                        builder.texOffs(uv.get(0).getAsInt(), uv.get(1).getAsInt())
                                .addBox(origin[0], origin[1], origin[2], size[0], size[1], size[2]);
                    }
                    MeshDefinition mesh = new MeshDefinition();
                    mesh.getRoot().addOrReplaceChild("geometry", builder, PartPose.ZERO);
                    ModelPart geometry = LayerDefinition.create(mesh, width, height).bakeRoot().getChild("geometry");
                    attachments.add(new Attachment(anchor, offset, rotation, axis, degrees, speed, geometry));
                }
                return new Appearance(texture, List.copyOf(attachments), List.of());
            }
        } catch (Exception exception) {
            AscensionCraft.LOGGER.warn("Unable to load player appearance model {}", path, exception);
            return null;
        }
    }

    private static Appearance loadGecko(JsonObject root, Identifier texture) {
        Identifier model = Identifier.parse(root.get("model").getAsString());
        Identifier animation = root.has("animation")
                ? Identifier.parse(root.get("animation").getAsString()) : model;
        JsonObject animations = root.has("animations") ? root.getAsJsonObject("animations") : null;
        String idle = animations == null || !animations.has("idle") ? "" : animations.get("idle").getAsString();
        String walk = animations == null || !animations.has("walk") ? "" : animations.get("walk").getAsString();
        JsonArray parts = root.getAsJsonArray("parts");
        if (parts == null || parts.size() == 0 || parts.size() > 48) {
            throw new IllegalArgumentException("Expected 1 to 48 GeckoLib attachments");
        }
        List<GeoAttachment> attachments = new ArrayList<>();
        for (var entry : parts) {
            JsonObject part = entry.getAsJsonObject();
            String anchor = part.get("anchor").getAsString();
            if (!List.of("head", "body", "left_arm", "right_arm", "left_leg", "right_leg").contains(anchor)) {
                throw new IllegalArgumentException("Unknown model anchor " + anchor);
            }
            float[] offset = vector(part, "offset", new float[]{0, 0, 0});
            float[] rotation = vector(part, "rotation", new float[]{0, 0, 0});
            float scale = part.has("scale") ? part.get("scale").getAsFloat() : 1.0f;
            if (!Float.isFinite(scale) || scale <= 0.0f || scale > 16.0f) {
                throw new IllegalArgumentException("Invalid GeckoLib attachment scale");
            }
            attachments.add(new GeoAttachment(anchor, offset, rotation, scale, model, animation, idle, walk));
        }
        return new Appearance(texture, List.of(), List.copyOf(attachments));
    }

    private static float[] vector(JsonObject object, String key, float[] fallback) {
        if (!object.has(key)) {
            if (fallback != null) {
                return fallback;
            }
            throw new IllegalArgumentException("Missing vector " + key);
        }
        JsonArray value = object.getAsJsonArray(key);
        if (value == null || value.size() != 3) {
            throw new IllegalArgumentException("Expected three values in " + key);
        }
        float[] result = new float[3];
        for (int i = 0; i < 3; i++) {
            result[i] = value.get(i).getAsFloat();
            if (!Float.isFinite(result[i]) || Math.abs(result[i]) > 1024.0F) {
                throw new IllegalArgumentException("Invalid value in " + key);
            }
        }
        return result;
    }

    public record Appearance(Identifier texture, List<Attachment> parts, List<GeoAttachment> geckoParts) {
    }

    public record GeoAttachment(String anchor, float[] offset, float[] rotation, float scale,
                                Identifier model, Identifier animation, String idle, String walk) {
    }

    public record Attachment(String anchor, float[] offset, float[] rotation, String swayAxis, float swayDegrees,
                             float swaySpeed, ModelPart geometry) {
    }
}
