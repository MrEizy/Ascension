package net.zic.ascension.api.ascension.core.physique;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.zenithlib.nbt.NbtHelpers;
import net.zic.zenithlib.network.ByteBufHelpers;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Optional;

/**
 * A physique held by an origin source.
 * Wraps a Physique and its data, and defers calls to the physique so it always receives its own typed data.
 */
public final class PhysiqueInstance<D extends PhysiqueData> {
    private final Identifier physiqueId;
    private final Physique<D> physique;
    private final D data;

    PhysiqueInstance(Identifier physiqueId, Physique<D> physique, D data) {
        this.physiqueId = physiqueId;
        this.physique = physique;
        this.data = data;
    }

    //creates an instance with fresh data
    public static <D extends PhysiqueData> PhysiqueInstance<D> create(Identifier physiqueId, Physique<D> physique, RegistryAccess access) {
        return new PhysiqueInstance<>(physiqueId, physique, physique.newData(access));
    }

    //creates a fresh instance by looking up the physique, null if it does not exist
    public static @Nullable PhysiqueInstance<?> create(Identifier physiqueId, RegistryAccess access) {
        Physique<?> physique = CoreRegistries.safeAccess(CoreRegistries.PHYSIQUE_REGISTRY, physiqueId, access);
        return physique == null ? null : create(physiqueId, physique, access);
    }

    //──Getters────────────────────────────────────────────────────────
    public Identifier getPhysiqueId() {return physiqueId;}
    public Physique<D> getPhysique() {return physique;}
    public D getData() {return data;}

    public Component getName() {return physique.getName(data);}
    public Component getDescription() {return physique.getDescription(data);}

    /**
     * narrows this instance to a concrete physique class, e.g. {@code instance.as(SimplePhysique.class)}.
     * the data is always created by the physique, so it always matches the physique's data type
     * @return this instance typed to the physique, or null if the physique is not of that class
     */
    @SuppressWarnings("unchecked")
    public <E extends PhysiqueData, T extends Physique<E>> @Nullable PhysiqueInstance<E> as(Class<T> physiqueClass) {
        return physiqueClass.isInstance(physique) ? (PhysiqueInstance<E>) this : null;
    }

    //──Dispatch────────────────────────────────────────────────────────

    //returns the paths the physique wants to try and add
    public Collection<Identifier> onAdded(OriginSource source) {
        return physique.onAdded(source, this);
    }

    //returns the paths the physique wants to try and remove
    public Collection<Identifier> onRemoved(OriginSource source) {
        return physique.onRemoved(source, this);
    }

    public void applyToEntity(LivingEntity entity) {
        physique.applyToEntity(entity, this);
    }

    public void removeFromEntity(LivingEntity entity) {
        physique.removeFromEntity(entity, this);
    }

    //──Serialization────────────────────────────────────────────────────────

    public void write(ValueOutput output, RegistryAccess access) {
        NbtHelpers.writeIdentifier(output, "physique", physiqueId);
        physique.serializerHandler().write(data, output.child("data"), access);
    }

    //loads a saved instance, returns null if the physique no longer exists
    public static @Nullable PhysiqueInstance<?> read(ValueInput input, RegistryAccess access) {
        Identifier physiqueId = NbtHelpers.readIdentifier(input, "physique");
        if (physiqueId == null) return null;
        Physique<?> physique = CoreRegistries.safeAccess(CoreRegistries.PHYSIQUE_REGISTRY, physiqueId, access);
        if (physique == null) return null;
        return readTyped(input, physiqueId, physique, access);
    }

    private static <D extends PhysiqueData> PhysiqueInstance<D> readTyped(ValueInput input, Identifier physiqueId, Physique<D> physique, RegistryAccess access) {
        Optional<ValueInput> dataInput = input.child("data");
        D data = dataInput.map(child -> physique.serializerHandler().read(child, access)).orElse(null);
        if (data == null) data = physique.newData(access);
        return new PhysiqueInstance<>(physiqueId, physique, data);
    }

    //──Sync────────────────────────────────────────────────────────

    //the data is length prefixed so a physique the client does not know can be skipped
    public void encode(ByteBuf buf, RegistryAccess access) {
        ByteBufHelpers.encodeIdentifier(physiqueId, buf);
        ByteBuf dataBuf = Unpooled.buffer();
        physique.syncHandler().encode(data, dataBuf, access);
        buf.writeInt(dataBuf.readableBytes());
        buf.writeBytes(dataBuf);
    }

    //decodes an instance, returns null if the physique does not exist on this side
    public static @Nullable PhysiqueInstance<?> decode(ByteBuf buf, RegistryAccess access) {
        Identifier physiqueId = ByteBufHelpers.decodeIdentifier(buf);
        int length = buf.readInt();
        ByteBuf dataBuf = buf.readSlice(length);
        Physique<?> physique = CoreRegistries.safeAccess(CoreRegistries.PHYSIQUE_REGISTRY, physiqueId, access);
        if (physique == null) return null;
        return decodeTyped(dataBuf, physiqueId, physique, access);
    }

    private static <D extends PhysiqueData> PhysiqueInstance<D> decodeTyped(ByteBuf buf, Identifier physiqueId, Physique<D> physique, RegistryAccess access) {
        D data = physique.newData(access);
        physique.syncHandler().decode(data, buf, access);
        return new PhysiqueInstance<>(physiqueId, physique, data);
    }
}
