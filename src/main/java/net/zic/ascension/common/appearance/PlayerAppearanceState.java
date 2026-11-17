package net.zic.ascension.common.appearance;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.attachment.AttachmentSyncHandler;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.zic.zenithlib.network.ByteBufHelpers;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class PlayerAppearanceState {
    private List<Identifier> appearances = List.of();

    public List<Identifier> appearances() {
        return appearances;
    }

    public boolean update(List<Identifier> next) {
        List<Identifier> resolved = List.copyOf(next);
        if (appearances.equals(resolved)) {
            return false;
        }
        appearances = resolved;
        return true;
    }

    public static final class SyncHandler implements AttachmentSyncHandler<PlayerAppearanceState> {
        @Override
        public boolean sendToPlayer(@NonNull IAttachmentHolder holder, @NonNull ServerPlayer to) {
            return true;
        }

        @Override
        public void write(RegistryFriendlyByteBuf buf, PlayerAppearanceState attachment, boolean initialSync) {
            buf.writeVarInt(attachment.appearances.size());
            for (Identifier id : attachment.appearances) {
                ByteBufHelpers.encodeIdentifier(id, buf);
            }
        }

        @Override
        public @Nullable PlayerAppearanceState read(IAttachmentHolder holder, RegistryFriendlyByteBuf buf,
                                                      @Nullable PlayerAppearanceState previousValue) {
            PlayerAppearanceState state = previousValue == null ? new PlayerAppearanceState() : previousValue;
            int size = buf.readVarInt();
            if (size > 256) {
                throw new IllegalArgumentException("Too many player appearance IDs: " + size);
            }
            List<Identifier> appearances = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                appearances.add(ByteBufHelpers.decodeIdentifier(buf));
            }
            state.update(appearances);
            return state;
        }
    }
}
