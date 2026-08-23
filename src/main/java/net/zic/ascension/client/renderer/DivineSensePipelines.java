package net.zic.ascension.client.renderer;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import net.zic.ascension.AscensionCraft;

import java.util.Optional;

public final class DivineSensePipelines {

    private static final ColorTargetState ADDITIVE_BLEND =
            new ColorTargetState(BlendFunction.ADDITIVE);

    public static RenderPipeline DIVINE_SENSE_EFFECT;

    public static void register(RegisterRenderPipelinesEvent event) {
        DIVINE_SENSE_EFFECT = RenderPipeline.builder()
            .withLocation(Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "pipeline/divine_sense_effect"))
            .withVertexShader(Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "core/divine_sense_effect"))
            .withFragmentShader(Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "core/divine_sense_effect"))
            .withVertexFormat(DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.QUADS)
            .withSampler("DepthSampler")
            .withUniform("DivineSenseEffectUniform", UniformType.UNIFORM_BUFFER)
            .withColorTargetState(ADDITIVE_BLEND)
            .withDepthStencilState(Optional.empty())
            .withCull(false)
            .build();

        event.registerPipeline(DIVINE_SENSE_EFFECT);
    }

    private DivineSensePipelines() {
    }
}
