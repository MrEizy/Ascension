package net.zic.ascension.impl.core.tribulation;

import net.minecraft.resources.Identifier;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.path.realm.Realm;
import net.zic.ascension.api.ascension.datapack.tribulation.TribulationType;
import net.zic.ascension.impl.datapack.tribulation.AscensionTribulationTypes;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

/**
 * lightning data that also holds the golden core formed by the tribulation.
 * the core, path and acquisition realm always start as the tribulation's defaults, whatever triggers the tribulation
 * (e.g. a golden core forming skill) can override them before it starts.
 * once completed the tribulation owns the core through {@link #owner()}
 */
public class GoldenCoreTribulationData extends LightningTribulationData {
    //only null while loading old data, fixed by GoldenCoreTribulation#validateLoadedData
    private @Nullable Identifier goldenCore;
    private @Nullable UUID goldenCoreId;
    private @Nullable Identifier path;
    private @Nullable Realm acquisitionRealm;

    public GoldenCoreTribulationData(int survived, int ticks, UUID id,
                                     Optional<Identifier> goldenCore, Optional<UUID> goldenCoreId,
                                     Optional<Identifier> path, Optional<Realm> acquisitionRealm) {
        super(survived, ticks, id);
        this.goldenCore = goldenCore.orElse(null);
        this.goldenCoreId = goldenCoreId.orElse(null);
        this.path = path.orElse(null);
        this.acquisitionRealm = acquisitionRealm.orElse(null);
    }

    //fresh data forming the given core
    public static GoldenCoreTribulationData of(Identifier goldenCore, Identifier path, Realm acquisitionRealm) {
        return new GoldenCoreTribulationData(0, 0, UUID.randomUUID(), Optional.of(goldenCore), Optional.of(UUID.randomUUID()),
                Optional.of(path), Optional.of(acquisitionRealm));
    }

    /**
     * overrides the golden core this tribulation forms, keeps the core's instance id
     * @param goldenCore the golden core
     * @param path the path the core tracks
     * @param acquisitionRealm the realm the core is formed at
     */
    public void setGoldenCore(Identifier goldenCore, Identifier path, Realm acquisitionRealm) {
        this.goldenCore = goldenCore;
        this.path = path;
        this.acquisitionRealm = acquisitionRealm;
        if (goldenCoreId == null) goldenCoreId = UUID.randomUUID();
    }

    //if every part of the core is set, always true once validated
    public boolean hasGoldenCore() {
        return goldenCore != null && goldenCoreId != null && path != null && acquisitionRealm != null;
    }

    //the owner of the formed golden core, unique to this tribulation
    public Identifier owner() {
        return Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "tribulation/" + getUUID());
    }

    public @Nullable Identifier getGoldenCore() {return goldenCore;}
    public @Nullable UUID getGoldenCoreId() {return goldenCoreId;}
    public @Nullable Identifier getPath() {return path;}
    public @Nullable Realm getAcquisitionRealm() {return acquisitionRealm;}

    @Override
    public TribulationType<?> getType() {
        return AscensionTribulationTypes.GOLDEN_CORE_TRIBULATION.get();
    }
}
