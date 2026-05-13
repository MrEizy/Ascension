package net.zic.ascension.shaders.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

/**
 * Pre-built {@link RenderType.TransparencyStateShard} constants.
 *
 * NeoForge equivalent of Lodestone's {@code Phases}.
 *
 * ADDITIVE_TRANSPARENCY  – SRC_ALPHA + ONE  (glowing/additive, light-only blend)
 * NORMAL_TRANSPARENCY    – SRC_ALPHA + ONE_MINUS_SRC_ALPHA  (standard alpha blend)
 */
public final class Phases {

    private Phases() {}

    /**
     * Additive blend: fragment colour is added to the framebuffer.
     * Produces a "glowing" effect — dark areas are invisible, bright areas add light.
     * Disables depth write so overlapping effects don't z-fight.
     */
    public static final RenderType.TransparencyStateShard ADDITIVE_TRANSPARENCY =
            new RenderStateShard.TransparencyStateShard(
                    "ascension_additive_transparency",
                    () -> {
                        RenderSystem.enableBlend();
                        RenderSystem.blendFunc(
                                GlStateManager.SourceFactor.SRC_ALPHA,
                                GlStateManager.DestFactor.ONE
                        );
                        RenderSystem.depthMask(false);
                    },
                    () -> {
                        RenderSystem.disableBlend();
                        RenderSystem.defaultBlendFunc();
                        RenderSystem.depthMask(true);
                    });

    /**
     * Standard alpha blend: normal transparency like glass.
     */
    public static final RenderType.TransparencyStateShard NORMAL_TRANSPARENCY =
            new RenderStateShard.TransparencyStateShard(
                    "ascension_normal_transparency",
                    () -> {
                        RenderSystem.enableBlend();
                        RenderSystem.blendFunc(
                                GlStateManager.SourceFactor.SRC_ALPHA,
                                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
                        );
                    },
                    () -> {
                        RenderSystem.disableBlend();
                        RenderSystem.defaultBlendFunc();
                    });
}