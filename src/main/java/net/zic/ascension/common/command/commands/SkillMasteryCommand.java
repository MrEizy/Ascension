package net.zic.ascension.common.command.commands;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
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
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.skill.SkillMasteryRank;
import net.zic.ascension.api.ascension.core.skill.SkillProgressionResolver;
import net.zic.ascension.api.ascension.core.skill.SkillProgressionService;
import net.zic.ascension.api.ascension.core.skill.SkillProgressionSnapshot;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.impl.core.skill.castable.ActiveSkill;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public final class SkillMasteryCommand {
    private static final Identifier COMMAND_CAP = AscensionCraft.prefix("command/mastery");

    private SkillMasteryCommand() {
    }

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        LiteralArgumentBuilder<CommandSourceStack> mastery = Commands.literal("mastery");
        mastery.then(Commands.literal("get")
                .then(skillArgument().executes(context -> execute(context, Operation.GET, false)))
                .then(targetedSkillArgument(Operation.GET)));
        mastery.then(Commands.literal("set")
                .then(skillArgument().then(rankArgument(false)))
                .then(targetedSkillArgument(Operation.SET)));
        mastery.then(experienceCommand("addXp"));
        mastery.then(experienceCommand("addxp"));
        mastery.then(clearOverrideCommand("clearOverride"));
        mastery.then(clearOverrideCommand("clearoverride"));
        return mastery;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> experienceCommand(String label) {
        return Commands.literal(label)
                .then(skillArgument().then(Commands.argument("amount", DoubleArgumentType.doubleArg(0.0D))
                        .executes(context -> execute(context, Operation.ADD_XP, false))))
                .then(targetedSkillArgument(Operation.ADD_XP));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> clearOverrideCommand(String label) {
        return Commands.literal(label)
                .then(skillArgument().executes(context -> execute(context, Operation.CLEAR, false)))
                .then(targetedSkillArgument(Operation.CLEAR));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> targetedSkillArgument(Operation op) {
        return Commands.literal("target")
                .then(Commands.argument("players", EntityArgument.players())
                        .then(switch (op) {
                            case GET, CLEAR -> skillArgument().executes(context -> execute(context, op, true));
                            case SET -> skillArgument().then(rankArgument(true));
                            case ADD_XP -> skillArgument().then(Commands.argument("amount", DoubleArgumentType.doubleArg(0.0D))
                                    .executes(context -> execute(context, op, true)));
                        }));
    }

    private static RequiredArgumentBuilder<CommandSourceStack, String> rankArgument(boolean targeted) {
        return Commands.argument("rank", StringArgumentType.word())
                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                        Arrays.stream(SkillMasteryRank.values()).map(SkillMasteryRank::getSerializedName).toList(), builder))
                .executes(context -> execute(context, Operation.SET, targeted));
    }

    private static RequiredArgumentBuilder<CommandSourceStack, Identifier> skillArgument() {
        return Commands.argument("skill", IdentifierArgument.id())
                .suggests((context, builder) -> {
                    Set<Identifier> skills;
                    ServerPlayer player = context.getSource().getPlayer();
                    if (player == null) {
                        skills = CoreRegistries.SKILL_REGISTRY.get(context.getSource().registryAccess()).keySet().stream()
                                .filter(id -> CoreRegistries.safeAccess(CoreRegistries.SKILL_REGISTRY, id,
                                        context.getSource().registryAccess()) instanceof ActiveSkill)
                                .collect(Collectors.toSet());
                    } else {
                        OriginSource source = AscensionOriginSourceHelper.getEntitySource(player);
                        skills = source == null ? Set.of() : AscensionOriginSourceHelper.getSkills(source).stream()
                                .filter(id -> CoreRegistries.safeAccess(CoreRegistries.SKILL_REGISTRY, id,
                                        context.getSource().registryAccess()) instanceof ActiveSkill)
                                .collect(Collectors.toSet());
                    }
                    return SharedSuggestionProvider.suggestResource(skills, builder);
                });
    }

    private enum Operation { GET, SET, ADD_XP, CLEAR }

    private static int execute(CommandContext<CommandSourceStack> context, Operation operation, boolean targeted)
            throws CommandSyntaxException {
        Collection<ServerPlayer> players = targeted ? EntityArgument.getPlayers(context, "players")
                : java.util.List.of(context.getSource().getPlayerOrException());
        Identifier id = IdentifierArgument.getId(context, "skill");
        SkillMasteryRank rank = null;
        if (operation == Operation.SET) {
            rank = SkillMasteryRank.fromName(StringArgumentType.getString(context, "rank"));
            if (rank == null) {
                context.getSource().sendFailure(Component.literal("Unknown mastery rank"));
                return 0;
            }
        }
        double xp = operation == Operation.ADD_XP ? DoubleArgumentType.getDouble(context, "amount") : 0.0D;
        int successful = 0;
        for (ServerPlayer player : players) {
            Resolved resolved = resolve(player, id);
            if (resolved == null) {
                context.getSource().sendFailure(Component.literal(player.getName().getString()
                        + " does not own active skill " + id));
                continue;
            }
            switch (operation) {
                case GET -> { }
                case SET -> {
                    SkillProgressionService.setCap(resolved.source(), id, COMMAND_CAP, rank.progression());
                    SkillProgressionService.setTrainedProgression(resolved.source(), id, rank.progression());
                }
                case ADD_XP -> SkillProgressionService.addExperience(resolved.source(), id, xp);
                case CLEAR -> SkillProgressionService.removeCap(resolved.source(), id, COMMAND_CAP);
            }
            sendSnapshot(context.getSource(), player, id, resolved.skill(), resolved.source());
            successful++;
        }
        return successful;
    }

    private static Resolved resolve(ServerPlayer player, Identifier skillId) {
        OriginSource source = AscensionOriginSourceHelper.getEntitySource(player);
        if (source == null || !AscensionOriginSourceHelper.hasSkill(source, skillId)) return null;
        if (!(CoreRegistries.safeAccess(CoreRegistries.SKILL_REGISTRY, skillId, player.registryAccess())
                instanceof ActiveSkill skill)) return null;
        return new Resolved(source, skill);
    }

    private static void sendSnapshot(CommandSourceStack source, ServerPlayer player, Identifier id,
                                     ActiveSkill skill, OriginSource origin) {
        SkillProgressionSnapshot snapshot = SkillProgressionResolver.resolve(origin, id);
        SkillMasteryRank trained = SkillMasteryRank.fromProgression(snapshot.trainedProgression());
        SkillMasteryRank cap = SkillMasteryRank.fromProgression(snapshot.accessibleCap());
        SkillMasteryRank effective = SkillMasteryRank.fromProgression(snapshot.effectiveProgression());
        String xp = trained == SkillMasteryRank.TRANSCENDENCE
                || snapshot.trainedProgression() >= snapshot.accessibleCap()
                ? "capped"
                : String.format(java.util.Locale.ROOT, "%.1f / %.1f XP",
                        snapshot.experience(), skill.getExperienceRequiredForNextProgression(snapshot.trainedProgression()));
        source.sendSuccess(() -> Component.literal(player.getName().getString() + " | " + id + " | "
                + effective.displayName() + " | trained " + trained.displayName()
                + " | cap " + cap.displayName() + " | " + xp), false);
    }

    private record Resolved(OriginSource source, ActiveSkill skill) {
    }
}
