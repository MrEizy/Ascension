package net.zic.ascension.impl.core.golden_core;

import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.api.ascension.core.golden_core.GoldenCoreData;
import net.zic.ascension.api.rpg_engine.source.data_source.util.SerializerHandler;
import net.zic.ascension.api.rpg_engine.source.data_source.util.SyncHandler;

//data for golden cores that hold no state
public final class EmptyGoldenCoreData implements GoldenCoreData {
    public static final SerializerHandler<EmptyGoldenCoreData> SERIALIZER_HANDLER = new SerializerHandler<>() {
        @Override
        public EmptyGoldenCoreData read(ValueInput input, RegistryAccess access) {
            return new EmptyGoldenCoreData();
        }

        @Override
        public void write(EmptyGoldenCoreData writable, ValueOutput output, RegistryAccess access) {
        }
    };
    public static final SyncHandler<EmptyGoldenCoreData> SYNC_HANDLER = new SyncHandler.UnitSyncHandler<>();
}
