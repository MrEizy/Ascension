package net.zic.ascension.impl.core.path.simple;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.ValueInput;
import net.zic.ascension.api.ascension.core.path.PathInstance;
import net.zic.ascension.api.ascension.core.path.Path;
import net.zic.ascension.api.ascension.core.path.interactions.PathInteractionHolder;
import net.zic.ascension.api.ascension.core.path.realm.CompositeRealmDefinition;
import net.zic.ascension.api.ascension.core.progression.ProgressActionHolder;
import net.zic.ascension.api.ascension.core.tribulation.TribulationDefinition;
import net.zic.ascension.api.ascension.datapack.path.PathType;
import net.zic.ascension.impl.core.path.realms.MajorRealmDefinition;
import net.zic.ascension.impl.datapack.path.AscensionPathTypes;

import java.util.List;

public class SimplePath implements Path {

    private final Component name;
    private final Component description;
    private final List<MajorRealmDefinition> realmDefinitions;
    private final ProgressActionHolder actions;

    public SimplePath(Component name, Component description, List<MajorRealmDefinition> realmDefinitions, ProgressActionHolder actions) {
        this.name = name;
        this.description = description;
        this.realmDefinitions = realmDefinitions;
        this.actions = actions;
    }
    public List<MajorRealmDefinition> getMajorRealmDefinitions(){
        return realmDefinitions;
    }
    public ProgressActionHolder getProgressActionHolder(){
        return actions;
    }
    @Override
    public PathType getType() {
        return AscensionPathTypes.SIMPLE_PATH_TYPE.get();
    }

    @Override
    public Component name() {
        return name;
    }

    @Override
    public Component description() {
        return description;
    }

    @Override
    public int getMaxMajorRealm() {
        return realmDefinitions.size();
    }

    @Override
    public int getMaxMinorRealm(int majorRealm) {
        return getRealmDefinition(majorRealm).getMaxRealm();
    }

    @Override
    public Component getMajorRealmName(int majorRealm) {
        return getRealmDefinition(majorRealm).getName();
    }

    @Override
    public Component getMinorRealmName(int majorRealm, int minorRealm) {
        return getRealmDefinition(majorRealm).getMinorRealmName(minorRealm);
    }

    @Override
    public Component getRealmName(int majorRealm, int minorRealm) {
        return majorRealm < 0 ? Component.literal("Mortal") :getRealmDefinition(majorRealm).getCompositeRealmName(minorRealm);
    }

    @Override
    public MajorRealmDefinition getRealmDefinition(int majorRealm) {

        return realmDefinitions.isEmpty() ? null : realmDefinitions.get(majorRealm);
    }

    @Override
    public double getMaxProgress(int majorRealm, int minorRealm) {
        return getRealmDefinition(majorRealm).getMaxProgress(minorRealm);
    }

    @Override
    public TribulationDefinition getTribulation(int majorRealm, int minorRealm, RegistryAccess access) {
        return hasTribulation(majorRealm,minorRealm) ? getRealmDefinition(majorRealm).getRealmTribulation(minorRealm).resolve(access) : null;
    }

    @Override
    public boolean hasTribulation(int majorRealm, int minorRealm) {
        return getRealmDefinition(majorRealm).getRealmTribulation(minorRealm) != null;
    }

    @Override
    public void registerInteractions(PathInteractionHolder holder, RegistryAccess access) {
        //TODO
    }

    @Override
    public PathInstance newInstance(RegistryAccess access) {
        return new SimplePathInstance(this);
    }

    @Override
    public PathInstance loadInstance(ValueInput input, RegistryAccess access) {
        return null; //TODO
    }

    @Override
    public PathInstance loadInstance(ByteBuf buf, RegistryAccess access) {
        return null; //TODO
    }
}
