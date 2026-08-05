package net.zic.ascension.api.ascension.core.path.realm;

import net.minecraft.network.chat.Component;
import net.zic.ascension.api.ascension.core.tribulation.TribulationDefinitionReference;

import javax.annotation.Nullable;

public interface RealmDefinition {

    Component getName();
    @Nullable
    TribulationDefinitionReference getTribulation();

    double getMaxProgression();

}
