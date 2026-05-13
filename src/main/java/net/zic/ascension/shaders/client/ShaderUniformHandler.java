package net.zic.ascension.shaders.client;

import net.minecraft.client.renderer.ShaderInstance;

/**
 * Functional interface for pushing per-frame uniform data into a shader
 * just before its render type's buffer is flushed.
 *
 * NeoForge equivalent of Lodestone's {@code ShaderUniformHandler}.
 *
 * Usage:
 * <pre>{@code
 * ShaderUniformHandler myHandler = shader -> {
 *     if (shader.getUniform("GameTime") != null)
 *         shader.getUniform("GameTime").set(currentTick);
 * };
 * }</pre>
 */
@FunctionalInterface
public interface ShaderUniformHandler {
    void updateShaderData(ShaderInstance shader);
}