package net.thejadeproject.ascension.common.command.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;

public class ReadChapterCommand {

    private static final Map<String, ResourceLocation> ENTRY_ADVANCEMENTS = Map.of(
            "ch1_1", ResourceLocation.fromNamespaceAndPath("ascension", "journal/read_ch1_1"),
            "ch1_2", ResourceLocation.fromNamespaceAndPath("ascension", "journal/read_ch1_2"),
            "ch1_3", ResourceLocation.fromNamespaceAndPath("ascension", "journal/read_ch1_3")
    );

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("read")
                .then(Commands.literal("chapter")
                        .then(Commands.argument("entry", StringArgumentType.word())
                                .executes(ReadChapterCommand::execute)
                        )
                )
        );
    }

    private static int execute(CommandContext<CommandSourceStack> ctx) {
        String entry = StringArgumentType.getString(ctx, "entry");
        ResourceLocation advancementId = ENTRY_ADVANCEMENTS.get(entry);

        if (advancementId == null) return 0;

        ServerPlayer player;
        try {
            player = ctx.getSource().getPlayerOrException();
        } catch (Exception e) {
            return 0;
        }

        AdvancementHolder holder = player.getServer().getAdvancements().get(advancementId);
        if (holder == null) return 0;

        player.getAdvancements().award(holder, "unlocked");
        return 1;
    }
}
