package net.thejadeproject.ascension.refactor_packages.handlers.player;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.data_attachments.ModAttachments;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID,value = Dist.CLIENT)
public class ClientPlayerEventHandlers {


    @SubscribeEvent
    public static void OnClientTick(ClientTickEvent.Pre event){
        if(Minecraft.getInstance().player == null)return;
        Minecraft.getInstance().player.getData(ModAttachments.ENTITY_DATA).getSkillCastHandler().tick(Minecraft.getInstance().player);
    }
}
