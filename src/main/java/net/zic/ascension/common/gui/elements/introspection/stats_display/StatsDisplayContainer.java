package net.zic.ascension.common.gui.elements.introspection.stats_display;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.textures.ITextureData;
import net.lucent.easygui.gui.textures.TextureDataSubsection;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.gui.elements.introspection.BackButton;
import net.zic.ascension.common.gui.elements.introspection.IntrospectionContainer;

public class StatsDisplayContainer extends RenderableElement {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            AscensionCraft.MOD_ID,
            "textures/gui/main/stats_menu/stats_menu.png"
    );

    private static final ITextureData BACKGROUND = new TextureDataSubsection(
            TEXTURE, 234, 236, 0, 39, 234, 140
    );

    public StatsDisplayContainer(UIFrame frame, IntrospectionContainer owner) {
        super(frame);
        setWidth(BACKGROUND.getWidth());
        setHeight(BACKGROUND.getHeight());
        getPositioning().setX(-getWidth() / 2);
        getPositioning().setY(-getHeight() / 2);

        BackButton backButton = new BackButton(frame, owner);
        backButton.getPositioning().setX(5);
        backButton.getPositioning().setY(5);
        addChild(backButton);

        addChild(new StatHolder(frame));
        addLeftAttributes(frame);
        addRightAttributes(frame);
    }

    private void addLeftAttributes(UIFrame frame) {
        int x = 13;
        int y = 18;

        addAttribute(frame, Attributes.MAX_HEALTH, 20, 227, 7, 7, x, y);
        addAttribute(frame, Attributes.ATTACK_DAMAGE, 28, 227, 9, 9, x, y + 17);
        addAttribute(frame, Attributes.ARMOR, 10, 227, 9, 9, x, y + 44);
        addAttribute(frame, Attributes.ARMOR_TOUGHNESS, 0, 227, 9, 9, x, y + 61);
        addAttribute(frame, Attributes.ATTACK_SPEED, 28, 227, 9, 9, x, y + 78);
    }

    private void addRightAttributes(UIFrame frame) {
        int x = 165;
        int y = 18;

        addAttribute(frame, Attributes.MOVEMENT_SPEED, 185, 226, 9, 9, x, y);
        addAttribute(frame, Attributes.JUMP_STRENGTH, 195, 226, 9, 9, x, y + 27);
        addAttribute(frame, Attributes.STEP_HEIGHT, 205, 226, 9, 9, x, y + 54);
        addAttribute(frame, Attributes.MINING_EFFICIENCY, 205, 226, 9, 9, x, y + 81);
    }

    private void addAttribute(
            UIFrame frame,
            Holder<Attribute> attribute,
            int textureX,
            int textureY,
            int textureWidth,
            int textureHeight,
            int x,
            int y
    ) {
        AttributeDisplayContainer display = new AttributeDisplayContainer(
                frame,
                attribute,
                new TextureDataSubsection(
                        TEXTURE,
                        234,
                        236,
                        textureX,
                        textureY,
                        textureWidth,
                        textureHeight
                )
        );
        display.getPositioning().setX(x);
        display.getPositioning().setY(y);
        addChild(display);
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        BACKGROUND.render(graphics);
    }
}
