package net.zic.ascension.client.keybind;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.zic.ascension.common.item.artifacts.consumable.TabletOfDestructionAscendant;
import net.zic.ascension.network.CycleShapePacket;

public class TabletScrollHandler {

    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        if (!mc.player.isShiftKeyDown()) return;

        ItemStack main = mc.player.getMainHandItem();
        if (!(main.getItem() instanceof TabletOfDestructionAscendant)) return;

        if (event.getScrollDeltaY() == 0) return;

        event.setCanceled(true);

        ClientPacketDistributor.sendToServer(new CycleShapePacket());
    }
}