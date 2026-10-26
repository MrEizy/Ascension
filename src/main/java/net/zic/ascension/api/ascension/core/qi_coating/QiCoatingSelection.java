package net.zic.ascension.api.ascension.core.qi_coating;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.attachment.AttachmentSyncHandler;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

/**
 * The element (a QiCoatingElement id) the player last picked for each Qi Coating channel. Kept through death and relogging, and
 * synced to the player themselves so their coating wheel can show the current pick.
 */
public class QiCoatingSelection {
    public static final Codec<QiCoatingSelection> CODEC = Codec.unboundedMap(QiCoatingChannel.CODEC, Identifier.CODEC)
            .xmap(QiCoatingSelection::new, selection -> selection.selected);

    private final Map<QiCoatingChannel, Identifier> selected = new EnumMap<>(QiCoatingChannel.class);

    public QiCoatingSelection() {
    }

    private QiCoatingSelection(Map<QiCoatingChannel, Identifier> selected) {
        this.selected.putAll(selected);
    }

    public Optional<Identifier> get(QiCoatingChannel channel) {
        return Optional.ofNullable(selected.get(channel));
    }

    public void set(QiCoatingChannel channel, Identifier element) {
        selected.put(channel, element);
    }

    public static class SyncHandler implements AttachmentSyncHandler<QiCoatingSelection> {
        @Override
        public void write(@NonNull RegistryFriendlyByteBuf buf, QiCoatingSelection attachment, boolean initialSync) {
            buf.writeVarInt(attachment.selected.size());
            attachment.selected.forEach((channel, element) -> {
                QiCoatingChannel.STREAM_CODEC.encode(buf, channel);
                buf.writeIdentifier(element);
            });
        }

        @Override
        public @Nullable QiCoatingSelection read(@NonNull IAttachmentHolder holder, @NonNull RegistryFriendlyByteBuf buf, @Nullable QiCoatingSelection previousValue) {
            QiCoatingSelection selection = new QiCoatingSelection();
            int size = buf.readVarInt();
            for (int i = 0; i < size; i++) {
                selection.selected.put(QiCoatingChannel.STREAM_CODEC.decode(buf), buf.readIdentifier());
            }
            return selection;
        }

        // Only the player themselves needs their pick.
        @Override
        public boolean sendToPlayer(@NonNull IAttachmentHolder holder, @NonNull ServerPlayer to) {
            return holder == to;
        }
    }
}
