package net.thejadeproject.ascension.common.items.artifacts.talismans;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.thejadeproject.ascension.common.items.artifacts.bases.BaseTeleportTalisman;

public class SoulsteadReturnTalisman extends BaseTeleportTalisman {
    private static final int COOLDOWN_TICKS = 5 * 60 * 20; // 5 minutes
    private static final int RECHARGE_MAX_VALUE = 50;

    public SoulsteadReturnTalisman(Properties properties) {
        super(properties.rarity(Rarity.UNCOMMON));
    }

    @Override protected String getCooldownTag() { return "SoulsteadReturn_Cooldown"; }
    @Override protected String getCooldownTimeTag() { return "SoulsteadReturn_CooldownTime"; }
    @Override protected String getCountdownTag() { return "SoulsteadReturn_Countdown"; }
    @Override protected String getInitialPosTag() { return "SoulsteadReturn_InitialPos"; }
    @Override protected String getInitialHealthTag() { return "SoulsteadReturn_InitialHealth"; }
    @Override protected String getUsedHandTag() { return "SoulsteadReturn_UsedHand"; }
    @Override protected String getUsedSlotTag() { return "SoulsteadReturn_UsedSlot"; }

    @Override protected int getCooldownTicks() { return COOLDOWN_TICKS; }
    @Override protected int getCountdownTicks() { return 5 * TICKS_PER_SECOND; }
    @Override protected Rarity getTalismanRarity() { return Rarity.UNCOMMON; }
    @Override protected String getDisplayNameKey() { return "item.ascension.soulstead_return_talisman"; }

    // NEW: Permanent item methods
    @Override protected int getRechargeMaxValue() { return RECHARGE_MAX_VALUE; }
    @Override protected String getPermanentVariantId() { return "permanent_soulstead_return"; }

    @Override
    protected void performTeleport(ServerPlayer player, ItemStack usedStack, int usedSlot) {
        ResourceKey<Level> respawnDim = player.getRespawnDimension();
        ServerLevel targetLevel = player.getServer().getLevel(respawnDim);
        if (targetLevel == null) {
            targetLevel = player.getServer().overworld();
        }

        BlockPos respawnPos = player.getRespawnPosition();
        if (respawnPos == null) {
            respawnPos = targetLevel.getSharedSpawnPos();
        }

        double x = respawnPos.getX() + 0.5;
        double y = respawnPos.getY() + 0.5;
        double z = respawnPos.getZ() + 0.5;

        BlockPos finalPos = new BlockPos((int) x, (int) y, (int) z);
        while (!targetLevel.isEmptyBlock(finalPos)) {
            finalPos = finalPos.above();
            if (finalPos.getY() > targetLevel.getMaxBuildHeight()) break;
        }
        y = Math.min(y, finalPos.getY());

        player.teleportTo(targetLevel, x, y, z, player.getYRot(), player.getXRot());

        finalizeTeleport(player, usedStack, usedSlot, true, false, "ascension.soulsteadreturn.teleported");
    }
}