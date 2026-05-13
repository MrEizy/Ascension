package net.zic.ascension.events;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.AscensionCommonConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.zic.ascension.Config;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class PlayerEventHandler {

    private static final String RECEIVED_STARTER_KIT_TAG = AscensionCraft.MOD_ID + ":received_starter_kit";

    @SubscribeEvent
    public static void onPlayerJoinFirstTime(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        // Get the config instance
        AscensionCommonConfig config = Config.COMMON;

        // Check if starter kit is enabled
        if (!config.STARTER_KIT_ENABLED.get()) {
            return;
        }

        // Check if player already received starter kit OR if player has played before
        if (player.getPersistentData().getBoolean(RECEIVED_STARTER_KIT_TAG) ||
                player.getStats().getValue(net.minecraft.stats.Stats.CUSTOM.get(net.minecraft.stats.Stats.PLAY_TIME)) > 0) {
            return;
        }

        // Give starter kit items
        giveStarterKit(player, config);

        // Mark player as having received starter kit
        player.getPersistentData().putBoolean(RECEIVED_STARTER_KIT_TAG, true);

        AscensionCraft.LOGGER.info("Given starter kit to player: {}", player.getScoreboardName());
    }

    private static void giveStarterKit(ServerPlayer player, AscensionCommonConfig config) {
        int itemsGiven = 0;

        for (AscensionCommonConfig.StarterKitItem kitItem : config.getStarterKitItems()) {
            try {
                ResourceLocation itemId = ResourceLocation.parse(kitItem.itemId());
                ItemStack itemStack = new ItemStack(BuiltInRegistries.ITEM.get(itemId), kitItem.amount());

                if (!itemStack.isEmpty()) {
                    // Try to add to inventory, drop if full
                    if (!player.getInventory().add(itemStack)) {
                        player.drop(itemStack, false);
                    }
                    itemsGiven++;
                } else {
                    AscensionCraft.LOGGER.warn("Invalid item in starter kit: {}", kitItem.itemId());
                }
            } catch (Exception e) {
                AscensionCraft.LOGGER.error("Error giving starter kit item {}: {}", kitItem.itemId(), e.getMessage());
            }
        }

        if (itemsGiven > 0) {
            player.containerMenu.broadcastChanges();
        }
    }
}