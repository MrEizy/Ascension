package net.thejadeproject.ascension.common.items.artifacts.talismans;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.thejadeproject.ascension.events.api.SpatialRuptureAPI;
import net.thejadeproject.ascension.common.items.artifacts.bases.BaseTeleportTalisman;

import java.util.concurrent.CompletableFuture;

public class SpatialRuptureTalismanT1 extends BaseTeleportTalisman {
    private static final int TELEPORT_RADIUS = 2500;
    private static final int COOLDOWN_TICKS = 60 * 60 * 20; // 60 minutes
    private static final int RECHARGE_MAX_VALUE = 200;

    public SpatialRuptureTalismanT1(Properties properties) {
        super(properties.rarity(Rarity.UNCOMMON));
    }

    @Override protected String getCooldownTag() { return "SpatialRuptureT1_Cooldown"; }
    @Override protected String getCooldownTimeTag() { return "SpatialRuptureT1_CooldownTime"; }
    @Override protected String getCountdownTag() { return "SpatialRuptureT1_Countdown"; }
    @Override protected String getInitialPosTag() { return "SpatialRuptureT1_InitialPos"; }
    @Override protected String getInitialHealthTag() { return "SpatialRuptureT1_InitialHealth"; }
    @Override protected String getUsedHandTag() { return "SpatialRuptureT1_UsedHand"; }
    @Override protected String getUsedSlotTag() { return "SpatialRuptureT1_UsedSlot"; }

    @Override protected int getCooldownTicks() { return COOLDOWN_TICKS; }
    @Override protected int getCountdownTicks() { return 5 * TICKS_PER_SECOND; }
    @Override protected Rarity getTalismanRarity() { return Rarity.UNCOMMON; }
    @Override protected String getDisplayNameKey() { return "item.ascension.spatial_rupture_talisman_t1"; }

    // NEW: Permanent item methods
    @Override protected int getRechargeMaxValue() { return RECHARGE_MAX_VALUE; }
    @Override protected String getPermanentVariantId() { return "permanent_spatial_rupture_t1"; }

    @Override
    protected void performTeleport(ServerPlayer player, ItemStack usedStack, int usedSlot) {
        ServerLevel level = (ServerLevel) player.level();

        CompletableFuture<Boolean> future = SpatialRuptureAPI.randomTeleport(player, level, TELEPORT_RADIUS);

        future.thenAccept(success -> {
            level.getServer().execute(() -> {
                finalizeTeleport(player, usedStack, usedSlot, success, true, "ascension.teleport.success.random");
            });
        });
    }
}