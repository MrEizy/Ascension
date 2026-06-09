package net.zic.ascension.client.keybind;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.zic.ascension.common.item.artifacts.base_templates.BaseTabletOfDestruction;
import net.zic.ascension.network.CycleDropModePacket;

/**
 * Fires the server packet whenever the cycle-drop-mode key is pressed
 * and the player is holding a Destruction Tablet.
 * Register on Bus.GAME with Dist.CLIENT.
 */
public class TabletKeybindHandler {

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        while (ModKeybinds.CYCLE_MODE.consumeClick()) {
            ItemStack main = mc.player.getMainHandItem();
            ItemStack off  = mc.player.getOffhandItem();
            boolean holding = main.getItem() instanceof BaseTabletOfDestruction
                    || off.getItem()  instanceof BaseTabletOfDestruction;
            if (holding) {
                ClientPacketDistributor.sendToServer(new CycleDropModePacket());
            }
        }
    }
}
