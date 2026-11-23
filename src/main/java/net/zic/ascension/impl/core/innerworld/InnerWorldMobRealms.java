package net.zic.ascension.impl.core.innerworld;

import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.configuration.mobs.MobConfiguration;
import net.zic.ascension.mob_cultivation.oliver_rewrite.MobConfigurationHolder;
import net.zic.ascension.mob_cultivation.oliver_rewrite.MobSpawnHelper;

import java.util.Map;

public final class InnerWorldMobRealms {
    private static final Identifier RARITY_POOL = Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "rarity");

    private InnerWorldMobRealms() {
    }

    public static void assign(Mob mob, int minRealm, int maxRealm, RandomSource random) {
        if (!MobSpawnHelper.hasConfig(mob)) return;

        MobConfiguration configuration = MobSpawnHelper.getConfig(mob);
        int realm = minRealm + random.nextInt(Math.max(1, maxRealm - minRealm + 1));
        String rarity = realm <= 0 ? "normal" : realm == 1 ? "elite" : "ancient";
        MobSpawnHelper.getConfigHolder(mob).setMobConfigurationInstance(
                MobSpawnHelper.generateInstance(configuration, mob, random, Map.of(RARITY_POOL, rarity)));
    }

    public static void applyNow(Mob mob) {
        if (!MobSpawnHelper.hasConfigHolder(mob)) return;
        MobConfigurationHolder holder = MobSpawnHelper.getConfigHolder(mob);
        holder.applyConfigurationToEntity();
        if (holder.getSource() != null) holder.applyConfigurationToSource();
    }
}
