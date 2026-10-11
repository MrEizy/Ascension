package net.zic.ascension.api.ascension.core.path.realm;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import javax.annotation.Nullable;

public interface RealmDefinition {

    Component getName();
    @Nullable
    Identifier getTribulationId();

    double getMaxProgression();

}
