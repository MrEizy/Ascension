package net.zic.ascension.api.ascension.core.skill.castable;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NullMarked;

import java.util.Optional;

/**
 * The player animation a cast plays on its caster, the {@code animation} field of a cast. Either just an id or an object.
 * Played by {@code client.animation.PlayerAnimations}.
 *
 * @param id         the animation, e.g. {@code ascension:meditate}
 * @param seatedFrom seconds to start at when on a cushion, skipping the sit-down
 * @param end        played once when the cast ends
 * @param seatedEnd  played when the cast ends on a cushion, holding its last frame
 * @param seatedOnly only plays on a cushion
 */
@NullMarked
public record CastAnimation(
        Identifier id,
        float seatedFrom,
        Optional<Identifier> end,
        Optional<Identifier> seatedEnd,
        boolean seatedOnly
) {
    private static final Codec<CastAnimation> FULL_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("id").forGetter(CastAnimation::id),
            Codec.floatRange(0.0F, 3600.0F).optionalFieldOf("seated_from", 0.0F).forGetter(CastAnimation::seatedFrom),
            Identifier.CODEC.optionalFieldOf("end").forGetter(CastAnimation::end),
            Identifier.CODEC.optionalFieldOf("seated_end").forGetter(CastAnimation::seatedEnd),
            Codec.BOOL.optionalFieldOf("seated_only", false).forGetter(CastAnimation::seatedOnly)
    ).apply(instance, CastAnimation::new));
    public static final Codec<CastAnimation> CODEC = Codec.either(Identifier.CODEC, FULL_CODEC).xmap(
            either -> either.map(
                    id -> new CastAnimation(id, 0.0F, Optional.empty(), Optional.empty(), false),
                    animation -> animation
            ),
            animation -> animation.seatedFrom() == 0.0F && animation.end().isEmpty() && animation.seatedEnd().isEmpty()
                    && !animation.seatedOnly()
                    ? Either.left(animation.id())
                    : Either.right(animation)
    );

    public Optional<Identifier> endFor(boolean seated) {
        return seated ? seatedEnd : end;
    }
}
