package net.zic.ascension.common.command.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.resources.Identifier;
import net.minecraft.server.permissions.Permissions;
import net.zic.ascension.api.capabilities.AscensionEntityDataHolder;
import net.zic.ascension.api.capabilities.CoreCapabilities;
import net.zic.ascension.api.core.CoreRegistries;
import net.zic.ascension.api.core.skill.castable.CastableSkill;

import java.util.HashSet;
import java.util.Set;

public class SelectSlotCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("slot")
                .requires(source -> source.permissions().hasPermission(Permissions.CHAT_SEND_MESSAGES))
                .then(Commands.argument("slot", IntegerArgumentType.integer())
                    .executes(
                            SelectSlotCommand::selectSlot
                    )
                );
    }

    private static int selectSlot(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {

        return 1;
    }
}
