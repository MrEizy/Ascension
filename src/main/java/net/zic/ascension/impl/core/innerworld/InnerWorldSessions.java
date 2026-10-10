package net.zic.ascension.impl.core.innerworld;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.entities.AscEntities;

import java.util.UUID;


public final class InnerWorldSessions {
    private InnerWorldSessions() {
    }

    public static boolean isInside(ServerPlayer player) {
        return player.level().dimension().equals(InnerWorld.DIMENSION);
    }

    public static void toggle(ServerPlayer player) {
        if (isInside(player)) {
            exitVoluntarily(player);
        } else {
            enter(player);
        }
    }

    public static void enter(ServerPlayer player) {
        MinecraftServer server = player.level().getServer();
        ServerLevel innerLevel = server.getLevel(InnerWorld.DIMENSION);
        if (innerLevel == null) {
            player.sendSystemMessage(Component.translatable("ascension.inner_world.unavailable"));
            return;
        }

        InnerWorldPlots.Plot plot = InnerWorldPlots.getOrCreate(player.getUUID());
        if (plot.session != null) {
            teleportIn(player, innerLevel, plot);
            return;
        }

        int tier = InnerWorld.majorRealm(player);
        if (tier > plot.tier) {
            plot.tier = tier;
        }
        InnerWorldGrowth.ensureQueued(server, player.getUUID(), plot);

        ServerLevel origin = (ServerLevel) player.level();
        InnerWorldPlots.Origin savedOrigin = new InnerWorldPlots.Origin(
                origin.dimension().identifier().toString(),
                player.getX(), player.getY(), player.getZ(),
                player.getYRot(), player.getXRot()
        );

        InnerWorldGhost ghost = new InnerWorldGhost(AscEntities.GHOST.get(), origin);
        ghost.setPos(player.getX(), player.getY(), player.getZ());
        ghost.setYRot(player.getYRot());
        ghost.setOwner(player.getUUID(), player.getName().getString());
        double maxHealth = player.getAttributeValue(Attributes.MAX_HEALTH);
        ghost.getAttribute(Attributes.MAX_HEALTH).setBaseValue(maxHealth);
        ghost.setHealth((float) maxHealth);
        origin.addFreshEntity(ghost);

        InnerWorldPlots.Session session = new InnerWorldPlots.Session();
        session.origin = savedOrigin;
        session.ghost = ghost.getUUID().toString();
        plot.session = session;
        InnerWorldPlots.save();

        teleportIn(player, innerLevel, plot);
        player.sendSystemMessage(Component.translatable("ascension.inner_world.entered"));
    }

    private static void teleportIn(ServerPlayer player, ServerLevel innerLevel, InnerWorldPlots.Plot plot) {
        var center = InnerWorld.plotCenter(plot.index);
        InnerWorldTeleport.to(player, innerLevel, center.getX() + 0.5, center.getY() + 1.0, center.getZ() + 0.5, player.getYRot(), player.getXRot());
    }

    public static void exitVoluntarily(ServerPlayer player) {
        InnerWorldPlots.Plot plot = InnerWorldPlots.get(player.getUUID());
        if (plot == null || plot.session == null) {
            player.sendSystemMessage(Component.translatable("ascension.inner_world.no_session"));
            return;
        }
        returnPlayer(player, plot, false);
    }

    public static void onGhostDeath(InnerWorldGhost ghost) {
        UUID ownerId = ghost.ownerId();
        if (ownerId == null) {
            return;
        }
        InnerWorldPlots.Plot plot = InnerWorldPlots.get(ownerId);
        if (plot == null || plot.session == null || !plot.session.ghost.equals(ghost.getUUID().toString())) {
            return;
        }
        MinecraftServer server = ((ServerLevel) ghost.level()).getServer();
        ServerPlayer player = server.getPlayerList().getPlayer(ownerId);
        if (player != null) {
            returnPlayer(player, plot, true);
        } else {
            plot.pendingReturn = plot.session.origin;
            plot.session = null;
            InnerWorldPlots.save();
        }
    }

    private static void returnPlayer(ServerPlayer player, InnerWorldPlots.Plot plot, boolean halfHealth) {
        InnerWorldPlots.Origin origin = plot.session.origin;
        plot.session = null;
        InnerWorldPlots.save();

        MinecraftServer server = player.level().getServer();
        var key = net.minecraft.resources.ResourceKey.create(
                net.minecraft.core.registries.Registries.DIMENSION,
                net.minecraft.resources.Identifier.parse(origin.dimension)
        );
        ServerLevel destination = server.getLevel(key);
        if (destination == null) {
            destination = server.overworld();
        }
        InnerWorldTeleport.to(player, destination, origin.x, origin.y, origin.z, origin.yaw, origin.pitch);

        if (halfHealth) {
            double maxHealth = player.getAttributeValue(Attributes.MAX_HEALTH);
            player.setHealth((float) (maxHealth * 0.5));
            player.sendSystemMessage(Component.translatable("ascension.inner_world.forced_return"));
        } else {
            player.sendSystemMessage(Component.translatable("ascension.inner_world.exited"));
        }
    }

    public static void onPlayerDiedInside(ServerPlayer player) {
        InnerWorldPlots.Plot plot = InnerWorldPlots.get(player.getUUID());
        if (plot == null || plot.session == null) {
            return;
        }
        InnerWorldPlots.Origin origin = plot.session.origin;
        String ghostIdRaw = plot.session.ghost;
        plot.session = null;
        InnerWorldPlots.save();

        try {
            UUID ghostId = UUID.fromString(ghostIdRaw);
            MinecraftServer server = player.level().getServer();
            var key = net.minecraft.resources.ResourceKey.create(
                    net.minecraft.core.registries.Registries.DIMENSION,
                    net.minecraft.resources.Identifier.parse(origin.dimension)
            );
            ServerLevel originLevel = server.getLevel(key);
            if (originLevel != null) {
                net.minecraft.world.entity.Entity ghost = originLevel.getEntity(ghostId);
                if (ghost != null) {
                    ghost.discard();
                }
            }
        } catch (Exception ignored) {
        }
    }

    public static void onPlayerLogin(ServerPlayer player) {
        InnerWorldPlots.Plot plot = InnerWorldPlots.get(player.getUUID());
        if (plot == null || plot.pendingReturn == null) {
            return;
        }
        InnerWorldPlots.Origin origin = plot.pendingReturn;
        plot.pendingReturn = null;
        InnerWorldPlots.save();

        if (isInside(player)) {
            MinecraftServer server = player.level().getServer();
            var key = net.minecraft.resources.ResourceKey.create(
                    net.minecraft.core.registries.Registries.DIMENSION,
                    net.minecraft.resources.Identifier.parse(origin.dimension)
            );
            ServerLevel destination = server.getLevel(key);
            if (destination == null) {
                destination = server.overworld();
            }
            InnerWorldTeleport.to(player, destination, origin.x, origin.y, origin.z, origin.yaw, origin.pitch);
        }
        double maxHealth = player.getAttributeValue(Attributes.MAX_HEALTH);
        player.setHealth((float) (maxHealth * 0.5));
        player.sendSystemMessage(Component.translatable("ascension.inner_world.forced_return"));
    }

    public static boolean isGhostValid(UUID ownerId, UUID ghostId) {
        InnerWorldPlots.Plot plot = InnerWorldPlots.get(ownerId);
        return plot != null && plot.session != null && ghostId.toString().equals(plot.session.ghost);
    }

    public static void enforceBorder(ServerPlayer player) {
        InnerWorldPlots.Plot plot = InnerWorldPlots.get(player.getUUID());
        if (plot == null) {
            return;
        }
        int builtRadius = Math.max(0, plot.generatedRadius);
        InnerWorld.Bounds bounds = InnerWorld.bounds(plot.index, builtRadius);
        double margin = 2.0;
        Vec3 pos = player.position();
        double x = Math.min(Math.max(pos.x, bounds.minX() + margin), bounds.maxX() - margin);
        double z = Math.min(Math.max(pos.z, bounds.minZ() + margin), bounds.maxZ() - margin);
        boolean outOfBounds = x != pos.x || z != pos.z;
        boolean fellThrough = pos.y < InnerWorld.VOID_RESCUE_Y;

        if (outOfBounds || fellThrough) {
            var center = InnerWorld.plotCenter(plot.index);
            if (fellThrough) {
                InnerWorldTeleport.to(player, (ServerLevel) player.level(), center.getX() + 0.5, center.getY() + 1.0, center.getZ() + 0.5, player.getYRot(), player.getXRot());
            } else {
                InnerWorldTeleport.to(player, (ServerLevel) player.level(), x, pos.y, z, player.getYRot(), player.getXRot());
            }
            player.sendSystemMessage(Component.translatable("ascension.inner_world.border"));
        }
    }
}