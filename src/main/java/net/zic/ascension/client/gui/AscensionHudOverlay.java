package net.zic.ascension.client.gui;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.overaly.EasyOverlayLayer;
import net.lucent.easygui.gui.overaly.EasyOverlayType;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.gui.elements.hud.CastProgressBar;
import net.zic.ascension.common.gui.elements.hud.CultivationProgressBar;
import net.zic.ascension.common.gui.elements.hud.HudContainer;
import net.zic.ascension.common.gui.elements.hud.HudVisibility;

import java.util.function.Function;

/**
 * the ascension HUD, each part is its own {@link EasyOverlayLayer} so its frame is resized with the window
 */
public final class AscensionHudOverlay {
    private static final Identifier ASCENSION_HUD = AscensionCraft.prefix("ascension_hud");
    private static final Identifier CULTIVATION_BAR = AscensionCraft.prefix("cultivation_bar");
    private static final Identifier CAST_BAR = AscensionCraft.prefix("cast_bar");
    private static final Identifier SKILL_WHEEL = AscensionCraft.prefix("skill_wheel");

    private AscensionHudOverlay() {
    }

    public static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.PLAYER_HEALTH, ASCENSION_HUD, layer(HudContainer::new));
        event.registerAbove(ASCENSION_HUD, CULTIVATION_BAR, layer(CultivationProgressBar::new));
        event.registerAbove(CULTIVATION_BAR, CAST_BAR, layer(CastProgressBar::new));
        event.registerAbove(CAST_BAR, SKILL_WHEEL, AscensionHudOverlay::renderSkillWheel);
    }

    @SubscribeEvent
    public static void hideVanillaHealth(RenderGuiLayerEvent.Pre event) {
        if (VanillaGuiLayers.PLAYER_HEALTH.equals(event.getName())) {
            event.setCanceled(true);
        }
    }

    //visibility is decided by each root element through isVisible
    private static EasyOverlayLayer layer(Function<UIFrame, RenderableElement> root) {
        UIFrame frame = new UIFrame();
        frame.setPauseGame(false);
        frame.setRoot(root.apply(frame));
        return new EasyOverlayLayer(frame, EasyOverlayType.GAME);
    }

    private static void renderSkillWheel(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        if (!HudVisibility.isHudVisible()) return;
        SkillWheelOverlay.render(graphics, deltaTracker.getGameTimeDeltaPartialTick(false));
        QiCoatingWheelOverlay.render(graphics, deltaTracker.getGameTimeDeltaPartialTick(false));
    }
}
