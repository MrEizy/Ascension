package net.zic.ascension.common.gui;

import net.minecraft.client.Minecraft;
import net.zic.ascension.common.gui.screens.IntrospectionScreen;

public final class AscensionGui {
    private AscensionGui() {
    }

    public static void openIntrospection() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }
        minecraft.setScreen(new IntrospectionScreen());
    }
}
