package net.zic.ascension.common.gui.screens;

import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.screen.EasyScreen;
import net.minecraft.network.chat.Component;
import net.zic.ascension.common.gui.elements.introspection.IntrospectionContainer;

public class IntrospectionScreen extends EasyScreen {
    public IntrospectionScreen() {
        super(Component.translatable("gui.ascension.introspection.title"));

        UIFrame frame = getUIFrame();
        frame.setPauseGame(false);
        frame.setRoot(new IntrospectionContainer(frame));
    }
}
