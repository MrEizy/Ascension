package net.zic.ascension.impl.core.skill.castable;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.skill.castable.action.SkillActionContext;
import net.zic.ascension.network.SpaceRiftClosePayload;
import net.zic.ascension.network.SpaceRiftPayload;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Void Pocket: the caster punches the air and space tears open in front of them. Once the rift is open
 * they can click into it to reach their ender chest. The rift closes again when the chest is closed, the caster
 * walks away, or it is left unclicked or open too long.
 */
public final class VoidPocketService {
    private static final Map<UUID, Session> ACTIVE = new ConcurrentHashMap<>();
    private static final double VISUAL_TRACKING_RANGE = 96.0D;
    private static final Component CONTAINER_TITLE = Component.translatable("container.enderchest");
    // How directly (cosine of the angle) the caster must be looking at the rift for a click to count.
    private static final double MIN_FACING_DOT = 0.5D;

    private VoidPocketService() {
    }

    public static void open(SkillActionContext context, SkillActions.VoidPocket config) {
        if (!(context.caster() instanceof ServerPlayer player)) {
            return;
        }
        ServerLevel level = player.level();
        Session previous = ACTIVE.remove(player.getUUID());
        if (previous != null) {
            end(previous, player);
        }

        player.swing(InteractionHand.MAIN_HAND, true);

        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 forward = new Vec3(look.x, 0.0D, look.z);
        forward = forward.lengthSqr() > 1.0E-4D ? forward.normalize() : new Vec3(0.0D, 0.0D, 1.0D);
        Vec3 center = eye.add(forward.scale(config.distance())).add(0.0D, config.heightOffset(), 0.0D);

        Session session = new Session(UUID.randomUUID(), player.getUUID(), level, center, config);
        // The client keeps the rift up until we close it; this is only a safety net.
        float lifetimeSeconds = config.openDelayTicks() / 20.0F + config.idleSeconds() + config.maxSeconds();
        double rangeSquared = VISUAL_TRACKING_RANGE * VISUAL_TRACKING_RANGE;
        for (ServerPlayer observer : level.players()) {
            if (observer.position().distanceToSqr(center) <= rangeSquared) {
                PacketDistributor.sendToPlayer(observer, new SpaceRiftPayload(session.riftId, level.dimension().identifier(),
                        center, forward.reverse(), new Vec3(0.0D, 1.0D, 0.0D), config.radius(), lifetimeSeconds,
                        observer == player));
                session.observers.add(observer.getUUID());
            }
        }
        ACTIVE.put(player.getUUID(), session);
    }

    /** The caster clicked into their rift: reach through it if it is open and they are close enough and facing it. */
    public static void interact(ServerPlayer player, UUID riftId) {
        Session session = ACTIVE.get(player.getUUID());
        if (session == null || !session.riftId.equals(riftId) || session.menu != null
                || player.level() != session.level || session.ticks < session.config.openDelayTicks()
                || session.outOfReach(player)) {
            return;
        }
        Vec3 toRift = session.center.subtract(player.getEyePosition()).normalize();
        if (player.getLookAngle().dot(toRift) < MIN_FACING_DOT) {
            return;
        }
        openEnderChest(player, session);
    }

    private static void openEnderChest(ServerPlayer player, Session session) {
        PlayerEnderChestContainer enderChest = player.getEnderChestInventory();
        // No block backs this container, so validity is only limited by our own reach check.
        enderChest.setActiveChest(null);
        player.openMenu(new SimpleMenuProvider(
                (containerId, inventory, _) -> ChestMenu.threeRows(containerId, inventory, enderChest), CONTAINER_TITLE));
        player.awardStat(Stats.OPEN_ENDERCHEST);
        session.menu = player.containerMenu;
        session.openedAtTick = session.ticks;
        session.level.playSound(null, session.center.x, session.center.y, session.center.z,
                SoundEvents.ENDER_CHEST_OPEN, SoundSource.PLAYERS, 0.8F, 0.7F);
    }

    /** Closes the rift for everyone who saw it open, and the chest if it is still showing. */
    private static void end(Session session, ServerPlayer player) {
        if (player != null && session.menu != null && player.containerMenu == session.menu) {
            player.closeContainer();
        }
        if (session.menu != null) {
            session.level.playSound(null, session.center.x, session.center.y, session.center.z,
                    SoundEvents.ENDER_CHEST_CLOSE, SoundSource.PLAYERS, 0.8F, 0.7F);
        }
        SpaceRiftClosePayload payload = new SpaceRiftClosePayload(session.riftId);
        for (UUID observerId : session.observers) {
            ServerPlayer observer = session.level.getServer().getPlayerList().getPlayer(observerId);
            if (observer != null) {
                PacketDistributor.sendToPlayer(observer, payload);
            }
        }
    }

    private static final class Session {
        final UUID riftId;
        final UUID playerId;
        final ServerLevel level;
        final Vec3 center;
        final SkillActions.VoidPocket config;
        final Set<UUID> observers = new HashSet<>();
        int ticks;
        AbstractContainerMenu menu;
        int openedAtTick;

        Session(UUID riftId, UUID playerId, ServerLevel level, Vec3 center, SkillActions.VoidPocket config) {
            this.riftId = riftId;
            this.playerId = playerId;
            this.level = level;
            this.center = center;
            this.config = config;
        }

        boolean outOfReach(ServerPlayer player) {
            double reach = config.maxReach();
            return player.getEyePosition().distanceToSqr(center) > reach * reach;
        }
    }

    @EventBusSubscriber(modid = AscensionCraft.MOD_ID)
    public static final class ServerTick {
        @SubscribeEvent
        public static void onServerTick(ServerTickEvent.Post event) {
            if (ACTIVE.isEmpty()) {
                return;
            }
            for (var iterator = ACTIVE.values().iterator(); iterator.hasNext(); ) {
                Session session = iterator.next();
                ServerPlayer player = session.level.getServer().getPlayerList().getPlayer(session.playerId);
                boolean playerGone = player == null || !player.isAlive() || player.level() != session.level;
                session.ticks++;

                boolean done;
                if (playerGone) {
                    done = true;
                } else if (session.menu == null) {
                    // Waiting for the caster to click into the rift; give up if they leave or never do.
                    done = session.outOfReach(player)
                            || session.ticks >= session.config.openDelayTicks() + session.config.idleSeconds() * 20;
                } else {
                    done = player.containerMenu != session.menu
                            || session.outOfReach(player)
                            || session.ticks - session.openedAtTick >= session.config.maxSeconds() * 20;
                }

                if (done) {
                    iterator.remove();
                    end(session, playerGone ? null : player);
                }
            }
        }
    }
}
