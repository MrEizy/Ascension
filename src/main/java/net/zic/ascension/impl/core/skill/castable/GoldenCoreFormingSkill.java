package net.zic.ascension.impl.core.skill.castable;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.path.Path;
import net.zic.ascension.api.ascension.core.path.PathInstance;
import net.zic.ascension.api.ascension.core.path.realm.Realm;
import net.zic.ascension.api.ascension.core.skill.SkillData;
import net.zic.ascension.api.ascension.core.skill.SkillInstance;
import net.zic.ascension.api.ascension.core.skill.castable.CastData;
import net.zic.ascension.api.ascension.core.skill.castable.CastableSkill;
import net.zic.ascension.api.ascension.core.skill.castable.CastingSkillInstance;
import net.zic.ascension.api.ascension.core.skill.castable.EquipSkillData;
import net.zic.ascension.api.ascension.core.skill.castable.data.CastResult;
import net.zic.ascension.api.ascension.core.skill.castable.data.CastStatus;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.ascension.core.tribulation.TribulationInstance;
import net.zic.ascension.api.ascension.datapack.skill.SkillType;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.api.rpg_engine.source.data_source.util.SerializerHandler;
import net.zic.ascension.api.rpg_engine.source.data_source.util.SyncHandler;
import net.zic.ascension.impl.core.tribulation.GoldenCoreTribulation;
import net.zic.ascension.impl.core.tribulation.GoldenCoreTribulationData;
import net.zic.ascension.impl.datapack.skill.AscensionSkillTypes;
import org.jspecify.annotations.Nullable;

/**
 * triggers the manual golden core tribulation of a path, setting the golden core it forms.
 * the skill grants nothing itself, the tribulation adds the core once it is completed.
 * <p>
 * can only be cast while the path is at exactly the configured realm, and that realm's golden core tribulation is ready
 */
public final class GoldenCoreFormingSkill implements CastableSkill<GoldenCoreFormingSkill.Data, GoldenCoreFormingSkill.Cast, EquipSkillData.Empty> {
    private static final SerializerHandler<Data> SERIALIZER_HANDLER = new DataSerializerHandler();
    private static final SyncHandler<Data> SYNC_HANDLER = new SyncHandler.UnitSyncHandler<>();
    private static final SyncHandler<Cast> CAST_SYNC_HANDLER = new SyncHandler.UnitSyncHandler<>();

    private final Component name;
    private final Component description;
    private final Identifier goldenCore;
    private final Identifier path;
    private final Realm realm;

    public GoldenCoreFormingSkill(Component name, Component description, Identifier goldenCore, Identifier path, Realm realm) {
        this.name = name;
        this.description = description;
        this.goldenCore = goldenCore;
        this.path = path;
        this.realm = realm;
    }

    public Identifier goldenCore() {return goldenCore;}
    public Identifier path() {return path;}
    public Realm realm() {return realm;}

    //the tribulation of the configured realm, the one this skill expects to trigger
    public @Nullable Identifier expectedTribulation(RegistryAccess access) {
        Path<?> definition = CoreRegistries.safeAccess(CoreRegistries.PATH_REGISTRY, path, access);
        if (definition == null || realm.majorRealm() < 0 || realm.majorRealm() >= definition.getMaxMajorRealm()) return null;
        return definition.getTribulationId(realm.majorRealm(), realm.minorRealm());
    }

    //──Cast────────────────────────────────────────────────────────

    @Override
    public CastResult tryCast(CastingSkillInstance<Data, Cast, EquipSkillData.Empty> instance) {
        LivingEntity caster = instance.getCaster();
        RegistryAccess access = caster.level().registryAccess();
        OriginSource source = AscensionOriginSourceHelper.getEntitySource(caster);
        PathInstance<?> pathInstance = source == null ? null : AscensionOriginSourceHelper.getPathInstance(source, path);
        if (pathInstance == null) {
            return CastResult.fail(Component.translatable("ascension.skill.golden_core_forming.fail.no_path"));
        }
        if (pathInstance.getCurrentMajorRealm() < 0 || !pathInstance.getCurrentRealm().equals(realm)) {
            return CastResult.fail(Component.translatable("ascension.skill.golden_core_forming.fail.wrong_realm"));
        }
        Identifier tribulation = expectedTribulation(access);
        if (tribulation == null
                || !(CoreRegistries.safeAccess(CoreRegistries.TRIBULATION_REGISTRY, tribulation, access) instanceof GoldenCoreTribulation)) {
            return CastResult.fail(Component.translatable("ascension.skill.golden_core_forming.fail.no_tribulation"));
        }
        if (!AscensionOriginSourceHelper.canTriggerPathTribulation(source, path, tribulation)) {
            return CastResult.fail(Component.translatable("ascension.skill.golden_core_forming.fail.not_ready"));
        }
        return CastResult.success();
    }

    @Override
    public void startCast(CastingSkillInstance<Data, Cast, EquipSkillData.Empty> instance) {
        LivingEntity caster = instance.getCaster();
        if (!caster.level().isClientSide()) {
            OriginSource source = AscensionOriginSourceHelper.getEntitySource(caster);
            Identifier tribulation = expectedTribulation(caster.level().registryAccess());
            if (source != null && tribulation != null) {
                AscensionOriginSourceHelper.triggerPathTribulation(source, path, tribulation, caster, this::setGoldenCore);
            }
        }
        instance.end(CastStatus.Reason.COMPLETED);
    }

    //tells the tribulation which golden core it forms
    private void setGoldenCore(TribulationInstance<?> tribulation) {
        TribulationInstance<GoldenCoreTribulationData> typed = tribulation.as(GoldenCoreTribulation.class);
        if (typed != null) typed.getData().setGoldenCore(goldenCore, path, realm);
    }

    @Override
    public void tickCast(CastingSkillInstance<Data, Cast, EquipSkillData.Empty> instance) {
    }

    @Override
    public void endCast(CastingSkillInstance<Data, Cast, EquipSkillData.Empty> instance) {
    }

    @Override
    public Cast newCastData(RegistryAccess access) {
        return new Cast();
    }

    @Override
    public SyncHandler<Cast> castSyncHandler() {
        return CAST_SYNC_HANDLER;
    }

    //──Equip────────────────────────────────────────────────────────

    @Override
    public EquipSkillData.Empty newEquipData(RegistryAccess access) {
        return EquipSkillData.Empty.INSTANCE;
    }

    @Override
    public SerializerHandler<EquipSkillData.Empty> equipSerializerHandler() {
        return EquipSkillData.Empty.SERIALIZER_HANDLER;
    }

    @Override
    public SyncHandler<EquipSkillData.Empty> equipSyncHandler() {
        return EquipSkillData.Empty.SYNC_HANDLER;
    }

    //──Skill────────────────────────────────────────────────────────

    @Override
    public SkillType getType() {
        return AscensionSkillTypes.GOLDEN_CORE_FORMING_SKILL_TYPE.get();
    }

    @Override
    public Component getName() {
        return name;
    }

    @Override
    public Component getDescription() {
        return description;
    }

    @Override
    public void onAdded(OriginSource source, SkillInstance<Data> instance) {
    }

    @Override
    public void onRemoved(OriginSource source, SkillInstance<Data> instance) {
    }

    @Override
    public void applyToEntity(LivingEntity entity, SkillInstance<Data> instance) {
    }

    @Override
    public void removeFromEntity(LivingEntity entity, SkillInstance<Data> instance) {
    }

    @Override
    public Data newData(RegistryAccess access) {
        return new Data();
    }

    @Override
    public SerializerHandler<Data> serializerHandler() {
        return SERIALIZER_HANDLER;
    }

    @Override
    public SyncHandler<Data> syncHandler() {
        return SYNC_HANDLER;
    }

    //the skill holds no state
    public static final class Data implements SkillData {
    }

    //the cast is instant and holds no state
    public static final class Cast implements CastData {
    }

    private static final class DataSerializerHandler implements SerializerHandler<Data> {
        @Override
        public Data read(ValueInput input, RegistryAccess access) {
            return new Data();
        }

        @Override
        public void write(Data writable, ValueOutput output, RegistryAccess access) {
        }
    }
}
