package net.zic.ascension.common.blocks.auction;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.zic.ascension.common.auction.AuctionHouseData;
import net.zic.ascension.common.auction.AuctionManager;
import net.zic.ascension.common.auction.AuctionScreenSync;

import javax.annotation.Nullable;

public final class AuctionHouseCoreBlock extends Block {
    public AuctionHouseCoreBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return null;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!(level instanceof ServerLevel serverLevel) || !(placer instanceof ServerPlayer owner)) {
            return;
        }
        AuctionManager manager = AuctionManager.getInstance();
        if (manager != null) {
            manager.registerHouse(serverLevel, pos, owner);
        }
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hitResult
    ) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(player instanceof ServerPlayer serverPlayer) || !(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.PASS;
        }

        AuctionManager manager = AuctionManager.getInstance();
        if (manager == null) {
            return InteractionResult.PASS;
        }

        AuctionHouseData house = manager.findHouse(level, pos);
        if (house == null || house.ownerId().equals(serverPlayer.getUUID())) {
            house = manager.registerHouse(serverLevel, pos, serverPlayer);
        }
        if (!house.ownerId().equals(serverPlayer.getUUID())) {
            serverPlayer.sendSystemMessage(net.minecraft.network.chat.Component.translatable(
                    "auction.ascension.not_owner",
                    house.ownerName()
            ));
            return InteractionResult.SUCCESS;
        }

        AuctionScreenSync.openOwnerHome(serverPlayer, house);
        return InteractionResult.SUCCESS;
    }
}
