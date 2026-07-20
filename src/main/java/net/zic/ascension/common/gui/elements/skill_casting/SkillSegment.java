package net.zic.ascension.common.gui.elements.skill_casting;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.textures.ITextureData;
import net.lucent.easygui.gui.textures.TextureData;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.gui.data.ClientAscensionData;

public class SkillSegment extends RenderableElement {
    private static final Identifier SELECTED_SEGMENT_TEXTURE = Identifier.fromNamespaceAndPath(
            AscensionCraft.MOD_ID,
            "textures/gui/overlay/skill_wheel_segment_selected.png"
    );

    private static final ITextureData SELECTED_SEGMENT = new TextureData(
            SELECTED_SEGMENT_TEXTURE,
            64,
            32
    );

    private static final float SEGMENT_SCALE = 3.0F;
    private static final float ICON_SCALE = 1.5F;
    private static final int SIZE = 192;
    private static final int CENTER = SIZE / 2;
    private static final double ICON_RADIUS = 60.0D;

    private final SkillHotBarContainer owner;
    private final int slot;

    public SkillSegment(UIFrame frame, SkillHotBarContainer owner, int slot) {
        super(frame);
        this.owner = owner;
        this.slot = slot;
        setWidth(SIZE);
        setHeight(SIZE);
    }

    public int getSlot() {
        return slot;
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        renderSelectedSegment(graphics);
        renderSkillIcon(graphics);
    }

    private void renderSelectedSegment(GuiGraphicsExtractor graphics) {
        if (owner.getSelectedSlot() != slot) {
            return;
        }

        float rotation = (float) Math.toRadians(slot * owner.getSegmentDegrees());

        graphics.pose().pushMatrix();
        graphics.pose().translate(CENTER, CENTER);
        graphics.pose().rotate(rotation);
        graphics.pose().scale(SEGMENT_SCALE, SEGMENT_SCALE);
        SELECTED_SEGMENT.renderAt(graphics, -32, -32);
        graphics.pose().popMatrix();
    }

    private void renderSkillIcon(GuiGraphicsExtractor graphics) {
        ClientAscensionData.getSkillCastHandler().ifPresent(handler -> {
            Identifier skillId = handler.getSkill(slot);
            if (skillId == null) {
                return;
            }

            double angle = Math.toRadians(-90.0D + slot * owner.getSegmentDegrees());
            float x = (float) (CENTER + Math.cos(angle) * ICON_RADIUS);
            float y = (float) (CENTER + Math.sin(angle) * ICON_RADIUS);
            ITextureData icon = new TextureData(SkillIconResolver.resolve(skillId), 16, 16);

            graphics.pose().pushMatrix();
            graphics.pose().translate(x, y);
            graphics.pose().scale(ICON_SCALE, ICON_SCALE);
            icon.renderAt(graphics, -8, -8);
            graphics.pose().popMatrix();
        });
    }
}
