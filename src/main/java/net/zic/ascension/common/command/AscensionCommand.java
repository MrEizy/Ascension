package net.zic.ascension.common.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.permissions.Permissions;
import net.zic.ascension.common.command.commands.SetRealmCommand;
import net.zic.ascension.common.command.commands.SlotSkillCommand;
import net.zic.ascension.common.command.commands.TribulationCommand;

public class AscensionCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("ascension")
                .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))

                /*

                All currently implemented commands


                    /ascension cultivation set <targets> <path> <majorRealm> <minorRealm> [progress]



                 */


                .then(SetRealmCommand.build())
                .then(Commands.literal("skill")
                        .then(SlotSkillCommand.buildSlot())
                        .then(SlotSkillCommand.buildUnSlot())
                        .then(SlotSkillCommand.buildDisplay())
                )
                .then(TribulationCommand.build())


        );
    }
}
