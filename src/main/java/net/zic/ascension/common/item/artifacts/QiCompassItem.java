package net.zic.ascension.common.item.artifacts;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.LodestoneTracker;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.zic.ascension.chunks.atmospheric_qi.QiCompassSearch;

import java.util.Optional;

public class QiCompassItem extends Item {
    public static final int BASIC_RADIUS = 16;
    public static final int ADVANCED_RADIUS = 32;
    private static final int COOLDOWN_TICKS = 100;
    private final int radius;

    public QiCompassItem(Properties properties, int radius) {
        super(properties.stacksTo(1));
        this.radius = radius;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(stack)) return InteractionResult.FAIL;

        if (level instanceof ServerLevel serverLevel) {
            Optional<ChunkPos> target = QiCompassSearch.findRichestChunk(serverLevel, player, radius);
            if (target.isPresent()) {
                ChunkPos pos = target.get();
                BlockPos center = new BlockPos(pos.x() * 16 + 8, player.blockPosition().getY(), pos.z() * 16 + 8);
                stack.set(DataComponents.LODESTONE_TRACKER,
                        new LodestoneTracker(Optional.of(GlobalPos.of(serverLevel.dimension(), center)), false));
            } else {
                stack.remove(DataComponents.LODESTONE_TRACKER);
            }
            player.getCooldowns().addCooldown(stack, COOLDOWN_TICKS);
        }
        return InteractionResult.SUCCESS;
    }
}
