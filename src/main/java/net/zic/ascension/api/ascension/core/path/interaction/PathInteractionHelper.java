package net.zic.ascension.api.ascension.core.path.interaction;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.path.Path;
import net.zic.ascension.configuration.ConfigurationDataMaps;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Reads path interactions from the {@link ConfigurationDataMaps#PATH_INTERACTIONS} data map.
 * The data map is keyed by the target path, so target lookups are direct and source lookups scan the map.
 */
public final class PathInteractionHelper {

    private PathInteractionHelper() {
    }

    //──Target────────────────────────────────────────────────────────

    /**
     * @return every interaction that influences {@code target}
     */
    public static List<PathInteraction> getInteractions(RegistryAccess access, Identifier target) {
        Registry<Path<?>> registry = CoreRegistries.PATH_REGISTRY.get(access);
        List<PathInteraction.Entry> entries = registry.getData(
                ConfigurationDataMaps.PATH_INTERACTIONS,
                ResourceKey.create(CoreRegistries.PATH_REGISTRY.key(), target)
        );
        if (entries == null) {
            return List.of();
        }
        List<PathInteraction> interactions = new ArrayList<>(entries.size());
        for (PathInteraction.Entry entry : entries) {
            interactions.add(entry.resolve(target));
        }
        return interactions;
    }

    /**
     * @return every interaction of {@code type} that influences {@code target}
     */
    public static List<PathInteraction> getInteractions(RegistryAccess access, Identifier target, PathInteractionType type) {
        List<PathInteraction> interactions = new ArrayList<>();
        for (PathInteraction interaction : getInteractions(access, target)) {
            if (interaction.type() == type) {
                interactions.add(interaction);
            }
        }
        return interactions;
    }

    public static List<PathInteraction> getRelated(RegistryAccess access, Identifier target) {
        return getInteractions(access, target, PathInteractionType.RELATED);
    }

    public static List<PathInteraction> getGenerative(RegistryAccess access, Identifier target) {
        return getInteractions(access, target, PathInteractionType.GENERATIVE);
    }

    public static List<PathInteraction> getDestructive(RegistryAccess access, Identifier target) {
        return getInteractions(access, target, PathInteractionType.DESTRUCTIVE);
    }

    //──Source────────────────────────────────────────────────────────

    /**
     * @return every interaction where {@code source} influences another path
     */
    public static List<PathInteraction> getInteractionsFrom(RegistryAccess access, Identifier source) {
        List<PathInteraction> interactions = new ArrayList<>();
        Map<ResourceKey<Path<?>>, List<PathInteraction.Entry>> dataMap =
                CoreRegistries.PATH_REGISTRY.get(access).getDataMap(ConfigurationDataMaps.PATH_INTERACTIONS);
        for (Map.Entry<ResourceKey<Path<?>>, List<PathInteraction.Entry>> target : dataMap.entrySet()) {
            for (PathInteraction.Entry entry : target.getValue()) {
                if (entry.source().equals(source)) {
                    interactions.add(entry.resolve(target.getKey().identifier()));
                }
            }
        }
        return interactions;
    }

    /**
     * @return every interaction of {@code type} where {@code source} influences another path
     */
    public static List<PathInteraction> getInteractionsFrom(RegistryAccess access, Identifier source, PathInteractionType type) {
        List<PathInteraction> interactions = new ArrayList<>();
        for (PathInteraction interaction : getInteractionsFrom(access, source)) {
            if (interaction.type() == type) {
                interactions.add(interaction);
            }
        }
        return interactions;
    }

    //──Pairs────────────────────────────────────────────────────────

    public static @Nullable PathInteraction getInteraction(RegistryAccess access, Identifier source, Identifier target) {
        for (PathInteraction interaction : getInteractions(access, target)) {
            if (interaction.source().equals(source)) {
                return interaction;
            }
        }
        return null;
    }

    public static boolean hasInteraction(RegistryAccess access, Identifier source, Identifier target) {
        return getInteraction(access, source, target) != null;
    }

    public static boolean hasInteraction(RegistryAccess access, Identifier source, Identifier target, PathInteractionType type) {
        for (PathInteraction interaction : getInteractions(access, target, type)) {
            if (interaction.source().equals(source)) {
                return true;
            }
        }
        return false;
    }
}
