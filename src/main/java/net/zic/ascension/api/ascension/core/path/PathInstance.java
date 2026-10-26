package net.zic.ascension.api.ascension.core.path;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.path.realm.Realm;
import net.zic.ascension.api.ascension.core.path.realm.TribulationMode;
import net.zic.ascension.api.ascension.core.tribulation.TribulationInstance;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.zenithlib.nbt.NbtHelpers;
import net.zic.zenithlib.network.ByteBufHelpers;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * A path held by an origin source.
 * Wraps a Path and its data, and defers calls to the path so it always receives its own typed data.
 */
public final class PathInstance<D extends PathData> {
    private final Identifier pathId;
    private final Path<D> path;
    private final D data;

    PathInstance(Identifier pathId, Path<D> path, D data) {
        this.pathId = pathId;
        this.path = path;
        this.data = data;
    }

    //creates an instance with fresh data
    public static <D extends PathData> PathInstance<D> create(Identifier pathId, Path<D> path, RegistryAccess access) {
        return new PathInstance<>(pathId, path, path.newData(access));
    }

    //creates a fresh instance by looking up the path, null if it does not exist
    public static @Nullable PathInstance<?> create(Identifier pathId, RegistryAccess access) {
        Path<?> path = CoreRegistries.safeAccess(CoreRegistries.PATH_REGISTRY, pathId, access);
        return path == null ? null : create(pathId, path, access);
    }

    //──Getters────────────────────────────────────────────────────────
    public Identifier getPathId() {return pathId;}
    public Path<D> getPath() {return path;}
    public D getData() {return data;}

    public Component getName() {return path.getName(data);}
    public Component getDescription() {return path.getDescription(data);}

    /**
     * narrows this instance to a concrete path class, e.g. {@code instance.as(SimplePath.class)}.
     * the data is always created by the path, so it always matches the path's data type
     * @return this instance typed to the path, or null if the path is not of that class
     */
    @SuppressWarnings("unchecked")
    public <E extends PathData, P extends Path<E>> @Nullable PathInstance<E> as(Class<P> pathClass) {
        return pathClass.isInstance(path) ? (PathInstance<E>) this : null;
    }

    //──Realms────────────────────────────────────────────────────────

    public double getProgress() {return data.getProgress();}
    public double getMaxProgress() {return path.getMaxProgress(data);}

    public boolean canProgress() {return path.canProgress(data);}
    public boolean canBreakthrough() {return path.canBreakthrough(data);}

    public int getCurrentMajorRealm() {return data.getCurrentMajorRealm();}
    public int getCurrentMinorRealm() {return data.getCurrentMinorRealm();}
    public Realm getCurrentRealm() {return data.getCurrentRealm();}
    public Realm getPreviousRealm() {return path.getPreviousRealm(data);}
    public Realm getNextRealm() {return path.getNextRealm(data);}
    public int getMaxMinorRealm(int majorRealm) {return path.getMaxMinorRealm(data, majorRealm);}

    //──Tribulations────────────────────────────────────────────────────────

    //the tribulation of the current realm, null if there is none or the path is not cultivated
    public @Nullable Identifier getCurrentTribulationId() {
        if (getCurrentMajorRealm() < 0) return null;
        return path.getTribulationId(getCurrentMajorRealm(), getCurrentMinorRealm());
    }

    //how the current realm's tribulation is started
    public TribulationMode getTribulationMode() {
        if (getCurrentMajorRealm() < 0) return TribulationMode.AUTO;
        return path.getTribulationMode(getCurrentMajorRealm(), getCurrentMinorRealm());
    }

    public boolean canTriggerTribulation(Identifier tribulation) {
        return path.canTriggerTribulation(data, tribulation);
    }

    /**
     * triggers the current realm's MANUAL tribulation, prefer {@link net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper#triggerPathTribulation}
     * as it also syncs the path
     * @param tribulation the tribulation the caller expects to trigger
     * @param beforeStart runs before the tribulation starts, used to modify its data
     * @return the started tribulation, or null if it could not be triggered
     */
    public @Nullable TribulationInstance<?> triggerTribulation(OriginSource source, Identifier tribulation, LivingEntity target,
                                                               @Nullable Consumer<TribulationInstance<?>> beforeStart) {
        return path.triggerTribulation(source, this, tribulation, target, beforeStart);
    }

    public @Nullable TribulationInstance<?> getActiveTribulation() {
        return path.getActiveTribulation(data);
    }

    //──Dispatch────────────────────────────────────────────────────────

    /**
     * progresses path by amount
     * @param amount the amount we are progressing by
     * @param source the origin source this path is attached too
     * @param entitySource the entity that caused the path to progress
     */
    public void progressPath(double amount, OriginSource source, LivingEntity entitySource) {
        path.progress(source, this, amount, entitySource);
    }

    public void handleRealmChange(Realm newRealm, OriginSource source) {
        path.changeRealm(source, this, newRealm);
    }

    //caches the current state then simulates applying it
    public void simulateProgression(OriginSource source) {
        path.simulateProgression(source, this);
    }

    //removes it from a specific source while maintaining data
    public void removeFromSource(OriginSource source) {
        path.removeFromSource(source, this);
    }

    //──Serialization────────────────────────────────────────────────────────

    public void write(ValueOutput output, RegistryAccess access) {
        NbtHelpers.writeIdentifier(output, "path", pathId);
        path.serializerHandler().write(data, output.child("data"), access);
    }

    //loads a saved instance, returns null if the path no longer exists
    public static @Nullable PathInstance<?> read(ValueInput input, RegistryAccess access) {
        Identifier pathId = NbtHelpers.readIdentifier(input, "path");
        if (pathId == null) return null;
        Path<?> path = CoreRegistries.safeAccess(CoreRegistries.PATH_REGISTRY, pathId, access);
        if (path == null) return null;
        return readTyped(input, pathId, path, access);
    }

    private static <D extends PathData> PathInstance<D> readTyped(ValueInput input, Identifier pathId, Path<D> path, RegistryAccess access) {
        Optional<ValueInput> dataInput = input.child("data");
        D data = dataInput.map(child -> path.serializerHandler().read(child, access)).orElse(null);
        if (data == null) data = path.newData(access);
        return new PathInstance<>(pathId, path, data);
    }

    //──Sync────────────────────────────────────────────────────────

    //the data is length prefixed so a path the client does not know can be skipped
    public void encode(ByteBuf buf, RegistryAccess access) {
        ByteBufHelpers.encodeIdentifier(pathId, buf);
        ByteBuf dataBuf = Unpooled.buffer();
        path.syncHandler().encode(data, dataBuf, access);
        buf.writeInt(dataBuf.readableBytes());
        buf.writeBytes(dataBuf);
    }

    //decodes an instance, returns null if the path does not exist on this side
    public static @Nullable PathInstance<?> decode(ByteBuf buf, RegistryAccess access) {
        Identifier pathId = ByteBufHelpers.decodeIdentifier(buf);
        int length = buf.readInt();
        ByteBuf dataBuf = buf.readSlice(length);
        Path<?> path = CoreRegistries.safeAccess(CoreRegistries.PATH_REGISTRY, pathId, access);
        if (path == null) return null;
        return decodeTyped(dataBuf, pathId, path, access);
    }

    private static <D extends PathData> PathInstance<D> decodeTyped(ByteBuf buf, Identifier pathId, Path<D> path, RegistryAccess access) {
        D data = path.newData(access);
        path.syncHandler().decode(data, buf, access);
        return new PathInstance<>(pathId, path, data);
    }
}
