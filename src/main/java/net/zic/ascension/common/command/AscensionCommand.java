package net.zic.ascension.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.zic.ascension.common.command.commands.*;
import net.zic.ascension.common.command.commands.mob.MobCommands;
import net.zic.ascension.common.command.commands.mob.NpcSocialCommands;

public final class AscensionCommand {
    private AscensionCommand() {
    }

    public static boolean isAdmin(CommandSourceStack source) {
        return source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER);
    }

    public static boolean isPlayer(CommandSourceStack source) {
        return source.getEntity() instanceof ServerPlayer;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> admin(LiteralArgumentBuilder<CommandSourceStack> command) {
        return command.requires(command.getRequirement().and(AscensionCommand::isAdmin));
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("ascension")
                .requires(source -> isAdmin(source) || isPlayer(source))
                .then(PlayerProgressionCommand.buildBloodline())
                .then(PlayerProgressionCommand.buildTechnique())
                .then(PlayerProgressionCommand.buildReset())
                .then(admin(CultivationCommand.build()))
                .then(admin(Commands.literal("skill")
                        .then(SlotSkillCommand.buildSlot())
                        .then(SlotSkillCommand.buildUnSlot())
                        .then(SlotSkillCommand.buildUnslotAlias())
                        .then(SlotSkillCommand.buildDisplay())
                        .then(SkillMasteryCommand.build())
                        .then(SkillGrantCommand.buildAdd())
                        .then(SkillGrantCommand.buildRemove())
                        .then(SkillGrantCommand.buildList())
                ))
                .then(admin(Commands.literal("mobs")
                        .then(MobCommands.build())
                        .then(NpcSocialCommands.build())
                        .then(NpcSocialCommands.affiliation())
                        .then(NpcSocialCommands.reputation())
                ))
                .then(admin(ViewStats.build()))
                .then(admin(Commands.literal("inspect")
                        .then(ViewStats.buildAs("stats"))
                        .then(MobCommands.buildAs("mob"))
                        .then(CultivationCommand.buildInspect())
                        .then(AffinityCommand.buildAs("affinity"))
                        .then(ChunkCommand.buildAs("chunk"))))
                .then(admin(Commands.literal("debug")
                        .then(WorldgenDebugCommand.build())
                        .then(ChunkCommand.buildAs("chunk"))))
                .then(admin(TribulationCommand.build()))
                .then(admin(ChunkCommand.build()))
                .then(admin(WorldgenDebugCommand.build()))
                .then(admin(AffinityCommand.build()))
                .then(admin(EffectCommand.build()))
                .then(admin(RealmTravelCommand.buildAscend()))
                .then(admin(RealmTravelCommand.buildDescend()))
                .then(admin(AscensionGive.build()))
                .then(admin(InnerWorldCommand.build()))
                .then(admin(GoldenCoreCommand.build()))
        );
    }
}
