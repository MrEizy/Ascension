package net.zic.ascension.api.ascension.core.tribulation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.level.ChunkWatchEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.network.tribulation.ClientboundTribulationAddPacket;
import net.zic.ascension.network.tribulation.ClientboundTribulationRemovePacket;
import net.zic.ascension.network.tribulation.ClientboundTribulationUpdatePacket;
import org.jspecify.annotations.Nullable;

import java.util.*;

/**
 * Holds and runs every active tribulation on the server.
 * Tribulations are kept in memory for as long as the server runs, but only tick while the chunk they are in is loaded.
 * Clients are kept in sync with any tribulation in a chunk they are tracking.
 */
@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public class TribulationManager extends SavedData {

    //instances are saved raw and only decoded once the server has started, that way the tribulation registry is available
    private static final Codec<TribulationManager> CODEC = Codec.PASSTHROUGH.listOf()
            .optionalFieldOf("tribulations", List.of())
            .codec()
            .xmap(TribulationManager::new, TribulationManager::encodeInstances);

    public static final SavedDataType<TribulationManager> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "tribulations"),
            TribulationManager::new,
            CODEC
    );

    private static @Nullable TribulationManager instance;

    public static @Nullable TribulationManager getInstance() {
        return instance;
    }

    public static TribulationManager get(MinecraftServer server) {
        TribulationManager manager = server.getDataStorage().computeIfAbsent(TYPE);
        manager.server = server;
        return manager;
    }

    private final List<Dynamic<?>> pending = new ArrayList<>();
    private final LinkedHashMap<UUID, TribulationInstance<?>> tribulations = new LinkedHashMap<>();
    private final HashMap<ResourceKey<Level>, HashMap<Long, HashSet<UUID>>> chunkTribulations = new HashMap<>();
    private @Nullable MinecraftServer server;

    //structural changes made while iterating tribulations are deferred to these, they are reused every tick to avoid allocations
    private boolean deferring;
    private final ArrayList<TribulationInstance<?>> toRemove = new ArrayList<>();
    private final ArrayList<TribulationInstance<?>> toAdd = new ArrayList<>();

    public TribulationManager() {
    }

    private TribulationManager(List<Dynamic<?>> saved) {
        pending.addAll(saved);
    }

    private List<Dynamic<?>> encodeInstances() {
        List<Dynamic<?>> encoded = new ArrayList<>(pending);
        for (TribulationInstance<?> tribulation : tribulations.values()) {
            DataResult<?> result = tribulation.encode(NbtOps.INSTANCE);
            result.resultOrPartial(error -> AscensionCraft.LOGGER.error("Failed to save tribulation {}: {}", tribulation.getId(), error))
                    .ifPresent(tag -> encoded.add(new Dynamic<>(NbtOps.INSTANCE, (net.minecraft.nbt.Tag) tag)));
        }
        return encoded;
    }

    private void loadPending(MinecraftServer server) {
        if (pending.isEmpty()) return;
        Registry<Tribulation<?>> registry = CoreRegistries.TRIBULATION_REGISTRY.get(server.registryAccess());
        for (Dynamic<?> saved : pending) {
            DataResult<TribulationInstance<?>> result = TribulationInstance.decode(saved, registry);
            if (result.isError()) {
                //treat it as if it never existed
                AscensionCraft.LOGGER.debug("Discarding tribulation: {}", result.error().map(DataResult.Error::message).orElse(""));
                continue;
            }
            addInstance(result.getOrThrow());
        }
        pending.clear();
        setDirty();
    }

    //──Starting────────────────────────────────────────────────────────

    public Builder create(Identifier tribulation) {
        return new Builder(this, tribulation);
    }

    private @Nullable UUID start(Builder builder) {
        if (server == null || builder.level == null || builder.position == null) return null;
        Registry<Tribulation<?>> registry = CoreRegistries.TRIBULATION_REGISTRY.get(server.registryAccess());
        Tribulation<?> tribulation = registry.getOptional(builder.tribulationId).orElse(null);
        if (tribulation == null) {
            AscensionCraft.LOGGER.warn("Tried to start unknown tribulation {}", builder.tribulationId);
            return null;
        }

        TribulationTarget target = null;
        if (builder.target != null) {
            target = new TribulationTarget(builder.target.getUUID(), builder.dimensionChangeBehaviour);
            target.setEntity(builder.target);
        }

        TribulationInstance<?> instance = TribulationInstance.create(
                UUID.randomUUID(),
                builder.tribulationId,
                tribulation,
                builder.level.dimension(),
                builder.position,
                target,
                builder.onSuccess,
                builder.onFailure
        );
        if (deferring) toAdd.add(instance);
        else addInstance(instance);
        instance.start(builder.level);
        syncInstance(instance);
        setDirty();
        return instance.getId();
    }

    private void addInstance(TribulationInstance<?> instance) {
        tribulations.put(instance.getId(), instance);
        indexChunk(instance, instance.getDimension(), instance.getChunkPos());
    }

    private void removeInstance(TribulationInstance<?> instance) {
        tribulations.remove(instance.getId());
        unindexChunk(instance, instance.getDimension(), instance.getChunkPos());
        sendRemove(instance);
        setDirty();
    }

    //──Access────────────────────────────────────────────────────────

    public boolean has(UUID id) {
        return get(id) != null;
    }

    public @Nullable TribulationInstance<?> get(UUID id) {
        TribulationInstance<?> instance = tribulations.get(id);
        if (instance != null) return instance;
        //started this tick, added to the map once the tick ends
        for (int i = 0; i < toAdd.size(); i++) {
            if (toAdd.get(i).getId().equals(id)) return toAdd.get(i);
        }
        return null;
    }

    public Collection<TribulationInstance<?>> getAll() {
        return Collections.unmodifiableCollection(tribulations.values());
    }

    public List<TribulationInstance<?>> getForEntity(UUID entity) {
        List<TribulationInstance<?>> result = new ArrayList<>();
        for (TribulationInstance<?> instance : tribulations.values()) {
            if (instance.isTargeting(entity)) result.add(instance);
        }
        return result;
    }

    //fails the tribulation, running failure actions
    public void cancel(UUID id) {
        TribulationInstance<?> instance = tribulations.get(id);
        if (instance == null) return;
        instance.fail();
        setDirty();
    }

    //removes the tribulation without running any resolution logic
    public void discard(UUID id) {
        TribulationInstance<?> instance = tribulations.get(id);
        if (instance == null) return;
        if (deferring) {
            instance.markDiscarded();
            queueRemoval(instance);
            return;
        }
        removeInstance(instance);
    }

    //removes every tribulation without running any resolution logic
    public void discardAll() {
        for (TribulationInstance<?> instance : tribulations.values()) {
            sendRemove(instance);
        }
        tribulations.clear();
        chunkTribulations.clear();
        toRemove.clear();
        toAdd.clear();
        setDirty();
    }

    //──Ticking────────────────────────────────────────────────────────

    private void tick(MinecraftServer server) {
        boolean started = beginDeferring();
        for (TribulationInstance<?> instance : tribulations.values()) {
            if (instance.isQueuedForRemoval()) continue;
            if (instance.isActive()) {
                //ticked whether or not the target is present, the tribulation decides what to do
                ServerLevel level = server.getLevel(instance.getDimension());
                if (level == null) continue;

                followTarget(instance);
                if (!level.isLoaded(instance.getBlockPosition())) continue;

                instance.tick(level);
                instance.incrementAge();
                setDirty();
            }
            if (instance.isFinished()) {
                queueRemoval(instance);
            } else {
                syncInstance(instance);
            }
        }
        endDeferring(started);
    }

    /**
     * defers adding/removing tribulations until {@link #endDeferring} so we can safely iterate them.
     * returns false if we were already deferring, in which case the outer call handles applying changes
     */
    private boolean beginDeferring() {
        if (deferring) return false;
        deferring = true;
        return true;
    }

    private void endDeferring(boolean started) {
        if (!started) return;
        deferring = false;

        //resolution happens outside of iteration, so actions are free to start or discard tribulations
        for (int i = 0; i < toRemove.size(); i++) {
            TribulationInstance<?> instance = toRemove.get(i);
            instance.setQueuedForRemoval(false);
            if (!tribulations.containsKey(instance.getId())) continue;
            if (instance.isDiscarded()) removeInstance(instance);
            else tryResolve(instance);
        }
        toRemove.clear();

        for (int i = 0; i < toAdd.size(); i++) {
            addInstance(toAdd.get(i));
        }
        toAdd.clear();
    }

    private void queueRemoval(TribulationInstance<?> instance) {
        if (instance.isQueuedForRemoval()) return;
        instance.setQueuedForRemoval(true);
        toRemove.add(instance);
    }

    //moves the tribulation to the targets position
    private void followTarget(TribulationInstance<?> instance) {
        LivingEntity entity = instance.getTargetEntity();
        if (entity == null) return;
        if (entity.isRemoved()) {
            instance.setTargetEntity(null);
            return;
        }
        if (!entity.level().dimension().equals(instance.getDimension())) {
            onTargetChangedDimension(instance, entity);
            return;
        }
        move(instance, instance.getDimension(), entity.position());
    }

    private void onTargetChangedDimension(TribulationInstance<?> instance, LivingEntity entity) {
        switch (instance.getTarget().getDimensionChangeBehaviour()) {
            case FOLLOW -> {
                instance.setTargetEntity(entity);
                move(instance, entity.level().dimension(), entity.position());
            }
            case FAIL -> {
                instance.setTargetEntity(entity);
                instance.fail();
            }
            case STAY -> instance.setTargetEntity(null);
        }
    }

    private void move(TribulationInstance<?> instance, ResourceKey<Level> dimension, Vec3 position) {
        ResourceKey<Level> oldDimension = instance.getDimension();
        if (oldDimension.equals(dimension) && instance.getPosition().equals(position)) return;
        ChunkPos oldChunk = instance.getChunkPos();
        instance.moveTo(dimension, position);
        ChunkPos newChunk = instance.getChunkPos();
        if (!oldDimension.equals(dimension) || !oldChunk.equals(newChunk)) {
            unindexChunk(instance, oldDimension, oldChunk);
            indexChunk(instance, dimension, newChunk);
        }
    }

    private void tryResolve(TribulationInstance<?> instance) {
        if (server == null) return;
        TribulationOutcome outcome = instance.getOutcome();
        List<TribulationResolutionAction> actions = outcome == TribulationOutcome.SUCCESS ? instance.getSuccessActions() : instance.getFailureActions();
        //the target may not be loaded, actions are expected to handle that
        LivingEntity target = instance.getTargetEntity();

        removeInstance(instance);
        TribulationResolutionContext context = new TribulationResolutionContext(server, instance, outcome, target);
        for (TribulationResolutionAction action : actions) {
            try {
                action.resolve(context);
            } catch (Exception exception) {
                AscensionCraft.LOGGER.error("Tribulation resolution action failed for {}", instance.getTribulationId(), exception);
            }
        }
    }

    //──Chunk Index────────────────────────────────────────────────────────

    private void indexChunk(TribulationInstance<?> instance, ResourceKey<Level> dimension, ChunkPos chunk) {
        chunkTribulations.computeIfAbsent(dimension, key -> new HashMap<>())
                .computeIfAbsent(chunk.pack(), key -> new HashSet<>())
                .add(instance.getId());
    }

    private void unindexChunk(TribulationInstance<?> instance, ResourceKey<Level> dimension, ChunkPos chunk) {
        HashMap<Long, HashSet<UUID>> chunks = chunkTribulations.get(dimension);
        if (chunks == null) return;
        HashSet<UUID> ids = chunks.get(chunk.pack());
        if (ids == null) return;
        ids.remove(instance.getId());
        if (ids.isEmpty()) chunks.remove(chunk.pack());
        if (chunks.isEmpty()) chunkTribulations.remove(dimension);
    }

    private List<TribulationInstance<?>> getInChunk(ResourceKey<Level> dimension, ChunkPos chunk) {
        HashMap<Long, HashSet<UUID>> chunks = chunkTribulations.get(dimension);
        if (chunks == null) return List.of();
        HashSet<UUID> ids = chunks.get(chunk.pack());
        if (ids == null) return List.of();

        List<TribulationInstance<?>> list = new ArrayList<>();
        for (UUID id : ids) {
            TribulationInstance<?> tribulationInstance = tribulations.get(id);
            if (tribulationInstance != null) {
                list.add(tribulationInstance);
            }
        }
        return list;
    }

    //──Sync────────────────────────────────────────────────────────

    private List<ServerPlayer> getTrackingPlayers(@Nullable ResourceKey<Level> dimension, @Nullable ChunkPos chunk) {
        if (server == null || dimension == null || chunk == null) return List.of();
        ServerLevel level = server.getLevel(dimension);
        if (level == null) return List.of();
        return level.getChunkSource().chunkMap.getPlayers(chunk, false);
    }

    //sends any changes since the last sync, players tracking the old chunk but not the new one get a remove
    private void syncInstance(TribulationInstance<?> instance) {
        if (!instance.isDirty()) return;
        List<ServerPlayer> newPlayers = getTrackingPlayers(instance.getDimension(), instance.getChunkPos());
        //the old position is only meaningful if it moved since the last sync, otherwise it is where clients already see it
        List<ServerPlayer> oldPlayers = new ArrayList<>(instance.isPositionDirty()
                ? getTrackingPlayers(instance.getOldDimension(), instance.getOldChunk())
                : newPlayers);

        ClientboundTribulationAddPacket add = null;
        ClientboundTribulationUpdatePacket update = null;
        for (ServerPlayer player : newPlayers) {
            if (oldPlayers.remove(player)) {
                if (update == null) update = createUpdatePacket(instance, player.registryAccess());
                PacketDistributor.sendToPlayer(player, update);
            } else {
                if (add == null) add = createAddPacket(instance, player.registryAccess());
                PacketDistributor.sendToPlayer(player, add);
            }
        }

        if (!oldPlayers.isEmpty()) {
            ClientboundTribulationRemovePacket remove = new ClientboundTribulationRemovePacket(instance.getId());
            for (ServerPlayer player : oldPlayers) PacketDistributor.sendToPlayer(player, remove);
        }
        instance.markSynced();
    }

    //sent to players based on where they were last told the tribulation was
    private void sendRemove(TribulationInstance<?> instance) {
        ClientboundTribulationRemovePacket remove = new ClientboundTribulationRemovePacket(instance.getId());
        List<ServerPlayer> players = instance.isPositionDirty()
                ? getTrackingPlayers(instance.getOldDimension(), instance.getOldChunk())
                : getTrackingPlayers(instance.getDimension(), instance.getChunkPos());
        for (ServerPlayer player : players) {
            PacketDistributor.sendToPlayer(player, remove);
        }
    }

    private ClientboundTribulationAddPacket createAddPacket(TribulationInstance<?> instance, RegistryAccess access) {
        return new ClientboundTribulationAddPacket(
                instance.getId(),
                instance.getTribulationId(),
                instance.getDimension(),
                instance.getPosition(),
                instance.encodeData(access)
        );
    }

    private ClientboundTribulationUpdatePacket createUpdatePacket(TribulationInstance<?> instance, RegistryAccess access) {
        return new ClientboundTribulationUpdatePacket(
                instance.getId(),
                instance.isPositionDirty() ? instance.getPosition() : null,
                instance.isDataDirty() ? instance.encodeData(access) : null
        );
    }

    //──Events────────────────────────────────────────────────────────

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        instance = get(event.getServer());
        instance.loadPending(event.getServer());
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        instance = null;
    }

    @SubscribeEvent
    public static void onTick(ServerTickEvent.Post event) {
        if (instance == null) return;
        instance.tick(event.getServer());
    }

    @SubscribeEvent
    public static void onChunkSent(ChunkWatchEvent.Sent event) {
        if (instance == null) return;
        for (TribulationInstance<?> tribulation : instance.getInChunk(event.getLevel().dimension(), event.getPos())) {
            PacketDistributor.sendToPlayer(event.getPlayer(), instance.createAddPacket(tribulation, event.getLevel().registryAccess()));
        }
    }

    @SubscribeEvent
    public static void onChunkUnWatch(ChunkWatchEvent.UnWatch event) {
        if (instance == null) return;
        for (TribulationInstance<?> tribulation : instance.getInChunk(event.getLevel().dimension(), event.getPos())) {
            PacketDistributor.sendToPlayer(event.getPlayer(), new ClientboundTribulationRemovePacket(tribulation.getId()));
        }
    }

    @SubscribeEvent
    public static void onJoinLevel(EntityJoinLevelEvent event) {
        if (instance == null || instance.tribulations.isEmpty() || event.getLevel().isClientSide() || !(event.getEntity() instanceof LivingEntity entity)) return;
        boolean started = instance.beginDeferring();
        for (TribulationInstance<?> tribulation : instance.tribulations.values()) {
            if (!tribulation.isActive() || !tribulation.isTargeting(entity.getUUID())) continue;
            if (entity.level().dimension().equals(tribulation.getDimension())) {
                tribulation.setTargetEntity(entity);
            } else {
                instance.onTargetChangedDimension(tribulation, entity);
            }
        }
        instance.endDeferring(started);
    }

    @SubscribeEvent
    public static void onLeaveLevel(EntityLeaveLevelEvent event) {
        if (instance == null || instance.tribulations.isEmpty() || event.getLevel().isClientSide()) return;
        for (TribulationInstance<?> tribulation : instance.tribulations.values()) {
            if (tribulation.getTargetEntity() == event.getEntity()) tribulation.setTargetEntity(null);
        }
    }

    @SubscribeEvent
    public static void onEntityDeath(LivingDeathEvent event) {
        if (instance == null || instance.tribulations.isEmpty() || event.getEntity().level().isClientSide()) return;
        //if we are not already ticking, this resolves immediately while the entity is still present
        boolean started = instance.beginDeferring();
        for (TribulationInstance<?> tribulation : instance.tribulations.values()) {
            if (!tribulation.isActive() || !tribulation.isTargeting(event.getEntity().getUUID())) continue;
            tribulation.setTargetEntity(event.getEntity());
            tribulation.onTargetDeath();
            if (tribulation.isFinished()) instance.queueRemoval(tribulation);
        }
        instance.endDeferring(started);
    }

    //──Builder────────────────────────────────────────────────────────

    public static final class Builder {
        private final TribulationManager manager;
        private final Identifier tribulationId;
        private @Nullable ServerLevel level;
        private @Nullable Vec3 position;
        private @Nullable LivingEntity target;
        private DimensionChangeBehaviour dimensionChangeBehaviour = DimensionChangeBehaviour.STAY;
        private final List<TribulationResolutionAction> onSuccess = new ArrayList<>();
        private final List<TribulationResolutionAction> onFailure = new ArrayList<>();

        private Builder(TribulationManager manager, Identifier tribulationId) {
            this.manager = manager;
            this.tribulationId = tribulationId;
        }

        public Builder at(ServerLevel level, Vec3 position) {
            this.level = level;
            this.position = position;
            return this;
        }

        //tracks the entity, also sets the position to the entity
        public Builder targeting(LivingEntity entity) {
            return targeting(entity, DimensionChangeBehaviour.STAY);
        }

        public Builder targeting(LivingEntity entity, DimensionChangeBehaviour behaviour) {
            this.target = entity;
            this.dimensionChangeBehaviour = behaviour;
            if (entity.level() instanceof ServerLevel serverLevel) at(serverLevel, entity.position());
            return this;
        }

        public Builder onSuccess(TribulationResolutionAction... actions) {
            onSuccess.addAll(List.of(actions));
            return this;
        }

        public Builder onFailure(TribulationResolutionAction... actions) {
            onFailure.addAll(List.of(actions));
            return this;
        }

        //returns the id of the started tribulation, or null if it could not be started
        public @Nullable UUID start() {
            return manager.start(this);
        }
    }
}
