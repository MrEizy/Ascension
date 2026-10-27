package net.zic.ascension.common.gui.elements.hud;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

//shared visibility rule for HUD elements, used from their isVisible
public final class HudVisibility {
    private HudVisibility() {
    }

    //the HUD is hidden without a player or level, for spectators and while the GUI is hidden (F1)
    public static boolean isHudVisible() {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        return player != null && minecraft.level != null && !player.isSpectator() && !minecraft.options.hideGui;
    }
}
