package net.zic.ascension.impl.core.connection;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.bloodline.Bloodline;
import net.zic.ascension.api.ascension.core.path.Path;
import net.zic.ascension.api.ascension.core.physique.Physique;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.ascension.core.technique.Technique;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Function;

/**
 * The four connection requirements (physique, technique, bloodline, path). Each one is a list of ids, and it is met when the source has at least one of
 * the listed ids. Every requirement that is set has to be met. A list is empty when the requirement is not used.
 * <p>
 * A connected type has no field for its own kind, so a connected_physique can never require a physique. Any of them can require a path.
 */
public record ConnectionRequirements(List<Identifier> physiques, List<Identifier> techniques, List<Identifier> bloodlines, List<Identifier> paths) {
    public static final ConnectionRequirements EMPTY = new ConnectionRequirements(List.of(), List.of(), List.of(), List.of());

    /** accepts "ascension:x" or ["ascension:x", "ascension:y"] */
    public static final Codec<List<Identifier>> IDS = Codec.withAlternative(Identifier.CODEC.listOf(), Identifier.CODEC, List::of);

    public ConnectionRequirements {
        physiques = physiques == null ? List.of() : List.copyOf(physiques);
        techniques = techniques == null ? List.of() : List.copyOf(techniques);
        bloodlines = bloodlines == null ? List.of() : List.copyOf(bloodlines);
        paths = paths == null ? List.of() : List.copyOf(paths);
    }

    public boolean isEmpty() {
        return physiques.isEmpty() && techniques.isEmpty() && bloodlines.isEmpty() && paths.isEmpty();
    }

    public static <T extends Connected> MapCodec<T> requireAtLeastOne(MapCodec<T> codec) {
        return codec.flatXmap(
                value -> value.connections().isEmpty()
                        ? DataResult.error(() -> "A connected definition needs at least one connection requirement")
                        : DataResult.success(value),
                DataResult::success
        );
    }

    public boolean isMet(OriginSource source) {
        return unmetKind(source) == null;
    }

    public @Nullable Component failureMessage(OriginSource source, RegistryAccess access) {
        Kind kind = unmetKind(source);
        if (kind == null) {
            return null;
        }
        return switch (kind) {
            case PHYSIQUE -> Component.translatable("ascension.connection.requires.physique", names(physiques, id -> physiqueName(id, access)));
            case TECHNIQUE -> Component.translatable("ascension.connection.requires.technique", names(techniques, id -> techniqueName(id, access)));
            case BLOODLINE -> Component.translatable("ascension.connection.requires.bloodline", names(bloodlines, id -> bloodlineName(id, access)));
            case PATH -> Component.translatable("ascension.connection.requires.path", names(paths, id -> pathName(id, access)));
        };
    }

    private @Nullable Kind unmetKind(OriginSource source) {
        if (source == null) {
            return isEmpty() ? null : Kind.PHYSIQUE;
        }
        if (!physiques.isEmpty()) {
            Identifier heldPhysique = AscensionOriginSourceHelper.getPhysiqueId(source);
            if (heldPhysique == null || !physiques.contains(heldPhysique)) {
                return Kind.PHYSIQUE;
            }
        }
        if (!techniques.isEmpty() && techniques.stream().noneMatch(id -> AscensionOriginSourceHelper.hasTechnique(source, id))) {
            return Kind.TECHNIQUE;
        }
        if (!bloodlines.isEmpty()) {
            List<Identifier> held = List.copyOf(AscensionOriginSourceHelper.getBloodlines(source));
            if (bloodlines.stream().noneMatch(held::contains)) {
                return Kind.BLOODLINE;
            }
        }
        if (!paths.isEmpty() && paths.stream().noneMatch(id -> AscensionOriginSourceHelper.hasPath(source, id))) {
            return Kind.PATH;
        }
        return null;
    }

    private enum Kind {
        PHYSIQUE, TECHNIQUE, BLOODLINE, PATH
    }

    private static Component names(List<Identifier> ids, Function<Identifier, Component> namer) {
        MutableComponent joined = Component.empty();
        for (int i = 0; i < ids.size(); i++) {
            if (i > 0) {
                joined.append(" / ");
            }
            joined.append(namer.apply(ids.get(i)));
        }
        return joined;
    }

    public static Component physiqueName(Identifier id, RegistryAccess access) {
        Physique<?> physique = CoreRegistries.safeAccess(CoreRegistries.PHYSIQUE_REGISTRY, id, access);
        return physique == null ? unknown(id, "physique") : physique.getName(null);
    }

    public static Component techniqueName(Identifier id, RegistryAccess access) {
        Technique<?> technique = CoreRegistries.safeAccess(CoreRegistries.TECHNIQUE_REGISTRY, id, access);
        return technique == null ? unknown(id, "technique") : technique.getName(null);
    }

    public static Component pathName(Identifier id, RegistryAccess access) {
        Path<?> path = CoreRegistries.safeAccess(CoreRegistries.PATH_REGISTRY, id, access);
        return path == null ? unknown(id, "path") : path.getName(null);
    }

    public static Component bloodlineName(Identifier id, RegistryAccess access) {
        Bloodline<?> bloodline = CoreRegistries.safeAccess(CoreRegistries.BLOODLINE_REGISTRY, id, access);
        return bloodline == null ? unknown(id, "bloodline") : bloodline.getName(null);
    }

    private static Component unknown(Identifier id, String kind) {
        AscensionCraft.LOGGER.warn("[Connection] {} connection {} is not registered, check the id in the datapack", kind, id);
        String path = id.getPath();
        path = path.substring(path.lastIndexOf('/') + 1);
        StringBuilder readable = new StringBuilder();
        for (String word : path.split("_")) {
            if (word.isBlank()) {
                continue;
            }
            if (readable.length() > 0) {
                readable.append(' ');
            }
            readable.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return Component.literal(readable.toString());
    }
}