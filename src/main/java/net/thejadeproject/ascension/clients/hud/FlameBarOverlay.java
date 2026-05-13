package net.thejadeproject.ascension.clients.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.thejadeproject.ascension.common.blocks.entity.FlameStandBlockEntity;
import net.thejadeproject.ascension.common.blocks.entity.PillCauldronLowHumanEntity;

/**
 * HUD overlay shown when looking at a Flame Stand or Pill Cauldron.
 *
 * Layout (centred below crosshair):
 *   ┌─────────────────────────────────────────────────────┐   semi-transparent
 *   │  Temperature:  547°    [crafting: 42% · 6s left]   │
 *   │  ████████░░░░░░░▲░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░  │  ▲ = ideal temp
 *   │          ↑min        ↑ideal        max↑             │  yellow ticks
 *   │  ✔ In range — hold steady!                         │
 *   └─────────────────────────────────────────────────────┘
 *
 * Register with: NeoForge.EVENT_BUS.register(FlameBarOverlay.class)
 */
@OnlyIn(Dist.CLIENT)
public class FlameBarOverlay {

    private static float smoothTemp  = 0f;
    private static float smoothAlpha = 0f;

    @SubscribeEvent
    public static void onRenderGui(RenderGuiLayerEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        if (mc.options.hideGui) return;

        HitResult hit = mc.hitResult;
        if (!(hit instanceof BlockHitResult blockHit)) return;
        if (blockHit.getType() == HitResult.Type.MISS) return;

        BlockPos lookPos = blockHit.getBlockPos();
        FlameStandBlockEntity flameStand = null;
        PillCauldronLowHumanEntity cauldron = null;

        BlockEntity be = mc.level.getBlockEntity(lookPos);
        if (be instanceof FlameStandBlockEntity fs) {
            flameStand = fs;
        } else if (be instanceof PillCauldronLowHumanEntity ce) {
            cauldron = ce;
            BlockEntity below = mc.level.getBlockEntity(lookPos.below());
            if (below instanceof FlameStandBlockEntity fs) flameStand = fs;
        }

        boolean shouldShow = flameStand != null && flameStand.isLit();
        smoothAlpha = shouldShow
                ? Math.min(1f, smoothAlpha + 0.12f)
                : Math.max(0f, smoothAlpha - 0.08f);
        if (smoothAlpha <= 0.01f) return;

        int screenW = mc.getWindow().getGuiScaledWidth();
        int screenH = mc.getWindow().getGuiScaledHeight();

        int panelW = 160;
        int panelH = 48;
        int panelX = (screenW - panelW) / 2;
        int panelY = screenH / 2 + 18;

        // ── Background — properly translucent ────────────────────
        // 0xAA000000 = 67% opaque black. With smoothAlpha we scale further.
        int bgAlpha = (int)(smoothAlpha * 0xAA);
        GuiGraphics g = event.getGuiGraphics();
        g.fill(panelX - 3, panelY - 3, panelX + panelW + 3, panelY + panelH + 3,
                bgAlpha << 24); // pure black, variable alpha
        // Subtle border (slightly less transparent)
        int borderAlpha = (int)(smoothAlpha * 0xCC);
        g.fill(panelX - 3, panelY - 3, panelX + panelW + 3, panelY - 2,   borderAlpha << 24 | 0x444444);
        g.fill(panelX - 3, panelY + panelH + 2, panelX + panelW + 3, panelY + panelH + 3, borderAlpha << 24 | 0x444444);
        g.fill(panelX - 3, panelY - 3, panelX - 2, panelY + panelH + 3,   borderAlpha << 24 | 0x444444);
        g.fill(panelX + panelW + 2, panelY - 3, panelX + panelW + 3, panelY + panelH + 3, borderAlpha << 24 | 0x444444);

        if (flameStand == null) return;

        int rawTemp = flameStand.getTemperature();
        float dispTemp = flameStand.getDisplayTemp();
        smoothTemp += (dispTemp - smoothTemp) * 0.15f;

        int minTemp = 0, maxTemp = 0, idealTemp = 0;
        int progress = 0, maxProgress = 0;
        if (cauldron != null) {
            minTemp     = cauldron.getRecipeMinTemp();
            maxTemp     = cauldron.getRecipeMaxTemp();
            idealTemp   = cauldron.getRecipeIdealTemp();
            progress    = cauldron.data.get(0);
            maxProgress = cauldron.data.get(1);
        }

        int alpha = (int)(smoothAlpha * 255);

        // ── Row 1: temperature label + craft status ───────────────
        String tempLabel = "Temperature: " + rawTemp + "°";
        g.drawString(mc.font, tempLabel, panelX + 4, panelY + 2,
                argb(alpha, 255, 200, 80), true);

        if (maxProgress > 0) {
            int pct     = (int)(progress * 100f / maxProgress);
            int secLeft = Math.max(0, (maxProgress - progress) / 20);
            String craftLabel = "  Crafting: " + pct + "% · " + secLeft + "s";
            int craftX = panelX + 4 + mc.font.width(tempLabel);
            g.drawString(mc.font, craftLabel, craftX, panelY + 2,
                    argb(alpha, 120, 220, 120), false);
        }

        // ── Row 2: temperature bar ────────────────────────────────
        int barW = panelW - 8;
        int barH = 7;
        int barX = panelX + 4;
        int barY = panelY + 14;

        // Background
        g.fill(barX - 1, barY - 1, barX + barW + 1, barY + barH + 1,
                argb(alpha, 30, 30, 30));
        g.fill(barX, barY, barX + barW, barY + barH,
                argb(alpha, 65, 65, 65));

        // Green zone
        if (minTemp > 0 && maxTemp > minTemp) {
            int gx0 = barX + (int)(barW * (minTemp  / (float) FlameStandBlockEntity.MAX_TEMP));
            int gx1 = barX + (int)(barW * (maxTemp  / (float) FlameStandBlockEntity.MAX_TEMP));
            g.fill(gx0, barY, gx1, barY + barH, argb((int)(alpha * 0.45f), 30, 180, 30));
        }

        // Fill
        int filled = (int)(barW * Math.max(0f, Math.min(1f, smoothTemp)));
        int barColor = getTempColor(rawTemp, minTemp, maxTemp, alpha);
        if (filled > 0) g.fill(barX, barY, barX + filled, barY + barH, barColor);

        // Bright edge flicker
        if (filled > 2) {
            g.fill(barX + filled - 2, barY, barX + filled, barY + barH,
                    argb((int)(smoothAlpha * 220), 255, 255, 180));
        }

        // ── Tick marks: min, ideal, max ───────────────────────────
        if (minTemp > 0 && maxTemp > minTemp) {
            drawTick(g, barX, barW, barY, barH, minTemp,   alpha, 255, 220, 50);
            drawTick(g, barX, barW, barY, barH, maxTemp,   alpha, 255, 220, 50);
            if (idealTemp > 0) {
                // Ideal tick is diamond-shaped / different colour (cyan)
                drawTick(g, barX, barW, barY, barH, idealTemp, alpha, 80, 230, 255);
                // "▲" label under ideal
                int ix = barX + (int)(barW * (idealTemp / (float) FlameStandBlockEntity.MAX_TEMP)) - 2;
                g.drawString(mc.font, "▲", ix, barY + barH + 1,
                        argb(alpha, 80, 230, 255), false);
            }
        }

        // ── Row 3: craft progress bar (when active) ───────────────
        if (maxProgress > 0) {
            int pbY = barY + barH + 12;
            int pFilled = (int)(barW * (progress / (float) maxProgress));
            g.fill(barX - 1, pbY - 1, barX + barW + 1, pbY + 4, argb(alpha, 30, 30, 30));
            g.fill(barX, pbY, barX + barW, pbY + 3, argb(alpha, 55, 55, 55));
            if (pFilled > 0)
                g.fill(barX, pbY, barX + pFilled, pbY + 3, argb(alpha, 70, 200, 70));
        }

        // ── Row 4: status text ────────────────────────────────────
        String status = getStatusText(rawTemp, minTemp, maxTemp,
                flameStand.needsFanning(), flameStand.isCritical());
        int statusY = panelY + panelH - 10;
        // Draw with drop shadow for readability over any background
        g.drawString(mc.font, status, panelX + 5, statusY, 0xFFFFFFFF, true);
    }

    // ── Helpers ───────────────────────────────────────────────────

    private static void drawTick(GuiGraphics g, int barX, int barW, int barY, int barH,
                                 int temp, int alpha, int r, int grn, int b) {
        int tx = barX + (int)(barW * (temp / (float) FlameStandBlockEntity.MAX_TEMP));
        g.fill(tx, barY - 2, tx + 1, barY + barH + 2, argb(alpha, r, grn, b));
    }

    private static int getTempColor(int temp, int minTemp, int maxTemp, int alpha) {
        if (maxTemp > 0 && temp >= minTemp && temp <= maxTemp) return argb(alpha, 60, 210, 60);
        if (maxTemp > 0 && temp < minTemp)  return argb(alpha, 70, 130, 220);
        float f = Math.min(1f, temp / (float) FlameStandBlockEntity.MAX_TEMP);
        return argb(alpha, (int)(200 + 55 * f), (int)(100 * (1f - f)), 20);
    }

    private static String getStatusText(int temp, int minTemp, int maxTemp,
                                        boolean fan, boolean critical) {
        if (critical) return "§c⚠ Critical! Fan immediately!";
        if (fan)      return "§e⚡ Fan the flame!";
        if (maxTemp > 0) {
            if (temp < minTemp) return "§9❄ Too cold — fan to raise temperature";
            if (temp > maxTemp) return "§cToo hot — let it cool down";
            return "§a✔ In range — hold steady!";
        }
        return "§7Flame burning";
    }

    private static int argb(int alpha, int r, int g, int b) {
        return (Math.max(0, Math.min(255, alpha)) << 24) | (r << 16) | (g << 8) | b;
    }
}