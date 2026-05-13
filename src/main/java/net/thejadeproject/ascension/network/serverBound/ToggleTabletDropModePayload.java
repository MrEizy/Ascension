package net.thejadeproject.ascension.network.serverBound;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.common.items.artifacts.TabletOfDestructionHeaven;
import net.thejadeproject.ascension.common.items.artifacts.TabletOfDestructionEarth;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public record ToggleTabletDropModePayload() implements CustomPacketPayload {
    public static final Type<ToggleTabletDropModePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "toggle_tablet_drop_mode"));

    // Internal cooldown tracking per player - 1 second (20 ticks) cooldown
    private static final Map<UUID, Long> lastToggleTime = new HashMap<>();
    private static final long COOLDOWN_TICKS = 20;

    public static final StreamCodec<FriendlyByteBuf, ToggleTabletDropModePayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {}, // No data to write
            buf -> new ToggleTabletDropModePayload()
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handlePayload(ToggleTabletDropModePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                UUID playerId = player.getUUID();
                long currentTime = player.level().getGameTime();

                // Check cooldown
                if (lastToggleTime.containsKey(playerId)) {
                    long lastToggle = lastToggleTime.get(playerId);
                    if (currentTime - lastToggle < COOLDOWN_TICKS) {
                        // Still on cooldown, ignore this toggle
                        return;
                    }
                }

                // Update last toggle time
                lastToggleTime.put(playerId, currentTime);

                // Check both hands for tablets
                ItemStack mainHand = player.getMainHandItem();
                ItemStack offHand = player.getOffhandItem();

                boolean toggled = false;

                if (mainHand.getItem() instanceof TabletOfDestructionHeaven || mainHand.getItem() instanceof TabletOfDestructionEarth) {
                    toggleDropMode(mainHand, player);
                    toggled = true;
                }

                if (!toggled && (offHand.getItem() instanceof TabletOfDestructionHeaven || offHand.getItem() instanceof TabletOfDestructionEarth)) {
                    toggleDropMode(offHand, player);
                }
            }
        });
    }

    private static void toggleDropMode(ItemStack stack, ServerPlayer player) {
        if (stack.getItem() instanceof TabletOfDestructionHeaven) {
            TabletOfDestructionHeaven.toggleDropModeServer(stack, player);
        } else if (stack.getItem() instanceof TabletOfDestructionEarth) {
            TabletOfDestructionEarth.toggleDropModeServer(stack, player);
        }
    }
}