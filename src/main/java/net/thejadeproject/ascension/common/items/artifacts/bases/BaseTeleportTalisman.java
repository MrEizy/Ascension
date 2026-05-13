package net.thejadeproject.ascension.common.items.artifacts.bases;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.thejadeproject.ascension.common.items.data_components.ModDataComponents;

import net.thejadeproject.ascension.data_attachments.ModAttachments;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class BaseTeleportTalisman extends Item {
    protected static final int TICKS_PER_SECOND = 20;
    protected static final double MOVEMENT_THRESHOLD_SQ = 1.0 * 1.0;
    protected static final double Y_MOVEMENT_THRESHOLD = 0.5;

    public static final String TRANSLOC_COUNTDOWN = "ascension.teleport.countdown";
    public static final String TRANSLOC_CANCELLED = "ascension.teleport.cancelled";
    public static final String TRANSLOC_CANCEL_MOVE = "ascension.teleport.cancel.movement";
    public static final String TRANSLOC_CANCEL_DAMAGE = "ascension.teleport.cancel.damage";
    public static final String TRANSLOC_CANCEL_NO_ITEM = "ascension.teleport.cancel.no_item";
    public static final String TELEPORT_ACTIVE = "TeleportActive";

    // Generic tooltip key for all permanent talismans
    private static final String PERM_RECHARGE_TOOLTIP_KEY = "item.ascension.permanent_talisman.recharging";
    // Light blue color for the durability bar (RGB: 0x00AAFF)
    private static final int BAR_COLOR_LIGHT_BLUE = 0x00AAFF;

    public BaseTeleportTalisman(Properties properties) {
        super(properties.stacksTo(16));
    }

    protected abstract String getCooldownTag();
    protected abstract String getCooldownTimeTag();
    protected abstract String getCountdownTag();
    protected abstract String getInitialPosTag();
    protected abstract String getInitialHealthTag();
    protected abstract String getUsedHandTag();
    protected abstract String getUsedSlotTag();
    protected abstract int getCooldownTicks();
    protected abstract int getCountdownTicks();
    protected abstract Rarity getTalismanRarity();
    protected abstract String getDisplayNameKey();
    protected abstract void performTeleport(ServerPlayer player, ItemStack usedStack, int usedSlot);

    protected abstract int getRechargeMaxValue();
    protected abstract String getPermanentVariantId();

    protected boolean isPermanent(ItemStack stack) {
        return Boolean.TRUE.equals(stack.get(ModDataComponents.PERMANENT.get()));
    }



    // PERFECT ISOLATION: Prefix with item type first, then variant
    protected String getActualCountdownTag(ItemStack stack) {
        return getCountdownTag() + (isPermanent(stack) ? "_PERM" : "_NORM");
    }

    protected String getActualCooldownTag(ItemStack stack) {
        return getCooldownTag() + (isPermanent(stack) ? "_PERM" : "_NORM");
    }

    protected String getActualCooldownTimeTag(ItemStack stack) {
        return getCooldownTimeTag() + (isPermanent(stack) ? "_PERM" : "_NORM");
    }

    @Override
    public Component getName(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            CompoundTag tag = customData.copyTag();
            if (tag.contains("CooldownMinutes") && tag.contains("CooldownSeconds")) {
                int minutes = tag.getInt("CooldownMinutes");
                int seconds = tag.getInt("CooldownSeconds");
                return Component.translatable(getDisplayNameKey() + ".cooldown", minutes, seconds);
            }
        }

        if (isPermanent(stack)) {
            return Component.translatable(getDisplayNameKey() + ".permanent");
        }

        return Component.translatable(getDisplayNameKey());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        // Show recharging status in tooltip for all permanent talismans
        if (isPermanent(stack)) {
            Integer progress = stack.get(ModDataComponents.RECHARGE_PROGRESS.get());
            if (progress != null && progress > 0) {
                int maxRecharge = getRechargeMaxValue();
                int chargedPercent = (int)(100.0 * (maxRecharge - progress) / maxRecharge);
                tooltipComponents.add(Component.translatable(
                        PERM_RECHARGE_TOOLTIP_KEY,
                        chargedPercent, 100
                ).withStyle(ChatFormatting.BLUE));
            }
        }
    }

    // Custom durability bar for recharge progress
    @Override
    public boolean isBarVisible(ItemStack stack) {
        if (!isPermanent(stack)) return false;
        Integer progress = stack.get(ModDataComponents.RECHARGE_PROGRESS.get());
        return progress != null && progress > 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        if (!isPermanent(stack)) {
            return 0;
        }

        Integer progress = stack.get(ModDataComponents.RECHARGE_PROGRESS.get());
        if (progress == null || progress == 0) {
            return 13;
        }

        int maxRecharge = getRechargeMaxValue();
        int charged = maxRecharge - progress;
        return (int)(13.0 * charged / maxRecharge);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        // Return light blue color for all permanent talismans
        return BAR_COLOR_LIGHT_BLUE;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (isItemOnCooldown(player, stack)) {
            return InteractionResultHolder.fail(stack);
        }

        if (level.isClientSide) {
            level.playSound(player, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT,
                    SoundSource.PLAYERS, 1.0F, 1.0F);
            return InteractionResultHolder.consume(stack);
        }

        ServerPlayer serverPlayer = (ServerPlayer) player;
        int slot = (hand == InteractionHand.MAIN_HAND) ?
                serverPlayer.getInventory().selected : 40;

        startCountdown(serverPlayer, hand, slot, stack);
        playDepartureEffects(serverPlayer);

        return InteractionResultHolder.success(stack);
    }

    protected void startCountdown(ServerPlayer player, InteractionHand hand, int slot, ItemStack usedStack) {
        String countdownTag = getActualCountdownTag(usedStack);

        CompoundTag data = player.getPersistentData();
        data.putInt(countdownTag, getCountdownTicks());
        data.putBoolean(TELEPORT_ACTIVE, true);

        CompoundTag posTag = new CompoundTag();
        posTag.putDouble("x", player.getX());
        posTag.putDouble("y", player.getY());
        posTag.putDouble("z", player.getZ());
        posTag.putBoolean("onGround", player.onGround());
        data.put(getInitialPosTag(), posTag);

        data.putFloat(getInitialHealthTag(), player.getHealth());
        data.putString(getUsedHandTag(), hand.name());
        data.putInt(getUsedSlotTag(), slot);

        player.displayClientMessage(
                Component.translatable(TRANSLOC_COUNTDOWN, getCountdownTicks() / TICKS_PER_SECOND),
                true
        );
    }

    protected void playDepartureEffects(ServerPlayer player) {
        ServerLevel level = (ServerLevel) player.level();
        level.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT,
                SoundSource.PLAYERS, 1.0F, 1.0F);
        level.sendParticles(ParticleTypes.PORTAL,
                player.getX(), player.getY() + 1, player.getZ(),
                50, 0.5, 1, 0.5, 0.1);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!level.isClientSide && entity instanceof ServerPlayer player) {
            handleCountdown(player, getActualCountdownTag(stack));

            if (isPermanent(stack)) {
                if (player.tickCount % 40 == 0) {
                    handleRechargeTick(player, stack);
                }
            } else {
                updateCooldownDisplay(stack, player);
            }
        }
        super.inventoryTick(stack, level, entity, slotId, isSelected);
    }

    // FIXED: PERMANENT ITEMS USE RECHARGE ONLY
    protected boolean isItemOnCooldown(Player player, ItemStack stack) {
        if (isPermanent(stack)) {
            Integer progress = stack.get(ModDataComponents.RECHARGE_PROGRESS.get());
            return progress != null && progress > 0;
        }

        // Normal items: check their isolated cooldown
        return player instanceof ServerPlayer serverPlayer &&
                hasNormalCooldownActive(serverPlayer, stack);
    }

    // FIXED: ABSOLUTE SLOT CONSUMPTION - NO FALLBACK
    protected void consumeItem(ServerPlayer player, ItemStack usedStack, int usedSlot) {
        if (isPermanent(usedStack)) {
            usedStack.set(ModDataComponents.RECHARGE_PROGRESS.get(), getRechargeMaxValue());
            return;
        }

        if (player.getAbilities().instabuild) {
            return;
        }

        // Get the exact slot that was stored at teleport start
        ItemStack slotStack = player.getInventory().getItem(usedSlot);

        // Verify it matches the expected item type AND permanent status
        if (slotStack.getItem() == this && !isPermanent(slotStack)) {
            slotStack.shrink(1);
            return;
        }

        // If exact slot fails, search entire inventory for a matching normal item
        // This is a safety net but should never trigger if slot tracking is correct
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack invStack = player.getInventory().getItem(i);
            if (invStack.getItem() == this && !isPermanent(invStack)) {
                invStack.shrink(1);
                return;
            }
        }
    }

    protected void handleRechargeTick(ServerPlayer player, ItemStack stack) {
        Integer progress = stack.get(ModDataComponents.RECHARGE_PROGRESS.get());
        if (progress == null || progress <= 0) return;

        IEntityData entityData = player.getData(ModAttachments.ENTITY_DATA);
        if (entityData == null) return;
        if (!entityData.getQiContainer().tryConsumeQi(3)) return;

        stack.set(ModDataComponents.RECHARGE_PROGRESS.get(), progress - 1);
    }

    protected void updateCooldownDisplay(ItemStack stack, ServerPlayer player) {
        int remainingTicks = getRemainingCooldownTicks(player, stack);
        if (remainingTicks > 0) {
            int totalSeconds = remainingTicks / TICKS_PER_SECOND;
            int minutes = totalSeconds / 60;
            int seconds = totalSeconds % 60;

            CompoundTag tag = new CompoundTag();
            tag.putInt("CooldownMinutes", minutes);
            tag.putInt("CooldownSeconds", seconds);

            preserveCustomData(stack, tag);

            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            player.getCooldowns().addCooldown(this, Math.min(remainingTicks, 200));
        } else {
            clearCooldownDisplayOnly(stack);

            if (player.getCooldowns().isOnCooldown(this)) {
                player.getCooldowns().removeCooldown(this);
            }
        }
    }

    protected void preserveCustomData(ItemStack stack, CompoundTag cooldownTag) {}

    protected void clearCooldownDisplayOnly(ItemStack stack) {
        stack.remove(DataComponents.CUSTOM_DATA);
    }

    protected void handleCountdown(ServerPlayer player, String countdownTag) {
        CompoundTag data = player.getPersistentData();
        if (!data.contains(countdownTag)) {
            return;
        }

        int countdown = data.getInt(countdownTag);
        if (countdown <= 0) {
            return;
        }

        String cancelReason = getCancellationReason(player, countdownTag);
        if (cancelReason != null) {
            cancelTeleport(player, cancelReason, countdownTag);
            return;
        }

        countdown--;
        data.putInt(countdownTag, countdown);

        if (countdown % TICKS_PER_SECOND == 0) {
            int seconds = countdown / TICKS_PER_SECOND;
            player.displayClientMessage(
                    Component.translatable(TRANSLOC_COUNTDOWN, seconds),
                    true
            );
        }

        if (countdown == 0) {
            executeTeleport(player, countdownTag);
        }
    }

    @Nullable
    protected String getCancellationReason(ServerPlayer player, String countdownTag) {
        CompoundTag data = player.getPersistentData();

        if (!hasCorrectItemInSlot(player, countdownTag)) {
            return TRANSLOC_CANCEL_NO_ITEM;
        }

        if (hasSignificantMovement(player)) {
            return TRANSLOC_CANCEL_MOVE;
        }

        if (data.contains(getInitialHealthTag())) {
            float initialHealth = data.getFloat(getInitialHealthTag());
            if (player.getHealth() < initialHealth - 0.001F) {
                return TRANSLOC_CANCEL_DAMAGE;
            }
        }

        return null;
    }

    protected boolean hasCorrectItemInSlot(ServerPlayer player, String countdownTag) {
        CompoundTag data = player.getPersistentData();
        if (!data.contains(getUsedHandTag()) || !data.contains(getUsedSlotTag())) {
            return false;
        }

        String handName = data.getString(getUsedHandTag());
        int expectedSlot = data.getInt(getUsedSlotTag());

        InteractionHand hand = InteractionHand.valueOf(handName);
        ItemStack itemInHand = player.getItemInHand(hand);

        if (itemInHand.getItem() != this) {
            return false;
        }

        boolean isCountdownForPermanent = countdownTag.endsWith("_PERM");
        boolean isItemPermanent = isPermanent(itemInHand);

        if (isCountdownForPermanent != isItemPermanent) {
            return false;
        }

        if (hand == InteractionHand.MAIN_HAND) {
            return player.getInventory().selected == expectedSlot;
        }

        return true;
    }

    protected boolean hasSignificantMovement(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        if (!data.contains(getInitialPosTag())) {
            return false;
        }

        CompoundTag posTag = data.getCompound(getInitialPosTag());
        double x0 = posTag.getDouble("x");
        double y0 = posTag.getDouble("y");
        double z0 = posTag.getDouble("z");
        boolean wasOnGround = posTag.getBoolean("onGround");

        double dx = player.getX() - x0;
        double dy = player.getY() - y0;
        double dz = player.getZ() - z0;

        if (dx * dx + dz * dz > MOVEMENT_THRESHOLD_SQ) {
            return true;
        }

        if (Math.abs(dy) > Y_MOVEMENT_THRESHOLD) {
            if (!wasOnGround || Math.abs(dy) > 1.0) {
                return true;
            }
        }

        return player.getVehicle() != null;
    }

    protected void executeTeleport(ServerPlayer player, String countdownTag) {
        CompoundTag data = player.getPersistentData();

        String handName = data.getString(getUsedHandTag());
        int slot = data.getInt(getUsedSlotTag());

        InteractionHand hand = InteractionHand.valueOf(handName);
        ItemStack usedStack = player.getItemInHand(hand);

        if (usedStack.getItem() != this) {
            cancelTeleport(player, TRANSLOC_CANCEL_NO_ITEM, countdownTag);
            return;
        }

        boolean isCountdownForPermanent = countdownTag.endsWith("_PERM");
        if (isPermanent(usedStack) != isCountdownForPermanent) {
            cancelTeleport(player, TRANSLOC_CANCEL_NO_ITEM, countdownTag);
            return;
        }

        performTeleport(player, usedStack, slot);
    }

    protected void cancelTeleport(ServerPlayer player, String reasonKey, String countdownTag) {
        player.displayClientMessage(
                Component.translatable(TRANSLOC_CANCELLED, Component.translatable(reasonKey)),
                true
        );
        clearCountdownData(player, countdownTag);
    }

    protected void clearCountdownData(ServerPlayer player, String countdownTag) {
        CompoundTag data = player.getPersistentData();
        data.remove(countdownTag);
        data.remove(TELEPORT_ACTIVE);
        data.remove(getInitialPosTag());
        data.remove(getInitialHealthTag());
        data.remove(getUsedHandTag());
        data.remove(getUsedSlotTag());
    }

    private boolean hasNormalCooldownActive(ServerPlayer player, ItemStack stack) {
        if (isPermanent(stack)) {
            return false; // Extra guard
        }

        CompoundTag data = player.getPersistentData();
        String cooldownTag = getActualCooldownTag(stack);
        String timeTag = getActualCooldownTimeTag(stack);

        if (!data.contains(cooldownTag) || !data.contains(timeTag)) {
            return false;
        }

        int remaining = data.getInt(cooldownTag);
        long lastUpdate = data.getLong(timeTag);
        long elapsedTicks = (System.currentTimeMillis() - lastUpdate) / 50;

        return Math.max(0, remaining - (int) elapsedTicks) > 0;
    }

    protected boolean isOnCooldown(Player player, ItemStack stack) {
        if (player.level().isClientSide || !(player instanceof ServerPlayer serverPlayer)) {
            return false;
        }

        if (isPermanent(stack)) {
            return false;
        }

        return hasNormalCooldownActive(serverPlayer, stack);
    }

    protected int getRemainingCooldownTicks(Player player, ItemStack stack) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return 0;
        }

        if (isPermanent(stack)) {
            return 0;
        }

        return getRemainingCooldownTime(serverPlayer, stack);
    }

    private int getRemainingCooldownTime(ServerPlayer player, ItemStack stack) {
        if (isPermanent(stack)) return 0; // Triple guard

        CompoundTag data = player.getPersistentData();
        String cooldownTag = getActualCooldownTag(stack);
        String timeTag = getActualCooldownTimeTag(stack);

        if (!data.contains(cooldownTag) || !data.contains(timeTag)) {
            return 0;
        }

        int remaining = data.getInt(cooldownTag);
        long lastUpdate = data.getLong(timeTag);
        long elapsedTicks = (System.currentTimeMillis() - lastUpdate) / 50;

        return Math.max(0, remaining - (int) elapsedTicks);
    }

    protected void startCooldown(ServerPlayer player, ItemStack stack) {
        if (isPermanent(stack)) return; // Triple guard

        CompoundTag data = player.getPersistentData();
        data.putInt(getActualCooldownTag(stack), getCooldownTicks());
        data.putLong(getActualCooldownTimeTag(stack), System.currentTimeMillis());
    }

    protected void clearCooldown(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        data.remove(getCooldownTag());
        data.remove(getCooldownTimeTag());
    }

    protected void playArrivalEffects(ServerPlayer player) {
        ServerLevel level = (ServerLevel) player.level();
        BlockPos pos = player.blockPosition();
        level.playSound(null, pos, SoundEvents.ENDERMAN_TELEPORT,
                SoundSource.PLAYERS, 1.0F, 1.0F);
        level.sendParticles(ParticleTypes.PORTAL,
                pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5,
                50, 0.5, 1, 0.5, 0.1);
    }

    protected boolean isPlayerValid(ServerPlayer player) {
        return player.isAlive() && !player.hasDisconnected() && player.getServer() != null;
    }

    protected void finalizeTeleport(ServerPlayer player, ItemStack usedStack, int usedSlot,
                                    boolean success, boolean consumeOnFailure, String successMessageKey) {
        if (!isPlayerValid(player)) {
            clearCountdownData(player, getActualCountdownTag(usedStack));
            return;
        }

        if (success) {
            playArrivalEffects(player);
            if (!isPermanent(usedStack)) {
                startCooldown(player, usedStack);
            }
            consumeItem(player, usedStack, usedSlot);
            player.displayClientMessage(Component.translatable(successMessageKey), true);
        } else {
            if (consumeOnFailure) {
                consumeItem(player, usedStack, usedSlot);
            }
            player.displayClientMessage(
                    Component.translatable("ascension.teleport.failed.no_safe_location"),
                    true
            );
        }

        clearCountdownData(player, getActualCountdownTag(usedStack));
    }
}