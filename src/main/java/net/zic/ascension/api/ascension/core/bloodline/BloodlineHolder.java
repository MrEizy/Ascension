package net.zic.ascension.api.ascension.core.bloodline;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.CoreDataSources;
import net.zic.ascension.api.rpg_engine.source.data_source.DataSourceInstance;
import net.zic.zenithlib.network.ByteBufHelpers;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BloodlineHolder implements DataSourceInstance<BloodlineDataSource> {
    private final Map<Identifier, BloodlineInstance<?>> bloodlines = new HashMap<>();

    //sync state, only the dirty and removed bloodlines are sent in a partial patch
    private final Set<Identifier> dirtyBloodlines = new HashSet<>();
    private final Set<Identifier> removedBloodlines = new HashSet<>();

    public boolean addBloodline(BloodlineInstance<?> instance) {
        if (instance == null || hasBloodline(instance.getBloodlineId())) {
            return false;
        }
        Identifier bloodline = instance.getBloodlineId();
        bloodlines.put(bloodline, instance);
        removedBloodlines.remove(bloodline);
        dirtyBloodlines.add(bloodline);
        return true;
    }

    public boolean removeBloodline(Identifier bloodline) {
        if (!hasBloodline(bloodline)) {
            return false;
        }
        bloodlines.remove(bloodline);
        dirtyBloodlines.remove(bloodline);
        removedBloodlines.add(bloodline);
        return true;
    }

    public boolean hasBloodline(Identifier bloodline) {
        return bloodlines.containsKey(bloodline);
    }

    public @Nullable BloodlineInstance<?> getBloodline(Identifier bloodline) {
        return bloodlines.get(bloodline);
    }

    public Collection<Identifier> getBloodlines() {
        return bloodlines.keySet();
    }

    public Collection<BloodlineInstance<?>> getBloodlineInstances() {
        return bloodlines.values();
    }

    //should be used if you changed a bloodlines BloodlineData
    public void markBloodlineDirty(Identifier bloodline) {
        if (hasBloodline(bloodline)) {
            dirtyBloodlines.add(bloodline);
        }
    }

    //──Raw Manipulation────────────────────────────────────────────────────────
    public Map<Identifier, BloodlineInstance<?>> getBloodlineSnapshot() {
        return Map.copyOf(bloodlines);
    }

    //restores bloodlines without running any bloodline logic
    public void restore(Map<Identifier, BloodlineInstance<?>> rawBloodlines) {
        bloodlines.clear();
        bloodlines.putAll(rawBloodlines);
        dirtyBloodlines.addAll(rawBloodlines.keySet());
    }

    public void clearContainer() {
        bloodlines.clear();
        dirtyBloodlines.clear();
        removedBloodlines.clear();
    }

    //──Sync────────────────────────────────────────────────────────

    void encodeFull(ByteBuf buf, RegistryAccess access) {
        buf.writeInt(bloodlines.size());
        for (BloodlineInstance<?> instance : bloodlines.values()) {
            instance.encode(buf, access);
        }
        clearSyncState();
    }

    void encodePartial(ByteBuf buf, RegistryAccess access) {
        buf.writeInt(dirtyBloodlines.size());
        for (Identifier bloodline : dirtyBloodlines) {
            bloodlines.get(bloodline).encode(buf, access);
        }
        ByteBufHelpers.encodeCollection(removedBloodlines, buf, ByteBufHelpers::encodeIdentifier);
        clearSyncState();
    }

    void decodeFull(ByteBuf buf, RegistryAccess access) {
        bloodlines.clear();
        decodeInstances(buf, access);
        clearSyncState();
    }

    void decodePartial(ByteBuf buf, RegistryAccess access) {
        decodeInstances(buf, access);
        ByteBufHelpers.decodeArray(buf, ByteBufHelpers::decodeIdentifier).forEach(bloodlines::remove);
        clearSyncState();
    }

    private void decodeInstances(ByteBuf buf, RegistryAccess access) {
        int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            BloodlineInstance<?> instance = BloodlineInstance.decode(buf, access);
            if (instance != null) bloodlines.put(instance.getBloodlineId(), instance);
        }
    }

    void clearSyncState() {
        dirtyBloodlines.clear();
        removedBloodlines.clear();
    }

    @Override
    public BloodlineDataSource getDataSource() {
        return CoreDataSources.BLOODLINE_DATA_SOURCE.get();
    }
}
