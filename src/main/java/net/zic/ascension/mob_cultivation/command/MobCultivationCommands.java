package net.zic.ascension.mob_cultivation.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
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
import net.zic.ascension.api.core.CoreRegistries;
import net.zic.ascension.impl.core.path.foundation.FoundationPath;
import net.zic.ascension.mob_cultivation.MobCultivationManager;
import net.zic.ascension.mob_cultivation.generation.MobCultivationEliteTier;
import net.zic.ascension.mob_cultivation.skill.MobCultivationSkillPoolManager;
import net.zic.ascension.mob_cultivation.trait.MobCultivationTraitManager;

import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public final class MobCultivationCommands {
    private MobCultivationCommands() {
    }

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("mob")
                .then(buildInspect())
                .then(buildReroll())
                .then(buildSet())
                .then(buildSummon())
                .then(buildSubPaths())
                .then(buildTraits())
                .then(buildSkillPools())
                .then(buildElite())
                .then(buildFreezeGrowth())
                .then(buildCopy())
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
        var progress = Commands.argument("progressPercent", DoubleArgumentType.doubleArg(0.0D, 100.0D))
                .executes(context -> set(context, DoubleArgumentType.getDouble(context, "progressPercent")));
        var minorRealm = Commands.argument("minorRealm", IntegerArgumentType.integer(0))
                .executes(context -> set(context, 0.0D))
                .then(progress);
        var majorRealm = Commands.argument("majorRealm", IntegerArgumentType.integer(0)).then(minorRealm);
        var path = Commands.argument("path", IdentifierArgument.id())
                .suggests(MobCultivationCommands::suggestFoundationPaths)
                .then(majorRealm);
        return Commands.literal("set").then(Commands.argument("targets", EntityArgument.entities()).then(path));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildSummon() {
        var progress = Commands.argument("progressPercent", DoubleArgumentType.doubleArg(0.0D, 100.0D))
                .executes(context -> summon(context, DoubleArgumentType.getDouble(context, "progressPercent")));
        var minorRealm = Commands.argument("minorRealm", IntegerArgumentType.integer(0))
                .executes(context -> summon(context, 0.0D))
                .then(progress);
        var majorRealm = Commands.argument("majorRealm", IntegerArgumentType.integer(0)).then(minorRealm);
        var path = Commands.argument("path", IdentifierArgument.id())
                .suggests(MobCultivationCommands::suggestFoundationPaths)
                .then(majorRealm);
        var entity = Commands.argument("entity", IdentifierArgument.id())
                .suggests((context, builder) -> SharedSuggestionProvider.suggestResource(
                        BuiltInRegistries.ENTITY_TYPE.keySet(), builder
                ))
                .then(path);
        return Commands.literal("summon").then(entity);
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildSubPaths() {
        var add = Commands.literal("add")
                .then(Commands.argument("targets", EntityArgument.entities())
                        .then(Commands.argument("path", IdentifierArgument.id())
                                .suggests(MobCultivationCommands::suggestSubPaths)
                                .executes(context -> changeSubPath(context, true))));
        var remove = Commands.literal("remove")
                .then(Commands.argument("targets", EntityArgument.entities())
                        .then(Commands.argument("path", IdentifierArgument.id())
                                .suggests(MobCultivationCommands::suggestSubPaths)
                                .executes(context -> changeSubPath(context, false))));
        var reroll = Commands.literal("reroll")
                .then(Commands.argument("targets", EntityArgument.entities())
                        .executes(MobCultivationCommands::rerollSubPaths));
        return Commands.literal("subpath").then(add).then(remove).then(reroll);
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildTraits() {
        var add = Commands.literal("add")
                .then(Commands.argument("targets", EntityArgument.entities())
                        .then(Commands.argument("trait", IdentifierArgument.id())
                                .suggests(MobCultivationCommands::suggestTraits)
                                .executes(context -> changeTrait(context, true))));
        var remove = Commands.literal("remove")
                .then(Commands.argument("targets", EntityArgument.entities())
                        .then(Commands.argument("trait", IdentifierArgument.id())
                                .suggests(MobCultivationCommands::suggestTraits)
                                .executes(context -> changeTrait(context, false))));
        return Commands.literal("trait").then(add).then(remove);
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildSkillPools() {
        var add = Commands.literal("add")
                .then(Commands.argument("targets", EntityArgument.entities())
                        .then(Commands.argument("pool", IdentifierArgument.id())
                                .suggests(MobCultivationCommands::suggestSkillPools)
                                .executes(context -> changeSkillPool(context, true))));
        var remove = Commands.literal("remove")
                .then(Commands.argument("targets", EntityArgument.entities())
                        .then(Commands.argument("pool", IdentifierArgument.id())
                                .suggests(MobCultivationCommands::suggestSkillPools)
                                .executes(context -> changeSkillPool(context, false))));
        return Commands.literal("skill_pool").then(add).then(remove);
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildElite() {
        return Commands.literal("elite")
                .then(Commands.argument("targets", EntityArgument.entities())
                        .then(Commands.argument("tier", StringArgumentType.word())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                        java.util.Arrays.stream(MobCultivationEliteTier.values())
                                                .map(value -> value.name().toLowerCase(Locale.ROOT)),
                                        builder
                                ))
                                .executes(MobCultivationCommands::setElite)));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildFreezeGrowth() {
        return Commands.literal("freeze_growth")
                .then(Commands.argument("targets", EntityArgument.entities())
                        .then(Commands.argument("frozen", BoolArgumentType.bool())
                                .executes(MobCultivationCommands::freezeGrowth)));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildCopy() {
        return Commands.literal("copy")
                .then(Commands.argument("source", EntityArgument.entity())
                        .then(Commands.argument("targets", EntityArgument.entities())
                                .executes(MobCultivationCommands::copy)));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildDebugNames() {
        return Commands.literal("debug_names")
                .then(Commands.argument("enabled", BoolArgumentType.bool())
                        .executes(MobCultivationCommands::setDebugNames));
    }

    private static CompletableFuture<Suggestions> suggestFoundationPaths(
            CommandContext<CommandSourceStack> context,
            SuggestionsBuilder builder
    ) {
        return SharedSuggestionProvider.suggestResource(
                CoreRegistries.PATH_REGISTRY.get(context.getSource().registryAccess())
                        .keySet().stream()
                        .filter(pathId -> isFoundationPath(pathId, context.getSource().registryAccess())),
                builder
        );
    }

    private static CompletableFuture<Suggestions> suggestSubPaths(
            CommandContext<CommandSourceStack> context,
            SuggestionsBuilder builder
    ) {
        return SharedSuggestionProvider.suggestResource(
                CoreRegistries.PATH_REGISTRY.get(context.getSource().registryAccess())
                        .keySet().stream()
                        .filter(pathId -> !isFoundationPath(pathId, context.getSource().registryAccess())),
                builder
        );
    }

    private static CompletableFuture<Suggestions> suggestTraits(
            CommandContext<CommandSourceStack> context,
            SuggestionsBuilder builder
    ) {
        return SharedSuggestionProvider.suggestResource(MobCultivationTraitManager.ids(), builder);
    }

    private static CompletableFuture<Suggestions> suggestSkillPools(
            CommandContext<CommandSourceStack> context,
            SuggestionsBuilder builder
    ) {
        return SharedSuggestionProvider.suggestResource(MobCultivationSkillPoolManager.ids(), builder);
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
        int changed = 0;
        for (Entity entity : EntityArgument.getEntities(context, "targets")) {
            if (entity instanceof Mob mob) {
                MobCultivationManager.reroll(mob);
                changed++;
            }
        }
        return successCount(context, "Rerolled cultivation", changed);
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
                    mob, path, majorRealm, minorRealm, progressPercentage
            )) changed++;
        }
        return successCount(context, "Set cultivation", changed);
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
            if (spawned != null) spawned.discard();
            context.getSource().sendFailure(Component.literal("The selected entity type is not a mob."));
            return 0;
        }
        if (!MobCultivationManager.setCultivation(mob, path, majorRealm, minorRealm, progressPercentage)) {
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

    private static int changeSubPath(CommandContext<CommandSourceStack> context, boolean add)
            throws CommandSyntaxException {
        Identifier path = IdentifierArgument.getId(context, "path");
        int changed = 0;
        for (Entity entity : EntityArgument.getEntities(context, "targets")) {
            if (!(entity instanceof Mob mob)) continue;
            boolean result = add
                    ? MobCultivationManager.addSubPath(mob, path)
                    : MobCultivationManager.removeSubPath(mob, path);
            if (result) changed++;
        }
        return successCount(context, add ? "Added sub-path" : "Removed sub-path", changed);
    }

    private static int rerollSubPaths(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        int changed = 0;
        for (Entity entity : EntityArgument.getEntities(context, "targets")) {
            if (entity instanceof Mob mob) {
                MobCultivationManager.rerollSubPaths(mob);
                changed++;
            }
        }
        return successCount(context, "Rerolled sub-paths", changed);
    }

    private static int changeTrait(CommandContext<CommandSourceStack> context, boolean add)
            throws CommandSyntaxException {
        Identifier trait = IdentifierArgument.getId(context, "trait");
        int changed = 0;
        for (Entity entity : EntityArgument.getEntities(context, "targets")) {
            if (!(entity instanceof Mob mob)) continue;
            boolean result = add
                    ? MobCultivationManager.addTrait(mob, trait)
                    : MobCultivationManager.removeTrait(mob, trait);
            if (result) changed++;
        }
        return successCount(context, add ? "Added trait" : "Removed trait", changed);
    }

    private static int changeSkillPool(CommandContext<CommandSourceStack> context, boolean add)
            throws CommandSyntaxException {
        Identifier pool = IdentifierArgument.getId(context, "pool");
        int changed = 0;
        for (Entity entity : EntityArgument.getEntities(context, "targets")) {
            if (!(entity instanceof Mob mob)) continue;
            boolean result = add
                    ? MobCultivationManager.addSkillPool(mob, pool)
                    : MobCultivationManager.removeSkillPool(mob, pool);
            if (result) changed++;
        }
        return successCount(context, add ? "Added skill pool" : "Removed skill pool", changed);
    }

    private static int setElite(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        MobCultivationEliteTier tier = MobCultivationEliteTier.parse(StringArgumentType.getString(context, "tier"));
        int changed = 0;
        for (Entity entity : EntityArgument.getEntities(context, "targets")) {
            if (entity instanceof Mob mob && MobCultivationManager.setEliteTier(mob, tier)) changed++;
        }
        return successCount(context, "Set elite tier to " + tier.name().toLowerCase(Locale.ROOT), changed);
    }

    private static int freezeGrowth(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        boolean frozen = BoolArgumentType.getBool(context, "frozen");
        int changed = 0;
        for (Entity entity : EntityArgument.getEntities(context, "targets")) {
            if (entity instanceof Mob mob) {
                MobCultivationManager.setGrowthFrozen(mob, frozen);
                changed++;
            }
        }
        return successCount(context, "Set growth frozen to " + frozen, changed);
    }

    private static int copy(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Entity sourceEntity = EntityArgument.getEntity(context, "source");
        if (!(sourceEntity instanceof Mob sourceMob)) {
            context.getSource().sendFailure(Component.literal("The source entity is not a mob."));
            return 0;
        }
        int changed = 0;
        for (Entity entity : EntityArgument.getEntities(context, "targets")) {
            if (entity instanceof Mob target && target != sourceMob
                    && MobCultivationManager.copyCultivation(sourceMob, target)) {
                changed++;
            }
        }
        return successCount(context, "Copied cultivation", changed);
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

    private static int successCount(CommandContext<CommandSourceStack> context, String action, int count) {
        context.getSource().sendSuccess(
                () -> Component.literal(action + " for " + count + " mob(s)."),
                true
        );
        return count;
    }

    private static boolean isFoundationPath(Identifier pathId, RegistryAccess registryAccess) {
        return CoreRegistries.safeAccess(CoreRegistries.PATH_REGISTRY, pathId, registryAccess) instanceof FoundationPath;
    }
}
