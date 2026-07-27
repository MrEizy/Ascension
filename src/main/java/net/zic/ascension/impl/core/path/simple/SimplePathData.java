package net.zic.ascension.impl.core.path.simple;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.path.Path;
import net.zic.ascension.api.ascension.core.path.PathData;
import net.zic.ascension.api.ascension.core.path.Realm;

import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.ascension.core.technique.Technique;
import net.zic.ascension.api.ascension.core.technique.TechniqueData;
import net.zic.ascension.api.ascension.core.tribulation.TribulationData;
import net.zic.ascension.api.ascension.core.tribulation.TribulationDefinition;
import net.zic.ascension.api.ascension.core.tribulation.TribulationManager;
import net.zic.ascension.api.ascension.datapack.tribulation.TribulationType;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.impl.core.path.CompletedTribulation;
import net.zic.zenithlib.network.ByteBufHelpers;

import java.util.*;

public class SimplePathData implements PathData {
    private final Identifier path;


    private int minorRealm;
    private double progress;
    private boolean cultivating;

    private UUID breakthroughInstance; //TODO implement
    //holds the technique used/being used to cultivate that realm
    private final ArrayList<Identifier> techniqueHistory = new ArrayList<>();

    //holds the technique data for each "loaded" technique
    private final HashMap<Identifier,TechniqueData> techniqueData = new HashMap<>();

    //TODO update to be a record of techniqueSource(if null assume path) that can be used to validate
    private final HashMap<Realm, CompletedTribulation> tribulationHistory = new HashMap<>();
    private final HashMap<Realm, CompletedTribulation> cachedTribulationHistory = new HashMap<>();
    private UUID tribulationId;
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
        return tribulationId != null && TribulationManager.getInstance().hasTribulation(tribulationId);
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
    public TribulationData getCompletedTribulationData(int majorRealm, int minorRealm) {
        return tribulationHistory.containsKey(new Realm(majorRealm,minorRealm)) ?
                tribulationHistory.get(new Realm(majorRealm,minorRealm)).data() : null;
    }

    @Override
    public TribulationDefinition getCompletedTribulationDefinition(int majorRealm, int minorRealm) {
        return tribulationHistory.containsKey(new Realm(majorRealm,minorRealm)) ?
                tribulationHistory.get(new Realm(majorRealm,minorRealm)).definition() : null;
    }

    @Override
    public Collection<Realm> getCompletedTribulationRealms() {
        return tribulationHistory.keySet();
    }

    @Override
    public UUID getBreakthroughTribulation() {
        return tribulationId;
    }

    @Override
    public void setMajorRealm(int majorRealm, OriginSource source) {
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
    @Override
    public boolean setCurrentTechnique(Identifier technique, TechniqueData data, OriginSource source) {
        if(getCurrentTechnique() == null && technique == null) return true;
        if(getCurrentTechnique() != null && technique != null && getCurrentTechnique().equals(technique))return true;



        //remove existing technique
        if(getCurrentTechnique() != null){
            int targetRealm = getMaxMilestoneRealm(source.getRegistryAccess());
            if(targetRealm != getMajorRealm()){
                handleRealmChange(source,targetRealm,0);
            }
            Identifier old = getCurrentTechnique();
            TechniqueData oldData = techniqueData.get(old);
            if(old != null){
                boolean result = AscensionOriginSourceHelper.broadcastTechniqueRemovedAttempt(source,old,oldData);
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
                AscensionOriginSourceHelper.broadcastTechniqueRemoved(source,old,oldData);

            }
        }
        if(technique == null) return true;// we are just trying to remove the previous technique
        Technique techniqueInstance = CoreRegistries.safeAccess(CoreRegistries.TECHNIQUE_REGISTRY,technique,source.getRegistryAccess());
        if(techniqueInstance == null) return false; // the technique does not exist
        dropToValidFamilyRealm(source,technique);

        if(techniqueInstance.getMinMajorRealm(source.getRegistryAccess()) > getMajorRealm()) return false;

        boolean result = AscensionOriginSourceHelper.broadcastTechniqueAddedAttempt(source,technique,data);
        if(!result) return false;
        techniqueHistory.removeLast();
        if (!hasCultivatedTechnique(technique)){
            techniqueInstance.onAdded(source,data);
            techniqueHistory.add(technique);
            techniqueData.put(technique,data);
        }else techniqueHistory.add(technique);

        AscensionOriginSourceHelper.broadcastTechniqueAdded(source,technique,techniqueData.get(technique));
        return true;
    }

    @Override
    public void setCompletedTribulation(OriginSource source, int majorRealm, int minorRealm, TribulationDefinition definition, TribulationData data) {
        tribulationHistory.put(new Realm(majorRealm,minorRealm),new CompletedTribulation(definition,data));
        data.getType().onAdded(source,definition,data);
        AscensionOriginSourceHelper.markPathDirty(source,getPath());
    }

    @Override
    public void removeCompletedTribulation(OriginSource source, int majorRealm, int minorRealm) {
        tribulationHistory.remove(new Realm(majorRealm,minorRealm));
    }


    //TODO validate the definition against what we expect to be there
    @Override
    public void setBreakthroughTribulation(UUID tribulation,RegistryAccess access) {
        if(!TribulationManager.getInstance().hasTribulation(tribulation)) return;
        TribulationDefinition definition = TribulationManager.getInstance().getTribulation(tribulation);
        Realm realm = new Realm(getMajorRealm(),getMinorRealm());
        Technique technique = CoreRegistries.safeAccess(CoreRegistries.TECHNIQUE_REGISTRY,getCurrentTechnique(),access);
        if(technique == null) {
            TribulationManager.getInstance().finishTribulation(tribulationId);
            return;
        }
        TribulationDefinition expected = technique.getTribulation(
                realm.majorRealm(),
                realm.minorRealm(),
                access
        );
        if(!definition.equals(expected)){
            TribulationManager.getInstance().finishTribulation(tribulationId);
            return;
        }

        this.tribulationId = tribulation;
    }

    @Override
    public void onRealmUp(OriginSource source) {
        PathData.super.onRealmUp(source);
        //TODO
        CompletedTribulation completedTribulation = cachedTribulationHistory.remove(new Realm(getMajorRealm(),getMinorRealm()));
        if(completedTribulation != null){
            //validate
            Technique technique = CoreRegistries.safeAccess(CoreRegistries.TECHNIQUE_REGISTRY,getCurrentTechnique(),source.getRegistryAccess());
            int majorRealm = getMajorRealm();
            int minorRealm = getMinorRealm()-1;
            if(minorRealm <= 0){
                majorRealm -=1;
                technique = CoreRegistries.safeAccess(CoreRegistries.TECHNIQUE_REGISTRY,getTechniqueForRealm(majorRealm),source.getRegistryAccess());
                if(technique==null){
                    return; //no valid technique found
                }
                minorRealm = technique.getMaxMinorRealm(majorRealm,getTechniqueData(getTechniqueForRealm(majorRealm)),source.getRegistryAccess());
            }
            if(technique == null) return;
            TribulationDefinition expected = technique.getTribulation(majorRealm,minorRealm,source.getRegistryAccess());

            TribulationData freshData = expected.getType().validateAndCovert(expected,completedTribulation.definition(),completedTribulation.data());

            setCompletedTribulation(source,getMajorRealm(),getMinorRealm(),expected,freshData);


        }
    }

    @Override
    public void simulateProgression(OriginSource source) {
        if(techniqueHistory.isEmpty()) return;

        ArrayList<Identifier> cachedTechniqueHistory = new ArrayList<>(techniqueHistory);
        HashMap<Identifier,TechniqueData> cachedTechniqueData = new HashMap<>(techniqueData);
        cachedTribulationHistory.putAll(tribulationHistory);
        int cachedMinorRealm = minorRealm;
        double cachedProgress = progress;

        minorRealm = 0;
        progress = 0;
        techniqueHistory.clear();
        techniqueData.clear();
        tribulationHistory.clear();


        while(cachedTechniqueHistory.size() > 1){
            Identifier technique = cachedTechniqueHistory.removeFirst();
            TechniqueData data = cachedTechniqueData.get(technique);
            setCurrentTechnique(technique,data,source);
            handleRealmChange(source,getMajorRealm()+1,0);
        }

        Identifier currentTechnique = cachedTechniqueHistory.removeFirst();
        setCurrentTechnique(currentTechnique,cachedTechniqueData.get(currentTechnique),source);
        handleRealmChange(source,getMajorRealm(),cachedMinorRealm);
        setProgress(cachedProgress);

        if(isBreakingThrough()){

            TribulationManager.getInstance().setTribulationConsumer(
                    getBreakthroughTribulation(),
                    (definition,data)->{
                        PathData pathData = AscensionOriginSourceHelper.getPathData(source,getPath());

                        pathData.handleRealmChange(
                                source,pathData.getMajorRealm()+1,0);
                        pathData.setProgress(0);
                        pathData.setCompletedTribulation(source,pathData.getMajorRealm(),pathData.getMinorRealm(),definition,data);
                    }
            );
        }
        cachedTribulationHistory.clear();
    }

    @Override
    public void removeFromSource(OriginSource source) {
        ArrayList<Identifier> cachedTechniqueHistory = new ArrayList<>(techniqueHistory);
        HashMap<Identifier,TechniqueData> cachedTechniqueData = new HashMap<>(techniqueData);
        cachedTribulationHistory.putAll(tribulationHistory);
        int cachedMinorRealm = minorRealm;
        double cachedProgress = progress;

        handleRealmChange(source,0,0);

        setCurrentTechnique(null,source);

        techniqueHistory.clear();
        techniqueHistory.addAll(cachedTechniqueHistory);
        techniqueData.clear();
        techniqueData.putAll(cachedTechniqueData);
        tribulationHistory.putAll(cachedTribulationHistory);
        cachedTribulationHistory.clear();
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

        //write tribulation history
        ValueOutput.ValueOutputList tribulationHistoryOutput = output.childrenList("tribulation_history");
        for(Realm realm : getCompletedTribulationRealms()){
            ValueOutput realmOutput = tribulationHistoryOutput.addChild();
            realmOutput.putInt("major_realm",realm.majorRealm());
            realmOutput.putInt("minor_realm",realm.minorRealm());
            realmOutput.store("definition", TribulationType.TRIBULATION_CODEC,tribulationHistory.get(realm).definition());
            realmOutput.store("data", TribulationType.TRIBULATION_DATA_CODEC,tribulationHistory.get(realm).data());
        }
        if(getBreakthroughTribulation() != null) output.putString("tribulation",getBreakthroughTribulation().toString());

    }

    public void load(ValueInput input,RegistryAccess registryAccess){

        //important note, here we should as early as possible trim error data before simulating
        //for this reason directly add to technique history FIRST, the moment we hit an error stop
        //then when adding data check if the technique is Cultivated, if so add otherwise remove
        minorRealm = input.getIntOr("minor_realm",0);
        progress = input.getDoubleOr("progress",0);

        ValueInput.ValueInputList techniqueHistoryInput = input.childrenListOrEmpty("technique_history");
        boolean realmsDiscarded = false;
        for(ValueInput techniqueInput : techniqueHistoryInput){
            String rawTechnique = techniqueInput.getStringOr("technique","none");
            if(rawTechnique.equals("none")){
                techniqueHistory.add(null);
                continue;
            }
            Identifier technique = Identifier.parse(rawTechnique);

            // there was a problem with the technique, cut cultivation after this point
            if(CoreRegistries.safeAccess(CoreRegistries.TECHNIQUE_REGISTRY,technique,registryAccess) == null) {
                realmsDiscarded = true;
                break;
            }
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

        //read tribulation history
        ValueInput.ValueInputList tribulationHistoryInput = input.childrenListOrEmpty("tribulation_history");
        for(ValueInput realmInput : tribulationHistoryInput){
            int major = realmInput.getIntOr("major_realm",0);
            int minor = realmInput.getIntOr("minor_realm",0);
            TribulationDefinition definition = realmInput.read("definition",TribulationType.TRIBULATION_CODEC).orElse(null);
            TribulationData data = realmInput.read("data", TribulationType.TRIBULATION_DATA_CODEC).orElse(null);
            if(data == null || definition == null){
                AscensionCraft.LOGGER.debug("Invalid Tribulation Type discarding");
                continue;
            }
            tribulationHistory.put(new Realm(major,minor),new CompletedTribulation(definition,data));
        }
        if(realmsDiscarded) return;
        //TODO validate that no major realms where lost. if so ignore this field
        Optional<String> tribulationId = input.getString("tribulation");
        tribulationId.ifPresent(s -> setBreakthroughTribulation(UUID.fromString(s),registryAccess));

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
    public void decode(ByteBuf buf,RegistryAccess access){
        minorRealm = buf.readInt();
        progress = buf.readDouble();

        techniqueHistory.clear();
        techniqueHistory.addAll(ByteBufHelpers.decodeArray(
                buf,
                (byteBuf)-> buf.readBoolean() ? ByteBufHelpers.decodeIdentifier(buf) : null
        ));

        techniqueData.clear();
        int size = buf.readInt();
        for(int i = 0;i<size;i++){
            Identifier techniqueId = ByteBufHelpers.decodeIdentifier(buf);
            if(!buf.readBoolean()) continue;
            Technique technique = CoreRegistries.safeAccess(CoreRegistries.TECHNIQUE_REGISTRY,techniqueId,access);
            if(technique == null) continue;
            techniqueData.put(techniqueId,technique.loadData(buf));

        }
    }
}
