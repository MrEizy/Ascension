package net.zic.ascension.client.animation;

import com.google.common.collect.MapMaker;
import com.zigythebird.playeranim.animation.PlayerAnimationController;
import com.zigythebird.playeranim.api.PlayerAnimationAccess;
import com.zigythebird.playeranim.api.PlayerAnimationFactory;
import com.zigythebird.playeranimcore.animation.layered.IAnimation;
import com.zigythebird.playeranimcore.enums.PlayState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.Identifier;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.skill.castable.ActiveCastVisualState;
import net.zic.ascension.api.ascension.core.skill.castable.CastAnimation;
import net.zic.ascension.common.data_attachements.AscensionAttachments;
import net.zic.ascension.common.entities.CushionEntity;
import net.zic.ascension.impl.core.skill.castable.ActiveSkill;
import net.zic.ascension.skill_casting.AscensionSkillListener;
import net.zic.ascension.skill_casting.SkillCaster;
import net.zic.zenithlib.common.ZenithAttachments;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Plays Player Animation Library animations on players, from state every client already has: a cast whose definition names
 * an {@code animation} (meditation while cultivating), then that cast's ending once it stops (floating down, standing up),
 * otherwise the cross-legged pose while riding a cushion. Nothing is sent
 * for it; casts reach other clients through the synced {@link SkillCaster} attachment and riding is vanilla.
 * {@link #register()} runs in client setup and {@link #tick()} every client tick, both from {@code AscensionCraftClient}.
 */
@NullMarked
public final class PlayerAnimations {
    public static final Identifier LAYER = AscensionCraft.prefix("player_animations");
    private static final Playing SITTING = new Playing(AscensionCraft.prefix("sit_crosslegged"), null, false, true, false);
    // sit_crosslegged's last keyframe: a cushion shows the finished pose instead of the player sitting down onto it
    private static final float SEATED_TICK = 40.0F;
    // above the default layers, so this pose wins over other mods' idle animations
    private static final int LAYER_PRIORITY = 1000;
    private static final float TICKS_PER_SECOND = 20.0F;
    // 0.01 blocks in a tick, as RecoveryTicker counts movement; sneaking is about 0.065
    private static final double MIN_MOVEMENT_SQR = 1.0E-4D;

    // what each player is playing, so an animation is only (re)started when it changes. Keyed on the player object (weakly):
    // a player who leaves and comes back into view is a new object with a new controller and starts again
    private static final Map<AbstractClientPlayer, Playing> PLAYING = new MapMaker().weakKeys().makeMap();
    private static @Nullable ClientLevel activeLevel;

    private PlayerAnimations() {
    }

    public static void register() {
        // the controller only plays what tick() triggers; it has no idle animation of its own
        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(LAYER, LAYER_PRIORITY,
                avatar -> new PlayerAnimationController(avatar, (controller, data, setter) -> PlayState.STOP));
    }

    public static void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level != activeLevel) {
            // the players (and their controllers) of the old level are gone with it
            PLAYING.clear();
            activeLevel = level;
        }
        if (level == null) {
            return;
        }

        List<AbstractClientPlayer> players = level.players();
        for (int index = 0; index < players.size(); index++) {
            update(players.get(index), players.get(index) == minecraft.player);
        }
    }

    private static void update(AbstractClientPlayer player, boolean local) {
        boolean seated = player.getVehicle() instanceof CushionEntity;
        Playing current = PLAYING.get(player);
        // off a cushion, moving or being hit cuts a cast animation or its ending off at once, with no ending
        boolean disturbed = !seated && (player.hurtTime > 0 || moved(player));

        CastAnimation cast = castAnimation(player, local);
        if (cast != null && cast.seatedOnly() && !seated) {
            cast = null;
        }
        if (cast != null) {
            if (current != null && current.interrupted() && current.cast() == cast) {
                // stays off until this cast ends, rather than restarting the moment the player stands still
                return;
            }
            boolean started = current != null && !current.ending() && cast.id().equals(current.id());
            if (disturbed) {
                // only a meditation already under way is cut off; one not started yet (the player still sliding to a stop
                // as the cast began) just waits until they are still
                if (started) {
                    stop(player);
                    PLAYING.put(player, new Playing(cast.id(), cast, false, false, true));
                } else if (current != null) {
                    // an ending of the previous cast still playing
                    stop(player);
                    PLAYING.remove(player);
                }
                return;
            }
            if (!started) {
                play(player, new Playing(cast.id(), cast, false, seated, false),
                        seated ? cast.seatedFrom() * TICKS_PER_SECOND : 0.0F);
            }
            return;
        }

        if (current != null && current.cast() != null && !current.interrupted()) {
            if (!current.ending()) {
                // the cast just ended: play its ending for where the player is, if it has one
                Optional<Identifier> end = current.cast().endFor(seated);
                if (end.isPresent() && !disturbed) {
                    play(player, new Playing(end.get(), current.cast(), true, seated, false), 0.0F);
                    return;
                }
            } else if (current.seated() == seated && !disturbed && isActive(player)) {
                // let the ending finish; a seated ending holds its last frame (the seated pose) until the player gets up
                return;
            }
        }

        if (seated) {
            if (current == null || !SITTING.id().equals(current.id())) {
                play(player, SITTING, SEATED_TICK);
            }
        } else if (current != null) {
            stop(player);
            PLAYING.remove(player);
        }
    }

    // Whether the player's position changed since last tick (walking, jumping, knockback). Works for other players too: their
    // position only changes when the server sends movement.
    private static boolean moved(AbstractClientPlayer player) {
        double dx = player.getX() - player.xo;
        double dy = player.getY() - player.yo;
        double dz = player.getZ() - player.zo;
        return dx * dx + dy * dy + dz * dz > MIN_MOVEMENT_SQR;
    }

    private static void stop(AbstractClientPlayer player) {
        PlayerAnimationController controller = controller(player);
        if (controller != null) {
            controller.stop();
        }
    }

    private static void play(AbstractClientPlayer player, Playing playing, float startTick) {
        PlayerAnimationController controller = controller(player);
        if (controller == null) {
            return;
        }
        // a missing animation is logged once by PAL; remembering it anyway stops that repeating every tick
        controller.triggerAnimation(playing.id(), startTick);
        PLAYING.put(player, playing);
    }

    private static boolean isActive(AbstractClientPlayer player) {
        PlayerAnimationController controller = controller(player);
        return controller != null && controller.isActive();
    }

    private static @Nullable PlayerAnimationController controller(AbstractClientPlayer player) {
        IAnimation layer = PlayerAnimationAccess.getPlayerAnimationLayer(player, LAYER);
        return layer instanceof PlayerAnimationController controller ? controller : null;
    }

    // The animation of the cast the player is channelling, read the same way ParticleFieldController reads cast visuals.
    private static @Nullable CastAnimation castAnimation(AbstractClientPlayer player, boolean local) {
        ActiveCastVisualState state;
        if (local) {
            // the local cast is predicted and reconciled with the server, so it is always current
            if (!player.getData(ZenithAttachments.ACTION_MANAGER).isActive(AscensionSkillListener.skillCast)) {
                return null;
            }
            state = player.getData(AscensionAttachments.ASCENSION_SKILL_CAST_HANDLER).getActiveCastVisualState();
        } else {
            SkillCaster caster = SkillCaster.getExisting(player);
            state = caster == null ? null : caster.getActiveCastVisualState();
        }
        if (state == null) {
            return null;
        }
        if (!(CoreRegistries.safeAccess(CoreRegistries.SKILL_REGISTRY, state.skill(), player.registryAccess())
                instanceof ActiveSkill skill)) {
            return null;
        }
        return skill.cast().animation().orElse(null);
    }

    /**
     * What a player is playing.
     *
     * @param id     the animation
     * @param cast   the cast animation it belongs to, kept to find the cast's ending once the cast state is gone
     * @param ending whether it is that cast's ending
     * @param seated      whether the player was on a cushion when it started; getting on or off cuts an ending short
     * @param interrupted the cast's animation was cut off by moving or being hit: nothing plays until the cast ends
     */
    private record Playing(Identifier id, @Nullable CastAnimation cast, boolean ending, boolean seated, boolean interrupted) {
    }
}
