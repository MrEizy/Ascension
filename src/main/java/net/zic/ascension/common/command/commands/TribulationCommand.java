package net.zic.ascension.common.command.commands;

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
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.tribulation.TribulationDefinition;
import net.zic.ascension.api.ascension.core.tribulation.TribulationInstance;
import net.zic.ascension.api.ascension.core.tribulation.TribulationManager;
import net.zic.ascension.api.ascension.datapack.TypeRegistries;

import java.util.Map;
import java.util.UUID;

public class TribulationCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("tribulation")
               .then(Commands.literal("spawn")
                        .then(Commands.argument("definition", IdentifierArgument.id())
                                .suggests((context, builder) ->
                                        SharedSuggestionProvider.suggestResource(
                                                CoreRegistries.TRIBULATION_DEFINITION_REGISTRY.get(context.getSource().registryAccess()).keySet(),
                                                builder
                                        )
                                )
                                .then(Commands.argument("target", EntityArgument.players())
                                        .executes(TribulationCommand::spawnTribulation)
                                )
                        )
                )
                .then(Commands.literal("view")
                        .executes(TribulationCommand::viewTribulations)
                )
                .then(Commands.literal("clear")
                        .executes(TribulationCommand::clear)
                );
    }

    private static int spawnTribulation(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Identifier tribulationDefinitionId = IdentifierArgument.getId(context,"definition");
        var players = EntityArgument.getPlayers(context,"target");

        TribulationDefinition definition = CoreRegistries.safeAccess(CoreRegistries.TRIBULATION_DEFINITION_REGISTRY,tribulationDefinitionId,context.getSource().registryAccess());

        if(definition == null){
            context.getSource().sendFailure(
                    Component.literal("no definition "+tribulationDefinitionId)
            );
            return 0;
        }
        for(ServerPlayer player : players){
            TribulationManager.getInstance().triggerTribulation(
                    definition,player
            );
        }

        return 1;
    }

    public static int viewTribulations(CommandContext<CommandSourceStack> context) throws CommandSyntaxException{
        Map< UUID, TribulationInstance> tribulations = TribulationManager.getInstance().getTribulations();
        ServerPlayer cause = context.getSource().getPlayer();
        cause.sendSystemMessage(Component.literal("===Tribulations==="));
        for(Map.Entry<UUID,TribulationInstance> entry : tribulations.entrySet()){
            cause.sendSystemMessage(
                    Component.literal(entry.getKey()+" : (entity: "+entry.getValue().getEntityId()+", tribulation: " +
                            TypeRegistries.TRIBULATION_TYPE_REGISTRY.getKey(entry.getValue().getTribulation().getType()))
            );
        }

        return 1;

    }
    private static int clear(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        TribulationManager.getInstance().getTribulations().clear();
        TribulationManager.getInstance().getEntities().clear();
        TribulationManager.getInstance().setDirty();
        context.getSource().getPlayer().sendSystemMessage(Component.literal("cleared tribulations"));
        return 1;
    }
}
