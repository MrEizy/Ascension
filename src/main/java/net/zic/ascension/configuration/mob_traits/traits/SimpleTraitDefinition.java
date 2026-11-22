package net.zic.ascension.configuration.mob_traits.traits;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Mob;
import net.zic.ascension.api.ascension.core.CoreAttachments;
import net.zic.ascension.api.ascension.core.CoreDataSources;
import net.zic.ascension.api.ascension.core.path.PathHolder;
import net.zic.ascension.api.ascension.core.path.PathInstance;
import net.zic.ascension.api.ascension.core.path.bonus.EntityPathBonusHolder;
import net.zic.ascension.api.ascension.core.path.bonus.PathBonus;
import net.zic.ascension.api.ascension.core.path.realm.Realm;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.configuration.mob_traits.MobTraitDefinition;
import net.zic.ascension.configuration.mob_traits.MobTraitDefinitionType;
import net.zic.ascension.configuration.mob_traits.traits.cultivation_traits.PathDefinition;
import net.zic.ascension.configuration.mob_traits.traits.cultivation_traits.PotentialRealmDefinition;
import net.zic.zenithlib.common.ZenithAttachments;
import net.zic.zenithlib.custom_attributes.SuppressedAttributeHelper;
import net.zic.zenithlib.custom_attributes.ZenithAttribute;
import net.zic.zenithlib.custom_attributes.ZenithAttributeHolder;
import net.zic.zenithlib.stats.EntityStatHolder;
import net.zic.zenithlib.stats.Stat;
import net.zic.zenithlib.stats.ZenithStatHelper;
import net.zic.zenithlib.value_containers.typed.Modifier;
import net.zic.zenithlib.value_containers.typed.ModifierHolder;
import net.zic.zenithlib.value_containers.typed.ValueContainer;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public record SimpleTraitDefinition(Component name,
                                    Component prefix,
                                    List<PathDefinition> paths,
                                    Map<Identifier, ModifierHolder<Double>> statModifiers,
                                    Map<Identifier, ModifierHolder<Double>> attributeModifiers,
                                    Map<PathBonus, ModifierHolder<Double>> pathBonusModifiers,
                                    List<Identifier> skills) implements MobTraitDefinition {

    @Override
    public MobTraitDefinitionType getType() {
        return AscensionTraitTypes.SIMPLE_TRAIT_TYPE.get();
    }

    @Override
    public void applyToSource(OriginSource source, Identifier owner) {
        if (source == null) return;
        for (PathDefinition path : paths) {
            boolean hasPath = AscensionOriginSourceHelper.hasPath(source, path.path());
            PathHolder holder = source.getDataSource(CoreDataSources.PATH_DATA_SOURCE.get());
            if (hasPath || (holder != null && holder.hasCachedPath(path.path()))) {
                AscensionOriginSourceHelper.addPath(source, path.path(), owner);
            } else {
                createFreshPath(source, path, owner);
            }
        }
        for (Identifier skill : skills) {
            AscensionOriginSourceHelper.addSkill(source, skill, owner);
        }
    }

    @Override
    public void applyToMob(Mob mob) {
        EntityStatHolder statHolder = mob.getData(ZenithAttachments.STAT_HOLDER);
        statHolder.startProcess("bulk_stat_gain");
        for (Map.Entry<Identifier, ModifierHolder<Double>> modifiers : statModifiers.entrySet()) {
            Stat stat = ZenithStatHelper.stat(modifiers.getKey());
            for (Modifier<Double> modifier : modifiers.getValue().flat()) {
                statHolder.addFlatModifier(stat, modifier);
            }
            for (Modifier<Double> modifier : modifiers.getValue().multiplier()) {
                statHolder.addMultiplierModifier(stat, modifier);
            }
        }
        statHolder.resolveProcess("bulk_stat_gain");

        ZenithAttributeHolder attributeHolder = mob.getData(ZenithAttachments.ATTRIBUTE_HOLDER);
        attributeHolder.startProcess("bulk_attribute_modification");
        for (Map.Entry<Identifier, ModifierHolder<Double>> modifiers : attributeModifiers.entrySet()) {
            ZenithAttribute attribute = attributeHolder.getAttribute(SuppressedAttributeHelper.getAttribute(modifiers.getKey()));
            attribute.startProcess("bulk_attribute_modification");
            for (Modifier<Double> modifier : modifiers.getValue().flat()) {
                attribute.addFlatModifier(modifier);
            }
            for (Modifier<Double> modifier : modifiers.getValue().multiplier()) {
                attribute.addMultiplierModifier(modifier);
            }
            attribute.resolveProcess("bulk_attribute_modification");
        }
        attributeHolder.resolveProcess("bulk_attribute_modification");

        EntityPathBonusHolder pathBonusHolder = mob.getData(CoreAttachments.PATH_BONUS_HOLDER);
        pathBonusHolder.startProcess("bulk_bonus_gain");
        for (Map.Entry<PathBonus, ModifierHolder<Double>> modifiers : pathBonusModifiers.entrySet()) {
            PathBonus bonus = modifiers.getKey();
            for (Modifier<Double> modifier : modifiers.getValue().flat()) {
                pathBonusHolder.addFlatModifier(bonus.category(), bonus.path(), modifier);
            }
            for (Modifier<Double> modifier : modifiers.getValue().multiplier()) {
                pathBonusHolder.addMultiplierModifier(bonus.category(), bonus.path(), modifier);
            }
        }
        pathBonusHolder.resolveProcess("bulk_bonus_gain");
    }

    @Override
    public void removeFromSource(OriginSource source, Identifier owner) {
        if (source == null) return;
        for (Identifier skill : skills) {
            AscensionOriginSourceHelper.removeSkill(source, skill, owner);
        }
        for (PathDefinition path : paths) {
            AscensionOriginSourceHelper.removePath(source, path.path(), owner);
        }
    }

    @Override
    public void removeFromMob(Mob mob) {
        EntityStatHolder statHolder = mob.getData(ZenithAttachments.STAT_HOLDER);
        statHolder.startProcess("bulk_stat_removal");
        for (Map.Entry<Identifier, ModifierHolder<Double>> modifiers : statModifiers.entrySet()) {
            ValueContainer<Double> container = statHolder.getStatInstance(ZenithStatHelper.stat(modifiers.getKey()));
            if (container == null) continue;
            for (Modifier<Double> modifier : modifiers.getValue().flat()) {
                container.removeModifier(modifier.id());
            }
            for (Modifier<Double> modifier : modifiers.getValue().multiplier()) {
                container.removeModifier(modifier.id());
            }
        }
        statHolder.resolveProcess("bulk_stat_removal");

        ZenithAttributeHolder attributeHolder = mob.getData(ZenithAttachments.ATTRIBUTE_HOLDER);
        attributeHolder.startProcess("bulk_attribute_removal");
        for (Map.Entry<Identifier, ModifierHolder<Double>> modifiers : attributeModifiers.entrySet()) {
            ZenithAttribute attribute = attributeHolder.getAttribute(SuppressedAttributeHelper.getAttribute(modifiers.getKey()));
            attribute.startProcess("bulk_attribute_removal");
            for (Modifier<Double> modifier : modifiers.getValue().flat()) {
                attribute.removeModifier(modifier.id());
            }
            for (Modifier<Double> modifier : modifiers.getValue().multiplier()) {
                attribute.removeModifier(modifier.id());
            }
            attribute.resolveProcess("bulk_attribute_removal");
        }
        attributeHolder.resolveProcess("bulk_attribute_removal");

        EntityPathBonusHolder pathBonusHolder = mob.getData(CoreAttachments.PATH_BONUS_HOLDER);
        pathBonusHolder.startProcess("bulk_bonus_removal");
        for (Map.Entry<PathBonus, ModifierHolder<Double>> modifiers : pathBonusModifiers.entrySet()) {
            PathBonus bonus = modifiers.getKey();
            for (Modifier<Double> modifier : modifiers.getValue().flat()) {
                pathBonusHolder.removeModifier(bonus.category(), bonus.path(), modifier.id());
            }
            for (Modifier<Double> modifier : modifiers.getValue().multiplier()) {
                pathBonusHolder.removeModifier(bonus.category(), bonus.path(), modifier.id());
            }
        }
        pathBonusHolder.resolveProcess("bulk_bonus_removal");
    }

    public void createFreshPath(OriginSource source, PathDefinition definition, Identifier owner) {
        long totalWeight = 0;
        for (PotentialRealmDefinition potential : definition.potentialRealms()) {
            if (potential.weight() > 0 && potential.realm() >= 0
                    && potential.minMinorRealm() >= 0 && potential.maxMinorRealm() >= potential.minMinorRealm()) {
                totalWeight += potential.weight();
            }
        }
        if (totalWeight == 0) {
            addPath(source, definition.path(), Realm.of(0, 0), owner);
            return;
        }

        long roll = ThreadLocalRandom.current().nextLong(totalWeight);
        long cumulativeWeight = 0;
        for (PotentialRealmDefinition potential : definition.potentialRealms()) {
            if (potential.weight() <= 0 || potential.realm() < 0
                    || potential.minMinorRealm() < 0 || potential.maxMinorRealm() < potential.minMinorRealm()) continue;
            cumulativeWeight += potential.weight();
            if (roll < cumulativeWeight) {
                int minorRealm = (int) ThreadLocalRandom.current().nextLong(
                        potential.minMinorRealm(), (long) potential.maxMinorRealm() + 1);
                addPath(source, definition.path(), Realm.of(potential.realm(), minorRealm), owner);
                return;
            }
        }
    }

    public PathDefinition getPath(Identifier id) {
        for (PathDefinition definition : paths) {
            if (definition.path().equals(id)) return definition;
        }
        return null;
    }

    private void addPath(OriginSource source, Identifier path, Realm realm, Identifier owner) {
        if (!AscensionOriginSourceHelper.addPath(source, path, owner)) return;
        PathInstance<?> instance = AscensionOriginSourceHelper.getPathInstance(source, path);
        if (instance == null || instance.getCurrentRealm().compareTo(realm) >= 0) return;
        instance.handleRealmChange(realm, source);
    }
}
