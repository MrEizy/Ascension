package net.zic.ascension.impl.core.entity;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.attachment.AttachmentSyncHandler;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.zic.ascension.api.ascension.core.CoreAttachments;
import net.zic.ascension.api.ascension.core.entity.AscensionEntityData;
import net.zic.ascension.api.ascension.core.path.bonus.EntityPathBonusHolder;
import net.zic.ascension.api.ascension.core.path.bonus.PathBonus;
import net.zic.ascension.api.ascension.core.path.bonus.MultiSourcePathBonusHolder;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.api.rpg_engine.source.OriginSourcePatch;
import net.zic.ascension.common.data_attachements.AscensionAttachments;
import net.zic.ascension.common.starter.StarterSelectionStage;
import net.zic.zenithlib.common.ZenithAttachments;
import net.zic.zenithlib.custom_attributes.ZenithAttributeHolder;
import net.zic.zenithlib.network.ByteBufHelpers;
import net.zic.zenithlib.stats.*;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class SimpleAscensionEntityData implements AscensionEntityData {



    private final OriginSource source;
    private final LivingEntity attachedEntity;

    private float cachedHealth;

    private boolean fullPatch;
    private OriginSourcePatch patch;

    private boolean cultivationSuppressed;

    private StarterSelectionStage starterSelectionStage = StarterSelectionStage.BLOODLINE;
    private final List<Identifier> offeredStarterBloodlines = new ArrayList<>();
    private final List<Identifier> offeredStarterPhysiques = new ArrayList<>();
    private Identifier selectedStarterBloodline;
    private Identifier selectedStarterPhysique;
    private boolean starterSelectionComplete;

    private final Random random = new Random();

    private String process = null;

    public SimpleAscensionEntityData(OriginSource source, LivingEntity entity) {
        this.source = source;
        this.attachedEntity = entity;
        this.cachedHealth = Math.max(0.0F, entity.getHealth());
        this.source.setRegistryAccess(entity.registryAccess());
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
        initializeInternal(false);
    }

    @Override
    public void initializeAfterRespawn() {
        initializeInternal(true);
    }

    private void initializeInternal(boolean fullHealth) {
        AscensionEntityData.super.initialize();

        ZenithAttributeHolder attributeHolder = attachedEntity.getData(ZenithAttachments.ATTRIBUTE_HOLDER);
        EntityStatHolder statHolder = attachedEntity.getData(ZenithAttachments.STAT_HOLDER);
        EntityPathBonusHolder pathBonusHolder = attachedEntity.getData(CoreAttachments.PATH_BONUS_HOLDER);

        attributeHolder.startProcess("initialize_on_entity");
        statHolder.startProcess("initialize_on_entity");
        pathBonusHolder.startProcess("initialize_on_entity");

        markDirty(getSource().load(),true);
        getSource().attachToEntity(getEntity());

        pathBonusHolder.resolveProcess("initialize_on_entity");
        statHolder.resolveProcess("initialize_on_entity");
        attributeHolder.resolveProcess("initialize_on_entity");

        float restoredHealth = fullHealth ? attachedEntity.getMaxHealth() : Math.min(cachedHealth, attachedEntity.getMaxHealth());
        attachedEntity.setHealth(Math.max(0.0F, restoredHealth));
        cachedHealth = attachedEntity.getHealth();
    }




    @Override
    public void markDirty(OriginSourcePatch patch, boolean fullPatch) {
        if (attachedEntity == null || attachedEntity.level().isClientSide()) {
            return;
        }

        this.patch = patch;
        this.fullPatch = fullPatch;
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


            buf.writeEnum(attachment.starterSelectionStage);
            buf.writeBoolean(attachment.starterSelectionComplete);
            encodeIdentifierList(buf, attachment.offeredStarterBloodlines);
            encodeIdentifierList(buf, attachment.offeredStarterPhysiques);
            encodeOptionalIdentifier(buf, attachment.selectedStarterBloodline);
            encodeOptionalIdentifier(buf, attachment.selectedStarterPhysique);


            buf.writeBoolean(attachment.patch != null);
            if(attachment.patch != null){
                if(attachment.fullPatch) OriginSourcePatch.fullEncode(attachment.patch,buf,attachment.getEntity().registryAccess());
                else OriginSourcePatch.encodePatch(attachment.patch,buf,attachment.getEntity().registryAccess());
                attachment.patch = null;
            }
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


            data.setStarterSelectionStage(buf.readEnum(StarterSelectionStage.class));
            data.setStarterSelectionComplete(buf.readBoolean());
            data.setOfferedStarterBloodlines(decodeIdentifierList(buf));
            data.setOfferedStarterPhysiques(decodeIdentifierList(buf));
            data.setSelectedStarterBloodline(decodeOptionalIdentifier(buf));
            data.setSelectedStarterPhysique(decodeOptionalIdentifier(buf));

            if (buf.readBoolean()) {
                data.source.applyPatch(buf);
            }


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

            OriginSource originSource = new OriginSource();
            originSource.setCachedData(input.childOrEmpty("source_data"));



            SimpleAscensionEntityData data = new SimpleAscensionEntityData(originSource, entity);
            float savedHealth = input.getFloatOr("cached_health", entity.getHealth());
            data.cachedHealth = Float.isFinite(savedHealth) ? Math.max(0.0F, savedHealth) : Math.max(0.0F, entity.getHealth());

            data.setCultivationSuppressed(
                    input.getBooleanOr("cultivation_suppressed", false)
            );



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
            attachment.source.writeOriginSourceData(output.child("source_data"));
            output.putFloat("cached_health", Math.max(0.0F, attachment.getEntity().getHealth()));
            output.putBoolean(
                    "cultivation_suppressed",
                    attachment.isCultivationSuppressed()
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