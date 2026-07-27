package net.zic.ascension.impl.core.entity;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.attachment.AttachmentSyncHandler;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.core.entity.AscensionEntityData;
import net.zic.ascension.api.core.source.OriginSource;
import net.zic.ascension.api.core.source.ServerOriginSource;
import net.zic.ascension.api.core.source.SourceChangesSnapshot;
import net.zic.ascension.impl.core.source.SourceHandler;
import net.zic.ascension.common.data_attachements.AscensionAttachments;
import net.zic.ascension.common.starter.StarterSelectionStage;
import net.zic.ascension.common.util.AscensionAttributes;
import net.zic.zenithlib.common.ZenithAttachments;
import net.zic.zenithlib.custom_attributes.ZenithAttribute;
import net.zic.zenithlib.custom_attributes.ZenithAttributeHolder;
import net.zic.zenithlib.nbt.NbtHelpers;
import net.zic.zenithlib.network.ByteBufHelpers;
import net.zic.zenithlib.stats.Stat;
import net.zic.zenithlib.value_containers.ModifierOperation;
import net.zic.zenithlib.value_containers.ValueContainerModifier;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SimpleAscensionEntityData implements AscensionEntityData {
    private static final double MIN_SUPPRESSION_PERCENTAGE = 0.001D;
    private static final double MAX_SUPPRESSION_PERCENTAGE = 1.0D;

    private static final List<Holder<Attribute>> SUPPRESSIBLE_ATTRIBUTES = List.of(
            Attributes.ATTACK_DAMAGE,
            Attributes.ATTACK_SPEED,
            Attributes.MOVEMENT_SPEED,
            Attributes.JUMP_STRENGTH,
            Attributes.STEP_HEIGHT,
            Attributes.MINING_EFFICIENCY
    );

    private final OriginSource source;
    private final LivingEntity attachedEntity;

    private final Map<Identifier, Double> attributeSuppression = new HashMap<>();

    private SourceChangesSnapshot snapshot;
    private boolean cultivationSuppressed;

    private StarterSelectionStage starterSelectionStage = StarterSelectionStage.BLOODLINE;
    private final List<Identifier> offeredStarterBloodlines = new ArrayList<>();
    private final List<Identifier> offeredStarterPhysiques = new ArrayList<>();
    private Identifier selectedStarterBloodline;
    private Identifier selectedStarterPhysique;
    private boolean starterSelectionComplete;

    public SimpleAscensionEntityData(OriginSource source, LivingEntity entity) {
        this.source = source;
        this.attachedEntity = entity;
        this.source.setRegistryAccess(entity.registryAccess());

        if (!attachedEntity.level().isClientSide()) {
            SourceHandler sourceHandler = AscensionCraft.getSourceHandler();
            if (sourceHandler != null && !sourceHandler.isWatcher(attachedEntity)) {
                sourceHandler.addWatcher(attachedEntity, source);
            }
        }
    }

    public void initializeAttributes() {
        ZenithAttributeHolder attributeHolder = attachedEntity.getData(
                ZenithAttachments.ATTRIBUTE_HOLDER
        );

        // WIll BE BALANCED OVER TIME HOPEFULLY
        addStatScaling(attributeHolder, Attributes.MAX_HEALTH,
                AscensionStats.VITALITY.get(), "base_scaling", 2.0D);

        addStatScaling(attributeHolder, Attributes.ATTACK_DAMAGE,
                AscensionStats.STRENGTH.get(), "strength_damage_scaling", 1.0D);

        addStatScaling(attributeHolder, Attributes.JUMP_STRENGTH,
                AscensionStats.STRENGTH.get(), "strength_jump_scaling", 0.005D);

        addStatScaling(attributeHolder, Attributes.MOVEMENT_SPEED,
                AscensionStats.STRENGTH.get(), "strength_movement_scaling", 0.00005D);

        addStatScaling(attributeHolder, Attributes.MOVEMENT_SPEED,
                AscensionStats.AGILITY.get(), "agility_movement_scaling", 0.002D);

        addStatScaling(attributeHolder, Attributes.STEP_HEIGHT,
                AscensionStats.AGILITY.get(), "agility_step_height_scaling", 0.05D);

        attributeHolder.addAttribute(Attributes.ARMOR);
        attributeHolder.addAttribute(Attributes.ARMOR_TOUGHNESS);

        addStatScaling(attributeHolder, Attributes.ATTACK_SPEED,
                AscensionStats.STRENGTH.get(), "strength_attack_speed_scaling", 0.001D);

        addStatScaling(attributeHolder, Attributes.ATTACK_SPEED,
                AscensionStats.AGILITY.get(), "agility_attack_speed_scaling", 0.001D);

        attributeHolder.addAttribute(Attributes.LUCK);

        addStatScaling(attributeHolder, Attributes.MINING_EFFICIENCY,
                AscensionStats.STRENGTH.get(), "strength_mining_scaling", 0.001D);

        addStatScaling(attributeHolder, Attributes.SAFE_FALL_DISTANCE,
                AscensionStats.STRENGTH.get(), "strength_safe_fall_scaling", 0.1D);

        addStatScaling(attributeHolder, AscensionAttributes.MAX_QI,
                AscensionStats.SPIRIT.get(), "spirit_max_qi_scaling", 10.0D);

        addStatScaling(attributeHolder, AscensionAttributes.QI_REGEN_RATE,
                AscensionStats.SPIRIT.get(), "spirit_qi_regen_scaling", 0.25D);

        attributeHolder.addAttribute(AscensionAttributes.HEALTH_REGEN_RATE);

        addStatScaling(attributeHolder, AscensionAttributes.MAX_STAMINA,
                AscensionStats.VITALITY.get(), "vitality_max_stamina_scaling", 5.0D);

        addStatScaling(attributeHolder, AscensionAttributes.MAX_STAMINA,
                AscensionStats.STRENGTH.get(), "strength_max_stamina_scaling", 2.0D);

        addStatScaling(attributeHolder, AscensionAttributes.STAMINA_REGEN_RATE,
                AscensionStats.VITALITY.get(), "vitality_stamina_regen_scaling", 0.02D);

        addStatScaling(attributeHolder, AscensionAttributes.STAMINA_REGEN_RATE,
                AscensionStats.AGILITY.get(), "agility_stamina_regen_scaling", 0.01D);

        attributeHolder.addAttribute(AscensionAttributes.STAMINA_REGEN_DELAY);

        source.updateAttributes(attributeHolder);
        applyAllAttributeSuppressions();
    }

    private void addStatScaling(
            ZenithAttributeHolder attributeHolder,
            Holder<Attribute> attribute,
            Stat stat,
            String scalingName,
            double value
    ) {
        Identifier scalingId = Identifier.fromNamespaceAndPath(
                AscensionCraft.MOD_ID,
                scalingName
        );

        attributeHolder.addAttribute(attribute);

        ZenithAttribute zenithAttribute = attributeHolder.getAttribute(attribute);
        if (zenithAttribute == null) {
            return;
        }

        zenithAttribute.removeScaling(stat, scalingId);
        zenithAttribute.addStatScaling(stat, scalingId, value);
    }

    @Override
    public LivingEntity getEntity() {
        return attachedEntity;
    }

    @Override
    public OriginSource getSource() {
        return source;
    }

    @Override
    public void initialize() {
        source.load();
        initializeAttributes();
        applyAllAttributeSuppressions();

        SourceHandler sourceHandler = AscensionCraft.getSourceHandler();
        if (!attachedEntity.level().isClientSide() && sourceHandler != null) {
            if (!sourceHandler.isWatcher(attachedEntity)) {
                sourceHandler.addWatcher(attachedEntity, source);
            } else {
                sourceHandler.changeWatcherState(attachedEntity, true);
            }
            sourceHandler.applyToWatcher(attachedEntity);
        }

        if (!attachedEntity.level().isClientSide()) {
            snapshot = null;
            attachedEntity.syncData(AscensionAttachments.SIMPLE_ENTITY_DATA);
        }
    }

    @Override
    public void markDirty(SourceChangesSnapshot snapshot) {
        if (attachedEntity.level().isClientSide()) {
            return;
        }

        this.snapshot = snapshot;
        attachedEntity.syncData(AscensionAttachments.SIMPLE_ENTITY_DATA);
    }

    @Override
    public boolean isCultivationSuppressed() {
        return cultivationSuppressed;
    }

    @Override
    public void setCultivationSuppressed(boolean state) {
        cultivationSuppressed = state;
    }

    public StarterSelectionStage getStarterSelectionStage() {
        return starterSelectionStage;
    }

    public void setStarterSelectionStage(StarterSelectionStage stage) {
        starterSelectionStage = stage == null ? StarterSelectionStage.BLOODLINE : stage;
    }

    public List<Identifier> getOfferedStarterBloodlines() {
        return List.copyOf(offeredStarterBloodlines);
    }

    public void setOfferedStarterBloodlines(Collection<Identifier> bloodlines) {
        offeredStarterBloodlines.clear();
        if (bloodlines != null) {
            offeredStarterBloodlines.addAll(bloodlines.stream()
                    .filter(id -> id != null)
                    .distinct()
                    .toList());
        }
    }

    public List<Identifier> getOfferedStarterPhysiques() {
        return List.copyOf(offeredStarterPhysiques);
    }

    public void setOfferedStarterPhysiques(Collection<Identifier> physiques) {
        offeredStarterPhysiques.clear();
        if (physiques != null) {
            offeredStarterPhysiques.addAll(physiques.stream()
                    .filter(id -> id != null)
                    .distinct()
                    .toList());
        }
    }

    public Identifier getSelectedStarterBloodline() {
        return selectedStarterBloodline;
    }

    public void setSelectedStarterBloodline(Identifier bloodline) {
        selectedStarterBloodline = bloodline;
    }

    public Identifier getSelectedStarterPhysique() {
        return selectedStarterPhysique;
    }

    public void setSelectedStarterPhysique(Identifier physique) {
        selectedStarterPhysique = physique;
    }

    public boolean isStarterSelectionComplete() {
        return starterSelectionComplete;
    }

    public void setStarterSelectionComplete(boolean complete) {
        starterSelectionComplete = complete;
        if (complete) {
            starterSelectionStage = StarterSelectionStage.COMPLETE;
        }
    }

    public static Identifier getAttributeId(Holder<Attribute> attribute) {
        return BuiltInRegistries.ATTRIBUTE.getKey(attribute.value());
    }

    public static Optional<Holder<Attribute>> getSuppressibleAttribute(Identifier attributeId) {
        if (attributeId == null) {
            return Optional.empty();
        }

        return SUPPRESSIBLE_ATTRIBUTES.stream()
                .filter(attribute -> attributeId.equals(getAttributeId(attribute)))
                .findFirst();
    }

    public static boolean isSuppressibleAttribute(Identifier attributeId) {
        return getSuppressibleAttribute(attributeId).isPresent();
    }

    public static boolean isSuppressibleAttribute(Holder<Attribute> attribute) {
        return isSuppressibleAttribute(getAttributeId(attribute));
    }

    private static Identifier getSuppressionModifierId(Identifier attributeId) {
        return Identifier.fromNamespaceAndPath(
                AscensionCraft.MOD_ID,
                "suppression/" + attributeId.getNamespace() + "/" + attributeId.getPath()
        );
    }

    @Override
    public double getAttributeSuppression(Holder<Attribute> attribute) {
        if (!isSuppressibleAttribute(attribute)) {
            return 1.0D;
        }

        Identifier attributeId = getAttributeId(attribute);
        if (attributeId == null) {
            return 1.0D;
        }

        return attributeSuppression.getOrDefault(attributeId, 1.0D);
    }

    @Override
    public void setAttributeSuppression(Holder<Attribute> attribute, double percentage) {
        if (!isSuppressibleAttribute(attribute)) {
            return;
        }

        Identifier attributeId = getAttributeId(attribute);
        if (attributeId == null) {
            return;
        }

        percentage = Math.clamp(
                percentage,
                MIN_SUPPRESSION_PERCENTAGE,
                MAX_SUPPRESSION_PERCENTAGE
        );

        if (percentage >= 1.0D) {
            attributeSuppression.remove(attributeId);
        } else {
            attributeSuppression.put(attributeId, percentage);
        }
    }

    @Override
    public void applyAttributeSuppression(Holder<Attribute> attribute) {
        Identifier attributeId = getAttributeId(attribute);

        if (!isSuppressibleAttribute(attribute)) {
            return;
        }

        if (attributeId == null) {
            return;
        }

        ZenithAttributeHolder attributeHolder = attachedEntity.getData(
                ZenithAttachments.ATTRIBUTE_HOLDER
        );

        ZenithAttribute zenithAttribute = attributeHolder.getAttribute(attribute);
        if (zenithAttribute == null) {
            return;
        }

        Identifier modifierId = getSuppressionModifierId(attributeId);

        zenithAttribute.removeModifier(modifierId);

        double percentage = getAttributeSuppression(attribute);

        if (percentage < 1.0D) {
            zenithAttribute.addModifier(new ValueContainerModifier(
                    percentage - 1.0D,
                    ModifierOperation.MULTIPLY_FINAL,
                    modifierId
            ));
        }
    }

    @Override
    public void applyAllAttributeSuppressions() {
        for (Holder<Attribute> attribute : SUPPRESSIBLE_ATTRIBUTES) {
            applyAttributeSuppression(attribute);
        }
    }

    private static void encodeIdentifierList(RegistryFriendlyByteBuf buf, Collection<Identifier> identifiers) {
        buf.writeVarInt(identifiers.size());
        for (Identifier identifier : identifiers) {
            ByteBufHelpers.encodeIdentifier(identifier, buf);
        }
    }

    private static List<Identifier> decodeIdentifierList(RegistryFriendlyByteBuf buf) {
        int size = buf.readVarInt();
        List<Identifier> identifiers = new ArrayList<>(size);
        for (int index = 0; index < size; index++) {
            identifiers.add(ByteBufHelpers.decodeIdentifier(buf));
        }
        return identifiers;
    }

    private static void encodeOptionalIdentifier(RegistryFriendlyByteBuf buf, Identifier identifier) {
        buf.writeBoolean(identifier != null);
        if (identifier != null) {
            ByteBufHelpers.encodeIdentifier(identifier, buf);
        }
    }

    private static Identifier decodeOptionalIdentifier(RegistryFriendlyByteBuf buf) {
        return buf.readBoolean() ? ByteBufHelpers.decodeIdentifier(buf) : null;
    }

    private static StarterSelectionStage readStarterSelectionStage(String name) {
        try {
            return StarterSelectionStage.valueOf(name);
        } catch (IllegalArgumentException exception) {
            return StarterSelectionStage.BLOODLINE;
        }
    }

    private static Identifier readOptionalIdentifier(ValueInput input, String key) {
        String value = input.getStringOr(key, "");
        if (value.isBlank()) {
            return null;
        }
        try {
            return Identifier.parse(value);
        } catch (Exception exception) {
            return null;
        }
    }

    private static List<Identifier> readIdentifierList(ValueInput input, String key) {
        List<Identifier> identifiers = new ArrayList<>();
        for (ValueInput element : input.childrenListOrEmpty(key)) {
            String value = element.getStringOr("id", "");
            if (value.isBlank()) {
                continue;
            }
            try {
                identifiers.add(Identifier.parse(value));
            } catch (Exception ignored) {
            }
        }
        return identifiers;
    }

    private static void writeOptionalIdentifier(ValueOutput output, String key, Identifier identifier) {
        if (identifier != null) {
            output.putString(key, identifier.toString());
        }
    }

    private static void writeIdentifierList(ValueOutput output, String key, Collection<Identifier> identifiers) {
        ValueOutput.ValueOutputList list = output.childrenList(key);
        for (Identifier identifier : identifiers) {
            if (identifier == null) {
                continue;
            }
            ValueOutput element = list.addChild();
            element.putString("id", identifier.toString());
        }
    }

    public static class SyncHandler implements AttachmentSyncHandler<SimpleAscensionEntityData> {
        @Override
        public boolean sendToPlayer(@NonNull IAttachmentHolder holder, @NonNull ServerPlayer to) {
            return holder == to;
        }

        @Override
        public void write(
                RegistryFriendlyByteBuf buf,
                SimpleAscensionEntityData attachment,
                boolean initialSync
        ) {
            buf.writeBoolean(attachment.cultivationSuppressed);

            buf.writeVarInt(attachment.attributeSuppression.size());
            for (Map.Entry<Identifier, Double> entry : attachment.attributeSuppression.entrySet()) {
                ByteBufHelpers.encodeIdentifier(entry.getKey(), buf);
                buf.writeDouble(entry.getValue());
            }

            buf.writeEnum(attachment.starterSelectionStage);
            buf.writeBoolean(attachment.starterSelectionComplete);
            encodeIdentifierList(buf, attachment.offeredStarterBloodlines);
            encodeIdentifierList(buf, attachment.offeredStarterPhysiques);
            encodeOptionalIdentifier(buf, attachment.selectedStarterBloodline);
            encodeOptionalIdentifier(buf, attachment.selectedStarterPhysique);

            boolean encodePatch = !initialSync && attachment.snapshot != null;
            buf.writeBoolean(encodePatch);

            if (encodePatch) {
                attachment.snapshot.encode(buf);
            } else {
                attachment.source.encode(buf);
            }

            attachment.snapshot = null;
        }

        @Override
        public @Nullable SimpleAscensionEntityData read(
                IAttachmentHolder holder,
                RegistryFriendlyByteBuf buf,
                @Nullable SimpleAscensionEntityData previousValue
        ) {
            if (!(holder instanceof LivingEntity entity)) {
                return null;
            }

            if (previousValue == null) {
                previousValue = new SimpleAscensionEntityData(
                        new OriginSource(),
                        entity
                );
            }

            SimpleAscensionEntityData data = previousValue;

            data.setCultivationSuppressed(buf.readBoolean());
            data.attributeSuppression.clear();

            int suppressionCount = buf.readVarInt();
            for (int i = 0; i < suppressionCount; i++) {
                Identifier attributeId = ByteBufHelpers.decodeIdentifier(buf);
                double percentage = buf.readDouble();

                getSuppressibleAttribute(attributeId).ifPresent(attribute ->
                        data.setAttributeSuppression(attribute, percentage)
                );
            }

            data.setStarterSelectionStage(buf.readEnum(StarterSelectionStage.class));
            data.setStarterSelectionComplete(buf.readBoolean());
            data.setOfferedStarterBloodlines(decodeIdentifierList(buf));
            data.setOfferedStarterPhysiques(decodeIdentifierList(buf));
            data.setSelectedStarterBloodline(decodeOptionalIdentifier(buf));
            data.setSelectedStarterPhysique(decodeOptionalIdentifier(buf));

            if (buf.readBoolean()) {
                data.source.apply(
                        SourceChangesSnapshot.decode(buf, buf.registryAccess())
                );
            } else {
                data.source.decode(buf);
            }

            data.initializeAttributes();

            return data;
        }
    }

    public static class Provider implements IAttachmentSerializer<SimpleAscensionEntityData> {
        @Override
        public SimpleAscensionEntityData read(
                @NonNull IAttachmentHolder holder,
                ValueInput input
        ) {
            if (!(holder instanceof LivingEntity entity)) {
                return null;
            }


            OriginSource source = entity.level().isClientSide()
                    ? new OriginSource(input.childOrEmpty("source_data"))
                    : new ServerOriginSource(input.childOrEmpty("source_data"));

            SimpleAscensionEntityData data = new SimpleAscensionEntityData(source, entity);

            data.setCultivationSuppressed(
                    input.getBooleanOr("cultivation_suppressed", false)
            );

            NbtHelpers.readList(input, "attribute_suppression", (elementInput, id) -> {
                Identifier attributeId = NbtHelpers.readIdentifier(elementInput, "attribute");
                double percentage = elementInput.getDoubleOr("percentage", 1.0D);

                getSuppressibleAttribute(attributeId).ifPresent(attribute ->
                        data.setAttributeSuppression(attribute, percentage)
                );

                return attributeId;
            });

            data.setStarterSelectionStage(readStarterSelectionStage(
                    input.getStringOr("starter_selection_stage", StarterSelectionStage.BLOODLINE.name())
            ));
            data.setStarterSelectionComplete(
                    input.getBooleanOr("starter_selection_complete", false)
            );
            data.setOfferedStarterBloodlines(readIdentifierList(input, "offered_starter_bloodlines"));
            data.setOfferedStarterPhysiques(readIdentifierList(input, "offered_starter_physiques"));
            data.setSelectedStarterBloodline(readOptionalIdentifier(input, "selected_starter_bloodline"));
            data.setSelectedStarterPhysique(readOptionalIdentifier(input, "selected_starter_physique"));

            return data;
        }

        @Override
        public boolean write(SimpleAscensionEntityData attachment, ValueOutput output) {
            attachment.source.write(output.child("source_data"));

            output.putBoolean(
                    "cultivation_suppressed",
                    attachment.isCultivationSuppressed()
            );

            NbtHelpers.writeCollection(
                    output,
                    "attribute_suppression",
                    attachment.attributeSuppression.entrySet(),
                    (elementOutput, id, entry) -> {
                        NbtHelpers.writeIdentifier(elementOutput, "attribute", entry.getKey());
                        elementOutput.putDouble("percentage", entry.getValue());
                    }
            );

            output.putString("starter_selection_stage", attachment.starterSelectionStage.name());
            output.putBoolean("starter_selection_complete", attachment.starterSelectionComplete);
            writeIdentifierList(output, "offered_starter_bloodlines", attachment.offeredStarterBloodlines);
            writeIdentifierList(output, "offered_starter_physiques", attachment.offeredStarterPhysiques);
            writeOptionalIdentifier(output, "selected_starter_bloodline", attachment.selectedStarterBloodline);
            writeOptionalIdentifier(output, "selected_starter_physique", attachment.selectedStarterPhysique);

            return true;
        }
    }
}