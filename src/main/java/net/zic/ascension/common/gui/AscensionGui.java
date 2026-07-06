package net.zic.ascension.common.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.zic.ascension.common.gui.screens.IntrospectionScreen;
import net.zic.ascension.common.gui.screens.StarterSelectionScreen;
import net.zic.ascension.common.starter.StarterSelectionStage;

import java.util.List;

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

    public static void openStarterSelection(
            StarterSelectionStage stage,
            List<Identifier> options,
            Identifier selectedBloodline
    ) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }

        if (stage == StarterSelectionStage.COMPLETE) {
            if (minecraft.screen instanceof StarterSelectionScreen) {
                minecraft.setScreen(null);
            }
            return;
        }

        minecraft.setScreen(new StarterSelectionScreen(stage, options, selectedBloodline));
    }
}
