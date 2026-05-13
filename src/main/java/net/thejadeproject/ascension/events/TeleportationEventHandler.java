// TeleportationEventHandler.java
package net.thejadeproject.ascension.events;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.thejadeproject.ascension.common.items.artifacts.bases.BaseTeleportTalisman;

public class TeleportationEventHandler {

    @SubscribeEvent
    public static void onPlayerHurt(LivingIncomingDamageEvent event) {
        // Only handle real damage to players
        if (event.getAmount() <= 0 || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        CompoundTag data = player.getPersistentData();

        // If any teleport is active, cancel it
        if (data.getBoolean(BaseTeleportTalisman.TELEPORT_ACTIVE)) {
            for (String key : data.getAllKeys()) {
                if (key.endsWith("Countdown") && data.getInt(key) > 0) {

                    // Extract talisman prefix
                    String prefix = key.substring(0, key.length() - "Countdown".length());

                    // Clear teleport data
                    data.remove(key);
                    data.remove(BaseTeleportTalisman.TELEPORT_ACTIVE);
                    data.remove(prefix + "InitialPos");
                    data.remove(prefix + "InitialHealth");
                    data.remove(prefix + "UsedHand");
                    data.remove(prefix + "UsedSlot");

                    // Notify player
                    player.displayClientMessage(
                            Component.translatable(
                                    BaseTeleportTalisman.TRANSLOC_CANCELLED,
                                    Component.translatable(BaseTeleportTalisman.TRANSLOC_CANCEL_DAMAGE)
                            ),
                            true
                    );
                    break;
                }
            }
        }
    }
}
