package net.zic.ascension.common.blocks;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.blocks.spirit_stone.SpiritStoneCluster;
import net.zic.ascension.common.blocks.spirit_stone.SpiritStoneClusterBE;

import java.util.function.Supplier;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, AscensionCraft.MOD_ID);

    public static final Supplier<BlockEntityType<SpiritStoneClusterBE>> SPIRIT_STONE_CLUSTER = BLOCK_ENTITY_TYPES.register(
            "spirit_stone_cluster",
            // The block entity type.
            () -> new BlockEntityType<>(
                    SpiritStoneClusterBE::new,
                    false,
                    ModBlocks.SPIRIT_STONE_CLUSTER.get()
            )
    );
}
