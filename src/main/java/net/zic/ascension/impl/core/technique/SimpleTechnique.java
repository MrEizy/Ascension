package net.zic.ascension.impl.core.technique;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.path.Path;
import net.zic.ascension.api.ascension.core.path.PathData;
import net.zic.ascension.api.ascension.core.progression.ProgressActionHolder;
import net.zic.ascension.api.ascension.core.progression.ProgressDirection;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSource;
import net.zic.ascension.api.ascension.core.technique.Technique;
import net.zic.ascension.api.ascension.core.technique.TechniqueData;
import net.zic.ascension.api.ascension.core.tribulation.TribulationDefinition;
import net.zic.ascension.api.ascension.core.tribulation.TribulationManager;
import net.zic.ascension.api.ascension.datapack.technique.TechniqueType;
import net.zic.ascension.api.tooltip.AscensionItemTooltipDefinition;
import net.zic.ascension.impl.core.technique.realm.MajorRealmDefinitionOverride;
import net.zic.ascension.impl.datapack.technique.AscensionTechniqueTypes;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class SimpleTechnique implements Technique {
    private final Component name;
    private final Component description;
    private final Identifier path;

    private final List<Integer> milestoneRealms;
    //{2,6}
    //technique 1 at realm 4
    //swap with technique 2.
    // resetting to realm 4 -> 2
    // real was 8 -> 6

    //technique 2 had a min requirement of 3
    //in the attempt to swap realm 4->2, 2 <3 fail to set
    //add warning
    private final List<String> techniqueFamilies;
    //bloodfeast
    //standard_demonic

    //enhance bloodfeast
    //standard_demonic

    //budhist technique
    //standard_budisht

    //demonic bodvista technique
    //standard_demonic,standard_budhist

    //technique compatible, if not compatible try force learn. it would reset cultivation. learn it
    //add warning
    private final Integer maxMajorRealm;
    private final Integer maxMinorRealm;
    private final int minMajorRealm;

    private final ProgressActionHolder holder;

    private final Map<Integer,MajorRealmDefinitionOverride> majorRealmOverrides;

    private final Optional<AscensionItemTooltipDefinition> itemTooltip;


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
            ProgressActionHolder holder,
            Map<Integer, MajorRealmDefinitionOverride> majorRealmOverrides) {
        this.name = name;
        this.description = description;
        this.path = path;
        this.holder = holder;
        this.milestoneRealms = milestoneRealms;
        this.techniqueFamilies = techniqueFamilies;
        this.majorRealmOverrides = majorRealmOverrides;
        this.maxMajorRealm = maxMajorRealm;
        this.maxMinorRealm = maxMinorRealm;
        this.minMajorRealm = minMajorRealm;
        this.itemTooltip = itemTooltip == null ? Optional.empty() : itemTooltip;
    }



    public ProgressActionHolder getHolder(){
        return holder;
    }
    public Map<Integer,MajorRealmDefinitionOverride> getMajorRealmOverrides(){
        return majorRealmOverrides;
    }

    public Optional<Integer> getHardCodedMaxMajorRealm(){
        return Optional.of(maxMajorRealm);
    }
    public Optional<Integer> getHardCodedMaxMinorRealm(){
        return Optional.of(maxMinorRealm);
    }
    public Optional<Integer> getHardCodedMinMajorRealm(){
        return Optional.of(minMajorRealm);
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
    public void onAdded(AscensionOriginSource source, TechniqueData data) {

        holder.run(source,CoreRegistries.TECHNIQUE_REGISTRY.get(source.getRegistryAccess()).getKey(this),data,ProgressDirection.UP);
    }
    //TODO UPDATE PROGRESSION TEST TO TAKE IN A TYPE CALLED REGISTRY_OBJECT_DATA AS CONTEXT DATA
    @Override
    public void onRemoved(AscensionOriginSource source, TechniqueData data) {
        holder.run(source,CoreRegistries.TECHNIQUE_REGISTRY.get(source.getRegistryAccess()).getKey(this),data,ProgressDirection.DOWN);
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
        if(majorRealmOverrides.containsKey(majorRealm) && majorRealmOverrides.get(majorRealm).hasNameOverride()){
            return majorRealmOverrides.get(majorRealm).getName();
        }
        Path pathInstance = CoreRegistries.safeAccess(CoreRegistries.PATH_REGISTRY,path,registryAccess);
        return pathInstance == null ? Component.empty() : pathInstance.getMajorRealmName(majorRealm);
    }

    @Override
    public Component getMinorRealmName(int majorRealm, int minorRealm, @Nullable TechniqueData techniqueData, RegistryAccess registryAccess) {
        if(majorRealmOverrides.containsKey(majorRealm) &&
                majorRealmOverrides.get(majorRealm).hasRealmOverride(minorRealm) &&
                majorRealmOverrides.get(majorRealm).getRealmOverride(minorRealm).hasNameOverride()){
            return majorRealmOverrides.get(majorRealm).getRealmOverride(minorRealm).getName();
        }
        Path pathInstance = CoreRegistries.safeAccess(CoreRegistries.PATH_REGISTRY,path,registryAccess);

        return pathInstance == null ? Component.empty() : pathInstance.getMinorRealmName(majorRealm,minorRealm);
    }

    @Override
    public Component getRealmName(int majorRealm, int minorRealm, @Nullable TechniqueData techniqueData, RegistryAccess registryAccess) {
        return Component.empty()
                .append(getMajorRealmName(majorRealm,techniqueData,registryAccess))
                .append("(")
                .append(getMinorRealmName(majorRealm,minorRealm,techniqueData,registryAccess))
                .append(")");
    }

    @Override
    public int getMaxMajorRealm(@Nullable TechniqueData techniqueData, RegistryAccess registryAccess) {
        if(maxMajorRealm != null)return maxMajorRealm;
        Path pathInstance = CoreRegistries.safeAccess(CoreRegistries.PATH_REGISTRY,path,registryAccess);

        return pathInstance == null ? 9: pathInstance.getMaxMajorRealm();
    }

    @Override
    public int getMaxMinorRealm(int majorRealm, @Nullable TechniqueData techniqueData, RegistryAccess registryAccess) {
        Path pathInstance = CoreRegistries.safeAccess(CoreRegistries.PATH_REGISTRY,path,registryAccess);
        if(majorRealm == getMaxMajorRealm(techniqueData,registryAccess)) return maxMinorRealm;
        return pathInstance == null ? 9: pathInstance.getMaxMinorRealm(majorRealm);
    }

    @Override
    public int getMinMajorRealm(RegistryAccess registryAccess) {
        return minMajorRealm;
    }

    @Override
    public double getMaxProgress(int majorRealm, int minorRealm, @Nullable TechniqueData techniqueData, RegistryAccess registryAccess) {
        if(majorRealmOverrides.containsKey(majorRealm) &&
                majorRealmOverrides.get(majorRealm).hasRealmOverride(minorRealm) &&
                majorRealmOverrides.get(majorRealm).getRealmOverride(minorRealm).hasProgressOverride()){
            return majorRealmOverrides.get(majorRealm).getRealmOverride(minorRealm).getProgress();
        }
        Path pathInstance = CoreRegistries.safeAccess(CoreRegistries.PATH_REGISTRY,path,registryAccess);

        return pathInstance == null ? 100 : pathInstance.getMaxProgress(majorRealm,minorRealm);
    }

    @Override
    public TribulationDefinition getTribulation(int majorRealm, int minorRealm, RegistryAccess access) {
        if(majorRealmOverrides.containsKey(majorRealm) &&
                majorRealmOverrides.get(majorRealm).hasRealmOverride(minorRealm) &&
                majorRealmOverrides.get(majorRealm).getRealmOverride(minorRealm).hasTribulationOverride()){
            return majorRealmOverrides.get(majorRealm).getRealmOverride(minorRealm).getTribulation(access);
        }
        Path pathInstance = CoreRegistries.safeAccess(CoreRegistries.PATH_REGISTRY,path,access);

        return pathInstance == null ? null : pathInstance.getTribulationDefinition(majorRealm,minorRealm,access);
    }

    @Override
    public boolean tryBreakthrough(LivingEntity entity, AscensionOriginSource source, int majorRealm, int minorRealm, double progress, @Nullable TechniqueData techniqueData) {
        if(source.getPathData(getPath()).isBreakingThrough()) return false;
        double maxProgress = getMaxProgress(majorRealm,minorRealm,techniqueData,source.getRegistryAccess());
        double maxMajorRealm = getMaxMajorRealm(techniqueData,source.getRegistryAccess());
        double maxMinorRealm = getMaxMinorRealm(majorRealm,techniqueData,source.getRegistryAccess());
        //TODO trigger breakthrough here

        boolean canBreakthrough = maxProgress <= progress &&
                (
                        (maxMinorRealm > minorRealm && maxMajorRealm >= majorRealm) ||
                                (maxMinorRealm <= minorRealm && maxMajorRealm > majorRealm) );
        if(!canBreakthrough) return false;


        TribulationDefinition definition = getTribulation(majorRealm,minorRealm,source.getRegistryAccess());
        if(definition == null) return true;
        UUID id =   TribulationManager.getInstance().triggerTribulation(definition,entity);
        source.getPathData(getPath()).setBreakthroughTribulation(
                id,
                source.getRegistryAccess()
        );

        TribulationManager.getInstance().setTribulationConsumer(id,(tribulationDefinition,data)->{
            PathData pathData = source.getPathData(getPath());

            pathData.handleRealmChange(
                source,pathData.getMajorRealm()+1,0);
            pathData.setProgress(0);
            pathData.setCompletedTribulation(source,pathData.getMajorRealm(),pathData.getMinorRealm(),tribulationDefinition,data);
        });
        return false;

    }

    @Override
    public void onRealmUp(AscensionOriginSource source, TechniqueData techniqueData) {
        holder.run(source,CoreRegistries.TECHNIQUE_REGISTRY.get(source.getRegistryAccess()).getKey(this),techniqueData,ProgressDirection.UP);
    }

    @Override
    public void onRealmDown(AscensionOriginSource source, TechniqueData techniqueData) {
        holder.run(source,CoreRegistries.TECHNIQUE_REGISTRY.get(source.getRegistryAccess()).getKey(this),techniqueData,ProgressDirection.DOWN);
    }

    @Override
    public TechniqueData newData() {
        return new EmptyTechniqueData();
    }

    @Override
    public TechniqueData loadData(ValueInput input) {
        return new EmptyTechniqueData();
    }

    @Override
    public TechniqueData loadData(ByteBuf buf) {
        return new EmptyTechniqueData();
    }
}
