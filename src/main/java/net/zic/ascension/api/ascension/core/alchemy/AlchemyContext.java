package net.zic.ascension.api.ascension.core.alchemy;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public record AlchemyContext(Level level, BlockPos pos, LivingEntity alchemist) {
    public static final AlchemyContext EMPTY = new AlchemyContext(null, null, null);

    public static AlchemyContext of(Level level, BlockPos pos, LivingEntity alchemist) {
        return new AlchemyContext(level, pos, alchemist);
    }

    public static AlchemyContext of(LivingEntity alchemist) {
        return alchemist == null ? EMPTY : new AlchemyContext(alchemist.level(), alchemist.blockPosition(), alchemist);
    }
}
