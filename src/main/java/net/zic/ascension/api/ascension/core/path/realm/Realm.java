package net.zic.ascension.api.ascension.core.path.realm;

public record Realm(int majorRealm,int minorRealm) {
    public static Realm of(int majorRealm,int minorRealm){
        return new Realm(majorRealm,minorRealm);
    }
}
