package net.zic.ascension.api.ascension.core.path.bonus.data_source;

import net.minecraft.core.RegistryAccess;
import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.api.ascension.core.CoreAttachments;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.api.rpg_engine.source.data_source.DataSource;
import net.zic.ascension.api.rpg_engine.source.data_source.LoadPriority;
import net.zic.ascension.api.rpg_engine.source.data_source.util.SerializerHandler;
import net.zic.ascension.api.rpg_engine.source.data_source.util.SyncHandler;

public class PathBonusDataSource implements DataSource<DataSourcePathBonusHolder> {

    public static final SerializerHandler<DataSourcePathBonusHolder> SERIALIZER_HANDLER = new SerializerHandler.UnitSerializerHandler<>();
    public static final SyncHandler<DataSourcePathBonusHolder> SYNC_HANDLER = new SyncHandler.UnitSyncHandler<>();

    @Override
    public LoadPriority loadPriority() {
        return LoadPriority.NO_LOAD;
    }

    @Override
    public void onAdded(OriginSource source, DataSourcePathBonusHolder holder) {

    }

    @Override
    public void onRemoved(OriginSource source, DataSourcePathBonusHolder holder) {

    }

    @Override
    public void preFinishedLoading(OriginSource source, DataSourcePathBonusHolder holder) {

    }

    @Override
    public void finishedLoading(OriginSource source, DataSourcePathBonusHolder holder) {

    }

    @Override
    public void applyToEntity(LivingEntity entity, DataSourcePathBonusHolder holder) {
        if(!entity.level().isClientSide())entity.getData(CoreAttachments.PATH_BONUS_HOLDER).registerProvider(holder);

    }

    @Override
    public void removeFromEntity(LivingEntity entity, DataSourcePathBonusHolder holder) {
        if(!entity.level().isClientSide()) entity.getData(CoreAttachments.PATH_BONUS_HOLDER).removeProvider(holder);

    }

    @Override
    public DataSourcePathBonusHolder newInstance(RegistryAccess access) {
        return new DataSourcePathBonusHolder();
    }

    @Override
    public Class<DataSourcePathBonusHolder> getInstanceClass() {
        return DataSourcePathBonusHolder.class;
    }

    @Override
    public SerializerHandler<DataSourcePathBonusHolder> serializerHandler() {
        return SERIALIZER_HANDLER;
    }

    @Override
    public SyncHandler<DataSourcePathBonusHolder> syncHandler(boolean fullPatch) {
        return SYNC_HANDLER;
    }
}
