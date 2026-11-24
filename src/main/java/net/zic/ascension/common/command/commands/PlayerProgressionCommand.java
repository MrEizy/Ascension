package net.zic.ascension.common.command.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.common.command.AscensionCommand;
import net.zic.ascension.common.starter.PlayerProgressionReset;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public final class PlayerProgressionCommand {
    private PlayerProgressionCommand() {
    }

    public static LiteralArgumentBuilder<CommandSourceStack> buildBloodline() {
        return build(Kind.BLOODLINE);
    }

    public static LiteralArgumentBuilder<CommandSourceStack> buildTechnique() {
        return build(Kind.TECHNIQUE);
    }

    private static LiteralArgumentBuilder<CommandSourceStack> build(Kind kind) {
        return Commands.literal(kind.command)
                .then(Commands.literal("remove").requires(AscensionCommand::isAdmin)
                        .then(Commands.argument("target", EntityArgument.players())
                                .then(Commands.argument(kind.command + "s", StringArgumentType.greedyString())
                                        .suggests((context, builder) -> suggestHeld(context, builder, kind))
                                        .executes(context -> remove(context, kind)))));
    }

    public static LiteralArgumentBuilder<CommandSourceStack> buildReset() {
        return Commands.literal("reset").requires(AscensionCommand::isAdmin)
                .then(Commands.argument("target", EntityArgument.players())
                        .executes(PlayerProgressionCommand::reset));
    }

    private static CompletableFuture<Suggestions> suggestHeld(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder, Kind kind) {
        if (!AscensionCommand.isAdmin(context.getSource())) return builder.buildFuture();
        Set<Identifier> ids = new LinkedHashSet<>();
        try {
            for (ServerPlayer player : EntityArgument.getPlayers(context, "target")) {
                ids.addAll(kind.held(AscensionOriginSourceHelper.getEntitySource(player)));
            }
        } catch (CommandSyntaxException exception) {
            return builder.buildFuture();
        }
        String remaining = builder.getRemaining();
        int offset = Math.max(remaining.lastIndexOf(','), remaining.lastIndexOf(' ')) + 1;
        for (String previous : remaining.substring(0, offset).split("[,\\s]+")) {
            if (!previous.isBlank()) {
                try {
                    ids.remove(Identifier.parse(previous));
                } catch (RuntimeException ignored) {
                }
            }
        }
        return SharedSuggestionProvider.suggestResource(ids, builder.createOffset(builder.getStart() + offset));
    }

    private static int remove(CommandContext<CommandSourceStack> context, Kind kind) throws CommandSyntaxException {
        if (!AscensionCommand.isAdmin(context.getSource())) return 0;
        String input = StringArgumentType.getString(context, kind.command + "s").trim();
        Set<Identifier> ids = new LinkedHashSet<>();
        for (String token : input.split("[,\\s]+")) {
            try {
                if (token.isBlank()) throw new IllegalArgumentException();
                ids.add(Identifier.parse(token));
            } catch (RuntimeException exception) {
                context.getSource().sendFailure(Component.literal("Invalid " + kind.command + " ID: " + token));
                return 0;
            }
        }
        if (ids.isEmpty()) return 0;
        int removed = 0;
        for (ServerPlayer player : EntityArgument.getPlayers(context, "target")) {
            OriginSource source = AscensionOriginSourceHelper.getEntitySource(player);
            for (Identifier id : ids) {
                if (kind.remove(source, id)) {
                    removed++;
                    context.getSource().sendSuccess(() -> Component.literal("Removed " + id + " from ").append(player.getName()), true);
                } else {
                    context.getSource().sendFailure(Component.literal("Could not remove " + id + " from " + player.getName().getString() + " (not held or removal rejected)"));
                }
            }
        }
        return removed;
    }

    private static int reset(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if (!AscensionCommand.isAdmin(context.getSource())) return 0;
        int count = 0;
        for (ServerPlayer player : EntityArgument.getPlayers(context, "target")) {
            if (!PlayerProgressionReset.reset(player)) {
                context.getSource().sendFailure(Component.literal("Cannot reset " + player.getName().getString() + " while dead or inside an inner world"));
                continue;
            }
            context.getSource().sendSuccess(() -> Component.literal("Reset Ascension progression for ").append(player.getName()), true);
            count++;
        }
        return count;
    }

    private enum Kind {
        BLOODLINE("bloodline"), TECHNIQUE("technique");

        private final String command;

        Kind(String command) {
            this.command = command;
        }

        Collection<Identifier> held(OriginSource source) {
            return switch (this) {
                case BLOODLINE -> AscensionOriginSourceHelper.getBloodlines(source);
                case TECHNIQUE -> AscensionOriginSourceHelper.getTechniques(source);
            };
        }

        boolean remove(OriginSource source, Identifier id) {
            return switch (this) {
                case BLOODLINE -> AscensionOriginSourceHelper.removeBloodline(source, id);
                case TECHNIQUE -> AscensionOriginSourceHelper.removeTechnique(source, id);
            };
        }
    }
}
