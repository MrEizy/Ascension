package net.zic.ascension.impl.core.technique;

import net.zic.ascension.api.ascension.datapack.technique.TechniqueType;
import net.zic.ascension.impl.core.connection.Connected;
import net.zic.ascension.impl.core.connection.ConnectionRequirements;
import net.zic.ascension.impl.datapack.technique.AscensionTechniqueTypes;

/**
 * A simple technique that can only be learned while its physique and/or bloodline and/or path connections are met.
 * Takes everything else from the SimpleTechnique it is built on.
 */
public class ConnectedTechnique extends SimpleTechnique implements Connected {
    private final ConnectionRequirements connections;

    public ConnectedTechnique(SimpleTechnique base, ConnectionRequirements connections) {
        super(base.getName(null), base.getDescription(null), base.getOnAddedActions(), base.getRealmStatGrowth(),
                base.getStatsPerRealm(), base.getCultivationPaths(), base.getMaxRealm(), base.getMaxMinorRealm(),
                base.itemTooltip(), base.requirements());
        this.connections = connections == null ? ConnectionRequirements.EMPTY : connections;
    }

    @Override
    public ConnectionRequirements connections() {
        return connections;
    }

    @Override
    public TechniqueType getType() {
        return AscensionTechniqueTypes.CONNECTED_TECHNIQUE_TYPE.get();
    }
}