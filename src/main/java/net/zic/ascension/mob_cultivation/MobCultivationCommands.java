package net.zic.ascension.mob_cultivation;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.zic.ascension.api.ascension.core.CoreRegistries;

import java.util.Collection;

public final class MobCultivationCommands {
    private MobCultivationCommands() {
    }

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("mob")
                .then(buildInspect())
                .then(buildReroll())
                .then(buildSet())
                .then(buildSummon())
                .then(buildDebugNames());
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildInspect() {
        return Commands.literal("inspect")
                .then(Commands.argument("target", EntityArgument.entity())
                        .executes(MobCultivationCommands::inspect));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildReroll() {
        return Commands.literal("reroll")
                .then(Commands.argument("targets", EntityArgument.entities())
                        .executes(MobCultivationCommands::reroll));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildSet() {
        var progress = Commands.argument(
                        "progressPercent",
                        DoubleArgumentType.doubleArg(0.0D, 100.0D)
                )
                .executes(context -> set(
                        context,
                        DoubleArgumentType.getDouble(context, "progressPercent")
                ));
        var minorRealm = Commands.argument("minorRealm", IntegerArgumentType.integer(0))
                .executes(context -> set(context, 0.0D))
                .then(progress);
        var majorRealm = Commands.argument("majorRealm", IntegerArgumentType.integer(0))
                .then(minorRealm);
        var path = Commands.argument("path", IdentifierArgument.id())
                .suggests(MobCultivationCommands::suggestFoundationPaths)
                .then(majorRealm);
        var targets = Commands.argument("targets", EntityArgument.entities())
                .then(path);
        return Commands.literal("set").then(targets);
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildSummon() {
        var progress = Commands.argument(
                        "progressPercent",
                        DoubleArgumentType.doubleArg(0.0D, 100.0D)
                )
                .executes(context -> summon(
                        context,
                        DoubleArgumentType.getDouble(context, "progressPercent")
                ));
        var minorRealm = Commands.argument("minorRealm", IntegerArgumentType.integer(0))
                .executes(context -> summon(context, 0.0D))
                .then(progress);
        var majorRealm = Commands.argument("majorRealm", IntegerArgumentType.integer(0))
                .then(minorRealm);
        var path = Commands.argument("path", IdentifierArgument.id())
                .suggests(MobCultivationCommands::suggestFoundationPaths)
                .then(majorRealm);
        var entity = Commands.argument("entity", IdentifierArgument.id())
                .suggests((context, builder) -> SharedSuggestionProvider.suggestResource(
                        BuiltInRegistries.ENTITY_TYPE.keySet(),
                        builder
                ))
                .then(path);
        return Commands.literal("summon").then(entity);
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildDebugNames() {
        return Commands.literal("debug_names")
                .then(Commands.argument("enabled", BoolArgumentType.bool())
                        .executes(MobCultivationCommands::setDebugNames));
    }

    private static java.util.concurrent.CompletableFuture<com.mojang.brigadier.suggestion.Suggestions>
    suggestFoundationPaths(CommandContext<CommandSourceStack> context, com.mojang.brigadier.suggestion.SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggestResource(
                CoreRegistries.PATH_REGISTRY.get(context.getSource().registryAccess())
                        .keySet()
                        .stream()
                        .filter(pathId -> isFoundationPath(pathId, context.getSource().registryAccess())),
                builder
        );
    }

    private static int inspect(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Entity entity = EntityArgument.getEntity(context, "target");
        if (!(entity instanceof Mob mob)) {
            context.getSource().sendFailure(Component.literal("The selected entity is not a mob."));
            return 0;
        }

        MobCultivationManager.initialize(mob);
        context.getSource().sendSuccess(() -> MobCultivationManager.describe(mob), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int reroll(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Collection<? extends Entity> entities = EntityArgument.getEntities(context, "targets");
        int changed = 0;

        for (Entity entity : entities) {
            if (!(entity instanceof Mob mob)) {
                continue;
            }
            MobCultivationManager.reroll(mob);
            changed++;
        }

        int result = changed;
        context.getSource().sendSuccess(
                () -> Component.literal("Rerolled cultivation for " + result + " mob(s)."),
                true
        );
        return changed;
    }

    private static int set(CommandContext<CommandSourceStack> context, double progressPercentage)
            throws CommandSyntaxException {
        Collection<? extends Entity> entities = EntityArgument.getEntities(context, "targets");
        Identifier path = IdentifierArgument.getId(context, "path");
        int majorRealm = IntegerArgumentType.getInteger(context, "majorRealm");
        int minorRealm = IntegerArgumentType.getInteger(context, "minorRealm");

        if (!isFoundationPath(path, context.getSource().registryAccess())) {
            context.getSource().sendFailure(Component.literal("Unknown or non-foundation path: " + path));
            return 0;
        }

        int changed = 0;
        for (Entity entity : entities) {
            if (entity instanceof Mob mob && MobCultivationManager.setCultivation(
                    mob,
                    path,
                    majorRealm,
                    minorRealm,
                    progressPercentage
            )) {
                changed++;
            }
        }

        int result = changed;
        context.getSource().sendSuccess(
                () -> Component.literal("Set cultivation for " + result + " mob(s)."),
                true
        );
        return changed;
    }

    private static int summon(CommandContext<CommandSourceStack> context, double progressPercentage) {
        Identifier entityId = IdentifierArgument.getId(context, "entity");
        Identifier path = IdentifierArgument.getId(context, "path");
        int majorRealm = IntegerArgumentType.getInteger(context, "majorRealm");
        int minorRealm = IntegerArgumentType.getInteger(context, "minorRealm");

        EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.getValue(entityId);
        if (entityType == null || !entityType.canSummon()) {
            context.getSource().sendFailure(Component.literal("Unknown or unsummonable entity type: " + entityId));
            return 0;
        }
        if (!isFoundationPath(path, context.getSource().registryAccess())) {
            context.getSource().sendFailure(Component.literal("Unknown or non-foundation path: " + path));
            return 0;
        }

        ServerLevel level = context.getSource().getLevel();
        var sourcePosition = context.getSource().getPosition();
        BlockPos position = BlockPos.containing(sourcePosition.x, sourcePosition.y, sourcePosition.z);
        Entity spawned = entityType.spawn(level, position, EntitySpawnReason.COMMAND);

        if (!(spawned instanceof Mob mob)) {
            if (spawned != null) {
                spawned.discard();
            }
            context.getSource().sendFailure(Component.literal("The selected entity type is not a mob."));
            return 0;
        }

        if (!MobCultivationManager.setCultivation(
                mob,
                path,
                majorRealm,
                minorRealm,
                progressPercentage
        )) {
            mob.discard();
            context.getSource().sendFailure(Component.literal("Failed to apply mob cultivation."));
            return 0;
        }

        context.getSource().sendSuccess(
                () -> Component.literal("Summoned cultivated ")
                        .append(mob.getType().getDescription())
                        .append(" at ")
                        .append(position.toShortString()),
                true
        );
        return Command.SINGLE_SUCCESS;
    }

    private static int setDebugNames(CommandContext<CommandSourceStack> context) {
        boolean enabled = BoolArgumentType.getBool(context, "enabled");
        MobCultivationManager.setDebugNamesEnabled(enabled);
        context.getSource().sendSuccess(
                () -> Component.literal("Mob cultivation debug names: " + enabled),
                true
        );
        return Command.SINGLE_SUCCESS;
    }

    private static boolean isFoundationPath(Identifier pathId, RegistryAccess registryAccess) {
        return CoreRegistries.safeAccess(
                CoreRegistries.PATH_REGISTRY,
                pathId,
                registryAccess
        ) instanceof FoundationPath;
    }
}
