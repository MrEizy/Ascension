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
    private UUID pathInstanceId = UUID.randomUUID();

    private final List<MajorRealm> realms = new ArrayList<>();
    private final Map<Identifier, CompletedTribulation> completedTribulations = new HashMap<>();

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


    public void removeCompletedTribulation(OriginSource source, int majorRealm, int minorRealm) {
        //TODO
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

    @Override
    public void handleRealmChange(Realm newRealm, OriginSource source) {

    }

    @Override
    public void simulateProgression(OriginSource source) {

    }

    @Override
    public void removeFromSource(OriginSource source) {

    }

    @Override
    public void write(ValueOutput output, RegistryAccess access) {

    }

    @Override
    public void encode(ByteBuf buf, RegistryAccess access) {

    }


}
