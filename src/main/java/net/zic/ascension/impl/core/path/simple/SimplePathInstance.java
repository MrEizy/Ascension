package net.zic.ascension.impl.core.path.simple;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.NeoForge;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.path.PathInstance;
import net.zic.ascension.api.ascension.core.path.realm.CompositeRealm;
import net.zic.ascension.api.ascension.core.path.realm.Realm;
import net.zic.ascension.api.ascension.core.progression.ProgressDirection;
import net.zic.ascension.api.ascension.core.tribulation.TribulationData;
import net.zic.ascension.api.ascension.core.tribulation.TribulationDefinition;
import net.zic.ascension.api.ascension.core.tribulation.TribulationManager;
import net.zic.ascension.api.ascension.datapack.path.realm.PathRealmChangeEvent;
import net.zic.ascension.api.ascension.datapack.tribulation.TribulationType;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.impl.core.path.CompletedTribulation;
import net.zic.ascension.impl.core.path.realms.BreakthroughBehaviour;
import net.zic.ascension.impl.core.path.realms.MajorRealm;
import net.zic.ascension.impl.core.path.realms.MinorRealmDefinition;

import java.util.*;

public class SimplePathInstance implements PathInstance {

    private final SimplePath path;

    private final List<MajorRealm> realms = new ArrayList<>();
    private final List<MajorRealm> cachedRealms = new ArrayList<>();

    private final Map<Realm, CompletedTribulation> completedTribulations = new HashMap<>();

    private UUID activeTribulationId;

    double progress; //TODO consider updating this to be a resource that handles multiple "elements"


    public SimplePathInstance(SimplePath path){
        this.path = path;
    }



    //──SETTERS────────────────────────────────────────────────────────
    //need to consider how I want tribulations to be handled should the be auto triggered? or would they need
    //to use a skill to trigger it?
    //maybe add a tracker? where say progress is 100, they can use a skill to trigger tribulation, but they can hold off
    //but not for too long, as we keep track of every bit of progress they make, and if it breaches a certain point it auto trigger?
    //not sure tbh
    @Override
    public void progressPath(Identifier path, double amount, OriginSource source,LivingEntity entitySource) {

        progress = Math.min(progress + amount, getMaxProgress());

        if (!canBreakthrough()) return;

        BreakthroughBehaviour behaviour = getCurrentRealmInstance().definition().getBreakthroughBehaviour(getCurrentMinorRealm());

        if (behaviour == BreakthroughBehaviour.INSTANT) {
            tryBreakthrough(source,entitySource);
        } else if (behaviour != BreakthroughBehaviour.NONE) {
            //TODO add timer
        }

    }

    //──GETTERS────────────────────────────────────────────────────────
    public Identifier getPathId(RegistryAccess access){
        return CoreRegistries.PATH_REGISTRY.get(access).getKey(path);
    }
    public boolean isLimitBroken(){
        if(getCurrentMajorRealm() < 0) return false;
        return getCurrentRealmInstance().isLimitBroken();
    }
    @Override
    public double getProgress() {
        return progress;
    }

    @Override
    public double getMaxProgress() {
        return getCurrentRealmInstance().definition().getMaxProgress(getCurrentMinorRealm());
    }

    @Override
    public boolean canProgress() {
        CompositeRealm currentRealm = getCurrentRealmInstance();
        int minorRealm = currentRealm.getCurrentRealm();

        boolean isProgressFull = currentRealm.definition().getMaxProgress(minorRealm) <= progress;
        boolean maxMajorRealm = path.getMaxMajorRealm() == getCurrentMajorRealm();
        boolean maxMinorRealm = getMaxMinorRealm(getCurrentMajorRealm()) == getCurrentMinorRealm();

        return !isProgressFull && !isBreakingThrough()  && !(maxMinorRealm && maxMajorRealm);
    }

    @Override
    public boolean canBreakthrough() {
        MajorRealm currentRealm = getCurrentRealmInstance();
        int minorRealm = currentRealm.getCurrentRealm();

        boolean isProgressFull = currentRealm.definition().getMaxProgress(minorRealm) <= progress;
        boolean maxMajorRealm = path.getMaxMajorRealm() == getCurrentMajorRealm();
        boolean maxMinorRealm = getMaxMinorRealm(getCurrentMajorRealm()) == getCurrentMinorRealm();

        if(currentRealm.isLimitBroken()) {
            System.out.println("conditions");
            System.out.println(isProgressFull);
            System.out.println(!isBreakingThrough());
            System.out.println(!(maxMinorRealm && maxMajorRealm));
        }
        return isProgressFull && !isBreakingThrough() && !(maxMinorRealm && maxMajorRealm);
    }


    public boolean isBreakingThrough(){
        return activeTribulationId != null;
    }



    @Override
    public int getCurrentMajorRealm() {
        return realms.size()-1;
    }

    @Override
    public int getCurrentMinorRealm() {
        return getCurrentRealmInstance().getCurrentRealm();
    }

    @Override
    public int getMaxMinorRealm(int realm) {
        return getCurrentMajorRealm() < 0 ? 0 : path.getMaxMinorRealm(realm);
    }

    @Override
    public Realm getCurrentRealm() {
        return getCurrentMajorRealm() < 0 ? Realm.of(-1,0) : Realm.of(getCurrentMajorRealm(),getCurrentMinorRealm());
    }

    public MajorRealm getCurrentRealmInstance(){
        if(realms.isEmpty()){
            realms.add(MajorRealm.of(path.getRealmDefinition(0)));
        }
        return realms.getLast();
    }

    //──Tribulations────────────────────────────────────────────────────────

    public void setCompletedTribulation(OriginSource source, int majorRealm, int minorRealm, TribulationDefinition definition, TribulationData data) {
        completedTribulations.put(Realm.of(majorRealm,minorRealm),new CompletedTribulation(definition,data));
    }
    public void continueTribulation(UUID tribulationUUID, OriginSource source){
        if(!TribulationManager.getInstance().hasTribulation(tribulationUUID)) {
            activeTribulationId = null;
            return;
        };
        TribulationDefinition definition = TribulationManager.getInstance().getTribulation(tribulationUUID);

        TribulationDefinition expected = path.getTribulation(getCurrentMajorRealm(),getCurrentMinorRealm(),source.getRegistryAccess());
        if(!definition.equals(expected)){
            TribulationManager.getInstance().finishTribulation(tribulationUUID);
            activeTribulationId = null;
            return;
        }


        this.activeTribulationId = tribulationUUID;
    }
    public void startTribulation(OriginSource source, TribulationDefinition definition, LivingEntity target){
        if(activeTribulationId != null) return;
        activeTribulationId = TribulationManager.getInstance().triggerTribulation(definition,target);
        TribulationManager.getInstance().setTribulationConsumer(activeTribulationId,(finishedDefinition,data)->{
            SimplePathInstance pathInstance = this;

            pathInstance.setCompletedTribulation(source,pathInstance.getCurrentMajorRealm(),pathInstance.getCurrentMinorRealm(),finishedDefinition,data);

            if(getCurrentRealmInstance().isLimitBroken()){
                handleRealmChange(Realm.of(getCurrentMajorRealm(),getCurrentMinorRealm()+1),source);
            }else  handleRealmChange(Realm.getNext(Realm.of(getCurrentMajorRealm(),getCurrentMinorRealm()),path),source);
            pathInstance.progress = 0;
            pathInstance.activeTribulationId = null;

        });
    }


    public void completeTribulationForRealm(Realm realm,OriginSource source){
        if(realm.majorRealm() < 0) return;
        CompletedTribulation completedTribulation = completedTribulations.get(realm);
        TribulationDefinition definition = path.getTribulation(realm.majorRealm(),realm.minorRealm(),source.getRegistryAccess());
        if(completedTribulation == null && definition == null) return;
        if(definition == null){
            completedTribulations.remove(realm);
            return;
        }
        if(completedTribulation == null){
            //no data present, create new data
            completedTribulation = new CompletedTribulation(definition,definition.getType().newData(definition));
            completedTribulations.put(realm, completedTribulation);
        }else{
            //data already present, try convert
            TribulationData data = definition.getType().validateAndCovert(definition,completedTribulation.definition(), completedTribulation.data());
            completedTribulation = new CompletedTribulation(definition,data);
            completedTribulations.put(realm, completedTribulation);
        }

        definition.getType().onAdded(source,definition,completedTribulation.data());
    }
    public void removeTribulationForRealm(Realm realm,OriginSource source){
        CompletedTribulation completedTribulation = completedTribulations.remove(realm);
        if(completedTribulation == null) return;
        completedTribulation.definition().getType().onRemoved(source,completedTribulation.definition(),completedTribulation.data());
    }


    //──Realm Change Logic────────────────────────────────────────────────────────

    public void limitBreakRealm(OriginSource source,int realm,Identifier limitBreakSource){
        if(realm >= realms.size()) return;
        //TODO if realm != current realm handle realm change
        realms.get(realm).setLimitBroken(true,limitBreakSource);
    }
    public void removeRealmLimitBreak(OriginSource source,int realm,Identifier limitBreakSource){
        if(realm >= realms.size()) return;
        if(realms.get(realm).setLimitBroken(false,limitBreakSource)) return;

        handleRealmChange(Realm.of(getCurrentMajorRealm(),0),source);
    }

    public void tryBreakthrough(OriginSource source,LivingEntity entitySource){

        if(getCurrentRealmInstance().definition().getRealmTribulation(getCurrentMinorRealm()) == null){
            if(getCurrentRealmInstance().isLimitBroken()){
                handleRealmChange(Realm.of(getCurrentMajorRealm(),getCurrentMinorRealm()+1),source);
            }else  handleRealmChange(Realm.getNext(Realm.of(getCurrentMajorRealm(),getCurrentMinorRealm()),path),source);
        }else{
            startTribulation(source,path.getTribulation(getCurrentMajorRealm(),getCurrentMinorRealm(),source.getRegistryAccess()),entitySource);
        }
    }

    @Override
    public void onRealmUp(OriginSource source) {
        path.getProgressActionHolder().run(source,CoreRegistries.PATH_REGISTRY.get(source.getRegistryAccess()).getKey(path),this,ProgressDirection.UP);
    }
    public void broadcastRealmUp(OriginSource source,Realm oldRealm){
        PathRealmChangeEvent.PathRealmUpEvent event = new PathRealmChangeEvent.PathRealmUpEvent(source,Realm.of(getCurrentMajorRealm(),getCurrentMinorRealm()),oldRealm,getPathId(source.getRegistryAccess()),this);
        NeoForge.EVENT_BUS.post(event);
    }

    @Override
    public void onRealmDown(OriginSource source) {
        path.getProgressActionHolder().run(source,CoreRegistries.PATH_REGISTRY.get(source.getRegistryAccess()).getKey(path),this,ProgressDirection.DOWN);
    }
    public void broadcastRealmDown(OriginSource source,Realm oldRealm){
        PathRealmChangeEvent.PathRealmUpEvent event = new PathRealmChangeEvent.PathRealmUpEvent(source,Realm.of(getCurrentMajorRealm(),getCurrentMinorRealm()),oldRealm,getPathId(source.getRegistryAccess()),this);
        NeoForge.EVENT_BUS.post(event);
    }
    public void addNewMajorRealm(){
        if(cachedRealms.isEmpty()){
            realms.add(MajorRealm.of(path.getRealmDefinition(getCurrentMajorRealm()+1)));
        }else{
            realms.add(cachedRealms.removeFirst());
        }
    }

    @Override
    public void handleRealmChange(Realm newRealm, OriginSource source) {

        boolean fromStart = getCurrentMajorRealm() < 0;
        Realm oldRealm = fromStart ? Realm.of(0,0) :  Realm.of(getCurrentMajorRealm(),getCurrentMinorRealm());
        List<Realm> traversedRealms = Realm.getRange(oldRealm,newRealm,path);


        ProgressDirection direction= ProgressDirection.UP;
        if(oldRealm.compareTo(newRealm) >0){
            direction = ProgressDirection.DOWN;
            traversedRealms = traversedRealms.reversed();
        }
        System.out.println("traversing realms : "+traversedRealms);
        //we do not traverse the start realm
        if(!fromStart) traversedRealms.removeFirst();
        while(!traversedRealms.isEmpty()){
            Realm realm = traversedRealms.removeFirst();
            Realm previousRealm = getCurrentMajorRealm() < 0?Realm.of(-1,0) : Realm.of(getCurrentMajorRealm(),getCurrentMinorRealm());
            if(direction == ProgressDirection.UP){
                if(realm.majorRealm() == getCurrentMajorRealm()){
                    completeTribulationForRealm(previousRealm,source);
                    getCurrentRealmInstance().setCurrentRealm(realm.minorRealm());
                    onRealmUp(source);
                    broadcastRealmUp(source,previousRealm);
                }else{
                    completeTribulationForRealm(previousRealm,source);
                    addNewMajorRealm();

                    if(getCurrentRealmInstance().isLimitBroken()){

                        //this only happens when simulating from a cached majorRealm
                        //we are basically telling it to simulate extra realms

                        int index = 0;
                        for(int i = 0;i<=traversedRealms.size();i++){
                            index = i;
                            if(i >= traversedRealms.size()) break;
                            if(traversedRealms.get(i).majorRealm() != getCurrentMajorRealm())break;
                        }

                        for(int minorRealm = getCurrentRealmInstance().definition().getMaxRealm()+1;
                            minorRealm <= getCurrentRealmInstance().getCurrentRealm();
                            minorRealm ++){
                            System.out.println("added Realm : "+minorRealm);
                            traversedRealms.add(index,Realm.of(getCurrentMajorRealm(),minorRealm));
                            index++;
                        }

                        System.out.println(traversedRealms.toString());
                    }
                    if(getCurrentRealmInstance().getCurrentRealm() != 0){
                        getCurrentRealmInstance().setCurrentRealm(0);
                    }
                    onRealmUp(source);
                    broadcastRealmUp(source,previousRealm);
                }
            }else{
                if(realm.majorRealm() == getCurrentMajorRealm()) {
                    removeTribulationForRealm(previousRealm,source);
                    getCurrentRealmInstance().setCurrentRealm(realm.minorRealm());
                    onRealmDown(source);
                    broadcastRealmDown(source,previousRealm);
                }else{
                    removeTribulationForRealm(previousRealm,source);
                    realms.removeLast();
                    if(getCurrentRealmInstance().isLimitBroken() && getCurrentRealmInstance().getCurrentRealm() > getCurrentRealmInstance().definition().getMaxRealm()){
                        //the realm we have loaded has extra realms we need to include
                        for(int minorRealm = getCurrentRealmInstance().definition().getMaxRealm()+1;
                            minorRealm <= getCurrentRealmInstance().getCurrentRealm();
                            minorRealm ++){
                            traversedRealms.addFirst(Realm.of(getCurrentMajorRealm(),minorRealm));
                        }
                    }
                    onRealmDown(source);
                    broadcastRealmDown(source,previousRealm);
                }
            }
            progress = 0;
        }

    }

    @Override
    public void simulateProgression(OriginSource source) {
        boolean realmsDiscarded = false;
        while(!realms.isEmpty()){
            MajorRealm realm = realms.removeFirst();
            cachedRealms.add(realm);
            if(realm.definition().getMaxRealm() < realm.getCurrentRealm() && !realm.isLimitBroken()) {
                //limit broken realm is no longer limit broken so return cultivation to this realm
                realms.clear();
                realm.setCurrentRealm(0);
                progress = 0;
                realmsDiscarded = true;
            }
        }


        double cachedProgress = progress;
        if(cachedRealms.isEmpty()) return;

        MajorRealm finalRealm = cachedRealms.getLast();
        //logic is already in place to handle limit broken from cached inside handleRealm change
        //so we just need to tell it to go to the max of the realm and ensure it is limit broken
        int minorRealm = finalRealm.definition().getMaxRealm() < finalRealm.getCurrentRealm() ? finalRealm.definition().getMaxRealm() : finalRealm.getCurrentRealm();
        handleRealmChange(Realm.of(cachedRealms.size()-1,minorRealm),source);

        cachedRealms.clear();
        progress = cachedProgress;

        if(!realmsDiscarded && activeTribulationId != null){
            continueTribulation(activeTribulationId,source);
        }else if (activeTribulationId != null){
            TribulationManager.getInstance().finishTribulation(activeTribulationId);
        }
    }

    @Override
    public void removeFromSource(OriginSource source) {
        if(getCurrentMajorRealm() < 0) return;
        //create a deep cached copy, this way we can properly maintain limit broken info
        for(MajorRealm realm : realms){
            cachedRealms.add(MajorRealm.of(realm));
        }
        Map<Realm, CompletedTribulation> cachedTribulations = new HashMap<>(completedTribulations);
        double cachedProgress = progress;

        handleRealmChange(Realm.of(0,0),source);
        realms.removeFirst();
        path.getProgressActionHolder().run(source,CoreRegistries.PATH_REGISTRY.get(source.getRegistryAccess()).getKey(path),this,ProgressDirection.DOWN);

        realms.clear();
        realms.addAll(cachedRealms);
        cachedRealms.clear();

        completedTribulations.clear();
        completedTribulations.putAll(cachedTribulations);

    }

    @Override
    public void write(ValueOutput output, RegistryAccess access) {
        output.putDouble("progress",getProgress());

        ValueOutput.ValueOutputList realmsOutput = output.childrenList("realms");
        for(MajorRealm majorRealm : realms){
            ValueOutput majorRealmOutput = realmsOutput.addChild();
            majorRealmOutput.putInt("minor_realm",majorRealm.getCurrentRealm());
        }
        ValueOutput.ValueOutputList completedTribulationsOutput = output.childrenList("completed_tribulations");
        for(Realm realm : completedTribulations.keySet()){
            ValueOutput completedTribulationOutput = completedTribulationsOutput.addChild();
            completedTribulationOutput.putInt("major_realm",realm.majorRealm());
            completedTribulationOutput.putInt("minor_realm",realm.minorRealm());
            completedTribulationOutput.store("definition", TribulationType.TRIBULATION_CODEC,completedTribulations.get(realm).definition());
            completedTribulationOutput.store("data", TribulationType.TRIBULATION_DATA_CODEC,completedTribulations.get(realm).data());
        }
        if(activeTribulationId != null) output.putString("tribulation",activeTribulationId.toString());

    }
    public void read(ValueInput input,RegistryAccess access){
        realms.clear();
        progress = input.getDoubleOr("progress",0);
        ValueInput.ValueInputList realmsInput = input.childrenListOrEmpty("realms");
        for(ValueInput realmInput : realmsInput){
            MajorRealm realm = MajorRealm.of(path.getRealmDefinition(getCurrentMajorRealm()+1));
            realm.setCurrentRealm(realmInput.getIntOr("minor_realm",0));
            realms.add(realm);
        }
        ValueInput.ValueInputList completedTribulationsInput = input.childrenListOrEmpty("completed_tribulations");
        for(ValueInput completedTribulationInput : completedTribulationsInput){
            Realm realm = Realm.of(completedTribulationInput.getIntOr("major_realm",0),completedTribulationInput.getIntOr("minor_realm",0));
            TribulationDefinition definition = completedTribulationInput.read("definition",TribulationType.TRIBULATION_CODEC).orElse(null);
            TribulationData data = completedTribulationInput.read("data", TribulationType.TRIBULATION_DATA_CODEC).orElse(null);
            if(data == null || definition == null){
                AscensionCraft.LOGGER.debug("Invalid Tribulation Type discarding");
                continue;
            }
            completedTribulations.put(realm,new CompletedTribulation(definition,data));
        }
        Optional<String> tribulationId = input.getString("tribulation");
        tribulationId.ifPresent(s ->activeTribulationId = UUID.fromString(s));
    }

                     @Override
    public void encode(ByteBuf buf, RegistryAccess access) {

    }


}
