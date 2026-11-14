package net.zic.ascension.impl.core.innerworld;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.resource.ResourceOperation;
import net.zic.ascension.api.ascension.core.resource.ResourceType;
import net.zic.ascension.common.fluids.AscFluidTypes;
import net.zic.ascension.impl.resource.AscensionResourceTypes;

/**
 * Spirit spring effects. A spring is any liquified spiritual qi fluid, so these work in the inner world's generated
 * springs and in any pool a player builds with your bucket.
 *  - Restore: standing in the fluid restores qi, 2% of the player's maximum per second (4% with the head under).
 *  - Cultivation: you must be INSIDE the fluid for the bonus. cultivationMultiplier() is applied by the Cultivate
 *    skill action (one line in SkillActions.Cultivate.apply), so it speeds up exactly what meditation skills like
 *    stillfrost breathing already do, and only while you are in the spring.
 * Both numbers are plain constants below to tune.
 */
@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class InnerWorldSprings {
    private InnerWorldSprings() {
    }

    private static final double RESTORE_FRACTION_PER_SECOND = 0.02;
    private static final double SUBMERGED_BONUS = 2.0;
    public static final double CULTIVATION_MULTIPLIER = 2.0;

    public static boolean isInSpring(LivingEntity entity) {
        return entity.isInFluidType(AscFluidTypes.LIQUIFIED_SPIRITUAL_QI_TYPE.get());
    }

    public static double cultivationMultiplier(LivingEntity entity) {
        return isInSpring(entity) ? CULTIVATION_MULTIPLIER : 1.0;
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Pre event) {
        if (event.getServer().getTickCount() % 20 != 0) {
            return;
        }
        ResourceType qi = AscensionResourceTypes.QI.get();
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            if (!player.isAlive() || !isInSpring(player)) {
                continue;
            }
            double amount = qi.getMaximum(player) * RESTORE_FRACTION_PER_SECOND
                    * (player.isEyeInFluidType(AscFluidTypes.LIQUIFIED_SPIRITUAL_QI_TYPE.get()) ? SUBMERGED_BONUS : 1.0);
            if (amount > 0.0) {
                ResourceOperation.RESTORE.apply(qi, player, amount, false);
            }
        }
    }
}