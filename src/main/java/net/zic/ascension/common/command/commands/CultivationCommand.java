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
import net.zic.ascension.api.ascension.core.path.PathData;
import net.zic.ascension.api.ascension.core.path.Realm;
import net.zic.ascension.api.ascension.core.source.OriginSource;
import net.zic.ascension.api.ascension.datapack.TypeRegistries;
import net.zic.ascension.impl.core.path.foundation.FoundationPath;
import net.zic.ascension.impl.core.path.foundation.FoundationPathData;

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
        Identifier path = IdentifierArgument.getId(context, "path");
        Path pathInstance = CoreRegistries.safeAccess(CoreRegistries.PATH_REGISTRY,path,context.getSource().registryAccess());
        if(pathInstance == null){
            context.getSource().sendFailure(Component.literal("no path :"+path));
            return 0;
        }
        Player player = context.getSource().getPlayer();

        for(ServerPlayer target : players) {
            AscensionEntityDataProvider holder = target.getCapability(CoreCapabilities.ASCENSION_ENTITY_DATA_PROVIDER_CAPABILITY);
            if(holder == null) continue;
            player.sendSystemMessage(Component.literal("==="+target.getDisplayName().getString()+"==="));

            OriginSource source = holder.getData(target).getSource();
            if(!source.hasPath(path)){
                player.sendSystemMessage(Component.literal("no path data"));
                continue;
            }
            PathData pathData = source.getPathData(path);
            player.sendSystemMessage(Component.literal("realm : ").append(pathData.getRealmName(pathData.getMajorRealm(),pathData.getMinorRealm(),source.getRegistryAccess())));
            player.sendSystemMessage(Component.literal("progress : "+pathData.getProgress()));
            player.sendSystemMessage(Component.literal("technique : "+pathData.getCurrentTechnique()));
            if(pathData instanceof FoundationPathData foundationPathData && pathInstance instanceof FoundationPath foundationPath){
                player.sendSystemMessage(Component.literal("Foundation : ").append(
                        foundationPath.getFoundationRealmName(
                                foundationPathData.getMajorRealm(),
                                foundationPathData.getFoundationRealm(foundationPathData.getMajorRealm())
                        )));
                System.out.println(foundationPathData.getFoundationRealm(foundationPathData.getMajorRealm()));
                player.sendSystemMessage(Component.literal(
                        "Foundation Progress : "+
                                foundationPathData.getFoundationRealmProgress(foundationPathData.getMajorRealm())
                ));
            }
            player.sendSystemMessage(Component.literal("Tribulations:"));
            for(Realm realm : pathData.getCompletedTribulationRealms()){
                Identifier id = TypeRegistries.TRIBULATION_TYPE_REGISTRY.getKey(pathData.getCompletedTribulationData(realm.majorRealm(),realm.minorRealm()).getType());

                player.sendSystemMessage(Component.literal(realm.toString()).append(" "+id));
            }
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
            PathData data = originSource.getPathData(pathId);

            if(data == null){
                source.sendFailure(Component.literal(
                        player.getName().getString() +" player has no path "+pathId
                ));
                return false;
            }

            if(data.getCurrentTechnique() == null){
                source.sendFailure(Component.literal(
                        player.getName().getString() + " has no technique"
                ));
                return false;
            }
            int oldMajor = data.getMajorRealm();
            int oldMinor = data.getMinorRealm();
            data.handleRealmChange(originSource,newMajorRealm,newMinorRealm);

            if(progressPercent > 0){
                progressPercent = Math.clamp(progressPercent,0,100);
                data.setProgress(data.getMaxProgress(data.getMajorRealm(),data.getMinorRealm(),originSource.getRegistryAccess())*progressPercent/100.0);
            }else{
                data.setProgress(0);
            }
            originSource.markPathDirty(pathId);

            String progressStr = (progressPercent >= 0)
                    ? String.format(" with %d%% progress", progressPercent) : "";

            String feedbackToSource = String.format(
                    "Set %s's %s cultivation to realm %d.%d (was %d.%d)%s",
                    player.getName().getString(),
                    pathId,
                    data.getMajorRealm(), data.getMinorRealm(),
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
