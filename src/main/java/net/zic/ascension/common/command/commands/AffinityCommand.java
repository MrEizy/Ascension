package net.zic.ascension.common.command.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.zic.ascension.api.ascension.capabilities.AscensionEntityDataProvider;
import net.zic.ascension.api.ascension.capabilities.CoreCapabilities;
import net.zic.ascension.api.ascension.core.entity.AscensionEntityData;

public class AffinityCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("affinity")
                .then(Commands.literal("view")
                        .then(Commands.argument("target", EntityArgument.players())
                                .executes(AffinityCommand::viewAffinity)
                                .then(Commands.argument("category",IdentifierArgument.id())
                                        .executes(AffinityCommand::viewAffinityCategory)
                                )
                        )

                );
    }

    private static int viewAffinityCategory(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {

        var players = EntityArgument.getPlayers(context, "target");
        Identifier category = IdentifierArgument.getId(context, "category");
        for(ServerPlayer player : players){
            AscensionEntityDataProvider holder = player.getCapability(CoreCapabilities.ASCENSION_ENTITY_DATA_PROVIDER_CAPABILITY);
            if(holder == null) continue;
            AscensionEntityData data = holder.getData(player);
            player.sendSystemMessage(Component.literal("===Affinities ("+category+") (").append(player.getName()).append(Component.literal(")===")));
            for(Identifier path : data.getAllAffinities()){
                player.sendSystemMessage(Component.literal(path+": "+data.getAffinity(category,path)));
            }
        }
        return 1;
    }

    private static int viewAffinity(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var players = EntityArgument.getPlayers(context, "target");

        for(ServerPlayer player : players){
            AscensionEntityDataProvider holder = player.getCapability(CoreCapabilities.ASCENSION_ENTITY_DATA_PROVIDER_CAPABILITY);
            if(holder == null) continue;
            AscensionEntityData data = holder.getData(player);
            player.sendSystemMessage(Component.literal("===Affinities (").append(player.getName()).append(Component.literal(")===")));
            for(Identifier path : data.getAllAffinities()){
                player.sendSystemMessage(Component.literal(path+": "+data.getAffinity(path)));
            }
        }
        return 1;
    }
}
