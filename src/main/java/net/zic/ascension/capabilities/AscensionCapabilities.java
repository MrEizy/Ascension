package net.zic.ascension.capabilities;

import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.capabilities.CoreCapabilities;
import net.zic.ascension.capabilities.entity_holder.PlayerDataHolder;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public class AscensionCapabilities {

    @SubscribeEvent // on the mod event bus
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerEntity(
                CoreCapabilities.ASCENSION_ENTITY_DATA_HOLDER_CAPABILITY,
                        EntityType.PLAYER,
                        (player,nul)->new PlayerDataHolder()
        );
    }
}
