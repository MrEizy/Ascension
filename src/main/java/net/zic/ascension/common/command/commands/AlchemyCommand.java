package net.zic.ascension.common.command.commands;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
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
import net.zic.ascension.api.ascension.core.alchemy.AlchemySubstance;
import net.zic.ascension.api.ascension.core.alchemy.medicinal.MedicinalCompositionEngine;
import net.zic.ascension.api.ascension.core.alchemy.medicinal.MedicinalComposition;
import net.zic.ascension.api.ascension.core.alchemy.medicinal.MedicinalProfile;
import net.zic.ascension.api.ascension.core.alchemy.medicinal.MedicinalPillFactory;
import net.zic.ascension.api.ascension.core.alchemy.medicinal.MedicinalDecay;
import net.zic.ascension.api.ascension.core.alchemy.medicinal.MedicinalIngredientQuality;
import net.zic.ascension.common.item.ModItems;

import java.util.ArrayList;
import java.util.List;


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
                .then(Commands.literal("simulate")
                        .executes(context -> simulate(context, false, -1))
                        .then(Commands.literal("reverse").executes(context -> simulate(context, true, -1)))
                        .then(Commands.literal("slot")
                                .then(Commands.argument("slot", IntegerArgumentType.integer(0, 35))
                                        .executes(context -> simulate(context, false, IntegerArgumentType.getInteger(context, "slot")))))
                )
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
                explosionDelta,
                player.registryAccess()
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
            player.sendSystemMessage(Component.literal("Hold alchemical ingredients in both hands."));
            return 0;
        }
        MedicinalCompositionEngine.Result result = MedicinalCompositionEngine.simulate(List.of(first, second), alchemyContext);
        if (result.outcome() == AlchemyMergeResolver.Outcome.CATASTROPHIC) {
            player.sendSystemMessage(Component.literal("The medicinal reaction was catastrophic; no pill formed."));
            return 0;
        }
        ItemStack pill = MedicinalPillFactory.condense(result.profile(), result.ingredientCount(), player.level().registryAccess(), MedicinalDecay.now(player.level()),
                MedicinalIngredientQuality.average(List.of(player.getMainHandItem(), player.getOffhandItem())));
        if (pill.isEmpty()) {
            player.sendSystemMessage(Component.literal("This combination did not express a usable medicinal effect."));
            return 0;
        }
        int count = pill.getCount();
        if (!player.addItem(pill)) player.drop(pill, false);
        player.sendSystemMessage(Component.literal("Test condensation produced " + count + " procedural pill(s)."));
        return 1;
    }

    private static int simulate(CommandContext<CommandSourceStack> context, boolean reverse, int slot)
            throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        AlchemyContext alchemyContext = AlchemyContext.of(player);
        ItemStack primary = reverse ? player.getOffhandItem() : player.getMainHandItem();
        ItemStack secondary = reverse ? player.getMainHandItem() : player.getOffhandItem();
        List<AlchemyMaterial> materials = new ArrayList<>();
        for (ItemStack stack : List.of(primary, secondary)) {
            AlchemyMaterial material = AlchemyMaterials.resolve(stack, alchemyContext).orElse(null);
            if (material == null) {
                player.sendSystemMessage(Component.literal("Both hands must hold alchemical materials."));
                return 0;
            }
            materials.add(material);
        }
        if (slot >= 0) {
            AlchemyMaterial third = AlchemyMaterials.resolve(player.getInventory().getItem(slot), alchemyContext).orElse(null);
            if (third == null) {
                player.sendSystemMessage(Component.literal("Inventory slot " + slot + " has no alchemical material."));
                return 0;
            }
            materials.add(third);
        }
        MedicinalCompositionEngine.Result result = MedicinalCompositionEngine.simulate(materials, alchemyContext);
        MedicinalProfile profile = result.profile();
        MedicinalComposition composition = profile.composition();
        player.sendSystemMessage(Component.literal("Medicinal simulation: " + result.ingredientCount()
                + " materials | " + result.outcome() + " | peak energy " + result.maximumEnergyDelta()));
        player.sendSystemMessage(Component.literal("Base: " + describe(composition.substance())));
        player.sendSystemMessage(Component.literal("Products: " + composition.products() + " | toxicity: " + composition.toxicity()));
        player.sendSystemMessage(Component.literal("Reactions (in order): " + result.reactions()));
        player.sendSystemMessage(Component.literal("Medicinal effects: " + profile.effects()));
        return 1;
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
