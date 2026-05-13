package net.thejadeproject.ascension.common.items.artifacts;

import com.google.common.collect.Maps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.nbt.CompoundTag;
import net.thejadeproject.ascension.common.items.ModItems;
import net.thejadeproject.ascension.common.blocks.ModBlocks;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.List;

public class FlameGourd extends Item {
    private static Map<Block, Item> FIRE_ITEMS;
    private static Set<Block> ABSORBABLE_FIRES;

    // Custom data component keys
    private static final String FILLED_KEY = "Filled";
    private static final String ABSORBED_FIRE_KEY = "AbsorbedFire";

    private static void initFireItems() {
        if (FIRE_ITEMS != null) return;

        Map<Block, Item> map = Maps.newHashMap();
        map.put(Blocks.FIRE, ModItems.FLAME.get());
        map.put(Blocks.SOUL_FIRE, ModItems.SOUL_FLAME.get());
        map.put(ModBlocks.CRIMSON_LOTUS_FIRE.get(), ModItems.CRIMSON_LOTUS_FLAME.get());

        FIRE_ITEMS = Map.copyOf(map);
        ABSORBABLE_FIRES = FIRE_ITEMS.keySet();
    }

    private static Map<Block, Item> getFireItems() {
        initFireItems();
        return FIRE_ITEMS;
    }

    private static Set<Block> getAbsorbableFires() {
        initFireItems();
        return ABSORBABLE_FIRES;
    }

    public FlameGourd(Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.RARE));
    }

    private boolean isFilled(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            CompoundTag nbt = customData.copyTag();
            return nbt.getBoolean(FILLED_KEY);
        }
        return false;
    }

    @Nullable
    private String getAbsorbedFireType(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            CompoundTag nbt = customData.copyTag();
            if (nbt.contains(ABSORBED_FIRE_KEY)) {
                return nbt.getString(ABSORBED_FIRE_KEY);
            }
        }
        return null;
    }

    @Nullable
    private Block getAbsorbedFireBlock(ItemStack stack) {
        String fireType = getAbsorbedFireType(stack);
        if (fireType != null) {
            ResourceLocation fireRl = ResourceLocation.tryParse(fireType);
            if (fireRl != null) {
                return BuiltInRegistries.BLOCK.get(fireRl);
            }
        }
        return null;
    }

    @Nullable
    private Item getConvertedItem(ItemStack stack) {
        Block fireBlock = getAbsorbedFireBlock(stack);
        if (fireBlock != null) {
            return getFireItems().get(fireBlock);
        }
        return null;
    }

    private void setFilled(ItemStack stack, boolean filled, @Nullable String fireType) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        CompoundTag nbt = customData != null ? customData.copyTag() : new CompoundTag();
        nbt.putBoolean(FILLED_KEY, filled);
        if (fireType != null) {
            nbt.putString(ABSORBED_FIRE_KEY, fireType);
        } else {
            nbt.remove(ABSORBED_FIRE_KEY);
        }
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        ItemStack stack = context.getItemInHand();
        Player player = context.getPlayer();

        if (player == null) {
            return InteractionResult.PASS;
        }

        if (getAbsorbableFires().contains(state.getBlock()) && !isFilled(stack)) {
            if (!level.isClientSide) {
                level.removeBlock(pos, false);
                level.gameEvent(player, GameEvent.BLOCK_DESTROY, pos);
                setFilled(stack, true, BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString());
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return super.useOn(context);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isCrouching() && isFilled(stack)) {
            if (!level.isClientSide) {
                String fireType = getAbsorbedFireType(stack);
                if (fireType != null) {
                    ResourceLocation fireRl = ResourceLocation.tryParse(fireType);
                    if (fireRl != null) {
                        Block fireBlock = BuiltInRegistries.BLOCK.get(fireRl);
                        Item item = getFireItems().get(fireBlock);
                        if (item != null) {
                            ItemStack toGive = new ItemStack(item);
                            if (!player.addItem(toGive)) {
                                player.drop(toGive, false);
                            }
                            setFilled(stack, false, null);
                        }
                    }
                }
            }
            return InteractionResultHolder.consume(stack);
        }
        return super.use(level, player, hand);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return isFilled(stack);
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return isFilled(stack) ? 13 : 0;
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0xFF6B00; // Orange color for filled state
    }

    // Custom tooltip component to show the fire sprite and item sprite
    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        if (isFilled(stack)) {
            Block fireBlock = getAbsorbedFireBlock(stack);
            Item convertedItem = getConvertedItem(stack);

            if (fireBlock != null && convertedItem != null) {
                return Optional.of(new FlameGourdTooltip(fireBlock, convertedItem));
            }
        }
        return Optional.empty();
    }

    // Custom tooltip component class
    public record FlameGourdTooltip(Block fireBlock, Item convertedItem) implements TooltipComponent {}
}