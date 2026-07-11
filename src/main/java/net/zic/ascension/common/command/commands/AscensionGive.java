package net.zic.ascension.common.command.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.commands.synchronization.brigadier.DoubleArgumentInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.capabilities.AscensionEntityDataHolder;
import net.zic.ascension.api.capabilities.CoreCapabilities;
import net.zic.ascension.api.core.CoreRegistries;
import net.zic.ascension.api.core.entity.AscensionEntityData;
import net.zic.ascension.common.item.ModItems;
import net.zic.ascension.common.item.components.AscensionComponents;

public class AscensionGive {
    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("give")
                .then(Commands.literal("physique")
                        .then(Commands.argument("physique", IdentifierArgument.id())
                                .suggests((context, builder) ->
                                        SharedSuggestionProvider.suggestResource(
                                                CoreRegistries.PHYSIQUE_REGISTRY.get(context.getSource().registryAccess()).keySet()
                                                ,builder))
                                .executes(AscensionGive::givePhysique)
                                .then(Commands.argument("purity",IntegerArgumentType.integer(1,100))
                                    .executes(AscensionGive::givePhysique)
                                    .then(Commands.argument("amount",IntegerArgumentType.integer(0,64))
                                            .executes(AscensionGive::givePhysique)
                                    )
                                )
                        )

                )
                .then(Commands.literal("bloodline")
                        .then(Commands.argument("bloodline", IdentifierArgument.id())
                                .suggests((context, builder) ->
                                        SharedSuggestionProvider.suggestResource(
                                                CoreRegistries.BLOODLINE_REGISTRY.get(context.getSource().registryAccess()).keySet()
                                                ,builder))
                                .executes(AscensionGive::giveBloodline)
                                .then(Commands.argument("purity",IntegerArgumentType.integer(1,100))
                                        .executes(AscensionGive::giveBloodline)
                                        .then(Commands.argument("amount",IntegerArgumentType.integer(0,64))
                                                .executes(AscensionGive::giveBloodline)
                                        )
                                )
                        )

                )
                .then(Commands.literal("technique")
                        .then(Commands.argument("technique", IdentifierArgument.id())
                                .suggests((context, builder) ->
                                        SharedSuggestionProvider.suggestResource(
                                                CoreRegistries.TECHNIQUE_REGISTRY.get(context.getSource().registryAccess()).keySet()
                                                ,builder))
                                .executes(AscensionGive::giveTechnique)
                                .then(Commands.argument("amount",IntegerArgumentType.integer(1,100))
                                        .executes(AscensionGive::giveTechnique)
                                )
                        )

                );
    }

    private static int giveTechnique(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        int amount = 1;
        try{
            amount = IntegerArgumentType.getInteger(context,"amount");
        } catch (Exception _){
        }

        Identifier technique = IdentifierArgument.getId(context,"technique");

        ItemStack stack = new ItemStack(ModItems.TECHNIQUE_MANUAL.get(),amount);
        stack.set(AscensionComponents.REGISTRY_ID_HOLDER, technique);
        context.getSource().getPlayer().addItem(stack);
        return 1;
    }
    private static int giveBloodline(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        int amount = 1;
        try{
            amount = IntegerArgumentType.getInteger(context,"amount");
        } catch (Exception _){
        }
        int purity = 1;
        try {
            purity = IntegerArgumentType.getInteger(context,"purity");
        }catch (Exception _){}

        Identifier bloodline = IdentifierArgument.getId(context,"bloodline");

        ItemStack stack = new ItemStack(ModItems.BLOODLINE_ESSENCE.get(),amount);
        stack.set(AscensionComponents.REGISTRY_ID_HOLDER, bloodline);
        stack.set(AscensionComponents.PURITY,purity);
        context.getSource().getPlayer().addItem(stack);
        return 1;
    }

    private static int givePhysique(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        int amount = 1;
        try{
            amount = IntegerArgumentType.getInteger(context,"amount");
        } catch (Exception _){
        }
        int purity = 1;
        try {
            purity = IntegerArgumentType.getInteger(context,"purity");
        }catch (Exception _){}

        Identifier physique = IdentifierArgument.getId(context,"physique");

        ItemStack stack = new ItemStack(ModItems.PHYSIQUE_ESSENCE.get(),amount);
        stack.set(AscensionComponents.REGISTRY_ID_HOLDER, physique);
        stack.set(AscensionComponents.PURITY,purity);
        context.getSource().getPlayer().addItem(stack);
        return 1;
    }

}
