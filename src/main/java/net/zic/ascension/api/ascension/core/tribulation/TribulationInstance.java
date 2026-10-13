package net.zic.ascension.api.ascension.core.tribulation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.RecordBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.zic.ascension.api.ascension.datapack.tribulation.TribulationResolutionActionType;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * A running tribulation, created and owned by the {@link TribulationManager}.
 * Wraps a Tribulation, its data, where it is, who it targets and what to do once it is finished.
 */
public final class TribulationInstance<T extends TribulationData> {
    public enum State implements StringRepresentable {
        ACTIVE("active"),
        SUCCEEDED("succeeded"),
        FAILED("failed");

        public static final Codec<State> CODEC = StringRepresentable.fromEnum(State::values);
        private final String name;

        State(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    private static final Codec<List<TribulationResolutionAction>> ACTIONS_CODEC = TribulationResolutionActionType.CODEC.listOf();

    private final UUID id;
    private final Identifier tribulationId;
    private final Tribulation<T> tribulation;
    private final T data;
    private ResourceKey<Level> dimension;
    private Vec3 position;
    private final @Nullable TribulationTarget target;
    private final List<TribulationResolutionAction> onSuccess;
    private final List<TribulationResolutionAction> onFailure;
    private State state;
    private long age;

    //cached since they are accessed multiple times per tick, only updated when the position changes
    private BlockPos blockPosition;
    private ChunkPos chunkPos;
    private final boolean successRequiresTarget; //TODO instance should not control this, it should be up to the trib
    private final boolean failureRequiresTarget;

    //manager state, not saved
    private boolean queuedForRemoval;
    private boolean discarded;
    private boolean awaitingResolution;

    //sync state, not saved
    private boolean dataDirty;
    private @Nullable ResourceKey<Level> syncedDimension;
    private @Nullable ChunkPos syncedChunk;
    private @Nullable Vec3 syncedPosition;

    TribulationInstance(
            UUID id,
            Identifier tribulationId,
            Tribulation<T> tribulation,
            ResourceKey<Level> dimension,
            Vec3 position,
            @Nullable TribulationTarget target,
            List<TribulationResolutionAction> onSuccess,
            List<TribulationResolutionAction> onFailure,
            State state,
            long age,
            T data
    ) {
        this.id = id;
        this.tribulationId = tribulationId;
        this.tribulation = tribulation;
        this.dimension = dimension;
        this.position = position;
        this.target = target;
        this.onSuccess = List.copyOf(onSuccess);
        this.onFailure = List.copyOf(onFailure);
        this.state = state;
        this.age = age;
        this.data = data;
        this.blockPosition = BlockPos.containing(position);
        this.chunkPos = ChunkPos.containing(blockPosition);
        this.successRequiresTarget = this.onSuccess.stream().anyMatch(TribulationResolutionAction::requiresLoadedTarget);
        this.failureRequiresTarget = this.onFailure.stream().anyMatch(TribulationResolutionAction::requiresLoadedTarget);
    }

    //creates a fresh instance for a tribulation that is just starting
    static <T extends TribulationData> TribulationInstance<T> create(
            UUID id,
            Identifier tribulationId,
            Tribulation<T> tribulation,
            ResourceKey<Level> dimension,
            Vec3 position,
            @Nullable TribulationTarget target,
            List<TribulationResolutionAction> onSuccess,
            List<TribulationResolutionAction> onFailure
    ) {
        return new TribulationInstance<>(id, tribulationId, tribulation, dimension, position, target, onSuccess, onFailure, State.ACTIVE, 0, tribulation.newData());
    }

    //──Getters────────────────────────────────────────────────────────
    public UUID getId() {return id;}
    public Identifier getTribulationId() {return tribulationId;}
    public Tribulation<T> getTribulation() {return tribulation;}
    public T getData() {return data;}
    public ResourceKey<Level> getDimension() {return dimension;}
    public Vec3 getPosition() {return position;}
    public BlockPos getBlockPosition() {return blockPosition;}
    public ChunkPos getChunkPos() {return chunkPos;}
    public @Nullable TribulationTarget getTarget() {return target;}
    public List<TribulationResolutionAction> getSuccessActions() {return onSuccess;}
    public List<TribulationResolutionAction> getFailureActions() {return onFailure;}
    public State getState() {return state;}
    public long getAge() {return age;}

    public boolean hasTarget() {
        return target != null;
    }

    public boolean isTargeting(UUID entity) {
        return target != null && target.getEntityId().equals(entity);
    }

    public @Nullable LivingEntity getTargetEntity() {
        return target == null ? null : target.getEntity();
    }

    public boolean isActive() {
        return state == State.ACTIVE;
    }

    public boolean isFinished() {
        return state != State.ACTIVE;
    }

    //──State────────────────────────────────────────────────────────

    public void succeed() {
        finish(State.SUCCEEDED, TribulationOutcome.SUCCESS);
    }

    public void fail() {
        finish(State.FAILED, TribulationOutcome.FAILURE);
    }

    private void finish(State newState, TribulationOutcome outcome) {
        if (isFinished()) return;
        state = newState;
        tribulation.onFinished(this, outcome);
    }

    public @Nullable TribulationOutcome getOutcome() {
        return switch (state) {
            case ACTIVE -> null;
            case SUCCEEDED -> TribulationOutcome.SUCCESS;
            case FAILED -> TribulationOutcome.FAILURE;
        };
    }

    public void moveTo(ResourceKey<Level> dimension, Vec3 position) {
        this.dimension = dimension;
        if (this.position.equals(position)) return;
        this.position = position;
        BlockPos newBlockPosition = BlockPos.containing(position);
        if (newBlockPosition.equals(blockPosition)) return;
        blockPosition = newBlockPosition;
        if (chunkPos.x() != SectionPos.blockToSectionCoord(blockPosition.getX()) || chunkPos.z() != SectionPos.blockToSectionCoord(blockPosition.getZ())) {
            chunkPos = ChunkPos.containing(blockPosition);
        }
    }

    //call when the TribulationData has changed, so it is resent to tracking clients
    public void markDataDirty() {
        dataDirty = true;
    }

    //──Dispatch────────────────────────────────────────────────────────
    //typed calls into the tribulation, so the manager never handles the untyped Tribulation

    void start(ServerLevel level) {
        tribulation.onStart(this, level);
    }

    void tick(ServerLevel level) {
        tribulation.tick(this, level);
    }

    void onTargetDeath() {
        tribulation.onTargetDeath(this);
    }

    //encodes the data for syncing to clients
    public ByteBuf encodeData(RegistryAccess access) {
        return TribulationSyncHelper.encode(tribulation, data, access);
    }

    void incrementAge() {
        age++;
    }

    void setTargetEntity(@Nullable LivingEntity entity) {
        if (target != null) target.setEntity(entity);
    }

    //whether the resolution actions for the current outcome need the target to be loaded
    boolean resolutionRequiresTarget() {
        return state == State.SUCCEEDED ? successRequiresTarget : failureRequiresTarget;
    }

    //──Manager State────────────────────────────────────────────────────────

    boolean isQueuedForRemoval() {return queuedForRemoval;}
    void setQueuedForRemoval(boolean queuedForRemoval) {this.queuedForRemoval = queuedForRemoval;}
    boolean isDiscarded() {return discarded;}
    void markDiscarded() {this.discarded = true;}
    //only set on finished instances whose resolution actions are waiting for the target to load
    boolean isAwaitingResolution() {return awaitingResolution;} //TODO remove, it is up to the tribulations to handle this
    void setAwaitingResolution(boolean awaitingResolution) {this.awaitingResolution = awaitingResolution;}

    //──Sync────────────────────────────────────────────────────────

    boolean consumeDataDirty() {
        boolean dirty = dataDirty;
        dataDirty = false;
        return dirty;
    }

    @Nullable ResourceKey<Level> getSyncedDimension() {return syncedDimension;}
    @Nullable ChunkPos getSyncedChunk() {return syncedChunk;}
    @Nullable Vec3 getSyncedPosition() {return syncedPosition;}

    void setSynced(@Nullable ResourceKey<Level> dimension, @Nullable ChunkPos chunk, @Nullable Vec3 position) {
        this.syncedDimension = dimension;
        this.syncedChunk = chunk;
        this.syncedPosition = position;
    }

    //──Serialization────────────────────────────────────────────────────────

    <V> DataResult<V> encode(DynamicOps<V> ops) {
        RecordBuilder<V> builder = ops.mapBuilder();
        builder.add("id", UUIDUtil.CODEC.encodeStart(ops, id));
        builder.add("tribulation", Identifier.CODEC.encodeStart(ops, tribulationId));
        builder.add("data", tribulation.getType().dataCodec().encodeStart(ops, data));
        builder.add("dimension", Level.RESOURCE_KEY_CODEC.encodeStart(ops, dimension));
        builder.add("pos", Vec3.CODEC.encodeStart(ops, position));
        if (target != null) builder.add("target", TribulationTarget.CODEC.encodeStart(ops, target));
        builder.add("on_success", ACTIONS_CODEC.encodeStart(ops, onSuccess));
        builder.add("on_failure", ACTIONS_CODEC.encodeStart(ops, onFailure));
        builder.add("state", State.CODEC.encodeStart(ops, state));
        builder.add("age", ops.createLong(age));
        return builder.build(ops.empty());
    }

    /**
     * loads a saved instance, returns an error if the tribulation no longer exists or any part fails to load,
     * in which case the instance is expected to be discarded.
     */
    static <V> DataResult<TribulationInstance<?>> decode(Dynamic<V> input, Registry<Tribulation<?>> registry) {
        DataResult<UUID> id = input.get("id").read(UUIDUtil.CODEC);
        DataResult<Identifier> tribulationId = input.get("tribulation").read(Identifier.CODEC);
        if (id.isError() || tribulationId.isError()) return DataResult.error(() -> "invalid tribulation id");

        Optional<Tribulation<?>> tribulation = registry.getOptional(tribulationId.getOrThrow());
        if (tribulation.isEmpty()) return DataResult.error(() -> "unknown tribulation " + tribulationId.getOrThrow());

        return decodeTyped(input, id.getOrThrow(), tribulationId.getOrThrow(), tribulation.get()).map(instance -> instance);
    }

    private static <V, T extends TribulationData> DataResult<TribulationInstance<T>> decodeTyped(Dynamic<V> input, UUID id, Identifier tribulationId, Tribulation<T> tribulation) {
        DataResult<T> data = input.get("data").read(tribulation.getType().dataCodec());
        DataResult<ResourceKey<Level>> dimension = input.get("dimension").read(Level.RESOURCE_KEY_CODEC);
        DataResult<Vec3> position = input.get("pos").read(Vec3.CODEC);
        Optional<DataResult<TribulationTarget>> target = input.get("target").result().map(dynamic -> dynamic.read(TribulationTarget.CODEC));
        DataResult<List<TribulationResolutionAction>> onSuccess = input.get("on_success").read(ACTIONS_CODEC);
        DataResult<List<TribulationResolutionAction>> onFailure = input.get("on_failure").read(ACTIONS_CODEC);
        DataResult<State> state = input.get("state").read(State.CODEC);

        if (data.isError() || dimension.isError() || position.isError() || onSuccess.isError()
                || onFailure.isError() || state.isError() || (target.isPresent() && target.get().isError())) {
            return DataResult.error(() -> "failed to load tribulation " + tribulationId);
        }

        T validatedData = tribulation.validateLoadedData(data.getOrThrow());
        if (validatedData == null) return DataResult.error(() -> "tribulation data rejected for " + tribulationId);

        return DataResult.success(new TribulationInstance<>(
                id,
                tribulationId,
                tribulation,
                dimension.getOrThrow(),
                position.getOrThrow(),
                target.map(DataResult::getOrThrow).orElse(null),
                onSuccess.getOrThrow(),
                onFailure.getOrThrow(),
                state.getOrThrow(),
                input.get("age").asLong(0),
                validatedData
        ));
    }
}
