package net.zic.ascension.api.ascension.core.path.interaction;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

import java.util.List;

/**
 * Maps a relationship source -> target
 * If some event happens the uses target and source is also present target is influenced by the interaction from source
 *
 * example
 * source = water target = ice type = Related value = 0.5
 * if i use an ice cultivation technique and i have water affinity the ice affinity gets 50% of the water affinity
 *
 * so source is the path that affects the target
 * <p>
 * Interactions are stored in the {@code ascension:path_interactions} data map, keyed by the target path,
 * see {@link PathInteractionHelper}.
 *
 * @param source the source of the interaction
 * @param target the target of the interaction
 */
public record PathInteraction(Identifier source,Identifier target,PathInteractionType type,double value) {

    private static final Codec<PathInteractionType> TYPE_CODEC = Codec.STRING.comapFlatMap(
            type -> {
                try {
                    return DataResult.success(
                            PathInteractionType.valueOf(type.toUpperCase())
                    );
                } catch (IllegalArgumentException e) {
                    return DataResult.<PathInteractionType>error(() ->
                            "Unknown interaction type: " + type);
                }
            },
            type -> type.name().toLowerCase()
    );

    /**
     * A single data map entry, the target is the path the data map value is attached to.
     */
    public record Entry(Identifier source, PathInteractionType type, double value) {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                        Identifier.CODEC.fieldOf("source").forGetter(Entry::source),
                        TYPE_CODEC.fieldOf("type").forGetter(Entry::type),
                        Codec.DOUBLE.fieldOf("value").forGetter(Entry::value)
                ).apply(instance, Entry::new)
        );
        public static final Codec<List<Entry>> LIST_CODEC = CODEC.listOf();

        public PathInteraction resolve(Identifier target){
            return new PathInteraction(source, target, type, value);
        }
    }
}
