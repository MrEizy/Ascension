package net.zic.ascension.common.blocks.spirit_stone;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.zic.ascension.api.ascension.core.qi.QiHandler;

public class SpiritStoneClusterQiHandler implements QiHandler {
    private final Level level;
    private final BlockPos pos;
    private final BlockState state;
    private final SpiritStoneClusterBE blockEntity;

    public SpiritStoneClusterQiHandler(Level level, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        this.level = level;
        this.pos = pos;
        this.state = state;
        this.blockEntity = (SpiritStoneClusterBE) blockEntity;
    }


    @Override
    public boolean tryConsume(int amount) {
        return false; //TODO
    }

    @Override
    public int insertQi(int amount) {
        return 0;//TODO
    }

    @Override
    public int extractQi(int amount) {
        return 0;//TODO
    }

    @Override
    public long getQi() {
        return blockEntity.getStoredQi();
    }

    @Override
    public long getCapacity() {
        return blockEntity.getCapacity();
    }

    @Override
    public boolean isFull() {
        return getCapacity() == getQi();
    }
}
