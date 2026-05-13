package net.thejadeproject.ascension.common.items.artifacts.talismans;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.thejadeproject.ascension.common.items.artifacts.bases.BaseTeleportTalisman;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DeathRecallTalisman extends BaseTeleportTalisman {
    private static final int COOLDOWN_TICKS = 30 * 20; // 30 seconds for normal version
    private static final int RECHARGE_MAX_VALUE = 100; // Qi cost for permanent recharge

    private static final String DEATH_X = "death_x";
    private static final String DEATH_Y = "death_y";
    private static final String DEATH_Z = "death_z";
    private static final String DEATH_DIMENSION = "death_dimension";

    public DeathRecallTalisman(Properties properties) {
        super(properties.rarity(Rarity.EPIC));
    }

    @Override protected String getCooldownTag() { return "DeathRecall_Cooldown"; }
    @Override protected String getCooldownTimeTag() { return "DeathRecall_CooldownTime"; }
    @Override protected String getCountdownTag() { return "DeathRecall_Countdown"; }
    @Override protected String getInitialPosTag() { return "DeathRecall_InitialPos"; }
    @Override protected String getInitialHealthTag() { return "DeathRecall_InitialHealth"; }
    @Override protected String getUsedHandTag() { return "DeathRecall_UsedHand"; }
    @Override protected String getUsedSlotTag() { return "DeathRecall_UsedSlot"; }

    @Override protected int getCooldownTicks() { return COOLDOWN_TICKS; }
    @Override protected int getCountdownTicks() { return 5 * TICKS_PER_SECOND; }
    @Override protected Rarity getTalismanRarity() { return Rarity.EPIC; }
    @Override protected String getDisplayNameKey() { return "item.ascension.death_recall_talisman"; }
    @Override protected int getRechargeMaxValue() { return RECHARGE_MAX_VALUE; }
    @Override protected String getPermanentVariantId() { return "permanent_death_recall"; }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // Must have a death location bound to use
        if (!hasDeathLocation(stack)) {
            if (!level.isClientSide) {
                player.displayClientMessage(
                        Component.translatable("ascension.deathrecall.no_location"),
                        true
                );
            }
            return InteractionResultHolder.fail(stack);
        }

        return super.use(level, player, hand);
    }

    @Override
    protected void performTeleport(ServerPlayer player, ItemStack usedStack, int usedSlot) {
        DeathLocation location = getDeathLocation(usedStack);

        if (location == null) {
            player.displayClientMessage(
                    Component.translatable("ascension.deathrecall.no_location"),
                    true
            );
            clearCountdownData(player, getActualCountdownTag(usedStack));
            return;
        }

        ServerLevel currentLevel = (ServerLevel) player.level();
        ResourceKey<Level> dimensionKey = ResourceKey.create(Registries.DIMENSION, location.dimension());
        ServerLevel targetLevel = currentLevel.getServer().getLevel(dimensionKey);

        if (targetLevel == null) {
            player.displayClientMessage(
                    Component.translatable("ascension.deathrecall.dimension_invalid"),
                    true
            );
            clearCountdownData(player, getActualCountdownTag(usedStack));
            return;
        }

        // Teleport to death location (+1 Y was added when saving)
        player.teleportTo(targetLevel, location.x(), location.y(), location.z(),
                player.getYRot(), player.getXRot());

        finalizeTeleport(player, usedStack, usedSlot, true, false, "ascension.deathrecall.success");

        // Clear death location after successful teleport (must die again to rebind)
        // For normal items this is moot (consumed), for permanent this prevents reuse
        if (isPermanent(usedStack)) {
            clearDeathLocation(usedStack);
        }
    }

    /**
     * Call this from your LivingDeathEvent or PlayerCloneEvent when the player dies.
     * This will bind the current death coordinates to the talisman.
     * For permanent versions: overwrites previous coords every death (as requested)
     */
    public void onPlayerDeath(ServerPlayer player, ItemStack stack) {
        double x = player.getX();
        double y = player.getY() + 1.0; // +1 Y level as requested
        double z = player.getZ();
        ResourceLocation dimension = player.level().dimension().location();

        saveDeathLocation(stack, x, y, z, dimension);
    }

    private void saveDeathLocation(ItemStack stack, double x, double y, double z, ResourceLocation dimension) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putDouble(DEATH_X, x);
        tag.putDouble(DEATH_Y, y);
        tag.putDouble(DEATH_Z, z);
        tag.putString(DEATH_DIMENSION, dimension.toString());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    private void clearDeathLocation(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data != null) {
            CompoundTag tag = data.copyTag();
            tag.remove(DEATH_X);
            tag.remove(DEATH_Y);
            tag.remove(DEATH_Z);
            tag.remove(DEATH_DIMENSION);

            if (tag.isEmpty()) {
                stack.remove(DataComponents.CUSTOM_DATA);
            } else {
                stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            }
        }
    }

    private boolean hasDeathLocation(ItemStack stack) {
        return getDeathLocation(stack) != null;
    }

    private @Nullable DeathLocation getDeathLocation(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return null;

        CompoundTag tag = data.copyTag();
        if (!tag.contains(DEATH_X) || !tag.contains(DEATH_DIMENSION)) {
            return null;
        }

        return new DeathLocation(
                tag.getDouble(DEATH_X),
                tag.getDouble(DEATH_Y),
                tag.getDouble(DEATH_Z),
                ResourceLocation.parse(tag.getString(DEATH_DIMENSION))
        );
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        DeathLocation location = getDeathLocation(stack);
        if (location != null) {
            tooltipComponents.add(Component.translatable("ascension.deathrecall.tooltip.bound")
                    .withStyle(ChatFormatting.RED));
            tooltipComponents.add(Component.translatable("ascension.deathrecall.tooltip.coords",
                    String.format("%.1f", location.x()),
                    String.format("%.1f", location.y()),
                    String.format("%.1f", location.z())));
            tooltipComponents.add(Component.translatable("ascension.deathrecall.tooltip.dimension",
                    location.dimension().getPath()));
        } else {
            tooltipComponents.add(Component.translatable("ascension.deathrecall.tooltip.unbound")
                    .withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        // Glow when bound to a death location
        return hasDeathLocation(stack) || super.isFoil(stack);
    }

    @Override
    protected void preserveCustomData(ItemStack stack, CompoundTag cooldownTag) {
        // Preserve death location when cooldown display updates
        DeathLocation location = getDeathLocation(stack);
        if (location != null) {
            cooldownTag.putDouble(DEATH_X, location.x());
            cooldownTag.putDouble(DEATH_Y, location.y());
            cooldownTag.putDouble(DEATH_Z, location.z());
            cooldownTag.putString(DEATH_DIMENSION, location.dimension().toString());
        }
    }

    @Override
    protected void clearCooldownDisplayOnly(ItemStack stack) {
        // Restore death location after clearing cooldown display
        DeathLocation location = getDeathLocation(stack);
        super.clearCooldownDisplayOnly(stack);
        if (location != null) {
            saveDeathLocation(stack, location.x(), location.y(), location.z(), location.dimension());
        }
    }

    private record DeathLocation(double x, double y, double z, ResourceLocation dimension) {}
}