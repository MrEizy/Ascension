package net.zic.ascension.api.core.tribulation;

import net.zic.ascension.api.datapack.tribulation.TribulationType;

import java.util.UUID;

public interface TribulationData {

    //the same entity may have multiple of the same tribulation, so if you want each to apply buffs
    //use a saved ID to salt any Identifiers
    UUID getUUID();
    TribulationType getType();
}
