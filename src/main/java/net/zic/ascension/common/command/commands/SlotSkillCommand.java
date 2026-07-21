package net.zic.ascension.common.command.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.zic.ascension.api.ascension.capabilities.AscensionEntityDataProvider;
import net.zic.ascension.api.ascension.capabilities.CoreCapabilities;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.skill.castable.CastableSkill;
import net.zic.ascension.api.ascension.core.source.OriginSource;
import net.zic.ascension.common.data_attachements.AscensionAttachments;
import net.zic.ascension.skill_casting.SkillCastHandler;

import java.util.HashSet;
import java.util.Set;

public class SlotSkillCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> buildSlot() {
        return Commands.literal("slot")
                .then(Commands.argument("slot", IntegerArgumentType.integer(0))
                        .then(Commands.argument("skill", IdentifierArgument.id())
                                .suggests((context, builder) -> {
                                    Player player = context.getSource().getPlayer();
                                    AscensionEntityDataProvider holder = player.getCapability(
                                            CoreCapabilities.ASCENSION_ENTITY_DATA_PROVIDER_CAPABILITY
                                    );
                                    if (holder == null) {
                                        return SharedSuggestionProvider.suggestResource(
                                                Set.of(),
                                                builder
                                        );
                                    }

                                    Set<Identifier> validSkills = new HashSet<>();
                                    for (Identifier skill : holder.getData(player)
                                            .getSource()
                                            .getSkills()) {
                                        if (CoreRegistries.safeAccess(
                                                CoreRegistries.SKILL_REGISTRY,
                                                skill,
                                                context.getSource().registryAccess()
                                        ) instanceof CastableSkill) {
                                            validSkills.add(skill);
                                        }
                                    }
                                    return SharedSuggestionProvider.suggestResource(
                                            validSkills,
                                            builder
                                    );
                                })
                                .executes(SlotSkillCommand::slotSkill)));
    }

    public static LiteralArgumentBuilder<CommandSourceStack> buildUnSlot() {
        return Commands.literal("unSlot")
                .then(Commands.argument("slot", IntegerArgumentType.integer(0))
                        .executes(SlotSkillCommand::unSlot));
    }

    public static LiteralArgumentBuilder<CommandSourceStack> buildDisplay() {
        return Commands.literal("show").executes(SlotSkillCommand::show);
    }

    private static int show(CommandContext<CommandSourceStack> context)
            throws CommandSyntaxException {
        Player player = context.getSource().getPlayer();
        SkillCastHandler handler = player.getData(
                AscensionAttachments.ASCENSION_SKILL_CAST_HANDLER
        );

        for (int slot = 0; slot < handler.getMaxSlots(); slot++) {
            Identifier skill = handler.getSkill(slot);
            player.sendSystemMessage(Component.literal(
                    "slot " + slot + " : " + (skill == null ? "null" : skill)
            ));
        }
        return 1;
    }

    private static int slotSkill(CommandContext<CommandSourceStack> context)
            throws CommandSyntaxException {
        Identifier skill = IdentifierArgument.getId(context, "skill");
        int slot = IntegerArgumentType.getInteger(context, "slot");
        Player player = context.getSource().getPlayer();

        AscensionEntityDataProvider holder = player.getCapability(
                CoreCapabilities.ASCENSION_ENTITY_DATA_PROVIDER_CAPABILITY
        );
        if (holder == null) {
            context.getSource().sendFailure(Component.literal("missing entity data"));
            return 0;
        }

        OriginSource source = holder.getData(player).getSource();
        SkillCastHandler handler = player.getData(
                AscensionAttachments.ASCENSION_SKILL_CAST_HANDLER
        );

        if (!source.hasSkill(skill)) {
            context.getSource().sendFailure(Component.literal(
                    "you do not have skill " + skill
            ));
            return 0;
        }
        if (!(CoreRegistries.safeAccess(
                CoreRegistries.SKILL_REGISTRY,
                skill,
                source.getRegistryAccess()
        ) instanceof CastableSkill)) {
            context.getSource().sendFailure(Component.literal(skill + " is not castable"));
            return 0;
        }
        if (!isValidSlot(handler, slot)) {
            sendInvalidSlot(context, handler, slot);
            return 0;
        }

        Identifier oldSkill = handler.getSkill(slot);
        handler.slotSkill(skill, slot);
        handler.resolve();

        context.getSource().sendSuccess(
                () -> Component.literal(String.format(
                        "slot %d : %s -> %s",
                        slot,
                        oldSkill == null ? "none" : oldSkill,
                        skill
                )),
                true
        );
        return 1;
    }

    private static int unSlot(CommandContext<CommandSourceStack> context)
            throws CommandSyntaxException {
        int slot = IntegerArgumentType.getInteger(context, "slot");
        Player player = context.getSource().getPlayer();
        SkillCastHandler handler = player.getData(
                AscensionAttachments.ASCENSION_SKILL_CAST_HANDLER
        );

        if (!isValidSlot(handler, slot)) {
            sendInvalidSlot(context, handler, slot);
            return 0;
        }

        Identifier oldSkill = handler.getSkill(slot);
        handler.slotSkill(null, slot);
        handler.resolve();

        context.getSource().sendSuccess(
                () -> Component.literal(String.format(
                        "slot %d : %s -> none",
                        slot,
                        oldSkill == null ? "none" : oldSkill
                )),
                true
        );
        return 1;
    }

    private static boolean isValidSlot(SkillCastHandler handler, int slot) {
        return slot >= 0 && slot < handler.getMaxSlots();
    }

    private static void sendInvalidSlot(
            CommandContext<CommandSourceStack> context,
            SkillCastHandler handler,
            int slot
    ) {
        context.getSource().sendFailure(Component.literal(
                "slot " + slot + " is out of range for max slots "
                        + handler.getMaxSlots()
        ));
    }
}
