package net.zic.ascension.client.gui;

import net.lucent.easygui.gui.UIFrame;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.gui.elements.hud.CultivationProgressBar;
import net.zic.ascension.common.gui.elements.hud.HudContainer;

public final class AscensionHudOverlay {
    private static final Identifier ASCENSION_HUD = Identifier.fromNamespaceAndPath(
            AscensionCraft.MOD_ID,
            "ascension_hud"
    );

    private static UIFrame hudFrame;
    private static UIFrame cultivationFrame;

    private AscensionHudOverlay() {
    }

    public static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(
                VanillaGuiLayers.PLAYER_HEALTH,
                ASCENSION_HUD,
                AscensionHudOverlay::render
        );
    }

    @SubscribeEvent
    public static void hideVanillaHealth(RenderGuiLayerEvent.Pre event) {
        if (VanillaGuiLayers.PLAYER_HEALTH.equals(event.getName())) {
            event.setCanceled(true);
        }
    }

    private static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.level == null || player.isSpectator() || minecraft.options.hideGui) {
            return;
        }

        float partialTick = deltaTracker.getGameTimeDeltaPartialTick(false);

        if (hudFrame == null) {
            hudFrame = new UIFrame();
            hudFrame.setPauseGame(false);
            hudFrame.setRoot(new HudContainer(hudFrame));
        }
        if (cultivationFrame == null) {
            cultivationFrame = new UIFrame();
            cultivationFrame.setPauseGame(false);
            cultivationFrame.setRoot(new CultivationProgressBar(cultivationFrame));
        }

        hudFrame.run(graphics, 0, 0, partialTick);
        cultivationFrame.run(graphics, 0, 0, partialTick);
        SkillWheelOverlay.render(graphics, partialTick);
    }
}
