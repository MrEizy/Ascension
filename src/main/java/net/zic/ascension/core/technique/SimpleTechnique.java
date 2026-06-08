package net.zic.ascension.core.technique;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.core.CoreRegistries;
import net.zic.ascension.api.core.path.Path;
import net.zic.ascension.api.core.progression.ProgressAction;
import net.zic.ascension.api.core.progression.ProgressActionCondition;
import net.zic.ascension.api.core.progression.ProgressActionHolder;
import net.zic.ascension.api.core.progression.ProgressDirection;
import net.zic.ascension.api.core.source.OriginSource;
import net.zic.ascension.api.core.technique.Technique;
import net.zic.ascension.api.core.technique.TechniqueData;
import net.zic.ascension.api.core.technique.realm_change.RealmChangeAction;
import net.zic.ascension.api.core.technique.realm_change.RealmChangeActionCondition;
import net.zic.ascension.api.datapack.technique.TechniqueType;
import net.zic.ascension.datapack.technique.AscensionTechniqueTypes;
import org.jspecify.annotations.Nullable;

import javax.swing.text.html.Option;
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
    private final int minMajorRealm;

    private final ProgressActionHolder holder;

    private final Map<Integer,MajorRealmNames> majorRealmOverrides;


    public SimpleTechnique(
            Component name,
            Component description,
            Identifier path,
            List<Integer> milestoneRealms,
            List<String> techniqueFamilies,
            Integer maxMajorRealm,
            int minMajorRealm,
            Map<Identifier,List<Identifier>> holder,
            Map<Integer, MajorRealmNames> majorRealmOverrides) {
        this.name = name;
        this.description = description;
        this.path = path;
        this.holder = ProgressActionHolder.fromMap(holder);
        this.milestoneRealms = milestoneRealms;
        this.techniqueFamilies = techniqueFamilies;
        this.majorRealmOverrides = majorRealmOverrides;
        this.maxMajorRealm = maxMajorRealm;
        this.minMajorRealm = minMajorRealm;
    }

    public record MajorRealmNames(Component name, Map<Integer, Component> minorRealmOverrides) {
        public boolean hasName(){return name != null;}
        public boolean hasMinorRealmName(int minorRealm){return minorRealmOverrides.containsKey(minorRealm);}

        public Component getName(int majorRealm,Identifier path, RegistryAccess registryAccess){
            if(hasName()) return name;
            Path pathInstance = CoreRegistries.safeAccess(CoreRegistries.PATH_REGISTRY,path,registryAccess);
            return pathInstance == null ? Component.empty() : pathInstance.getMajorRealmName(majorRealm);
        }
        public Component getMinorRealmName(int majorRealm,int minorRealm,Identifier path,RegistryAccess registryAccess){
            if(hasMinorRealmName(minorRealm)) return minorRealmOverrides.get(minorRealm);
            Path pathInstance = CoreRegistries.safeAccess(CoreRegistries.PATH_REGISTRY,path,registryAccess);

            return pathInstance == null ? Component.empty() : pathInstance.getMinorRealmName(majorRealm,minorRealm);
        }

        public static final Codec<MajorRealmNames> CODEC = RecordCodecBuilder.create(
                instance->
                        instance.group(
                            ComponentSerialization.CODEC.optionalFieldOf("name").forGetter(names-> Optional.of(names.name)),
                            Codec.unboundedMap(Codec.INT,ComponentSerialization.CODEC).fieldOf("minor_realms").forGetter(MajorRealmNames::minorRealmOverrides)
                    ).apply(instance,(name,names)->new MajorRealmNames(name.orElse(null),names))
        );
    }

    public Map<Identifier,List<Identifier>> getListeners(){
        return holder.listeners();
    }
    public Map<Integer,MajorRealmNames> getMajorRealmOverrides(){
        return majorRealmOverrides;
    }

    public Optional<Integer> getHardCodedMaxMinorRealm(){
        return Optional.of(maxMajorRealm);
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
    public void onAdded(OriginSource source, TechniqueData data) {

        holder.run(source,CoreRegistries.TECHNIQUE_REGISTRY.get(source.getRegistryAccess()).getKey(this),data,ProgressDirection.UP);
    }
    //TODO UPDATE PROGRESSION TEST TO TAKE IN A TYPE CALLED REGISTRY_OBJECT_DATA AS CONTEXT DATA
    @Override
    public void onRemoved(OriginSource source, TechniqueData data) {
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
        if(majorRealmOverrides.containsKey(majorRealm)){
            return majorRealmOverrides.get(majorRealm).getName(majorRealm,getPath(),registryAccess);
        }
        Path pathInstance = CoreRegistries.safeAccess(CoreRegistries.PATH_REGISTRY,path,registryAccess);
        return pathInstance == null ? Component.empty() : pathInstance.getMajorRealmName(majorRealm);
    }

    @Override
    public Component getMinorRealmName(int majorRealm, int minorRealm, @Nullable TechniqueData techniqueData, RegistryAccess registryAccess) {
        if(majorRealmOverrides.containsKey(majorRealm)){
            return majorRealmOverrides.get(majorRealm).getMinorRealmName(majorRealm,minorRealm,path,registryAccess);
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

        return pathInstance == null ? 9: pathInstance.getMaxMinorRealm(majorRealm);
    }

    @Override
    public int getMinMajorRealm(RegistryAccess registryAccess) {
        return minMajorRealm;
    }

    @Override
    public double getMaxProgress(int majorRealm, int minorRealm, @Nullable TechniqueData techniqueData, RegistryAccess registryAccess) {
        return 100; //TODO
    }

    @Override
    public boolean canBreakthrough(OriginSource source, int majorRealm, int minorRealm, double progress, @Nullable TechniqueData techniqueData) {
        double maxProgress = getMaxProgress(majorRealm,minorRealm,techniqueData,source.getRegistryAccess());
        double maxMajorRealm = getMaxMajorRealm(techniqueData,source.getRegistryAccess());
        double maxMinorRealm = getMaxMinorRealm(majorRealm,techniqueData,source.getRegistryAccess());

        /*
            first check if progress is max
            then check if it is a minor realm breakthrough where major realm < max
            (if for example our max is 12 we can breakthrough into 12 but not progress in it)
            if it is a major realm breakthrough ensure the new major realm is accessible
         */
        return maxProgress <= progress && ((maxMinorRealm > minorRealm && maxMajorRealm > majorRealm) || (maxMinorRealm <= minorRealm && maxMajorRealm > majorRealm));

    }

    @Override
    public void onRealmUp(OriginSource source, TechniqueData techniqueData) {
        holder.run(source,CoreRegistries.TECHNIQUE_REGISTRY.get(source.getRegistryAccess()).getKey(this),techniqueData,ProgressDirection.UP);
    }

    @Override
    public void onRealmDown(OriginSource source, TechniqueData techniqueData) {
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
