package net.zic.ascension.mob_cultivation.oliver_rewrite;

import net.zic.ascension.configuration.mob_traits.MobTraitDefinition;
import net.zic.ascension.mob_cultivation.generation.MobCultivationEliteTier;

import java.util.List;

/**
 * Holds the resolved configuration details of a mob.
 * this is then applied during the load.
 *
 * for applyToOrigin source that should be done by listening to OriginSourceEvent.OriginSourceFinishedLoadingEvent
 * that way cached paths and skills are still present
 */
public class MobConfigurationInstance {

    private MobCultivationEliteTier tier;
    private List<MobTraitDefinition> definitions;
}
