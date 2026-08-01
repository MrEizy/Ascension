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
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.skill.Skill;
import net.zic.ascension.common.gui.data.ClientAscensionData;

public class SkillHotBarContainer extends RenderableElement {
    private static final int WIDTH = 260;
    private static final int HEIGHT = 240;
    private static final int WHEEL_LEFT = 34;
    private static final int WHEEL_TOP = 14;
    private static final float WHEEL_SCALE = 3.0F;
    private static final int BACKGROUND_PASSES = 4;

    private static final Identifier WHEEL_TEXTURE = Identifier.fromNamespaceAndPath(
            AscensionCraft.MOD_ID,
            "textures/gui/overlay/skill_wheel.png"
    );
    private static final ITextureData WHEEL_BACKGROUND = new TextureData(
            WHEEL_TEXTURE,
            64,
            64
    );

    private int selectedSlot;
    private int maxSlots = 6;

    public SkillHotBarContainer(UIFrame frame) {
        super(frame);
        setWidth(WIDTH);
        setHeight(HEIGHT);

        getPositioning().setPositioningRule(PositioningRules.CENTER);
        getPositioning().setX(-WIDTH / 2);
        getPositioning().setY(-110);

        ClientAscensionData.getSkillCastHandler().ifPresent(handler -> {
            maxSlots = Math.max(1, handler.getMaxSlots());
            selectedSlot = handler.getSelectedSlot();
        });

        rebuildSegments();
    }

    public int getSelectedSlot() {
        return selectedSlot;
    }

    public void setSelectedSlot(int selectedSlot) {
        if (selectedSlot >= 0 && selectedSlot < maxSlots) {
            this.selectedSlot = selectedSlot;
        }
    }

    public double getSegmentDegrees() {
        return 360.0D / maxSlots;
    }

    public void refreshFromHandler() {
        ClientAscensionData.getSkillCastHandler().ifPresent(handler -> {
            int handlerSlots = Math.max(1, handler.getMaxSlots());
            if (handlerSlots != maxSlots) {
                maxSlots = handlerSlots;
                selectedSlot = Math.clamp(selectedSlot, 0, maxSlots - 1);
                rebuildSegments();
            }
        });
    }

    public void syncSelectionFromHandler() {
        ClientAscensionData.getSkillCastHandler().ifPresent(handler ->
                selectedSlot = Math.clamp(handler.getSelectedSlot(), 0, maxSlots - 1)
        );
    }

    private void rebuildSegments() {
        removeChildren();
        for (int slot = 0; slot < maxSlots; slot++) {
            SkillSegment segment = new SkillSegment(getUiFrame(), this, slot);
            segment.getPositioning().setX(WHEEL_LEFT);
            segment.getPositioning().setY(WHEEL_TOP);
            addChild(segment);
        }
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        renderWheelBackground(graphics);
        renderSelectedSkillName(graphics);
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

    private void renderSelectedSkillName(GuiGraphicsExtractor graphics) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }

        ClientAscensionData.getSkillCastHandler().ifPresent(handler -> {
            Identifier skillId = handler.getSkill(selectedSlot);
            if (skillId == null) {
                return;
            }

            Skill skill = CoreRegistries.safeAccess(
                    CoreRegistries.SKILL_REGISTRY,
                    skillId,
                    minecraft.player.registryAccess()
            );
            Component name = skill == null ? Component.literal(skillId.toString()) : skill.getName();
            int x = (WIDTH - minecraft.font.width(name)) / 2;
            graphics.text(minecraft.font, name, x, 218, 0xFFFFFFFF, true);
        });
    }
}
