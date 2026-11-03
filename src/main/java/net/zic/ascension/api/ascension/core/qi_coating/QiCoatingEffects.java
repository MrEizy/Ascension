package net.zic.ascension.api.ascension.core.qi_coating;

import net.minecraft.resources.Identifier;
import net.zic.ascension.AscensionCraft;

/**
 * The effects a Qi Coating can add through its {@code effects} list: behaviour that is coded in Java rather than described by
 * data (QiCoatingLayer and QiCoatingService run them). Unknown ids are ignored.
 */
public final class QiCoatingEffects {
    /**
     * Lightning Step: the player's own model is drawn see-through (and so is the arm in first person), crackling footprints are
     * left where they walk or land, and holding right click with a spear sends them flashing forward (QiCoatingService), leaving
     * a lightning trail.
     */
    public static final Identifier LIGHTNING_STEP = AscensionCraft.prefix("lightning_step");
    /**
     * Tornado Fist: a box of wind rings round each forearm, their streaks running round in time with the {@code ascension:wind}
     * skin look.
     */
    public static final Identifier WIND_RINGS = AscensionCraft.prefix("wind_rings");

    private QiCoatingEffects() {
    }
}
