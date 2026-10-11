package net.zic.ascension.impl.core.path;

import net.minecraft.resources.Identifier;
import net.zic.ascension.api.ascension.core.tribulation.Tribulation;
import net.zic.ascension.api.ascension.core.tribulation.TribulationData;

public record CompletedTribulation(Identifier id, Tribulation tribulation, TribulationData data) {

}
