package net.zic.ascension.impl.core.physique;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.path.PathInstance;
import net.zic.ascension.api.ascension.core.path.bonus.PathBonus;
import net.zic.ascension.api.ascension.core.path.realm.Realm;
import net.zic.ascension.api.ascension.core.physique.Physique;
import net.zic.ascension.api.ascension.core.physique.PhysiqueData;
import net.zic.ascension.api.ascension.core.requirement.RequirementHolder;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.ascension.datapack.path.PathBonusBase;
import net.zic.ascension.api.ascension.datapack.path.PathBonusModifier;
import net.zic.ascension.api.ascension.datapack.path.realm.PathRealmChangeEvent;
import net.zic.ascension.api.ascension.datapack.physique.PhysiqueType;
import net.zic.ascension.api.ascension.event.physique.PhysiqueChangedEvent;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.api.rpg_engine.source.OriginSourceEvent;
import net.zic.ascension.api.tooltip.AscensionItemTooltipDefinition;
import net.zic.ascension.impl.core.path.simple.SimplePathInstance;
import net.zic.ascension.impl.datapack.physique.AscensionPhysiqueTypes;
import net.zic.zenithlib.common.ZenithRegistries;

import net.zic.zenithlib.stats.Stat;
import net.zic.zenithlib.stats.ZenithStatHelper;
import net.zic.zenithlib.value_containers.typed.Modifier;
import net.zic.zenithlib.value_containers.typed.ModifierHolder;

import java.util.*;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public record InfiniteBreakthroughPhysique(Component name, Component description, List<Identifier> unlockedPaths,List<Identifier> skills,
                                           Map<Identifier, ModifierHolder<Double>> statModifiers,
                                           Map<PathBonus,ModifierHolder<Double>> pathBonusModifiers,
                                           Identifier path,
                                           int infiniteRealm,
                                           Optional<AscensionItemTooltipDefinition> itemTooltip,
                                           RequirementHolder requirements) implements Physique {

    public static List<InfiniteBreakthroughPhysique> tempRef = new ArrayList<>();
    public static Set<Identifier> existingPhysiques = new HashSet<>();

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event){
        while(!tempRef.isEmpty()){
            InfiniteBreakthroughPhysique physique = tempRef.removeLast();
            Identifier physiqueId = CoreRegistries.PHYSIQUE_REGISTRY.get(event.getServer().registryAccess()).getKey(physique);

            if(physiqueId == null) return;



            existingPhysiques.add(physiqueId);
        }
    }
    @SubscribeEvent
    public static void onServerEnd(ServerStoppedEvent event){
        existingPhysiques.clear();
    }
    @SubscribeEvent
    public static void onRealmUp(PathRealmChangeEvent.PathRealmUpEvent event){
        if(existingPhysiques.contains(AscensionOriginSourceHelper.getPhysiqueId(event.getSource()))){
            InfiniteBreakthroughPhysique physique = (InfiniteBreakthroughPhysique) AscensionOriginSourceHelper.getPhysique(event.getSource());
            if(physique == null) return;
            Realm realm = Realm.of(physique.infiniteRealm(),0);
            if(!realm.isInRange(event.getRealm(),event.getOldRealm())) return;

            PathInstance pathInstance = AscensionOriginSourceHelper.getPathInstance(event.getSource(),physique.path());
            if(!(pathInstance instanceof SimplePathInstance simplePathInstance)) return;

            simplePathInstance.limitBreakRealm(event.getSource(),physique.infiniteRealm,AscensionOriginSourceHelper.getPhysiqueId(event.getSource()));
        }
    }
    @SubscribeEvent
    public static void onSourceFinishedLoading(OriginSourceEvent.OriginSourceFinishedLoadingEvent event){
        if(existingPhysiques.contains(AscensionOriginSourceHelper.getPhysiqueId(event.getSource()))){
            InfiniteBreakthroughPhysique physique = (InfiniteBreakthroughPhysique) AscensionOriginSourceHelper.getPhysique(event.getSource());
            if(physique == null) return;
            Realm realm = Realm.of(physique.infiniteRealm(),0);


            PathInstance pathInstance = AscensionOriginSourceHelper.getPathInstance(event.getSource(),physique.path());
            if(!(pathInstance instanceof SimplePathInstance simplePathInstance)) return;

            if(simplePathInstance.getCurrentMajorRealm() < 0) return;

            Realm start = Realm.of(0,0);
            Realm end = simplePathInstance.getCurrentRealm();

            if(!realm.isInRange(start,end)) return;
            simplePathInstance.limitBreakRealm(event.getSource(),physique.infiniteRealm,AscensionOriginSourceHelper.getPhysiqueId(event.getSource()));
        }
    }
    @SubscribeEvent
    public static void onPhysiqueChangeEvent(PhysiqueChangedEvent.Post event){
        if(existingPhysiques.contains(event.getPhysiqueIdentifier())){
            InfiniteBreakthroughPhysique physique = (InfiniteBreakthroughPhysique) event.getPhysique(event.getSource().getRegistryAccess());

            PathInstance pathInstance = AscensionOriginSourceHelper.getPathInstance(event.getSource(),physique.path());
            if(!(pathInstance instanceof SimplePathInstance simplePathInstance)) return;

            simplePathInstance.removeRealmLimitBreak(event.getSource(),physique.infiniteRealm,event.getPhysiqueIdentifier());
        }
        if(existingPhysiques.contains(event.getNewPhysiqueIdentifier())){
            InfiniteBreakthroughPhysique physique = (InfiniteBreakthroughPhysique) event.getNewPhysique(event.getSource().getRegistryAccess());
            Realm realm = Realm.of(physique.infiniteRealm(),0);


            PathInstance pathInstance = AscensionOriginSourceHelper.getPathInstance(event.getSource(),physique.path());
            if(!(pathInstance instanceof SimplePathInstance simplePathInstance)) return;

            if(simplePathInstance.getCurrentMajorRealm() < 0) return;

            Realm start = Realm.of(0,0);
            Realm end = simplePathInstance.getCurrentRealm();

            if(!realm.isInRange(start,end)) return;
            simplePathInstance.limitBreakRealm(event.getSource(),physique.infiniteRealm,event.getNewPhysiqueIdentifier());

        }
    }

    public InfiniteBreakthroughPhysique(
            Component name,
             Component description,
             List<Identifier> unlockedPaths,
             List<Identifier> skills,
            Map<Identifier,ModifierHolder<Double>> statModifiers,
            Map<PathBonus,ModifierHolder<Double>> pathBonusModifiers,
             Identifier path,
             int infiniteRealm,
             Optional<AscensionItemTooltipDefinition> itemTooltip,
             RequirementHolder requirements
    ) {
        this.name = name;
        this.description = description;
        this.unlockedPaths = unlockedPaths;
        this.skills = skills;
        this.statModifiers = statModifiers;
        this.pathBonusModifiers = pathBonusModifiers;
        this.itemTooltip = itemTooltip == null ? Optional.empty() : itemTooltip;
        this.requirements = requirements == null ? RequirementHolder.EMPTY : requirements;
        this.infiniteRealm = infiniteRealm;
        this.path = path;
        tempRef.add(this);

    }



    @Override
    public PhysiqueType getType() {
        return AscensionPhysiqueTypes.INFINITE_BREAKTHROUGH_PHYSIQUE_TYPE.get();
    }

    @Override
    public Collection<Identifier> onAdded(OriginSource source, PhysiqueData data) {

        Identifier physiqueId = CoreRegistries.PHYSIQUE_REGISTRY.get(source.getRegistryAccess()).getKey(this);

        for(Map.Entry<Identifier,ModifierHolder<Double>> modifiers : statModifiers.entrySet()){
            Stat stat = ZenithStatHelper.stat(modifiers.getKey());
            for(Modifier<Double> modifier : modifiers.getValue().flat()){
                source.addFlatStatModifier(stat,modifier);
            }
            for(Modifier<Double> modifier : modifiers.getValue().multiplier()){
                source.addMultiplierStatModifier(stat,modifier);
            }
        }
        for(Map.Entry<PathBonus,ModifierHolder<Double>> modifiers : pathBonusModifiers.entrySet()){
            PathBonus bonus = modifiers.getKey();
            for(Modifier<Double> modifier : modifiers.getValue().flat()){
                AscensionOriginSourceHelper.addBonusFlatModifier(
                        source,
                        bonus.category(),
                        bonus.path(),
                        modifier
                );
            }
            for(Modifier<Double> modifier : modifiers.getValue().multiplier()){
                AscensionOriginSourceHelper.addBonusMultiplierModifier(
                        source,
                        bonus.category(),
                        bonus.path(),
                        modifier
                );
            }
        }

        for (Identifier skill : skills) {
            AscensionOriginSourceHelper.addSkill(source,skill,physiqueId);
        }

        return unlockedPaths;
    }

    @Override
    public Collection<Identifier> onRemoved(OriginSource source, PhysiqueData data) {
        Identifier physiqueId = CoreRegistries.PHYSIQUE_REGISTRY.get(source.getRegistryAccess()).getKey(this);
        for(Map.Entry<Identifier,ModifierHolder<Double>> modifiers : statModifiers.entrySet()){
            Stat stat = ZenithStatHelper.stat(modifiers.getKey());
            for(Modifier<Double> modifier : modifiers.getValue().flat()){
                source.removeStatModifier(stat,modifier.id());
            }
            for(Modifier<Double> modifier : modifiers.getValue().multiplier()){
                source.removeStatModifier(stat,modifier.id());
            }
        }
        for(Map.Entry<PathBonus,ModifierHolder<Double>> modifiers : pathBonusModifiers.entrySet()){
            PathBonus bonus = modifiers.getKey();
            for(Modifier<Double> modifier : modifiers.getValue().flat()){
                AscensionOriginSourceHelper.removeBonusModifier(
                        source,
                        bonus.category(),
                        bonus.path(),
                        modifier.id()
                );
            }
            for(Modifier<Double> modifier : modifiers.getValue().multiplier()){
                AscensionOriginSourceHelper.removeBonusModifier(
                        source,
                        bonus.category(),
                        bonus.path(),
                        modifier.id()
                );
            }
        }

        for (Identifier skill : skills) {
            AscensionOriginSourceHelper.removeSkill(source,skill,physiqueId);
        }

        return unlockedPaths;
    }

    @Override
    public void applyToEntity(LivingEntity entity, PhysiqueData data) {

    }

    @Override
    public void removeFromEntity(LivingEntity entity, PhysiqueData data) {

    }

    @Override
    public PhysiqueData newData(RegistryAccess access) {
        return new EmptyPhysiqueData();
    }

    @Override
    public PhysiqueData loadData(ValueInput input, RegistryAccess access) {
        return new EmptyPhysiqueData();
    }

    @Override
    public PhysiqueData loadData(ByteBuf buf) {
        return new EmptyPhysiqueData();
    }
}
