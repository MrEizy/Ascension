package net.thejadeproject.ascension.refactor_packages.gui;

import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.layout.positioning.rules.PositioningRules;
import net.lucent.easygui.gui.overaly.EasyOverlayHandler;
import net.lucent.easygui.gui.overaly.EasyOverlayPosition;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.refactor_packages.gui.elements.general.Container;
import net.thejadeproject.ascension.refactor_packages.gui.elements.hud.HudContainer;
import net.thejadeproject.ascension.refactor_packages.gui.elements.skill_casting.SkillHotBarContainer;

public class ModOverlays {

    public static void register(){
        if(Minecraft.getInstance() == null) return;
        UIFrame frame = new UIFrame();
        frame.setRoot(new SkillHotBarContainer(frame));
        EasyOverlayHandler.registerOverlay(ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID,"skill_wheel"), EasyOverlayPosition.ABOVE_ALL(),frame);

        UIFrame frame2 = new UIFrame();
        Container container = new Container(frame2,0,0);
        container.getPositioning().setPositioningRule(PositioningRules.CENTER);
        frame2.setRoot(container);
        EasyOverlayHandler.registerOverlay(ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID,"skill_casting"),EasyOverlayPosition.ABOVE_ALL(),frame2);

        UIFrame hudFrame=new UIFrame();
        HudContainer hudContainer = new HudContainer(frame);
        hudFrame.setRoot(hudContainer);
        EasyOverlayHandler.overrideVanillaOverlay(VanillaGuiLayers.PLAYER_HEALTH,hudFrame);

    }

}
