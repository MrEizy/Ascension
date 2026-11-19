package net.zic.ascension.common.command.commands;

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
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.common.item.ModItems;
import net.zic.ascension.common.item.components.AscensionComponents;
import net.zic.ascension.impl.core.technique.AlchemyTechnique;

import java.util.Collection;
import java.util.List;

public final class AscensionGive {
    private AscensionGive() {
    }

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("give")
                .then(Commands.literal("physique").then(essenceArguments(true)))
                .then(Commands.literal("bloodline").then(essenceArguments(false)))
                .then(Commands.literal("technique").then(techniqueArguments()));
    }

    private static RequiredArgumentBuilder<CommandSourceStack, Identifier> essenceArguments(boolean physique) {
        String name = physique ? "physique" : "bloodline";
        var root = Commands.argument(name, IdentifierArgument.id())
                .suggests((context, builder) -> SharedSuggestionProvider.suggestResource(
                        physique ? CoreRegistries.PHYSIQUE_REGISTRY.get(context.getSource().registryAccess()).keySet()
                                : CoreRegistries.BLOODLINE_REGISTRY.get(context.getSource().registryAccess()).keySet(), builder))
                .executes(context -> giveEssence(context, physique, false));
        root.then(targetArgument(context -> giveEssence(context, physique, true)));
        var purity = Commands.argument("purity", IntegerArgumentType.integer(1, 100))
                .executes(context -> giveEssence(context, physique, false));
        purity.then(targetArgument(context -> giveEssence(context, physique, true)));
        var amount = Commands.argument("amount", IntegerArgumentType.integer(1, 64))
                .executes(context -> giveEssence(context, physique, false));
        amount.then(targetArgument(context -> giveEssence(context, physique, true)));
        purity.then(amount);
        root.then(purity);
        return root;
    }

    private static RequiredArgumentBuilder<CommandSourceStack, Identifier> techniqueArguments() {
        var root = Commands.argument("technique", IdentifierArgument.id())
                .suggests((context, builder) -> SharedSuggestionProvider.suggestResource(
                        CoreRegistries.TECHNIQUE_REGISTRY.get(context.getSource().registryAccess()).keySet(), builder))
                .executes(context -> giveTechnique(context, false));
        root.then(targetArgument(context -> giveTechnique(context, true)));
        var amount = Commands.argument("amount", IntegerArgumentType.integer(1, 100))
                .executes(context -> giveTechnique(context, false));
        amount.then(targetArgument(context -> giveTechnique(context, true)));
        root.then(amount);
        return root;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> targetArgument(com.mojang.brigadier.Command<CommandSourceStack> execute) {
        return Commands.literal("to").then(Commands.argument("targets", EntityArgument.players()).executes(execute));
    }

    private static int giveTechnique(CommandContext<CommandSourceStack> context, boolean explicitTarget)
            throws CommandSyntaxException {
        Identifier id = IdentifierArgument.getId(context, "technique");
        Object technique = CoreRegistries.safeAccess(CoreRegistries.TECHNIQUE_REGISTRY, id,
                context.getSource().registryAccess());
        if (technique == null) {
            context.getSource().sendFailure(Component.literal("Unknown technique " + id));
            return 0;
        }
        ItemStack item = new ItemStack(ModItems.TECHNIQUE_MANUAL.get());
        item.set(AscensionComponents.REGISTRY_ID_HOLDER, id);
        if (technique instanceof AlchemyTechnique alchemy) {
            item.set(DataComponents.ITEM_NAME, alchemy.getName(null));
        }
        return deliver(context, explicitTarget, item, getOptionalInt(context, "amount", 1), id);
    }

    private static int giveEssence(CommandContext<CommandSourceStack> context, boolean physique, boolean explicitTarget)
            throws CommandSyntaxException {
        String arg = physique ? "physique" : "bloodline";
        Identifier id = IdentifierArgument.getId(context, arg);
        Object value = physique
                ? CoreRegistries.safeAccess(CoreRegistries.PHYSIQUE_REGISTRY, id, context.getSource().registryAccess())
                : CoreRegistries.safeAccess(CoreRegistries.BLOODLINE_REGISTRY, id, context.getSource().registryAccess());
        if (value == null) {
            context.getSource().sendFailure(Component.literal("Unknown " + arg + " " + id));
            return 0;
        }
        ItemStack item = new ItemStack(physique ? ModItems.PHYSIQUE_ESSENCE.get() : ModItems.BLOODLINE_ESSENCE.get());
        item.set(AscensionComponents.REGISTRY_ID_HOLDER, id);
        item.set(AscensionComponents.PURITY, getOptionalInt(context, "purity", 1));
        return deliver(context, explicitTarget, item, getOptionalInt(context, "amount", 1), id);
    }

    private static int getOptionalInt(CommandContext<CommandSourceStack> context, String key, int defaultValue) {
        try {
            return IntegerArgumentType.getInteger(context, key);
        } catch (IllegalArgumentException ignored) {
            return defaultValue;
        }
    }

    private static int deliver(CommandContext<CommandSourceStack> context, boolean explicitTarget,
                               ItemStack template, int amount, Identifier id) throws CommandSyntaxException {
        Collection<ServerPlayer> targets = explicitTarget ? EntityArgument.getPlayers(context, "targets")
                : List.of(context.getSource().getPlayerOrException());
        int delivered = 0;
        for (ServerPlayer target : targets) {
            int remaining = amount;
            int dropped = 0;
            while (remaining > 0) {
                int count = Math.min(remaining, template.getMaxStackSize());
                ItemStack part = template.copy();
                part.setCount(count);
                target.getInventory().add(part);
                if (!part.isEmpty()) {
                    dropped += part.getCount();
                    target.drop(part.copy(), false);
                }
                remaining -= count;
            }
            String feedback = "Gave " + amount + " x " + id + " to " + target.getName().getString()
                    + (dropped > 0 ? " (" + dropped + " dropped near player: inventory full)" : "");
            context.getSource().sendSuccess(() -> Component.literal(feedback), true);
            delivered++;
        }
        return delivered;
    }
}
