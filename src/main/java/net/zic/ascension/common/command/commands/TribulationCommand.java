package net.zic.ascension.common.command.commands;

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
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.path.PathInstance;
import net.zic.ascension.api.ascension.core.path.realm.TribulationMode;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.ascension.core.tribulation.DimensionChangeBehaviour;
import net.zic.ascension.api.ascension.core.tribulation.TribulationInstance;
import net.zic.ascension.api.ascension.core.tribulation.TribulationManager;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

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
                )
                .then(Commands.literal("trigger")
                        .then(Commands.argument("target", EntityArgument.players())
                                .then(Commands.argument("path", IdentifierArgument.id())
                                        .suggests(TribulationCommand::suggestHeldPaths)
                                        .then(Commands.argument("tribulation", IdentifierArgument.id())
                                                .suggests(TribulationCommand::suggestCurrentTribulations)
                                                .executes(TribulationCommand::trigger)
                                        )
                                )
                        )
                );
    }

    private static Collection<PathInstance<?>> getTargetPaths(CommandContext<CommandSourceStack> context, boolean onlyArgumentPath) {
        List<PathInstance<?>> paths = new ArrayList<>();
        try {
            for (ServerPlayer player : EntityArgument.getPlayers(context, "target")) {
                OriginSource source = AscensionOriginSourceHelper.getEntitySource(player);
                if (!onlyArgumentPath) {
                    paths.addAll(AscensionOriginSourceHelper.getPathInstances(source));
                    continue;
                }
                PathInstance<?> instance = AscensionOriginSourceHelper.getPathInstance(source, IdentifierArgument.getId(context, "path"));
                if (instance != null) paths.add(instance);
            }
        } catch (CommandSyntaxException | IllegalArgumentException e) {
            return List.of();
        }
        return paths;
    }

    private static CompletableFuture<Suggestions> suggestHeldPaths(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggestResource(getTargetPaths(context, false).stream().map(PathInstance::getPathId), builder);
    }

    private static CompletableFuture<Suggestions> suggestCurrentTribulations(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggestResource(getTargetPaths(context, true).stream()
                .map(PathInstance::getCurrentTribulationId)
                .filter(Objects::nonNull), builder);
    }

    private static int trigger(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Identifier path = IdentifierArgument.getId(context, "path");
        Identifier tribulation = IdentifierArgument.getId(context, "tribulation");
        int triggered = 0;
        for (ServerPlayer player : EntityArgument.getPlayers(context, "target")) {
            OriginSource source = AscensionOriginSourceHelper.getEntitySource(player);
            TribulationInstance<?> started = AscensionOriginSourceHelper.triggerPathTribulation(source, path, tribulation, player, null);
            if (started != null) {
                triggered++;
                context.getSource().sendSuccess(() -> Component.literal("Triggered " + tribulation + " (" + started.getId() + ") for ").append(player.getName()), true);
                continue;
            }
            context.getSource().sendFailure(Component.literal("Could not trigger " + tribulation + " for ").append(player.getName())
                    .append(Component.literal(": " + failureReason(AscensionOriginSourceHelper.getPathInstance(source, path), tribulation))));
        }
        return triggered;
    }

    private static String failureReason(@Nullable PathInstance<?> instance, Identifier tribulation) {
        if (instance == null) return "path is not held";
        Identifier current = instance.getCurrentTribulationId();
        if (current == null) return "the current realm has no tribulation";
        if (!current.equals(tribulation)) return "the current realm's tribulation is " + current;
        if (instance.getTribulationMode() != TribulationMode.MANUAL) return "the tribulation is not manual";
        if (instance.getActiveTribulation() != null) return "a tribulation is already running";
        return "the realm is not ready to break through";
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
        for(TribulationInstance<?> instance : getManager(context).getAll()){
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
