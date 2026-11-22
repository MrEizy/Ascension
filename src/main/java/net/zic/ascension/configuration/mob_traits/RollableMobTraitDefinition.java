package net.zic.ascension.configuration.mob_traits;

import net.minecraft.util.RandomSource;

public interface RollableMobTraitDefinition {
    MobTraitRoll roll(RandomSource random);
}
