package net.zic.ascension.api.core.path;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.core.CoreRegistries;
import net.zic.ascension.api.core.source.OriginSource;
import net.zic.ascension.api.core.technique.Technique;
import net.zic.ascension.api.core.technique.TechniqueData;

import java.util.ArrayList;
import java.util.Collection;

//TODO decide if i want to pass source or entity wrapper
public interface PathData {
    //──Getters────────────────────────────────────────────────────────
    Identifier getPath();

    int getMajorRealm();
    int getMinorRealm();

    int getMaxMinorRealm(int majorRealm, RegistryAccess access);
    int getMaxMajorRealm(RegistryAccess access);

    double getProgress();

    boolean isCultivating();

    boolean isBreakingThrough();

    Identifier getCurrentTechnique();

    TechniqueData getCurrentTechniqueData();

    ArrayList<Identifier> getTechniqueHistory();

    Collection<Identifier> getUniqueTechniques();

    boolean hasCultivatedTechnique(Identifier technique);

    Identifier getTechniqueForRealm(int majorRealm);

    TechniqueData getTechniqueData(Identifier technique);

    Collection<Integer> getCultivatedRealms(Identifier technique);

    //──Setters────────────────────────────────────────────────────────
    void setMajorRealm(int majorRealm);
    void setMinorRealm(int minorRealm);

    void setProgress(double progress);

    //only call onRemoved/onAdded if the technique is added for the first time or removed for the final time
    boolean setCurrentTechnique(Identifier technique,OriginSource source);
    boolean setCurrentTechnique(Identifier technique,TechniqueData data,OriginSource source);

    //updates the current technique, will call remove technique
    //if the new technique being "slotted" is different AND the to be removed is being FULLY removed
    //should also remove any and all technique history entries occurring after the current realm
    void updateTechnique(OriginSource source);
    //──Logic────────────────────────────────────────────────────────
    default void onRealmUp(OriginSource source) {
        if(getCurrentTechnique() == null) return;
        Technique technique = CoreRegistries.safeAccess(CoreRegistries.TECHNIQUE_REGISTRY,getCurrentTechnique(),source.getRegistryAccess());
        if(technique == null) return;//TODO add log here
        technique.onRealmUp(source,getCurrentTechniqueData());
    }
    default void onRealmDown(OriginSource source){
        Technique technique = CoreRegistries.safeAccess(CoreRegistries.TECHNIQUE_REGISTRY,getCurrentTechnique(),source.getRegistryAccess());
        if(technique == null) return;//TODO add log here
        technique.onRealmDown(source,getCurrentTechniqueData());
    }

    //takes in a potential realm change, and breaks it down into individual steps
    //TODO write default implementation
    default void handlerRealmChange(OriginSource source,int newMajorRealm,int newMinorRealm){
        int oldMajorRealm = getMajorRealm();
        int oldMinorRealm = getMinorRealm();
        if(oldMajorRealm < newMajorRealm || (oldMajorRealm == newMajorRealm && newMinorRealm>oldMinorRealm)){
            newMajorRealm = Math.min(newMajorRealm,getMaxMajorRealm(source.getRegistryAccess()));
            newMinorRealm = Math.min(newMinorRealm,getMaxMinorRealm(newMajorRealm,source.getRegistryAccess()));
            if(newMajorRealm != oldMajorRealm) {

                //loop through each major realm
                for(int i = oldMinorRealm+1;i <= getMaxMinorRealm(oldMajorRealm,source.getRegistryAccess());i++){
                    setMinorRealm(i);
                    onRealmUp(source);
                }
                for(int i = oldMajorRealm+1;i<newMajorRealm;i++){


                    setMajorRealm(i);
                    setMinorRealm(0);
                    onRealmUp(source);
                    for(int j = 1;j <= getMaxMinorRealm(oldMajorRealm,source.getRegistryAccess());j++){
                        setMinorRealm(j);
                        onRealmUp(source);
                    }
                }
                setMajorRealm(newMajorRealm);
                setMinorRealm(0);
                onRealmUp(source);

                for(int i =1;i<=newMinorRealm;i++){
                    setMinorRealm(i);
                    onRealmUp(source);
                }
            }else{

                for(int i = oldMinorRealm+1;i<=newMinorRealm;i++){
                    setMinorRealm(i);
                    onRealmUp(source);
                }
            }

        }else{

            newMajorRealm = Math.max(newMajorRealm,0);
            newMinorRealm = Math.max(newMinorRealm,0);

            if(newMajorRealm != oldMajorRealm){
                for(int i = oldMajorRealm-1;i>0;i--){
                    setMinorRealm(i);
                    onRealmDown(source);
                }

                for(int i = oldMajorRealm-1;i>newMajorRealm;i--){

                    updateTechnique(source);
                    setMajorRealm(i);
                    setMinorRealm(getMaxMinorRealm(getMajorRealm(),source.getRegistryAccess()));
                    onRealmDown(source);

                    for(int j = getMinorRealm()-1;j>0;j--){
                        setMinorRealm(j);
                        onRealmDown(source);
                    }
                }
                updateTechnique(source);

                setMajorRealm(newMajorRealm);
                setMinorRealm(getMaxMinorRealm(getMajorRealm(),source.getRegistryAccess()));
                onRealmDown(source);

                for(int i =getMinorRealm()-1;i>=newMinorRealm;i--){
                    setMinorRealm(i);
                    onRealmDown(source);
                }
            }else{

                for(int i = oldMinorRealm-1;i>=newMinorRealm;i--){
                    setMinorRealm(i);
                    onRealmDown(source);
                }

            }
        }
    }

    /**
     * gets the first of either different technique OR milestone realm
     * @param access allows us to access datapack registry
     * @return returns the major realm the source will be dropped to(e.g 5 = major 5 minor 0)
     */
    default int getMaxMilestoneRealm(RegistryAccess access){
        Identifier currentTechnique = getCurrentTechnique() == null ? Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID,"none") :getCurrentTechnique();
        Technique technique = CoreRegistries.safeAccess(CoreRegistries.TECHNIQUE_REGISTRY,currentTechnique,access);
        int currentRealm = getTechniqueHistory().size()-1;
        boolean hitCondition = false;
        while(!hitCondition){
            currentRealm--;
            if(!currentTechnique.equals(getTechniqueForRealm(currentRealm))){
                currentRealm++; //move to the realm ABOVE the other technique
                hitCondition = true;
            }else if(technique != null && technique.getMilestoneRealms().contains(currentRealm)){
                hitCondition = true;
            }
        }
        return currentRealm;
    }

    //drops the current realm until either the technique before has the same family or majorRealm = 0
    //TODO needs more testing
    default void dropToValidFamilyRealm(OriginSource source,Identifier targetTechnique){
        Technique technique = CoreRegistries.safeAccess(CoreRegistries.TECHNIQUE_REGISTRY,targetTechnique,source.getRegistryAccess());
        if(technique == null) return;
        Collection<String> families = technique.getTechniqueFamilies();
        boolean hitCondition = false;
        while(!hitCondition && getMajorRealm() > 0){
            int peekRealm = getMajorRealm() -1;

            Identifier techniqueForRealm = getTechniqueForRealm(peekRealm);
            Technique instance = CoreRegistries.safeAccess(CoreRegistries.TECHNIQUE_REGISTRY,techniqueForRealm,source.getRegistryAccess());
            if(instance == null){
                handlerRealmChange(source,peekRealm,0);
                continue;
            }
            for(String family : families){
                if(instance.getTechniqueFamilies().contains(family)){
                    hitCondition = true;
                    break;
                }
            }
            //no valid families lower realm and remove technique
            handlerRealmChange(source,peekRealm,0);
            setCurrentTechnique(null,source); //may also fully drop us down to 0 0
        }
    }
    //caches the current state then simulates applying it
    void simulateProgression(OriginSource source, RegistryAccess access);

    //removes it from a specific source but should still save its data (mainly used when transferring path data)
    void removeFromSource(OriginSource source,RegistryAccess access);


    //──Save Data────────────────────────────────────────────────────────

    void write(ValueOutput output);
    //──Network────────────────────────────────────────────────────────
    void encode(ByteBuf buf);
}
