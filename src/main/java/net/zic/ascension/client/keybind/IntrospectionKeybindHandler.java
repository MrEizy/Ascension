package net.zic.ascension.client.keybind;

import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.zic.ascension.common.gui.AscensionGui;

public final class IntrospectionKeybindHandler {
    private IntrospectionKeybindHandler() {
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            return;
        }

        while (ModKeybinds.OPEN_INTROSPECTION.consumeClick()) {
            if (minecraft.screen == null) {
                AscensionGui.openIntrospection();
            }
        }
    }
}
