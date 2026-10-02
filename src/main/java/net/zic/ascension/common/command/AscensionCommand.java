package net.zic.ascension.common.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.permissions.Permissions;
import net.zic.ascension.common.command.commands.*;
import net.zic.ascension.mob_cultivation.command.MobCultivationCommands;

public final class AscensionCommand {
    private AscensionCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("ascension")
                .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                .then(CultivationCommand.build())
                .then(Commands.literal("skill")
                        .then(SlotSkillCommand.buildSlot())
                        .then(SlotSkillCommand.buildUnSlot())
                        .then(SlotSkillCommand.buildDisplay())
                        .then(SkillMasteryCommand.build())
                )
                .then(TribulationCommand.build())
                .then(ChunkCommand.build())
                .then(WorldgenDebugCommand.build())
                .then(AffinityCommand.build())
                .then(AlchemyCommand.build())
                .then(AscensionGive.build())
                .then(MobCultivationCommands.build())
        );
    }
}
