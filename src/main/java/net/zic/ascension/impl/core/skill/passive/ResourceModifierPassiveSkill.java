package net.zic.ascension.impl.core.skill.passive;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
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
import net.zic.ascension.api.ascension.core.resource.ResourceTransactionRequest;
import net.zic.ascension.api.ascension.core.resource.ResourceTransactionService;
import net.zic.ascension.api.ascension.core.resource.ResourceModifiers;
import net.zic.ascension.api.ascension.core.skill.SkillData;
import net.zic.ascension.api.ascension.core.skill.ProgressingSkill;
import net.zic.ascension.api.ascension.core.skill.ProgressingSkillData;
import net.zic.ascension.api.ascension.core.skill.SkillDefinitions.Owner;
import net.zic.ascension.api.ascension.core.skill.SkillDefinitions;
import net.zic.ascension.api.ascension.core.skill.SkillProgressionResolver;
import net.zic.ascension.api.ascension.core.skill.SkillProgressionData;
import net.zic.ascension.api.ascension.core.skill.PassiveModule;
import net.zic.ascension.api.ascension.core.skill.toggleable.ToggleableSkill;
import net.zic.ascension.api.ascension.datapack.skill.SkillType;
import net.zic.ascension.api.ascension.value.ScaledValue;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.impl.datapack.skill.AscensionSkillTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.zic.ascension.api.ascension.core.resource.ResourceSourceIdentity;

public class ResourceModifierPassiveSkill implements ProgressingSkill, Owner {
    private final Component name;
    private final Component description;
    private final int defaultAccessibleLevel;
    private final List<Level> levels;
    private final List<Double> experienceRequirements;
    private final SkillDefinitions definitions;
    private final boolean enabledByDefault;
    private final Optional<Upkeep> upkeep;

    public ResourceModifierPassiveSkill(
            Component name,
            Component description,
            int defaultAccessibleLevel,
            List<LevelTemplate> templates,
            List<PassiveModule> rootModules,
            SkillDefinitions definitions,
            List<Double> experienceRequirements,
            boolean toggleable,
            boolean enabledByDefault,
            Optional<Upkeep> upkeep
    ) {
        this.name = name;
        this.description = description;
        this.levels = resolveLevels(templates, rootModules);
        this.defaultAccessibleLevel = Math.clamp(
                defaultAccessibleLevel <= 0 ? 1 : defaultAccessibleLevel,
                1,
                this.levels.size()
        );
        this.definitions = definitions == null ? SkillDefinitions.EMPTY : definitions;
        this.experienceRequirements = experienceRequirements == null
                ? List.of()
                : experienceRequirements.stream().map(value -> Math.max(0.0D, value)).toList();
        this.enabledByDefault = enabledByDefault;
        this.upkeep = upkeep == null ? Optional.empty() : upkeep;
    }

    public static ResourceModifierPassiveSkill create(
            Component name,
            Component description,
            int defaultAccessibleLevel,
            List<LevelTemplate> templates,
            List<PassiveModule> rootModules,
            SkillDefinitions definitions,
            List<Double> experienceRequirements,
            boolean toggleable,
            boolean enabledByDefault,
            Optional<Upkeep> upkeep
    ) {
        return toggleable
                ? new Toggleable(name, description, defaultAccessibleLevel, templates, rootModules, definitions, experienceRequirements, enabledByDefault, upkeep)
                : new ResourceModifierPassiveSkill(name, description, defaultAccessibleLevel, templates, rootModules, definitions, experienceRequirements, false, true, Optional.empty());
    }

    public int getConfiguredDefaultAccessibleLevel() {
        return defaultAccessibleLevel;
    }

    public List<Level> getLevels() {
        return levels;
    }

    public List<LevelTemplate> getLevelTemplates() {
        return levels.stream().map(level -> new LevelTemplate(Optional.of(level.modules()))).toList();
    }

    public List<Double> getExperienceRequirements() {
        return experienceRequirements;
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

    public List<PassiveModule> modules(int level) {
        if (levels.isEmpty()) {
            return List.of();
        }
        int resolved = Math.clamp(level <= 0 ? 1 : level, 1, levels.size());
        return levels.get(resolved - 1).modules();
    }

    public List<ResourceModifiers.Definition> getModifiers(int level) {
        List<ResourceModifiers.Definition> values = new ArrayList<>();
        for (PassiveModule module : modules(level)) {
            if (module instanceof PassiveModules.Resources resources) {
                values.addAll(resources.modifiers());
            }
        }
        return List.copyOf(values);
    }

    public List<PassiveModules.Defense> defenses(int level) {
        return modules(level).stream()
                .filter(PassiveModules.Defense.class::isInstance)
                .map(PassiveModules.Defense.class::cast)
                .toList();
    }

    public List<net.zic.ascension.api.ascension.core.projectile.NormalProjectileDefinition> projectileProfiles(int level) {
        List<net.zic.ascension.api.ascension.core.projectile.NormalProjectileDefinition> values = new ArrayList<>();
        for (PassiveModule module : modules(level)) {
            if (module instanceof PassiveModules.Projectiles projectiles) {
                values.addAll(projectiles.profiles());
            }
        }
        return List.copyOf(values);
    }

    @Override
    public int getMaximumProgression() {
        return levels.size();
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
        return currentLevel < 0 || currentLevel >= experienceRequirements.size()
                ? Double.POSITIVE_INFINITY
                : experienceRequirements.get(currentLevel);
    }

    @Override
    public void onProgressionChanged(
            OriginSource source,
            ProgressingSkillData data,
            int previousProgression,
            int currentProgression
    ) {
        if (!active(data) || previousProgression == currentProgression) {
            return;
        }
        Identifier skillId = skillId(source);
        if (skillId == null) {
            return;
        }
        modules(previousProgression).forEach(module -> module.remove(source, skillId));
        modules(currentProgression).forEach(module -> module.apply(source, skillId));
    }

    @Override
    public SkillType getType() {
        return AscensionSkillTypes.RESOURCE_MODIFIER_PASSIVE_SKILL_TYPE.get();
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
        if (active(data)) {
            applyModules(source, data);
        }
    }

    @Override
    public void onRemoved(OriginSource source, SkillData data) {
        if (active(data)) {
            removeModules(source, data);
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

    protected boolean active(SkillData data) {
        return !(this instanceof Toggleable) || data instanceof Data passiveData && passiveData.isEnabled();
    }

    protected void applyModules(OriginSource source, SkillData data) {
        Identifier skillId = skillId(source);
        if (skillId == null) {
            return;
        }
        int level = SkillProgressionResolver.resolve(source, skillId).effectiveProgression();
        modules(level).forEach(module -> module.apply(source, skillId));
    }

    protected void removeModules(OriginSource source, SkillData data) {
        Identifier skillId = skillId(source);
        if (skillId == null) {
            return;
        }
        int level = SkillProgressionResolver.resolve(source, skillId).effectiveProgression();
        modules(level).forEach(module -> module.remove(source, skillId));
    }

    private Identifier skillId(OriginSource source) {
        return CoreRegistries.SKILL_REGISTRY.get(source.getRegistryAccess()).getKey(this);
    }

    private static List<Level> resolveLevels(List<LevelTemplate> templates, List<PassiveModule> rootModules) {
        List<PassiveModule> current = rootModules == null ? List.of() : List.copyOf(rootModules);
        List<Level> resolved = new ArrayList<>();
        if (templates == null || templates.isEmpty()) {
            resolved.add(new Level(current));
            return List.copyOf(resolved);
        }
        for (LevelTemplate template : templates) {
            if (template != null && template.modules().isPresent()) {
                current = List.copyOf(template.modules().get());
            }
            resolved.add(new Level(current));
        }
        return List.copyOf(resolved);
    }

    public record Level(List<PassiveModule> modules) {
        public Level {
            modules = modules == null ? List.of() : List.copyOf(modules);
        }
    }

    public record LevelTemplate(Optional<List<PassiveModule>> modules) {
        public static final MapCodec<LevelTemplate> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                PassiveModule.CODEC.listOf().optionalFieldOf("modules").forGetter(LevelTemplate::modules)
        ).apply(instance, LevelTemplate::new));

        public LevelTemplate {
            modules = modules == null ? Optional.empty() : modules.map(List::copyOf);
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

    private static final class Toggleable extends ResourceModifierPassiveSkill implements ToggleableSkill {
        private Toggleable(
                Component name,
                Component description,
                int defaultAccessibleLevel,
                List<LevelTemplate> templates,
                List<PassiveModule> rootModules,
                SkillDefinitions definitions,
                List<Double> experienceRequirements,
                boolean enabledByDefault,
                Optional<Upkeep> upkeep
        ) {
            super(name, description, defaultAccessibleLevel, templates, rootModules, definitions, experienceRequirements, true, enabledByDefault, upkeep);
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
            applyModules(source, data);
        }

        @Override
        public void onDisabled(OriginSource source, SkillData data) {
            removeModules(source, data);
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
        public void write(ValueOutput output,RegistryAccess access) {
            progression.write(output.child("progression"));
            output.putBoolean("enabled", enabled);
        }

        @Override
        public void encode(ByteBuf buf,RegistryAccess access) {
            progression.encode(buf);
            buf.writeBoolean(enabled);
        }

        @Override
        public SkillType getType() {
            return AscensionSkillTypes.RESOURCE_MODIFIER_PASSIVE_SKILL_TYPE.get();
        }
    }

}
