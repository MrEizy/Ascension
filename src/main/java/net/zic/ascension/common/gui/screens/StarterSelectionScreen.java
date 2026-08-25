package net.zic.ascension.common.gui.screens;

import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.screen.EasyScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.zic.ascension.common.gui.elements.starter.StarterSelectionContainer;
import net.zic.ascension.common.starter.StarterSelectionStage;

import java.util.List;

public class StarterSelectionScreen extends EasyScreen {
    public StarterSelectionScreen(
            StarterSelectionStage stage,
            List<Identifier> options,
            Identifier selectedBloodline
    ) {
        super(Component.translatable("gui.ascension.starter.title"));

        UIFrame frame = getUIFrame();
        frame.setPauseGame(false);
        frame.setRoot(new StarterSelectionContainer(frame, stage, options));
    }

    @Override
    public void onClose() {
        super.onClose();

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            minecraft.player.sendOverlayMessage(
                    Component.translatable("gui.ascension.starter.incomplete_notice")
            );
        }
    }
}
