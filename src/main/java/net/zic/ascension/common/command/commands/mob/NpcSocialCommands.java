package net.zic.ascension.common.command.commands.mob;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.zic.ascension.common.entities.npc.CultivatorNpcEntity;
import net.zic.ascension.common.entities.npc.NpcAffiliation;
import net.zic.ascension.common.entities.npc.NpcReputation;

public final class NpcSocialCommands {
    private NpcSocialCommands() { }

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("relationship")
                .then(Commands.literal("view").executes(context -> relationship(context, "view", false))
                        .then(Commands.argument("npc", EntityArgument.entity()).executes(context -> relationship(context, "view", true))))
                .then(Commands.literal("set").then(Commands.argument("value", IntegerArgumentType.integer(-100, 100))
                        .executes(context -> relationship(context, "set", false))
                        .then(Commands.argument("npc", EntityArgument.entity()).executes(context -> relationship(context, "set", true)))))
                .then(Commands.literal("add").then(Commands.argument("value", IntegerArgumentType.integer(-100, 100))
                        .executes(context -> relationship(context, "add", false))
                        .then(Commands.argument("npc", EntityArgument.entity()).executes(context -> relationship(context, "add", true)))));
    }

    public static LiteralArgumentBuilder<CommandSourceStack> affiliation() {
        return Commands.literal("affiliation")
                .then(Commands.literal("view").executes(context -> affiliation(context, false, false))
                        .then(Commands.argument("npc", EntityArgument.entity()).executes(context -> affiliation(context, true, false))))
                .then(Commands.literal("set").then(Commands.argument("faction", StringArgumentType.word())
                        .suggests((context, builder) -> net.minecraft.commands.SharedSuggestionProvider.suggest(java.util.List.of("orthodox", "unorthodox", "demonic"), builder))
                        .executes(context -> affiliation(context, false, true))
                        .then(Commands.argument("npc", EntityArgument.entity()).executes(context -> affiliation(context, true, true)))));
    }

    public static LiteralArgumentBuilder<CommandSourceStack> reputation() {
        return Commands.literal("reputation")
                .then(Commands.literal("view").executes(context -> reputation(context, "view", false))
                        .then(Commands.argument("player", EntityArgument.player()).executes(context -> reputation(context, "view", true))))
                .then(Commands.literal("set").then(reputationArgument("set")))
                .then(Commands.literal("add").then(reputationArgument("add")));
    }

    private static com.mojang.brigadier.builder.RequiredArgumentBuilder<CommandSourceStack, String> reputationArgument(String action) {
        return Commands.argument("faction", StringArgumentType.word())
                .suggests((context, builder) -> net.minecraft.commands.SharedSuggestionProvider.suggest(java.util.List.of("orthodox", "unorthodox", "demonic"), builder))
                .then(Commands.argument("value", IntegerArgumentType.integer(-100, 100))
                        .executes(context -> reputation(context, action, false))
                        .then(Commands.argument("player", EntityArgument.player()).executes(context -> reputation(context, action, true))));
    }

    private static CultivatorNpcEntity target(CommandContext<CommandSourceStack> context, boolean selected) throws CommandSyntaxException {
        if (selected) {
            Entity entity = EntityArgument.getEntity(context, "npc");
            return entity instanceof CultivatorNpcEntity npc ? npc : null;
        }
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) return null;
        Vec3 origin = player.getEyePosition();
        Vec3 direction = player.getViewVector(1).scale(10);
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(player, origin, origin.add(direction),
                player.getBoundingBox().expandTowards(direction).inflate(1),
                entity -> entity instanceof CultivatorNpcEntity && !entity.isSpectator(), 100);
        return hit != null && hit.getEntity() instanceof CultivatorNpcEntity npc ? npc : null;
    }

    private static int relationship(CommandContext<CommandSourceStack> context, String action, boolean selected) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayer();
        CultivatorNpcEntity npc = target(context, selected);
        if (player == null || npc == null) {
            context.getSource().sendFailure(Component.literal("Look at an NPC within 10 blocks, or supply an NPC selector. A player must run this command."));
            return 0;
        }
        int respect = switch (action) {
            case "set" -> npc.setRespect(player, IntegerArgumentType.getInteger(context, "value"));
            case "add" -> npc.addRespect(player, IntegerArgumentType.getInteger(context, "value"));
            default -> npc.getRespect(player);
        };
        String message = npc.getName().getString() + ": " + npc.getPersonalityLabel() + " / " + npc.getAffiliation().label() + " / Respect " + respect + " (" + npc.getStanding(player) + ")";
        context.getSource().sendSuccess(() -> Component.literal(message), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int affiliation(CommandContext<CommandSourceStack> context, boolean selected, boolean change) throws CommandSyntaxException {
        CultivatorNpcEntity npc = target(context, selected);
        if (npc == null) {
            context.getSource().sendFailure(Component.literal("Look at an NPC within 10 blocks or supply an NPC selector."));
            return 0;
        }
        if (change) {
            NpcAffiliation faction = NpcAffiliation.parse(StringArgumentType.getString(context, "faction"));
            if (faction == null) {
                context.getSource().sendFailure(Component.literal("Expected orthodox, unorthodox or demonic."));
                return 0;
            }
            npc.setAffiliation(faction);
        }
        context.getSource().sendSuccess(() -> Component.literal(npc.getName().getString() + " affiliation: " + npc.getAffiliation().label()), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int reputation(CommandContext<CommandSourceStack> context, String action, boolean selected) throws CommandSyntaxException {
        ServerPlayer player = selected ? EntityArgument.getPlayer(context, "player") : context.getSource().getPlayer();
        if (player == null) {
            context.getSource().sendFailure(Component.literal("Provide a player, or run this in-game."));
            return 0;
        }
        NpcReputation reputation = NpcReputation.get(player.level().getServer());
        if (!action.equals("view")) {
            NpcAffiliation faction = NpcAffiliation.parse(StringArgumentType.getString(context, "faction"));
            if (faction == null) {
                context.getSource().sendFailure(Component.literal("Expected orthodox, unorthodox or demonic."));
                return 0;
            }
            int value = IntegerArgumentType.getInteger(context, "value");
            if (action.equals("set")) reputation.set(player.getUUID(), faction, value);
            else reputation.add(player.getUUID(), faction, value);
        }
        String message = player.getName().getString() + " reputation: Orthodox " + reputation.value(player.getUUID(), NpcAffiliation.ORTHODOX)
                + " | Unorthodox " + reputation.value(player.getUUID(), NpcAffiliation.UNORTHODOX)
                + " | Demonic " + reputation.value(player.getUUID(), NpcAffiliation.DEMONIC);
        context.getSource().sendSuccess(() -> Component.literal(message), false);
        return Command.SINGLE_SUCCESS;
    }
}
