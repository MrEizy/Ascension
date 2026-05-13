package net.thejadeproject.ascension.refactor_packages.gui.elements.introspection;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.layout.positioning.rules.PositioningRules;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.refactor_packages.gui.elements.info_elements.IInformationContainer;
import net.thejadeproject.ascension.refactor_packages.gui.elements.introspection.main.MainContainer;
import net.thejadeproject.ascension.refactor_packages.gui.elements.introspection.path_display.PathDisplayContainer;
import net.thejadeproject.ascension.refactor_packages.gui.elements.introspection.skill_display.SkillDisplayContainer;
import net.thejadeproject.ascension.refactor_packages.gui.elements.introspection.stats_display.StatsDisplayContainer;

import java.util.HashMap;

public class IntrospectionContainer extends RenderableElement {


    private final HashMap<String,RenderableElement> containerMap = new HashMap<>();
    private final HashMap<String,NavButton> buttonMap = new HashMap<>();
    private final HashMap<String, Component> nameMap = new HashMap<>();
    private ActiveMenuTitle menuTitle;
    public IntrospectionContainer(UIFrame frame) {
        super(frame);

        nameMap.put("main",Component.literal("Main"));
        nameMap.put("stats",Component.literal("Stats"));
        nameMap.put("skill",Component.literal("Skills"));
        nameMap.put("path",Component.literal("Cultivation"));

        //System.out.println("creating c1");
        getPositioning().setPositioningRule(PositioningRules.CENTER);

        MainContainer mainContainer = new MainContainer(frame);
        mainContainer.setActive(true);
        addChild(mainContainer);
        containerMap.put("main",mainContainer);

        menuTitle = new ActiveMenuTitle(frame);
        menuTitle.getPositioning().setFromRawX(mainContainer.getPositioning().getRawX()+88);
        menuTitle.getPositioning().setFromRawY(mainContainer.getPositioning().getRawY()-31);
        addChild(menuTitle);
        menuTitle.setMenuName(nameMap.get("main"));
        SkillDisplayContainer skillDisplayContainer = new SkillDisplayContainer(frame);
        skillDisplayContainer.setActive(false);
        addChild(skillDisplayContainer);
        containerMap.put("skill",skillDisplayContainer);

        PathDisplayContainer pathDisplayContainer = new PathDisplayContainer(frame);
        pathDisplayContainer.setActive(false);
        addChild(pathDisplayContainer);
        containerMap.put("path",pathDisplayContainer);

        StatsDisplayContainer statsDisplayContainer = new StatsDisplayContainer(frame);
        statsDisplayContainer.setActive(false);
        addChild(statsDisplayContainer);
        containerMap.put("stats",statsDisplayContainer);


        NavButton statBtn = new NavButton(frame,"stats",ResourceLocation.fromNamespaceAndPath(
                AscensionCraft.MOD_ID,
                "textures/gui/main/stats_menu/stats_buttons.png"
        ));
        statBtn.getPositioning().setX(-mainContainer.getWidth()/2+4);
        statBtn.getPositioning().setY(-mainContainer.getHeight()/2-27);
        addChild(statBtn);
        buttonMap.put("stats",statBtn);

        NavButton skillBtn = new NavButton(frame,"skill",ResourceLocation.fromNamespaceAndPath(
                AscensionCraft.MOD_ID,
                "textures/gui/main/skill_menu/skill_tab_buttons.png"
        ));
        skillBtn.getPositioning().setX(-mainContainer.getWidth()/2+30);
        skillBtn.getPositioning().setY(-mainContainer.getHeight()/2-27);
        addChild(skillBtn);
        buttonMap.put("skill",skillBtn);

        NavButton pathBtn = new NavButton(frame,"path",ResourceLocation.fromNamespaceAndPath(
                AscensionCraft.MOD_ID,
                "textures/gui/main/path_menu/path_buttons.png"
        ));
        pathBtn.getPositioning().setX(-mainContainer.getWidth()/2+56);
        pathBtn.getPositioning().setY(-mainContainer.getHeight()/2-27);
        addChild(pathBtn);
        buttonMap.put("path",pathBtn);

    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

    }

    public void openScreen(String screen){
        for(RenderableElement element : containerMap.values())element.setActive(false);
        for(NavButton element : buttonMap.values()) element.setActiveBtn(false);

        if(containerMap.containsKey(screen)) containerMap.get(screen).setActive(true);
        if(buttonMap.containsKey(screen)) buttonMap.get(screen).setActiveBtn(true);
        if(nameMap.containsKey(screen)) menuTitle.setMenuName(nameMap.get(screen));
    }
}
