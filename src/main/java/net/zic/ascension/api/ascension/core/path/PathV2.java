package net.zic.ascension.api.ascension.core.path;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.ValueInput;
import net.zic.ascension.api.ascension.core.path.interactions.PathInteractionHolder;
import net.zic.ascension.api.ascension.core.tribulation.TribulationDefinition;
import net.zic.ascension.api.ascension.datapack.path.PathType;

public interface PathV2 {

    PathType getType();

    Component name();
    Component description();


    //──Realms────────────────────────────────────────────────────────
    //TODO move some methods to PathData since CompositeRealm holds its own definition
    int getMajorRealms();
    int getMinorRealms(int majorRealm);

    Component getMajorRealmName(int majorRealm);
    Component getMinorRealmName(int majorRealm,int minorRealm);
    Component getRealmName(int majorRealm,int minorRealm);

    double getMaxProgress(int majorRealm,int minorRealm);

    TribulationDefinition getTribulation(int majorRealm, int minorRealm, RegistryAccess access);
    boolean hasTribulation(int majorRealm, int minorRealm);

    //──Path Interactions────────────────────────────────────────────────────────
    void registerInteractions(PathInteractionHolder holder, RegistryAccess access);


    //──Data────────────────────────────────────────────────────────

    PathInstance newInstance(RegistryAccess access);
    PathData loadInstance(ValueInput input, RegistryAccess access);
    PathData loadInstance(ByteBuf buf, RegistryAccess access);
}
