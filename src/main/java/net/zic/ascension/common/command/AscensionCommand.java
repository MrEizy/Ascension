package net.zic.ascension.common.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.permissions.Permissions;
import net.zic.ascension.common.command.commands.SetRealmCommand;

public class AscensionCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("ascension")
                .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))

                /*

                All currently implemented commands


                    /ascension cultivation set <targets> <path> <majorRealm> <minorRealm> [progress]



                 */


                .then(SetRealmCommand.build())


        );
    }
}
