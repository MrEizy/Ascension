package net.zic.ascension.common.command.commands;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
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
import java.util.Set;
import java.util.stream.Collectors;

public final class SkillMasteryCommand {
    private static final Identifier COMMAND_CAP = AscensionCraft.prefix("command/mastery");

    private SkillMasteryCommand() {
    }

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("mastery")
                .then(Commands.literal("get")
                        .then(skillArgument().executes(SkillMasteryCommand::get)))
                .then(Commands.literal("set")
                        .then(skillArgument()
                                .then(Commands.argument("rank", StringArgumentType.word())
                                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                                Arrays.stream(SkillMasteryRank.values())
                                                        .map(SkillMasteryRank::getSerializedName)
                                                        .toList(),
                                                builder
                                        ))
                                        .executes(SkillMasteryCommand::set))))
                .then(Commands.literal("addXp")
                        .then(skillArgument()
                                .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0.0D))
                                        .executes(SkillMasteryCommand::addExperience))))
                .then(Commands.literal("clearOverride")
                        .then(skillArgument().executes(SkillMasteryCommand::clearOverride)));
    }

    private static com.mojang.brigadier.builder.RequiredArgumentBuilder<CommandSourceStack, Identifier> skillArgument() {
        return Commands.argument("skill", IdentifierArgument.id())
                .suggests((context, builder) -> {
                    ServerPlayer player = context.getSource().getPlayer();
                    OriginSource source = AscensionOriginSourceHelper.getEntitySource(player);
                    if (source == null) {
                        return SharedSuggestionProvider.suggestResource(Set.<Identifier>of(), builder);
                    }
                    Set<Identifier> skills = AscensionOriginSourceHelper.getSkills(source).stream()
                            .filter(skillId -> CoreRegistries.safeAccess(
                                    CoreRegistries.SKILL_REGISTRY,
                                    skillId,
                                    context.getSource().registryAccess()
                            ) instanceof ActiveSkill)
                            .collect(Collectors.toSet());
                    return SharedSuggestionProvider.suggestResource(skills, builder);
                });
    }

    private static int get(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayer();
        Identifier skillId = IdentifierArgument.getId(context, "skill");
        Resolved resolved = resolve(player, skillId);
        if (resolved == null) {
            context.getSource().sendFailure(Component.literal(skillId + " is not an owned active skill"));
            return 0;
        }

        sendSnapshot(context.getSource(), skillId, resolved.skill(), resolved.source());
        return 1;
    }

    private static int set(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayer();
        Identifier skillId = IdentifierArgument.getId(context, "skill");
        SkillMasteryRank rank = SkillMasteryRank.fromName(StringArgumentType.getString(context, "rank"));
        Resolved resolved = resolve(player, skillId);
        if (resolved == null) {
            context.getSource().sendFailure(Component.literal(skillId + " is not an owned active skill"));
            return 0;
        }
        if (rank == null) {
            context.getSource().sendFailure(Component.literal("Unknown mastery rank"));
            return 0;
        }

        SkillProgressionService.setCap(resolved.source(), skillId, COMMAND_CAP, rank.progression());
        SkillProgressionService.setTrainedProgression(resolved.source(), skillId, rank.progression());
        sendSnapshot(context.getSource(), skillId, resolved.skill(), resolved.source());
        return 1;
    }

    private static int addExperience(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayer();
        Identifier skillId = IdentifierArgument.getId(context, "skill");
        double amount = DoubleArgumentType.getDouble(context, "amount");
        Resolved resolved = resolve(player, skillId);
        if (resolved == null) {
            context.getSource().sendFailure(Component.literal(skillId + " is not an owned active skill"));
            return 0;
        }

        SkillProgressionService.addExperience(resolved.source(), skillId, amount);
        sendSnapshot(context.getSource(), skillId, resolved.skill(), resolved.source());
        return 1;
    }

    private static int clearOverride(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayer();
        Identifier skillId = IdentifierArgument.getId(context, "skill");
        Resolved resolved = resolve(player, skillId);
        if (resolved == null) {
            context.getSource().sendFailure(Component.literal(skillId + " is not an owned active skill"));
            return 0;
        }

        SkillProgressionService.removeCap(resolved.source(), skillId, COMMAND_CAP);
        sendSnapshot(context.getSource(), skillId, resolved.skill(), resolved.source());
        return 1;
    }

    private static Resolved resolve(ServerPlayer player, Identifier skillId) {
        OriginSource source = AscensionOriginSourceHelper.getEntitySource(player);
        if (source == null || !AscensionOriginSourceHelper.hasSkill(source, skillId)) {
            return null;
        }
        if (!(CoreRegistries.safeAccess(
                CoreRegistries.SKILL_REGISTRY,
                skillId,
                player.registryAccess()
        ) instanceof ActiveSkill skill)) {
            return null;
        }
        return new Resolved(source, skill);
    }

    private static void sendSnapshot(
            CommandSourceStack source,
            Identifier skillId,
            ActiveSkill skill,
            OriginSource origin
    ) {
        SkillProgressionSnapshot snapshot = SkillProgressionResolver.resolve(origin, skillId);
        SkillMasteryRank trained = SkillMasteryRank.fromProgression(snapshot.trainedProgression());
        SkillMasteryRank cap = SkillMasteryRank.fromProgression(snapshot.accessibleCap());
        SkillMasteryRank effective = SkillMasteryRank.fromProgression(snapshot.effectiveProgression());

        String experience;
        if (trained == SkillMasteryRank.TRANSCENDENCE || snapshot.trainedProgression() >= snapshot.accessibleCap()) {
            experience = "capped";
        } else {
            double required = skill.getExperienceRequiredForNextProgression(snapshot.trainedProgression());
            experience = String.format("%.1f / %.1f XP", snapshot.experience(), required);
        }

        source.sendSuccess(
                () -> Component.literal(
                        skillId + " | " + effective.displayName()
                                + " | trained " + trained.displayName()
                                + " | cap " + cap.displayName()
                                + " | " + experience
                ),
                false
        );
    }

    private record Resolved(OriginSource source, ActiveSkill skill) {
    }
}
