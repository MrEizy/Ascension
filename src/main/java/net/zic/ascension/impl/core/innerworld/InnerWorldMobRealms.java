package net.zic.ascension.impl.core.innerworld;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.zic.ascension.configuration.mob_traits.MobTraitReference;
import net.zic.ascension.configuration.mobs.MobConfiguration;
import net.zic.ascension.configuration.mobs.PotentialTrait;
import net.zic.ascension.mob_cultivation.oliver_rewrite.MobCultivationEliteTier;
import net.zic.ascension.mob_cultivation.oliver_rewrite.MobConfigurationHolder;
import net.zic.ascension.mob_cultivation.oliver_rewrite.MobConfigurationInstance;
import net.zic.ascension.mob_cultivation.oliver_rewrite.MobSpawnHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Hands the inner world's allowed realm range to your mob cultivation system, written against MobSpawnHelper /
 * MobConfigurationInstance as they are today.
 *
 * Your rewrite has no numeric "soul realm" on a mob yet: what a mob is, is its elite TIER plus the traits rolled for that
 * tier (and MobCultivationEliteTier is marked deprecated, to become trait tiers). So a realm index maps onto the tier
 * ladder: realm 0 = NORMAL, 1 = ELITE, 2 and above = ANCIENT. assign() picks a realm in [min, max], turns it into that tier,
 * and rolls the tier's traits exactly the way MobSpawnHelper.generateInstance does. When trait tiers replace the enum,
 * only TIERS and assign() below need to change.
 *
 * ORDER MATTERS: FinalizeSpawnEvent (MobEventHandler.onEntitySpawn) rolls its own random tier, so this must run AFTER
 * finalize and BEFORE the mob joins the level (EntityJoinLevelEvent applies the instance), which is how InnerWorldMobs does it.
 * For mobs that are already in the world (villagers placed by a structure) call applyNow() afterwards.
 */
public final class InnerWorldMobRealms {
    private InnerWorldMobRealms() {
    }

    private static final MobCultivationEliteTier[] TIERS = {
            MobCultivationEliteTier.NORMAL, MobCultivationEliteTier.ELITE, MobCultivationEliteTier.ANCIENT
    };

    public static void assign(Mob mob, int minRealm, int maxRealm, RandomSource random) {
        if (!MobSpawnHelper.hasConfig(mob)) {
            return; // this entity type has no mob cultivation configuration
        }
        MobConfiguration configuration = MobSpawnHelper.getConfig(mob);
        int realm = minRealm + random.nextInt(Math.max(1, maxRealm - minRealm + 1));
        MobCultivationEliteTier tier = TIERS[Math.max(0, Math.min(realm, TIERS.length - 1))];

        List<MobTraitReference> traits = new ArrayList<>();
        for (PotentialTrait potential : configuration.getPotentialTraits(tier)) {
            if (potential.test(mob) && random.nextDouble() <= potential.chance()) {
                traits.add(potential.trait());
            }
        }
        MobSpawnHelper.getConfigHolder(mob).setMobConfigurationInstance(new MobConfigurationInstance(tier, traits));
    }

    /** For a mob that has already joined the level: apply the freshly assigned instance to the entity and its origin source. */
    public static void applyNow(Mob mob) {
        if (!MobSpawnHelper.hasConfigHolder(mob)) {
            return;
        }
        MobConfigurationHolder holder = MobSpawnHelper.getConfigHolder(mob);
        holder.applyConfigurationToEntity();
        if (holder.getSource() != null) {
            holder.applyConfigurationToSource();
        }
    }
}