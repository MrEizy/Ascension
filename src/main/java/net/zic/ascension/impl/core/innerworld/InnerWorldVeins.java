package net.zic.ascension.impl.core.innerworld;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.network.InnerWorldVeinsPayload;

import java.util.ArrayList;
import java.util.List;

/**
 * Buried treasure veins. Their positions are a pure function of the seed (SeededWorld.veinCentersNear), so nothing
 * is stored. Two ways to find them:
 *  - hint: every second, a faint particle column rises from the surface above any intact vein within 28 blocks;
 *  - reveal: Divine Sense calls reveal(), which sends the positions inside its radius to the caster so the client
 *    can outline them through walls as the wave reaches them.
 * A vein is "intact" while its centre block is not air, so a mined-out vein stops hinting and stops being revealed.
 */
@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class InnerWorldVeins {
    private InnerWorldVeins() {
    }

    private static final double HINT_RADIUS = 28.0;

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Pre event) {
        if (event.getServer().getTickCount() % 20 != 0) {
            return;
        }
        ServerLevel level = event.getServer().getLevel(InnerWorld.DIMENSION);
        if (level == null || level.players().isEmpty()) {
            return;
        }
        for (ServerPlayer player : level.players()) {
            for (BlockPos vein : intactNear(level, player, player.getX(), player.getZ(), HINT_RADIUS)) {
                int surface = level.getHeight(Heightmap.Types.MOTION_BLOCKING, vein.getX(), vein.getZ());
                level.sendParticles(ParticleTypes.END_ROD, vein.getX() + 0.5, surface + 0.3, vein.getZ() + 0.5,
                        3, 0.4, 0.3, 0.4, 0.02);
            }
        }
    }

    /**
     * Call this from the DivineSense skill action, right after its ClientboundDivineSensePacket is sent:
     *   InnerWorldVeins.reveal(player, center, resolvedRadius);
     * It does nothing outside the inner world.
     */
    public static void reveal(ServerPlayer player, Vec3 center, double radius) {
        ServerLevel level = player.level();
        if (!level.dimension().equals(InnerWorld.DIMENSION)) {
            return;
        }
        List<BlockPos> veins = intactNear(level, player, center.x, center.z, radius);
        if (!veins.isEmpty()) {
            PacketDistributor.sendToPlayer(player, new InnerWorldVeinsPayload(veins));
        }
    }

    private static List<BlockPos> intactNear(ServerLevel level, ServerPlayer player, double x, double z, double radius) {
        InnerWorldPlots.Plot plot = InnerWorldPlots.get(player.getUUID());
        if (plot == null || plot.generatorVersion < 1 || plot.seed == null) {
            return List.of();
        }
        BlockPos center = InnerWorld.plotCenter(plot.index);
        List<BlockPos> result = new ArrayList<>();
        for (BlockPos vein : InnerWorldGrowth.seededFor(level.getServer(), plot)
                .veinCentersNear(center.getX(), center.getZ(), x, z, radius)) {
            if (level.isLoaded(vein) && !level.getBlockState(vein).isAir()) {
                result.add(vein);
            }
        }
        return result;
    }
}