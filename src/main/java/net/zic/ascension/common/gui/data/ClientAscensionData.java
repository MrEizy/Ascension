package net.zic.ascension.common.gui.data;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.zic.ascension.api.capabilities.AscensionEntityDataHolder;
import net.zic.ascension.api.capabilities.CoreCapabilities;
import net.zic.ascension.api.core.entity.AscensionEntityData;
import net.zic.ascension.api.core.source.OriginSource;

import java.util.Optional;


public final class ClientAscensionData {
    private ClientAscensionData() {
    }

    public static Optional<Player> getPlayer() {
        return Optional.ofNullable(Minecraft.getInstance().player);
    }

    public static Optional<AscensionEntityData> getEntityData() {
        return getPlayer().flatMap(player -> {
            AscensionEntityDataHolder holder = player.getCapability(
                    CoreCapabilities.ASCENSION_ENTITY_DATA_HOLDER_CAPABILITY
            );
            if (holder == null) {
                return Optional.empty();
            }
            return Optional.ofNullable(holder.getData(player));
        });
    }

    public static Optional<OriginSource> getSource() {
        return getEntityData().map(AscensionEntityData::getSource);
    }
}
