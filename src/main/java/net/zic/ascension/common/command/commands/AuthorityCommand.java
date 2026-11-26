package net.zic.ascension.common.command.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.impl.core.cultivation.CultivationAuthority;

import java.util.Locale;

public final class AuthorityCommand {
    private AuthorityCommand() {}

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("authority")
                .then(Commands.argument("entity", EntityArgument.entity()).executes(AuthorityCommand::inspect));
    }

    private static int inspect(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if (!(EntityArgument.getEntity(context, "entity") instanceof LivingEntity entity)) {
            context.getSource().sendFailure(Component.literal("Authority requires a living entity"));
            return 0;
        }
        try {
            CultivationAuthority.Snapshot authority = CultivationAuthority.read(entity);
            context.getSource().sendSuccess(() -> Component.literal(String.format(Locale.ROOT,
                    "Authority of %s: Body %.6g | Essence %.6g | Soul %.6g",
                    entity.getName().getString(), authority.body(), authority.essence(), authority.soul())), false);
            double[] defense = authority.defensiveAuthorities();
            context.getSource().sendSuccess(() -> Component.literal(String.format(Locale.ROOT,
                    "Effective defence: Body %.6g | Essence %.6g | Soul %.6g (floor %.1f%%)",
                    defense[0], defense[1], defense[2], authority.existentialProtection() * 100.0D)), false);
            return Command.SINGLE_SUCCESS;
        } catch (IllegalArgumentException | IllegalStateException exception) {
            context.getSource().sendFailure(Component.literal(exception.getMessage()));
            return 0;
        }
    }
}
