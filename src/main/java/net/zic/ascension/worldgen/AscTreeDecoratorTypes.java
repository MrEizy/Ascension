package net.zic.ascension.worldgen;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.worldgen.tree.PodCropDecorator;

public class AscTreeDecoratorTypes {
    public static final DeferredRegister<TreeDecoratorType<?>> TREE_DECORATOR_TYPES =
            DeferredRegister.create(Registries.TREE_DECORATOR_TYPE, AscensionCraft.MOD_ID);

    public static final DeferredHolder<TreeDecoratorType<?>, TreeDecoratorType<PodCropDecorator>> PEACH_POD =
            TREE_DECORATOR_TYPES.register("peach_pod", () -> new TreeDecoratorType<>(PodCropDecorator.CODEC));

    public static void register(IEventBus eventBus) {
        TREE_DECORATOR_TYPES.register(eventBus);
    }
}
