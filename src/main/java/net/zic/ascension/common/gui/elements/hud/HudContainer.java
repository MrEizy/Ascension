package net.zic.ascension.common.gui.elements.hud;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.textures.ITextureData;
import net.lucent.easygui.gui.textures.TextureDataSubsection;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.zic.ascension.AscensionCraft;

public class HudContainer extends RenderableElement {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            AscensionCraft.MOD_ID,
            "textures/gui/main/overlays/main_hud.png"
    );

    private static final ITextureData HUD_TEXTURE = new TextureDataSubsection(
            TEXTURE,
            150, 44,
            0, 0,
            111, 33
    );

    public HudContainer(UIFrame frame) {
        super(frame);
        setWidth(HUD_TEXTURE.getWidth());
        setHeight(HUD_TEXTURE.getHeight());

        HealthBar healthBar = new HealthBar(frame);
        healthBar.getPositioning().setX(5);
        healthBar.getPositioning().setY(5);
        addChild(healthBar);

        QiBar qiBar = new QiBar(frame);
        qiBar.getPositioning().setX(5);
        qiBar.getPositioning().setY(19);
        addChild(qiBar);
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        HUD_TEXTURE.render(graphics);
    }
}
