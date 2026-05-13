package net.zic.ascension.shaders.client;

import net.minecraft.client.renderer.ShaderInstance;

import java.util.function.Supplier;

/**
 * Holds a lazily-assigned {@link ShaderInstance} reference and exposes a
 * {@link Supplier} for use inside {@link net.minecraft.client.renderer.RenderType}
 * shader state shards.
 *
 * This is the NeoForge equivalent of Lodestone's {@code ShaderHolder}.
 * The instance is null until the shader is successfully registered and loaded
 * via {@link ModShaders#register}.
 */
public class ShaderHolder {

    private ShaderInstance instance;

    /** Called by ModShaders after the shader is registered. */
    public void setInstance(ShaderInstance instance) {
        this.instance = instance;
    }

    public ShaderInstance getInstance() {
        return instance;
    }

    /**
     * Returns a {@link Supplier} suitable for passing to
     * {@link net.minecraft.client.renderer.RenderStateShard.ShaderStateShard}.
     * Uses a lambda so that the null-at-init problem is avoided — the supplier
     * is only evaluated at draw time, by which point the shader is loaded.
     */
    public Supplier<ShaderInstance> asSupplier() {
        return () -> instance;
    }
}