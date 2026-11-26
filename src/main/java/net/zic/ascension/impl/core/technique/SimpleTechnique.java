package net.zic.ascension.impl.core.technique;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.api.ascension.core.progression.ProgressActionList;
import net.zic.ascension.api.ascension.core.progression.ProgressDirection;
import net.zic.ascension.api.ascension.core.path.PathInstance;
import net.zic.ascension.api.ascension.core.path.Path;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.zenithlib.stats.Stat;
import net.zic.zenithlib.stats.ZenithStatHelper;
import net.zic.zenithlib.value_containers.typed.Modifier;
import net.zic.ascension.api.ascension.core.path.realm.Realm;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.ascension.core.requirement.RequirementHolder;
import net.zic.ascension.api.ascension.core.technique.Technique;
import net.zic.ascension.api.ascension.core.technique.TechniqueData;
import net.zic.ascension.api.ascension.core.technique.TechniqueInstance;
import net.zic.ascension.api.ascension.datapack.technique.TechniqueType;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.api.rpg_engine.source.data_source.util.SerializerHandler;
import net.zic.ascension.api.rpg_engine.source.data_source.util.SyncHandler;
import net.zic.ascension.api.tooltip.AscensionItemTooltipDefinition;
import net.zic.ascension.impl.datapack.technique.AscensionTechniqueTypes;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.Optional;

public class SimpleTechnique implements Technique<SimpleTechnique.Data> {
    private static final SerializerHandler<Data> SERIALIZER_HANDLER = new DataSerializerHandler();
    private static final SyncHandler<Data> SYNC_HANDLER = new SyncHandler.UnitSyncHandler<>();

    private final Component name;
    private final Component description;
    private final ProgressActionList onAddedActions;
    private final List<TechniqueRealmStatBonus> realmStatGrowth;
    private final Map<Identifier, TechniqueRealmStatRate> statsPerRealm;
    private final List<Identifier> cultivationPaths;
    private final Optional<Integer> maxRealm;
    private final Optional<Integer> maxMinorRealm;
    private final Optional<AscensionItemTooltipDefinition> itemTooltip;
    private final RequirementHolder requirements;

    public SimpleTechnique(
            Component name,
            Component description,
            ProgressActionList onAddedActions,
            List<TechniqueRealmStatBonus> realmStatGrowth,
            Map<Identifier, TechniqueRealmStatRate> statsPerRealm,
            List<Identifier> cultivationPaths,
            Optional<Integer> maxRealm,
            Optional<Integer> maxMinorRealm,
            Optional<AscensionItemTooltipDefinition> itemTooltip,
            RequirementHolder requirements
    ) {
        this.name = name;
        this.description = description;
        this.onAddedActions = onAddedActions == null ? ProgressActionList.EMPTY : onAddedActions;
        this.realmStatGrowth = realmStatGrowth == null ? List.of() : List.copyOf(realmStatGrowth);
        this.statsPerRealm = statsPerRealm == null ? Map.of() : Map.copyOf(statsPerRealm);
        this.cultivationPaths = cultivationPaths == null ? List.of() : List.copyOf(cultivationPaths);
        this.maxRealm = maxRealm == null ? Optional.empty() : maxRealm;
        this.maxMinorRealm = maxMinorRealm == null ? Optional.empty() : maxMinorRealm;
        this.itemTooltip = itemTooltip == null ? Optional.empty() : itemTooltip;
        this.requirements = requirements == null ? RequirementHolder.EMPTY : requirements;
    }

    public ProgressActionList getOnAddedActions() {
        return onAddedActions;
    }

    public List<TechniqueRealmStatBonus> getRealmStatGrowth() {
        return realmStatGrowth;
    }

    public Map<Identifier, TechniqueRealmStatRate> getStatsPerRealm() { return statsPerRealm; }
    public List<Identifier> getCultivationPaths() { return cultivationPaths; }
    public Optional<Integer> getMaxRealm() { return maxRealm; }
    public Optional<Integer> getMaxMinorRealm() { return maxMinorRealm; }

    public @Nullable Realm getCultivationLimit(PathInstance<?> path) {
        return getCultivationLimit(path.getPathId(), path.getPath());
    }

    private @Nullable Realm getCultivationLimit(Identifier pathId, Path<?> path) {
        if (maxRealm.isEmpty() || !cultivationPaths.contains(pathId)) return null;
        int major = maxRealm.get();
        if (major >= path.getMaxMajorRealm()) return null;
        int minor = Math.min(maxMinorRealm.orElseGet(() -> path.getMaxMinorRealm(major)),
                path.getMaxMinorRealm(major));
        return Realm.of(major, minor);
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
    public Component getName(@Nullable Data data) {
        return name;
    }

    @Override
    public Component getDescription(@Nullable Data data) {
        return description;
    }

    @Override
    public Optional<AscensionItemTooltipDefinition> itemTooltip() {
        return itemTooltip;
    }

    @Override
    public void onAdded(OriginSource source, TechniqueInstance<Data> instance) {
        runActions(source, instance, ProgressDirection.UP);
        applyOwnedRealmStatGrowth(source, instance.getTechniqueId(), ProgressDirection.UP);
        applyOwnedPerRealmStats(source, instance.getTechniqueId(), true);
    }

    @Override
    public void onRemoved(OriginSource source, TechniqueInstance<Data> instance) {
        applyOwnedRealmStatGrowth(source, instance.getTechniqueId(), ProgressDirection.DOWN);
        applyOwnedPerRealmStats(source, instance.getTechniqueId(), false);
        runActions(source, instance, ProgressDirection.DOWN);
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

    private void applyOwnedRealmStatGrowth(OriginSource source, Identifier techniqueId, ProgressDirection direction) {
        for (TechniqueRealmStatBonus bonus : realmStatGrowth) {
            PathInstance<?> path = AscensionOriginSourceHelper.getPathInstance(source, bonus.path());
            if (path != null && path.getCurrentMajorRealm() >= 0
                    && path.getCurrentRealm().compareTo(bonus.realm()) >= 0) {
                bonus.apply(source, techniqueId, direction);
            }
        }
    }

    public void updateRealmStatGrowth(OriginSource source, Identifier techniqueId, Identifier path,
                                      @Nullable Realm previous, @Nullable Realm current) {
        updatePerRealmStats(source, techniqueId, path, previous, current);
        for (TechniqueRealmStatBonus bonus : realmStatGrowth) {
            if (!bonus.path().equals(path)) continue;
            boolean wasActive = previous != null && previous.compareTo(bonus.realm()) >= 0;
            boolean isActive = current != null && current.compareTo(bonus.realm()) >= 0;
            if (wasActive != isActive) {
                bonus.apply(source, techniqueId, isActive ? ProgressDirection.UP : ProgressDirection.DOWN);
            }
        }
    }

    private void applyOwnedPerRealmStats(OriginSource source, Identifier techniqueId, boolean adding) {
        Set<Identifier> paths = new HashSet<>();
        for (TechniqueRealmStatRate rate : statsPerRealm.values()) {
            Identifier path = rate.path().orElseGet(() -> cultivationPaths.isEmpty() ? null : cultivationPaths.getFirst());
            if (path != null) paths.add(path);
        }
        for (Identifier path : paths) {
            PathInstance<?> current = AscensionOriginSourceHelper.getPathInstance(source, path);
            Realm realm = current != null && current.getCurrentMajorRealm() >= 0 ? current.getCurrentRealm() : null;
            updatePerRealmStats(source, techniqueId, path, adding ? null : realm, adding ? realm : null);
        }
    }

    private void updatePerRealmStats(OriginSource source, Identifier techniqueId, Identifier path,
                                     @Nullable Realm previous, @Nullable Realm current) {
        if (statsPerRealm.isEmpty()) return;
        PathInstance<?> instance = AscensionOriginSourceHelper.getPathInstance(source, path);
        Path<?> pathDefinition = instance == null ? CoreRegistries.safeAccess(
                CoreRegistries.PATH_REGISTRY, path, source.getRegistryAccess()) : instance.getPath();
        if (pathDefinition == null) return;
        Realm limit = getCultivationLimit(path, pathDefinition);
        Realm before = clamp(previous, limit);
        Realm after = clamp(current, limit);
        if (java.util.Objects.equals(before, after)) return;

        int beforeMinor = stages(before, pathDefinition);
        int afterMinor = stages(after, pathDefinition);
        int beforeMajor = before == null ? 0 : before.majorRealm();
        int afterMajor = after == null ? 0 : after.majorRealm();
        for (Map.Entry<Identifier, TechniqueRealmStatRate> entry : statsPerRealm.entrySet()) {
            Identifier ratePath = entry.getValue().path().orElseGet(() -> cultivationPaths.isEmpty() ? null : cultivationPaths.getFirst());
            if (!path.equals(ratePath)) continue;
            Stat stat = ZenithStatHelper.stat(entry.getKey());
            if (stat == null) continue;
            TechniqueRealmStatRate rate = entry.getValue().resolve(source.getRegistryAccess());
            Identifier flatId = modifierId(techniqueId, path, entry.getKey(), "minor_flat");
            Identifier multiplierId = modifierId(techniqueId, path, entry.getKey(), "major_percent");
            if (rate.flat() != 0.0D && beforeMinor != afterMinor) {
                source.removeStatModifier(stat, flatId);
                if (afterMinor > 0) source.addFlatStatModifier(stat, Modifier.flat(flatId, 0, rate.flat() * afterMinor));
            }
            if (rate.multiplier() != 0.0D && beforeMajor != afterMajor) {
                source.removeStatModifier(stat, multiplierId);
                if (afterMajor > 0) source.addMultiplierStatModifier(stat, Modifier.multiplier(multiplierId, 0, rate.multiplier() * afterMajor));
            }
        }
        source.updateEntityStatHolder();
    }

    private static @Nullable Realm clamp(@Nullable Realm realm, @Nullable Realm limit) {
        if (realm == null || realm.majorRealm() < 0) return null;
        return limit != null && realm.compareTo(limit) > 0 ? limit : realm;
    }

    private static int stages(@Nullable Realm realm, Path<?> path) {
        if (realm == null) return 0;
        int count = realm.minorRealm();
        for (int major = 0; major < realm.majorRealm(); major++) {
            count += path.getMaxMinorRealm(major) + 1;
        }
        return count;
    }

    private static Identifier modifierId(Identifier technique, Identifier path, Identifier stat, String kind) {
        return Identifier.fromNamespaceAndPath("ascension", "technique_growth/"
                + technique.getNamespace() + "/" + technique.getPath() + "/"
                + path.getNamespace() + "/" + path.getPath() + "/"
                + stat.getNamespace() + "/" + stat.getPath() + "/" + kind);
    }

    private void runActions(OriginSource source, TechniqueInstance<Data> instance, ProgressDirection direction) {
        onAddedActions.run(source, instance.getTechniqueId(), instance.getData(), direction, instance.getTechniqueId().getPath());
    }

    //simple techniques hold no state
    public static final class Data implements TechniqueData {
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
