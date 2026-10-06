package net.zic.ascension.common.command.commands;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.impl.core.effect.SkillEffectService;

import java.util.UUID;

public class EffectCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("effect")
                .then(Commands.literal("give")
                        .then(Commands.argument("targets", EntityArgument.entities())
                                .then(effectArgument()
                                        .then(Commands.argument("seconds", IntegerArgumentType.integer(1, Integer.MAX_VALUE / 20))
                                                .executes(EffectCommand::apply)
                                                .then(Commands.argument("potency", DoubleArgumentType.doubleArg(0.0D))
                                                        .executes(EffectCommand::apply)
                                                )
                                        )
                                )
                        )
                )
                .then(Commands.literal("remove")
                        .then(Commands.argument("targets", EntityArgument.entities())
                                .then(effectArgument()
                                        .executes(EffectCommand::remove)
                                )
                        )
                )
                .then(Commands.literal("clear")
                        .then(Commands.argument("targets", EntityArgument.entities())
                                .executes(EffectCommand::clear)
                        )
                );
    }

    private static RequiredArgumentBuilder<CommandSourceStack, Identifier> effectArgument() {
        return Commands.argument("effect", IdentifierArgument.id())
                .suggests((context, builder) ->
                        SharedSuggestionProvider.suggestResource(
                                CoreRegistries.SKILL_EFFECT_REGISTRY.get(context.getSource().registryAccess()).keySet(),
                                builder));
    }

    private static int apply(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Identifier effect = IdentifierArgument.getId(context, "effect");
        int seconds = IntegerArgumentType.getInteger(context, "seconds");
        int duration = seconds * 20;
        double potency = hasArgument(context, "potency") ? DoubleArgumentType.getDouble(context, "potency") : 1.0D;

        if (!CoreRegistries.SKILL_EFFECT_REGISTRY.get(context.getSource().registryAccess()).containsKey(effect)) {
            context.getSource().sendFailure(Component.literal("Unknown effect " + effect));
            return 0;
        }

        Entity sourceEntity = context.getSource().getEntity();
        UUID sourceId = sourceEntity == null ? null : sourceEntity.getUUID();

        int applied = 0;
        for (Entity entity : EntityArgument.getEntities(context, "targets")) {
            if (entity instanceof LivingEntity living
                    && SkillEffectService.apply(living, effect, sourceId, null, duration, potency)) {
                applied++;
            }
        }

        if (applied == 0) {
            context.getSource().sendFailure(Component.literal("Could not give " + effect + " to any target"));
            return 0;
        }
        int count = applied;
        context.getSource().sendSuccess(() -> Component.literal(
                "Gave " + effect + " (" + seconds + "s, potency " + potency + ") to " + count + " target(s)"), true);
        return applied;
    }

    private static int remove(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Identifier effect = IdentifierArgument.getId(context, "effect");

        int removed = 0;
        for (Entity entity : EntityArgument.getEntities(context, "targets")) {
            if (entity instanceof LivingEntity living && SkillEffectService.remove(living, effect)) {
                removed++;
            }
        }

        if (removed == 0) {
            context.getSource().sendFailure(Component.literal("No target had " + effect));
            return 0;
        }
        int count = removed;
        context.getSource().sendSuccess(() -> Component.literal("Removed " + effect + " from " + count + " target(s)"), true);
        return removed;
    }

    private static int clear(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        int cleared = 0;
        for (Entity entity : EntityArgument.getEntities(context, "targets")) {
            if (entity instanceof LivingEntity living) {
                cleared += SkillEffectService.clear(living);
            }
        }

        int count = cleared;
        context.getSource().sendSuccess(() -> Component.literal("Cleared " + count + " effect(s)"), true);
        return cleared;
    }

    @SuppressWarnings("SameParameterValue")
    private static boolean hasArgument(CommandContext<CommandSourceStack> context, String name) {
        try {
            context.getArgument(name, Object.class);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
