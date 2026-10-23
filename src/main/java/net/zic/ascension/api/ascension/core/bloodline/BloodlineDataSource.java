package net.zic.ascension.api.ascension.core.bloodline;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.api.rpg_engine.source.data_source.DataSource;
import net.zic.ascension.api.rpg_engine.source.data_source.LoadPriority;
import net.zic.ascension.api.rpg_engine.source.data_source.util.SerializerHandler;
import net.zic.ascension.api.rpg_engine.source.data_source.util.SyncHandler;

import java.util.Map;

public class BloodlineDataSource implements DataSource<BloodlineHolder> {
    public static final SerializerHandler<BloodlineHolder> SERIALIZER_HANDLER = new BloodlineSerializerHandler();
    public static final SyncHandler<BloodlineHolder> FULL_PATCH_SYNC_HANDLER = new BloodlineSyncHandlers.FullPatchSyncHandler();
    public static final SyncHandler<BloodlineHolder> PARTIAL_PATCH_SYNC_HANDLER = new BloodlineSyncHandlers.PartialPatchSyncHandler();

    @Override
    public LoadPriority loadPriority() {
        return LoadPriority.HIGHEST;
    }

    @Override
    public void onAdded(OriginSource source, BloodlineHolder holder) {
        Map<Identifier,BloodlineInstance<?>> bloodlines = holder.getBloodlineSnapshot();

        holder.clearContainer();

        for(BloodlineInstance<?> instance : bloodlines.values()){
            AscensionOriginSourceHelper.addBloodline(source,instance,false);
        }
    }

    @Override
    public void onRemoved(OriginSource source, BloodlineHolder holder) {
        Map<Identifier,BloodlineInstance<?>> bloodlines = holder.getBloodlineSnapshot();

        for(Identifier bloodline : bloodlines.keySet()){
            AscensionOriginSourceHelper.removeBloodline(source,bloodline);
        }

        holder.restore(bloodlines);
    }

    @Override
    public void preFinishedLoading(OriginSource source, BloodlineHolder instance) {

    }

    @Override
    public void finishedLoading(OriginSource source, BloodlineHolder instance) {

    }

    @Override
    public void applyToEntity(LivingEntity entity, BloodlineHolder holder) {
        for(BloodlineInstance<?> instance : holder.getBloodlineInstances()){
            instance.applyToEntity(entity);
        }
    }

    @Override
    public void removeFromEntity(LivingEntity entity, BloodlineHolder holder) {
        for(BloodlineInstance<?> instance : holder.getBloodlineInstances()){
            instance.removeFromEntity(entity);
        }
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
        return SERIALIZER_HANDLER;
    }

    @Override
    public SyncHandler<BloodlineHolder> syncHandler(boolean fullPatch) {
        return fullPatch ? FULL_PATCH_SYNC_HANDLER : PARTIAL_PATCH_SYNC_HANDLER;
    }
}
