package net.thejadeproject.ascension.refactor_packages.gui.elements.hud;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.layout.positioning.rules.PositioningRules;
import net.lucent.easygui.gui.textures.ITextureData;
import net.lucent.easygui.gui.textures.TextureDataSubsection;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.thejadeproject.ascension.AscensionCraft;

public class HudContainer extends RenderableElement {
    ResourceLocation textureIdentifier = ResourceLocation.fromNamespaceAndPath(
            AscensionCraft.MOD_ID,
            "textures/gui/main/overlays/main_hud.png"
    );
    ITextureData textureData = new TextureDataSubsection(
            textureIdentifier,
            150,44,
            0,0,
            111,33
    );
    public HudContainer(UIFrame frame) {
        super(frame);

        HealthBar healthBar = new HealthBar(frame);
        healthBar.getPositioning().setX(5);
        healthBar.getPositioning().setY(5);
        QiBar qiBar = new QiBar(frame);
        qiBar.getPositioning().setX(5);
        qiBar.getPositioning().setY(19);
        addChild(healthBar);
        addChild(qiBar);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        textureData.render(guiGraphics);
    }
}
