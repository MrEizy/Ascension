package net.zic.ascension.common.gui.elements.introspection.skill_display;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.textures.ITextureData;
import net.lucent.easygui.gui.textures.TextureDataSubsection;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.core.CoreRegistries;
import net.zic.ascension.api.core.skill.Skill;
import net.zic.ascension.common.gui.data.ClientAscensionData;
import net.zic.ascension.common.gui.elements.general.Container;
import net.zic.ascension.common.gui.elements.info.DescriptionDisplayContainer;
import net.zic.ascension.common.gui.elements.introspection.BackButton;
import net.zic.ascension.common.gui.elements.introspection.IntrospectionContainer;

import java.util.Comparator;
import java.util.List;

public class SkillDisplayContainer extends RenderableElement {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            AscensionCraft.MOD_ID,
            "textures/gui/main/skill_menu/skill_menu.png"
    );

    private static final ITextureData BACKGROUND = new TextureDataSubsection(
            TEXTURE, 234, 286, 0, 40, 234, 157
    );

    private final Container selectedSkillContainer;

    public SkillDisplayContainer(UIFrame frame, IntrospectionContainer owner) {
        super(frame);
        setWidth(BACKGROUND.getWidth());
        setHeight(BACKGROUND.getHeight());
        getPositioning().setX(-getWidth() / 2);
        getPositioning().setY(-getHeight() / 2 + 8);

        BackButton backButton = new BackButton(frame, owner);
        backButton.getPositioning().setX(5);
        backButton.getPositioning().setY(5);
        addChild(backButton);

        SkillOptionsScrollBox options = new SkillOptionsScrollBox(frame);
        options.getPositioning().setX(6);
        options.getPositioning().setY(43);
        addChild(options);

        selectedSkillContainer = new Container(frame, 90, 83);
        selectedSkillContainer.getPositioning().setX(121);
        selectedSkillContainer.getPositioning().setY(49);
        addChild(selectedSkillContainer);

        List<Identifier> skills = ClientAscensionData.getSource()
                .map(source -> source.getSkills().stream()
                        .sorted(Comparator.comparing(Identifier::toString))
                        .toList())
                .orElse(List.of());

        for (Identifier skillId : skills) {
            options.addChild(new SkillSelectionButton(frame, this, skillId));
        }

        if (skills.isEmpty()) {
            showInformation(
                    Component.translatable("gui.ascension.introspection.no_skills"),
                    Component.translatable("gui.ascension.introspection.no_skills_description")
            );
        } else {
            selectSkill(skills.getFirst());
        }
    }

    public void selectSkill(Identifier skillId) {
        ClientAscensionData.getPlayer().ifPresentOrElse(player -> {
            Skill skill = CoreRegistries.safeAccess(
                    CoreRegistries.SKILL_REGISTRY,
                    skillId,
                    player.registryAccess()
            );
            if (skill == null) {
                showInformation(
                        Component.literal(skillId.toString()),
                        Component.translatable("gui.ascension.introspection.missing_registry_entry")
                );
                return;
            }
            showInformation(skill.getName(), skill.getDescription());
        }, () -> showInformation(
                Component.literal(skillId.toString()),
                Component.translatable("gui.ascension.introspection.data_unavailable")
        ));
    }

    private void showInformation(Component title, Component description) {
        selectedSkillContainer.removeChildren();
        DescriptionDisplayContainer display = new DescriptionDisplayContainer(
                getUiFrame(),
                title,
                description
        );
        selectedSkillContainer.addChild(display);
        display.refresh();
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        BACKGROUND.render(graphics);
    }
}
