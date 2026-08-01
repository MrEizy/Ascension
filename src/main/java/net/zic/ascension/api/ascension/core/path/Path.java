package net.zic.ascension.api.ascension.core.path;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.ValueInput;
import net.zic.ascension.api.ascension.core.path.interactions.PathInteractionHolder;
import net.zic.ascension.api.ascension.core.path.interactions.PathInteractionType;
import net.zic.ascension.api.ascension.core.tribulation.TribulationDefinition;
import net.zic.ascension.api.ascension.datapack.path.PathType;

import java.util.Collection;

public interface Path {

    PathType getType();

    Component name();
    Component description();

    //──Realms────────────────────────────────────────────────────────

    Component getMajorRealmName(int majorRealm);
    Component getMinorRealmName(int majorRealm,int minorRealm);

    // returns the formatted name of the realm when both major and minor realm are displayed together
    Component getRealmName(int majorRealm, int minorRealm);

    //gives the default max minor realm and major realm a technique can cultivate
    int getMaxMajorRealm();
    int getMaxMinorRealm(int majorRealm);

    //gives the progress needed to progress a given realm
    double getMaxProgress(int majorRealm,int minorRealm);

    //──Tribulations────────────────────────────────────────────────────────
    TribulationDefinition getTribulationDefinition(int majorRealm,int minorRealm,RegistryAccess access);
    boolean hasTribulation(int majorRealm,int minorRealm);
    //──Interaction────────────────────────────────────────────────────────
    //TODO consider removing, OR updated to utilize path interaction holder
    double getInteractionValue(Identifier path);
    PathInteractionType getInteractionType(Identifier path);
    Collection<Identifier> getPathsOfInteraction(PathInteractionType type);
    void registerInteractions(PathInteractionHolder holder,RegistryAccess access);

    //──Data────────────────────────────────────────────────────────

    PathData newData(RegistryAccess access);
    PathData loadData(ValueInput input,RegistryAccess access);
    PathData loadData(ByteBuf buf,RegistryAccess access);
}
