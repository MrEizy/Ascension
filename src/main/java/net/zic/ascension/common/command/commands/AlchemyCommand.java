package net.zic.ascension.common.command.commands;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.zic.ascension.api.ascension.core.alchemy.AlchemyBatch;
import net.zic.ascension.api.ascension.core.alchemy.AlchemySubstance;

public final class AlchemyCommand {
    private AlchemyCommand() {
    }

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("alchemy")
                .then(Commands.literal("inspect")
                        .executes(AlchemyCommand::inspect))
                .then(Commands.literal("merge")
                        .then(Commands.argument("safe_delta", DoubleArgumentType.doubleArg(0.0D))
                                .then(Commands.argument("explosion_delta", DoubleArgumentType.doubleArg(0.0D))
                                        .executes(AlchemyCommand::merge))));
    }

    private static int inspect(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        AlchemySubstance.Material material = AlchemySubstance.resolve(player.getMainHandItem()).orElse(null);
        if (material == null) {
            player.sendSystemMessage(Component.literal("Held item has no alchemy substance."));
            return 0;
        }

        player.sendSystemMessage(Component.literal("Alchemy substance: " + describe(material.substance())));
        player.sendSystemMessage(Component.literal("Refinement difficulty: " + material.refinementDifficulty()));
        return 1;
    }

    private static int merge(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        AlchemySubstance.Material first = AlchemySubstance.resolve(player.getMainHandItem()).orElse(null);
        AlchemySubstance.Material second = AlchemySubstance.resolve(player.getOffhandItem()).orElse(null);
        if (first == null || second == null) {
            player.sendSystemMessage(Component.literal("Main hand and offhand must both contain alchemy substances."));
            return 0;
        }

        double safeDelta = DoubleArgumentType.getDouble(context, "safe_delta");
        double explosionDelta = DoubleArgumentType.getDouble(context, "explosion_delta");
        AlchemyBatch.MergeResult result = new AlchemyBatch(first.substance(), 1)
                .merge(second.substance(), safeDelta, explosionDelta);

        player.sendSystemMessage(Component.literal("Merge outcome: " + result.outcome() + " | delta: " + result.energyDelta()));
        player.sendSystemMessage(Component.literal("Result: " + describe(result.batch().substance())));
        return 1;
    }

    private static String describe(AlchemySubstance substance) {
        return "realm=" + substance.majorRealm() + "." + substance.minorRealm()
                + ", potency=" + substance.potency()
                + ", purity=" + substance.purity()
                + ", instability=" + substance.instability()
                + ", properties=" + substance.properties()
                + ", affinities=" + substance.affinities();
    }
}
