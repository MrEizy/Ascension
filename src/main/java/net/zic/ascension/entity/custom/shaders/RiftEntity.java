package net.zic.ascension.entity.custom.shaders;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;

public class RiftEntity extends Entity {


    public RiftEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {}

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {}

    @Override
    protected void addAdditionalSaveData(CompoundTag compoundTag) {}

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) return;

        ServerLevel endLevel = level().getServer().getLevel(Level.END);
        if (endLevel == null) return;

        level().getEntities(this, getBoundingBox().inflate(0.25))
                .forEach(entity -> {
                    if (entity == this) return;

                    if (!entity.canChangeDimensions(level(), endLevel)) return;

                    DimensionTransition transition = new DimensionTransition(
                            endLevel,
                            new Vec3(
                                    endLevel.getSharedSpawnPos().getX() + 0.5,
                                    endLevel.getSharedSpawnPos().getY(),
                                    endLevel.getSharedSpawnPos().getZ() + 0.5
                            ),
                            entity.getDeltaMovement(),
                            entity.getYRot(),
                            entity.getXRot(),
                            DimensionTransition.DO_NOTHING
                    );

                    entity.changeDimension(transition);
                });
    }
}
