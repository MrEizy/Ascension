package net.zic.ascension.api.ascension.core.skill.castable;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.zic.ascension.api.rpg_engine.source.data_source.util.SerializerHandler;
import net.zic.ascension.api.rpg_engine.source.data_source.util.SyncHandler;

/**
 * state a castable skill keeps while it sits in a hot bar slot, held by the {@link SkillSlot}.
 * saved with the hot bar and synced to the owner through the skill's equip handlers.
 */
public interface EquipSkillData {

    //for skills that keep no equip state
    final class Empty implements EquipSkillData {
        public static final Empty INSTANCE = new Empty();
        public static final SerializerHandler<Empty> SERIALIZER_HANDLER = new SerializerHandler<>() {
            @Override
            public Empty read(ValueInput input, RegistryAccess access) {
                return INSTANCE;
            }

            @Override
            public void write(Empty writable, ValueOutput output, RegistryAccess access) {
            }
        };
        public static final SyncHandler<Empty> SYNC_HANDLER = new SyncHandler.UnitSyncHandler<>();

        private Empty() {
        }
    }
}
