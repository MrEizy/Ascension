package net.zic.ascension.impl.core.innerworld;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.chunks.atmospheric_qi.ChunkHelper;
import net.zic.ascension.chunks.atmospheric_qi.ChunkQiHandler;
import net.zic.zenithlib.value_containers.typed.Modifier;

/**
 * Ambient qi for inner world chunks, applied on every chunk load (your qi modifiers aren't persisted, so
 * ChunkEventHandler re-adds biome/dimension ones on load too; this adds one more modifier layer the same way,
 * using the public ChunkHelper / ChunkQiHandler / ChunkPathAffinityProvider API).
 *
 * Density = the variant's own qi values x the world's tier scaling x (1.5 if this is the strongest path's region)
 * x (1 + 2 x share of the chunk covered by spirit springs). The affinity added is the region's own path, so a fire
 * region is fire-aspected air. Tier changes only reach already-loaded chunks on their next load.
 */
@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class InnerWorldQi {
    private InnerWorldQi() {
    }

    /**
     * CRASH SWITCH. A village cat crossing a chunk crashed in EntityPathBonusHolder.sync (null container in ValueContainer.encode),
     * reached through ChunkEventHandler.onEnterChunk, which re-syncs every path bonus a chunk provides. Adding a path AFFINITY to
     * inner world chunks is one of the two things that newly puts path bonuses on those chunks, so it is off until the cause is
     * confirmed. Capacity and regen (the ambient qi itself) are unaffected. Flip to true to test.
     */
    private static final boolean ADD_PATH_AFFINITY = false;

    private static final Identifier MODIFIER = AscensionCraft.prefix("inner_world_ambient");

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        LevelChunk chunk = event.getChunk();
        if (!(chunk.getLevel() instanceof ServerLevel level) || !level.dimension().equals(InnerWorld.DIMENSION)) {
            return;
        }
        int chunkX = chunk.getPos().x();
        int chunkZ = chunk.getPos().z();
        InnerWorldPlots.Plot plot = plotForChunk(chunkX, chunkZ);
        if (plot == null || plot.generatorVersion < 1 || plot.seed == null) {
            return;
        }

        BlockPos center = InnerWorld.plotCenter(plot.index);
        SeededWorld.QiInfo info = InnerWorldGrowth.seededFor(level.getServer(), plot)
                .qiAt(chunkX, chunkZ, center.getX(), center.getZ());

        double tierScale = 1.0 + plot.tier * 0.5;
        double strongest = info.strongest() ? 1.5 : 1.0;
        double density = strongest * (1.0 + 2.0 * info.springFraction());
        long capacity = Math.round(info.variant().qi().capacity() * tierScale * density);
        long regen = Math.round(info.variant().qi().regen() * tierScale * density);

        ChunkQiHandler qi = ChunkHelper.getQiHandler(chunk);
        qi.addCapacityFlatModifier(Modifier.base(MODIFIER, capacity));
        qi.addRegenFlatModifier(Modifier.base(MODIFIER, regen));
        if (ADD_PATH_AFFINITY && info.path() != null) {
            double affinity = info.variant().qi().affinity() * tierScale * strongest;
            ChunkHelper.getAffinityProvider(chunk).addFlatModifier(info.path(), Modifier.base(MODIFIER, affinity));
        }
    }

    static InnerWorldPlots.Plot plotForChunk(int chunkX, int chunkZ) {
        double step = InnerWorld.PLOT_SPACING_BLOCKS / 16.0;
        int gx = (int) Math.round(chunkX / step);
        int gz = (int) Math.round(chunkZ / step);
        for (InnerWorldPlots.Plot plot : InnerWorldPlots.entries().values()) {
            int[] grid = InnerWorld.plotGrid(plot.index);
            if (grid[0] == gx && grid[1] == gz) {
                return plot;
            }
        }
        return null;
    }
}