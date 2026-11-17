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
import net.zic.ascension.api.ascension.core.skill.castable.CastingSkillInstance;
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
 * Plays player animations: a cast's {@code animation} and its ending, otherwise the cross-legged pose on a cushion. Runs
 * from synced cast state and vanilla riding, so it needs no packets. Called from {@code AscensionCraftClient}.
 */
@NullMarked
public final class PlayerAnimations {
    public static final Identifier LAYER = AscensionCraft.prefix("player_animations");
    private static final Playing SITTING = new Playing(AscensionCraft.prefix("sit_crosslegged"), null, false, true, false);
    // skips the sit-down, straight to the seated pose
    private static final float SEATED_TICK = 40.0F;
    private static final int LAYER_PRIORITY = 1000;
    private static final float TICKS_PER_SECOND = 20.0F;
    private static final double MIN_MOVEMENT_SQR = 1.0E-4D;

    // weak keys: a player coming back into view is a new object and starts fresh
    private static final Map<AbstractClientPlayer, Playing> PLAYING = new MapMaker().weakKeys().makeMap();
    private static @Nullable ClientLevel activeLevel;

    private PlayerAnimations() {
    }

    public static void register() {
        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(LAYER, LAYER_PRIORITY,
                avatar -> new PlayerAnimationController(avatar, (controller, data, setter) -> PlayState.STOP));
    }

    public static void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level != activeLevel) {
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
        boolean disturbed = !seated && (player.hurtTime > 0 || moved(player));

        CastAnimation cast = castAnimation(player, local);
        if (cast != null && cast.seatedOnly() && !seated) {
            cast = null;
        }
        if (cast != null) {
            // stays off until the cast ends
            if (current != null && current.interrupted() && current.cast() == cast) {
                return;
            }
            boolean started = current != null && !current.ending() && cast.id().equals(current.id());
            if (disturbed) {
                // not started yet: wait until the player is still
                if (started) {
                    stop(player);
                    PLAYING.put(player, new Playing(cast.id(), cast, false, false, true));
                } else if (current != null) {
                    stop(player);
                    PLAYING.remove(player);
                }
                return;
            }
            if (!started) {
                float startTick = seated ? cast.seatedFrom() * TICKS_PER_SECOND : 0.0F;
                // a player just come into view joins the cast where it is instead of starting over
                if (current == null && !local) {
                    startTick += elapsedTicks(player);
                }
                play(player, new Playing(cast.id(), cast, false, seated, false), startTick);
            }
            return;
        }

        if (current != null && current.cast() != null && !current.interrupted()) {
            if (!current.ending()) {
                // the cast just ended
                Optional<Identifier> end = current.cast().endFor(seated);
                if (end.isPresent() && !disturbed) {
                    play(player, new Playing(end.get(), current.cast(), true, seated, false), 0.0F);
                    return;
                }
            } else if (current.seated() == seated && !disturbed && isActive(player)) {
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

    private static int elapsedTicks(AbstractClientPlayer player) {
        SkillCaster caster = SkillCaster.getExisting(player);
        CastingSkillInstance<?, ?, ?> casting = caster == null ? null : caster.getCasting();
        return casting == null ? 0 : casting.getTotalTicks();
    }

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
        // stored even if the animation is missing, so PAL only logs it once
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

    private static @Nullable CastAnimation castAnimation(AbstractClientPlayer player, boolean local) {
        ActiveCastVisualState state;
        if (local) {
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
     * @param id          the animation
     * @param cast        the cast it belongs to
     * @param ending      whether it is the cast's ending
     * @param seated      whether the player was on a cushion when it started
     * @param interrupted cut off by moving or being hit
     */
    private record Playing(Identifier id, @Nullable CastAnimation cast, boolean ending, boolean seated, boolean interrupted) {
    }
}
