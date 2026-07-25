package net.zic.ascension.common.gui.elements.introspection.skill_display;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.lucent.easygui.gui.textures.ITextureData;
import net.lucent.easygui.gui.textures.TextureDataSubsection;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.core.CoreRegistries;
import net.zic.ascension.api.core.skill.Skill;
import net.zic.ascension.api.core.skill.castable.CastableSkill;
import net.zic.ascension.api.core.source.OriginSource;
import net.zic.ascension.common.gui.data.ClientAscensionData;
import net.zic.ascension.common.gui.elements.info.DescriptionDisplayContainer;
import net.zic.ascension.common.gui.elements.introspection.BackButton;
import net.zic.ascension.common.gui.elements.introspection.IntrospectionContainer;
import net.zic.ascension.network.SelectSkillSlotPacket;
import net.zic.ascension.network.UpdateSkillSlotPacket;

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

    private final SkillOptionsScrollBox skillOptions;
    private final DescriptionDisplayContainer selectedSkillInformation;
    private final SkillBarContainer skillBar;

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

        skillOptions = new SkillOptionsScrollBox(frame);
        skillOptions.getPositioning().setX(6);
        skillOptions.getPositioning().setY(43);
        addChild(skillOptions);

        selectedSkillInformation = new DescriptionDisplayContainer(
                frame,
                90,
                83,
                Component.translatable("gui.ascension.introspection.skills"),
                Component.translatable("gui.ascension.introspection.data_unavailable")
        );
        selectedSkillInformation.getPositioning().setX(121);
        selectedSkillInformation.getPositioning().setY(49);
        addChild(selectedSkillInformation);

        skillBar = new SkillBarContainer(frame, this);
        skillBar.getPositioning().setX(3);
        skillBar.getPositioning().setY(154);
        skillBar.setActive(false);
        addChild(skillBar);

        addChild(new OpenActiveSelection(frame, skillBar, 96, 141));
        addChild(new TogglePassiveButton(frame, this, 128, 23));
        refreshSynchronizedState();
    }

    public Identifier getSelectedSkill() {
        return selectedSkill;
    }

    public boolean isSelectedSkill(Identifier skillId) {
        return skillId != null && skillId.equals(selectedSkill);
    }

    public void selectSkill(Identifier skillId) {
        if (skillId == null || !displayedSkills.contains(skillId)) {
            return;
        }
        selectedSkill = skillId;
        showSelectedSkill();
    }

    public void handleSlotClick(SkillSlotButton slotButton) {
        Identifier skillToAssign = getCastableSelectedSkill();
        if (skillToAssign == null) {
            ClientPacketDistributor.sendToServer(
                    new SelectSkillSlotPacket(slotButton.getSlot())
            );
            return;
        }

        Identifier currentSkill = slotButton.getDisplayedSkill();
        ClientPacketDistributor.sendToServer(new UpdateSkillSlotPacket(
                slotButton.getSlot(),
                skillToAssign.equals(currentSkill) ? null : skillToAssign
        ));
    }

    private Identifier getCastableSelectedSkill() {
        if (selectedSkill == null) {
            return null;
        }

        return ClientAscensionData.getPlayer().map(player -> {
            Skill skill = CoreRegistries.safeAccess(
                    CoreRegistries.SKILL_REGISTRY,
                    selectedSkill,
                    player.registryAccess()
            );
            return skill instanceof CastableSkill ? selectedSkill : null;
        }).orElse(null);
    }

    private void refreshSynchronizedState() {
        OriginSource source = ClientAscensionData.getSource().orElse(null);
        long revision = source == null ? -1L : source.getRevision();
        if (source == observedSource && revision == observedRevision) {
            return;
        }

        observedSource = source;
        observedRevision = revision;

        if (source == null) {
            displayedSkills = List.of();
            selectedSkill = null;
            skillOptions.setSkills(this, displayedSkills);
            showInformation(
                    Component.translatable("gui.ascension.introspection.skills"),
                    Component.translatable("gui.ascension.introspection.data_unavailable")
            );
            return;
        }

        List<Identifier> skills = source.getSkills().stream()
                .sorted(Comparator.comparing(Identifier::toString))
                .toList();

        if (!skills.equals(displayedSkills)) {
            displayedSkills = skills;
            skillOptions.setSkills(this, skills);
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
        }
        showSelectedSkill();
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

            Component description = skill.getDescription() == null
                    ? Component.empty()
                    : skill.getDescription();
            if (skill instanceof CastableSkill) {
                description = Component.empty()
                        .append(description);
            }
            showInformation(skill.getName(), description);
        }, () -> showInformation(
                Component.literal(selectedSkill.toString()),
                Component.translatable("gui.ascension.introspection.data_unavailable")
        ));
    }

    private void showInformation(Component title, Component description) {
        selectedSkillInformation.setInformation(title, description);
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
