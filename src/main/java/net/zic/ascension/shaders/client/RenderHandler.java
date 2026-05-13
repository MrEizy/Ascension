package net.zic.ascension.shaders.client;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages a registry of {@link ShaderUniformHandler}s keyed by {@link RenderType}.
 *
 * NeoForge equivalent of the relevant parts of Lodestone's {@code RenderHandler}.
 *
 * Before you call {@link MultiBufferSource.BufferSource#endBatch(RenderType)},
 * call {@link #applyHandler(RenderType)} to push uniforms into the active shader.
 *
 * Register handlers once (e.g. in your client setup or static init):
 * <pre>{@code
 * RenderHandler.registerHandler(ModRenderTypes.RIFT, shader -> {
 *     ModShaders.uploadRiftUniforms(partialTick);
 * });
 * }</pre>
 *
 * Then in your renderer, before flushing:
 * <pre>{@code
 * RenderHandler.applyHandler(ModRenderTypes.RIFT);
 * bufferSource.endBatch(ModRenderTypes.RIFT);
 * }</pre>
 */
public class RenderHandler {

    private static final Map<RenderType, ShaderUniformHandler> HANDLERS = new HashMap<>();

    /**
     * Register a uniform handler for a render type.
     * Can be called multiple times; last registration wins.
     */
    public static void registerHandler(RenderType type, ShaderUniformHandler handler) {
        HANDLERS.put(type, handler);
    }

    /**
     * If a handler is registered for this render type, fetch the shader from
     * the render type's shader state and call the handler.
     * Safe to call even if no handler is registered or the shader is null.
     */
    public static void applyHandler(RenderType type) {
        ShaderUniformHandler handler = HANDLERS.get(type);
        if (handler == null) return;

        // Retrieve the current shader from RenderSystem.
        // The ShaderStateShard sets this when the render type is activated.
        net.minecraft.client.renderer.ShaderInstance shader =
                com.mojang.blaze3d.systems.RenderSystem.getShader();
        if (shader != null) {
            handler.updateShaderData(shader);
        }
    }

    /** Remove a handler (e.g. on resource reload). */
    public static void removeHandler(RenderType type) {
        HANDLERS.remove(type);
    }
}