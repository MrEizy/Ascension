package net.zic.ascension.common.gui.elements.introspection.skill_display;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.textures.ITextureData;
import net.lucent.easygui.gui.textures.TextureDataSubsection;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.gui.data.ClientAscensionData;

public class SkillBarContainer extends RenderableElement {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            AscensionCraft.MOD_ID,
            "textures/gui/main/skill_menu/skill_menu.png"
    );

    private static final ITextureData BACKGROUND = new TextureDataSubsection(
            TEXTURE, 234, 286, 3, 198, 95, 60
    );

    public SkillBarContainer(UIFrame frame, SkillDisplayContainer owner) {
        super(frame);
        setWidth(BACKGROUND.getWidth());
        setHeight(BACKGROUND.getHeight());

        int maxSlots = ClientAscensionData.getSkillCastHandler()
                .map(handler -> handler.getMaxSlots())
                .orElse(6);
        for (int slot = 0; slot < maxSlots; slot++) {
            SkillSlotButton button = new SkillSlotButton(frame, owner, slot);
            button.getPositioning().setX(2);
            button.getPositioning().setY(slot * 9);
            addChild(button);
        }
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        BACKGROUND.render(graphics);
    }
}
