package net.zic.ascension.common.command.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.permissions.Permissions;

public class SelectSlotCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("slot")
                .requires(source -> source.permissions().hasPermission(Permissions.CHAT_SEND_MESSAGES))
                .then(Commands.argument("slot", IntegerArgumentType.integer())
                    .executes(
                            SelectSlotCommand::selectSlot
                    )
                );
    }

    private static int selectSlot(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {

        return 1;
    }
}
