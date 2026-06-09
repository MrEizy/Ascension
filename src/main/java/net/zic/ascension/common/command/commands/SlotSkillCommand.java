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
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.entity.player.Player;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.capabilities.AscensionEntityDataHolder;
import net.zic.ascension.api.capabilities.CoreCapabilities;
import net.zic.ascension.api.core.CoreRegistries;
import net.zic.ascension.api.core.skill.castable.CastableSkill;
import net.zic.ascension.api.core.source.OriginSource;
import net.zic.ascension.common.data_attachements.AscensionAttachments;
import net.zic.ascension.skill_casting.SkillCastHandler;

import java.util.HashSet;
import java.util.Set;

public class SlotSkillCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> buildSlot() {
        return Commands.literal("slot")
                .then(Commands.argument("slot",IntegerArgumentType.integer())
                    .then(Commands.argument("skill",IdentifierArgument.id())
                            .suggests(((context, builder) -> {
                                AscensionEntityDataHolder holder =
                                        context.getSource().getPlayer().getCapability(CoreCapabilities.ASCENSION_ENTITY_DATA_HOLDER_CAPABILITY);
                                if(holder == null) return SharedSuggestionProvider.suggestResource(Set.of(),builder);
                                HashSet<Identifier> validSkills = new HashSet<>();
                                for(Identifier skill : holder.getData(context.getSource().getPlayer()).getSource().getSkills()){
                                    if(!(CoreRegistries.safeAccess(CoreRegistries.SKILL_REGISTRY,skill,context.getSource().registryAccess()) instanceof CastableSkill castableSkill))continue;
                                    validSkills.add(skill);
                                }
                                return SharedSuggestionProvider.suggestResource(
                                        validSkills,
                                        builder
                                );
                        }))
                        .executes(
                                SlotSkillCommand::slotSkill
                        )
                    )

                );
    }
    public static LiteralArgumentBuilder<CommandSourceStack> buildUnSlot() {
        return Commands.literal("unSlot")
                 .then(Commands.argument("slot",IntegerArgumentType.integer())
                        .executes(
                                SlotSkillCommand::unSlot
                        )
                );
    }
    public static LiteralArgumentBuilder<CommandSourceStack> buildDisplay(){
        return Commands.literal("show")
                .executes(SlotSkillCommand::show);
    }
    private static int show(CommandContext<CommandSourceStack> context) throws CommandSyntaxException{

        Player player = context.getSource().getPlayer();
        SkillCastHandler handler = player.getData(AscensionAttachments.ASCENSION_SKILL_CAST_HANDLER);




        for(int i =0;i<handler.getMaxSlots();i++){
            context.getSource().getPlayer().sendSystemMessage(
                    Component.literal("slot "+i+" : "+(handler.getSkill(i) == null ?"null" :handler.getSkill(i)))
            );
        }
        return 1;
    }
    private static int slotSkill(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Identifier skill = IdentifierArgument.getId(context, "skill");
        int slot = IntegerArgumentType.getInteger(context, "slot");
        Player player = context.getSource().getPlayer();
        AscensionEntityDataHolder holder = player.getCapability(CoreCapabilities.ASCENSION_ENTITY_DATA_HOLDER_CAPABILITY);
        if(holder == null){
            context.getSource().sendFailure(Component.literal(
                    "missing entity data"
            ));
            return 0;
        }

        OriginSource source = holder.getData(player).getSource();
        SkillCastHandler handler = player.getData(AscensionAttachments.ASCENSION_SKILL_CAST_HANDLER);

        if(!source.hasSkill(skill)){
            context.getSource().sendFailure(Component.literal(
                    "you do not have skill "+skill
            ));
            return 0;
        }
        if(!(CoreRegistries.safeAccess(CoreRegistries.SKILL_REGISTRY,skill,source.getRegistryAccess())instanceof CastableSkill castableSkill)){
            context.getSource().sendFailure(Component.literal(
                    skill +" is not castable"
            ));
            return 0;
        }

        if(slot >= handler.getMaxSlots()){
            context.getSource().sendFailure(Component.literal(
                    "slot "+slot +" is out of range for max slots " + handler.getMaxSlots()
            ));
            return 0;
        }

        Identifier oldSlottedSkill = handler.getSkill(slot);

        handler.slotSkill(skill,slot);


        String feedbackToSource = String.format(
                "slot %d : %s -> %s",
                slot,
                (oldSlottedSkill == null ? "none":oldSlottedSkill.toString()),
                skill);

        context.getSource().sendSuccess(() -> Component.literal(feedbackToSource), true);

        return 1;
    }
    public static int unSlot(CommandContext<CommandSourceStack> context) throws CommandSyntaxException{
        int slot = IntegerArgumentType.getInteger(context, "slot");
        Player player = context.getSource().getPlayer();
        SkillCastHandler handler = player.getData(AscensionAttachments.ASCENSION_SKILL_CAST_HANDLER);
        if(slot >= handler.getMaxSlots()){
            context.getSource().sendFailure(Component.literal(
                    "slot "+slot +" is out of range for max slots " + handler.getMaxSlots()
            ));
            return 0;
        }

        Identifier oldSlottedSkill = handler.getSkill(slot);

        handler.slotSkill(null,slot);

        String feedbackToSource = String.format(
                "slot %d : %s -> %s",
                slot,
                (oldSlottedSkill == null ? "none":oldSlottedSkill.toString()),
                "none");

        context.getSource().sendSuccess(() -> Component.literal(feedbackToSource), true);

        return 1;

    }
}
