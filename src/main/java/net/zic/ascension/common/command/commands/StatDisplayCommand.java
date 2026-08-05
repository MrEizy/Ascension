package net.zic.ascension.common.command.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.zic.ascension.api.ascension.capabilities.AscensionEntityDataProvider;
import net.zic.ascension.api.ascension.capabilities.CoreCapabilities;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.entity.AscensionEntityData;
import net.zic.ascension.api.ascension.core.path.Path;
import net.zic.ascension.api.ascension.core.path.PathInstance;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.rpg_engine.source.OriginSource;

import net.zic.zenithlib.common.ZenithAttachments;
import net.zic.zenithlib.stats.Stat;
import net.zic.zenithlib.stats.ZenithStatHolder;

public class StatDisplayCommand {


    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("stats")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();

                            AscensionEntityDataProvider holder = player.getCapability(CoreCapabilities.ASCENSION_ENTITY_DATA_PROVIDER_CAPABILITY);
                            if(holder == null) return 0;
                            AscensionEntityData data = holder.getData(player);

                            OriginSource originSource =holder.getData(player).getSource();

                            ZenithStatHolder statHolder = player.getData(ZenithAttachments.STAT_HOLDER);
                            player.sendSystemMessage(Component.literal("===Stats==="));
                            for(Stat stat: statHolder.getStats()){
                                player.sendSystemMessage(Component.literal(stat.getName()+":"+statHolder.getStat(stat)));
                            }
                            player.sendSystemMessage(Component.literal("===Data==="));
                            player.sendSystemMessage(Component.literal("Physique :" +(AscensionOriginSourceHelper.getPhysiqueId(originSource) == null ? "none":AscensionOriginSourceHelper.getPhysiqueId(originSource))));
                            for(Identifier bloodline : AscensionOriginSourceHelper.getBloodlines(originSource)){
                                player.sendSystemMessage(Component.literal("Bloodline : "+bloodline+"("+AscensionOriginSourceHelper.getBloodlineData(originSource,bloodline).getPurity()+"%)"));
                            }
                            player.sendSystemMessage(Component.literal("===Skills==="));
                            for(Identifier skill : AscensionOriginSourceHelper.getSkills(originSource)){
                                player.sendSystemMessage(Component.literal(skill.toString()));
                            }
                            player.sendSystemMessage(Component.literal("Suppressed : "+data.isCultivationSuppressed()));
                            player.sendSystemMessage(Component.literal("===Paths==="));
                            for(Identifier pathId : AscensionOriginSourceHelper.getPaths(originSource)){
                                Path path = CoreRegistries.safeAccess(CoreRegistries.PATH_REGISTRY,pathId,data.getSource().getRegistryAccess());
                                player.sendSystemMessage(path.name());
                                PathInstance pathInstance = AscensionOriginSourceHelper.getPathInstance(originSource,pathId);

                                player.sendSystemMessage(Component.literal("realm : ").append(path.getRealmName(pathInstance.getCurrentMajorRealm(),pathInstance.getCurrentMinorRealm())));
                                player.sendSystemMessage(Component.literal("progress : "+pathInstance.getProgress()));


                            }
                            return 1;
                        })
            );

    }
}