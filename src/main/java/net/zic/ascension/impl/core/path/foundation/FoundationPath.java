package net.zic.ascension.impl.core.path.foundation;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.ValueInput;
import net.zic.ascension.api.core.CoreRegistries;
import net.zic.ascension.api.core.path.Path;
import net.zic.ascension.api.core.path.PathData;
import net.zic.ascension.api.core.path.interactions.PathInteractionHolder;
import net.zic.ascension.api.core.path.interactions.PathInteractionType;
import net.zic.ascension.api.datapack.path.PathType;
import net.zic.ascension.impl.core.path.PathRelationship;
import net.zic.ascension.impl.core.path.MajorRealmDefinition;
import net.zic.ascension.impl.core.path.RealmDefinition;
import net.zic.ascension.impl.datapack.path.AscensionPathTypes;

import java.util.Collection;
import java.util.List;

/**
 * Defines a path that can cultivate foundation.
 * the final realm of foundation path is blocked behind special methods, so cultivation skills can only go up to foundationRealms.size()-2
 * @param name
 * @param description
 * @param realms
 * @param foundationRealms
 * @param pathRelationships
 */
public record FoundationPath(Component name, Component description, List<MajorRealmDefinition> realms, List<RealmDefinition> foundationRealms, List<PathRelationship> pathRelationships) implements Path {


    @Override
    public PathType getType() {
        return AscensionPathTypes.FOUNDATION_PATH_TYPE.get();
    }

    @Override
    public Component getMajorRealmName(int majorRealm) {

        return realms.size() < majorRealm ? Component.empty() : realms.get(majorRealm).name();
    }

    @Override
    public Component getMinorRealmName(int majorRealm, int minorRealm) {
        return realms.size() < majorRealm ?
                Component.empty() :
                (realms.get(majorRealm).minorRealms().size() < minorRealm ?
                        Component.empty() :
                        realms.get(majorRealm).minorRealms().get(minorRealm).name());
    }

    @Override
    public Component getRealmName(int majorRealm, int minorRealm) {

        return Component.empty()
                .append(getMajorRealmName(majorRealm))
                .append("(")
                .append(getMinorRealmName(majorRealm, minorRealm))
                .append(")");
    }

    //returns the INDEX of the max major realm
    @Override
    public int getMaxMajorRealm() {
        return realms.size() - 1;
    }

    //returns the INDEX of the max minor realm
    @Override
    public int getMaxMinorRealm(int majorRealm) {
        if (majorRealm > getMaxMajorRealm()) return 0;
        return realms.get(majorRealm).minorRealms().size() - 1;
    }

    @Override
    public double getMaxProgress(int majorRealm, int minorRealm) {
        if(majorRealm > getMaxMajorRealm()) return 100;
        if(realms.get(majorRealm).minorRealms().size() <= minorRealm) return 100;
        return realms.get(majorRealm).minorRealms().get(minorRealm).progress();
    }

    @Override
    public double getInteractionValue(Identifier path) {
        return 0; //TODO
    }

    @Override
    public PathInteractionType getInteractionType(Identifier path) {
        return null; //TODO
    }

    @Override
    public Collection<Identifier> getPathsOfInteraction(PathInteractionType type) {
        return List.of(); //TODO
    }

    @Override
    public void registerInteractions(PathInteractionHolder holder,RegistryAccess access) {
        Identifier selfIdentifier = CoreRegistries.PATH_REGISTRY.get(access).getKey(this);
        for(PathRelationship relationship : pathRelationships){
            holder.registerInteraction(relationship.asInteraction(selfIdentifier));
        }
    }
    @Override
    public PathData newData(RegistryAccess access) {
        return null;
    }

    @Override
    public PathData loadData(ValueInput input, RegistryAccess access) {
        return null;
    }

    @Override
    public PathData loadData(ByteBuf buf, RegistryAccess access) {
        return null;
    }
}
