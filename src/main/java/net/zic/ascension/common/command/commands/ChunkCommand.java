package net.zic.ascension.common.command.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.zic.ascension.api.ascension.core.path.PathEffectValueUtil;
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
        for(Identifier id : chunkQiContainer.getAllPathBonusesInCategory(PathEffectValueUtil.AFFINITY_CATEGORY)){
            player.sendSystemMessage(Component.literal(id +":"+chunkQiContainer.getPathBonus(PathEffectValueUtil.AFFINITY_CATEGORY,id)));
        }

        return 1;
    }
}