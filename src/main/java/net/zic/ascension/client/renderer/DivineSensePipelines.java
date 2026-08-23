package net.zic.ascension.client.renderer;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import net.zic.ascension.AscensionCraft;

public final class DivineSensePipelines {

    public static final String EFFECT_UNIFORM = "DivineSenseEffect";
    public static final String DEPTH_SAMPLER = "DepthSampler";

    private static final ColorTargetState ADDITIVE_BLEND =
            new ColorTargetState(BlendFunction.ADDITIVE);

    public static RenderPipeline DIVINE_SENSE_EFFECT;

    public static void register(RegisterRenderPipelinesEvent event) {
        DIVINE_SENSE_EFFECT = RenderPipeline.builder(RenderPipelines.POST_PROCESSING_SNIPPET)
                .withLocation(Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "pipeline/divine_sense_effect"))
                .withVertexShader(Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "core/divine_sense_effect"))
                .withFragmentShader(Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "core/divine_sense_effect"))
                .withSampler(DEPTH_SAMPLER)
                .withUniform(EFFECT_UNIFORM, UniformType.UNIFORM_BUFFER)
                .withVertexFormat(DefaultVertexFormat.EMPTY, VertexFormat.Mode.TRIANGLES)
                .withColorTargetState(ADDITIVE_BLEND)
                .withCull(false)
                .build();

        event.registerPipeline(DIVINE_SENSE_EFFECT);
    }

    private DivineSensePipelines() {
    }
}
