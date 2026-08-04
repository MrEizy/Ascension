package net.zic.ascension.impl.core.path.realms;

import net.minecraft.network.chat.Component;
import net.zic.ascension.api.ascension.core.path.realm.RealmDefinition;
import net.zic.ascension.api.ascension.core.tribulation.TribulationDefinitionReference;
import org.jspecify.annotations.Nullable;

public record MinorRealmDefinition(
        Component name,
        TribulationDefinitionReference tribulation,
        BreakthroughBehaviour breakthroughBehaviour,
        double maxProgress) implements RealmDefinition {
    @Override
    public Component getName() {
        return name;
    }

    @Override
    public @Nullable TribulationDefinitionReference getTribulation() {
        return tribulation;
    }

    @Override
    public double getMaxProgression() {
        return maxProgress;
    }


}
