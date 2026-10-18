package net.zic.ascension.api.ascension.core.skill.castable;

/**
 * state of a single cast, created when the cast starts and discarded when it ends.
 * held by the {@link CastingSkillInstance}, synced through the skill's cast sync handler.
 * call {@link CastingSkillInstance#markCastDataDirty()} after changing it.
 */
public interface CastData {
}
