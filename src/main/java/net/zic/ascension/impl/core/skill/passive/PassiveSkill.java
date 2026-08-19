package net.zic.ascension.impl.core.skill.passive;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.resource.ResourceOperation;
import net.zic.ascension.api.ascension.core.resource.ResourceSourceIdentity;
import net.zic.ascension.api.ascension.core.resource.ResourceTransactionRequest;
import net.zic.ascension.api.ascension.core.resource.ResourceTransactionService;
import net.zic.ascension.api.ascension.core.skill.ProgressingSkill;
import net.zic.ascension.api.ascension.core.skill.ProgressingSkillData;
import net.zic.ascension.api.ascension.core.skill.SkillData;
import net.zic.ascension.api.ascension.core.skill.SkillDefinitions;
import net.zic.ascension.api.ascension.core.skill.SkillProgressionData;
import net.zic.ascension.api.ascension.core.skill.passive.PassiveModifier;
import net.zic.ascension.api.ascension.core.skill.passive.PassiveTrigger;
import net.zic.ascension.api.ascension.core.skill.toggleable.ToggleableSkill;
import net.zic.ascension.api.ascension.datapack.skill.SkillType;
import net.zic.ascension.api.ascension.value.ScaledValue;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.impl.datapack.skill.AscensionSkillTypes;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class PassiveSkill implements ProgressingSkill, SkillDefinitions.Owner {
    private final Component name;
    private final Component description;
    private final int levels;
    private final int defaultAccessibleLevel;
    private final List<Double> experienceRequirements;
    private final List<PassiveModifier> modifiers;
    private final List<PassiveTrigger> triggers;
    private final SkillDefinitions definitions;
    private final boolean enabledByDefault;
    private final Optional<Upkeep> upkeep;

    protected PassiveSkill(
            Component name,
            Component description,
            int levels,
            int defaultAccessibleLevel,
            List<Double> experienceRequirements,
            List<PassiveModifier> modifiers,
            List<PassiveTrigger> triggers,
            SkillDefinitions definitions,
            boolean enabledByDefault,
            Optional<Upkeep> upkeep
    ) {
        this.name = name;
        this.description = description;
        this.levels = Math.max(1, levels);
        this.defaultAccessibleLevel = Math.clamp(defaultAccessibleLevel <= 0 ? 1 : defaultAccessibleLevel, 1, this.levels);
        this.experienceRequirements = experienceRequirements == null
                ? List.of()
                : experienceRequirements.stream().map(value -> Math.max(0.0D, value)).toList();
        this.modifiers = modifiers == null ? List.of() : List.copyOf(modifiers);
        this.triggers = triggers == null ? List.of() : List.copyOf(triggers);
        this.definitions = definitions == null ? SkillDefinitions.EMPTY : definitions;
        this.enabledByDefault = enabledByDefault;
        this.upkeep = upkeep == null ? Optional.empty() : upkeep;
    }

    public static PassiveSkill create(
            Component name,
            Component description,
            int levels,
            int defaultAccessibleLevel,
            List<Double> experienceRequirements,
            List<PassiveModifier> modifiers,
            List<PassiveTrigger> triggers,
            SkillDefinitions definitions,
            boolean toggleable,
            boolean enabledByDefault,
            Optional<Upkeep> upkeep
    ) {
        return toggleable
                ? new Toggleable(name, description, levels, defaultAccessibleLevel, experienceRequirements, modifiers, triggers, definitions, enabledByDefault, upkeep)
                : new PassiveSkill(name, description, levels, defaultAccessibleLevel, experienceRequirements, modifiers, triggers, definitions, true, Optional.empty());
    }

    public int getConfiguredLevels() {
        return levels;
    }

    public int getConfiguredDefaultAccessibleLevel() {
        return defaultAccessibleLevel;
    }

    public List<Double> getExperienceRequirements() {
        return experienceRequirements;
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
        return this instanceof Toggleable;
    }

    public boolean isEnabledByDefault() {
        return enabledByDefault;
    }

    public Optional<Upkeep> getUpkeep() {
        return upkeep;
    }

    public boolean isActive(SkillData data) {
        return !(this instanceof Toggleable) || data instanceof Data passiveData && passiveData.isEnabled();
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
        return defaultAccessibleLevel;
    }

    @Override
    public double getExperienceRequiredForNextProgression(int currentLevel) {
        int index = currentLevel - 1;
        return index < 0 || index >= experienceRequirements.size()
                ? Double.POSITIVE_INFINITY
                : experienceRequirements.get(index);
    }

    @Override
    public void onProgressionChanged(OriginSource source, ProgressingSkillData data, int previousProgression, int currentProgression) {
        if (!isActive(data) || previousProgression == currentProgression) {
            return;
        }
        Identifier skillId = skillId(source);
        if (skillId == null) {
            return;
        }
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
    public void onAdded(OriginSource source, SkillData data) {
        if (isActive(data)) {
            applyModifiers(source);
        }
    }

    @Override
    public void onRemoved(OriginSource source, SkillData data) {
        if (isActive(data)) {
            removeModifiers(source);
        }
    }

    @Override
    public void applyToEntity(LivingEntity entity, SkillData data) {
    }

    @Override
    public void removeFromEntity(LivingEntity entity, SkillData data) {
    }

    @Override
    public SkillData newData(RegistryAccess access) {
        return new Data(new SkillProgressionData(), enabledByDefault);
    }

    @Override
    public SkillData loadData(ValueInput input, RegistryAccess access) {
        return new Data(input, enabledByDefault);
    }

    @Override
    public SkillData loadData(ByteBuf buf) {
        return new Data(buf);
    }

    protected void applyModifiers(OriginSource source) {
        Identifier skillId = skillId(source);
        if (skillId != null) {
            modifiers.forEach(modifier -> modifier.apply(source, skillId));
        }
    }

    protected void removeModifiers(OriginSource source) {
        Identifier skillId = skillId(source);
        if (skillId != null) {
            modifiers.forEach(modifier -> modifier.remove(source, skillId));
        }
    }

    private Identifier skillId(OriginSource source) {
        return CoreRegistries.SKILL_REGISTRY.get(source.getRegistryAccess()).getKey(this);
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

    private static final class Toggleable extends PassiveSkill implements ToggleableSkill {
        private Toggleable(
                Component name,
                Component description,
                int levels,
                int defaultAccessibleLevel,
                List<Double> experienceRequirements,
                List<PassiveModifier> modifiers,
                List<PassiveTrigger> triggers,
                SkillDefinitions definitions,
                boolean enabledByDefault,
                Optional<Upkeep> upkeep
        ) {
            super(name, description, levels, defaultAccessibleLevel, experienceRequirements, modifiers, triggers, definitions, enabledByDefault, upkeep);
        }

        @Override
        public boolean isEnabled(SkillData data) {
            return data instanceof Data passiveData && passiveData.isEnabled();
        }

        @Override
        public void setEnabled(SkillData data, boolean enabled) {
            if (data instanceof Data passiveData) {
                passiveData.setEnabled(enabled);
            }
        }

        @Override
        public boolean canEnable(LivingEntity entity, OriginSource source, SkillData data) {
            return upkeepAvailable(entity, source, true);
        }

        @Override
        public void onEnabled(OriginSource source, SkillData data) {
            applyModifiers(source);
        }

        @Override
        public void onDisabled(OriginSource source, SkillData data) {
            removeModifiers(source);
        }

        @Override
        public boolean tickEnabled(LivingEntity entity, OriginSource source, SkillData data) {
            return getUpkeep().isEmpty()
                    || entity.level().getGameTime() % getUpkeep().get().interval() != 0L
                    || upkeepAvailable(entity, source, false);
        }

        private boolean upkeepAvailable(LivingEntity entity, OriginSource source, boolean simulate) {
            if (getUpkeep().isEmpty()) {
                return true;
            }
            Identifier skillId = CoreRegistries.SKILL_REGISTRY.get(source.getRegistryAccess()).getKey(this);
            Upkeep upkeep = getUpkeep().get();
            ScaledValue.Context valueContext = new ScaledValue.Context(source, skillId, entity, null, 0.0D, Map.of());
            ResourceTransactionRequest request = ResourceTransactionRequest.of(
                    entity,
                    upkeep.resource(),
                    upkeep.operation(),
                    Math.max(0.0D, upkeep.amount().resolve(valueContext)),
                    new ResourceSourceIdentity.Simple(upkeep.source(), Set.of(ResourceSourceIdentity.Tags.SKILL))
            ).withSkill(skillId);
            if (simulate) {
                request = request.withFlags(Set.of(ResourceTransactionRequest.Flag.SIMULATE));
            }
            return ResourceTransactionService.transact(request).succeeded();
        }
    }

    public static final class Data implements ProgressingSkillData {
        private final SkillProgressionData progression;
        private boolean enabled;

        public Data(SkillProgressionData progression, boolean enabled) {
            this.progression = progression == null ? new SkillProgressionData() : progression;
            this.enabled = enabled;
        }

        public Data(ValueInput input, boolean defaultEnabled) {
            this(new SkillProgressionData(input.childOrEmpty("progression")), input.getBooleanOr("enabled", defaultEnabled));
        }

        public Data(ByteBuf buf) {
            this(new SkillProgressionData(buf), buf.readBoolean());
        }

        @Override
        public SkillProgressionData getSkillProgression() {
            return progression;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        @Override
        public void write(ValueOutput output, RegistryAccess access) {
            progression.write(output.child("progression"));
            output.putBoolean("enabled", enabled);
        }

        @Override
        public void encode(ByteBuf buf, RegistryAccess access) {
            progression.encode(buf);
            buf.writeBoolean(enabled);
        }

        @Override
        public SkillType getType() {
            return AscensionSkillTypes.PASSIVE_SKILL_TYPE.get();
        }
    }
}
