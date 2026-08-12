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
 *
 * @param source the source of the interaction
 * @param target the target of the interaction
 */
public record PathInteraction(Identifier source,Identifier target,PathInteractionType type,double value) {
    private static final Codec<PathInteraction> INTERACTION_CODEC =RecordCodecBuilder.create(
            instance->instance.group(
                    Identifier.CODEC.fieldOf("source").forGetter(PathInteraction::source),
                    Identifier.CODEC.fieldOf("target").forGetter(PathInteraction::target),
                    Codec.STRING.comapFlatMap(
                            type -> {
                                try {
                                    return DataResult.success(
                                            PathInteractionType.valueOf(type.toUpperCase())
                                    );
                                } catch (IllegalArgumentException e) {
                                    return DataResult.<PathInteractionType>error(() ->
                                            "Unknown target type: " + type);
                                }
                            },
                            PathInteractionType::name
                    ).fieldOf("type").forGetter(PathInteraction::type),
                    Codec.DOUBLE.fieldOf("value").forGetter(PathInteraction::value)
            ).apply(instance,PathInteraction::new)
    );

    public static final Codec<List<PathInteraction>> REGISTRY_CODEC = INTERACTION_CODEC.listOf();

}
