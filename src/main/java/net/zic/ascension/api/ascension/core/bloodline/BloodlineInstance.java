package net.zic.ascension.api.ascension.core.bloodline;

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
 * A bloodline held by an origin source.
 * Wraps a Bloodline and its data, and defers calls to the bloodline so it always receives its own typed data.
 */
public final class BloodlineInstance<D extends BloodlineData> {
    private final Identifier bloodlineId;
    private final Bloodline<D> bloodline;
    private final D data;

    BloodlineInstance(Identifier bloodlineId, Bloodline<D> bloodline, D data) {
        this.bloodlineId = bloodlineId;
        this.bloodline = bloodline;
        this.data = data;
    }

    //creates an instance with fresh data
    public static <D extends BloodlineData> BloodlineInstance<D> create(Identifier bloodlineId, Bloodline<D> bloodline, RegistryAccess access) {
        return new BloodlineInstance<>(bloodlineId, bloodline, bloodline.newData(access));
    }

    //creates a fresh instance by looking up the bloodline, null if it does not exist
    public static @Nullable BloodlineInstance<?> create(Identifier bloodlineId, RegistryAccess access) {
        Bloodline<?> bloodline = CoreRegistries.safeAccess(CoreRegistries.BLOODLINE_REGISTRY, bloodlineId, access);
        return bloodline == null ? null : create(bloodlineId, bloodline, access);
    }

    //──Getters────────────────────────────────────────────────────────
    public Identifier getBloodlineId() {return bloodlineId;}
    public Bloodline<D> getBloodline() {return bloodline;}
    public D getData() {return data;}

    public Component getName() {return bloodline.getName(data);}
    public Component getDescription() {return bloodline.getDescription(data);}

    /**
     * narrows this instance to a concrete bloodline class, e.g. {@code instance.as(SimpleBloodline.class)}.
     * the data is always created by the bloodline, so it always matches the bloodline's data type
     * @return this instance typed to the bloodline, or null if the bloodline is not of that class
     */
    @SuppressWarnings("unchecked")
    public <E extends BloodlineData, T extends Bloodline<E>> @Nullable BloodlineInstance<E> as(Class<T> bloodlineClass) {
        return bloodlineClass.isInstance(bloodline) ? (BloodlineInstance<E>) this : null;
    }

    //──Dispatch────────────────────────────────────────────────────────

    //returns the paths the bloodline wants to try and add
    public Collection<Identifier> onAdded(OriginSource source) {
        return bloodline.onAdded(source, this);
    }

    //returns the paths the bloodline wants to try and remove
    public Collection<Identifier> onRemoved(OriginSource source) {
        return bloodline.onRemoved(source, this);
    }

    public void handlePurityChange(OriginSource source, int newPurity) {
        bloodline.handlePurityChange(source, this, newPurity);
    }

    public void applyToEntity(LivingEntity entity) {
        bloodline.applyToEntity(entity, this);
    }

    public void removeFromEntity(LivingEntity entity) {
        bloodline.removeFromEntity(entity, this);
    }

    //──Serialization────────────────────────────────────────────────────────

    public void write(ValueOutput output, RegistryAccess access) {
        NbtHelpers.writeIdentifier(output, "id", bloodlineId);
        bloodline.serializerHandler().write(data, output.child("data"), access);
    }

    //loads a saved instance, returns null if the bloodline no longer exists
    public static @Nullable BloodlineInstance<?> read(ValueInput input, RegistryAccess access) {
        Identifier bloodlineId = NbtHelpers.readIdentifier(input, "id");
        if (bloodlineId == null) return null;
        Bloodline<?> bloodline = CoreRegistries.safeAccess(CoreRegistries.BLOODLINE_REGISTRY, bloodlineId, access);
        if (bloodline == null) return null;
        return readTyped(input, bloodlineId, bloodline, access);
    }

    private static <D extends BloodlineData> BloodlineInstance<D> readTyped(ValueInput input, Identifier bloodlineId, Bloodline<D> bloodline, RegistryAccess access) {
        Optional<ValueInput> dataInput = input.child("data");
        D data = dataInput.map(child -> bloodline.serializerHandler().read(child, access)).orElse(null);
        if (data == null) data = bloodline.newData(access);
        return new BloodlineInstance<>(bloodlineId, bloodline, data);
    }

    //──Sync────────────────────────────────────────────────────────

    //the data is length prefixed so a bloodline the client does not know can be skipped
    public void encode(ByteBuf buf, RegistryAccess access) {
        ByteBufHelpers.encodeIdentifier(bloodlineId, buf);
        ByteBuf dataBuf = Unpooled.buffer();
        bloodline.syncHandler().encode(data, dataBuf, access);
        buf.writeInt(dataBuf.readableBytes());
        buf.writeBytes(dataBuf);
    }

    //decodes an instance, returns null if the bloodline does not exist on this side
    public static @Nullable BloodlineInstance<?> decode(ByteBuf buf, RegistryAccess access) {
        Identifier bloodlineId = ByteBufHelpers.decodeIdentifier(buf);
        int length = buf.readInt();
        ByteBuf dataBuf = buf.readSlice(length);
        Bloodline<?> bloodline = CoreRegistries.safeAccess(CoreRegistries.BLOODLINE_REGISTRY, bloodlineId, access);
        if (bloodline == null) return null;
        return decodeTyped(dataBuf, bloodlineId, bloodline, access);
    }

    private static <D extends BloodlineData> BloodlineInstance<D> decodeTyped(ByteBuf buf, Identifier bloodlineId, Bloodline<D> bloodline, RegistryAccess access) {
        D data = bloodline.newData(access);
        bloodline.syncHandler().decode(data, buf, access);
        return new BloodlineInstance<>(bloodlineId, bloodline, data);
    }
}
