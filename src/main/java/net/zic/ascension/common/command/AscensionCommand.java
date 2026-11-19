package net.zic.ascension.common.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.permissions.Permissions;
import net.zic.ascension.common.command.commands.*;
import net.zic.ascension.common.command.commands.mob.MobCommands;
import net.zic.ascension.common.command.commands.mob.NpcSocialCommands;

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
                        .then(SlotSkillCommand.buildUnslotAlias())
                        .then(SlotSkillCommand.buildDisplay())
                        .then(SkillMasteryCommand.build())
                        .then(SkillGrantCommand.buildAdd())
                        .then(SkillGrantCommand.buildRemove())
                        .then(SkillGrantCommand.buildList())
                )
                .then(Commands.literal("mobs")
                        .then(MobCommands.build())
                        .then(NpcSocialCommands.build())
                        .then(NpcSocialCommands.affiliation())
                        .then(NpcSocialCommands.reputation())
                )
                .then(ViewStats.build())
                .then(Commands.literal("inspect")
                        .then(ViewStats.buildAs("stats"))
                        .then(MobCommands.buildAs("mob"))
                        .then(CultivationCommand.buildInspect())
                        .then(AffinityCommand.buildAs("affinity"))
                        .then(ChunkCommand.buildAs("chunk")))
                .then(Commands.literal("debug")
                        .then(WorldgenDebugCommand.build())
                        .then(ChunkCommand.buildAs("chunk")))
                .then(TribulationCommand.build())
                .then(ChunkCommand.build())
                .then(WorldgenDebugCommand.build())
                .then(AffinityCommand.build())
                .then(EffectCommand.build())
                .then(RealmTravelCommand.buildAscend())
                .then(RealmTravelCommand.buildDescend())
                .then(AscensionGive.build())
                .then(InnerWorldCommand.build())
                .then(GoldenCoreCommand.build())
        );
    }
}
