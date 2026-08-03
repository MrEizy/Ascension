package net.zic.ascension.api.ascension.core.path.realm;

import net.minecraft.resources.Identifier;

public interface CompositeRealm {
    CompositeRealmDefinition definition();
    int getCurrentRealm();
    //just says that this realm can have more realms than its max realm.
    //only used internally during loading and such
    boolean isLimitBroken();
    //sets if it is limit broken, and the source that tried to do this action
    void setLimitBroken(boolean state,Identifier source);

}
