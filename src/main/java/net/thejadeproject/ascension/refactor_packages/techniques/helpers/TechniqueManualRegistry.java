package net.thejadeproject.ascension.refactor_packages.techniques.helpers;

import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class TechniqueManualRegistry {
    public record ManualData(ResourceLocation techniqueId, int requiredPages, List<String> chapterTranslationKeys) {}

    private static final Map<ResourceLocation, ManualData> REGISTRY = new HashMap<>();

    public static void register(ResourceLocation techniqueId, int requiredPages, List<String> chapterTranslationKeys) {
        if (chapterTranslationKeys.size() != requiredPages) {
            throw new IllegalArgumentException("Chapter keys count must match requiredPages");
        }
        REGISTRY.put(techniqueId, new ManualData(techniqueId, requiredPages, chapterTranslationKeys));
    }

    public static void register(ResourceLocation techniqueId, int requiredPages) {
        List<String> defaultKeys = new java.util.ArrayList<>();
        for (int i = 1; i <= requiredPages; i++) {
            defaultKeys.add("ascension.chapter." + techniqueId.getPath() + "." + i);
        }
        register(techniqueId, requiredPages, defaultKeys);
    }

    public static Optional<ManualData> get(ResourceLocation techniqueId) {
        return Optional.ofNullable(REGISTRY.get(techniqueId));
    }

    public static Set<ResourceLocation> getRegisteredTechniques() {
        return Collections.unmodifiableSet(REGISTRY.keySet());
    }
}