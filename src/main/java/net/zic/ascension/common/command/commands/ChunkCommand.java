package net.zic.ascension.common.command.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.zic.ascension.chunks.atmospheric_qi.ChunkQiHandler;
import net.zic.ascension.chunks.atmospheric_qi.ChunkHelper;

public class ChunkCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return buildAs("chunk");
    }
    public static LiteralArgumentBuilder<CommandSourceStack> buildAs(String name) {
        return Commands.literal(name)
                .then(Commands.literal("view")
                        .executes(ChunkCommand::viewAtmosphericQi));
    }

    private static int viewAtmosphericQi(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {

        ServerPlayer player = context.getSource().getPlayerOrException();

        ChunkQiHandler qiHandler = ChunkHelper.getQiHandler(player.level().getChunk(player.blockPosition()));

        if (qiHandler == null) {
            context.getSource().sendFailure(Component.literal("Chunk has no atmospheric Qi data"));
            return 0;
        }
        context.getSource().sendSystemMessage(Component.literal("===Chunk==="));
        context.getSource().sendSystemMessage(Component.literal(qiHandler.getQi() + "/" + qiHandler.getCapacity()));
        context.getSource().sendSystemMessage(Component.literal(qiHandler.getRegenRate() + "/s"));

        return 1;
    }
}