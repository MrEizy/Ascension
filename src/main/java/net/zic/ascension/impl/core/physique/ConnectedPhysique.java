package net.zic.ascension.impl.core.physique;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.api.ascension.core.path.bonus.PathBonus;
import net.zic.ascension.api.ascension.core.physique.Physique;
import net.zic.ascension.api.ascension.core.physique.PhysiqueInstance;
import net.zic.ascension.api.ascension.core.requirement.RequirementHolder;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.ascension.datapack.physique.PhysiqueType;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.api.rpg_engine.source.data_source.util.SerializerHandler;
import net.zic.ascension.api.rpg_engine.source.data_source.util.SyncHandler;
import net.zic.ascension.api.tooltip.AscensionItemTooltipDefinition;
import net.zic.ascension.impl.core.connection.Connected;
import net.zic.ascension.impl.core.connection.ConnectionRequirements;
import net.zic.ascension.impl.datapack.physique.AscensionPhysiqueTypes;
import net.zic.zenithlib.stats.Stat;
import net.zic.zenithlib.stats.ZenithStatHelper;
import net.zic.zenithlib.value_containers.typed.Modifier;
import net.zic.zenithlib.value_containers.typed.ModifierHolder;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Behaves exactly like SimplePhysique, but can only be taken while its technique and/or bloodline connections are met.
 * It has no physique connection, a physique cannot require a physique.
 */
public record ConnectedPhysique(Component name, Component description, List<Identifier> unlockedPaths, List<Identifier> skills,
                                Map<Identifier, ModifierHolder<Double>> statModifiers,
                                Map<PathBonus, ModifierHolder<Double>> pathBonusModifiers,
                                Optional<AscensionItemTooltipDefinition> itemTooltip,
                                RequirementHolder requirements,
                                ConnectionRequirements connections) implements Physique<EmptyPhysiqueData>, Connected {

    public ConnectedPhysique {
        itemTooltip = itemTooltip == null ? Optional.empty() : itemTooltip;
        requirements = requirements == null ? RequirementHolder.EMPTY : requirements;
        connections = connections == null ? ConnectionRequirements.EMPTY : connections;
    }

    @Override
    public PhysiqueType getType() {
        return AscensionPhysiqueTypes.CONNECTED_PHYSIQUE_TYPE.get();
    }

    @Override
    public Collection<Identifier> onAdded(OriginSource source, PhysiqueInstance<EmptyPhysiqueData> instance) {
        Identifier physiqueId = instance.getPhysiqueId();

        for (Map.Entry<Identifier, ModifierHolder<Double>> modifiers : statModifiers.entrySet()) {
            Stat stat = ZenithStatHelper.stat(modifiers.getKey());
            for (Modifier<Double> modifier : modifiers.getValue().flat()) {
                source.addFlatStatModifier(stat, modifier);
            }
            for (Modifier<Double> modifier : modifiers.getValue().multiplier()) {
                source.addMultiplierStatModifier(stat, modifier);
            }
        }
        for (Map.Entry<PathBonus, ModifierHolder<Double>> modifiers : pathBonusModifiers.entrySet()) {
            PathBonus bonus = modifiers.getKey();
            for (Modifier<Double> modifier : modifiers.getValue().flat()) {
                AscensionOriginSourceHelper.addBonusFlatModifier(source, bonus.category(), bonus.path(), modifier);
            }
            for (Modifier<Double> modifier : modifiers.getValue().multiplier()) {
                AscensionOriginSourceHelper.addBonusMultiplierModifier(source, bonus.category(), bonus.path(), modifier);
            }
        }
        for (Identifier skill : skills) {
            AscensionOriginSourceHelper.addSkill(source, skill, physiqueId);
        }
        return unlockedPaths;
    }

    @Override
    public Collection<Identifier> onRemoved(OriginSource source, PhysiqueInstance<EmptyPhysiqueData> instance) {
        Identifier physiqueId = instance.getPhysiqueId();

        for (Map.Entry<Identifier, ModifierHolder<Double>> modifiers : statModifiers.entrySet()) {
            Stat stat = ZenithStatHelper.stat(modifiers.getKey());
            for (Modifier<Double> modifier : modifiers.getValue().flat()) {
                source.removeStatModifier(stat, modifier.id());
            }
            for (Modifier<Double> modifier : modifiers.getValue().multiplier()) {
                source.removeStatModifier(stat, modifier.id());
            }
        }
        for (Map.Entry<PathBonus, ModifierHolder<Double>> modifiers : pathBonusModifiers.entrySet()) {
            PathBonus bonus = modifiers.getKey();
            for (Modifier<Double> modifier : modifiers.getValue().flat()) {
                AscensionOriginSourceHelper.removeBonusModifier(source, bonus.category(), bonus.path(), modifier.id());
            }
            for (Modifier<Double> modifier : modifiers.getValue().multiplier()) {
                AscensionOriginSourceHelper.removeBonusModifier(source, bonus.category(), bonus.path(), modifier.id());
            }
        }
        for (Identifier skill : skills) {
            AscensionOriginSourceHelper.removeSkill(source, skill, physiqueId);
        }
        return unlockedPaths;
    }

    @Override
    public void applyToEntity(LivingEntity entity, PhysiqueInstance<EmptyPhysiqueData> instance) {
    }

    @Override
    public void removeFromEntity(LivingEntity entity, PhysiqueInstance<EmptyPhysiqueData> instance) {
    }

    @Override
    public Component getName(@Nullable EmptyPhysiqueData data) {
        return name;
    }

    @Override
    public Component getDescription(@Nullable EmptyPhysiqueData data) {
        return description;
    }

    @Override
    public EmptyPhysiqueData newData(RegistryAccess access) {
        return new EmptyPhysiqueData();
    }

    @Override
    public SerializerHandler<EmptyPhysiqueData> serializerHandler() {
        return EmptyPhysiqueData.SERIALIZER_HANDLER;
    }

    @Override
    public SyncHandler<EmptyPhysiqueData> syncHandler() {
        return EmptyPhysiqueData.SYNC_HANDLER;
    }
}