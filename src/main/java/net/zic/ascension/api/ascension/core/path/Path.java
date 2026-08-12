package net.zic.ascension.api.ascension.core.path;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.ValueInput;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.path.interaction.PathInteraction;
import net.zic.ascension.api.ascension.core.path.interaction.PathInteractionType;
import net.zic.ascension.api.ascension.core.path.realm.CompositeRealmDefinition;
import net.zic.ascension.api.ascension.core.tribulation.TribulationDefinition;
import net.zic.ascension.api.ascension.datapack.path.PathType;
import org.apache.logging.log4j.core.Core;

import java.util.Collection;

public interface Path {

    PathType getType();

    Component name();
    Component description();

    default Identifier getId(RegistryAccess access){
        return CoreRegistries.PATH_REGISTRY.get(access).getKey(this);
    }
    //──Realms────────────────────────────────────────────────────────
    //TODO move some methods to PathInstance since CompositeRealm holds its own definition
    int getMaxMajorRealm();
    int getMaxMinorRealm(int majorRealm);

    Component getMajorRealmName(int majorRealm);
    Component getMinorRealmName(int majorRealm,int minorRealm);
    Component getRealmName(int majorRealm,int minorRealm);

    CompositeRealmDefinition getRealmDefinition(int majorRealm);

    double getMaxProgress(int majorRealm,int minorRealm);



    TribulationDefinition getTribulation(int majorRealm, int minorRealm, RegistryAccess access);
    boolean hasTribulation(int majorRealm, int minorRealm);

    //──Path Interactions────────────────────────────────────────────────────────

    // methods marked with source treat this path as source, and opposite for those marked with target


    boolean hasSourceInteraction(Identifier target,RegistryAccess access);
    boolean hasSourceInteraction(Identifier target,PathInteractionType type,RegistryAccess access);
    boolean hasTargetInteraction(Identifier source,RegistryAccess access);
    boolean hasTargetInteraction(Identifier source,PathInteractionType type,RegistryAccess access);

    PathInteraction getSourceInteraction(Identifier target,RegistryAccess access);
    PathInteraction getTargetInteraction(Identifier source,RegistryAccess access);

    Collection<PathInteraction> getAllSourceInteractions(RegistryAccess access);
    Collection<PathInteraction> getAllTargetInteractions(RegistryAccess access);

    //──Data────────────────────────────────────────────────────────

    PathInstance newInstance(RegistryAccess access);
    PathInstance loadInstance(ValueInput input, RegistryAccess access);
    PathInstance loadInstance(ByteBuf buf, RegistryAccess access);
}
