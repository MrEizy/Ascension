package net.zic.ascension.refactor_packages.gui.elements.skill_casting;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.textures.ITextureData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.zic.ascension.data_attachments.ModAttachments;
import net.zic.ascension.refactor_packages.registries.AscensionRegistries;
import org.joml.Matrix4f;

public class SkillSegment extends RenderableElement {
    public final int slot;

    public final double startRadius;
    public final double endRadius;

    private final int BASE_COLOUR =    -931957901;
    private final int HOVER_COLOUR = -926299703;
    public static final float OUTER_RADIUS = 2;
    public static final float INNER_RADIUS = 100;
    private final int SEGMENTS = 24;

    public boolean hovered;

    public ITextureData skillIcon;


    public SkillSegment(UIFrame frame, int slot, double startRadius, double endRadius){
        super(frame);
        this.slot = slot;

        this.startRadius = startRadius;
        this.endRadius = endRadius;
        ResourceLocation skill = Minecraft.getInstance().player.getData(ModAttachments.ENTITY_DATA).getSkillCastHandler().getHotBar().getSkillKey(slot);
        if(skill != null){
            skillIcon = AscensionRegistries.Skills.SKILL_REGISTRY.get(skill).getIcon();
        }
    }



    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        double radiusTraveled = 0;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        Matrix4f matrix = guiGraphics.pose().last().pose();

        double diffAngle = (endRadius - startRadius + 360) % 360;
        float startRad = (float) Math.toRadians(startRadius);
        float diffRad = (float) Math.toRadians(diffAngle);
        builder.addVertex(matrix, 0, 0, 0).setColor(isFocused() ? HOVER_COLOUR : BASE_COLOUR);




        // Inner arc - same solid color
        for (int i = SEGMENTS; i >= 0; i--) {
            float t = i / (float) SEGMENTS;
            float angle = (startRad + diffRad * t);
            float x =  (float) Math.cos(angle) * INNER_RADIUS;
            float y =  (float) Math.sin(angle) * INNER_RADIUS;
            builder.addVertex(matrix, x, y, 0).setColor(isFocused() ? HOVER_COLOUR : BASE_COLOUR);
        }


        BufferUploader.drawWithShader(builder.buildOrThrow());
        //render skill icon
        if(skillIcon == null) return;


        float angle = (startRad + diffRad * 0.5f);
        double x =   Math.cos(angle) * INNER_RADIUS/2-  (double) skillIcon.getWidth() /2;
        double y =   Math.sin(angle) * INNER_RADIUS/2-  (double) skillIcon.getHeight() /2;
        skillIcon.renderAt(guiGraphics, (int) x, (int) y);


    }

    @Override
    public boolean isFocusable() {
        return true;
    }
}
