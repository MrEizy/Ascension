package net.zic.ascension.impl.core.innerworld;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.UUID;

/**
 * The body left behind in the overworld while its owner is inside their inner world. A real
 * LivingEntity so it has health, can be hit by anything, and fires a death event we intercept.
 * Anchored in place (no gravity, no pushing) so the return spot is always exactly where you cast.
 */
public final class InnerWorldGhost extends LivingEntity {
    private static final EntityDataAccessor<String> OWNER_ID =
            SynchedEntityData.defineId(InnerWorldGhost.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> OWNER_NAME =
            SynchedEntityData.defineId(InnerWorldGhost.class, EntityDataSerializers.STRING);

    public InnerWorldGhost(EntityType<? extends InnerWorldGhost> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(OWNER_ID, "");
        builder.define(OWNER_NAME, "");
    }

    public void setOwner(UUID id, String name) {
        this.entityData.set(OWNER_ID, id.toString());
        this.entityData.set(OWNER_NAME, name);
    }

    public UUID ownerId() {
        String raw = this.entityData.get(OWNER_ID);
        if (raw.isEmpty()) {
            return null;
        }
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public String ownerName() {
        return this.entityData.get(OWNER_NAME);
    }

    @Override
    public HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide() && this.tickCount % 20 == 0) {
            UUID owner = ownerId();
            if (owner == null || !InnerWorldSessions.isGhostValid(owner, this.getUUID())) {
                this.discard();
            }
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putString("owner_id", this.entityData.get(OWNER_ID));
        output.putString("owner_name", this.entityData.get(OWNER_NAME));
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.entityData.set(OWNER_ID, input.getString("owner_id").orElse(""));
        this.entityData.set(OWNER_NAME, input.getString("owner_name").orElse(""));
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(Entity entity) {
    }

    @Override
    protected void pushEntities() {
    }

    @Override
    public void knockback(double power, double xd, double zd) {
    }

    @Override
    protected boolean shouldDropLoot(ServerLevel level) {
        return false;
    }
}