package net.zic.ascension.impl.core.technique;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.path.Path;
import net.zic.ascension.api.ascension.core.path.PathInstance;
import net.zic.ascension.api.ascension.core.progression.ProgressActionHolder;
import net.zic.ascension.api.ascension.core.progression.ProgressDirection;
import net.zic.ascension.api.ascension.core.requirement.RequirementHolder;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.ascension.core.technique.Technique;
import net.zic.ascension.api.ascension.core.technique.TechniqueData;
import net.zic.ascension.api.ascension.core.technique.TechniqueSkillDefinition;
import net.zic.ascension.api.ascension.core.tribulation.TribulationDefinition;
import net.zic.ascension.api.ascension.core.tribulation.TribulationManager;
import net.zic.ascension.api.ascension.datapack.technique.TechniqueType;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.api.tooltip.AscensionItemTooltipDefinition;
import net.zic.ascension.impl.core.technique.realm.MajorRealmDefinitionOverride;
import net.zic.ascension.impl.datapack.technique.AscensionTechniqueTypes;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SimpleTechnique implements Technique {
    private final Component name;
    private final Component description;
    private final Identifier path;
    private final List<Integer> milestoneRealms;
    private final List<String> techniqueFamilies;
    private final Integer maxMajorRealm;
    private final Integer maxMinorRealm;
    private final int minMajorRealm;
    private final Map<Identifier, TechniqueSkillDefinition> skills;
    private final ProgressActionHolder holder;
    private final Map<Integer, MajorRealmDefinitionOverride> majorRealmOverrides;
    private final Optional<AscensionItemTooltipDefinition> itemTooltip;
    private final RequirementHolder requirements;

    public SimpleTechnique(
            Component name,
            Component description,
            Identifier path,
            List<Integer> milestoneRealms,
            List<String> techniqueFamilies,
            Integer maxMajorRealm,
            Integer maxMinorRealm,
            int minMajorRealm,
            Optional<AscensionItemTooltipDefinition> itemTooltip,
            Map<Identifier, TechniqueSkillDefinition> skills,
            ProgressActionHolder holder,
            Map<Integer, MajorRealmDefinitionOverride> majorRealmOverrides,
            RequirementHolder requirements
    ) {
        this.name = name;
        this.description = description;
        this.path = path;
        this.milestoneRealms = milestoneRealms == null ? List.of() : List.copyOf(milestoneRealms);
        this.techniqueFamilies = techniqueFamilies == null ? List.of() : List.copyOf(techniqueFamilies);
        this.maxMajorRealm = maxMajorRealm;
        this.maxMinorRealm = maxMinorRealm;
        this.minMajorRealm = minMajorRealm;
        this.itemTooltip = itemTooltip == null ? Optional.empty() : itemTooltip;
        this.skills = skills == null ? Map.of() : Map.copyOf(skills);
        this.holder = holder;
        this.majorRealmOverrides = majorRealmOverrides == null ? Map.of() : Map.copyOf(majorRealmOverrides);
        this.requirements = requirements == null ? RequirementHolder.EMPTY : requirements;
    }

    public ProgressActionHolder getHolder() {
        return holder;
    }

    public Map<Identifier, TechniqueSkillDefinition> getSkills() {
        return skills;
    }

    public Map<Integer, MajorRealmDefinitionOverride> getMajorRealmOverrides() {
        return majorRealmOverrides;
    }

    public Optional<Integer> getHardCodedMaxMajorRealm() {
        return Optional.ofNullable(maxMajorRealm);
    }

    public Optional<Integer> getHardCodedMaxMinorRealm() {
        return Optional.ofNullable(maxMinorRealm);
    }

    public Optional<Integer> getHardCodedMinMajorRealm() {
        return Optional.of(minMajorRealm);
    }


    @Override
    public RequirementHolder requirements() {
        return requirements;
    }

    @Override
    public TechniqueType getType() {
        return AscensionTechniqueTypes.SIMPLE_TECHNIQUE_TYPE.get();
    }

    @Override
    public Component getName(@Nullable TechniqueData techniqueData) {
        return name;
    }

    @Override
    public Component getDescription(@Nullable TechniqueData techniqueData) {
        return description;
    }

    @Override
    public Identifier getPath() {
        return path;
    }

    @Override
    public Optional<AscensionItemTooltipDefinition> itemTooltip() {
        return itemTooltip;
    }

    @Override
    public void onAdded(OriginSource source, TechniqueData data) {
        PathInstance pathInstance = AscensionOriginSourceHelper.getPathInstance(source, path);
        Identifier techniqueId = techniqueId(source);
        if (pathInstance == null || techniqueId == null) {
            return;
        }
        TechniqueSkillService.reconcile(source, techniqueId, pathInstance, skills);
        holder.run(source, techniqueId, pathInstance, ProgressDirection.UP);
        TechniqueSkillService.reconcile(source, techniqueId, pathInstance, skills);
    }

    @Override
    public void onRemoved(OriginSource source, TechniqueData data) {
        PathInstance pathInstance = AscensionOriginSourceHelper.getPathInstance(source, path);
        Identifier techniqueId = techniqueId(source);
        if (techniqueId == null) {
            return;
        }
        if (pathInstance != null) {
            holder.run(source, techniqueId, pathInstance, ProgressDirection.DOWN);
        }
        TechniqueSkillService.remove(source, techniqueId, skills);
    }

    @Override
    public List<Integer> getMilestoneRealms() {
        return milestoneRealms;
    }

    @Override
    public List<String> getTechniqueFamilies() {
        return techniqueFamilies;
    }

    @Override
    public Component getMajorRealmName(int majorRealm, @Nullable TechniqueData techniqueData, RegistryAccess registryAccess) {
        if (majorRealmOverrides.containsKey(majorRealm) && majorRealmOverrides.get(majorRealm).hasNameOverride()) {
            return majorRealmOverrides.get(majorRealm).getName();
        }
        Path pathDefinition = CoreRegistries.safeAccess(CoreRegistries.PATH_REGISTRY, path, registryAccess);
        return pathDefinition == null ? Component.empty() : pathDefinition.getMajorRealmName(majorRealm);
    }

    @Override
    public Component getMinorRealmName(int majorRealm, int minorRealm, @Nullable TechniqueData techniqueData, RegistryAccess registryAccess) {
        if (majorRealmOverrides.containsKey(majorRealm)
                && majorRealmOverrides.get(majorRealm).hasRealmOverride(minorRealm)
                && majorRealmOverrides.get(majorRealm).getRealmOverride(minorRealm).hasNameOverride()) {
            return majorRealmOverrides.get(majorRealm).getRealmOverride(minorRealm).getName();
        }
        Path pathDefinition = CoreRegistries.safeAccess(CoreRegistries.PATH_REGISTRY, path, registryAccess);
        return pathDefinition == null ? Component.empty() : pathDefinition.getMinorRealmName(majorRealm, minorRealm);
    }

    @Override
    public Component getRealmName(int majorRealm, int minorRealm, @Nullable TechniqueData techniqueData, RegistryAccess registryAccess) {
        return Component.empty()
                .append(getMajorRealmName(majorRealm, techniqueData, registryAccess))
                .append("(")
                .append(getMinorRealmName(majorRealm, minorRealm, techniqueData, registryAccess))
                .append(")");
    }

    @Override
    public int getMaxMajorRealm(@Nullable TechniqueData techniqueData, RegistryAccess registryAccess) {
        if (maxMajorRealm != null) {
            return maxMajorRealm;
        }
        Path pathDefinition = CoreRegistries.safeAccess(CoreRegistries.PATH_REGISTRY, path, registryAccess);
        return pathDefinition == null ? 9 : pathDefinition.getMaxMajorRealm();
    }

    @Override
    public int getMaxMinorRealm(int majorRealm, @Nullable TechniqueData techniqueData, RegistryAccess registryAccess) {
        Path pathDefinition = CoreRegistries.safeAccess(CoreRegistries.PATH_REGISTRY, path, registryAccess);
        if (majorRealm == getMaxMajorRealm(techniqueData, registryAccess) && maxMinorRealm != null) {
            return maxMinorRealm;
        }
        return pathDefinition == null ? 9 : pathDefinition.getMaxMinorRealm(majorRealm);
    }

    @Override
    public int getMinMajorRealm(RegistryAccess registryAccess) {
        return minMajorRealm;
    }

    @Override
    public double getMaxProgress(int majorRealm, int minorRealm, @Nullable TechniqueData techniqueData, RegistryAccess registryAccess) {
        if (majorRealmOverrides.containsKey(majorRealm)
                && majorRealmOverrides.get(majorRealm).hasRealmOverride(minorRealm)
                && majorRealmOverrides.get(majorRealm).getRealmOverride(minorRealm).hasProgressOverride()) {
            return majorRealmOverrides.get(majorRealm).getRealmOverride(minorRealm).getProgress();
        }
        Path pathDefinition = CoreRegistries.safeAccess(CoreRegistries.PATH_REGISTRY, path, registryAccess);
        return pathDefinition == null ? 100 : pathDefinition.getMaxProgress(majorRealm, minorRealm);
    }

    @Override
    public TribulationDefinition getTribulation(int majorRealm, int minorRealm, RegistryAccess access) {
        return null;
    }

    @Override
    public boolean tryBreakthrough(
            LivingEntity entity,
            OriginSource source,
            int majorRealm,
            int minorRealm,
            double progress,
            @Nullable TechniqueData techniqueData
    ) {
        double maxProgress = getMaxProgress(majorRealm, minorRealm, techniqueData, source.getRegistryAccess());
        double maxMajor = getMaxMajorRealm(techniqueData, source.getRegistryAccess());
        double maxMinor = getMaxMinorRealm(majorRealm, techniqueData, source.getRegistryAccess());
        boolean canBreakthrough = maxProgress <= progress
                && ((maxMinor > minorRealm && maxMajor >= majorRealm)
                || (maxMinor <= minorRealm && maxMajor > majorRealm));
        if (!canBreakthrough) {
            return false;
        }

        TribulationDefinition definition = getTribulation(majorRealm, minorRealm, source.getRegistryAccess());
        if (definition == null) {
            return true;
        }
        TribulationManager.getInstance().triggerTribulation(definition, entity);
        return false;
    }

    @Override
    public void onRealmUp(OriginSource source, TechniqueData techniqueData) {
        reconcileRealm(source, ProgressDirection.UP);
    }

    @Override
    public void onRealmDown(OriginSource source, TechniqueData techniqueData) {
        reconcileRealm(source, ProgressDirection.DOWN);
    }

    @Override
    public TechniqueData newData() {
        return new EmptyData();
    }

    @Override
    public TechniqueData loadData(ValueInput input) {
        return new EmptyData();
    }

    @Override
    public TechniqueData loadData(ByteBuf buf) {
        return new EmptyData();
    }

    private void reconcileRealm(OriginSource source, ProgressDirection direction) {
        PathInstance pathInstance = AscensionOriginSourceHelper.getPathInstance(source, path);
        Identifier techniqueId = techniqueId(source);
        if (pathInstance == null || techniqueId == null) {
            return;
        }
        TechniqueSkillService.reconcile(source, techniqueId, pathInstance, skills);
        holder.run(source, techniqueId, pathInstance, direction);
        TechniqueSkillService.reconcile(source, techniqueId, pathInstance, skills);
    }

    private Identifier techniqueId(OriginSource source) {
        return CoreRegistries.TECHNIQUE_REGISTRY.get(source.getRegistryAccess()).getKey(this);
    }

    public static final class EmptyData implements TechniqueData {
        @Override
        public void write(ValueOutput output) {
        }

        @Override
        public void encode(ByteBuf buf) {
        }
    }
}
