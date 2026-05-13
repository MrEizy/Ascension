package net.thejadeproject.ascension.menus.custom.pill_cauldron;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.common.blocks.entity.FlameStandBlockEntity;

@OnlyIn(Dist.CLIENT)
public class PillCauldronLowHumanScreen extends AbstractContainerScreen<PillCauldronLowHumanMenu> {

    private static final ResourceLocation GUI_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID,
                    "textures/gui/pill_cauldron_low_human/pill_cauldron_low_human_menu.png");
    private static final ResourceLocation ARROW_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID,
                    "textures/gui/arrow_progress.png");

    // Client-side smooth temperature for the in-GUI bar
    private float smoothTemp = 0f;

    public PillCauldronLowHumanScreen(PillCauldronLowHumanMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        g.blit(GUI_TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
        renderProgressArrow(g, x, y);
        renderTempBar(g, x, y, partialTick);
    }

    private void renderProgressArrow(GuiGraphics g, int x, int y) {
        if (menu.isCrafting()) {
            g.blit(ARROW_TEXTURE, x + 80, y + 33, 0, 0, 16, menu.getScaledArrowProgress(), 16, 21);
        }
    }

    private void renderTempBar(GuiGraphics g, int x, int y, float partialTick) {
        boolean lit   = menu.isFlameStandLit();
        int rawTemp   = menu.getCurrentTemp();
        int minTemp   = menu.getRecipeMinTemp();
        int maxTemp   = menu.getRecipeMaxTemp();

        // Smooth toward raw temp
        float target  = lit ? (rawTemp / (float) FlameStandBlockEntity.MAX_TEMP) : 0f;
        smoothTemp   += (target - smoothTemp) * 0.12f;

        // Bar geometry (right side of GUI)
        int barX = x + 148;
        int barY = y + 70;
        int barH = 50;

        // Background
        g.fill(barX, barY - barH, barX + 5, barY, 0xFF333333);

        // Green zone (recipe range)
        if (minTemp > 0 && maxTemp > minTemp) {
            int minPx = (int)(barH * (minTemp  / (float) FlameStandBlockEntity.MAX_TEMP));
            int maxPx = (int)(barH * (maxTemp  / (float) FlameStandBlockEntity.MAX_TEMP));
            g.fill(barX, barY - maxPx, barX + 5, barY - minPx, 0x5500CC00);
        }

        // Fill
        int filled = (int)(barH * smoothTemp);
        int color  = getTempBarColor(rawTemp, minTemp, maxTemp);
        g.fill(barX, barY - filled, barX + 5, barY, color);

        // Flame icon / lit indicator dot
        int dotColor = lit ? 0xFFFF6600 : 0xFF555555;
        g.fill(x + 8, y + 30, x + 16, y + 38, dotColor);
    }

    private int getTempBarColor(int temp, int minTemp, int maxTemp) {
        if (maxTemp > 0 && temp >= minTemp && temp <= maxTemp) return 0xFF00CC00;
        if (maxTemp > 0 && temp < minTemp)  return 0xFF4488FF;
        return 0xFFFF4400;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        this.renderTooltip(g, mouseX, mouseY);

        // Tooltip – flame dot
        if (isHovering(8, 30, 8, 8, mouseX, mouseY)) {
            g.renderTooltip(font,
                    Component.literal(menu.isFlameStandLit()
                            ? "Flame Stand: Lit (" + menu.getCurrentTemp() + "°)"
                            : "Flame Stand: Unlit"),
                    mouseX, mouseY);
        }

        // Tooltip – temp bar
        if (isHovering(148, 70 - 50, 5, 50, mouseX, mouseY)) {
            int min = menu.getRecipeMinTemp(), max = menu.getRecipeMaxTemp();
            String rangeText = (min > 0 && max > 0)
                    ? "Range: " + min + "° — " + max + "°"
                    : "No active recipe";
            g.renderTooltip(font,
                    Component.literal("Temp: " + menu.getCurrentTemp() + "°\n" + rangeText),
                    mouseX, mouseY);
        }
    }
}