package net.zic.ascension.api.ascension.core.path.realm;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public interface CompositeRealmDefinition extends RealmDefinition{


    Component getCompositeRealmName(int realm);
    Component getMinorRealmName(int realm);

    //returns the id of the tribulation in the tribulation registry, or null if there is none
    Identifier getRealmTribulationId(int realm);
    //how the realm's tribulation is started
    default TribulationMode getRealmTribulationMode(int realm) {
        return TribulationMode.AUTO;
    }
    double getMaxProgress(int realm);

    RealmDefinition realmDefinition(int realm);
    //0 is counted as a realm, so if you have 12 realms, the max realm is 11
    int getMaxRealm();
}
