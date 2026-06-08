package net.zic.ascension.core.path;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.api.core.CoreRegistries;
import net.zic.ascension.api.core.path.Path;
import net.zic.ascension.api.core.path.PathData;
import net.zic.ascension.api.core.source.OriginSource;
import net.zic.ascension.api.core.technique.Technique;
import net.zic.ascension.api.core.technique.TechniqueData;
import net.zic.zenithlib.nbt.NbtHelpers;
import net.zic.zenithlib.network.ByteBufHelpers;
import org.apache.logging.log4j.core.Core;

import java.util.*;

public class SimplePathData implements PathData {
    private final Identifier path;


    private int minorRealm;
    private double progress;
    private boolean cultivating;
    private boolean breakingThrough;

    //holds the technique used/being used to cultivate that realm
    private final ArrayList<Identifier> techniqueHistory = new ArrayList<>();

    //holds the technique data for each "loaded" technique
    private final HashMap<Identifier,TechniqueData> techniqueData = new HashMap<>();

    public SimplePathData(Identifier path) {
        this.path = path;
    }

    @Override
    public Identifier getPath() {
        return path;
    }

    @Override
    public int getMajorRealm() {
        if(techniqueHistory.isEmpty()) techniqueHistory.add(null);
        return techniqueHistory.size() -1;
    }

    @Override
    public int getMinorRealm() {
        return minorRealm;
    }

    @Override
    public int getMaxMinorRealm(int majorRealm, RegistryAccess access) {
        Technique technique = CoreRegistries.safeAccess(CoreRegistries.TECHNIQUE_REGISTRY,getCurrentTechnique(),access);
        if(technique == null){
            Path pathInstance = CoreRegistries.safeAccess(CoreRegistries.PATH_REGISTRY,getPath(),access);
            return pathInstance == null ? 0 : pathInstance.getMaxMinorRealm(majorRealm);
        }

        return technique.getMaxMinorRealm(majorRealm,getCurrentTechniqueData(),access);
    }

    @Override
    public int getMaxMajorRealm(RegistryAccess access) {
        Technique technique = CoreRegistries.safeAccess(CoreRegistries.TECHNIQUE_REGISTRY,getCurrentTechnique(),access);
        if(technique == null){
            Path pathInstance = CoreRegistries.safeAccess(CoreRegistries.PATH_REGISTRY,getPath(),access);
            return pathInstance == null ? 0 : pathInstance.getMaxMajorRealm();
        }

        return technique.getMaxMajorRealm(getCurrentTechniqueData(),access);
    }

    @Override
    public double getProgress() {
        return progress;
    }

    @Override
    public double getMaxProgress(int majorRealm, int minorRealm, RegistryAccess access) {
        Technique technique = CoreRegistries.safeAccess(CoreRegistries.TECHNIQUE_REGISTRY,getCurrentTechnique(),access);
        if(technique == null){
            Path pathInstance = CoreRegistries.safeAccess(CoreRegistries.PATH_REGISTRY,getPath(),access);
            return pathInstance == null ? 0 : pathInstance.getMaxProgress(majorRealm,minorRealm);
        }

        return technique.getMaxProgress(majorRealm,minorRealm,getCurrentTechniqueData(),access);
    }

    @Override
    public boolean isCultivating() {
        return cultivating;
    }

    @Override
    public boolean isBreakingThrough() {
        return breakingThrough;
    }

    @Override
    public Identifier getCurrentTechnique() {
        if(techniqueHistory.isEmpty()){
            techniqueHistory.add(null);
        }
        return techniqueHistory.getLast();
    }

    @Override
    public TechniqueData getCurrentTechniqueData() {

        return (getCurrentTechnique() == null ||!techniqueData.containsKey(getCurrentTechnique())) ? null : techniqueData.get(getCurrentTechnique()) ;
    }

    @Override
    public ArrayList<Identifier> getTechniqueHistory() {
        return techniqueHistory;
    }

    @Override
    public Collection<Identifier> getUniqueTechniques() {
        return new HashSet<>(techniqueHistory);
    }

    @Override
    public boolean hasCultivatedTechnique(Identifier technique) {
        return techniqueHistory.contains(technique);
    }

    @Override
    public Identifier getTechniqueForRealm(int majorRealm) {
        return (majorRealm > getMajorRealm()) ? null : techniqueHistory.get(majorRealm);
    }

    @Override
    public TechniqueData getTechniqueData(Identifier technique) {
        return techniqueData.get(technique);
    }

    @Override
    public Collection<Integer> getCultivatedRealms(Identifier technique) {
        ArrayList<Integer> realms = new ArrayList<>();
        for(int realm = 0; realm < techniqueHistory.size(); realm ++) if(technique.equals(techniqueHistory.get(realm))) realms.add(realm);
        return realms;
    }

    @Override
    public Component getMajorRealmName(int majorRealm, RegistryAccess access) {
        Technique technique = CoreRegistries.safeAccess(CoreRegistries.TECHNIQUE_REGISTRY,getTechniqueForRealm(majorRealm),access);
        Path pathInstance = CoreRegistries.safeAccess(CoreRegistries.PATH_REGISTRY,getPath(),access);
        return technique == null ? pathInstance.getMajorRealmName(majorRealm) : technique.getMajorRealmName(majorRealm,getTechniqueData(getTechniqueForRealm(majorRealm)),access);
    }

    @Override
    public Component getMinorRealmName(int majorRealm, int minorRealm, RegistryAccess access) {
        Technique technique = CoreRegistries.safeAccess(CoreRegistries.TECHNIQUE_REGISTRY,getTechniqueForRealm(majorRealm),access);
        Path pathInstance = CoreRegistries.safeAccess(CoreRegistries.PATH_REGISTRY,getPath(),access);
        return technique == null ? pathInstance.getMinorRealmName(majorRealm,minorRealm) :
                technique.getMinorRealmName(majorRealm,minorRealm,getTechniqueData(getTechniqueForRealm(majorRealm)),access);

    }

    @Override
    public Component getRealmName(int majorRealm, int minorRealm, RegistryAccess access) {
        Technique technique = CoreRegistries.safeAccess(CoreRegistries.TECHNIQUE_REGISTRY,getTechniqueForRealm(majorRealm),access);
        Path pathInstance = CoreRegistries.safeAccess(CoreRegistries.PATH_REGISTRY,getPath(),access);
        return technique == null ? pathInstance.getRealmName(majorRealm,minorRealm) :
                technique.getRealmName(majorRealm,minorRealm,getTechniqueData(getTechniqueForRealm(majorRealm)),access);

    }

    @Override
    public void setMajorRealm(int majorRealm,OriginSource source) {
        if(majorRealm > techniqueHistory.size()-1){
            for(int i = techniqueHistory.size(); i <=majorRealm;i++){
                techniqueHistory.add(null);
            }
        }else {
            for(int i = techniqueHistory.size()-1;i>majorRealm;i--){

                Identifier techniqueId = techniqueHistory.removeLast();
                Technique technique = CoreRegistries.safeAccess(CoreRegistries.TECHNIQUE_REGISTRY,techniqueId,source.getRegistryAccess());
                if(technique == null) continue;
                if(!hasCultivatedTechnique(techniqueId)){
                    TechniqueData data = techniqueData.remove(techniqueId);
                    technique.onRemoved(source,data);
                }
            }
        }
    }


    @Override
    public void setMinorRealm(int minorRealm) {
        this.minorRealm = minorRealm;
    }

    @Override
    public void setProgress(double progress) {
        this.progress =progress;
    }

    @Override
    public boolean setCurrentTechnique(Identifier technique, OriginSource source) {
        if(technique == null) return setCurrentTechnique(null,null,source);
        Technique techniqueInstance = CoreRegistries.safeAccess(CoreRegistries.TECHNIQUE_REGISTRY,technique,source.getRegistryAccess());
        return techniqueInstance != null && setCurrentTechnique(technique, techniqueInstance.newData(), source);
    }

    @Override
    public boolean setCurrentTechnique(Identifier technique, TechniqueData data, OriginSource source) {
        if(getCurrentTechnique() == null && technique == null) return true;
        if(getCurrentTechnique() != null && technique != null && getCurrentTechnique().equals(technique))return true;

        /*
            if current technique != null, it means we must first handle the logic to remove that technique
            to remove it we first get the highest realm between
                1. realm with different technique (e.g if technique 1 is realms 5 6 7 and technique 2 is 0-4. we can drop to 5 0 and remove the technique
                2. first milestone realm (e.g if we are m 6 and milestone realm is 5 it means we can drop to realm 5 0 and remove the technique


            then we run handleRealmChange IF the target realm is != current realm

            before this we run broadcastTechnqiueRemovedAttempt()
            and only run realm change if that is true

            then if the above ran fine we add our new technique
            then we validate the technique family of the new technique (if it is not valid we need to fully reset the cultivation)


            then after we have done all that we validate the min realm required

            TODO have transfer items warn players about compatability issues



         */

        //remove existing technique
        if(getCurrentTechnique() != null){
            int targetRealm = getMaxMilestoneRealm(source.getRegistryAccess());
            if(targetRealm != getMajorRealm()){
                handlerRealmChange(source,targetRealm,0);
            }
            Identifier old = getCurrentTechnique();
            TechniqueData oldData = techniqueData.get(old);
            if(old != null){
                boolean result = source.broadcastTechniqueRemovedAttempt(old,oldData);
                if(!result) return false;

                techniqueHistory.removeLast();
                techniqueHistory.add(null);
                if(!techniqueHistory.contains(old)){
                    techniqueData.remove(old);
                    Technique oldInstance = CoreRegistries.safeAccess(CoreRegistries.TECHNIQUE_REGISTRY,old,source.getRegistryAccess());
                    if(oldInstance != null){
                        oldInstance.onRemoved(source,oldData);
                    }
                }
                source.broadcastTechniqueRemoved(old,oldData);

            }
        }
        if(technique == null) return true;// we are just trying to remove the previous technique
        Technique techniqueInstance = CoreRegistries.safeAccess(CoreRegistries.TECHNIQUE_REGISTRY,technique,source.getRegistryAccess());
        if(techniqueInstance == null) return false; // the technique does not exist
        dropToValidFamilyRealm(source,technique);

        if(techniqueInstance.getMinMajorRealm(source.getRegistryAccess()) > getMajorRealm()) return false;

        boolean result = source.broadcastTechniqueAddedAttempt(technique,data);
        if(!result) return false;
        techniqueHistory.removeLast();
        if (!hasCultivatedTechnique(technique)){
            techniqueInstance.onAdded(source,data);
            techniqueHistory.add(technique);
            techniqueData.put(technique,data);
        }else techniqueHistory.add(technique);

        source.broadcastTechniqueAdded(technique,techniqueData.get(technique));
        return true;
    }



    @Override
    public void simulateProgression(OriginSource source) {

        ArrayList<Identifier> cachedTechniqueHistory = new ArrayList<>(techniqueHistory);
        HashMap<Identifier,TechniqueData> cachedTechniqueData = new HashMap<>(techniqueData);
        int cachedMinorRealm = minorRealm;
        double cachedProgress = progress;

        minorRealm = 0;
        progress = 0;
        techniqueHistory.clear();
        techniqueData.clear();

        while(cachedTechniqueHistory.size() > 1){
            Identifier technique = cachedTechniqueHistory.removeFirst();
            TechniqueData data = cachedTechniqueData.get(technique);
            setCurrentTechnique(technique,data,source);
            handlerRealmChange(source,getMajorRealm()+1,0);
        }
        Identifier currentTechnique = cachedTechniqueHistory.removeFirst();
        setCurrentTechnique(currentTechnique,cachedTechniqueData.get(currentTechnique),source);
        handlerRealmChange(source,getMajorRealm(),cachedMinorRealm);
        setProgress(cachedProgress);
    }

    @Override
    public void removeFromSource(OriginSource source) {
        ArrayList<Identifier> cachedTechniqueHistory = new ArrayList<>(techniqueHistory);
        HashMap<Identifier,TechniqueData> cachedTechniqueData = new HashMap<>(techniqueData);
        int cachedMinorRealm = minorRealm;
        double cachedProgress = progress;

        handlerRealmChange(source,0,0);

        setCurrentTechnique(null,source);

        techniqueHistory.clear();
        techniqueHistory.addAll(cachedTechniqueHistory);
        techniqueData.clear();
        techniqueData.putAll(cachedTechniqueData);
        minorRealm = cachedMinorRealm;
        progress = cachedProgress;
    }



    @Override
    public void write(ValueOutput output) {

        //write the current realm
        output.putInt("minor_realm",getMinorRealm());
        output.putDouble("progress",getProgress());

        //write technique history
        ValueOutput.ValueOutputList techniqueHistoryOutput = output.childrenList("technique_history");
        for(Identifier technique : techniqueHistory) techniqueHistoryOutput.addChild().putString("technique",(technique == null ? "none" : technique.toString()));

        //write technique data

        ValueOutput.ValueOutputList techniqueDataOutput = output.childrenList("technique_data");
        for(Identifier technique : techniqueData.keySet()){
            ValueOutput dataOutput = techniqueDataOutput.addChild();
            dataOutput.putString("technique",(technique == null ? "none" : technique.toString()));
            TechniqueData data = techniqueData.get(technique);
            if(data != null) data.write(dataOutput.child("data"));
        }

    }

    public void load(ValueInput input,RegistryAccess registryAccess){

        //important note, here we should as early as possible trim error data before simulating
        //for this reason directly add to technique history FIRST, the moment we hit an error stop
        //then when adding data check if the technique is Cultivated, if so add otherwise remove
        minorRealm = input.getIntOr("minor_realm",0);
        progress = input.getDoubleOr("progress",0);

        ValueInput.ValueInputList techniqueHistoryInput = input.childrenListOrEmpty("technique_history");
        for(ValueInput techniqueInput : techniqueHistoryInput){
            String rawTechnique = techniqueInput.getStringOr("technique","none");
            if(rawTechnique.equals("none")){
                techniqueHistory.add(null);
                continue;
            }
            Identifier technique = Identifier.parse(rawTechnique);

            // there was a problem with the technique, cut cultivation after this point
            if(CoreRegistries.safeAccess(CoreRegistries.TECHNIQUE_REGISTRY,technique,registryAccess) == null) break;

            techniqueHistory.add(technique);
        }

        ValueInput.ValueInputList techniqueDataInput = input.childrenListOrEmpty("technique_data");
        for(ValueInput techniqueInput : techniqueDataInput){
            String rawTechnique = techniqueInput.getStringOr("technique","none");
            if(rawTechnique.equals("none")){
                continue;
            }
            Identifier technique = Identifier.parse(rawTechnique);
            Technique techniqueInstance = CoreRegistries.safeAccess(CoreRegistries.TECHNIQUE_REGISTRY,technique,registryAccess);

            if(techniqueInstance == null) continue;

            //was removed so do not load
            if(!techniqueHistory.contains(technique)) continue;

            TechniqueData data = techniqueInstance.loadData(techniqueInput.childOrEmpty("data"));

            techniqueData.put(technique,data);
        }
    }

    @Override
    public void encode(ByteBuf buf) {
        buf.writeInt(getMinorRealm());
        buf.writeDouble(progress);

        ByteBufHelpers.encodeCollection(techniqueHistory,buf,(id,byteBuf)->{
            byteBuf.writeBoolean(id != null);
            if(id != null) ByteBufHelpers.encodeIdentifier(id,byteBuf);
        });

        ByteBufHelpers.encodeMap(techniqueData,ByteBufHelpers::encodeIdentifier,(data,byteBuf)->{
            byteBuf.writeBoolean(data != null);
            if(data != null)data.encode(byteBuf);
        },buf);
    }
    public void decode(ByteBuf buf){

    }
}
