package net.zic.ascension.api.ascension.core.path.realm;

import net.minecraft.network.chat.Component;
import net.zic.ascension.api.ascension.core.technique.realm_change.RealmChangeAction;
import net.zic.ascension.api.ascension.core.tribulation.TribulationDefinitionReference;

import javax.annotation.Nullable;
import java.util.Collection;

public interface RealmDefinition {

    Component getName();
    @Nullable
    TribulationDefinitionReference getTribulation();
    //holds all the actions for this realm
    Collection<RealmChangeAction> actions();
    double getMaxProgression();

}
