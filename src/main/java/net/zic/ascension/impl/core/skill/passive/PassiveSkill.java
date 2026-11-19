package net.zic.ascension.impl.core.skill.passive;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.resource.ResourceOperation;
import net.zic.ascension.api.ascension.core.requirement.RequirementHolder;
import net.zic.ascension.api.ascension.core.skill.ProgressingSkill;
import net.zic.ascension.api.ascension.core.skill.ProgressingSkillData;
import net.zic.ascension.api.ascension.core.skill.SkillInstance;
import net.zic.ascension.api.ascension.core.skill.SkillDefinitions;
import net.zic.ascension.api.ascension.core.skill.SkillProgressionData;
import net.zic.ascension.api.ascension.core.skill.castable.SkillCondition;
import net.zic.ascension.api.ascension.core.skill.castable.action.SkillAction;
import net.zic.ascension.api.ascension.core.skill.castable.action.SkillActionContext;
import net.zic.ascension.api.ascension.core.skill.passive.PassiveModifier;
import net.zic.ascension.api.ascension.core.skill.passive.PassiveTrigger;
import net.zic.ascension.api.ascension.core.skill.toggleable.ToggleableSkill;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.ascension.datapack.skill.SkillType;
import net.zic.ascension.api.rpg_engine.source.data_source.util.SerializerHandler;
import net.zic.ascension.api.rpg_engine.source.data_source.util.SyncHandler;
import net.zic.ascension.api.ascension.value.ScaledValue;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.impl.datapack.skill.AscensionSkillTypes;
import net.zic.ascension.impl.core.skill.castable.SkillActionRuntime;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PassiveSkill implements ProgressingSkill<PassiveSkill.Data>, SkillDefinitions.Owner {
    private static final SyncHandler<Data> SYNC_HANDLER = new DataSyncHandler();

    private final Component name;
    private final Component description;
    private final ProgressionSettings progressionSettings;
    private final int levels;
    private final int baseLevelCap;
    private final List<Double> levelExperienceRequirements;
    private final List<PassiveModifier> modifiers;
    private final List<PassiveTrigger> triggers;
    private final SkillDefinitions definitions;
    private final boolean enabledByDefault;
    private final Optional<Upkeep> upkeep;
    private final Optional<Identifier> stateGroup;
    private final List<Identifier> grantedSkills;
    private final List<SkillCondition> enableConditions;
    private final List<SkillAction> enableActions;
    private final List<SkillAction> disableActions;
    private final SerializerHandler<Data> serializerHandler;

    protected PassiveSkill(
            Component name,
            Component description,
            ProgressionSettings progressionSettings,
            List<PassiveModifier> modifiers,
            List<PassiveTrigger> triggers,
            SkillDefinitions definitions,
            boolean enabledByDefault,
            Optional<Upkeep> upkeep,
            Optional<Identifier> stateGroup,
            List<Identifier> grantedSkills,
            List<SkillCondition> enableConditions,
            List<SkillAction> enableActions,
            List<SkillAction> disableActions
    ) {
        this.name = name;
        this.description = description;
        this.progressionSettings = progressionSettings == null ? ProgressionSettings.DEFAULT : progressionSettings;
        this.levels = this.progressionSettings.levels();
        this.baseLevelCap = Math.clamp(this.progressionSettings.levelCap(), 1, this.levels);
        this.levelExperienceRequirements = this.progressionSettings.levelExperience();
        this.modifiers = modifiers == null ? List.of() : List.copyOf(modifiers);
        this.triggers = triggers == null ? List.of() : List.copyOf(triggers);
        this.definitions = definitions == null ? SkillDefinitions.EMPTY : definitions;
        this.enabledByDefault = enabledByDefault;
        this.upkeep = upkeep == null ? Optional.empty() : upkeep;
        this.stateGroup = stateGroup == null ? Optional.empty() : stateGroup;
        this.grantedSkills = grantedSkills == null ? List.of() : List.copyOf(grantedSkills);
        this.enableConditions = enableConditions == null ? List.of() : List.copyOf(enableConditions);
        this.enableActions = enableActions == null ? List.of() : List.copyOf(enableActions);
        this.disableActions = disableActions == null ? List.of() : List.copyOf(disableActions);
        this.serializerHandler = new DataSerializerHandler(this.enabledByDefault);
    }

    public static PassiveSkill create(
            Component name,
            Component description,
            ProgressionSettings progressionSettings,
            List<PassiveModifier> modifiers,
            List<PassiveTrigger> triggers,
            SkillDefinitions definitions,
            boolean toggleable,
            boolean enabledByDefault,
            Optional<Upkeep> upkeep,
            Optional<Identifier> stateGroup,
            List<Identifier> grantedSkills,
            List<SkillCondition> enableConditions,
            List<SkillAction> enableActions,
            List<SkillAction> disableActions
    ) {
        return toggleable
                ? new ToggleablePassiveSkill(name, description, progressionSettings, modifiers, triggers, definitions, enabledByDefault, upkeep, stateGroup, grantedSkills, enableConditions, enableActions, disableActions)
                : new PassiveSkill(name, description, progressionSettings, modifiers, triggers, definitions, true, Optional.empty(), Optional.empty(), List.of(), List.of(), List.of(), List.of());
    }

    public ProgressionSettings progressionSettings() {
        return progressionSettings;
    }

    public Training training() {
        return progressionSettings.training();
    }

    public int getConfiguredLevels() {
        return levels;
    }

    public int getConfiguredBaseLevelCap() {
        return baseLevelCap;
    }

    public List<Double> getLevelExperienceRequirements() {
        return levelExperienceRequirements;
    }

    public List<PassiveModifier> modifiers() {
        return modifiers;
    }

    public List<PassiveTrigger> triggers() {
        return triggers;
    }

    @Override
    public SkillDefinitions definitions() {
        return definitions;
    }

    public boolean isToggleable() {
        return this instanceof ToggleableSkill<?>;
    }

    public boolean isEnabledByDefault() {
        return enabledByDefault;
    }

    public Optional<Upkeep> getUpkeep() {
        return upkeep;
    }

    public Optional<Identifier> stateGroup() {
        return stateGroup;
    }

    public List<Identifier> grantedSkills() {
        return grantedSkills;
    }

    public List<SkillCondition> enableConditions() {
        return enableConditions;
    }

    public List<SkillAction> enableActions() {
        return enableActions;
    }

    public List<SkillAction> disableActions() {
        return disableActions;
    }

    public boolean isActive(Data data) {
        return !(this instanceof ToggleableSkill<?>) || data != null && data.isEnabled();
    }

    @Override
    public int getMaximumProgression() {
        return levels;
    }

    @Override
    public int getInitialProgression() {
        return 1;
    }

    @Override
    public int getDefaultProgressionCap() {
        return baseLevelCap;
    }

    @Override
    public double getExperienceRequiredForNextProgression(int currentLevel) {
        int index = currentLevel - 1;
        return index < 0 || index >= levelExperienceRequirements.size()
                ? Double.POSITIVE_INFINITY
                : levelExperienceRequirements.get(index);
    }

    @Override
    public void onProgressionChanged(OriginSource source, SkillInstance<Data> instance, int previousProgression, int currentProgression) {
        if (!isActive(instance.getData()) || previousProgression == currentProgression) {
            return;
        }
        Identifier skillId = instance.getSkillId();
        modifiers.forEach(modifier -> modifier.remove(source, skillId));
        modifiers.forEach(modifier -> modifier.apply(source, skillId));
    }

    @Override
    public SkillType getType() {
        return AscensionSkillTypes.PASSIVE_SKILL_TYPE.get();
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
        if (isActive(instance.getData())) {
            if (this instanceof ToggleableSkill<?>) {
                activateState(source, instance.getSkillId());
            } else {
                applyModifiers(source, instance.getSkillId());
            }
        }
    }

    @Override
    public void onRemoved(OriginSource source, SkillInstance<Data> instance) {
        if (isActive(instance.getData())) {
            if (this instanceof ToggleableSkill<?>) {
                deactivateState(source, instance.getSkillId());
            } else {
                removeModifiers(source, instance.getSkillId());
            }
        }
    }

    @Override
    public void applyToEntity(LivingEntity entity, SkillInstance<Data> instance) {
        if (!isActive(instance.getData())) {
            return;
        }
        Identifier skillId = instance.getSkillId();
        modifiers.forEach(modifier -> modifier.applyToEntity(entity, skillId));
        if (this instanceof ToggleableSkill<?> && entity.level() instanceof ServerLevel level) {
            SkillActionContext context = new SkillActionContext(level, entity, skillId, null, null, 0.0D, Map.of());
            SkillActionRuntime.restorePersistentVisuals(context, enableActions);
        }
    }

    @Override
    public void removeFromEntity(LivingEntity entity, SkillInstance<Data> instance) {
        if (!isActive(instance.getData())) {
            return;
        }
        Identifier skillId = instance.getSkillId();
        modifiers.forEach(modifier -> modifier.removeFromEntity(entity, skillId));
        if (this instanceof ToggleableSkill<?> && entity.level() instanceof ServerLevel level) {
            SkillActionContext context = new SkillActionContext(level, entity, skillId, null, null, 0.0D, Map.of());
            SkillActionRuntime.clearPersistentVisuals(context, enableActions);
        }
    }

    @Override
    public Data newData(RegistryAccess access) {
        return new Data(new SkillProgressionData(), enabledByDefault);
    }

    @Override
    public SerializerHandler<Data> serializerHandler() {
        return serializerHandler;
    }

    @Override
    public SyncHandler<Data> syncHandler() {
        return SYNC_HANDLER;
    }

    protected void applyModifiers(OriginSource source, Identifier skillId) {
        modifiers.forEach(modifier -> modifier.apply(source, skillId));
    }

    protected void removeModifiers(OriginSource source, Identifier skillId) {
        modifiers.forEach(modifier -> modifier.remove(source, skillId));
    }

    protected void activateState(OriginSource source, Identifier skillId) {
        stateGroup.ifPresent(group -> PassiveSkillService.disableStateGroup(source, group, skillId));
        applyModifiers(source, skillId);
        grantedSkills.forEach(skill -> AscensionOriginSourceHelper.addSkill(source, skill, skillId));
        runStateActions(source, skillId, enableActions);
    }

    protected void deactivateState(OriginSource source, Identifier skillId) {
        runStateActions(source, skillId, disableActions);
        grantedSkills.forEach(skill -> AscensionOriginSourceHelper.removeSkill(source, skill, skillId));
        removeModifiers(source, skillId);
    }

    private void runStateActions(OriginSource source, Identifier skillId, List<SkillAction> actions) {
        if (actions.isEmpty()) {
            return;
        }
        for (LivingEntity entity : source.getAttachedEntities()) {
            if (!(entity.level() instanceof ServerLevel level)) {
                continue;
            }
            SkillActionContext context = new SkillActionContext(level, entity, skillId, null, null, 0.0D, Map.of());
            SkillActionRuntime.execute(context, actions);
        }
    }

    public record ProgressionSettings(int levels, int levelCap, List<Double> levelExperience, Training training) {
        public static final ProgressionSettings DEFAULT = new ProgressionSettings(1, 1, List.of(), Training.NONE);
        public static final com.mojang.serialization.MapCodec<ProgressionSettings> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.intRange(1, 1024).optionalFieldOf("levels", 1).forGetter(ProgressionSettings::levels),
                Codec.INT.optionalFieldOf("level_cap", 1).forGetter(ProgressionSettings::levelCap),
                Codec.DOUBLE.listOf().optionalFieldOf("level_xp", List.of()).forGetter(ProgressionSettings::levelExperience),
                Training.CODEC.optionalFieldOf("training", Training.NONE).forGetter(ProgressionSettings::training)
        ).apply(instance, ProgressionSettings::new));

        public ProgressionSettings {
            levels = Math.max(1, levels);
            levelCap = Math.clamp(levelCap, 1, levels);
            levelExperience = levelExperience == null ? List.of() : levelExperience.stream()
                    .map(value -> Double.isFinite(value) ? Math.max(0.0D, value) : 0.0D).toList();
            training = training == null ? Training.NONE : training;
        }
    }

    public record Training(int paidUptimeTicks, double experience, List<Milestone> milestones) {
        public static final Training NONE = new Training(0, 0.0D, List.of());
        public static final Codec<Training> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.intRange(0, 72000).optionalFieldOf("paid_uptime_ticks", 0).forGetter(Training::paidUptimeTicks),
                Codec.doubleRange(0.0D, 100000.0D).optionalFieldOf("experience", 0.0D).forGetter(Training::experience),
                Milestone.CODEC.listOf().optionalFieldOf("milestones", List.of()).forGetter(Training::milestones)
        ).apply(instance, Training::new));

        public Training {
            paidUptimeTicks = Math.max(0, paidUptimeTicks);
            experience = Double.isFinite(experience) ? Math.max(0.0D, experience) : 0.0D;
            milestones = milestones == null ? List.of() : List.copyOf(milestones);
        }

        public boolean hasPaidTraining() {
            return paidUptimeTicks > 0 && experience > 0.0D;
        }
    }

    public record Milestone(int level, RequirementHolder requirements) {
        public static final Codec<Milestone> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.intRange(2, 1024).fieldOf("level").forGetter(Milestone::level),
                RequirementHolder.CODEC.fieldOf("requirements").forGetter(Milestone::requirements)
        ).apply(instance, Milestone::new));

        public Milestone {
            requirements = requirements == null ? RequirementHolder.EMPTY : requirements;
        }
    }

    public record Upkeep(
            Identifier resource,
            ResourceOperation operation,
            Identifier source,
            ScaledValue amount,
            int interval
    ) {
        public static final Codec<Upkeep> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Identifier.CODEC.fieldOf("resource").forGetter(Upkeep::resource),
                ResourceOperation.CODEC.optionalFieldOf("operation", ResourceOperation.CONSUME).forGetter(Upkeep::operation),
                Identifier.CODEC.optionalFieldOf("source", AscensionCraft.prefix("skill_casting")).forGetter(Upkeep::source),
                ScaledValue.COMPACT_CODEC.fieldOf("amount").forGetter(Upkeep::amount),
                Codec.intRange(1, 1200).optionalFieldOf("interval", 20).forGetter(Upkeep::interval)
        ).apply(instance, Upkeep::new));
    }


    public static final class Data implements ProgressingSkillData {
        private final SkillProgressionData progression;
        private long lastProgressionExperienceTick = Long.MIN_VALUE / 2L;
        private int paidUptimeTicks;
        private boolean enabled;

        public Data(SkillProgressionData progression, boolean enabled) {
            this.progression = progression == null ? new SkillProgressionData() : progression;
            this.enabled = enabled;
        }

        @Override
        public SkillProgressionData getSkillProgression() {
            return progression;
        }

        public boolean canAwardExperienceAt(long gameTime) {
            return gameTime < lastProgressionExperienceTick
                    || gameTime - lastProgressionExperienceTick >= 20L;
        }

        public void markExperienceAwardedAt(long gameTime) {
            lastProgressionExperienceTick = gameTime;
        }

        public int addPaidUptimeTicks(int ticks, int target) {
            paidUptimeTicks = (int) Math.min(Math.max(0, target),
                    (long) Math.max(0, paidUptimeTicks) + Math.max(0, ticks));
            return paidUptimeTicks;
        }

        public void resetPaidUptimeTicks() {
            paidUptimeTicks = 0;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    //the default enabled state depends on the skill, so each skill has its own serializer
    private record DataSerializerHandler(boolean defaultEnabled) implements SerializerHandler<Data> {
        @Override
        public Data read(ValueInput input, RegistryAccess access) {
            Data data = new Data(new SkillProgressionData(input.childOrEmpty("progression")), input.getBooleanOr("enabled", defaultEnabled));
            data.paidUptimeTicks = Math.max(0, input.getIntOr("paid_uptime_ticks", 0));
            return data;
        }

        @Override
        public void write(Data data, ValueOutput output, RegistryAccess access) {
            data.progression.write(output.child("progression"));
            output.putBoolean("enabled", data.enabled);
            output.putInt("paid_uptime_ticks", data.paidUptimeTicks);
        }
    }

    private static final class DataSyncHandler implements SyncHandler<Data> {
        @Override
        public void decode(Data data, ByteBuf buf, RegistryAccess access) {
            data.progression.decode(buf);
            data.enabled = buf.readBoolean();
        }

        @Override
        public void encode(Data data, ByteBuf buf, RegistryAccess access) {
            data.progression.encode(buf);
            buf.writeBoolean(data.enabled);
        }
    }
}
