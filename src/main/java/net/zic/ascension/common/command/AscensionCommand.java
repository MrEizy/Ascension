package net.zic.ascension.common.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.permissions.Permissions;
import net.zic.ascension.common.command.commands.*;
import net.zic.ascension.mob_cultivation.command.MobCultivationCommands;

public class AscensionCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("ascension")
                .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))

                /*

                All currently implemented commands


                    /ascension cultivation set <targets> <path> <majorRealm> <minorRealm> [progress]



                 */


                .then(CultivationCommand.build())
                .then(Commands.literal("skill")
                        .then(SlotSkillCommand.buildSlot())
                        .then(SlotSkillCommand.buildUnSlot())
                        .then(SlotSkillCommand.buildDisplay())
                )
                .then(TribulationCommand.build())
                .then(ChunkCommand.build())
                .then(WorldgenDebugCommand.build())
                .then(AffinityCommand.build())
                .then(AscensionGive.build())
                .then(MobCultivationCommands.build())


        );
    }
}
