package net.zic.ascension.impl.core.technique;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.progression.ProgressActionList;
import net.zic.ascension.api.ascension.core.requirement.RequirementHolder;
import net.zic.ascension.api.ascension.datapack.technique.TechniqueType;
import net.zic.ascension.api.tooltip.AscensionItemTooltipDefinition;
import net.zic.ascension.impl.core.connection.Connected;
import net.zic.ascension.impl.core.connection.ConnectionRequirements;
import net.zic.ascension.impl.datapack.technique.AscensionTechniqueTypes;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A simple technique that can only be learned while its physique and/or bloodline connections are met.
 */
public class ConnectedTechnique extends SimpleTechnique implements Connected {
    private final ConnectionRequirements connections;

    public ConnectedTechnique(
            Component name,
            Component description,
            ProgressActionList onAddedActions,
            List<TechniqueRealmStatBonus> realmStatGrowth,
            Map<Identifier, TechniqueRealmStatRate> statsPerRealm,
            List<Identifier> cultivationPaths,
            Optional<Integer> maxRealm,
            Optional<Integer> maxMinorRealm,
            Optional<AscensionItemTooltipDefinition> itemTooltip,
            RequirementHolder requirements,
            ConnectionRequirements connections
    ) {
        super(name, description, onAddedActions, realmStatGrowth, statsPerRealm, cultivationPaths, maxRealm, maxMinorRealm, itemTooltip, requirements);
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