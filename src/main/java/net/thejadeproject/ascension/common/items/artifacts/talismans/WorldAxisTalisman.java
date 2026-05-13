package net.thejadeproject.ascension.common.items.artifacts.talismans;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.thejadeproject.ascension.common.items.artifacts.bases.BaseTeleportTalisman;

public class WorldAxisTalisman extends BaseTeleportTalisman {
    private static final int COOLDOWN_TICKS = 5 * 60 * 20; // 5 minutes
    private static final int RECHARGE_MAX_VALUE = 50;

    public WorldAxisTalisman(Properties properties) {
        super(properties.rarity(Rarity.RARE));
    }

    @Override protected String getCooldownTag() { return "WorldAxis_Cooldown"; }
    @Override protected String getCooldownTimeTag() { return "WorldAxis_CooldownTime"; }
    @Override protected String getCountdownTag() { return "WorldAxis_Countdown"; }
    @Override protected String getInitialPosTag() { return "WorldAxis_InitialPos"; }
    @Override protected String getInitialHealthTag() { return "WorldAxis_InitialHealth"; }
    @Override protected String getUsedHandTag() { return "WorldAxis_UsedHand"; }
    @Override protected String getUsedSlotTag() { return "WorldAxis_UsedSlot"; }

    @Override protected int getCooldownTicks() { return COOLDOWN_TICKS; }
    @Override protected int getCountdownTicks() { return 5 * TICKS_PER_SECOND; }
    @Override protected Rarity getTalismanRarity() { return Rarity.UNCOMMON; }
    @Override protected String getDisplayNameKey() { return "item.ascension.world_axis_talisman"; }

    // NEW: Permanent item methods
    @Override protected int getRechargeMaxValue() { return RECHARGE_MAX_VALUE; }
    @Override protected String getPermanentVariantId() { return "permanent_world_axis"; }

    @Override
    protected void performTeleport(ServerPlayer player, ItemStack usedStack, int usedSlot) {
        ServerLevel targetLevel = player.getServer().overworld();
        if (targetLevel == null) {
            cancelTeleport(player, TRANSLOC_CANCEL_NO_ITEM, getActualCountdownTag(usedStack)); // FIXED
            return;
        }

        BlockPos spawnPos = targetLevel.getSharedSpawnPos();
        double x = spawnPos.getX() + 0.5;
        double y = spawnPos.getY();
        double z = spawnPos.getZ() + 0.5;

        player.teleportTo(targetLevel, x, y, z, player.getYRot(), player.getXRot());

        finalizeTeleport(player, usedStack, usedSlot, true, false, "ascension.worldaxis.teleported");
    }
}