package net.zic.ascension.impl.core.path.realms;

import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.path.realm.CompositeRealm;
import net.zic.ascension.api.ascension.core.path.realm.CompositeRealmDefinition;

import java.util.HashSet;
import java.util.Set;

public class MajorRealm implements CompositeRealm {

    private final MajorRealmDefinition definition;
    private int currentRealm;

    private final Set<Identifier> limitBrokenSources = new HashSet<>();
    public MajorRealm(MajorRealmDefinition definition) {
        this.definition = definition;
    }

    @Override
    public MajorRealmDefinition definition() {
        return definition;
    }

    @Override
    public int getCurrentRealm() {
        return currentRealm;
    }

    public void setCurrentRealm(int newRealm){
        this.currentRealm = isLimitBroken() ? newRealm : Math.min(newRealm,definition().getMaxRealm());
    }

    @Override
    public boolean isLimitBroken() {
        return !limitBrokenSources.isEmpty();
    }

    @Override
    public boolean setLimitBroken(boolean state, Identifier source) {
        if(state) limitBrokenSources.add(source);
        else limitBrokenSources.remove(source);
        return isLimitBroken();
    }
    public static MajorRealm of(MajorRealmDefinition definition){
        return new MajorRealm(definition);
    }
}
