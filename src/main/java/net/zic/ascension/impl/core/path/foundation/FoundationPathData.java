package net.zic.ascension.impl.core.path.foundation;

import net.minecraft.resources.Identifier;
import net.zic.ascension.api.core.source.OriginSource;
import net.zic.ascension.impl.core.path.MajorRealmDefinition;
import net.zic.ascension.impl.core.path.simple.SimplePathData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FoundationPathData extends SimplePathData {
    public FoundationPathData(Identifier path) {
        super(path);
    }

    private static class MajorRealmFoundation{
        private double progress;
        private int foundationRealm;

        public void setProgress(double progress){
            this.progress = progress;
        }
        public double getProgress(){
            return progress;
        }
        public void setFoundationRealm(int realm){
            this.foundationRealm = realm;
        }
        public int getFoundationRealm(){
            return foundationRealm;
        }
    }
    private final ArrayList<MajorRealmFoundation> foundations = new ArrayList<>(){{add(new MajorRealmFoundation());}};
    private ArrayList<MajorRealmFoundation> cachedFoundations = new ArrayList<>();
    @Override
    public void onRealmUp(OriginSource source) {
        super.onRealmUp(source);
        if(getMinorRealm() == 0 && foundations.size() <=getMajorRealm()) addFoundation();
    }

    @Override
    public void onRealmDown(OriginSource source) {
        super.onRealmDown(source);
        if(getMinorRealm() == getMaxMinorRealm(getMajorRealm(),source.getRegistryAccess())) removeFoundation();
    }

    public void addFoundation(){
        MajorRealmFoundation foundation = cachedFoundations.isEmpty() ? new MajorRealmFoundation() : cachedFoundations.removeFirst();
        foundations.add(foundation);
    }
    public void removeFoundation(){
        //TODO handle foundation change
        foundations.removeLast();
    }
    public int getFoundationRealm(int majorRealm){
        return majorRealm>=foundations.size() ? 0 : foundations.get(majorRealm).foundationRealm;
    }
    public double getFoundationRealmProgress(int majorRealm){
        return majorRealm>=foundations.size() ? 0 : foundations.get(majorRealm).progress;
    }

    @Override
    public void simulateProgression(OriginSource source) {
        cachedFoundations = new ArrayList<>(foundations);
        foundations.clear();
        super.simulateProgression(source);
        cachedFoundations.clear();

    }

    @Override
    public void removeFromSource(OriginSource source) {
        cachedFoundations = new ArrayList<>(foundations);
        super.removeFromSource(source);

        foundations.clear();
        foundations.addAll(cachedFoundations);
        cachedFoundations.clear();
    }
}
