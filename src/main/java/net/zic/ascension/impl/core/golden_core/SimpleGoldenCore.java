package net.zic.ascension.impl.core.golden_core;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.api.ascension.core.golden_core.GoldenCore;
import net.zic.ascension.api.ascension.core.golden_core.GoldenCoreInstance;
import net.zic.ascension.api.ascension.core.path.PathInstance;
import net.zic.ascension.api.ascension.core.path.bonus.PathBonus;
import net.zic.ascension.api.ascension.core.path.realm.Realm;
import net.zic.ascension.api.ascension.core.path.realm.realm_change.RealmChangeAction;
import net.zic.ascension.api.ascension.core.path.realm.realm_change.RealmChangeActionCondition;
import net.zic.ascension.api.ascension.core.progression.ProgressAction;
import net.zic.ascension.api.ascension.core.progression.ProgressActionCondition;
import net.zic.ascension.api.ascension.core.progression.ProgressActionConditionReference;
import net.zic.ascension.api.ascension.core.progression.ProgressActionHolder;
import net.zic.ascension.api.ascension.core.progression.ProgressActionList;
import net.zic.ascension.api.ascension.core.progression.ProgressActionReference;
import net.zic.ascension.api.ascension.core.progression.ProgressDirection;
import net.zic.ascension.api.ascension.core.requirement.RequirementHolder;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.ascension.datapack.golden_core.GoldenCoreType;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.api.rpg_engine.source.data_source.util.SerializerHandler;
import net.zic.ascension.api.rpg_engine.source.data_source.util.SyncHandler;
import net.zic.ascension.api.tooltip.AscensionItemTooltipDefinition;
import net.zic.ascension.impl.core.progression.ProgressionModifiers;
import net.zic.ascension.impl.core.progression.modifier.ModifierConflictAction;
import net.zic.ascension.impl.datapack.golden_core.AscensionGoldenCoreTypes;
import net.zic.zenithlib.value_containers.typed.ModifierHolder;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.UnaryOperator;

/**
 * a golden core configured fully from json.
 * stats and path bonuses are salted with the instance id, so multiple instances of the same core each apply their own modifiers
 */
public record SimpleGoldenCore(Component name, Component description, List<Identifier> skills,
                               Map<Identifier, ModifierHolder<Double>> statModifiers,
                               Map<PathBonus, ModifierHolder<Double>> pathBonusModifiers,
                               ProgressActionList onAddedActions,
                               ProgressActionHolder realmActions,
                               Optional<AscensionItemTooltipDefinition> itemTooltip,
                               RequirementHolder requirements
) implements GoldenCore<EmptyGoldenCoreData> {

    public static final ProgressActionHolder EMPTY_REALM_ACTIONS = new ProgressActionHolder(UUID.randomUUID(), List.of());

    public SimpleGoldenCore {
        onAddedActions = onAddedActions == null ? ProgressActionList.EMPTY : onAddedActions;
        realmActions = realmActions == null ? EMPTY_REALM_ACTIONS : realmActions;
        itemTooltip = itemTooltip == null ? Optional.empty() : itemTooltip;
        requirements = requirements == null ? RequirementHolder.EMPTY : requirements;
    }

    @Override
    public GoldenCoreType getType() {
        return AscensionGoldenCoreTypes.SIMPLE_GOLDEN_CORE_TYPE.get();
    }

    @Override
    public Component getName(@Nullable EmptyGoldenCoreData data) {
        return name;
    }

    @Override
    public Component getDescription(@Nullable EmptyGoldenCoreData data) {
        return description;
    }

    @Override
    public void onAdded(OriginSource source, GoldenCoreInstance<EmptyGoldenCoreData> instance) {
        applyModifiers(source, instance, ProgressDirection.UP);
        for (Identifier skill : skills) {
            AscensionOriginSourceHelper.addSkill(source, skill, instance.contextId());
        }
        onAddedActions.run(source, instance.contextId(), instance.getData(), ProgressDirection.UP, instance.getId().toString());
    }

    @Override
    public void onRemoved(OriginSource source, GoldenCoreInstance<EmptyGoldenCoreData> instance) {
        onAddedActions.run(source, instance.contextId(), instance.getData(), ProgressDirection.DOWN, instance.getId().toString());
        for (Identifier skill : skills) {
            AscensionOriginSourceHelper.removeSkill(source, skill, instance.contextId());
        }
        applyModifiers(source, instance, ProgressDirection.DOWN);
    }

    private void applyModifiers(OriginSource source, GoldenCoreInstance<EmptyGoldenCoreData> instance, ProgressDirection direction) {
        String salt = instance.getId().toString();
        ProgressionModifiers.applyStats(source, statModifiers, ModifierConflictAction.SALT, UnaryOperator.identity(), direction.value, salt);
        ProgressionModifiers.applyPathBonuses(source, pathBonusModifiers, ModifierConflictAction.SALT, UnaryOperator.identity(), direction.value, salt);
    }

    /**
     * runs the realm actions for one realm. the listeners are walked here instead of through {@link ProgressActionHolder#run},
     * as realm conditions/actions otherwise read the path's current realm, which is not the realm being synced when catching up.
     */
    @Override
    public void onRealmChange(OriginSource source, GoldenCoreInstance<EmptyGoldenCoreData> instance, PathInstance<?> path, Realm realm, ProgressDirection direction) {
        RegistryAccess access = source.getRegistryAccess();
        String salt = instance.getId() + "_" + realm.asSalt();
        for (Pair<ProgressActionConditionReference, List<ProgressActionReference>> listener : realmActions.listeners()) {
            ProgressActionCondition condition = listener.getFirst().resolve(access);
            if (condition == null) continue;
            boolean passed = condition instanceof RealmChangeActionCondition realmCondition
                    ? realmCondition.test(source, instance.getPath(), path, realm.majorRealm(), realm.minorRealm(), direction)
                    : condition.test(source, instance.contextId(), path, direction, salt);
            if (!passed) continue;

            for (ProgressActionReference reference : listener.getSecond()) {
                ProgressAction action = reference.resolve(access);
                if (action == null) continue;
                if (action instanceof RealmChangeAction realmAction) {
                    realmAction.run(instance.getId(), source, instance.getPath(), path, realm.majorRealm(), realm.minorRealm(), direction);
                } else {
                    action.run(instance.getId(), source, instance.contextId(), path, direction, salt);
                }
            }
        }
    }

    @Override
    public void applyToEntity(LivingEntity entity, GoldenCoreInstance<EmptyGoldenCoreData> instance) {

    }

    @Override
    public void removeFromEntity(LivingEntity entity, GoldenCoreInstance<EmptyGoldenCoreData> instance) {

    }

    @Override
    public EmptyGoldenCoreData newData(RegistryAccess access) {
        return new EmptyGoldenCoreData();
    }

    @Override
    public SerializerHandler<EmptyGoldenCoreData> serializerHandler() {
        return EmptyGoldenCoreData.SERIALIZER_HANDLER;
    }

    @Override
    public SyncHandler<EmptyGoldenCoreData> syncHandler() {
        return EmptyGoldenCoreData.SYNC_HANDLER;
    }
}
