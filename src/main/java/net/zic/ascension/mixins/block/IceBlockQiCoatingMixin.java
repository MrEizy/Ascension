package net.zic.ascension.mixins.block;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.IceBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.zic.ascension.impl.core.skill.castable.QiCoatingMining;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Ice mined with a tool whose Qi Coating element keeps it whole (external_on_mine silk_touch, QiCoatingMining.keepsWhole)
 * doesn't melt where it stood, as if the tool had Silk Touch.
 */
@Mixin(IceBlock.class)
public abstract class IceBlockQiCoatingMixin {
    @ModifyExpressionValue(method = "playerDestroy", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;hasTag(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/tags/TagKey;)Z"))
    private boolean ascension$keepWhole(boolean preventsMelting, Level level, Player player, BlockPos pos, BlockState state,
                                        BlockEntity blockEntity, ItemStack destroyedWith) {
        return preventsMelting || QiCoatingMining.keepsWhole(player, state, destroyedWith);
    }
}
