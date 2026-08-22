package net.zic.ascension.client.renderer;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.resources.Identifier;
import net.zic.ascension.AscensionCraft;

/**
 * Corrected against the real decompiled 26.1.2 sources:
 *  - BlendFunction.ADDITIVE already exists as a named constant — no need to
 *    construct one.
 *  - There's no .withBlend()/.withDepthWrite()/.withDepthTestFunction() on
 *    Builder at all. Blend + color/alpha write mask live together in a
 *    ColorTargetState passed to .withColorTargetState(...); depth test +
 *    depth write live together in a DepthStencilState passed to
 *    .withDepthStencilState(...).
 *  - DepthTestFunction doesn't exist in 26.1 — it's CompareOp now
 *    (NO_DEPTH_TEST -> ALWAYS_PASS).
 *
 * No depth write for either pipeline (both are pure overlay VFX that
 * shouldn't occlude anything or leave depth artifacts), and depth test set
 * to ALWAYS_PASS since the wave shader does its own depth comparison
 * manually via the sampled DepthSampler, and the markers are meant to draw
 * through terrain regardless of the real depth buffer.
 */
public final class DivineSensePipelines {

    private static final ColorTargetState ADDITIVE_BLEND =
            new ColorTargetState(BlendFunction.ADDITIVE);

    private static final DepthStencilState NO_DEPTH_TEST_NO_WRITE =
            new DepthStencilState(CompareOp.ALWAYS_PASS, false);

    public static final RenderPipeline DIVINE_SENSE_EFFECT = RenderPipeline.builder()
            .withLocation(Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "pipeline/divine_sense_effect"))
            .withVertexShader(Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "divine_sense_effect"))
            .withFragmentShader(Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "divine_sense_effect"))
            .withVertexFormat(DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS)
            .withSampler("DepthSampler")
            .withUniform("DivineSenseEffectUniform", UniformType.UNIFORM_BUFFER)
            .withColorTargetState(ADDITIVE_BLEND)
            .withDepthStencilState(NO_DEPTH_TEST_NO_WRITE)
            .withCull(false)
            .build();

    public static final RenderPipeline DIVINE_SENSE_RESULT = RenderPipeline.builder()
            .withLocation(Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "pipeline/divine_sense_result"))
            .withVertexShader(Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "divine_sense_result"))
            .withFragmentShader(Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "divine_sense_result"))
            .withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS)
            .withUniform("DivineSenseResultUniform", UniformType.UNIFORM_BUFFER)
            .withColorTargetState(ADDITIVE_BLEND)
            .withDepthStencilState(NO_DEPTH_TEST_NO_WRITE)
            .withCull(false)
            .build();

    private DivineSensePipelines() {
    }
}