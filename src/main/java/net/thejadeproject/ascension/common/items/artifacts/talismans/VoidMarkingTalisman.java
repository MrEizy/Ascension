package net.thejadeproject.ascension.common.items.artifacts.talismans;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.thejadeproject.ascension.common.items.artifacts.bases.BaseTeleportTalisman;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class VoidMarkingTalisman extends BaseTeleportTalisman {
    private static final int COOLDOWN_TICKS = 30 * 20; // 30 seconds
    private static final int RECHARGE_MAX_VALUE = 25;
    private static final String SAVED_LOCATION_TAG = "SavedLocation";
    private static final String SAVED_X = "saved_x";
    private static final String SAVED_Y = "saved_y";
    private static final String SAVED_Z = "saved_z";
    private static final String SAVED_DIMENSION = "saved_dimension";

    public VoidMarkingTalisman(Properties properties) {
        super(properties.rarity(Rarity.EPIC));
    }

    // Player data NBT keys
    @Override protected String getCooldownTag() { return "VoidMarking_Cooldown"; }
    @Override protected String getCooldownTimeTag() { return "VoidMarking_CooldownTime"; }
    @Override protected String getCountdownTag() { return "VoidMarking_Countdown"; }
    @Override protected String getInitialPosTag() { return "VoidMarking_InitialPos"; }
    @Override protected String getInitialHealthTag() { return "VoidMarking_InitialHealth"; }
    @Override protected String getUsedHandTag() { return "VoidMarking_UsedHand"; }
    @Override protected String getUsedSlotTag() { return "VoidMarking_UsedSlot"; }

    @Override protected int getCooldownTicks() { return COOLDOWN_TICKS; }
    @Override protected int getCountdownTicks() { return 5 * TICKS_PER_SECOND; }
    @Override protected Rarity getTalismanRarity() { return Rarity.RARE; }
    @Override protected String getDisplayNameKey() { return "item.ascension.void_marking_talisman"; }
    @Override protected int getRechargeMaxValue() { return RECHARGE_MAX_VALUE; }
    @Override protected String getPermanentVariantId() { return "permanent_void_marking"; }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.isShiftKeyDown()) {
            if (level.isClientSide) {
                level.playSound(player, player.blockPosition(), SoundEvents.ENDER_EYE_DEATH,
                        SoundSource.PLAYERS, 1.0F, 1.0F);
                return InteractionResultHolder.consume(stack);
            }

            saveLocation(stack, player.blockPosition(), level.dimension());
            player.displayClientMessage(
                    Component.translatable("ascension.voidmarking.location_saved"),
                    true
            );

            ServerLevel serverLevel = (ServerLevel) level;
            serverLevel.playSound(null, player.blockPosition(), SoundEvents.ENDER_EYE_DEATH,
                    SoundSource.PLAYERS, 1.0F, 1.0F);

            return InteractionResultHolder.success(stack);
        } else {
            if (!hasSavedLocation(stack)) {
                if (!level.isClientSide) {
                    player.displayClientMessage(
                            Component.translatable("ascension.voidmarking.no_location"),
                            true
                    );
                }
                return InteractionResultHolder.fail(stack);
            }

            return super.use(level, player, hand);
        }
    }

    @Override
    protected void performTeleport(ServerPlayer player, ItemStack usedStack, int usedSlot) {
        SavedLocationData location = getSavedLocation(usedStack);
        if (location == null) {
            player.displayClientMessage(
                    Component.translatable("ascension.voidmarking.no_location"),
                    true
            );
            clearCountdownData(player, getActualCountdownTag(usedStack)); // FIXED
            return;
        }

        ServerLevel currentLevel = (ServerLevel) player.level();
        ResourceKey<Level> dimensionKey = ResourceKey.create(Registries.DIMENSION, location.dimension());
        ServerLevel targetLevel = currentLevel.getServer().getLevel(dimensionKey);

        if (targetLevel == null) {
            player.displayClientMessage(
                    Component.translatable("ascension.voidmarking.dimension_invalid"),
                    true
            );
            clearCountdownData(player, getActualCountdownTag(usedStack)); // FIXED
            return;
        }

        player.teleportTo(targetLevel, location.x(), location.y(), location.z(),
                player.getYRot(), player.getXRot());

        playArrivalEffects(player);
        startCooldown(player, usedStack);
        consumeItem(player, usedStack, usedSlot);

        player.displayClientMessage(
                Component.translatable("ascension.voidmarking.teleported"),
                true
        );

        clearCountdownData(player, getActualCountdownTag(usedStack)); // FIXED
    }

    @Override
    protected void preserveCustomData(ItemStack stack, CompoundTag cooldownTag) {
        CustomData existingData = stack.get(DataComponents.CUSTOM_DATA);
        if (existingData != null) {
            CompoundTag existingTag = existingData.copyTag();
            if (existingTag.contains(SAVED_X)) {
                cooldownTag.putDouble(SAVED_X, existingTag.getDouble(SAVED_X));
                cooldownTag.putDouble(SAVED_Y, existingTag.getDouble(SAVED_Y));
                cooldownTag.putDouble(SAVED_Z, existingTag.getDouble(SAVED_Z));
                cooldownTag.putString(SAVED_DIMENSION, existingTag.getString(SAVED_DIMENSION));
            }
        }
    }

    @Override
    protected void clearCooldownDisplayOnly(ItemStack stack) {
        CustomData existingData = stack.get(DataComponents.CUSTOM_DATA);
        if (existingData != null) {
            CompoundTag tag = existingData.copyTag();
            tag.remove("CooldownMinutes");
            tag.remove("CooldownSeconds");
            tag.remove("RechargeStatus");

            if (!tag.isEmpty()) {
                stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
            } else {
                stack.remove(DataComponents.CUSTOM_DATA);
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);

        SavedLocationData location = getSavedLocation(stack);
        if (location != null) {
            tooltip.add(Component.translatable("ascension.voidmarking.tooltip.saved"));
            tooltip.add(Component.translatable("ascension.voidmarking.tooltip.coords",
                    String.format("%.1f", location.x()),
                    String.format("%.1f", location.y()),
                    String.format("%.1f", location.z())));

            String dimensionName = location.dimension().getPath();
            String formatted = Arrays.stream(dimensionName.split("_"))
                    .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1))
                    .collect(Collectors.joining(" "));
            tooltip.add(Component.translatable("ascension.voidmarking.tooltip.dimension", formatted));
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return hasSavedLocation(stack);
    }

    // Location persistence methods
    private void saveLocation(ItemStack stack, BlockPos pos, ResourceKey<Level> dimension) {
        CompoundTag tag = new CompoundTag();
        tag.putDouble(SAVED_X, pos.getX() + 0.5);
        tag.putDouble(SAVED_Y, pos.getY());
        tag.putDouble(SAVED_Z, pos.getZ() + 0.5);
        tag.putString(SAVED_DIMENSION, dimension.location().toString());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    private @Nullable SavedLocationData getSavedLocation(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return null;

        CompoundTag tag = data.copyTag();
        if (!tag.contains(SAVED_X) || !tag.contains(SAVED_Y) ||
                !tag.contains(SAVED_Z) || !tag.contains(SAVED_DIMENSION)) {
            return null;
        }

        return new SavedLocationData(
                tag.getDouble(SAVED_X),
                tag.getDouble(SAVED_Y),
                tag.getDouble(SAVED_Z),
                ResourceLocation.parse(tag.getString(SAVED_DIMENSION))
        );
    }

    private boolean hasSavedLocation(ItemStack stack) {
        return getSavedLocation(stack) != null;
    }

    private record SavedLocationData(double x, double y, double z, ResourceLocation dimension) {}
}