package net.zic.ascension.common.gui.elements.general;

import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.elements.EasyTooltip;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import org.joml.Vector2i;

import java.util.List;

public class AscensionTooltip extends EasyTooltip {

    public AscensionTooltip(UIFrame frame) {
        super(frame);
    }

    public void setText(Component component) {
        this.component = component == null ? Component.empty() : component;
    }

    @Override
    public void appendText(Component component) {
        this.component = Component.empty().append(this.component).append(component);
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        if (component == null || component.getString().isBlank()) {
            return;
        }

        Font font = Minecraft.getInstance().font;

        graphics.tooltip(
                font,
                List.of(createTooltipComponent(font)),
                0,
                0,
                (
                        screenWidth,
                        screenHeight,
                        anchorX,
                        anchorY,
                        tooltipWidth,
                        tooltipHeight
                ) -> new Vector2i(0, 0),
                null
        );
    }

    private ClientTooltipComponent createTooltipComponent(Font font) {
        return new ClientTooltipComponent() {
            @Override
            public int getHeight(Font ignored) {
                int width = Math.max(1, getWidth(font));

                return font.lineHeight * font.split(component, width).size();
            }

            @Override
            public int getWidth(Font ignored) {
                return font.width(component);
            }

            @Override
            public void extractText(GuiGraphicsExtractor graphics, Font ignored, int x, int y) {
                graphics.text(font, component, x, y, 0xFFFFFFFF, false);
            }
        };
    }
}