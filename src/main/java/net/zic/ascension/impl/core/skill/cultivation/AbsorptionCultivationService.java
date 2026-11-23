package net.zic.ascension.impl.core.skill.cultivation;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.path.PathInstance;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.impl.core.evolution.EvolutionService;
import net.zic.ascension.impl.core.skill.cultivation.AbsorptionCultivationSkill.Environment;
import net.zic.ascension.impl.core.skill.cultivation.AbsorptionCultivationSkill.ItemAbsorption;
import net.zic.ascension.util.CultivationUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.UUID;

/**
 * Runs one tick of an absorption_cultivation skill for a player while they hold its cast, and keeps how far each absorption has got.
 * <p>
 * An absorbed item or block does one of two things, never both at once: if it can evolve something the player has (its entry has
 * evolution_type, it is an absorb_catalyst of the player's evolution physique / bloodline and that evolution's requirements are
 * met) it evolves it, otherwise it gives its value as progress on the skill's path (cultivation_type for items, always for
 * blocks). If it can do neither it is not absorbed at all.
 */
public final class AbsorptionCultivationService {
    private static final int SCAN_INTERVAL = 10;
    private static final int OVERLAY_INTERVAL = 5;
    private static final int FAILURE_INTERVAL = 40;
    // The skill ticks every tick of the cast. This many ticks without it and the absorption has stopped.
    private static final int STALE_TICKS = 3;

    private static final Map<UUID, ItemProgress> ITEMS = new HashMap<>();
    private static final Map<UUID, BlockProgress> BLOCKS = new HashMap<>();
    private static final Map<UUID, Long> LAST_TICK = new HashMap<>();

    private AbsorptionCultivationService() {
    }

    private record Entry(Identifier skillId, AbsorptionCultivationSkill action) {
    }

    private static final class ItemProgress {
        final Identifier skillId;
        final Identifier itemId;
        int ticks;

        ItemProgress(Identifier skillId, Identifier itemId) {
            this.skillId = skillId;
            this.itemId = itemId;
        }
    }

    private static final class BlockProgress {
        final Identifier skillId;
        final ServerLevel level;
        final BlockPos pos;
        int ticks;
        int stage = -1;

        BlockProgress(Identifier skillId, ServerLevel level, BlockPos pos) {
            this.skillId = skillId;
            this.level = level;
            this.pos = pos;
        }
    }

    public static void tick(ServerPlayer player, Identifier skillId, AbsorptionCultivationSkill action) {
        if (!player.isAlive() || player.isRemoved() || player.isSpectator()) {
            stop(player);
            return;
        }
        OriginSource source = AscensionOriginSourceHelper.getEntitySource(player);
        if (source == null) {
            return;
        }
        LAST_TICK.put(player.getUUID(), player.level().getGameTime());
        Entry entry = new Entry(skillId, action);
        tickItem(player, source, entry);
        tickEnvironment(player, source, entry);
    }

    /** drops what the player was absorbing, and takes the block breaking animation off */
    public static void stop(ServerPlayer player) {
        ITEMS.remove(player.getUUID());
        LAST_TICK.remove(player.getUUID());
        clearBlock(player);
    }

    // ── held item ──────────────────────────────────────────────────────────────────────

    private record ItemMatch(Identifier itemId, double value, boolean evolution) {
    }

    private static void tickItem(ServerPlayer player, OriginSource source, Entry entry) {
        ItemAbsorption items = entry.action().items().orElse(null);
        ItemStack held = player.getMainHandItem();
        if (items == null || held.isEmpty()) {
            ITEMS.remove(player.getUUID());
            return;
        }
        Component[] failure = new Component[1];
        ItemMatch match = findItemMatch(player, source, entry, items, held, failure);
        if (match == null) {
            ITEMS.remove(player.getUUID());
            if (failure[0] != null && player.tickCount % FAILURE_INTERVAL == 0) {
                player.sendOverlayMessage(failure[0]);
            }
            return;
        }

        ItemProgress progress = ITEMS.get(player.getUUID());
        if (progress == null || !progress.skillId.equals(entry.skillId()) || !progress.itemId.equals(match.itemId())) {
            progress = new ItemProgress(entry.skillId(), match.itemId());
            ITEMS.put(player.getUUID(), progress);
        }
        progress.ticks++;
        if (progress.ticks % OVERLAY_INTERVAL == 0) {
            int percent = Math.min(100, progress.ticks * 100 / items.timeNeeded());
            player.sendOverlayMessage(Component.translatable("ascension.absorption.progress", held.getHoverName(), percent));
        }
        if (progress.ticks < items.timeNeeded()) {
            return;
        }
        ITEMS.remove(player.getUUID());
        if (absorb(player, source, entry.action(), match.itemId(), match.value(), match.evolution())
                && !player.getAbilities().instabuild) {
            held.shrink(1);
        }
    }

    private static ItemMatch findItemMatch(ServerPlayer player, OriginSource source, Entry entry, ItemAbsorption items,
                                           ItemStack held, Component[] failure) {
        OptionalDouble value = items.value(held);
        if (value.isEmpty()) {
            return null;
        }
        Identifier itemId = items.id(held);
        boolean evolution = false;
        if (items.evolutionType()) {
            EvolutionService.AbsorbCheck check = EvolutionService.checkAbsorb(source, itemId, player.level().registryAccess());
            evolution = check.possible();
            if (!evolution && check.failure() != null) {
                failure[0] = check.failure();
            }
        }
        if (evolution || (items.cultivates() && canCultivate(source, entry.action()))) {
            return new ItemMatch(itemId, value.getAsDouble(), evolution);
        }
        return null;
    }

    // ── blocks around the player ─────────────────────────────────────────────────────

    private static void tickEnvironment(ServerPlayer player, OriginSource source, Entry entry) {
        Environment environment = entry.action().environment().orElse(null);
        if (environment == null) {
            clearBlock(player);
            return;
        }
        ServerLevel level = player.level();

        BlockProgress progress = BLOCKS.get(player.getUUID());
        if (progress != null && !stillValid(player, progress, entry, environment)) {
            clearBlock(player);
            progress = null;
        }
        if (progress == null) {
            if (player.tickCount % SCAN_INTERVAL != 0) {
                return;
            }
            BlockPos found = findBlock(player, source, entry, environment);
            if (found == null) {
                return;
            }
            progress = new BlockProgress(entry.skillId(), level, found);
            BLOCKS.put(player.getUUID(), progress);
        }

        progress.ticks++;
        if (environment.breakingAnimation()) {
            int stage = Math.min(9, progress.ticks * 10 / environment.timeNeeded());
            if (stage != progress.stage) {
                progress.stage = stage;
                level.destroyBlockProgress(player.getId(), progress.pos, stage);
            }
        }
        if (progress.ticks < environment.timeNeeded()) {
            return;
        }

        BlockState state = level.getBlockState(progress.pos);
        BlockPos pos = progress.pos;
        clearBlock(player);
        OptionalDouble value = environment.value(state);
        if (value.isEmpty()) {
            return;
        }
        boolean evolution = environment.evolutionType()
                && EvolutionService.checkAbsorb(source, environment.id(state), level.registryAccess()).possible();
        if (absorb(player, source, entry.action(), environment.id(state), value.getAsDouble(), evolution)) {
            level.destroyBlock(pos, false, player);
        }
    }

    private static boolean stillValid(ServerPlayer player, BlockProgress progress, Entry entry, Environment environment) {
        if (!progress.skillId.equals(entry.skillId()) || progress.level != player.level()) {
            return false;
        }
        BlockPos feet = player.blockPosition();
        // the block the player stands on is left alone, and so is anything that drifted out of the area
        if (progress.pos.equals(feet.below())
                || !environment.covers(progress.pos.getX() - feet.getX(), progress.pos.getY() - feet.getY(), progress.pos.getZ() - feet.getZ(), 1)) {
            return false;
        }
        BlockState state = progress.level.getBlockState(progress.pos);
        return !state.isAir() && environment.value(state).isPresent();
    }

    private static BlockPos findBlock(ServerPlayer player, OriginSource source, Entry entry, Environment environment) {
        ServerLevel level = player.level();
        BlockPos center = player.blockPosition();
        int radius = environment.radius();
        boolean canCultivate = canCultivate(source, entry.action());
        Map<Identifier, Boolean> evolutionCache = new HashMap<>();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    double distance = dx * dx + dy * dy + dz * dz;
                    // the block the player stands on is never absorbed
                    if ((dx == 0 && dy == -1 && dz == 0) || !environment.covers(dx, dy, dz, 0) || distance >= bestDistance) {
                        continue;
                    }
                    cursor.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
                    BlockState state = level.getBlockState(cursor);
                    if (state.isAir() || environment.value(state).isEmpty()
                            || state.getDestroySpeed(level, cursor) < 0.0F
                            || level.getBlockEntity(cursor) != null) {
                        continue;
                    }
                    boolean evolution = environment.evolutionType() && evolutionCache.computeIfAbsent(environment.id(state),
                            id -> EvolutionService.checkAbsorb(source, id, level.registryAccess()).possible());
                    if (!canCultivate && !evolution) {
                        continue;
                    }
                    best = cursor.immutable();
                    bestDistance = distance;
                }
            }
        }
        return best;
    }

    // ── shared ────────────────────────────────────────────────────────────────────────

    /**
     * Applies one absorbed item / block.
     *
     * @return whether it was absorbed (so the item / block should be used up)
     */
    private static boolean absorb(ServerPlayer player, OriginSource source, AbsorptionCultivationSkill action, Identifier id,
                                  double value, boolean evolution) {
        if (evolution && EvolutionService.applyAbsorb(player, source, id, player.level().registryAccess())) {
            return true;
        }
        // not an evolution (or it no longer is one): items only cultivate when cultivation_type is on, blocks always do
        return cultivate(player, source, action, value);
    }

    private static boolean canCultivate(OriginSource source, AbsorptionCultivationSkill action) {
        PathInstance<?> path = AscensionOriginSourceHelper.getPathInstance(source, action.path());
        return path != null && path.canProgress();
    }

    private static boolean cultivate(ServerPlayer player, OriginSource source, AbsorptionCultivationSkill action, double amount) {
        PathInstance<?> path = AscensionOriginSourceHelper.getPathInstance(source, action.path());
        if (path == null || !path.canProgress() || amount <= 0.0D) {
            return false;
        }
        CultivationUtil.cultivate(player, source, action.path(), path, action.path(), amount);
        return true;
    }

    private static void clearBlock(ServerPlayer player) {
        BlockProgress progress = BLOCKS.remove(player.getUUID());
        if (progress != null && progress.stage >= 0) {
            progress.level.destroyBlockProgress(player.getId(), progress.pos, -1);
        }
    }

    @EventBusSubscriber(modid = AscensionCraft.MOD_ID)
    public static final class ServerEvents {
        // the absorb action stopped running (the cast ended), so drop what was in progress
        @SubscribeEvent
        public static void onPlayerTick(PlayerTickEvent.Post event) {
            if (!(event.getEntity() instanceof ServerPlayer player)) {
                return;
            }
            Long last = LAST_TICK.get(player.getUUID());
            if (last != null && player.level().getGameTime() - last > STALE_TICKS) {
                stop(player);
            }
        }

        @SubscribeEvent
        public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
            if (event.getEntity() instanceof ServerPlayer player) {
                stop(player);
            }
        }

        @SubscribeEvent
        public static void onServerStopped(ServerStoppedEvent event) {
            ITEMS.clear();
            BLOCKS.clear();
            LAST_TICK.clear();
        }
    }
}