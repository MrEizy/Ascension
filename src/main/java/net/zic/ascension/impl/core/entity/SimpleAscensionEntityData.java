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
import net.zic.ascension.api.ascension.core.CoreAttachments;
import net.zic.ascension.api.ascension.core.entity.AscensionEntityData;
import net.zic.ascension.api.ascension.core.path.bonus.PathBonus;
import net.zic.ascension.api.ascension.core.path.bonus.PathBonusHolder;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.api.rpg_engine.source.OriginSourcePatch;
import net.zic.ascension.common.data_attachements.AscensionAttachments;
import net.zic.ascension.common.starter.StarterSelectionStage;
import net.zic.ascension.common.util.AscensionAttributes;
import net.zic.ascension.configuration.RealmEffectivenessConfiguration;
import net.zic.zenithlib.common.ZenithAttachments;
import net.zic.zenithlib.custom_attributes.ZenithAttribute;
import net.zic.zenithlib.custom_attributes.ZenithAttributeHolder;
import net.zic.zenithlib.network.ByteBufHelpers;
import net.zic.zenithlib.stats.*;
import net.zic.zenithlib.value_containers.ValueContainer;
import net.zic.zenithlib.value_containers.ValueContainerModifier;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class SimpleAscensionEntityData implements AscensionEntityData {

    private final StatSheet statSheet = new StatSheet();
    private final Set<Stat> dirtyStats = new HashSet<>();

    private final PathBonusHolder pathBonusHolder = new PathBonusHolder();
    private final PathBonusHolder cachedPathBonusHolder = new PathBonusHolder();


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
    public void startProcess(String process){
        if(this.process == null) this.process = process;
    }
    public boolean resolveProcess(String process){

        if(this.process == null) return false;
        if(!this.process.equals(process)) return false;
        this.process = null;
        resolve();
        return true;
    }

    protected void resolve(){
        if(!dirtyStats.isEmpty()) attachedEntity.getData(ZenithAttachments.STAT_HOLDER).updateStats(dirtyStats);
        dirtyStats.clear();
        //TODO add path bonus values here as well
    }

    public void initializeAttributes() {
        ZenithAttributeHolder attributeHolder = attachedEntity.getData(
                ZenithAttachments.ATTRIBUTE_HOLDER
        );

        applyAttributeStatScalings(attributeHolder, true);

        attributeHolder.addAttribute(Attributes.ARMOR);
        attributeHolder.addAttribute(Attributes.ARMOR_TOUGHNESS);
        attributeHolder.addAttribute(Attributes.LUCK);
        attributeHolder.addAttribute(AscensionAttributes.HEALTH_REGEN_RATE);
        attributeHolder.addAttribute(AscensionAttributes.STAMINA_REGEN_DELAY);
    }

    public void refreshRealmEffectiveness() {
        if (attachedEntity == null) {
            return;
        }
        applyAttributeStatScalings(attachedEntity.getData(ZenithAttachments.ATTRIBUTE_HOLDER), false);
    }

    private void applyAttributeStatScalings(ZenithAttributeHolder attributeHolder, boolean initializeAttributes) {
        // WIll BE BALANCED OVER TIME HOPEFULLY
        configureAttributeStatScaling(attributeHolder, Attributes.MAX_HEALTH,
                AscensionStats.VITALITY.get(), "base_scaling", 2.0D, 0.65D, false, initializeAttributes);

        configureAttributeStatScaling(attributeHolder, Attributes.ATTACK_DAMAGE,
                AscensionStats.STRENGTH.get(), "strength_damage_scaling", 1.0D, 1.0D, true, initializeAttributes);

        configureAttributeStatScaling(attributeHolder, Attributes.JUMP_STRENGTH,
                AscensionStats.STRENGTH.get(), "strength_jump_scaling", 0.005D, 0.2D, true, initializeAttributes);

        configureAttributeStatScaling(attributeHolder, Attributes.MOVEMENT_SPEED,
                AscensionStats.STRENGTH.get(), "strength_movement_scaling", 0.00005D, 0.15D, true, initializeAttributes);

        configureAttributeStatScaling(attributeHolder, Attributes.MOVEMENT_SPEED,
                AscensionStats.AGILITY.get(), "agility_movement_scaling", 0.002D, 0.15D, true, initializeAttributes);

        configureAttributeStatScaling(attributeHolder, Attributes.STEP_HEIGHT,
                AscensionStats.AGILITY.get(), "agility_step_height_scaling", 0.05D, 0.1D, true, initializeAttributes);

        configureAttributeStatScaling(attributeHolder, Attributes.ATTACK_SPEED,
                AscensionStats.STRENGTH.get(), "strength_attack_speed_scaling", 0.001D, 0.1D, true, initializeAttributes);

        configureAttributeStatScaling(attributeHolder, Attributes.ATTACK_SPEED,
                AscensionStats.AGILITY.get(), "agility_attack_speed_scaling", 0.001D, 0.1D, true, initializeAttributes);

        configureAttributeStatScaling(attributeHolder, Attributes.MINING_EFFICIENCY,
                AscensionStats.STRENGTH.get(), "strength_mining_scaling", 0.001D, 0.35D, true, initializeAttributes);

        configureAttributeStatScaling(attributeHolder, Attributes.SAFE_FALL_DISTANCE,
                AscensionStats.STRENGTH.get(), "strength_safe_fall_scaling", 0.1D, 0.35D, false, initializeAttributes);

        configureAttributeStatScaling(attributeHolder, AscensionAttributes.MAX_QI,
                AscensionStats.SPIRIT.get(), "spirit_max_qi_scaling", 10.0D, 0.75D, false, initializeAttributes);

        configureAttributeStatScaling(attributeHolder, AscensionAttributes.QI_REGEN_RATE,
                AscensionStats.SPIRIT.get(), "spirit_qi_regen_scaling", 0.25D, 0.45D, false, initializeAttributes);

        configureAttributeStatScaling(attributeHolder, AscensionAttributes.MAX_STAMINA,
                AscensionStats.VITALITY.get(), "vitality_max_stamina_scaling", 5.0D, 0.65D, false, initializeAttributes);

        configureAttributeStatScaling(attributeHolder, AscensionAttributes.MAX_STAMINA,
                AscensionStats.STRENGTH.get(), "strength_max_stamina_scaling", 2.0D, 0.65D, false, initializeAttributes);

        configureAttributeStatScaling(attributeHolder, AscensionAttributes.STAMINA_REGEN_RATE,
                AscensionStats.VITALITY.get(), "vitality_stamina_regen_scaling", 0.4D, 0.45D, false, initializeAttributes);

        configureAttributeStatScaling(attributeHolder, AscensionAttributes.STAMINA_REGEN_RATE,
                AscensionStats.AGILITY.get(), "agility_stamina_regen_scaling", 0.2D, 0.45D, false, initializeAttributes);
    }

    private void configureAttributeStatScaling(
            ZenithAttributeHolder holder,
            Holder<Attribute> attributeHolder,
            Stat stat,
            String scalingName,
            double baseScaling,
            double realmResponse,
            boolean suppressed,
            boolean initializeAttribute) {
        if (initializeAttribute) {
            if (suppressed) {
                holder.addSuppressedAttribute(attributeHolder);
            } else {
                holder.addAttribute(attributeHolder);
            }
        }

        ZenithAttribute zenithAttribute = holder.getAttribute(attributeHolder);
        if (zenithAttribute == null) {
            return;
        }

        Identifier scalingId = Identifier.fromNamespaceAndPath(
                AscensionCraft.MOD_ID,
                scalingName
        );
        double effectiveScaling = RealmEffectivenessConfiguration.apply(source, baseScaling, realmResponse);

        zenithAttribute.removeScaling(stat, scalingId);
        zenithAttribute.addStatScaling(stat, scalingId, effectiveScaling);
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
        ZenithStatHolder statHolder = attachedEntity.getData(ZenithAttachments.STAT_HOLDER);
        statHolder.startProcess("initialize_on_entity");
        attributeHolder.startProcess("initialize_on_entity");
        initializeAttributes();


        markDirty(getSource().load(),true);
        refreshRealmEffectiveness();
        initializePathBonuses();
        getSource().attachToEntity(getEntity());
        statHolder.resolveProcess("initialize_on_entity");
        attributeHolder.resolveProcess("initialize_on_entity");

        float restoredHealth = fullHealth ? attachedEntity.getMaxHealth() : Math.min(cachedHealth, attachedEntity.getMaxHealth());
        attachedEntity.setHealth(Math.max(0.0F, restoredHealth));
        cachedHealth = attachedEntity.getHealth();
    }


    //──Path Bonus────────────────────────────────────────────────────────
    //TODO trigger path bonus update
    @Override
    public ValueContainer getPathBonusContainer(Identifier category, Identifier path) {
        return cachedPathBonusHolder.getPathBonusContainer(category,path);
    }

    @Override
    public double getPathBonus(Identifier category, Identifier path) {
        return cachedPathBonusHolder.getBonus(category,path);
    }

    @Override
    public Collection<PathBonus> getAllPathBonuses() {
        return cachedPathBonusHolder.getAllPathBonuses();
    }

    @Override
    public Collection<Identifier> getAllPathBonusesInCategory(Identifier category) {
        return cachedPathBonusHolder.getAllPathBonusesInCategory(category);
    }

    @Override
    public void addBonus(Identifier category, Identifier path, double val) {
        pathBonusHolder.addBonus(category,path,val);
        updatePathBonus(category,path);
    }

    @Override
    public void addBonusModifier(Identifier category, Identifier path, ValueContainerModifier modifier) {
        pathBonusHolder.addBonusModifier(category,path,modifier);
        updatePathBonus(category,path);
    }

    @Override
    public void removeBonus(Identifier category, Identifier path, double val) {
        pathBonusHolder.removeBonus(category,path,val);
        updatePathBonus(category,path);
    }

    @Override
    public void removeBonusModifier(Identifier category, Identifier path, Identifier modifier) {
        pathBonusHolder.removeBonusModifier(category,path,modifier);
        updatePathBonus(category,path);
    }

    @Override
    public void updatePathBonus(Identifier category, Identifier path) {
        ValueContainer local = pathBonusHolder.getPathBonusContainer(category,path);
        ValueContainer sourceContainer = AscensionOriginSourceHelper.getPathBonusContainer(source,category,path);
        double baseValue = (local == null ? 0 : local.getBaseValue())+(sourceContainer == null ? 0: sourceContainer.getBaseValue());

        ValueContainer newContainer = new ValueContainer(path,baseValue);
        if(local != null) for(ValueContainerModifier modifier : local.getAllModifiers()) newContainer.addModifierNoCacheUpdate(modifier);
        if(sourceContainer != null) for(ValueContainerModifier modifier : sourceContainer.getAllModifiers()) newContainer.addModifierNoCacheUpdate(modifier);

        if(attachedEntity == null) return;
        cachedPathBonusHolder.setPathBonusContainer(category,path,newContainer);
        getEntity().getData(CoreAttachments.PATH_BONUS_HOLDER).updatePathBonus(new PathBonus(category,path));
        getEntity().syncData(CoreAttachments.PATH_BONUS_HOLDER);
    }

    @Override
    public void updatePathBonuses(Collection<PathBonus> bonuses) {
        //TODO
    }

    public void initializePathBonuses(){
        Collection<PathBonus> bonuses = AscensionOriginSourceHelper.getAllPathBonuses(source);
        Collection<PathBonus> selfBonuses = pathBonusHolder.getAllPathBonuses();

        for(PathBonus bonus : bonuses) updatePathBonus(bonus.category(),bonus.path());
        for(PathBonus bonus : selfBonuses) updatePathBonus(bonus.category(),bonus.path());
    }

    //──Stats────────────────────────────────────────────────────────
    //TODO trigger stat update

    @Override
    public Collection<Stat> getStats() {
        return statSheet.getAllStats();
    }

    @Override
    public StatInstance getStatInstance(Stat stat) {
        return statSheet.getStatInstance(stat);
    }

    @Override
    public double getStat(Stat stat) {
        return statSheet.getStatInstance(stat) == null ? 0 : statSheet.getStatInstance(stat).getValue();
    }

    @Override
    public double getBaseStat(Stat stat) {
        return statSheet.getStatInstance(stat) == null ? 0 : statSheet.getStatInstance(stat).getBaseValue();
    }

    @Override
    public void addStat(Stat stat, double val) {
        statSheet.addStat(stat,val);
        updateStatHolder(stat);
    }

    @Override
    public void removeStat(Stat stat, double val) {
        statSheet.removeStat(stat,val);
        updateStatHolder(stat);
    }

    @Override
    public void addStatModifier(Stat stat, ValueContainerModifier modifier) {
        if(statSheet.getStatInstance(stat) == null) return;
        statSheet.getStatInstance(stat).addModifier(modifier);
        updateStatHolder(stat);
    }

    @Override
    public void removeStatModifier(Stat stat, Identifier modifier) {
        if(statSheet.getStatInstance(stat) == null) return;
        statSheet.getStatInstance(stat).removeModifier(modifier);
        updateStatHolder(stat);
    }





    public void updateStatHolder(Stat stat){
        if(getEntity() == null) return;
        String processId = "singel_stat_update"+random.nextLong();
        startProcess(processId);
        dirtyStats.add(stat);
        resolveProcess(process);
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