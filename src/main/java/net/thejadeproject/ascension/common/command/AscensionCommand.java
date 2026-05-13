package net.thejadeproject.ascension.common.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.thejadeproject.ascension.common.command.commands.MobCultivationCommand;
import net.thejadeproject.ascension.common.command.commands.ResetAscensionCommand;
import net.thejadeproject.ascension.common.command.commands.SetCultivationCommand;

public class AscensionCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("ascension")
                .requires(source -> source.hasPermission(2))

                /*

                All currently implemented commands

                    /ascension reset all <targets>
                    /ascension reset attributes <targets>
                    /ascension reset paths <targets>
                    /ascension reset path <targets> <path>
                    /ascension reset skills <targets>
                    /ascension reset techniques <targets>
                    /ascension reset technique <targets> <path>
                    /ascension reset physique <targets>

                    /ascension cultivation set <targets> <path> <majorRealm> <minorRealm> [progress]
                    /ascension cultivation get <target>
                    /ascension cultivation get <target> physique

                    /ascension mob summon entity major_realm stage_out_of_3 ~ ~ ~
                    /ascension mob set @e[type=minecraft:zombie,limit=1,sort=nearest] earth_immortal 3
                    /ascension mob getstats @e[type=entity,limit=1,sort=nearest]

                    these three also work with mob uuid by looking at them :)

                 */

                .then(ResetAscensionCommand.build())
                .then(SetCultivationCommand.build())
                .then(MobCultivationCommand.build())

        );
    }
}