package net.zic.ascension.common.command.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.golden_core.GoldenCoreInstance;
import net.zic.ascension.api.ascension.core.path.realm.Realm;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.rpg_engine.source.OriginSource;

import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

//debug command for golden cores, cores added here are owned by the command
public class GoldenCoreCommand {
    public static final Identifier COMMAND_OWNER = Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "command");

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("golden_core")
                .then(Commands.literal("add")
                        .then(Commands.argument("target", EntityArgument.players())
                                .then(Commands.argument("core", IdentifierArgument.id())
                                        .suggests((context, builder) -> SharedSuggestionProvider.suggestResource(
                                                CoreRegistries.GOLDEN_CORE_REGISTRY.get(context.getSource().registryAccess()).keySet(), builder))
                                        .then(Commands.argument("path", IdentifierArgument.id())
                                                .suggests((context, builder) -> SharedSuggestionProvider.suggestResource(
                                                        CoreRegistries.PATH_REGISTRY.get(context.getSource().registryAccess()).keySet(), builder))
                                                .then(Commands.argument("major", IntegerArgumentType.integer(0))
                                                        .then(Commands.argument("minor", IntegerArgumentType.integer(0))
                                                                .executes(GoldenCoreCommand::add)
                                                        )
                                                )
                                        )
                                )
                        )
                )
                .then(Commands.literal("remove")
                        .then(Commands.argument("target", EntityArgument.players())
                                .then(Commands.argument("id", UuidArgument.uuid())
                                        .suggests(GoldenCoreCommand::suggestHeldCores)
                                        .executes(GoldenCoreCommand::remove)
                                )
                        )
                )
                .then(Commands.literal("list")
                        .then(Commands.argument("target", EntityArgument.players())
                                .executes(GoldenCoreCommand::list)
                        )
                );
    }

    //suggests the ids of every core the targets hold, with the core and path as the tooltip
    private static CompletableFuture<Suggestions> suggestHeldCores(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        Collection<ServerPlayer> players;
        try {
            players = EntityArgument.getPlayers(context, "target");
        } catch (CommandSyntaxException e) {
            return builder.buildFuture();
        }
        String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);
        Set<UUID> suggested = new HashSet<>();
        for (ServerPlayer player : players) {
            OriginSource source = AscensionOriginSourceHelper.getEntitySource(player);
            for (GoldenCoreInstance<?> instance : AscensionOriginSourceHelper.getGoldenCoreInstances(source)) {
                String id = instance.getId().toString();
                if (!id.startsWith(remaining) || !suggested.add(instance.getId())) continue;
                builder.suggest(id, Component.empty()
                        .append(instance.getName())
                        .append(Component.literal(" [" + instance.getCoreId() + "] on " + instance.getPath())));
            }
        }
        return builder.buildFuture();
    }

    private static int add(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Identifier core = IdentifierArgument.getId(context, "core");
        Identifier path = IdentifierArgument.getId(context, "path");
        Realm realm = Realm.of(IntegerArgumentType.getInteger(context, "major"), IntegerArgumentType.getInteger(context, "minor"));
        int added = 0;
        for (ServerPlayer player : EntityArgument.getPlayers(context, "target")) {
            UUID id = UUID.randomUUID();
            OriginSource source = AscensionOriginSourceHelper.getEntitySource(player);
            if (AscensionOriginSourceHelper.addGoldenCore(source, id, core, path, realm, COMMAND_OWNER)) {
                added++;
                context.getSource().sendSuccess(() -> Component.literal("Added " + core + " (" + id + ") to ").append(player.getName()), true);
            }
        }
        if (added == 0) context.getSource().sendFailure(Component.literal("Could not add golden core " + core));
        return added;
    }

    private static int remove(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        UUID id = UuidArgument.getUuid(context, "id");
        int removed = 0;
        for (ServerPlayer player : EntityArgument.getPlayers(context, "target")) {
            OriginSource source = AscensionOriginSourceHelper.getEntitySource(player);
            if (AscensionOriginSourceHelper.removeGoldenCore(source, id, COMMAND_OWNER)) removed++;
        }
        if (removed == 0) context.getSource().sendFailure(Component.literal("No golden core " + id + " owned by the command was removed"));
        else context.getSource().sendSuccess(() -> Component.literal("Removed golden core " + id), true);
        return removed;
    }

    private static int list(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        for (ServerPlayer player : EntityArgument.getPlayers(context, "target")) {
            OriginSource source = AscensionOriginSourceHelper.getEntitySource(player);
            context.getSource().sendSystemMessage(Component.literal("===Golden Cores (").append(player.getName()).append(Component.literal(")===")));
            for (GoldenCoreInstance<?> instance : AscensionOriginSourceHelper.getGoldenCoreInstances(source)) {
                Realm applied = instance.getAppliedRealm();
                context.getSource().sendSystemMessage(Component.empty()
                        .append(instance.getName())
                        .append(Component.literal(" [" + instance.getCoreId() + "] " + instance.getId()
                                + "\n  path: " + instance.getPath()
                                + ", acquired: " + instance.getAcquisitionRealm().asSalt()
                                + ", applied: " + (applied == null ? "none" : applied.asSalt())
                                + ", owners: " + AscensionOriginSourceHelper.getGoldenCoreOwners(source, instance.getId()))));
            }
        }
        return 1;
    }
}
