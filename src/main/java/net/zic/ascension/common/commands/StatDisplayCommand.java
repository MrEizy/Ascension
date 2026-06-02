package net.zic.ascension.common.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.zic.ascension.api.capabilities.AscensionEntityDataHolder;
import net.zic.ascension.api.capabilities.CoreCapabilities;
import net.zic.ascension.api.core.entity.AscensionEntityData;
import net.zic.zenithlib.stats.Stat;

import java.util.Map;

public class StatDisplayCommand {


    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("stats")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();

                            AscensionEntityDataHolder holder = player.getCapability(CoreCapabilities.ASCENSION_ENTITY_DATA_HOLDER_CAPABILITY);
                            if(holder == null) return 0;
                            AscensionEntityData data = holder.getData(player);
                            for(Stat stat:holder.getData(player).getSource().getAllStats()){
                                //TODO fix later to include name
                                player.sendSystemMessage(Component.literal("stat : "+holder.getData(player).getSource().getValue(stat)));
                            }
                            player.sendSystemMessage(Component.literal("max health : "+player.getMaxHealth()));
                            player.sendSystemMessage(Component.literal("Physique :" +(data.getSource().getPhysique() == null ? "none":data.getSource().getPhysique())));
                            for(Identifier bloodline : data.getSource().getBloodlines()){
                                player.sendSystemMessage(Component.literal("Bloodline : "+bloodline+"("+data.getSource().getBloodlineData(bloodline).getPurity()+"%)"));
                            }
                            player.sendSystemMessage(Component.literal("skills:"));
                            for(Identifier skill : data.getSource().getSkills()){
                                player.sendSystemMessage(Component.literal(skill.toString()));
                            }
                            return 1;
                        })
            );

    }
}