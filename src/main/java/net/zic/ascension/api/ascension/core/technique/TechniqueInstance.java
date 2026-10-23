package net.zic.ascension.api.ascension.core.technique;

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

import java.util.Optional;

/**
 * A technique held by an origin source.
 * Wraps a Technique and its data, and defers calls to the technique so it always receives its own typed data.
 */
public final class TechniqueInstance<D extends TechniqueData> {
    private final Identifier techniqueId;
    private final Technique<D> technique;
    private final D data;

    TechniqueInstance(Identifier techniqueId, Technique<D> technique, D data) {
        this.techniqueId = techniqueId;
        this.technique = technique;
        this.data = data;
    }

    //creates an instance with fresh data
    public static <D extends TechniqueData> TechniqueInstance<D> create(Identifier techniqueId, Technique<D> technique, RegistryAccess access) {
        return new TechniqueInstance<>(techniqueId, technique, technique.newData(access));
    }

    //creates a fresh instance by looking up the technique, null if it does not exist
    public static @Nullable TechniqueInstance<?> create(Identifier techniqueId, RegistryAccess access) {
        Technique<?> technique = CoreRegistries.safeAccess(CoreRegistries.TECHNIQUE_REGISTRY, techniqueId, access);
        return technique == null ? null : create(techniqueId, technique, access);
    }

    //──Getters────────────────────────────────────────────────────────
    public Identifier getTechniqueId() {return techniqueId;}
    public Technique<D> getTechnique() {return technique;}
    public D getData() {return data;}

    public Component getName() {return technique.getName(data);}
    public Component getDescription() {return technique.getDescription(data);}

    /**
     * narrows this instance to a concrete technique class, e.g. {@code instance.as(SimpleTechnique.class)}.
     * the data is always created by the technique, so it always matches the technique's data type
     * @return this instance typed to the technique, or null if the technique is not of that class
     */
    @SuppressWarnings("unchecked")
    public <E extends TechniqueData, T extends Technique<E>> @Nullable TechniqueInstance<E> as(Class<T> techniqueClass) {
        return techniqueClass.isInstance(technique) ? (TechniqueInstance<E>) this : null;
    }

    //──Dispatch────────────────────────────────────────────────────────

    public void onAdded(OriginSource source) {
        technique.onAdded(source, this);
    }

    public void onRemoved(OriginSource source) {
        technique.onRemoved(source, this);
    }

    public void applyToEntity(LivingEntity entity) {
        technique.applyToEntity(entity, this);
    }

    public void removeFromEntity(LivingEntity entity) {
        technique.removeFromEntity(entity, this);
    }

    //──Serialization────────────────────────────────────────────────────────

    public void write(ValueOutput output, RegistryAccess access) {
        NbtHelpers.writeIdentifier(output, "id", techniqueId);
        technique.serializerHandler().write(data, output.child("data"), access);
    }

    //loads a saved instance, returns null if the technique no longer exists
    public static @Nullable TechniqueInstance<?> read(ValueInput input, RegistryAccess access) {
        Identifier techniqueId = NbtHelpers.readIdentifier(input, "id");
        if (techniqueId == null) return null;
        Technique<?> technique = CoreRegistries.safeAccess(CoreRegistries.TECHNIQUE_REGISTRY, techniqueId, access);
        if (technique == null) return null;
        return readTyped(input, techniqueId, technique, access);
    }

    private static <D extends TechniqueData> TechniqueInstance<D> readTyped(ValueInput input, Identifier techniqueId, Technique<D> technique, RegistryAccess access) {
        Optional<ValueInput> dataInput = input.child("data");
        D data = dataInput.map(child -> technique.serializerHandler().read(child, access)).orElse(null);
        if (data == null) data = technique.newData(access);
        return new TechniqueInstance<>(techniqueId, technique, data);
    }

    //──Sync────────────────────────────────────────────────────────

    //the data is length prefixed so a technique the client does not know can be skipped
    public void encode(ByteBuf buf, RegistryAccess access) {
        ByteBufHelpers.encodeIdentifier(techniqueId, buf);
        ByteBuf dataBuf = Unpooled.buffer();
        technique.syncHandler().encode(data, dataBuf, access);
        buf.writeInt(dataBuf.readableBytes());
        buf.writeBytes(dataBuf);
    }

    //decodes an instance, returns null if the technique does not exist on this side
    public static @Nullable TechniqueInstance<?> decode(ByteBuf buf, RegistryAccess access) {
        Identifier techniqueId = ByteBufHelpers.decodeIdentifier(buf);
        int length = buf.readInt();
        ByteBuf dataBuf = buf.readSlice(length);
        Technique<?> technique = CoreRegistries.safeAccess(CoreRegistries.TECHNIQUE_REGISTRY, techniqueId, access);
        if (technique == null) return null;
        return decodeTyped(dataBuf, techniqueId, technique, access);
    }

    private static <D extends TechniqueData> TechniqueInstance<D> decodeTyped(ByteBuf buf, Identifier techniqueId, Technique<D> technique, RegistryAccess access) {
        D data = technique.newData(access);
        technique.syncHandler().decode(data, buf, access);
        return new TechniqueInstance<>(techniqueId, technique, data);
    }
}
