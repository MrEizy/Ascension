package net.zic.ascension.client.visual.runtime;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.phys.Vec3;
import net.zic.ascension.AscensionCraft;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class GuardianDharmaModel {
    private static final double PIXELS_PER_BLOCK = 16.0D;
    private static final double MODEL_CENTER_X = 8.0D;
    private static final double MODEL_CENTER_Z = 8.0D;

    private final Identifier modelId;

    private ResourceManager loadedFrom;
    private LoadedModel loadedModel = LoadedModel.EMPTY;

    public GuardianDharmaModel(Identifier modelId) {
        if (modelId == null) {
            throw new IllegalArgumentException("modelId cannot be null");
        }
        this.modelId = modelId;
    }

    public void invalidate() {
        loadedFrom = null;
        loadedModel = LoadedModel.EMPTY;
    }

    public void render(
            PoseStack.Pose pose,
            MultiBufferSource.BufferSource buffers,
            Vec3 origin,
            double yawRadians,
            double scale,
            int red,
            int green,
            int blue,
            int alpha,
            int packedLight
    ) {
        if (pose == null || buffers == null || origin == null) {
            return;
        }

        ensureLoaded();

        double resolvedScale = Double.isFinite(scale)
                ? Math.max(0.0001D, scale)
                : 1.0D;

        loadedModel.render(
                pose,
                buffers,
                origin,
                yawRadians,
                resolvedScale,
                Math.clamp(red, 0, 255),
                Math.clamp(green, 0, 255),
                Math.clamp(blue, 0, 255),
                Math.clamp(alpha, 0, 255),
                packedLight
        );
    }

    private void ensureLoaded() {
        Minecraft minecraft = Minecraft.getInstance();
        ResourceManager resourceManager = minecraft.getResourceManager();

        if (loadedFrom == resourceManager) {
            return;
        }

        loadedFrom = resourceManager;
        loadedModel = load(resourceManager);
    }

    private LoadedModel load(ResourceManager resourceManager) {
        try {
            Resource resource = resourceManager.getResource(modelId)
                    .orElseThrow(() -> new IllegalStateException(
                            "Missing runtime model resource " + modelId
                    ));

            try (Reader reader = new InputStreamReader(
                    resource.open(),
                    StandardCharsets.UTF_8
            )) {
                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                return parse(root);
            }
        } catch (Exception exception) {
            AscensionCraft.LOGGER.error(
                    "Unable to load Guardian Dharma runtime model {}",
                    modelId,
                    exception
            );
            return LoadedModel.EMPTY;
        }
    }

    private LoadedModel parse(JsonObject root) {
        double textureWidth = 16.0D;
        double textureHeight = 16.0D;

        JsonArray textureSize = array(root, "texture_size");
        if (textureSize != null && textureSize.size() >= 2) {
            textureWidth = positive(textureSize.get(0).getAsDouble(), 16.0D);
            textureHeight = positive(textureSize.get(1).getAsDouble(), 16.0D);
        }

        Map<String, String> textureReferences = new LinkedHashMap<>();
        JsonObject textures = object(root, "textures");
        if (textures != null) {
            for (Map.Entry<String, JsonElement> entry : textures.entrySet()) {
                if (entry.getValue().isJsonPrimitive()) {
                    textureReferences.put(entry.getKey(), entry.getValue().getAsString());
                }
            }
        }

        Map<Identifier, List<ModelFace>> facesByTexture = new LinkedHashMap<>();
        JsonArray elements = array(root, "elements");
        if (elements == null) {
            return LoadedModel.EMPTY;
        }

        for (JsonElement elementValue : elements) {
            if (!elementValue.isJsonObject()) {
                continue;
            }

            JsonObject element = elementValue.getAsJsonObject();
            Vec3 from = vector(element, "from", Vec3.ZERO);
            Vec3 to = vector(element, "to", Vec3.ZERO);
            ElementRotation rotation = parseRotation(object(element, "rotation"));
            JsonObject faces = object(element, "faces");

            if (faces == null) {
                continue;
            }

            for (Map.Entry<String, JsonElement> faceEntry : faces.entrySet()) {
                if (!faceEntry.getValue().isJsonObject()) {
                    continue;
                }

                FaceDirection direction = FaceDirection.fromName(faceEntry.getKey());
                if (direction == null) {
                    continue;
                }

                JsonObject faceObject = faceEntry.getValue().getAsJsonObject();
                String textureReference = string(faceObject, "texture", null);
                Identifier texture = resolveTexture(textureReference, textureReferences);
                JsonArray uvArray = array(faceObject, "uv");

                if (texture == null || uvArray == null || uvArray.size() < 4) {
                    continue;
                }

                Vec3[] vertices = direction.vertices(from, to);
                for (int index = 0; index < vertices.length; index++) {
                    vertices[index] = toLocal(rotation.apply(vertices[index]));
                }

                Vec3 normal = rotation.applyDirection(direction.normal()).normalize();
                Uv[] uvs = parseUvs(
                        uvArray,
                        integer(faceObject, "rotation", 0)
                );

                facesByTexture.computeIfAbsent(texture, ignored -> new ArrayList<>())
                        .add(new ModelFace(vertices, uvs, normal));
            }
        }

        if (facesByTexture.isEmpty()) {
            return LoadedModel.EMPTY;
        }

        Map<Identifier, List<ModelFace>> immutable = new LinkedHashMap<>();
        for (Map.Entry<Identifier, List<ModelFace>> entry : facesByTexture.entrySet()) {
            immutable.put(entry.getKey(), List.copyOf(entry.getValue()));
        }
        return new LoadedModel(Map.copyOf(immutable));
    }

    private Identifier resolveTexture(
            String reference,
            Map<String, String> textureReferences
    ) {
        if (reference == null || reference.isBlank()) {
            return null;
        }

        String resolved = reference;
        int guard = 0;
        while (resolved.startsWith("#") && guard++ < 16) {
            resolved = textureReferences.get(resolved.substring(1));
            if (resolved == null || resolved.isBlank()) {
                return null;
            }
        }

        String namespace = modelId.getNamespace();
        String path = resolved;
        int separator = resolved.indexOf(':');
        if (separator >= 0) {
            namespace = resolved.substring(0, separator);
            path = resolved.substring(separator + 1);
        }

        if (path.startsWith("textures/")) {
            path = path.substring("textures/".length());
        }
        if (path.endsWith(".png")) {
            path = path.substring(0, path.length() - ".png".length());
        }

        return Identifier.fromNamespaceAndPath(
                namespace,
                "textures/" + path + ".png"
        );
    }

    private static ElementRotation parseRotation(JsonObject object) {
        if (object == null) {
            return ElementRotation.NONE;
        }

        Vec3 origin = vector(object, "origin", new Vec3(8.0D, 8.0D, 8.0D));
        Vec3 degrees;

        if (object.has("angle") && object.has("axis")) {
            double angle = number(object, "angle", 0.0D);
            String axis = string(object, "axis", "y").toLowerCase(Locale.ROOT);
            degrees = switch (axis) {
                case "x" -> new Vec3(angle, 0.0D, 0.0D);
                case "z" -> new Vec3(0.0D, 0.0D, angle);
                default -> new Vec3(0.0D, angle, 0.0D);
            };
        } else {
            degrees = new Vec3(
                    number(object, "x", 0.0D),
                    number(object, "y", 0.0D),
                    number(object, "z", 0.0D)
            );
        }

        return new ElementRotation(origin, degrees);
    }

    private static Uv[] parseUvs(
            JsonArray array,
            int rotation
    ) {
        double firstU = array.get(0).getAsDouble() / 16;
        double firstV = array.get(1).getAsDouble() / 16;
        double secondU = array.get(2).getAsDouble() / 16;
        double secondV = array.get(3).getAsDouble() / 16;

        Uv[] values = new Uv[]{
                new Uv(firstU, secondV),
                new Uv(secondU, secondV),
                new Uv(secondU, firstV),
                new Uv(firstU, firstV)
        };

        int turns = Math.floorMod(rotation, 360) / 90;
        for (int turn = 0; turn < turns; turn++) {
            Uv last = values[values.length - 1];
            System.arraycopy(values, 0, values, 1, values.length - 1);
            values[0] = last;
        }

        return values;
    }

    private static Vec3 toLocal(Vec3 pixelPosition) {
        return new Vec3(
                (pixelPosition.x - MODEL_CENTER_X) / PIXELS_PER_BLOCK,
                pixelPosition.y / PIXELS_PER_BLOCK,
                (pixelPosition.z - MODEL_CENTER_Z) / PIXELS_PER_BLOCK
        );
    }

    private static Vec3 rotate(Vec3 value, Vec3 degrees) {
        Vec3 result = value;

        if (degrees.x != 0.0D) {
            double radians = Math.toRadians(degrees.x);
            double cos = Math.cos(radians);
            double sin = Math.sin(radians);
            result = new Vec3(
                    result.x,
                    result.y * cos - result.z * sin,
                    result.y * sin + result.z * cos
            );
        }

        if (degrees.y != 0.0D) {
            result = rotateY(result, Math.toRadians(degrees.y));
        }

        if (degrees.z != 0.0D) {
            double radians = Math.toRadians(degrees.z);
            double cos = Math.cos(radians);
            double sin = Math.sin(radians);
            result = new Vec3(
                    result.x * cos - result.y * sin,
                    result.x * sin + result.y * cos,
                    result.z
            );
        }

        return result;
    }

    private static Vec3 rotateY(Vec3 value, double radians) {
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        return new Vec3(
                value.x * cos - value.z * sin,
                value.y,
                value.x * sin + value.z * cos
        );
    }

    private static JsonObject object(JsonObject parent, String key) {
        JsonElement value = parent == null ? null : parent.get(key);
        return value != null && value.isJsonObject() ? value.getAsJsonObject() : null;
    }

    private static JsonArray array(JsonObject parent, String key) {
        JsonElement value = parent == null ? null : parent.get(key);
        return value != null && value.isJsonArray() ? value.getAsJsonArray() : null;
    }

    private static Vec3 vector(JsonObject parent, String key, Vec3 fallback) {
        JsonArray value = array(parent, key);
        if (value == null || value.size() < 3) {
            return fallback;
        }
        return new Vec3(
                value.get(0).getAsDouble(),
                value.get(1).getAsDouble(),
                value.get(2).getAsDouble()
        );
    }

    private static String string(JsonObject parent, String key, String fallback) {
        JsonElement value = parent == null ? null : parent.get(key);
        return value != null && value.isJsonPrimitive()
                ? value.getAsString()
                : fallback;
    }

    private static double number(JsonObject parent, String key, double fallback) {
        JsonElement value = parent == null ? null : parent.get(key);
        return value != null && value.isJsonPrimitive()
                ? value.getAsDouble()
                : fallback;
    }

    private static int integer(JsonObject parent, String key, int fallback) {
        JsonElement value = parent == null ? null : parent.get(key);
        return value != null && value.isJsonPrimitive()
                ? value.getAsInt()
                : fallback;
    }

    private static double positive(double value, double fallback) {
        return Double.isFinite(value) && value > 0.0D ? value : fallback;
    }

    private record ElementRotation(Vec3 origin, Vec3 degrees) {
        private static final ElementRotation NONE = new ElementRotation(
                Vec3.ZERO,
                Vec3.ZERO
        );

        private Vec3 apply(Vec3 point) {
            if (degrees.lengthSqr() <= 1.0E-12D) {
                return point;
            }
            return rotate(point.subtract(origin), degrees).add(origin);
        }

        private Vec3 applyDirection(Vec3 direction) {
            return degrees.lengthSqr() <= 1.0E-12D
                    ? direction
                    : rotate(direction, degrees);
        }
    }

    private record Uv(double u, double v) {
    }

    private record ModelFace(Vec3[] vertices, Uv[] uvs, Vec3 normal) {
    }

    private record LoadedModel(Map<Identifier, List<ModelFace>> facesByTexture) {
        private static final LoadedModel EMPTY = new LoadedModel(Map.of());

        private void render(
                PoseStack.Pose pose,
                MultiBufferSource.BufferSource buffers,
                Vec3 origin,
                double yawRadians,
                double scale,
                int red,
                int green,
                int blue,
                int alpha,
                int packedLight
        ) {
            for (Map.Entry<Identifier, List<ModelFace>> entry : facesByTexture.entrySet()) {
                VertexConsumer vertices = buffers.getBuffer(
                        RenderTypes.entityTranslucent(entry.getKey())
                );

                for (ModelFace face : entry.getValue()) {
                    Vec3 normal = rotateY(face.normal(), yawRadians).normalize();

                    for (int index = 0; index < 4; index++) {
                        Vec3 point = rotateY(
                                face.vertices()[index].scale(scale),
                                yawRadians
                        ).add(origin);
                        Uv uv = face.uvs()[index];

                        vertices.addVertex(
                                        pose,
                                        (float) point.x,
                                        (float) point.y,
                                        (float) point.z
                                )
                                .setColor(red, green, blue, alpha)
                                .setUv((float) uv.u(), (float) uv.v())
                                .setOverlay(OverlayTexture.NO_OVERLAY)
                                .setLight(packedLight)
                                .setNormal(
                                        pose,
                                        (float) normal.x,
                                        (float) normal.y,
                                        (float) normal.z
                                );
                    }
                }
            }
        }
    }

    private enum FaceDirection {
        NORTH("north", new Vec3(0.0D, 0.0D, -1.0D)) {
            @Override
            Vec3[] vertices(Vec3 from, Vec3 to) {
                return new Vec3[]{
                        new Vec3(to.x, from.y, from.z),
                        new Vec3(from.x, from.y, from.z),
                        new Vec3(from.x, to.y, from.z),
                        new Vec3(to.x, to.y, from.z)
                };
            }
        },
        EAST("east", new Vec3(1.0D, 0.0D, 0.0D)) {
            @Override
            Vec3[] vertices(Vec3 from, Vec3 to) {
                return new Vec3[]{
                        new Vec3(to.x, from.y, to.z),
                        new Vec3(to.x, from.y, from.z),
                        new Vec3(to.x, to.y, from.z),
                        new Vec3(to.x, to.y, to.z)
                };
            }
        },
        SOUTH("south", new Vec3(0.0D, 0.0D, 1.0D)) {
            @Override
            Vec3[] vertices(Vec3 from, Vec3 to) {
                return new Vec3[]{
                        new Vec3(from.x, from.y, to.z),
                        new Vec3(to.x, from.y, to.z),
                        new Vec3(to.x, to.y, to.z),
                        new Vec3(from.x, to.y, to.z)
                };
            }
        },
        WEST("west", new Vec3(-1.0D, 0.0D, 0.0D)) {
            @Override
            Vec3[] vertices(Vec3 from, Vec3 to) {
                return new Vec3[]{
                        new Vec3(from.x, from.y, from.z),
                        new Vec3(from.x, from.y, to.z),
                        new Vec3(from.x, to.y, to.z),
                        new Vec3(from.x, to.y, from.z)
                };
            }
        },
        UP("up", new Vec3(0.0D, 1.0D, 0.0D)) {
            @Override
            Vec3[] vertices(Vec3 from, Vec3 to) {
                return new Vec3[]{
                        new Vec3(from.x, to.y, to.z),
                        new Vec3(to.x, to.y, to.z),
                        new Vec3(to.x, to.y, from.z),
                        new Vec3(from.x, to.y, from.z)
                };
            }
        },
        DOWN("down", new Vec3(0.0D, -1.0D, 0.0D)) {
            @Override
            Vec3[] vertices(Vec3 from, Vec3 to) {
                return new Vec3[]{
                        new Vec3(from.x, from.y, from.z),
                        new Vec3(to.x, from.y, from.z),
                        new Vec3(to.x, from.y, to.z),
                        new Vec3(from.x, from.y, to.z)
                };
            }
        };

        private final String serializedName;
        private final Vec3 normal;

        FaceDirection(String serializedName, Vec3 normal) {
            this.serializedName = serializedName;
            this.normal = normal;
        }

        abstract Vec3[] vertices(Vec3 from, Vec3 to);

        Vec3 normal() {
            return normal;
        }

        static FaceDirection fromName(String name) {
            if (name == null) {
                return null;
            }
            for (FaceDirection value : values()) {
                if (value.serializedName.equals(name)) {
                    return value;
                }
            }
            return null;
        }
    }
}
