package net.zic.ascension.shaders.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import org.lwjgl.opengl.GL14;

/**
 * Custom render types for Ascension.
 *
 * The RIFT type mirrors how Lodestone's LodestoneRenderLayers creates its
 * additive-texture render types:
 *  - ShaderStateShard uses a lambda supplier from the ShaderHolder (never null at init)
 *  - EmptyTextureStateShard because all textures are bound manually in the shader
 *  - Phases.ADDITIVE_TRANSPARENCY (SRC_ALPHA + ONE) for the glowing crack look
 *  - NO_CULL, LIGHTMAP, OVERLAY as standard VFX options
 */
public class ModRenderTypes {

    public static final RenderType RIFT = RenderType.create(
            "ascension_rift",
            DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
            VertexFormat.Mode.QUADS,
            256,
            RenderType.CompositeState.builder()
                    // Lambda supplier via ShaderHolder — null-safe at class init time
                    .setShaderState(new RenderStateShard.ShaderStateShard(
                            ModShaders.RIFT.asSupplier()))
                    // All textures are bound manually inside the shader;
                    // no render-type-level texture binding needed
                    .setTextureState(new RenderStateShard.EmptyTextureStateShard(
                            () -> {}, () -> {}))
                    // Additive: dark = invisible, bright = adds light to scene
                    .setTransparencyState(Phases.ADDITIVE_TRANSPARENCY)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .setOverlayState(RenderStateShard.OVERLAY)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                    .createCompositeState(false)
    );

    // ── Ghost (unchanged from original) ──────────────────────────────────────

    private static final RenderType.TransparencyStateShard GHOST_TRANSPARENCY =
            new RenderStateShard.TransparencyStateShard(
                    "ghost_transparency",
                    () -> {
                        RenderSystem.enableBlend();
                        RenderSystem.blendFunc(
                                GlStateManager.SourceFactor.CONSTANT_ALPHA,
                                GlStateManager.DestFactor.ONE_MINUS_CONSTANT_ALPHA);
                        GL14.glBlendColor(1f, 1f, 1f, 0.5f);
                    },
                    () -> {
                        GL14.glBlendColor(1f, 1f, 1f, 1f);
                        RenderSystem.disableBlend();
                        RenderSystem.defaultBlendFunc();
                    });

    public static final RenderType GHOST = RenderType.create(
            "ascension:ghost",
            DefaultVertexFormat.BLOCK,
            VertexFormat.Mode.QUADS,
            2097152, true, false,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderType.POSITION_COLOR_TEX_LIGHTMAP_SHADER)
                    .setTextureState(RenderType.BLOCK_SHEET)
                    .setTransparencyState(GHOST_TRANSPARENCY)
                    .createCompositeState(false)
    );
}