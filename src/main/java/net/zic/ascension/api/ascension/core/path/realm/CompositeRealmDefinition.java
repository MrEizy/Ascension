package net.zic.ascension.api.ascension.core.path.realm;

import net.minecraft.network.chat.Component;
import net.zic.ascension.api.ascension.core.tribulation.TribulationDefinition;

public interface CompositeRealmDefinition extends RealmDefinition{


    Component getCompositeRealmName(int realm);
    Component getMinorRealmName(int realm);

    TribulationDefinition getRealmTribulation(int realm);
    double getMaxProgress(int realm);

    int getTotalRealms();
}
