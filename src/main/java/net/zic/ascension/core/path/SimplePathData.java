package net.zic.ascension.core.path;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.api.core.CoreRegistries;
import net.zic.ascension.api.core.path.PathData;
import net.zic.ascension.api.core.source.OriginSource;
import net.zic.ascension.api.core.technique.Technique;
import net.zic.ascension.api.core.technique.TechniqueData;

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
        return 0;//TODO
    }

    @Override
    public int getMaxMajorRealm(RegistryAccess access) {
        return 0;//TODO
    }

    @Override
    public double getProgress() {
        return progress;
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
        return techniqueHistory.getLast();
    }

    @Override
    public TechniqueData getCurrentTechniqueData() {
        return (techniqueHistory.getLast() == null ||!techniqueData.containsKey(techniqueHistory.getLast())) ? null : techniqueData.get(techniqueHistory.getLast()) ;
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
        for(int realm = 0; realm <= techniqueHistory.size(); realm ++) if(technique.equals(techniqueHistory.get(realm))) realms.add(realm);
        return realms;
    }

    @Override
    public void setMajorRealm(int majorRealm) {
        if(majorRealm > techniqueHistory.size()-1){
            for(int i = techniqueHistory.size(); i <=majorRealm;i++){
                techniqueHistory.add(null);
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
        //TODO
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
        techniqueHistory.add(technique);
        if (!hasCultivatedTechnique(technique)){
            techniqueInstance.onAdded(source,data);
            techniqueData.put(technique,data);
        }
        source.broadcastTechniqueAdded(technique,techniqueData.get(technique));
        return true;
    }

    @Override
    public void updateTechnique(OriginSource source) {
        if(techniqueHistory.size() >getMajorRealm()+1){
            for(int i = techniqueHistory.size()-1;i>getMajorRealm();i--){
                Identifier toBeRemoved = techniqueHistory.removeLast();
                if(getCultivatedRealms(toBeRemoved).isEmpty()){
                    //Technique is now no longer in the history
                    Technique technique = CoreRegistries.safeAccess(CoreRegistries.TECHNIQUE_REGISTRY,toBeRemoved,source.getRegistryAccess());
                    if(technique == null) continue;
                    technique.onRemoved(source,techniqueData.remove(toBeRemoved));
                }
            }
        }
    }

    @Override
    public void simulateProgression(OriginSource source, RegistryAccess access) {
        //TODO
        //important notes, create a cache of everything, then simulate every major realm
    }

    @Override
    public void removeFromSource(OriginSource source, RegistryAccess access) {
        //TODO
        //same as simulate but in reverse, cacheEverything, handle realm to 0,0, remove final technique then reinstate everything
    }



    @Override
    public void write(ValueOutput output) {
        //TODO
    }

    public void load(ValueInput input){
        //TODO
        //important note, here we should as early as possible trim error data before simulating
        //for this reason directly add to technique history FIRST, the moment we hit an error stop
        //then when adding data check if the technique is Cultivated, if so add otherwise remove
    }

    @Override
    public void encode(ByteBuf buf) {

    }
}
