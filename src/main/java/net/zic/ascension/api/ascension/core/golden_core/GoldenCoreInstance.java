package net.zic.ascension.api.ascension.core.golden_core;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.path.PathInstance;
import net.zic.ascension.api.ascension.core.path.realm.Realm;
import net.zic.ascension.api.ascension.core.progression.ProgressDirection;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.zenithlib.nbt.NbtHelpers;
import net.zic.zenithlib.network.ByteBufHelpers;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * A golden core held by an origin source.
 * Wraps a GoldenCore and its data, and defers calls to the core so it always receives its own typed data.
 * <p>
 * Each instance is unique by its {@link #getId()}, multiple instances of the same golden core can be held at once.
 * Realm actions are tracked with a cursor ({@link #getAppliedRealm()}) that is never saved, every realm in
 * (acquisition realm, applied realm] has had its UP actions run. {@link #syncRealms} moves the cursor one realm at a time,
 * so calling it multiple times with the same target does nothing.
 */
public final class GoldenCoreInstance<D extends GoldenCoreData> {
    private final UUID id;
    private final Identifier coreId;
    private final GoldenCore<D> core;
    private final D data;
    private final Identifier path;
    private final Realm acquisitionRealm;

    //runtime only, null means no realm above the acquisition realm is applied
    private @Nullable Realm appliedRealm;
    //the path instance the applied realms ran with, used to roll back once the path is no longer held
    private @Nullable PathInstance<?> appliedPath;

    GoldenCoreInstance(UUID id, Identifier coreId, GoldenCore<D> core, D data, Identifier path, Realm acquisitionRealm) {
        this.id = id;
        this.coreId = coreId;
        this.core = core;
        this.data = data;
        this.path = path;
        this.acquisitionRealm = acquisitionRealm;
    }

    //creates an instance with fresh data
    public static <D extends GoldenCoreData> GoldenCoreInstance<D> create(UUID id, Identifier coreId, GoldenCore<D> core, Identifier path, Realm acquisitionRealm, RegistryAccess access) {
        return new GoldenCoreInstance<>(id, coreId, core, core.newData(access), path, acquisitionRealm);
    }

    //creates a fresh instance by looking up the core, null if it does not exist
    public static @Nullable GoldenCoreInstance<?> create(UUID id, Identifier coreId, Identifier path, Realm acquisitionRealm, RegistryAccess access) {
        GoldenCore<?> core = CoreRegistries.safeAccess(CoreRegistries.GOLDEN_CORE_REGISTRY, coreId, access);
        return core == null ? null : create(id, coreId, core, path, acquisitionRealm, access);
    }

    //a copy of this instance with new details, keeps the id, core and data
    public GoldenCoreInstance<D> withDetails(Identifier path, Realm acquisitionRealm) {
        return new GoldenCoreInstance<>(id, coreId, core, data, path, acquisitionRealm);
    }

    public boolean hasSameDetails(GoldenCoreInstance<?> other) {
        return path.equals(other.path) && acquisitionRealm.equals(other.acquisitionRealm);
    }

    //──Getters────────────────────────────────────────────────────────
    public UUID getId() {return id;}
    public Identifier getCoreId() {return coreId;}
    public GoldenCore<D> getCore() {return core;}
    public D getData() {return data;}
    public Identifier getPath() {return path;}
    public Realm getAcquisitionRealm() {return acquisitionRealm;}
    public @Nullable Realm getAppliedRealm() {return appliedRealm;}

    public Component getName() {return core.getName(data);}
    public Component getDescription() {return core.getDescription(data);}

    /**
     * a per-instance identifier, used as the owner/context of anything this instance adds (e.g. skills).
     * the core id can not be used as multiple instances of the same core can be held
     */
    public Identifier contextId() {
        return Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "golden_core/" + id);
    }

    /**
     * narrows this instance to a concrete core class, e.g. {@code instance.as(SimpleGoldenCore.class)}.
     * @return this instance typed to the core, or null if the core is not of that class
     */
    @SuppressWarnings("unchecked")
    public <E extends GoldenCoreData, T extends GoldenCore<E>> @Nullable GoldenCoreInstance<E> as(Class<T> coreClass) {
        return coreClass.isInstance(core) ? (GoldenCoreInstance<E>) this : null;
    }

    //──Dispatch────────────────────────────────────────────────────────

    public void onAdded(OriginSource source) {
        core.onAdded(source, this);
    }

    public void onRemoved(OriginSource source) {
        core.onRemoved(source, this);
    }

    public void applyToEntity(LivingEntity entity) {
        core.applyToEntity(entity, this);
    }

    public void removeFromEntity(LivingEntity entity) {
        core.removeFromEntity(entity, this);
    }

    //──Realm Tracking────────────────────────────────────────────────────────

    //the realm the tracked path is currently at, null if the path is not held or not cultivated
    public @Nullable Realm targetRealm(OriginSource source) {
        PathInstance<?> pathInstance = AscensionOriginSourceHelper.getPathInstance(source, path);
        if (pathInstance == null || pathInstance.getCurrentMajorRealm() < 0) return null;
        return pathInstance.getCurrentRealm();
    }

    public void syncRealms(OriginSource source) {
        syncRealms(source, null, targetRealm(source));
    }

    /**
     * runs realm actions until every realm in (acquisition realm, target] is applied and nothing above target is.
     * @param pathInstance the tracked path instance, if null it is looked up from the source
     * @param target the realm to sync to, null rolls back every applied realm
     */
    public void syncRealms(OriginSource source, @Nullable PathInstance<?> pathInstance, @Nullable Realm target) {
        if (pathInstance == null) pathInstance = AscensionOriginSourceHelper.getPathInstance(source, path);
        if (pathInstance == null) pathInstance = appliedPath;
        if (target != null && target.compareTo(acquisitionRealm) <= 0) target = null;

        Realm from = appliedRealm == null ? acquisitionRealm : appliedRealm;
        Realm to = target == null ? acquisitionRealm : target;
        if (from.equals(to)) return;
        if (pathInstance == null) {
            //nothing can be applied without the path, and nothing was applied without it either
            appliedRealm = null;
            return;
        }

        if (to.compareTo(from) > 0) {
            appliedPath = pathInstance;
            for (Realm realm : realmsBetween(from, to, pathInstance)) {
                core.onRealmChange(source, this, pathInstance, realm, ProgressDirection.UP);
                appliedRealm = realm;
            }
        } else {
            List<Realm> realms = realmsBetween(to, from, pathInstance);
            for (int i = realms.size() - 1; i >= 0; i--) {
                core.onRealmChange(source, this, pathInstance, realms.get(i), ProgressDirection.DOWN);
                appliedRealm = i == 0 ? null : realms.get(i - 1);
            }
            if (to.compareTo(acquisitionRealm) > 0) appliedRealm = to;
        }
        if (appliedRealm == null) appliedPath = null;
    }

    //ordered realms in (start, end]
    private static List<Realm> realmsBetween(Realm start, Realm end, PathInstance<?> pathInstance) {
        List<Realm> realms = new ArrayList<>();
        Realm realm = start;
        while (realm.compareTo(end) < 0) {
            //limit broken realms can go past the max minor realm, so never skip past the end's major realm
            boolean nextMajor = realm.majorRealm() != end.majorRealm()
                    && realm.minorRealm() >= pathInstance.getPath().getMaxMinorRealm(realm.majorRealm());
            realm = nextMajor ? Realm.of(realm.majorRealm() + 1, 0) : Realm.of(realm.majorRealm(), realm.minorRealm() + 1);
            realms.add(realm);
        }
        return realms;
    }

    //──Serialization────────────────────────────────────────────────────────

    public void write(ValueOutput output, RegistryAccess access) {
        output.store("id", UUIDUtil.CODEC, id);
        NbtHelpers.writeIdentifier(output, "golden_core", coreId);
        NbtHelpers.writeIdentifier(output, "path", path);
        output.putInt("acquisition_major", acquisitionRealm.majorRealm());
        output.putInt("acquisition_minor", acquisitionRealm.minorRealm());
        core.serializerHandler().write(data, output.child("data"), access);
    }

    //loads a saved instance, returns null if the core no longer exists
    public static @Nullable GoldenCoreInstance<?> read(ValueInput input, RegistryAccess access) {
        Optional<UUID> id = input.read("id", UUIDUtil.CODEC);
        Identifier coreId = NbtHelpers.readIdentifier(input, "golden_core");
        Identifier path = NbtHelpers.readIdentifier(input, "path");
        if (id.isEmpty() || coreId == null || path == null) return null;
        GoldenCore<?> core = CoreRegistries.safeAccess(CoreRegistries.GOLDEN_CORE_REGISTRY, coreId, access);
        if (core == null) return null;
        Realm acquisitionRealm = Realm.of(input.getIntOr("acquisition_major", 0), input.getIntOr("acquisition_minor", 0));
        return readTyped(input, id.get(), coreId, core, path, acquisitionRealm, access);
    }

    private static <D extends GoldenCoreData> GoldenCoreInstance<D> readTyped(ValueInput input, UUID id, Identifier coreId, GoldenCore<D> core,
                                                                             Identifier path, Realm acquisitionRealm, RegistryAccess access) {
        Optional<ValueInput> dataInput = input.child("data");
        D data = dataInput.map(child -> core.serializerHandler().read(child, access)).orElse(null);
        if (data == null) data = core.newData(access);
        return new GoldenCoreInstance<>(id, coreId, core, data, path, acquisitionRealm);
    }

    //──Sync────────────────────────────────────────────────────────

    public static void encodeId(UUID id, ByteBuf buf) {
        buf.writeLong(id.getMostSignificantBits());
        buf.writeLong(id.getLeastSignificantBits());
    }

    public static UUID decodeId(ByteBuf buf) {
        return new UUID(buf.readLong(), buf.readLong());
    }

    //the data is length prefixed so a core the client does not know can be skipped
    public void encode(ByteBuf buf, RegistryAccess access) {
        encodeId(id, buf);
        ByteBufHelpers.encodeIdentifier(coreId, buf);
        ByteBufHelpers.encodeIdentifier(path, buf);
        buf.writeInt(acquisitionRealm.majorRealm());
        buf.writeInt(acquisitionRealm.minorRealm());
        ByteBuf dataBuf = Unpooled.buffer();
        core.syncHandler().encode(data, dataBuf, access);
        buf.writeInt(dataBuf.readableBytes());
        buf.writeBytes(dataBuf);
    }

    //decodes an instance, returns null if the core does not exist on this side
    public static @Nullable GoldenCoreInstance<?> decode(ByteBuf buf, RegistryAccess access) {
        UUID id = decodeId(buf);
        Identifier coreId = ByteBufHelpers.decodeIdentifier(buf);
        Identifier path = ByteBufHelpers.decodeIdentifier(buf);
        Realm acquisitionRealm = Realm.of(buf.readInt(), buf.readInt());
        int length = buf.readInt();
        ByteBuf dataBuf = buf.readSlice(length);
        GoldenCore<?> core = CoreRegistries.safeAccess(CoreRegistries.GOLDEN_CORE_REGISTRY, coreId, access);
        if (core == null) return null;
        return decodeTyped(dataBuf, id, coreId, core, path, acquisitionRealm, access);
    }

    private static <D extends GoldenCoreData> GoldenCoreInstance<D> decodeTyped(ByteBuf buf, UUID id, Identifier coreId, GoldenCore<D> core,
                                                                               Identifier path, Realm acquisitionRealm, RegistryAccess access) {
        D data = core.newData(access);
        core.syncHandler().decode(data, buf, access);
        return new GoldenCoreInstance<>(id, coreId, core, data, path, acquisitionRealm);
    }
}
