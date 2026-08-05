package net.zic.ascension.api.ascension.datapack;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ExtensionTypeRegistry<T> {
    private final DeferredRegister<CodecType<T>> register;

    public ExtensionTypeRegistry(Registry<CodecType<T>> registry, String namespace) {
        register = DeferredRegister.create(registry, namespace);
    }

    public <V extends T> DeferredHolder<CodecType<T>, CodecType<T>> add(String id, MapCodec<V> codec) {
        return register.register(id, () -> new CodecType<>(codec));
    }

    public void register(IEventBus eventBus) {
        register.register(eventBus);
    }
}
