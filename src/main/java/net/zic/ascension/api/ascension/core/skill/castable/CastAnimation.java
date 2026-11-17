package net.zic.ascension.api.ascension.core.skill.castable;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NullMarked;

import java.util.Optional;

/**
 * The player animation a cast plays on its caster while it lasts, the {@code animation} field of a cast definition. Written
 * as just the animation's id, or as {@code {"id": ..., "seated_from": ..., "end": ..., "seated_end": ...}}. Played on the
 * client by {@code client.animation.PlayerAnimations} from the cast state every client already has.
 *
 * @param id         the Player Animation Library animation, the name inside a file in
 *                   {@code assets/<namespace>/player_animations/} (for example {@code ascension:meditate})
 * @param seatedFrom seconds into the animation to start at when the caster is already seated (on a cushion), so a sit-down
 *                   at its start is skipped; 0 plays it from the start
 * @param end        played once when the cast ends, before the player goes back to normal (standing back up); without it
 *                   the player snaps back
 * @param seatedEnd  played when the cast ends while seated, holding its last frame until the player gets up (floating back
 *                   down onto the cushion); without it the player snaps to the seated pose
 * @param seatedOnly plays only while the caster sits on a cushion; elsewhere the cast shows no animation (for casts people
 *                   often walk around during)
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

    /** The animation to play when the cast ends, for a caster who is or isn't seated. */
    public Optional<Identifier> endFor(boolean seated) {
        return seated ? seatedEnd : end;
    }
}
