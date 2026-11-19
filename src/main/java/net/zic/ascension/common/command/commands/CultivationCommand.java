package net.zic.ascension.common.command.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.zic.ascension.api.ascension.capabilities.AscensionEntityDataProvider;
import net.zic.ascension.api.ascension.capabilities.CoreCapabilities;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.path.Path;
import net.zic.ascension.api.ascension.core.path.PathInstance;
import net.zic.ascension.api.ascension.core.path.realm.Realm;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.impl.core.path.simple.SimplePathData;

import java.util.Collection;
import java.util.Comparator;

public final class CultivationCommand {
    private CultivationCommand() {
    }

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("cultivation")
                .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                .then(Commands.literal("add")
                        .then(Commands.argument("target", EntityArgument.players())
                                .then(pathArgument().executes(context -> modifyOwner(context, true)))))
                .then(Commands.literal("remove")
                        .then(Commands.argument("target", EntityArgument.players())
                                .then(pathArgument().executes(context -> modifyOwner(context, false)))))
                .then(Commands.literal("list")
                        .then(Commands.argument("target", EntityArgument.players())
                                .executes(CultivationCommand::listPaths)))
                .then(Commands.literal("set")
                        .then(Commands.argument("target", EntityArgument.players())
                                .then(pathArgument()
                                        .then(Commands.argument("majorRealm", IntegerArgumentType.integer(0))
                                                .then(Commands.argument("minorRealm", IntegerArgumentType.integer(0))
                                                        .executes(CultivationCommand::setRealm)
                                                        .then(Commands.argument("progress", IntegerArgumentType.integer(0, 100))
                                                                .executes(CultivationCommand::setRealm)))))))
                .then(Commands.literal("toggle_suppressed")
                        .then(Commands.argument("target", EntityArgument.players())
                                .executes(CultivationCommand::toggleSuppressed)))
                .then(Commands.literal("suppress")
                        .then(Commands.argument("target", EntityArgument.players())
                                .then(Commands.literal("true").executes(context -> setSuppressed(context, true)))
                                .then(Commands.literal("false").executes(context -> setSuppressed(context, false)))))
                .then(Commands.literal("show")
                        .then(Commands.argument("target", EntityArgument.players())
                                .then(pathArgument().executes(CultivationCommand::showPath))));
    }

    public static LiteralArgumentBuilder<CommandSourceStack> buildInspect() {
        return Commands.literal("cultivation")
                .then(Commands.argument("target", EntityArgument.players())
                        .then(pathArgument().executes(CultivationCommand::showPath)));
    }

    private static com.mojang.brigadier.builder.RequiredArgumentBuilder<CommandSourceStack, Identifier> pathArgument() {
        return Commands.argument("path", IdentifierArgument.id())
                .suggests((context, builder) -> SharedSuggestionProvider.suggestResource(
                        CoreRegistries.PATH_REGISTRY.get(context.getSource().registryAccess()).keySet(), builder));
    }

    private static OriginSource getSource(ServerPlayer player, CommandSourceStack commandSource) {
        AscensionEntityDataProvider provider = player.getCapability(CoreCapabilities.ASCENSION_ENTITY_DATA_PROVIDER_CAPABILITY);
        if (provider == null || provider.getData() == null || provider.getData().getSource() == null) {
            commandSource.sendFailure(Component.literal("Missing cultivation data for " + player.getName().getString()));
            return null;
        }
        return provider.getData().getSource();
    }

    private static int modifyOwner(CommandContext<CommandSourceStack> context, boolean adding) throws CommandSyntaxException {
        Identifier id = IdentifierArgument.getId(context, "path");
        if (CoreRegistries.safeAccess(CoreRegistries.PATH_REGISTRY, id, context.getSource().registryAccess()) == null) {
            context.getSource().sendFailure(Component.literal("Unknown cultivation path " + id));
            return 0;
        }
        int changes = 0;
        for (ServerPlayer player : EntityArgument.getPlayers(context, "target")) {
            OriginSource source = getSource(player, context.getSource());
            if (source == null) continue;
            boolean changed = adding
                    ? AscensionOriginSourceHelper.addPath(source, id, AscensionOriginSourceHelper.COMMAND_PATH_OWNER)
                    : AscensionOriginSourceHelper.removePath(source, id, AscensionOriginSourceHelper.COMMAND_PATH_OWNER);
            if (!changed) {
                context.getSource().sendSystemMessage(Component.literal(
                        player.getName().getString() + ": no command-owned " + id + " path change"));
                continue;
            }
            changes++;
            boolean held = AscensionOriginSourceHelper.hasPath(source, id);
            String feedback = (adding ? "Granted " : "Revoked command ownership of ") + id + " for "
                    + player.getName().getString() + (held && !adding ? " (other owners retain the path)" : "");
            context.getSource().sendSuccess(() -> Component.literal(feedback), true);
        }
        return changes;
    }

    private static int listPaths(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        int count = 0;
        for (ServerPlayer player : EntityArgument.getPlayers(context, "target")) {
            OriginSource source = getSource(player, context.getSource());
            if (source == null) continue;
            Collection<Identifier> paths = AscensionOriginSourceHelper.getPaths(source);
            context.getSource().sendSystemMessage(Component.literal("=== " + player.getName().getString() + " (" + paths.size() + " paths) ==="));
            for (Identifier id : paths.stream().sorted(Comparator.comparing(Identifier::toString)).toList()) {
                PathInstance<?> data = AscensionOriginSourceHelper.getPathInstance(source, id);
                if (data == null) continue;
                context.getSource().sendSystemMessage(Component.literal(
                        id + " : " + data.getCurrentMajorRealm() + "." + data.getCurrentMinorRealm()
                                + " | owners: " + AscensionOriginSourceHelper.getPathOwners(source, id)));
            }
            count++;
        }
        return count;
    }

    private static int showPath(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Identifier id = IdentifierArgument.getId(context, "path");
        Path<?> path = CoreRegistries.safeAccess(CoreRegistries.PATH_REGISTRY, id, context.getSource().registryAccess());
        if (path == null) {
            context.getSource().sendFailure(Component.literal("Unknown path " + id));
            return 0;
        }
        int shown = 0;
        for (ServerPlayer player : EntityArgument.getPlayers(context, "target")) {
            OriginSource source = getSource(player, context.getSource());
            if (source == null) continue;
            PathInstance<?> data = AscensionOriginSourceHelper.getPathInstance(source, id);
            if (data == null) {
                context.getSource().sendSystemMessage(Component.literal(player.getName().getString() + " does not have " + id));
                continue;
            }
            context.getSource().sendSystemMessage(Component.literal("=== " + player.getName().getString() + ": " + id + " ==="));
            context.getSource().sendSystemMessage(Component.literal("Realm: ")
                    .append(path.getRealmName(data.getCurrentMajorRealm(), data.getCurrentMinorRealm())));
            double maxProgress = data.getMaxProgress();
            context.getSource().sendSystemMessage(Component.literal("Progress: " + data.getProgress() + "/" + maxProgress));
            context.getSource().sendSystemMessage(Component.literal("Owners: " + AscensionOriginSourceHelper.getPathOwners(source, id)));
            shown++;
        }
        return shown;
    }

    private static int setRealm(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Identifier id = IdentifierArgument.getId(context, "path");
        int major = IntegerArgumentType.getInteger(context, "majorRealm");
        int minor = IntegerArgumentType.getInteger(context, "minorRealm");
        int percent;
        try {
            percent = IntegerArgumentType.getInteger(context, "progress");
        } catch (IllegalArgumentException ignored) {
            percent = 0;
        }
        Path<?> path = CoreRegistries.safeAccess(CoreRegistries.PATH_REGISTRY, id, context.getSource().registryAccess());
        if (path == null) {
            context.getSource().sendFailure(Component.literal("Unknown path " + id));
            return 0;
        }
        if (major >= path.getMaxMajorRealm() || minor > path.getMaxMinorRealm(major)) {
            context.getSource().sendFailure(Component.literal("Invalid realm " + major + "." + minor + " for " + id));
            return 0;
        }
        int updated = 0;
        for (ServerPlayer player : EntityArgument.getPlayers(context, "target")) {
            OriginSource source = getSource(player, context.getSource());
            if (source == null) continue;
            PathInstance<?> data = AscensionOriginSourceHelper.getPathInstance(source, id);
            if (data == null) {
                context.getSource().sendFailure(Component.literal(player.getName().getString() + " doesn't own path " + id));
                continue;
            }
            if (data.getData() instanceof SimplePathData simpleData) {
                try {
                    data.handleRealmChange(Realm.of(major, minor), source);
                    simpleData.setProgressFromCommand(data.getMaxProgress() * percent / 100.0D, data.getMaxProgress());
                    AscensionOriginSourceHelper.markPathDirty(source, id);
                    String message = "Set " + player.getName().getString() + "'s " + id + " to "
                            + data.getCurrentMajorRealm() + "." + data.getCurrentMinorRealm() + " (" + percent + "% progress)";
                    context.getSource().sendSuccess(() -> Component.literal(message), true);
                    updated++;
                } catch (RuntimeException exception) {
                    context.getSource().sendFailure(Component.literal("Could not set " + id + " for "
                            + player.getName().getString() + ": " + exception.getMessage()));
                }
            } else {
                context.getSource().sendFailure(Component.literal("Path " + id + " does not support direct progress assignment"));
            }
        }
        return updated;
    }

    private static int toggleSuppressed(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return changeSuppression(context, null);
    }

    private static int setSuppressed(CommandContext<CommandSourceStack> context, boolean suppressed) throws CommandSyntaxException {
        return changeSuppression(context, suppressed);
    }

    private static int changeSuppression(CommandContext<CommandSourceStack> context, Boolean state) throws CommandSyntaxException {
        int updated = 0;
        for (ServerPlayer player : EntityArgument.getPlayers(context, "target")) {
            AscensionEntityDataProvider provider = player.getCapability(CoreCapabilities.ASCENSION_ENTITY_DATA_PROVIDER_CAPABILITY);
            if (provider == null || provider.getData() == null) continue;
            boolean suppressed = state == null ? !provider.getData().isCultivationSuppressed() : state;
            provider.getData().setCultivationSuppressed(suppressed);
            context.getSource().sendSuccess(() -> Component.literal(player.getName().getString()
                    + " cultivation suppressed: " + suppressed), true);
            updated++;
        }
        return updated;
    }
}
