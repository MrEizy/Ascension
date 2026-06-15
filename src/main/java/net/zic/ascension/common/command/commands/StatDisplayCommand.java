package net.zic.ascension.common.command.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.zic.ascension.api.capabilities.AscensionEntityDataHolder;
import net.zic.ascension.api.capabilities.CoreCapabilities;
import net.zic.ascension.api.core.CoreRegistries;
import net.zic.ascension.api.core.entity.AscensionEntityData;
import net.zic.ascension.api.core.path.Path;
import net.zic.ascension.api.core.path.PathData;
import net.zic.ascension.impl.core.path.foundation.FoundationPath;
import net.zic.ascension.impl.core.path.foundation.FoundationPathData;
import net.zic.zenithlib.stats.Stat;

public class StatDisplayCommand {


    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("stats")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();

                            AscensionEntityDataHolder holder = player.getCapability(CoreCapabilities.ASCENSION_ENTITY_DATA_HOLDER_CAPABILITY);
                            if(holder == null) return 0;
                            AscensionEntityData data = holder.getData(player);
                            player.sendSystemMessage(Component.literal("===Stats==="));
                            for(Stat stat:holder.getData(player).getSource().getAllStats()){
                                //TODO fix later to include name
                                player.sendSystemMessage(Component.literal(stat.getName()+":"+holder.getData(player).getSource().getValue(stat)));
                            }
                            player.sendSystemMessage(Component.literal("===Data==="));
                            player.sendSystemMessage(Component.literal("Physique :" +(data.getSource().getPhysique() == null ? "none":data.getSource().getPhysique())));
                            for(Identifier bloodline : data.getSource().getBloodlines()){
                                player.sendSystemMessage(Component.literal("Bloodline : "+bloodline+"("+data.getSource().getBloodlineData(bloodline).getPurity()+"%)"));
                            }
                            player.sendSystemMessage(Component.literal("===Skills==="));
                            for(Identifier skill : data.getSource().getSkills()){
                                player.sendSystemMessage(Component.literal(skill.toString()));
                            }
                            player.sendSystemMessage(Component.literal("Suppressed : "+data.isCultivationSuppressed()));
                            player.sendSystemMessage(Component.literal("===Paths==="));
                            for(Identifier path : data.getSource().getPaths()){
                                Path pathInstance = CoreRegistries.safeAccess(CoreRegistries.PATH_REGISTRY,path,data.getSource().getRegistryAccess());
                                player.sendSystemMessage(pathInstance.name());
                                PathData pathData = data.getSource().getPathData(path);
                                player.sendSystemMessage(Component.literal("realm : ").append(pathData.getRealmName(pathData.getMajorRealm(),pathData.getMinorRealm(),data.getSource().getRegistryAccess())));
                                player.sendSystemMessage(Component.literal("progress : "+pathData.getProgress()));
                                player.sendSystemMessage(Component.literal("technique : "+pathData.getCurrentTechnique()));
                                if(pathData instanceof FoundationPathData foundationPathData && pathInstance instanceof FoundationPath foundationPath){
                                    player.sendSystemMessage(Component.literal("Foundation : ").append(
                                            foundationPath.getFoundationRealmName(
                                                    foundationPathData.getMajorRealm(),
                                                    foundationPathData.getFoundationRealm(foundationPathData.getMajorRealm())
                                            )));
                                    System.out.println(foundationPathData.getFoundationRealm(foundationPathData.getMajorRealm()));
                                    player.sendSystemMessage(Component.literal(
                                            "Foundation Progress : "+
                                                    foundationPathData.getFoundationRealmProgress(foundationPathData.getMajorRealm())
                                    ));
                                }
                            }
                            return 1;
                        })
            );

    }
}