package net.zic.ascension.common.gui.screens;

import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.screen.EasyScreen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.zic.ascension.common.gui.elements.general.TextInputField;
import net.zic.ascension.common.gui.elements.introspection.IntrospectionContainer;

public class IntrospectionScreen extends EasyScreen {
    public IntrospectionScreen() {
        super(Component.translatable("gui.ascension.introspection.title"));

        UIFrame frame = getUIFrame();
        frame.setPauseGame(false);
        frame.setRoot(new IntrospectionContainer(frame));
    }

    //easygui does not forward typed characters, so text fields get them from here
    @Override
    public boolean charTyped(CharacterEvent event) {
        if (getUIFrame().getFocusedElement() instanceof TextInputField field && field.charTyped(event)) {
            return true;
        }
        return super.charTyped(event);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (getUIFrame().getFocusedElement() instanceof TextInputField field && field.keyPressed(event)) {
            return true;
        }
        return super.keyPressed(event);
    }
}
