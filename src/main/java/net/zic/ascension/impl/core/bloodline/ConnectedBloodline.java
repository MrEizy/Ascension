package net.zic.ascension.impl.core.bloodline;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.progression.ProgressActionHolder;
import net.zic.ascension.api.ascension.core.requirement.RequirementHolder;
import net.zic.ascension.api.ascension.datapack.bloodline.BloodlineType;
import net.zic.ascension.api.tooltip.AscensionItemTooltipDefinition;
import net.zic.ascension.impl.core.connection.Connected;
import net.zic.ascension.impl.core.connection.ConnectionRequirements;
import net.zic.ascension.impl.datapack.bloodline.AscensionBloodlineTypes;

import java.util.List;
import java.util.Optional;

/**
 * A simple bloodline that can only be taken while its physique and/or technique connections are met.
 * It has no bloodline connection, a bloodline cannot require a bloodline.
 * Assumes SimpleBloodline is not final and has the public constructor SimpleBloodlineType uses.
 */
public class ConnectedBloodline extends SimpleBloodline implements Connected {
    private final ConnectionRequirements connections;

    public ConnectedBloodline(
            Component name,
            Component description,
            List<Identifier> paths,
            ProgressActionHolder purityHandler,
            Optional<AscensionItemTooltipDefinition> itemTooltip,
            RequirementHolder requirements,
            ConnectionRequirements connections
    ) {
        super(name, description, paths, purityHandler, itemTooltip, requirements);
        this.connections = connections == null ? ConnectionRequirements.EMPTY : connections;
    }

    @Override
    public ConnectionRequirements connections() {
        return connections;
    }

    @Override
    public BloodlineType getType() {
        return AscensionBloodlineTypes.CONNECTED_BLOODLINE_TYPE.get();
    }
}