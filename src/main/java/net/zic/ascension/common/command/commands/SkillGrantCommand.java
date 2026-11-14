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
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.skill.SkillInstance;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.rpg_engine.source.OriginSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

//debug command for giving skills to any living entity, skills added here are owned by the command
public class SkillGrantCommand {
    public static final Identifier COMMAND_OWNER = Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "command");

    public static LiteralArgumentBuilder<CommandSourceStack> buildAdd() {
        return Commands.literal("add")
                .then(Commands.argument("targets", EntityArgument.entities())
                        .then(Commands.argument("skill", IdentifierArgument.id())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggestResource(
                                        CoreRegistries.SKILL_REGISTRY.get(context.getSource().registryAccess()).keySet(), builder))
                                .executes(SkillGrantCommand::add)
                        )
                );
    }

    public static LiteralArgumentBuilder<CommandSourceStack> buildRemove() {
        return Commands.literal("remove")
                .then(Commands.argument("targets", EntityArgument.entities())
                        .then(Commands.argument("skill", IdentifierArgument.id())
                                .suggests(SkillGrantCommand::suggestHeldSkills)
                                .executes(SkillGrantCommand::remove)
                        )
                );
    }

    public static LiteralArgumentBuilder<CommandSourceStack> buildList() {
        return Commands.literal("list")
                .then(Commands.argument("targets", EntityArgument.entities())
                        .executes(SkillGrantCommand::list)
                );
    }

    //the living entities among the targets, non living entities can not hold an origin source
    private static List<LivingEntity> getTargets(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        List<LivingEntity> targets = new ArrayList<>();
        for (Entity entity : EntityArgument.getEntities(context, "targets")) {
            if (entity instanceof LivingEntity living) targets.add(living);
        }
        return targets;
    }

    //suggests every skill the targets hold
    private static CompletableFuture<Suggestions> suggestHeldSkills(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        Collection<LivingEntity> targets;
        try {
            targets = getTargets(context);
        } catch (CommandSyntaxException e) {
            return builder.buildFuture();
        }
        Set<Identifier> skills = new HashSet<>();
        for (LivingEntity target : targets) {
            skills.addAll(AscensionOriginSourceHelper.getSkills(AscensionOriginSourceHelper.getEntitySource(target)));
        }
        return SharedSuggestionProvider.suggestResource(skills, builder);
    }

    private static int add(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Identifier skill = IdentifierArgument.getId(context, "skill");
        if (CoreRegistries.safeAccess(CoreRegistries.SKILL_REGISTRY, skill, context.getSource().registryAccess()) == null) {
            context.getSource().sendFailure(Component.literal("Unknown skill " + skill));
            return 0;
        }
        int added = 0;
        for (LivingEntity target : getTargets(context)) {
            OriginSource source = AscensionOriginSourceHelper.getEntitySource(target);
            if (AscensionOriginSourceHelper.addSkill(source, skill, COMMAND_OWNER)) {
                added++;
                context.getSource().sendSuccess(() -> Component.literal("Gave " + skill + " to ").append(target.getName()), true);
            }
        }
        if (added == 0) context.getSource().sendFailure(Component.literal("Could not give skill " + skill));
        return added;
    }

    private static int remove(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Identifier skill = IdentifierArgument.getId(context, "skill");
        int removed = 0;
        for (LivingEntity target : getTargets(context)) {
            OriginSource source = AscensionOriginSourceHelper.getEntitySource(target);
            if (AscensionOriginSourceHelper.removeSkill(source, skill, COMMAND_OWNER)) {
                removed++;
                context.getSource().sendSuccess(() -> Component.literal("Removed " + skill + " from ").append(target.getName()), true);
            }
        }
        if (removed == 0) context.getSource().sendFailure(Component.literal("Skill " + skill + " was not removed (not held, or it still has other owners)"));
        return removed;
    }

    private static int list(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        for (LivingEntity target : getTargets(context)) {
            OriginSource source = AscensionOriginSourceHelper.getEntitySource(target);
            context.getSource().sendSystemMessage(Component.literal("===Skills (").append(target.getName()).append(Component.literal(")===")));
            for (SkillInstance<?> instance : AscensionOriginSourceHelper.getSkillInstances(source)) {
                context.getSource().sendSystemMessage(Component.empty()
                        .append(instance.getSkill().getName())
                        .append(Component.literal(" [" + instance.getSkillId() + "]")));
            }
        }
        return 1;
    }
}
