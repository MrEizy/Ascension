package net.zic.ascension.api.ascension.core.bloodline;

import net.minecraft.core.RegistryAccess;
import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.api.rpg_engine.source.data_source.DataSource;
import net.zic.ascension.api.rpg_engine.source.data_source.LoadPriority;
import net.zic.ascension.api.rpg_engine.source.data_source.util.SerializerHandler;
import net.zic.ascension.api.rpg_engine.source.data_source.util.SyncHandler;

public class BloodlineDataSource implements DataSource<BloodlineHolder> {

    public static final SyncHandler<BloodlineHolder> FULL_PATCH_SYNC_HANDLER = new BloodlineSyncHandlers.FullPatchSyncHandler();
    public static final SyncHandler<BloodlineHolder> PARTIAL_PATCH_SYNC_HANDLER = new BloodlineSyncHandlers.PartialPatchSyncHandler();

    @Override
    public LoadPriority loadPriority() {
        return LoadPriority.HIGHEST;
    }

    @Override
    public void onAdded(OriginSource source, BloodlineHolder instance) {

    }

    @Override
    public void onRemoved(OriginSource source, BloodlineHolder instance) {

    }

    @Override
    public void preFinishedLoading(OriginSource source, BloodlineHolder instance) {

    }

    @Override
    public void finishedLoading(OriginSource source, BloodlineHolder instance) {

    }

    @Override
    public void applyToEntity(LivingEntity entity, BloodlineHolder instance) {

    }

    @Override
    public void removeFromEntity(LivingEntity entity, BloodlineHolder instance) {

    }

    @Override
    public BloodlineHolder newInstance(RegistryAccess access) {
        return new BloodlineHolder();
    }

    @Override
    public Class<BloodlineHolder> getInstanceClass() {
        return BloodlineHolder.class;
    }

    @Override
    public SerializerHandler<BloodlineHolder> serializerHandler() {
        return null;
    }

    @Override
    public SyncHandler<BloodlineHolder> syncHandler(boolean fullPatch) {
        return fullPatch ? FULL_PATCH_SYNC_HANDLER : PARTIAL_PATCH_SYNC_HANDLER;
    }
}
