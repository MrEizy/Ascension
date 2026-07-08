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
import net.zic.ascension.api.capabilities.AscensionEntityDataHolder;
import net.zic.ascension.api.capabilities.CoreCapabilities;
import net.zic.ascension.api.core.CoreRegistries;
import net.zic.ascension.api.core.entity.AscensionEntityData;

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

                );
    }



    private static int givePhysique(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        int amount = 0;
        try{
            amount = IntegerArgumentType.getInteger(context,"amount");
        } catch (Exception _){
        }
        int purity = 1;
        try {
            purity = IntegerArgumentType.getInteger(context,"purity");
        }catch (Exception _){}

        Identifier physique = IdentifierArgument.getId(context,"physique");
        return 1;
    }

}
