package net.zic.ascension.impl.core.path;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.ValueInput;
import net.zic.ascension.api.core.CoreRegistries;
import net.zic.ascension.api.core.path.Path;
import net.zic.ascension.api.core.path.PathData;
import net.zic.ascension.api.core.path.interactions.PathInteractionHolder;
import net.zic.ascension.api.core.path.interactions.PathInteractionType;
import net.zic.ascension.api.datapack.path.PathType;

import java.util.Collection;
import java.util.List;

public record SimplePath(Component name, Component description, List<MajorRealm> realmNames) implements Path {
    public record MajorRealm(Component name, List<Component> minorRealms) {

        public static Codec<MajorRealm> CODEC =
                RecordCodecBuilder.create(instance -> instance.group(
                        ComponentSerialization.CODEC.fieldOf("name").forGetter(MajorRealm::name),
                        ComponentSerialization.CODEC.listOf()
                                .fieldOf("minor_realms")
                                .forGetter(MajorRealm::minorRealms)
                ).apply(instance, MajorRealm::new));
    }

    @Override
    public PathType getType() {
        return null;
    }

    @Override
    public Component getMajorRealmName(int majorRealm) {

        return realmNames.size() < majorRealm ? Component.empty() : realmNames.get(majorRealm).name;
    }

    @Override
    public Component getMinorRealmName(int majorRealm, int minorRealm) {
        return realmNames.size() < majorRealm ?
                Component.empty() :
                (realmNames.get(majorRealm).minorRealms.size() < minorRealm ?
                        Component.empty() :
                        realmNames.get(majorRealm).minorRealms.get(minorRealm));
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
        return realmNames.size() - 1;
    }

    //returns the INDEX of the max minor realm
    @Override
    public int getMaxMinorRealm(int majorRealm) {
        if (majorRealm > getMaxMajorRealm()) return 0;
        return realmNames.get(majorRealm).minorRealms().size() - 1;
    }

    @Override
    public double getMaxProgress(int majorRealm, int minorRealm) {
        return 100;//TODO
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
    public void registerInteractions(PathInteractionHolder holder) {

    }

    @Override
    public PathData newData(RegistryAccess access) {
        return new SimplePathData(CoreRegistries.PATH_REGISTRY.get(access).getKey(this));
    }

    @Override
    public PathData loadData(ValueInput input, RegistryAccess access) {
        SimplePathData pathData = new SimplePathData(CoreRegistries.PATH_REGISTRY.get(access).getKey(this));
        pathData.load(input, access);
        return pathData;
    }

    @Override
    public PathData loadData(ByteBuf buf, RegistryAccess access) {
        SimplePathData pathData = new SimplePathData(CoreRegistries.PATH_REGISTRY.get(access).getKey(this));
        pathData.decode(buf,access);
        return pathData;
    }
}
