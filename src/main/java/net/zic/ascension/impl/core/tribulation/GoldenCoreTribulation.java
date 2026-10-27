package net.zic.ascension.impl.core.tribulation;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.path.realm.Realm;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.impl.datapack.tribulation.AscensionTribulationTypes;
import net.zic.ascension.impl.datapack.tribulation.GoldenCoreTribulationType;

/**
 * a lightning tribulation that forms a golden core.
 * the data always holds a core, path and acquisition realm, starting as this tribulation's defaults, which the trigger
 * can override before it starts. A realm triggering this automatically therefore needs its own tribulation with matching defaults.
 * once completed the path holds the data and this tribulation adds/removes the core as the realm is gained/lost,
 * owning it through {@link GoldenCoreTribulationData#owner()}
 */
public class GoldenCoreTribulation extends AbstractLightningTribulation<GoldenCoreTribulationData> {
    private final Identifier defaultGoldenCore;
    private final Identifier defaultPath;
    private final Realm defaultRealm;

    public GoldenCoreTribulation(int strikes, double damage, int tickDelay, Identifier defaultGoldenCore, Identifier defaultPath, Realm defaultRealm) {
        super(strikes, damage, tickDelay);
        this.defaultGoldenCore = defaultGoldenCore;
        this.defaultPath = defaultPath;
        this.defaultRealm = defaultRealm;
    }

    //the core formed when no other core is provided, or the held core no longer exists
    public Identifier defaultGoldenCore() {return defaultGoldenCore;}
    //the path the default core tracks
    public Identifier defaultPath() {return defaultPath;}
    //the realm the default core is formed at
    public Realm defaultRealm() {return defaultRealm;}

    @Override
    public GoldenCoreTribulationType getType() {
        return AscensionTribulationTypes.GOLDEN_CORE_TRIBULATION.get();
    }

    @Override
    public GoldenCoreTribulationData newData() {
        return GoldenCoreTribulationData.of(defaultGoldenCore, defaultPath, defaultRealm);
    }

    @Override
    public GoldenCoreTribulationData validateLoadedData(GoldenCoreTribulationData data, RegistryAccess access) {
        super.validateLoadedData(data, access);
        Identifier path = data.getPath() == null ? defaultPath : data.getPath();
        Realm realm = data.getAcquisitionRealm() == null ? defaultRealm : data.getAcquisitionRealm();
        //the held core may have been removed from the datapack, the core id is kept so the instance stays the same
        Identifier goldenCore = data.getGoldenCore();
        if (goldenCore == null || CoreRegistries.safeAccess(CoreRegistries.GOLDEN_CORE_REGISTRY, goldenCore, access) == null) {
            goldenCore = defaultGoldenCore;
        }
        data.setGoldenCore(goldenCore, path, realm);
        return data;
    }

    @Override
    public void onAdded(OriginSource source, GoldenCoreTribulationData data) {
        if (!data.hasGoldenCore()) return;
        AscensionOriginSourceHelper.addGoldenCore(source, data.getGoldenCoreId(), data.getGoldenCore(), data.getPath(),
                data.getAcquisitionRealm(), data.owner());
    }

    @Override
    public void onRemoved(OriginSource source, GoldenCoreTribulationData data) {
        if (!data.hasGoldenCore()) return;
        AscensionOriginSourceHelper.removeGoldenCore(source, data.getGoldenCoreId(), data.owner());
    }
}
