package net.zic.ascension.common.command.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.portal.TeleportTransition;
import net.zic.ascension.worldgen.dimension.FloatingRealm;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class RealmTravelCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> buildAscend() {
        return Commands.literal("ascend")
                .executes(context -> ascend(context, List.of(context.getSource().getPlayerOrException())))
                .then(Commands.argument("targets", EntityArgument.players())
                        .executes(context -> ascend(context, EntityArgument.getPlayers(context, "targets")))
                );
    }

    public static LiteralArgumentBuilder<CommandSourceStack> buildDescend() {
        return Commands.literal("descend")
                .executes(context -> descend(context, List.of(context.getSource().getPlayerOrException())))
                .then(Commands.argument("targets", EntityArgument.players())
                        .executes(context -> descend(context, EntityArgument.getPlayers(context, "targets")))
                );
    }

    private static int ascend(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> players) {
        ServerLevel realm = context.getSource().getServer().getLevel(FloatingRealm.DIMENSION);
        if (realm == null) {
            context.getSource().sendFailure(Component.literal("The floating realm is not loaded"));
            return 0;
        }

        BlockPos landing = FloatingRealm.findLanding(realm);
        for (ServerPlayer player : players) {
            player.teleportTo(realm, landing.getX() + 0.5D, landing.getY(), landing.getZ() + 0.5D, Set.of(), player.getYRot(), player.getXRot(), true);
        }

        int count = players.size();
        context.getSource().sendSuccess(() -> Component.literal("Sent " + count + " player(s) to the floating realm"), true);
        return count;
    }

    @SuppressWarnings("resource") //Level is AutoCloseable only for server shutdown
    private static int descend(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> players) {
        int sent = 0;
        for (ServerPlayer player : players) {
            if (!player.level().dimension().equals(FloatingRealm.DIMENSION)) {
                continue;
            }
            //go to their bed/anchor if it is outside the realm, otherwise world spawn
            TeleportTransition transition = player.findRespawnPositionAndUseSpawnBlock(false, TeleportTransition.DO_NOTHING);
            if (transition.newLevel().dimension().equals(FloatingRealm.DIMENSION)) {
                transition = TeleportTransition.createDefault(player, TeleportTransition.DO_NOTHING);
            }
            player.teleport(transition);
            sent++;
        }

        if (sent == 0) {
            context.getSource().sendFailure(Component.literal("No target is in the floating realm"));
            return 0;
        }
        int count = sent;
        context.getSource().sendSuccess(() -> Component.literal("Returned " + count + " player(s) from the floating realm"), true);
        return sent;
    }
}
