package net.zic.ascension.impl.core.path.realms;

import net.minecraft.network.chat.Component;
import net.zic.ascension.api.ascension.core.path.realm.CompositeRealmDefinition;
import net.zic.ascension.api.ascension.core.path.realm.RealmDefinition;
import net.zic.ascension.api.ascension.core.tribulation.TribulationDefinitionReference;
import org.jspecify.annotations.Nullable;

import java.util.List;

public record MajorRealmDefinition(
        Component name,
        List<MinorRealmDefinition> minorRealms
        ) implements CompositeRealmDefinition {
    @Override
    public Component getCompositeRealmName(int realm) {
        return Component.empty().append(name).append(" (").append(getMinorRealmName(realm)).append(")");
    }

    @Override
    public Component getMinorRealmName(int realm) {
        return minorRealms.get(realm).name();
    }

    @Override
    public TribulationDefinitionReference getRealmTribulation(int realm) {
        return realmDefinition(realm).getTribulation();
    }

    public BreakthroughBehaviour getBreakthroughBehaviour(int realm){
        return realmDefinition(realm).breakthroughBehaviour();
    }
    @Override
    public double getMaxProgress(int realm) {
        return 0;
    }

    @Override
    public MinorRealmDefinition realmDefinition(int realm) {
        return minorRealms.get(realm);
    }

    @Override
    public int getMaxRealm() {
        return minorRealms.size()-1;
    }

    @Override
    public Component getName() {
        return name;
    }

    @Override
    public @Nullable TribulationDefinitionReference getTribulation() {
        return null;
    }

    @Override
    public double getMaxProgression() {
        return 0;
    }
}
