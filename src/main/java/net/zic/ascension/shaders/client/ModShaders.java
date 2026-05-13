package net.zic.ascension.shaders.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.zic.ascension.AscensionCraft;

import java.io.IOException;

/**
 * Registers and holds all mod shaders.
 *
 * Pattern mirrors Lodestone's LodestoneShaderRegistry / ShaderHolder:
 *  - Each shader has a static ShaderHolder that stores the live instance.
 *  - Render types reference holders via holder.asSupplier(), never a raw null ref.
 *  - Uniforms are pushed per-frame via uploadRiftUniforms(), equivalent to
 *    Lodestone's ShaderUniformHandler lambda stored in RenderHandler.HANDLERS.
 */
public class ModShaders {

    // ── Shader holders (one per program) ─────────────────────────────────────
    public static final ShaderHolder RIFT = new ShaderHolder();

    // ── VFX texture locations ─────────────────────────────────────────────────
    public static final ResourceLocation TEX_VORTEX    = rl("textures/vfx/vortex.png");
    public static final ResourceLocation TEX_CRACK_1   = rl("textures/vfx/rift_crack_bloom_1.png");
    public static final ResourceLocation TEX_CRACK_2   = rl("textures/vfx/rift_crack_bloom_2.png");
    public static final ResourceLocation TEX_CRACK_3   = rl("textures/vfx/rift_crack_bloom_3.png");
    public static final ResourceLocation TEX_CRACK_4   = rl("textures/vfx/rift_crack_bloom_4.png");
    public static final ResourceLocation TEX_CRACK_5   = rl("textures/vfx/rift_crack_bloom_5.png");
    public static final ResourceLocation TEX_RUNE_GLOW = rl("textures/vfx/rune_glow.png");

    // ── Registration ──────────────────────────────────────────────────────────

    public static void register(RegisterShadersEvent event) throws IOException {
        event.registerShader(
                new ShaderInstance(
                        event.getResourceProvider(),
                        rl("rift"),
                        DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP
                ),
                RIFT::setInstance
        );
    }

    // ── Per-frame uniform upload ───────────────────────────────────────────────

    public static void uploadRiftUniforms(float partialTick) {
        ShaderInstance s = RIFT.getInstance();
        if (s == null) return;

        float ticks = (Minecraft.getInstance().level != null
                ? (float) Minecraft.getInstance().level.getGameTime()
                : 0f) + partialTick;

        if (s.getUniform("GameTime") != null) s.getUniform("GameTime").set(ticks);

        // Texture units 3-9: above Minecraft's reserved 0 (diffuse), 1 (overlay), 2 (lightmap)
        bindSampler(s, "SamplerVortex",   TEX_VORTEX,    3);
        bindSampler(s, "SamplerCrack1",   TEX_CRACK_1,   4);
        bindSampler(s, "SamplerCrack2",   TEX_CRACK_2,   5);
        bindSampler(s, "SamplerCrack3",   TEX_CRACK_3,   6);
        bindSampler(s, "SamplerCrack4",   TEX_CRACK_4,   7);
        bindSampler(s, "SamplerCrack5",   TEX_CRACK_5,   8);
        bindSampler(s, "SamplerRuneGlow", TEX_RUNE_GLOW, 9);
    }

    private static void bindSampler(ShaderInstance s, String name, ResourceLocation loc, int unit) {
        var uniform = s.getUniform(name);
        if (uniform != null) uniform.set(unit);
        var tex = Minecraft.getInstance().getTextureManager().getTexture(loc, null);
        if (tex != null) RenderSystem.setShaderTexture(unit, tex.getId());
    }

    private static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, path);
    }
}