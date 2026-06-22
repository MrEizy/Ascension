package net.zic.ascension.common.command.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.zic.ascension.api.core.CoreRegistries;
import net.zic.ascension.chunks.atmospheric_qi.ChunkQiContainer;
import net.zic.ascension.common.data_attachements.AscensionAttachments;

public class ChunkCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("chunk")
                .then(Commands.literal("view")
                        .executes(ChunkCommand::viewAtmosphericQi));
    }

    private static int viewAtmosphericQi(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {

        ServerPlayer player = context.getSource().getPlayer();

        ChunkQiContainer chunkQiContainer = player.level().getChunk(player.blockPosition()).getData(AscensionAttachments.ASCENSION_CHUNK_QI_CONTAINER);

        player.sendSystemMessage(Component.literal("===Chunk==="));
        player.sendSystemMessage(Component.literal(chunkQiContainer.getEnergy()+"/"+chunkQiContainer.getEnergyCap()));
        player.sendSystemMessage(Component.literal(chunkQiContainer.getEnergyRegenRate()+"/s"));
        player.sendSystemMessage(Component.literal("Affinities:"));
        for(Identifier id : chunkQiContainer.getAllAffinities()){
            player.sendSystemMessage(Component.literal(id +":"+chunkQiContainer.getAffinity(id)));
        }

        return 1;
    }
}