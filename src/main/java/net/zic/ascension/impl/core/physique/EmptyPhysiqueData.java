package net.zic.ascension.impl.core.physique;

import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.api.ascension.core.physique.PhysiqueData;
import net.zic.ascension.api.rpg_engine.source.data_source.util.SerializerHandler;
import net.zic.ascension.api.rpg_engine.source.data_source.util.SyncHandler;

//data for physiques that hold no state
public final class EmptyPhysiqueData implements PhysiqueData {
    public static final SerializerHandler<EmptyPhysiqueData> SERIALIZER_HANDLER = new SerializerHandler<>() {
        @Override
        public EmptyPhysiqueData read(ValueInput input, RegistryAccess access) {
            return new EmptyPhysiqueData();
        }

        @Override
        public void write(EmptyPhysiqueData writable, ValueOutput output, RegistryAccess access) {
        }
    };
    public static final SyncHandler<EmptyPhysiqueData> SYNC_HANDLER = new SyncHandler.UnitSyncHandler<>();
}
