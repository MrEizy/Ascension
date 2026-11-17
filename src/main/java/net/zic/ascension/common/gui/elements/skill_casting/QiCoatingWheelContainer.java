package net.zic.ascension.common.gui.elements.skill_casting;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.layout.positioning.rules.PositioningRules;
import net.lucent.easygui.gui.textures.ITextureData;
import net.lucent.easygui.gui.textures.TextureData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.zic.ascension.AscensionCraft;

import java.util.List;

/**
 * The coating wheel: the skill wheel's look, with the Qi Coating elements the player can pick around it by name.
 */
public class QiCoatingWheelContainer extends RenderableElement {
    private static final int WIDTH = 260;
    private static final int HEIGHT = 240;
    private static final int WHEEL_LEFT = 34;
    private static final int WHEEL_TOP = 14;
    private static final float WHEEL_SCALE = 3.0F;
    private static final int BACKGROUND_PASSES = 4;
    private static final int WHEEL_SIZE = 192;
    private static final int CENTER = WHEEL_SIZE / 2;
    private static final float SEGMENT_SCALE = 3.0F;
    private static final double LABEL_RADIUS = 60.0D;

    private static final ITextureData WHEEL_BACKGROUND = new TextureData(
            Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "textures/gui/overlay/skill_wheel.png"),
            64,
            64
    );
    private static final ITextureData SELECTED_SEGMENT = new TextureData(
            Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "textures/gui/overlay/skill_wheel_segment_selected.png"),
            64,
            32
    );

    private List<Component> names = List.of();
    private int hovered = -1;

    public QiCoatingWheelContainer(UIFrame frame) {
        super(frame);
        setWidth(WIDTH);
        setHeight(HEIGHT);

        getPositioning().setPositioningRule(PositioningRules.CENTER);
        getPositioning().setX(-WIDTH / 2);
        getPositioning().setY(-110);
    }

    public void setNames(List<Component> names) {
        this.names = List.copyOf(names);
    }

    public void setHovered(int hovered) {
        this.hovered = hovered;
    }

    public double getSegmentDegrees() {
        return 360.0D / Math.max(1, names.size());
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        renderWheelBackground(graphics);
        if (names.isEmpty()) {
            renderCentered(graphics, Component.translatable("gui.ascension.qi_coating_wheel.empty"), WHEEL_LEFT + CENTER, WHEEL_TOP + CENTER - 4);
            return;
        }
        renderHoveredSegment(graphics);
        for (int index = 0; index < names.size(); index++) {
            double angle = Math.toRadians(-90.0D + index * getSegmentDegrees());
            int x = (int) Math.round(WHEEL_LEFT + CENTER + Math.cos(angle) * LABEL_RADIUS);
            int y = (int) Math.round(WHEEL_TOP + CENTER + Math.sin(angle) * LABEL_RADIUS) - 4;
            renderCentered(graphics, names.get(index), x, y);
        }
        if (hovered >= 0 && hovered < names.size()) {
            renderCentered(graphics, names.get(hovered), WIDTH / 2, 218);
        }
    }

    private void renderWheelBackground(GuiGraphicsExtractor graphics) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(WHEEL_LEFT, WHEEL_TOP);
        graphics.pose().scale(WHEEL_SCALE, WHEEL_SCALE);
        for (int pass = 0; pass < BACKGROUND_PASSES; pass++) {
            WHEEL_BACKGROUND.render(graphics);
        }
        graphics.pose().popMatrix();
    }

    private void renderHoveredSegment(GuiGraphicsExtractor graphics) {
        if (hovered < 0 || hovered >= names.size()) {
            return;
        }
        graphics.pose().pushMatrix();
        graphics.pose().translate(WHEEL_LEFT + CENTER, WHEEL_TOP + CENTER);
        graphics.pose().rotate((float) Math.toRadians(hovered * getSegmentDegrees()));
        graphics.pose().scale(SEGMENT_SCALE, SEGMENT_SCALE);
        SELECTED_SEGMENT.renderAt(graphics, -32, -32);
        graphics.pose().popMatrix();
    }

    private static void renderCentered(GuiGraphicsExtractor graphics, Component text, int centerX, int y) {
        Minecraft minecraft = Minecraft.getInstance();
        graphics.text(minecraft.font, text, centerX - minecraft.font.width(text) / 2, y, 0xFFFFFFFF, true);
    }
}
