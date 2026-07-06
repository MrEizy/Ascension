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
import net.zic.ascension.common.data_attachements.AscensionAttachments;
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

    public SimpleAscensionEntityData(OriginSource source, LivingEntity entity) {
        this.source = source;
        this.attachedEntity = entity;

        if (!attachedEntity.level().isClientSide()) {
            AscensionCraft.getSourceHandler().addWatcher(attachedEntity, source);
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

        AscensionCraft.getSourceHandler().applyToWatcher(attachedEntity);

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

            if (buf.readBoolean()) {
                data.source.apply(
                        SourceChangesSnapshot.decode(buf, buf.registryAccess())
                );
            } else {
                data.source.decode(buf);
            }

            data.source.updateAttributes(
                    entity.getData(ZenithAttachments.ATTRIBUTE_HOLDER)
            );
            data.applyAllAttributeSuppressions();

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

            return true;
        }
    }
}