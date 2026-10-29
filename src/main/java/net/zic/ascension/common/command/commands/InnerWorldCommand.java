package net.zic.ascension.common.command.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.zic.ascension.impl.core.innerworld.InnerWorld;
import net.zic.ascension.impl.core.innerworld.InnerWorldGrowth;
import net.zic.ascension.impl.core.innerworld.InnerWorldDebug;
import net.zic.ascension.impl.core.innerworld.InnerWorldPlots;

/**
 * /ascension innerworld grow <player> [tiers]   — add tiers (each tier = +CHUNKS_PER_REALM chunks of radius)
 * /ascension innerworld settier <player> <tier> — raise the player's tier to an exact value
 * /ascension innerworld info <player>           — show tier, built radius, target radius, queue state
 *
 * Tiers only ever go UP: land that's already been generated is never un-generated, so lowering a
 * tier would just desync the number from reality. Growth itself is the same queued, tick-budgeted
 * ring generation realm-ups use (InnerWorld.raiseTier), so a big jump is spread over many ticks
 * rather than freezing the server. Permission is inherited from the parent /ascension command.
 *
 * Confidence note: CommandSourceStack.sendSuccess(Supplier<Component>, boolean) and
 * sendFailure(Component) are the long-standing vanilla signatures — if your build changed them,
 * the send* calls below are the only lines to adjust.
 */
public final class InnerWorldCommand {
    private InnerWorldCommand() {
    }

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("innerworld")
                .then(Commands.literal("grow")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> grow(ctx.getSource(), EntityArgument.getPlayer(ctx, "player"), 1))
                                .then(Commands.argument("tiers", IntegerArgumentType.integer(1, 64))
                                        .executes(ctx -> grow(
                                                ctx.getSource(),
                                                EntityArgument.getPlayer(ctx, "player"),
                                                IntegerArgumentType.getInteger(ctx, "tiers"))))))
                .then(Commands.literal("settier")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("tier", IntegerArgumentType.integer(0, 64))
                                        .executes(ctx -> setTier(
                                                ctx.getSource(),
                                                EntityArgument.getPlayer(ctx, "player"),
                                                IntegerArgumentType.getInteger(ctx, "tier"))))))
                .then(Commands.literal("debug")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> {
                                    ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                                    for (String line : InnerWorldDebug.report(target)) {
                                        ctx.getSource().sendSuccess(() -> Component.literal(line), false);
                                    }
                                    return 1;
                                })))
                .then(Commands.literal("info")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> info(ctx.getSource(), EntityArgument.getPlayer(ctx, "player")))));
    }

    private static int grow(CommandSourceStack source, ServerPlayer target, int tiers) {
        InnerWorldPlots.Plot plot = InnerWorldPlots.getOrCreate(target.getUUID());
        return apply(source, target, plot.tier + tiers);
    }

    private static int setTier(CommandSourceStack source, ServerPlayer target, int tier) {
        InnerWorldPlots.getOrCreate(target.getUUID());
        return apply(source, target, tier);
    }

    private static int apply(CommandSourceStack source, ServerPlayer target, int newTier) {
        InnerWorldPlots.Plot plot = InnerWorldPlots.getOrCreate(target.getUUID());
        if (newTier <= plot.tier) {
            source.sendFailure(Component.literal(target.getName().getString() + "'s inner world is already tier "
                    + plot.tier + " (tiers only go up)."));
            return 0;
        }
        InnerWorld.raiseTier(target, newTier);
        int radius = InnerWorld.radiusChunksForTier(plot.tier);
        source.sendSuccess(() -> Component.literal(target.getName().getString() + "'s inner world raised to tier "
                + plot.tier + " (" + radius + "-chunk radius, growing in the background)."), true);
        return 1;
    }

    private static int info(CommandSourceStack source, ServerPlayer target) {
        InnerWorldPlots.Plot plot = InnerWorldPlots.get(target.getUUID());
        if (plot == null) {
            source.sendFailure(Component.literal(target.getName().getString() + " has no inner world yet."));
            return 0;
        }
        int targetRadius = InnerWorld.radiusChunksForTier(plot.tier);
        boolean growing = InnerWorldGrowth.isQueued(target.getUUID()) && plot.generatedRadius < targetRadius;
        source.sendSuccess(() -> Component.literal(target.getName().getString() + ": plot #" + plot.index
                + ", tier " + plot.tier + ", built radius " + plot.generatedRadius + "/" + targetRadius
                + " chunks" + (growing ? " (growing)" : "")
                + (plot.session != null ? ", currently inside" : "")), false);
        return 1;
    }
}