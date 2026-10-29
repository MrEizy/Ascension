package net.zic.ascension.impl.core.innerworld;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Text report behind /ascension innerworld debug <player>: says what the plot, the local region, the mob spawner and the
 * structure scanner each think, and (most usefully) why they are idle. Safe to leave in; remove when everything works.
 */
public final class InnerWorldDebug {
    private InnerWorldDebug() {
    }

    public static List<String> report(ServerPlayer player) {
        List<String> lines = new ArrayList<>();
        InnerWorldPlots.Plot plot = InnerWorldPlots.get(player.getUUID());
        if (plot == null) {
            lines.add("[InnerWorld] " + player.getName().getString() + " has no inner world plot.");
            return lines;
        }
        int target = InnerWorld.radiusChunksForTier(plot.tier);
        int life = InnerWorldLife.lifeRealm(player);
        lines.add("[Plot] #" + plot.index + ", tier " + plot.tier + ", built radius " + plot.generatedRadius + "/" + target
                + " chunks, generator v" + plot.generatorVersion + ", seeded: " + (plot.seed != null));
        lines.add("[Life Dao] life major realm " + life + " -> " + (life >= InnerWorldLife.REQUIRED_MAJOR_REALM ? "HAS the Life Dao" : "no Life Dao (needs major realm 1+)"));

        if (plot.generatorVersion < 1 || plot.seed == null) {
            lines.add("[!] Legacy or unseeded plot: mobs, structures, herbs and ambient qi only work on a seeded plot. Use a fresh plot.");
            return lines;
        }
        ServerLevel level = player.level();
        if (!level.dimension().equals(InnerWorld.DIMENSION)) {
            lines.add("[!] You are not inside the inner world: the spawner and structure scan only run for players in it.");
        } else {
            BlockPos center = InnerWorld.plotCenter(plot.index);
            SeededWorld world = InnerWorldGrowth.seededFor(level.getServer(), plot);
            InnerWorldProfile.Variant variant = world.variantAt(player.blockPosition().getX() - center.getX(), player.blockPosition().getZ() - center.getZ());
            int eligible = 0;
            for (InnerWorldProfile.StructureEntry entry : variant.structures()) {
                if (!entry.requiresLife() || life >= InnerWorldLife.REQUIRED_MAJOR_REALM) {
                    eligible++;
                }
            }
            lines.add("[Region here] biome " + variant.biome().map(Object::toString).orElse("none") + ", top block " + variant.palette().top());
            lines.add("[Region mobs] " + variant.mobs().spawns().size() + " spawn entries, realm " + variant.mobs().minRealm() + ".."
                    + variant.mobs().maxRealm() + " without Life Dao" + (variant.mobs().spawns().isEmpty()
                    ? "  <-- EMPTY: this profile JSON has no mobs block (old copy?)" : ""));
            lines.add("[Region structures] " + variant.structures().size() + " entries, " + eligible + " allowed for you right now");
            lines.add("[Mobs] " + InnerWorldMobs.countNear(level, player) + " of its mobs near you. Spawner: " + InnerWorldMobs.lastStatus);
        }
        lines.add("[Structures] " + plot.structureSites.size() + " grid cells decided, " + InnerWorldStructures.placedCount + " placed this session. Scanner: "
                + InnerWorldStructures.lastStatus);
        return lines;
    }
}