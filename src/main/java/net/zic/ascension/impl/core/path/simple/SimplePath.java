package net.zic.ascension.impl.core.path.simple;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.ValueInput;
import net.zic.ascension.api.ascension.core.path.Path;
import net.zic.ascension.api.ascension.core.path.interaction.PathInteraction;
import net.zic.ascension.api.ascension.core.path.interaction.PathInteractionType;
import net.zic.ascension.api.ascension.core.progression.ProgressActionHolder;
import net.zic.ascension.api.ascension.core.tribulation.TribulationDefinition;
import net.zic.ascension.api.ascension.datapack.path.PathType;
import net.zic.ascension.configuration.interactions.PathInteractions;
import net.zic.ascension.impl.core.path.realms.MajorRealmDefinition;
import net.zic.ascension.impl.datapack.path.AscensionPathTypes;

import java.util.Collection;
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

    //──Realms────────────────────────────────────────────────────────

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
    //──Tribulation────────────────────────────────────────────────────────
    @Override
    public TribulationDefinition getTribulation(int majorRealm, int minorRealm, RegistryAccess access) {
        return hasTribulation(majorRealm,minorRealm) ? getRealmDefinition(majorRealm).getRealmTribulation(minorRealm).resolve(access) : null;
    }

    @Override
    public boolean hasTribulation(int majorRealm, int minorRealm) {
        return getRealmDefinition(majorRealm).getRealmTribulation(minorRealm) != null;
    }

    //──Path Interactions────────────────────────────────────────────────────────



    @Override
    public boolean hasSourceInteraction(Identifier target, RegistryAccess access) {
        return PathInteractions.hasInteraction(getId(access),target);
    }

    @Override
    public boolean hasSourceInteraction(Identifier target, PathInteractionType type, RegistryAccess access) {
        return PathInteractions.hasInteraction(getId(access),target,type);
    }

    @Override
    public boolean hasTargetInteraction(Identifier source, RegistryAccess access) {
        return PathInteractions.hasInteraction(source,getId(access));
    }

    @Override
    public boolean hasTargetInteraction(Identifier source, PathInteractionType type, RegistryAccess access) {
        return PathInteractions.hasInteraction(source,getId(access),type);
    }

    @Override
    public PathInteraction getSourceInteraction(Identifier target, RegistryAccess access) {
        return PathInteractions.getInteraction(getId(access),target);
    }

    @Override
    public PathInteraction getTargetInteraction(Identifier source, RegistryAccess access) {
        return PathInteractions.getInteraction(source,getId(access));
    }

    @Override
    public Collection<PathInteraction> getAllSourceInteractions(RegistryAccess access) {
        return PathInteractions.getInteractionsForSource(getId(access));
    }

    @Override
    public Collection<PathInteraction> getAllTargetInteractions(RegistryAccess access) {
        return PathInteractions.getInteractionsForTarget(getId(access));
    }


    //──Data────────────────────────────────────────────────────────

    @Override
    public SimplePathInstance newInstance(RegistryAccess access) {
        return new SimplePathInstance(this);
    }

    @Override
    public SimplePathInstance loadInstance(ValueInput input, RegistryAccess access) {
        SimplePathInstance pathInstance = newInstance(access);
        pathInstance.read(input,access);
        return pathInstance; //TODO
    }

    @Override
    public SimplePathInstance loadInstance(ByteBuf buf, RegistryAccess access) {
        SimplePathInstance instance = newInstance(access);
        instance.decode(buf,access);
        return instance;
    }
}
