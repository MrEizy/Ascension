package net.zic.ascension.common.gui.elements.introspection.cultivation;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.gui.elements.introspection.IntrospectionContainer;
import net.zic.ascension.common.gui.elements.introspection.path_display.PathDisplayContainer;
import net.zic.ascension.common.gui.elements.introspection.technique_display.TechniqueDisplayContainer;

import java.util.EnumMap;
import java.util.Map;

public class CultivationDisplayContainer extends RenderableElement {
    public enum Page {
        PATHS,
        TECHNIQUES
    }

    private final IntrospectionContainer owner;
    private final Map<Page, RenderableElement> pages = new EnumMap<>(Page.class);
    private final Map<Page, CultivationPageButton> buttons = new EnumMap<>(Page.class);
    private Page currentPage = Page.PATHS;

    public CultivationDisplayContainer(UIFrame frame, IntrospectionContainer owner) {
        super(frame);
        this.owner = owner;
        setWidth(234);
        setHeight(140);
        getPositioning().setX(-getWidth() / 2);
        getPositioning().setY(-getHeight() / 2);

        PathDisplayContainer pathDisplay = new PathDisplayContainer(frame, owner);
        pathDisplay.getPositioning().setX(0);
        pathDisplay.getPositioning().setY(0);
        addChild(pathDisplay);
        pages.put(Page.PATHS, pathDisplay);

        TechniqueDisplayContainer techniqueDisplay = new TechniqueDisplayContainer(frame, owner);
        techniqueDisplay.getPositioning().setX(0);
        techniqueDisplay.getPositioning().setY(0);
        addChild(techniqueDisplay);
        pages.put(Page.TECHNIQUES, techniqueDisplay);

        CultivationPageButton pathButton = createPageButton(
                frame,
                Page.PATHS,
                "textures/gui/main/path_menu/path_buttons.png",
                29
        );
        buttons.put(Page.PATHS, pathButton);

        CultivationPageButton techniqueButton = createPageButton(
                frame,
                Page.TECHNIQUES,
                "textures/gui/main/technique_menu/technique_tab_buttons.png",
                55
        );
        buttons.put(Page.TECHNIQUES, techniqueButton);

        openPage(Page.PATHS);
    }

    private CultivationPageButton createPageButton(
            UIFrame frame,
            Page page,
            String texturePath,
            int x
    ) {
        CultivationPageButton button = new CultivationPageButton(
                frame,
                this,
                page,
                Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, texturePath)
        );
        button.getPositioning().setX(x);
        button.getPositioning().setY(5);
        addChild(button);
        return button;
    }

    public void openPage(Page page) {
        currentPage = page;
        for (Map.Entry<Page, RenderableElement> entry : pages.entrySet()) {
            entry.getValue().setActive(entry.getKey() == page);
        }
        for (Map.Entry<Page, CultivationPageButton> entry : buttons.entrySet()) {
            entry.getValue().setSelected(entry.getKey() == page);
        }
        owner.refreshMenuTitle();
    }

    public Component getPageName() {
        return switch (currentPage) {
            case PATHS -> Component.translatable("gui.ascension.introspection.paths");
            case TECHNIQUES -> Component.translatable("gui.ascension.introspection.techniques");
        };
    }
}
