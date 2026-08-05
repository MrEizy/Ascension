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
import net.minecraft.world.entity.player.Player;
import net.zic.ascension.api.ascension.capabilities.AscensionEntityDataProvider;
import net.zic.ascension.api.ascension.capabilities.CoreCapabilities;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.path.Path;
import net.zic.ascension.api.ascension.core.path.PathInstance;
import net.zic.ascension.api.ascension.core.path.realm.Realm;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.ascension.datapack.TypeRegistries;
import net.zic.ascension.api.rpg_engine.source.OriginSource;


public class CultivationCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("cultivation")
                .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                .then(Commands.literal("set")
                        .then(Commands.argument("target", EntityArgument.players())
                                .then(Commands.argument("path", IdentifierArgument.id())
                                        .suggests((context, builder) ->
                                                SharedSuggestionProvider.suggestResource(
                                                        CoreRegistries.PATH_REGISTRY.get(context.getSource().registryAccess()).keySet()
                                                        ,builder))
                                        .then(Commands.argument("majorRealm", IntegerArgumentType.integer(0, 11))
                                                .then(Commands.argument("minorRealm", IntegerArgumentType.integer(0, 9))
                                                        .executes(CultivationCommand::setRealm)
                                                        .then(Commands.argument("progress", IntegerArgumentType.integer(0, 100))
                                                                .executes(CultivationCommand::setRealm)
                                                        )
                                                )
                                        )
                                )
                        )
                )
                .then(Commands.literal("toggle_suppressed")
                        .then(Commands.argument("target", EntityArgument.players())
                        .executes(CultivationCommand::toggleSuppressed))
                )
                .then(Commands.literal("show")
                        .then(Commands.argument("target",EntityArgument.players())
                                .then(Commands.argument("path", IdentifierArgument.id())
                                .suggests((context, builder) ->
                                        SharedSuggestionProvider.suggestResource(
                                                CoreRegistries.PATH_REGISTRY.get(context.getSource().registryAccess()).keySet()
                                                ,builder))
                                                .executes(CultivationCommand::showPath)
                                )
                        )
                );
        }

    private static int toggleSuppressed(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var players = EntityArgument.getPlayers(context, "target");
        for (ServerPlayer player : players) {
            AscensionEntityDataProvider holder = player.getCapability(CoreCapabilities.ASCENSION_ENTITY_DATA_PROVIDER_CAPABILITY);
            if(holder == null) continue;
            holder.getData(player).setCultivationSuppressed(!holder.getData(player).isCultivationSuppressed());
            player.sendSystemMessage(Component.literal("Cultivation Suppressed : "+holder.getData(player).isCultivationSuppressed()));

        }
        return 1;
    }
    private static int showPath(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var players = EntityArgument.getPlayers(context, "target");
        Identifier id = IdentifierArgument.getId(context, "path");
        Path path = CoreRegistries.safeAccess(CoreRegistries.PATH_REGISTRY,id,context.getSource().registryAccess());
        if(path == null){
            context.getSource().sendFailure(Component.literal("no path :"+id));
            return 0;
        }
        Player player = context.getSource().getPlayer();

        for(ServerPlayer target : players) {
            AscensionEntityDataProvider holder = target.getCapability(CoreCapabilities.ASCENSION_ENTITY_DATA_PROVIDER_CAPABILITY);
            if(holder == null) continue;
            player.sendSystemMessage(Component.literal("==="+target.getDisplayName().getString()+"==="));

            OriginSource source = holder.getData(target).getSource();
            if(!AscensionOriginSourceHelper.hasPath(source,id)){
                player.sendSystemMessage(Component.literal("no path data"));
                continue;
            }
            PathInstance pathInstance = AscensionOriginSourceHelper.getPathInstance(source,id);
            player.sendSystemMessage(Component.literal("realm : ").append(path.getRealmName(pathInstance.getCurrentMajorRealm(),pathInstance.getCurrentMinorRealm())));
            player.sendSystemMessage(Component.literal("progress : "+pathInstance.getProgress()));


        }
        return 1;
    }
        private static int setRealm(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
            var players = EntityArgument.getPlayers(context, "target");
            Identifier pathId = IdentifierArgument.getId(context, "path");
            int majorRealm = IntegerArgumentType.getInteger(context, "majorRealm");
            int minorRealm = IntegerArgumentType.getInteger(context, "minorRealm");
            int progressPercent = 0;
            try {
                progressPercent = IntegerArgumentType.getInteger(context, "progress");
            } catch (IllegalArgumentException e) {

            }
            Path path = CoreRegistries.safeAccess(CoreRegistries.PATH_REGISTRY,pathId,context.getSource().registryAccess());
            if(path == null){
                context.getSource().sendFailure(
                        Component.literal("Unknown path '" + pathId + "'. Use a registered path ID (e.g. ascension:body).")
                );
                return 0;
            }

            int successCount = 0;
            for (ServerPlayer player : players) {
                if (setPlayerRealm(player, pathId, majorRealm, minorRealm, progressPercent, context.getSource())) {
                    successCount++;
                }
            }
            return successCount;


    }

    private static boolean setPlayerRealm(ServerPlayer player, Identifier pathId,
                                                     int newMajorRealm, int newMinorRealm,
                                                     int progressPercent,
                                                     CommandSourceStack source) {
        try {
            AscensionEntityDataProvider holder = player.getCapability(CoreCapabilities.ASCENSION_ENTITY_DATA_PROVIDER_CAPABILITY);
            if(holder == null){
                source.sendFailure(Component.literal(
                        player.getName().getString() + " is missing an entity data holder"));
                return false;
            }

            OriginSource originSource = holder.getData(player).getSource();

            if(originSource == null){
                source.sendFailure(Component.literal(
                        player.getName().getString() +" has no origin source"
                ));
                return false;
            }
            PathInstance data = AscensionOriginSourceHelper.getPathInstance(originSource,pathId);

            if(data == null){
                source.sendFailure(Component.literal(
                        player.getName().getString() +" player has no path "+pathId
                ));
                return false;
            }


            int oldMajor = data.getCurrentMajorRealm();
            int oldMinor = data.getCurrentMinorRealm();
            data.handleRealmChange(Realm.of(newMajorRealm,newMinorRealm),originSource);

            if(progressPercent > 0){
                progressPercent = Math.clamp(progressPercent,0,100);
                data.progressPath(pathId,data.getMaxProgress()*progressPercent/100.0,originSource);
            }
            AscensionOriginSourceHelper.markPathDirty(originSource,pathId);

            String progressStr = (progressPercent >= 0)
                    ? String.format(" with %d%% progress", progressPercent) : "";

            String feedbackToSource = String.format(
                    "Set %s's %s cultivation to realm %d.%d (was %d.%d)%s",
                    player.getName().getString(),
                    pathId,
                    data.getCurrentMajorRealm(), data.getCurrentMinorRealm(),
                    oldMajor, oldMinor,
                    progressStr
            );
            source.sendSuccess(() -> Component.literal(feedbackToSource), true);

            return true;
        }catch(Exception e) {
            source.sendFailure(Component.literal(
                    "Failed to set cultivation for " + player.getName().getString()
                            + ": " + e.getMessage()));
            return false;
        }
    }
}
