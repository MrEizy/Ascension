package net.zic.ascension.common.command.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.tribulation.DimensionChangeBehaviour;
import net.zic.ascension.api.ascension.core.tribulation.TribulationInstance;
import net.zic.ascension.api.ascension.core.tribulation.TribulationManager;

import java.util.UUID;

public class TribulationCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("tribulation")
               .then(Commands.literal("spawn")
                        .then(Commands.argument("tribulation", IdentifierArgument.id())
                                .suggests((context, builder) ->
                                        SharedSuggestionProvider.suggestResource(
                                                CoreRegistries.TRIBULATION_REGISTRY.get(context.getSource().registryAccess()).keySet(),
                                                builder
                                        )
                                )
                                .then(Commands.argument("target", EntityArgument.players())
                                        .executes(context -> spawnTribulation(context, DimensionChangeBehaviour.STAY))
                                        .then(Commands.literal("follow")
                                                .executes(context -> spawnTribulation(context, DimensionChangeBehaviour.FOLLOW))
                                        )
                                )
                        )
                )
                .then(Commands.literal("view")
                        .executes(TribulationCommand::viewTribulations)
                )
                .then(Commands.literal("cancel")
                        .then(Commands.argument("id", UuidArgument.uuid())
                                .executes(TribulationCommand::cancel)
                        )
                )
                .then(Commands.literal("clear")
                        .executes(TribulationCommand::clear)
                );
    }

    private static TribulationManager getManager(CommandContext<CommandSourceStack> context) {
        return TribulationManager.get(context.getSource().getServer());
    }

    private static int spawnTribulation(CommandContext<CommandSourceStack> context, DimensionChangeBehaviour behaviour) throws CommandSyntaxException {
        Identifier tribulation = IdentifierArgument.getId(context,"tribulation");
        var players = EntityArgument.getPlayers(context,"target");

        if(CoreRegistries.safeAccess(CoreRegistries.TRIBULATION_REGISTRY, tribulation, context.getSource().registryAccess()) == null){
            context.getSource().sendFailure(Component.literal("no tribulation "+tribulation));
            return 0;
        }
        for(ServerPlayer player : players){
            getManager(context).create(tribulation).targeting(player, behaviour).start();
        }

        return 1;
    }

    public static int viewTribulations(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSystemMessage(Component.literal("===Tribulations==="));
        for(TribulationInstance instance : getManager(context).getAll()){
            context.getSource().sendSystemMessage(Component.literal(
                    instance.getId() + " : (tribulation: " + instance.getTribulationId()
                            + ", state: " + instance.getState().getSerializedName()
                            + ", age: " + instance.getAge()
                            + ", dimension: " + instance.getDimension().identifier()
                            + ", pos: " + instance.getBlockPosition().toShortString()
                            + (instance.hasTarget() ? ", target: " + instance.getTarget().getEntityId() : "") + ")"
            ));
        }
        return 1;
    }

    private static int cancel(CommandContext<CommandSourceStack> context) {
        UUID id = UuidArgument.getUuid(context, "id");
        if(!getManager(context).has(id)){
            context.getSource().sendFailure(Component.literal("no tribulation with id "+id));
            return 0;
        }
        getManager(context).cancel(id);
        context.getSource().sendSystemMessage(Component.literal("cancelled tribulation "+id));
        return 1;
    }

    private static int clear(CommandContext<CommandSourceStack> context) {
        getManager(context).discardAll();
        context.getSource().sendSystemMessage(Component.literal("cleared tribulations"));
        return 1;
    }
}
