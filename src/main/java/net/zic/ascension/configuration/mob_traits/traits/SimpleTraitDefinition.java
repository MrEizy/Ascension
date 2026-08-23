package net.zic.ascension.configuration.mob_traits.traits;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.capabilities.AscensionEntityDataProvider;
import net.zic.ascension.api.ascension.capabilities.CoreCapabilities;
import net.zic.ascension.api.ascension.core.entity.AscensionEntityData;
import net.zic.ascension.api.ascension.core.path.PathInstance;
import net.zic.ascension.api.ascension.core.path.realm.Realm;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.configuration.mob_traits.traits.cultivation_traits.PathDefinition;
import net.zic.ascension.configuration.mob_traits.traits.cultivation_traits.PotentialRealmDefinition;
import net.zic.ascension.configuration.mobs.condition.MobConfigurationCondition;
import net.zic.ascension.configuration.mob_traits.MobTraitDefinition;
import net.zic.ascension.configuration.mob_traits.MobTraitDefinitionType;
import net.zic.zenithlib.common.ZenithAttachments;
import net.zic.zenithlib.common.ZenithRegistries;
import net.zic.zenithlib.custom_attributes.ZenithAttributeHolder;
import net.zic.zenithlib.stats.Stat;
import net.zic.zenithlib.value_containers.ValueContainer;
import net.zic.zenithlib.value_containers.ValueContainerModifier;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public record SimpleTraitDefinition (UUID traitId,
                                     Component name,
                                     Component prefix,
                                     List<PathDefinition> paths,
                                     List<ValueContainer.BaseModifier> baseStats,
                                     Map<Identifier, List<ValueContainerModifier>> statModifiers,
                                     Map<Identifier, List<ValueContainerModifier>> attributeModifiers,
                                     List<Identifier> skills) implements MobTraitDefinition {

    public SimpleTraitDefinition(Component name,
                                 Component prefix,
                                 List<PathDefinition> paths,
                                 List<ValueContainer.BaseModifier> baseStats,
                                 Map<Identifier, List<ValueContainerModifier>> statModifiers,
                                 Map<Identifier, List<ValueContainerModifier>> attributeModifiers,
                                 List<Identifier> skills) {
        this(UUID.randomUUID(), name, prefix, paths, baseStats, statModifiers, attributeModifiers, skills);

    }


    @Override
    public MobTraitDefinitionType getType() {
        return AscensionTraitTypes.SIMPLE_TRAIT_TYPE.get();
    }


    public Identifier getId(){
        return Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID,"trait_"+traitId.toString());
    }
    @Override
    public void applyToSource(OriginSource source) {
        paths.forEach(path-> {
            if(AscensionOriginSourceHelper.hasPath(source,path.path())) AscensionOriginSourceHelper.addPath(source, path.path(), getId());
            else createFreshPath(source,path);
        });
        skills.forEach(skill->AscensionOriginSourceHelper.addSkill(source,skill,getId()));
    }

    @Override
    public void applyToMob(Mob mob) {
        AscensionEntityDataProvider provider = mob.getCapability(CoreCapabilities.ASCENSION_ENTITY_DATA_PROVIDER_CAPABILITY);
        if(provider == null) return;

        AscensionEntityData data = provider.getData();
        if(data == null) return;

        for(ValueContainer.BaseModifier baseStat : baseStats) data.addStat(ZenithRegistries.STAT_REGISTRY.getValue(baseStat.container()),baseStat.val());
        for(Identifier stat : statModifiers.keySet()){
            Stat statObj = ZenithRegistries.STAT_REGISTRY.getValue(stat);
            for(ValueContainerModifier modifier : statModifiers.get(stat)) data.addStatModifier(statObj,modifier);
        }

        ZenithAttributeHolder holder = mob.getData(ZenithAttachments.ATTRIBUTE_HOLDER);

        holder.startProcess("mob_trait_process"+traitId);
        for(Identifier attributeId : attributeModifiers.keySet()){
            if(!BuiltInRegistries.ATTRIBUTE.containsKey(attributeId)) continue;
            Holder<Attribute> attributeHolder =    BuiltInRegistries.ATTRIBUTE.wrapAsHolder(BuiltInRegistries.ATTRIBUTE.getValue(attributeId));
            for(ValueContainerModifier modifier : attributeModifiers.get(attributeId)) holder.getAttribute(attributeHolder).addModifierNoCacheUpdate(modifier);
        }

        holder.resolveProcess("mob_trait_process"+traitId);


    }

    @Override
    public void removeFromSource(OriginSource source) {
        //TODO
    }

    @Override
    public void removeFromMob(Mob mob) {
        //TODO
    }
    public void createFreshPath(OriginSource source, PathDefinition definition){



        if(definition.potentialRealms().isEmpty()) {
            addPath(source, definition.path(), Realm.of(0, 0));
            return;
        }
        int totalWeight = 0;

        for(PotentialRealmDefinition potentialRealmDefinition : definition.potentialRealms()){
            totalWeight += potentialRealmDefinition.weight();
        }
        int rolledValue = ThreadLocalRandom.current().nextInt(totalWeight);
        totalWeight = 0;
        PotentialRealmDefinition selectedRealm = null;
        for(PotentialRealmDefinition potentialRealmDefinition : definition.potentialRealms()){
            totalWeight += potentialRealmDefinition.weight();
            if(rolledValue <= totalWeight){
                selectedRealm = potentialRealmDefinition;
                break;
            }

        }
        int majorRealm = selectedRealm.realm();
        int minorRealm = ThreadLocalRandom.current().nextInt(selectedRealm.minMinorRealm(),selectedRealm.maxMinorRealm()+1);
        addPath(source,definition.path(),Realm.of(majorRealm,minorRealm));

    }
    public PathDefinition getPath(Identifier id){
        for(PathDefinition  definition : paths) {
            if(definition.path().equals(id)) return definition;
        }
        return null;
    }

    private void addPath(OriginSource source,Identifier path, Realm realm){


        if(!AscensionOriginSourceHelper.addPath(source,path,getId())) return;
        PathInstance pathInstance = AscensionOriginSourceHelper.getPathInstance(source,path);

        if(pathInstance.getCurrentRealm().compareTo(realm) >= 0) return;


        pathInstance.handleRealmChange(realm,source);
    }

}
