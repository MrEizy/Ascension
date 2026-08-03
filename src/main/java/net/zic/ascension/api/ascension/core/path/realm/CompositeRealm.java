package net.zic.ascension.api.ascension.core.path.realm;

public interface CompositeRealm {
    CompositeRealmDefinition definition();
    int getCurrentRealm();
    boolean isLimitBroken();
}
