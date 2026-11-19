package net.zic.ascension.client.combat;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.zic.ascension.AscensionCraft;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID, value = Dist.CLIENT)
public final class DualWieldCooldownOverlay {
    private DualWieldCooldownOverlay() {
    }

    @SubscribeEvent
    public static void registerLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.CROSSHAIR, AscensionCraft.prefix("dual_wield_cooldown"), DualWieldCooldownOverlay::render);
    }

    private static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!DualWieldClientState.active() || minecraft.options.hideGui || minecraft.gameMode == null || !minecraft.gameMode.canHurtPlayer()) {
            return;
        }
        int total = DualWieldClientState.offhandCooldownTotal();
        int remaining = DualWieldClientState.offhandCooldown();
        if (total <= 0 || remaining <= 0) {
            return;
        }

        float filled = 1.0F - remaining / (float) total;
        int width = 12;
        int x = graphics.guiWidth() / 2 - width - 3; // offhand side
        int y = graphics.guiHeight() / 2 + 12;

        graphics.fill(x, y, x + width, y + 2, 0x80000000);
        graphics.fill(x, y, x + Math.round(width * filled), y + 2, 0xFFFFFFFF);
    }
}