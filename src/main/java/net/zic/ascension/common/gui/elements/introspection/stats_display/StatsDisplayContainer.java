package net.zic.ascension.common.gui.elements.introspection.stats_display;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.textures.ITextureData;
import net.lucent.easygui.gui.textures.TextureDataSubsection;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
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
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        BACKGROUND.render(graphics);
    }
}
