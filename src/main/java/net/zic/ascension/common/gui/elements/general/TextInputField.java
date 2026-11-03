package net.zic.ascension.common.gui.elements.general;

import com.mojang.blaze3d.platform.InputConstants;
import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.events.EasyEvents;
import net.lucent.easygui.gui.events.type.EasyEvent;
import net.lucent.easygui.gui.events.type.EasyMouseEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

/**
 * Single line text input. easygui has no character event, so the owning screen
 * has to forward charTyped/keyPressed to the focused field.
 */
public class TextInputField extends RenderableElement {
    private static final int TEXT_COLOR = 0xFFFFFFFF;
    private static final int PLACEHOLDER_COLOR = 0xFF6F7C8F;
    private static final int CURSOR_COLOR = 0xFF3FB4F0;

    private final Component placeholder;
    private final int maximumLength;
    private final Consumer<String> changeListener;

    private String value = "";
    private boolean selected;

    public TextInputField(
            UIFrame frame,
            int width,
            int height,
            Component placeholder,
            int maximumLength,
            Consumer<String> changeListener
    ) {
        super(frame);
        this.placeholder = placeholder == null ? Component.empty() : placeholder;
        this.maximumLength = Math.max(1, maximumLength);
        this.changeListener = changeListener;
        setWidth(width);
        setHeight(height);
        setShouldCull(true);

        //targeted so only the visible, hovered field can be selected
        addEventListener(EasyEvents.MOUSE_DOWN_EVENT, this::onMouseDown);
        addEventListener(EasyEvents.GLOBAL_MOUSE_DOWN_EVENT, this::onGlobalMouseDown);
    }

    public String getValue() {
        return value;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        if (selected) {
            setFocused(true);
        }
    }

    public boolean charTyped(CharacterEvent event) {
        if (!selected || !event.isAllowedChatCharacter()) {
            return false;
        }

        String typed = event.codepointAsString();
        if (value.length() + typed.length() <= maximumLength) {
            setValue(value + typed);
        }
        return true;
    }

    public boolean keyPressed(KeyEvent event) {
        if (!selected) {
            return false;
        }

        return switch (event.key()) {
            case GLFW.GLFW_KEY_BACKSPACE -> {
                if (!value.isEmpty()) {
                    setValue(value.substring(0, value.offsetByCodePoints(value.length(), -1)));
                }
                yield true;
            }
            case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER, GLFW.GLFW_KEY_ESCAPE -> {
                selected = false;
                yield true;
            }
            default -> false;
        };
    }

    private void setValue(String next) {
        if (next.equals(value)) {
            return;
        }
        value = next;
        if (changeListener != null) {
            changeListener.accept(value);
        }
    }

    private void onMouseDown(EasyEvent event) {
        if (!(event instanceof EasyMouseEvent mouseEvent) || !isLeftButton(mouseEvent)) {
            return;
        }
        if (isPointBounded(mouseEvent.getMouseX(), mouseEvent.getMouseY())) {
            setSelected(true);
        }
    }

    private void onGlobalMouseDown(EasyEvent event) {
        if (!selected || !(event instanceof EasyMouseEvent mouseEvent)) {
            return;
        }
        if (!isPointBounded(mouseEvent.getMouseX(), mouseEvent.getMouseY())) {
            selected = false;
        }
    }

    private static boolean isLeftButton(EasyMouseEvent mouseEvent) {
        MouseButtonEvent buttonEvent = mouseEvent.getMouseEvent();
        return buttonEvent != null && buttonEvent.button() == InputConstants.MOUSE_BUTTON_LEFT;
    }

    @Override
    public void createCullRegion(GuiGraphicsExtractor graphics) {
        graphics.enableScissor(0, 0, getWidth(), getHeight());
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        Font font = Minecraft.getInstance().font;
        int textY = Math.max(0, (getHeight() - font.lineHeight + 2) / 2);

        if (value.isEmpty() && !selected) {
            graphics.text(font, placeholder, 2, textY, PLACEHOLDER_COLOR, false);
            return;
        }

        String visible = visibleTail(font, value, getWidth() - 5);
        graphics.text(font, visible, 2, textY, TEXT_COLOR, false);

        if (selected && (System.currentTimeMillis() / 500L) % 2L == 0L) {
            int cursorX = Math.min(getWidth() - 2, 3 + font.width(visible));
            graphics.fill(cursorX, textY - 1, cursorX + 1, textY + font.lineHeight - 1, CURSOR_COLOR);
        }
    }

    //keeps the end of the text in view once it is wider than the field
    private static String visibleTail(Font font, String text, int maximumWidth) {
        String visible = text;
        while (!visible.isEmpty() && font.width(visible) > maximumWidth) {
            visible = visible.substring(visible.offsetByCodePoints(0, 1));
        }
        return visible;
    }
}
