package net.zic.ascension.api.ascension.core.path.realm;

import net.zic.ascension.api.ascension.core.path.Path;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public record Realm(int majorRealm, int minorRealm) implements Comparable<Realm>{
    public static Realm of(int majorRealm,int minorRealm){
        return new Realm(majorRealm,minorRealm);
    }

    /**
     *
     * @param realm1
     * @param realm2
     * @return an ordered list of realms between realm 1 and 2, with 0 being smallest. includes both start and end
     */
    public static List<Realm> getRange(Realm realm1, Realm realm2, Path path){
        if(realm1.equals(realm2)) return new ArrayList<>(){{
            add(realm1);
        }};
        Realm start = realm1.compareTo(realm2) <= 0 ? realm1 : realm2;
        Realm end = realm1.compareTo(realm2) <= 0 ? realm2 : realm1;

        List<Realm> result = new ArrayList<>();

        if(start.majorRealm == end.majorRealm()){
            for(int minorRealm = start.minorRealm();minorRealm<=end.minorRealm();minorRealm++){
                result.add(Realm.of(start.majorRealm,minorRealm));
            }
            return result;
        }


        for(int minorRealm = start.minorRealm;minorRealm<=path.getMaxMinorRealm(start.majorRealm);minorRealm++){
            result.add(Realm.of(start.majorRealm,minorRealm));
        }

        for(int majorRealm = start.majorRealm+1; majorRealm < end.majorRealm; majorRealm ++){
            for(int minorRealm = 0;minorRealm<=path.getMaxMinorRealm(majorRealm);minorRealm++){
                result.add(Realm.of(majorRealm,minorRealm));
            }
        }
        for(int minorRealm = 0;minorRealm<=end.minorRealm;minorRealm++){
            result.add(Realm.of(end.majorRealm,minorRealm));
        }

        return result;
    }
    public static Realm getNext(Realm realm,Path path){
         return realm.minorRealm == path.getMaxMinorRealm(realm.majorRealm()) ?
                 Realm.of(realm.majorRealm+1,0):
                 Realm.of(realm.majorRealm(), realm.minorRealm()+1);
    }

    //returns if given ream is in between 2 realms inclusive
    public boolean isInRange(Realm realm1,Realm realm2){

        Realm start = realm1.compareTo(realm2) <= 0 ? realm1 : realm2;
        Realm end = realm1.compareTo(realm2) <= 0 ? realm2 : realm1;
        return compareTo(start) >= 0 && compareTo(end) <= 0;
    }

    @Override
    public int compareTo(@NonNull Realm other) {
        if(this.equals(other)) return 0;
        return ((majorRealm > other.majorRealm) || (majorRealm == other.majorRealm && minorRealm > other.minorRealm))
                ? 1 : -1;

    }
}
