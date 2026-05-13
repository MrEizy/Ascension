package net.thejadeproject.ascension.common.items.artifacts.talismans;

import net.minecraft.ChatFormatting;
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
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class SoulAnchorTalisman extends Item {

    private static final String ANCHOR_X         = "anchor_x";
    private static final String ANCHOR_Y         = "anchor_y";
    private static final String ANCHOR_Z         = "anchor_z";
    private static final String ANCHOR_DIMENSION = "anchor_dimension";
    private static final String CONSUMED         = "consumed";

    public SoulAnchorTalisman(Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.RARE));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (isConsumed(stack)) {
            if (!level.isClientSide) {
                player.displayClientMessage(
                        Component.translatable("ascension.soulanchor.already_used"), true);
            }
            return InteractionResultHolder.fail(stack);
        }

        if (player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                saveAnchor(stack, player.blockPosition(), level.dimension());
                level.playSound(null, player.blockPosition(),
                        SoundEvents.ENDER_EYE_DEATH, SoundSource.PLAYERS, 1.0F, 0.8F);
                player.displayClientMessage(
                        Component.translatable("ascension.soulanchor.anchor_set"), true);
            } else {
                level.playSound(player, player.blockPosition(),
                        SoundEvents.ENDER_EYE_DEATH, SoundSource.PLAYERS, 1.0F, 0.8F);
            }
            return InteractionResultHolder.success(stack);

        } else {
            if (!level.isClientSide) {
                AnchorData anchor = getAnchor(stack);
                if (anchor == null) {
                    player.displayClientMessage(
                            Component.translatable("ascension.soulanchor.no_anchor"), true);
                } else {
                    player.displayClientMessage(
                            Component.translatable("ascension.soulanchor.anchor_info",
                                    (int) anchor.x(), (int) anchor.y(), (int) anchor.z()), true);
                }
            }
            return InteractionResultHolder.success(stack);
        }
    }

    public static void tryActivateFromDrops(ServerPlayer player, Collection<ItemEntity> drops) {
        ItemStack anchorStack = removeAnchorTalismanFromDrops(drops);
        if (anchorStack == null) return;

        AnchorData anchor = getAnchor(anchorStack);
        if (anchor == null) return;

        ResourceKey<Level> dimensionKey = ResourceKey.create(
                Registries.DIMENSION, anchor.dimension());
        ServerLevel targetLevel = player.getServer().getLevel(dimensionKey);

        if (targetLevel == null) {
            player.sendSystemMessage(
                    Component.translatable("ascension.soulanchor.dimension_invalid")
                            .withStyle(ChatFormatting.RED));
            return;
        }

        if (drops.isEmpty()) {
            markConsumed(anchorStack);
            return;
        }

        BlockPos anchorPos = new BlockPos((int) anchor.x(), (int) anchor.y(), (int) anchor.z());
        int count = drops.size();

        List<ItemStack> stacks = drops.stream()
                .map(ie -> ie.getItem().copy())
                .collect(Collectors.toList());
        drops.clear();

        for (ItemStack stack : stacks) {
            ItemEntity newDrop = new ItemEntity(
                    targetLevel,
                    anchor.x() + 0.5, anchor.y() + 0.5, anchor.z() + 0.5,
                    stack
            );
            newDrop.setPickUpDelay(40);
            targetLevel.addFreshEntity(newDrop);
        }

        targetLevel.sendParticles(
                net.minecraft.core.particles.ParticleTypes.PORTAL,
                anchor.x() + 0.5, anchor.y() + 1.0, anchor.z() + 0.5,
                30, 0.4, 0.6, 0.4, 0.1);
        targetLevel.playSound(null, anchorPos,
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 0.7F);

        markConsumed(anchorStack);

        player.sendSystemMessage(
                Component.translatable("ascension.soulanchor.activated",
                        count, (int) anchor.x(), (int) anchor.y(), (int) anchor.z()));
    }


    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);

        if (isConsumed(stack)) {
            tooltip.add(Component.translatable("ascension.soulanchor.tooltip.consumed")
                    .withStyle(ChatFormatting.DARK_RED));
            return;
        }

        AnchorData anchor = getAnchor(stack);
        if (anchor != null) {
            tooltip.add(Component.translatable("ascension.soulanchor.tooltip.anchored")
                    .withStyle(ChatFormatting.GREEN));
            tooltip.add(Component.translatable("ascension.soulanchor.tooltip.coords",
                            (int) anchor.x(), (int) anchor.y(), (int) anchor.z())
                    .withStyle(ChatFormatting.GRAY));

            String dimName = anchor.dimension().getPath();
            String formatted = Arrays.stream(dimName.split("_"))
                    .map(w -> w.substring(0, 1).toUpperCase() + w.substring(1))
                    .collect(Collectors.joining(" "));
            tooltip.add(Component.translatable("ascension.soulanchor.tooltip.dimension", formatted)
                    .withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Component.translatable("ascension.soulanchor.tooltip.unset")
                    .withStyle(ChatFormatting.GRAY));
        }

        tooltip.add(Component.translatable("ascension.soulanchor.tooltip.hint")
                .withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("ascension.soulanchor.tooltip.hint2")
                .withStyle(ChatFormatting.DARK_GRAY));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return hasAnchor(stack) && !isConsumed(stack);
    }

    private static void saveAnchor(ItemStack stack, BlockPos pos, ResourceKey<Level> dimension) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putDouble(ANCHOR_X, pos.getX() + 0.5);
        tag.putDouble(ANCHOR_Y, pos.getY());
        tag.putDouble(ANCHOR_Z, pos.getZ() + 0.5);
        tag.putString(ANCHOR_DIMENSION, dimension.location().toString());
        tag.remove(CONSUMED);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    private static @Nullable AnchorData getAnchor(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return null;
        CompoundTag tag = data.copyTag();
        if (!tag.contains(ANCHOR_X) || !tag.contains(ANCHOR_DIMENSION)) return null;
        return new AnchorData(
                tag.getDouble(ANCHOR_X),
                tag.getDouble(ANCHOR_Y),
                tag.getDouble(ANCHOR_Z),
                ResourceLocation.parse(tag.getString(ANCHOR_DIMENSION))
        );
    }

    private static boolean hasAnchor(ItemStack stack) {
        return getAnchor(stack) != null;
    }

    private static void markConsumed(ItemStack stack) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putBoolean(CONSUMED, true);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    private static boolean isConsumed(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return false;
        return data.copyTag().getBoolean(CONSUMED);
    }

    private static @Nullable ItemStack removeAnchorTalismanFromDrops(Collection<ItemEntity> drops) {
        Iterator<ItemEntity> iterator = drops.iterator();
        while (iterator.hasNext()) {
            ItemEntity itemEntity = iterator.next();
            ItemStack stack = itemEntity.getItem();
            if (stack.getItem() instanceof SoulAnchorTalisman
                    && hasAnchor(stack) && !isConsumed(stack)) {
                iterator.remove();
                return stack;
            }
        }
        return null;
    }

    private record AnchorData(double x, double y, double z, ResourceLocation dimension) {}
}