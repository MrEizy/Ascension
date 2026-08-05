package net.zic.ascension.impl.core.path.simple;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.path.PathInstance;
import net.zic.ascension.api.ascension.core.path.realm.CompositeRealm;
import net.zic.ascension.api.ascension.core.path.realm.Realm;
import net.zic.ascension.api.ascension.core.progression.ProgressActionHolder;
import net.zic.ascension.api.ascension.core.progression.ProgressDirection;
import net.zic.ascension.api.ascension.core.tribulation.TribulationData;
import net.zic.ascension.api.ascension.core.tribulation.TribulationDefinition;
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
    public void progressPath(Identifier path, double amount, OriginSource source) {
        progress = Math.min(progress + amount, getMaxProgress());
        if (!canBreakthrough()) return;

        BreakthroughBehaviour behaviour = getCurrentRealm().definition().getBreakthroughBehaviour(getCurrentMinorRealm());

        if (behaviour == BreakthroughBehaviour.INSTANT) {
            tryBreakthrough(source);
        } else if (behaviour != BreakthroughBehaviour.NONE) {
            //TODO add timer
        }

    }

    //──GETTERS────────────────────────────────────────────────────────

    @Override
    public double getProgress() {
        return progress;
    }

    @Override
    public double getMaxProgress() {
        return getCurrentRealm().definition().getMaxProgress(getCurrentMinorRealm());
    }

    @Override
    public boolean canProgress() {
        CompositeRealm currentRealm = getCurrentRealm();
        int minorRealm = currentRealm.getCurrentRealm();

        boolean isProgressFull = currentRealm.definition().getMaxProgress(minorRealm) <= progress;
        boolean maxMajorRealm = path.getMaxMajorRealm() == getCurrentMajorRealm();
        boolean maxMinorRealm = getMaxMinorRealm(getCurrentMajorRealm()) == getCurrentMinorRealm();

        return !isProgressFull && !isBreakingThrough()  && !(maxMinorRealm && maxMajorRealm);
    }

    @Override
    public boolean canBreakthrough() {
        CompositeRealm currentRealm = getCurrentRealm();
        int minorRealm = currentRealm.getCurrentRealm();

        boolean isProgressFull = currentRealm.definition().getMaxProgress(minorRealm) <= progress;
        boolean maxMajorRealm = path.getMaxMajorRealm() == getCurrentMajorRealm();
        boolean maxMinorRealm = getMaxMinorRealm(getCurrentMajorRealm()) == getCurrentMinorRealm();

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
        return getCurrentRealm().getCurrentRealm();
    }

    @Override
    public int getMaxMinorRealm(int realm) {
        return path.getMaxMinorRealm(realm);
    }

    public MajorRealm getCurrentRealm(){
        if(realms.isEmpty()){
            realms.add(MajorRealm.of(path.getRealmDefinition(0)));
        }
        return realms.getLast();
    }

    //──Tribulations────────────────────────────────────────────────────────

    public void setCompletedTribulation(OriginSource source, int majorRealm, int minorRealm, TribulationDefinition definition, TribulationData data) {
        //TODO
    }




    public void completeTribulationForRealm(Realm realm,OriginSource source){
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
        completedTribulation.definition().getType().onRemoved(source,completedTribulation.definition(),completedTribulation.data());
    }


    //──Realm Change Logic────────────────────────────────────────────────────────

    public void tryBreakthrough(OriginSource source){
        MinorRealmDefinition definition = getCurrentRealm().definition().realmDefinition(getCurrentMinorRealm());

        if(definition.getTribulation() == null){
            handleRealmChange(Realm.of(getCurrentMajorRealm()+1,0),source);
        }else{
            //TODO start tribulation
        }
    }

    @Override
    public void onRealmUp(OriginSource source) {
        path.getProgressActionHolder().run(source,CoreRegistries.PATH_REGISTRY.get(source.getRegistryAccess()).getKey(path),this,ProgressDirection.UP);
    }

    @Override
    public void onRealmDown(OriginSource source) {
        path.getProgressActionHolder().run(source,CoreRegistries.PATH_REGISTRY.get(source.getRegistryAccess()).getKey(path),this,ProgressDirection.DOWN);
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
        Realm oldRealm = Realm.of(getCurrentMajorRealm(),getCurrentMinorRealm());
        List<Realm> traversedRealms = Realm.getRange(oldRealm,newRealm,path);

        if(oldRealm.equals(newRealm)) return;
        ProgressDirection direction= ProgressDirection.UP;
        if(oldRealm.compareTo(newRealm) >0){
            direction = ProgressDirection.DOWN;
            traversedRealms = traversedRealms.reversed();
        }

        //we do not traverse the start realm
        traversedRealms.removeFirst();
        while(!traversedRealms.isEmpty()){
            Realm realm = traversedRealms.removeFirst();
            Realm previousRealm = Realm.of(getCurrentMajorRealm(),getCurrentMinorRealm());
            if(direction == ProgressDirection.UP){
                if(realm.majorRealm() == getCurrentMajorRealm()){
                    completeTribulationForRealm(previousRealm,source);
                    getCurrentRealm().setCurrentRealm(realm.minorRealm());
                    onRealmUp(source);
                }else{
                    completeTribulationForRealm(previousRealm,source);
                    addNewMajorRealm();
                    if(getCurrentRealm().isLimitBroken()){

                        //this only happens when simulating from a cached majorRealm
                        //we are basically telling it to simulate extra realms

                        int index = 0;
                        for(int i = 0;i<traversedRealms.size();i++){
                            index = i;
                            if(traversedRealms.get(i).majorRealm() != getCurrentMajorRealm())break;
                        }

                        for(int minorRealm = getCurrentRealm().definition().getMaxRealm()+1;
                            minorRealm <= getCurrentRealm().getCurrentRealm();
                            minorRealm ++){
                            traversedRealms.add(index,Realm.of(getCurrentMajorRealm(),minorRealm));
                            index++;
                        }
                    }
                    onRealmUp(source);
                }
            }else{
                if(realm.majorRealm() == getCurrentMajorRealm()) {
                    removeTribulationForRealm(previousRealm,source);
                    getCurrentRealm().setCurrentRealm(realm.minorRealm());
                    onRealmDown(source);
                }else{
                    removeTribulationForRealm(previousRealm,source);
                    realms.removeLast();
                    if(getCurrentRealm().isLimitBroken() && getCurrentRealm().getCurrentRealm() > getCurrentRealm().definition().getMaxRealm()){
                        //the realm we have loaded has extra realms we need to include
                        for(int minorRealm = getCurrentRealm().definition().getMaxRealm()+1;
                            minorRealm <= getCurrentRealm().getCurrentRealm();
                            minorRealm ++){
                            traversedRealms.addFirst(Realm.of(getCurrentMajorRealm(),minorRealm));
                        }
                    }
                    onRealmDown(source);
                }
            }
            progress = 0;
        }

    }

    @Override
    public void simulateProgression(OriginSource source) {
        cachedRealms.addAll(realms);
        realms.clear();
        double cachedProgress = progress;
        if(cachedRealms.isEmpty()) return;

        MajorRealm finalRealm = cachedRealms.getLast();
        //logic is already in place to handle limit broken from cached inside handleRealm change
        //so we just need to tell it to go to the max of the realm and ensure it is limit broken
        int minorRealm = finalRealm.definition().getMaxRealm() < finalRealm.getCurrentRealm() ? finalRealm.definition().getMaxRealm() : finalRealm.getCurrentRealm();
        handleRealmChange(Realm.of(cachedRealms.size()-1,minorRealm),source);

        progress = cachedProgress;
    }

    @Override
    public void removeFromSource(OriginSource source) {
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

    }

    @Override
    public void encode(ByteBuf buf, RegistryAccess access) {

    }


}
