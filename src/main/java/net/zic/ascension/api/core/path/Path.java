package net.zic.ascension.api.core.path;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.ValueInput;
import net.zic.ascension.api.datapack.path.PathType;

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

    //──Interaction────────────────────────────────────────────────────────
    double getInteractionValue(Identifier path);
    PathInteraction getInteractionType(Identifier path);
    Collection<Identifier> getPathsOfInteraction(PathInteraction type);


    //──Data────────────────────────────────────────────────────────

    PathData newData(RegistryAccess access);
    PathData loadData(ValueInput input,RegistryAccess access);
    PathData loadData(ByteBuf buf,RegistryAccess access);
}
