package net.zic.ascension.common.gui.data;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.zic.ascension.api.capabilities.AscensionEntityDataHolder;
import net.zic.ascension.api.capabilities.CoreCapabilities;
import net.zic.ascension.api.capabilities.EntityQiProvider;
import net.zic.ascension.api.core.entity.AscensionEntityData;
import net.zic.ascension.api.core.source.OriginSource;
import net.zic.ascension.api.core.resource.stamina.StaminaService;
import net.zic.ascension.common.data_attachements.AscensionAttachments;
import net.zic.ascension.skill_casting.SkillCastHandler;

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

    public static Optional<SkillCastHandler> getSkillCastHandler() {
        return getPlayer().map(player -> player.getData(
                AscensionAttachments.ASCENSION_SKILL_CAST_HANDLER
        ));
    }
    public static double getQi(){

        if(getPlayer().isEmpty()) return 0;

        Player player = getPlayer().get();

        EntityQiProvider provider = player.getCapability(CoreCapabilities.ASCENSION_ENTITY_QI_PROVIDER);
        if(provider == null) return 0;
        return provider.getQi();

    }
    public static double getMaxQi(){

        if(getPlayer().isEmpty()) return 0;

        Player player = getPlayer().get();

        EntityQiProvider provider = player.getCapability(CoreCapabilities.ASCENSION_ENTITY_QI_PROVIDER);
        if(provider == null) return 0;
        return provider.getMaxQi();
    }



    public static double getStamina() {
        return getPlayer().map(StaminaService::getStamina).orElse(0.0D);
    }

    public static double getMaximumStamina() {
        return getPlayer().map(StaminaService::getMaximumStamina).orElse(0.0D);
    }

    public static long getRevision() {
        return getSource().map(OriginSource::getRevision).orElse(-1L);
    }
}
