package net.zic.ascension.impl.core.path.foundation;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.path.Path;
import net.zic.ascension.api.ascension.core.path.PathData;
import net.zic.ascension.api.ascension.core.path.interactions.PathInteractionHolder;
import net.zic.ascension.api.ascension.core.path.interactions.PathInteractionType;
import net.zic.ascension.api.ascension.core.progression.ProgressActionHolder;
import net.zic.ascension.api.ascension.core.tribulation.TribulationDefinition;
import net.zic.ascension.api.ascension.datapack.path.PathType;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.impl.core.path.PathRelationship;
import net.zic.ascension.impl.core.path.simple.SimplePathData;
import net.zic.ascension.impl.datapack.path.AscensionPathTypes;

import java.util.Collection;
import java.util.List;

/**
 * Defines a path that can cultivate foundation.
 * the final realm of foundation path is blocked behind special methods, so cultivation skills can only go up to foundationRealms.size()-2
 * @param name
 * @param description
 * @param realms
 * @param pathRelationships
 */
public record FoundationPath(Component name, Component description, List<FoundationMajorRealmDefinition> realms, List<PathRelationship> pathRelationships) implements Path {


    @Override
    public PathType getType() {
        return AscensionPathTypes.FOUNDATION_PATH_TYPE.get();
    }
    //──Foundation────────────────────────────────────────────────────────

    public int getMaxFoundationRealm(int majorRealm){
        if(majorRealm > getMaxMajorRealm()) return 0;
        return Math.max(realms.get(majorRealm).foundationRealms().size()-1,0);
    }

    public double getMaxFoundationProgress(int majorRealm,int foundationRealm){
        if (majorRealm > getMaxMajorRealm()) return 1000;
        if(realms.get(majorRealm).foundationRealms().size() >= foundationRealm) return 1000;
        return realms.get(majorRealm).foundationRealms().get(foundationRealm).progress();
    }

    public Component getFoundationRealmName(int majorRealm,int foundationRealm){
        if (majorRealm > getMaxMajorRealm()) return Component.empty();
        if(  foundationRealm>= realms.get(majorRealm).foundationRealms().size()) return Component.empty();
        return realms.get(majorRealm).foundationRealms().get(foundationRealm).name();
    }
    public ProgressActionHolder getFoundationActionHolder(int majorRealm){
        if (majorRealm > getMaxMajorRealm()) return null;
        return realms.get(majorRealm).foundationActionHolder();
    }

    public boolean tryBreakthroughFoundation(LivingEntity entity, OriginSource source, int majorRealm, int foundationRealm, double foundationProgress){
        if(majorRealm > getMaxMajorRealm()) return false;
        if(foundationRealm >= getMaxFoundationRealm(majorRealm)) return false;

        return foundationProgress >= getMaxFoundationProgress(majorRealm,foundationRealm);
    }
    //──Cultivation────────────────────────────────────────────────────────

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
    public TribulationDefinition getTribulationDefinition(int majorRealm, int minorRealm,RegistryAccess access) {

        return hasTribulation(majorRealm,minorRealm) ? realms.get(majorRealm).minorRealms().get(minorRealm).tribulationReference().resolve(access) :null;
    }

    @Override
    public boolean hasTribulation(int majorRealm, int minorRealm) {
        if(majorRealm>=realms.size())return false;
        if(minorRealm>= realms.get(majorRealm).minorRealms().size()) return false;

        return realms.get(majorRealm).minorRealms().get(minorRealm).tribulationReference() != null;
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
        return new FoundationPathData(CoreRegistries.PATH_REGISTRY.get(access).getKey(this));
    }

    @Override
    public PathData loadData(ValueInput input, RegistryAccess access) {
        SimplePathData pathData = new FoundationPathData(CoreRegistries.PATH_REGISTRY.get(access).getKey(this));
        pathData.load(input, access);
        return pathData;
    }

    @Override
    public PathData loadData(ByteBuf buf, RegistryAccess access) {
        SimplePathData pathData = new FoundationPathData(CoreRegistries.PATH_REGISTRY.get(access).getKey(this));
        pathData.decode(buf,access);
        return pathData;
    }
}
