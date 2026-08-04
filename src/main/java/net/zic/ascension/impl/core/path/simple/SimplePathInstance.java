package net.zic.ascension.impl.core.path.simple;

import net.minecraft.resources.Identifier;
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
import net.zic.ascension.impl.core.path.realms.MajorRealm;

import java.util.*;

public class SimplePathInstance implements PathInstance {

    private final SimplePath path;
    private UUID pathInstanceId = UUID.randomUUID();

    private final List<MajorRealm> realms = new ArrayList<>();
    private final Map<Identifier, CompletedTribulation> completedTribulations = new HashMap<>();

    private UUID activeTribulationId;

    private ProgressActionHolder actions;
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
        progress = Math.min(progress+amount,getMaxProgress());
        //TODO not sure yet how I want to handle breakthroughs.
        /*
            TODO:
                what i will do is add an option to realms, this option determines HOW we breakhtrough between the realms
                it will simply be a field called "instant breakthrough" if the field is TRUE then we move on or trigger breakthrough when we hit max.
                otherwise we just freeze path progression till something happens
                in the future i might add more conditions like delayed(after an amount of time trigger)
         */
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
        return !isProgressFull && !isBreakingThrough();
    }

    @Override
    public boolean canBreakthrough() {
        CompositeRealm currentRealm = getCurrentRealm();
        int minorRealm = currentRealm.getCurrentRealm();

        boolean isProgressFull = currentRealm.definition().getMaxProgress(minorRealm) <= progress;
        return isProgressFull && !isBreakingThrough();
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
    @Override
    public void onRealmUp(OriginSource source) {
        actions.run(source,CoreRegistries.PATH_REGISTRY.get(source.getRegistryAccess()).getKey(path),this,ProgressDirection.UP);
    }

    @Override
    public void onRealmDown(OriginSource source) {
        actions.run(source,CoreRegistries.PATH_REGISTRY.get(source.getRegistryAccess()).getKey(path),this,ProgressDirection.DOWN);
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




}
