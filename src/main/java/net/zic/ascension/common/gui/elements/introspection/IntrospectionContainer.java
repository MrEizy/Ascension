package net.zic.ascension.common.gui.elements.introspection;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.layout.positioning.rules.PositioningRules;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.gui.elements.introspection.main.MainContainer;
import net.zic.ascension.common.gui.elements.introspection.path_display.PathDisplayContainer;
import net.zic.ascension.common.gui.elements.introspection.skill_display.SkillDisplayContainer;
import net.zic.ascension.common.gui.elements.introspection.stats_display.StatsDisplayContainer;

import java.util.EnumMap;
import java.util.Map;

public class IntrospectionContainer extends RenderableElement {
    public enum Panel {
        MAIN,
        STATS,
        SKILLS,
        CULTIVATION
    }

    private final Map<Panel, RenderableElement> containers = new EnumMap<>(Panel.class);
    private final Map<Panel, NavButton> buttons = new EnumMap<>(Panel.class);
    private final ActiveMenuTitle menuTitle;

    public IntrospectionContainer(UIFrame frame) {
        super(frame);
        getPositioning().setPositioningRule(PositioningRules.CENTER);

        MainContainer mainContainer = new MainContainer(frame);
        addChild(mainContainer);
        containers.put(Panel.MAIN, mainContainer);

        SkillDisplayContainer skillContainer = new SkillDisplayContainer(frame, this);
        skillContainer.setActive(false);
        addChild(skillContainer);
        containers.put(Panel.SKILLS, skillContainer);

        PathDisplayContainer pathContainer = new PathDisplayContainer(frame, this);
        pathContainer.setActive(false);
        addChild(pathContainer);
        containers.put(Panel.CULTIVATION, pathContainer);

        StatsDisplayContainer statsContainer = new StatsDisplayContainer(frame, this);
        statsContainer.setActive(false);
        addChild(statsContainer);
        containers.put(Panel.STATS, statsContainer);

        menuTitle = new ActiveMenuTitle(frame);
        menuTitle.getPositioning().setFromRawX(mainContainer.getPositioning().getRawX() + 88);
        menuTitle.getPositioning().setFromRawY(mainContainer.getPositioning().getRawY() - 31);
        addChild(menuTitle);

        NavButton statsButton = createNavigationButton(
                frame,
                Panel.STATS,
                "textures/gui/main/stats_menu/stats_buttons.png",
                -mainContainer.getWidth() / 2 + 4,
                -mainContainer.getHeight() / 2 - 27
        );
        buttons.put(Panel.STATS, statsButton);

        NavButton skillsButton = createNavigationButton(
                frame,
                Panel.SKILLS,
                "textures/gui/main/skill_menu/skill_tab_buttons.png",
                -mainContainer.getWidth() / 2 + 30,
                -mainContainer.getHeight() / 2 - 27
        );
        buttons.put(Panel.SKILLS, skillsButton);

        NavButton cultivationButton = createNavigationButton(
                frame,
                Panel.CULTIVATION,
                "textures/gui/main/path_menu/path_buttons.png",
                -mainContainer.getWidth() / 2 + 56,
                -mainContainer.getHeight() / 2 - 27
        );
        buttons.put(Panel.CULTIVATION, cultivationButton);

        openPanel(Panel.MAIN);
    }

    private NavButton createNavigationButton(
            UIFrame frame,
            Panel panel,
            String texturePath,
            int x,
            int y
    ) {
        NavButton button = new NavButton(
                frame,
                this,
                panel,
                Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, texturePath)
        );
        button.getPositioning().setX(x);
        button.getPositioning().setY(y);
        addChild(button);
        return button;
    }

    public void openPanel(Panel panel) {
        for (Map.Entry<Panel, RenderableElement> entry : containers.entrySet()) {
            entry.getValue().setActive(entry.getKey() == panel);
        }
        for (Map.Entry<Panel, NavButton> entry : buttons.entrySet()) {
            entry.getValue().setSelected(entry.getKey() == panel);
        }
        menuTitle.setMenuName(getPanelName(panel));
    }

    private static Component getPanelName(Panel panel) {
        return switch (panel) {
            case MAIN -> Component.translatable("gui.ascension.introspection.main");
            case STATS -> Component.translatable("gui.ascension.introspection.stats");
            case SKILLS -> Component.translatable("gui.ascension.introspection.skills");
            case CULTIVATION -> Component.translatable("gui.ascension.introspection.cultivation");
        };
    }
}
