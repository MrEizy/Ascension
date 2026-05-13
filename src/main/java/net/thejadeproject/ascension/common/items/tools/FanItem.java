package net.thejadeproject.ascension.common.items.tools;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.thejadeproject.ascension.common.blocks.entity.FlameStandBlockEntity;

/**
 * The Fan Item. Right-clicking on a lit Flame Stand raises its temperature.
 *
 * Each fan action adds {@link #TEMP_BOOST} degrees instantly, then a brief
 * overshoot is handled naturally by the decay curve.
 *
 * Register in ModItems:
 *   public static final DeferredItem<Item> FAN =
 *       ITEMS.register("fan", () -> new FanItem(new Item.Properties().stacksTo(1).durability(64)));
 */
public class FanItem extends Item {

    /** Temperature added per fan action. */
    public static final int TEMP_BOOST = 45;

    public FanItem(Properties properties) {
        super(properties.durability(128).stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        Player player = ctx.getPlayer();
        ItemStack stack = ctx.getItemInHand();

        if (level.isClientSide()) return InteractionResult.SUCCESS;

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof FlameStandBlockEntity flameStand)) {
            return InteractionResult.PASS;
        }

        if (!flameStand.isLit()) return InteractionResult.PASS;

        flameStand.fan(TEMP_BOOST);

        // Damage the fan item (uses durability as charge)
        if (!player.getAbilities().instabuild) {
            stack.hurtAndBreak(1, (ServerLevel) level, (ServerPlayer) player,
                    item -> player.getItemBySlot(EquipmentSlot.MAINHAND));
        }

        level.playSound(null, pos, SoundEvents.WOOL_HIT,
                SoundSource.BLOCKS, 0.6f, 1.4f + (float)(Math.random() * 0.2 - 0.1));

        return InteractionResult.sidedSuccess(false);
    }
}