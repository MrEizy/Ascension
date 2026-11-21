package net.zic.ascension.impl.core.bloodline;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.progression.ProgressActionHolder;
import net.zic.ascension.api.ascension.core.requirement.RequirementHolder;
import net.zic.ascension.api.ascension.datapack.bloodline.BloodlineType;
import net.zic.ascension.api.tooltip.AscensionItemTooltipDefinition;
import net.zic.ascension.impl.core.evolution.EvolutionRequirements;
import net.zic.ascension.impl.core.evolution.Evolving;
import net.zic.ascension.impl.datapack.bloodline.AscensionBloodlineTypes;

import java.util.List;
import java.util.Optional;

/**
 * A simple bloodline that can evolve into another one once its evolution requirements are met.
 * Assumes SimpleBloodline is not final and has the public constructor SimpleBloodlineType uses.
 */
public class EvolutionBloodline extends SimpleBloodline implements Evolving {
    private final EvolutionRequirements evolution;

    public EvolutionBloodline(
            Component name,
            Component description,
            List<Identifier> paths,
            ProgressActionHolder purityHandler,
            Optional<AscensionItemTooltipDefinition> itemTooltip,
            RequirementHolder requirements,
            EvolutionRequirements evolution
    ) {
        super(name, description, paths, purityHandler, itemTooltip, requirements);
        this.evolution = evolution;
    }

    @Override
    public EvolutionRequirements evolution() {
        return evolution;
    }

    @Override
    public BloodlineType getType() {
        return AscensionBloodlineTypes.EVOLUTION_BLOODLINE_TYPE.get();
    }
}