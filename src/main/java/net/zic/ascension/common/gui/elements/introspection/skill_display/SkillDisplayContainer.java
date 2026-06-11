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
import net.zic.ascension.api.core.source.OriginSource;
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

    private final Container optionsHolder;
    private final Container selectedSkillContainer;

    private OriginSource observedSource;
    private long observedRevision = Long.MIN_VALUE;
    private List<Identifier> displayedSkills = List.of();
    private Identifier selectedSkill;

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

        optionsHolder = new Container(frame, 89, 91);
        optionsHolder.getPositioning().setX(6);
        optionsHolder.getPositioning().setY(43);
        addChild(optionsHolder);

        selectedSkillContainer = new Container(frame, 90, 83);
        selectedSkillContainer.getPositioning().setX(121);
        selectedSkillContainer.getPositioning().setY(49);
        addChild(selectedSkillContainer);

        refreshSynchronizedState();
    }

    public void selectSkill(Identifier skillId) {
        selectedSkill = skillId;
        showSelectedSkill();
    }

    private void refreshSynchronizedState() {
        OriginSource source = ClientAscensionData.getSource().orElse(null);
        long revision = source == null ? -1L : source.getRevision();
        if (source == observedSource && revision == observedRevision) {
            return;
        }

        boolean sourceChanged = source != observedSource;
        observedSource = source;
        observedRevision = revision;

        if (source == null) {
            displayedSkills = List.of();
            selectedSkill = null;
            rebuildSkillOptions(displayedSkills);
            showInformation(
                    Component.translatable("gui.ascension.introspection.skills"),
                    Component.translatable("gui.ascension.introspection.data_unavailable")
            );
            return;
        }

        List<Identifier> skills = source.getSkills().stream()
                .sorted(Comparator.comparing(Identifier::toString))
                .toList();

        boolean skillsChanged = !skills.equals(displayedSkills);
        if (skillsChanged) {
            displayedSkills = skills;
            rebuildSkillOptions(skills);
        }

        if (skills.isEmpty()) {
            selectedSkill = null;
            showInformation(
                    Component.translatable("gui.ascension.introspection.no_skills"),
                    Component.translatable("gui.ascension.introspection.no_skills_description")
            );
            return;
        }

        if (selectedSkill == null || !skills.contains(selectedSkill)) {
            selectedSkill = skills.getFirst();
            showSelectedSkill();
            return;
        }

        if (skillsChanged || sourceChanged) {
            showSelectedSkill();
        }
    }

    private void rebuildSkillOptions(List<Identifier> skills) {
        optionsHolder.removeChildren();
        SkillOptionsScrollBox options = new SkillOptionsScrollBox(getUiFrame());
        optionsHolder.addChild(options);
        for (Identifier skillId : skills) {
            options.addChild(new SkillSelectionButton(getUiFrame(), this, skillId));
        }
    }

    private void showSelectedSkill() {
        if (selectedSkill == null) {
            showInformation(
                    Component.translatable("gui.ascension.introspection.no_skills"),
                    Component.translatable("gui.ascension.introspection.no_skills_description")
            );
            return;
        }

        ClientAscensionData.getPlayer().ifPresentOrElse(player -> {
            Skill skill = CoreRegistries.safeAccess(
                    CoreRegistries.SKILL_REGISTRY,
                    selectedSkill,
                    player.registryAccess()
            );
            if (skill == null) {
                showInformation(
                        Component.literal(selectedSkill.toString()),
                        Component.translatable("gui.ascension.introspection.missing_registry_entry")
                );
                return;
            }

            showInformation(
                    skill.getName(),
                    skill.getDescription() == null ? Component.empty() : skill.getDescription()
            );
        }, () -> showInformation(
                Component.literal(selectedSkill.toString()),
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
    public void renderTick(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        refreshSynchronizedState();
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        BACKGROUND.render(graphics);
    }
}
