package net.zic.ascension.common.command.commands;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.zic.ascension.api.ascension.core.alchemy.AlchemyBatch;
import net.zic.ascension.api.ascension.core.alchemy.AlchemyContext;
import net.zic.ascension.api.ascension.core.alchemy.AlchemyMaterial;
import net.zic.ascension.api.ascension.core.alchemy.AlchemyMaterials;
import net.zic.ascension.api.ascension.core.alchemy.AlchemyMergeResolver;
import net.zic.ascension.api.ascension.core.alchemy.AlchemyRefinementResolver;
import net.zic.ascension.api.ascension.core.alchemy.AlchemyFormulaResolver;
import net.zic.ascension.api.ascension.core.alchemy.AlchemySubstance;
import net.zic.ascension.common.item.ModItems;

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
                                        .executes(AlchemyCommand::merge))))
                .then(Commands.literal("condense")
                        .executes(AlchemyCommand::condense));
    }

    private static int inspect(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        AlchemyContext alchemyContext = AlchemyContext.of(player);
        AlchemyMaterial material = AlchemyMaterials.resolve(player.getMainHandItem(), alchemyContext).orElse(null);
        if (material == null) {
            player.sendSystemMessage(Component.literal("Held item has no alchemy substance."));
            return 0;
        }

        AlchemyRefinementResolver.RefinementResult refinement = AlchemyRefinementResolver.refine(material, alchemyContext);
        player.sendSystemMessage(Component.literal("Alchemy material: " + describe(refinement.substance())));
        player.sendSystemMessage(Component.literal("Refinement difficulty: " + material.refinementDifficulty() + " | strain: " + refinement.strain()));
        return 1;
    }

    private static int merge(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        AlchemyContext alchemyContext = AlchemyContext.of(player);
        AlchemyMaterial first = AlchemyMaterials.resolve(player.getMainHandItem(), alchemyContext).orElse(null);
        AlchemyMaterial second = AlchemyMaterials.resolve(player.getOffhandItem(), alchemyContext).orElse(null);
        if (first == null || second == null) {
            player.sendSystemMessage(Component.literal("Main hand and offhand must both contain alchemy substances."));
            return 0;
        }

        double safeDelta = DoubleArgumentType.getDouble(context, "safe_delta");
        double explosionDelta = DoubleArgumentType.getDouble(context, "explosion_delta");
        AlchemySubstance firstSubstance = AlchemyRefinementResolver.refine(first, alchemyContext).substance();
        AlchemySubstance secondSubstance = AlchemyRefinementResolver.refine(second, alchemyContext).substance();
        AlchemyMergeResolver.MergeResult result = AlchemyMergeResolver.merge(
                new AlchemyBatch(firstSubstance, 1),
                secondSubstance,
                safeDelta,
                explosionDelta
        );

        player.sendSystemMessage(Component.literal("Merge outcome: " + result.outcome() + " | delta: " + result.energyDelta()));
        player.sendSystemMessage(Component.literal("Result: " + describe(result.batch().substance())));
        return 1;
    }

    private static int condense(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        AlchemyContext alchemyContext = AlchemyContext.of(player);
        AlchemyMaterial first = AlchemyMaterials.resolve(player.getMainHandItem(), alchemyContext).orElse(null);
        AlchemyMaterial second = AlchemyMaterials.resolve(player.getOffhandItem(), alchemyContext).orElse(null);
        if (first == null || second == null) {
            player.sendSystemMessage(Component.literal("Main hand and offhand must both contain alchemy substances."));
            return 0;
        }

        AlchemySubstance firstSubstance = AlchemyRefinementResolver.refine(first, alchemyContext).substance();
        AlchemySubstance secondSubstance = AlchemyRefinementResolver.refine(second, alchemyContext).substance();
        AlchemyMergeResolver.MergeResult merge = AlchemyMergeResolver.merge(
                new AlchemyBatch(firstSubstance, 1),
                secondSubstance,
                alchemyContext
        );

        if (merge.outcome() == AlchemyMergeResolver.Outcome.CATASTROPHIC) {
            player.sendSystemMessage(Component.literal("The merge became catastrophic before condensation."));
            player.sendSystemMessage(Component.literal("Energy delta: " + merge.energyDelta()));
            return 0;
        }

        AlchemyBatch batch = merge.batch();
        AlchemyFormulaResolver.CondensationResult result = AlchemyFormulaResolver.condense(batch, alchemyContext);
        for (ItemStack output : result.outputs()) {
            if (!player.addItem(output)) {
                player.drop(output, false);
            }
        }

        if (result.success()) {
            if (result.hasRemainder()) {
                ItemStack residue = new ItemStack(ModItems.PILL_RESIDUE.get());
                if (!player.addItem(residue)) {
                    player.drop(residue, false);
                }
            }
            player.sendSystemMessage(Component.literal(
                    "Condensed " + result.outputCount() + " pill(s) from " + result.matchedFormulas().size() + " formula match(es)."
            ));
            return 1;
        }

        ItemStack residue = new ItemStack(ModItems.PILL_RESIDUE.get());
        if (!player.addItem(residue)) {
            player.drop(residue, false);
        }
        player.sendSystemMessage(Component.literal("No pill formula matched. The batch became Pill Residue."));
        player.sendSystemMessage(Component.literal("Batch: " + describe(batch.substance())));
        return 0;
    }

    private static String describe(AlchemySubstance substance) {
        return "rankTier=" + substance.rankTier()
                + ", amplifier=" + substance.amplifier()
                + ", purity=" + substance.purity()
                + ", instability=" + substance.instability()
                + ", properties=" + substance.properties()
                + ", affinities=" + substance.affinities();
    }
}
