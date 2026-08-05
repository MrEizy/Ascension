package net.zic.ascension.impl.runtime.weapon;

import net.minecraft.resources.Identifier;
import net.zic.ascension.AscensionCraft;

import java.util.HashMap;
import java.util.Map;


public final class VfxColorRegistry {
    private static final Map<String, Map<Identifier, String>> REGISTRY = new HashMap<>();

    private VfxColorRegistry() {
    }

    public static void register(String vfxType, Identifier techniqueId, String colorFolder) {
        if (vfxType == null || vfxType.isBlank()
                || techniqueId == null
                || colorFolder == null || colorFolder.isBlank()) {
            return;
        }
        REGISTRY.computeIfAbsent(vfxType, ignored -> new HashMap<>())
                .put(techniqueId, colorFolder);
    }

    public static String resolve(String vfxType, Identifier techniqueId, String fallback) {
        String resolvedFallback = fallback == null || fallback.isBlank() ? "blue" : fallback;
        if (vfxType == null || techniqueId == null) {
            return resolvedFallback;
        }
        Map<Identifier, String> mappings = REGISTRY.get(vfxType);
        return mappings == null ? resolvedFallback : mappings.getOrDefault(techniqueId, resolvedFallback);
    }

    public static String resolve(String vfxType, Identifier techniqueId) {
        return resolve(vfxType, techniqueId, "blue");
    }

    public static Identifier resolveTextureBase(
            String vfxType,
            Identifier techniqueId,
            String fallbackColor
    ) {
        return textureBase(vfxType, resolve(vfxType, techniqueId, fallbackColor));
    }

    public static Identifier textureBase(String vfxType, String colorFolder) {
        return Identifier.fromNamespaceAndPath(
                AscensionCraft.MOD_ID,
                "entity/vfx/" + sanitize(vfxType, "sword_swing") + "/"
                        + sanitize(colorFolder, "blue")
        );
    }

    private static String sanitize(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.replace('\\', '/').replace("..", "");
    }
}
