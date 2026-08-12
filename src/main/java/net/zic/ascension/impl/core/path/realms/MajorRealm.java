package net.zic.ascension.impl.core.path.realms;

import io.netty.buffer.ByteBuf;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.path.realm.CompositeRealm;
import net.zic.zenithlib.network.ByteBufHelpers;

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

    //does not apply limit broken limits, that is the job of the path
    public void setCurrentRealm(int newRealm){
        this.currentRealm = newRealm;
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
    public static MajorRealm of(ByteBuf buf,MajorRealmDefinition definition){
        MajorRealm majorRealm = MajorRealm.of(definition);

        majorRealm.currentRealm = buf.readInt();
        majorRealm.limitBrokenSources.addAll(ByteBufHelpers.decodeArray(buf,ByteBufHelpers::decodeIdentifier));
        return  majorRealm;
    }

    public void encode(ByteBuf buf){
        buf.writeInt(currentRealm);
        ByteBufHelpers.encodeCollection(limitBrokenSources,buf,ByteBufHelpers::encodeIdentifier);
    }

}
