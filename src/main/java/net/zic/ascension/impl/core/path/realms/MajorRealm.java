package net.zic.ascension.impl.core.path.realms;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.path.realm.CompositeRealm;
import net.zic.ascension.api.ascension.core.path.realm.CompositeRealmDefinition;
import net.zic.ascension.impl.core.path.RealmDefinition;

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

    //only used internally during loading and such
    public boolean isLimitBroken() {
        return !limitBrokenSources.isEmpty();
    }
    /**
     * sets if it is limit broken, and the source that tried to do this action
     * @param state the new state
     * @param source the source setting the new state
     * @return the state of limit break after the action
     */
    public boolean setLimitBroken(boolean state, Identifier source) {
        if(state) limitBrokenSources.add(source);
        else limitBrokenSources.remove(source);
        return isLimitBroken();
    }
    public static MajorRealm of(MajorRealmDefinition definition){
        return new MajorRealm(definition);
    }
    public static MajorRealm of(MajorRealm realm){
        MajorRealm majorRealm = new MajorRealm(realm.definition);
        majorRealm.currentRealm = realm.currentRealm;
        for(Identifier limitBrokenSource : realm.limitBrokenSources){
            majorRealm.setLimitBroken(true,limitBrokenSource);
        }
        return majorRealm;
    }

}
