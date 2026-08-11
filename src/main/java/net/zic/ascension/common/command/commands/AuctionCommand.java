package net.zic.ascension.common.command.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.zic.ascension.common.auction.AuctionScreenSync;

public final class AuctionCommand {
    private AuctionCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("auction")
                .then(Commands.literal("inbox")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            AuctionScreenSync.openInbox(player);
                            return 1;
                        })));
    }
}
