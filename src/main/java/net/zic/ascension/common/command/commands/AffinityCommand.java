package net.zic.ascension.common.command.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.zic.ascension.api.ascension.core.CoreAttachments;
import net.zic.ascension.api.ascension.core.entity.AscensionEntityPathBonusHolder;
import net.zic.ascension.api.ascension.core.path.PathEffectValueUtil;

public class AffinityCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("affinity")
                .then(Commands.literal("view")
                        .then(Commands.argument("target", EntityArgument.players())
                                .executes(AffinityCommand::viewAffinity)
                        )

                );
    }



    private static int viewAffinity(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var players = EntityArgument.getPlayers(context, "target");
        for(ServerPlayer player : players){
            AscensionEntityPathBonusHolder pathBonusHolder = player.getData(CoreAttachments.PATH_BONUS_HOLDER);
            player.sendSystemMessage(Component.literal("===Affinities (").append(player.getName()).append(Component.literal(")===")));
            for(Identifier path : pathBonusHolder.getAllPathBonusesInCategory(PathEffectValueUtil.AFFINITY_CATEGORY)){
                player.sendSystemMessage(Component.literal(path+": "+pathBonusHolder.getPathBonus(PathEffectValueUtil.AFFINITY_CATEGORY,path)));
            }
        }
        return 1;
    }
}
