package net.zic.ascension.api.ascension.core.path.interactions;

import net.minecraft.resources.Identifier;

/**
 * Maps a relationship pathA -> pathB
 * If some event happens the uses pathB and pathA is also present pathB is influenced by the interaction from pathA
 *
 * example
 * path A = water pathB = ice type = Related value = 0.5
 * if i use an ice cultivation technique and i have water affinity the ice affinity gets 50% of the water affinity
 *
 * @param pathA the source of the interaction
 * @param pathB the target of the interaction
 */
public record PathInteraction(Identifier pathA,Identifier pathB,PathInteractionType type,double value) {
}
