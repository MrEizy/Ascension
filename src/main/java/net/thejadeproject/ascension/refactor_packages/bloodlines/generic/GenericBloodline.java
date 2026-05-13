package net.thejadeproject.ascension.refactor_packages.bloodlines.generic;

import net.lucent.easygui.gui.RenderableElement;
import net.lucent.easygui.gui.UIFrame;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.thejadeproject.ascension.refactor_packages.attributes.AttributeValueContainer;
import net.thejadeproject.ascension.refactor_packages.bloodlines.BloodlineSuppression;
import net.thejadeproject.ascension.refactor_packages.bloodlines.IBloodline;
import net.thejadeproject.ascension.refactor_packages.bloodlines.IBloodlineData;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
import net.thejadeproject.ascension.refactor_packages.gui.elements.info_elements.DescriptionDisplayContainer;
import net.thejadeproject.ascension.refactor_packages.util.value_modifiers.ModifierOperation;
import net.thejadeproject.ascension.refactor_packages.util.value_modifiers.ValueContainerModifier;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GenericBloodline implements IBloodline {

    private final Component title;
    private Component description;
    private Component shortDescription;

    private final LinkedHashMap<Holder<Attribute>, ValueContainerModifier> attributeModifiers = new LinkedHashMap<>();
    private final List<ResourceLocation> inherentSkills = new ArrayList<>();
    private final BloodlineSuppression suppression = new BloodlineSuppression();

    public GenericBloodline(Component title) {
        this.title = title;
    }

    public GenericBloodline setDescription(Component description) {
        this.description = description;
        return this;
    }

    public GenericBloodline setShortDescription(Component shortDescription) {
        this.shortDescription = shortDescription;
        return this;
    }

    /*
    Apply a modifier directly to an attributes ValueContainer when this bloodline is given.
    */
    public GenericBloodline addAttributeModifier(Holder<Attribute> attribute, ValueContainerModifier modifier) {
        attributeModifiers.put(attribute, modifier);
        return this;
    }

    /*
     this is a flat bonus to attributes. Need to be changed to be Mod shit later.
     */
    public GenericBloodline addFlatAttribute(Holder<Attribute> attribute, double value, ResourceLocation modifierKey) {
        return addAttributeModifier(attribute, new ValueContainerModifier(value, ModifierOperation.ADD_BASE, modifierKey));
    }

    /*
     This allows you to add a Inherent Skill to a bloodline that players get immedietly.
     */
    public GenericBloodline addInherentSkill(ResourceLocation skillId) {
        inherentSkills.add(skillId);
        return this;
    }

    /**
     * Sets the suppression tier for this bloodline.
     * A bloodline with tier 9 automatically suppresses all bloodlines with a tier strictly below 9.
     * Use values 1-9 (or however many tiers you define).
     * Bloodlines with no tier set (default 0) are not part of the tier hierarchy.
     *
     * Example: tier 3 suppresses tier 2 and tier 1 bloodlines.
     */
    public GenericBloodline addSuppressionTier(int tier) {
        suppression.setTier(tier);
        return this;
    }

    /**
     * Adds a specific bloodline that this bloodline always suppresses,
     * regardless of tier. Useful for rivalries between bloodlines that don't share a tier hierarchy.
     *
     * Example: .addSuppressionBloodline(rl("phoenix_bloodline"))
     */
    public GenericBloodline addSuppressionBloodline(ResourceLocation bloodlineId) {
        suppression.addSpecificTarget(bloodlineId);
        return this;
    }

    /** Convenience overload — accepts a string in "namespace:path" format. */
    public GenericBloodline addSuppressionBloodline(String bloodlineId) {
        return addSuppressionBloodline(ResourceLocation.parse(bloodlineId));
    }

    // ─────────────────────────────── IBloodline ───────────────────────────────

    @Override
    public IBloodlineData onBloodlineAdded(IEntityData heldEntity, ResourceLocation newBloodline) {
        IBloodlineData data = freshBloodlineData(heldEntity);
        applyAttributes(heldEntity);
        grantSkills(heldEntity);
        return data;
    }

    @Override
    public void onBloodlineAdded(IEntityData heldEntity, IBloodlineData bloodlineData, ResourceLocation newBloodline) {
        applyAttributes(heldEntity);
        grantSkills(heldEntity);
    }

    @Override
    public void onEntityTethered(IEntityData heldEntity, IEntityData tetheredEntity, IBloodlineData bloodlineData) {}

    @Override
    public void onEntityUntethered(IEntityData heldEntity, IEntityData oldTetheredEntity, IBloodlineData bloodlineData) {}

    @Override
    public void onFormAdded(IEntityData heldEntity, ResourceLocation form, IBloodlineData bloodlineData) {}

    @Override
    public void onFormRemoved(IEntityData heldEntity, ResourceLocation form, IBloodlineData bloodlineData) {}

    @Override
    public IBloodlineData freshBloodlineData(IEntityData heldEntity) {
        return new GenericBloodlineData();
    }

    @Override
    public IBloodlineData fromCompound(CompoundTag tag, IEntityData heldEntity) {
        return new GenericBloodlineData();
    }

    @Override
    public IBloodlineData fromNetwork(RegistryFriendlyByteBuf buf) {
        buf.readUtf();
        return new GenericBloodlineData();
    }

    // ─────────────────────────────── suppression ──────────────────────────────

    /** Returns the suppression profile for this bloodline. Used by BloodlineSuppressionHandler. */
    public BloodlineSuppression getSuppression() {
        return suppression;
    }

    public boolean hasSuppression() {
        return suppression.hasSuppression();
    }


    // ─────────────────────────────── helpers ──────────────────────────────────

    private void applyAttributes(IEntityData heldEntity) {
        for (Map.Entry<Holder<Attribute>, ValueContainerModifier> entry : attributeModifiers.entrySet()) {
            AttributeValueContainer container = heldEntity.getAscensionAttributeHolder().getAttribute(entry.getKey());
            if (container == null) continue;
            container.addModifier(entry.getValue());
        }
        heldEntity.getAscensionAttributeHolder().updateAttributes(heldEntity);
    }

    protected void removeAttributes(IEntityData heldEntity) {
        for (Map.Entry<Holder<Attribute>, ValueContainerModifier> entry : attributeModifiers.entrySet()) {
            AttributeValueContainer container = heldEntity.getAscensionAttributeHolder().getAttribute(entry.getKey());
            if (container == null) continue;
            container.removeModifier(entry.getValue().getIdentifier());
        }
        heldEntity.getAscensionAttributeHolder().updateAttributes(heldEntity);
    }

    private void grantSkills(IEntityData heldEntity) {
        ResourceLocation form = heldEntity.getBloodlineForm();
        for (ResourceLocation skill : inherentSkills) {
            if (!heldEntity.hasSkill(skill)) {
                heldEntity.giveSkill(skill, form);
            }
        }
    }

    protected void revokeSkills(IEntityData heldEntity) {
        for (ResourceLocation skill : inherentSkills) {
            heldEntity.removeSkill(skill);
        }
    }

    // ─────────────────────────────── display ──────────────────────────────────

    public Component getDisplayTitle()     { return title; }
    public Component getShortDescription() { return shortDescription; }
    public Component getDescription()      { return description; }

    @OnlyIn(Dist.CLIENT)
    public RenderableElement getInformationContainer(UIFrame frame) {
        return new DescriptionDisplayContainer(frame, getDisplayTitle(), getDescription());
    }

    protected void broadcastRareAcquired(IEntityData heldEntity, String translationKey) {
        if (!(heldEntity.getAttachedEntity() instanceof ServerPlayer player)) return;
        Component message = Component.translatable(
                translationKey,
                player.getDisplayName().copy().withStyle(ChatFormatting.WHITE),
                getDisplayTitle().copy().withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD)
        ).withStyle(ChatFormatting.GOLD);
        player.server.getPlayerList().broadcastSystemMessage(message, false);
    }
}