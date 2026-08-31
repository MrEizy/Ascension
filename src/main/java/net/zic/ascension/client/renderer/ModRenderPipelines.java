package net.zic.ascension.client.renderer;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import net.zic.ascension.AscensionCraft;

import java.util.Optional;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID, value = Dist.CLIENT)
public class ModRenderPipelines {

    public static final String DIVINE_SENSE_UNIFORM = "DivineSenseWave";
    public static final String WORLD_DEPTH_SAMPLER = "WorldDepth";

    public static RenderPipeline LINES_NO_DEPTH;
    public static RenderPipeline ENERGY_LINES;
    public static RenderPipeline ENERGY_SURFACE;
    public static RenderPipeline ENERGY_SURFACE_NO_DEPTH;
    public static RenderPipeline AURA_SURFACE;
    public static RenderPipeline AURA_SURFACE_NO_DEPTH;
    public static RenderPipeline DIVINE_SENSE_WAVE;

    @SubscribeEvent
    public static void onRegisterRenderPipelines(RegisterRenderPipelinesEvent event) {
        LINES_NO_DEPTH = RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
                .withLocation(Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "pipeline/lines_no_depth"))
                .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                .withDepthStencilState(Optional.empty())
                .build();

        ENERGY_LINES = RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
                .withLocation(Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "pipeline/energy_lines"))
                .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                .build();

        ENERGY_SURFACE = RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
                .withLocation(Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "pipeline/energy_surface"))
                .withColorTargetState(new ColorTargetState(BlendFunction.ADDITIVE))
                .withCull(false)
                .build();

        ENERGY_SURFACE_NO_DEPTH = RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
                .withLocation(Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "pipeline/energy_surface_no_depth"))
                .withColorTargetState(new ColorTargetState(BlendFunction.ADDITIVE))
                .withDepthStencilState(Optional.empty())
                .withCull(false)
                .build();

        AURA_SURFACE = RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
                .withLocation(Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "pipeline/aura_surface"))
                .withVertexShader(Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "core/aura"))
                .withFragmentShader(Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "core/aura"))
                .withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS)
                .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                .withCull(false)
                .build();

        AURA_SURFACE_NO_DEPTH = RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
                .withLocation(Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "pipeline/aura_surface_no_depth"))
                .withVertexShader(Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "core/aura"))
                .withFragmentShader(Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "core/aura"))
                .withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS)
                .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                .withDepthStencilState(Optional.empty())
                .withCull(false)
                .build();

        DIVINE_SENSE_WAVE = RenderPipeline.builder()
                .withLocation(Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "pipeline/divine_sense_wave"))
                .withVertexShader(Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "core/screen_effect"))
                .withFragmentShader(Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "core/divine_sense_wave"))
                .withSampler(WORLD_DEPTH_SAMPLER)
                .withUniform(DIVINE_SENSE_UNIFORM, UniformType.UNIFORM_BUFFER)
                .withVertexFormat(DefaultVertexFormat.POSITION, VertexFormat.Mode.TRIANGLES)
                .withColorTargetState(new ColorTargetState(BlendFunction.ADDITIVE))
                .withDepthStencilState(Optional.empty())
                .withCull(false)
                .build();

        event.registerPipeline(LINES_NO_DEPTH);
        event.registerPipeline(ENERGY_LINES);
        event.registerPipeline(ENERGY_SURFACE);
        event.registerPipeline(ENERGY_SURFACE_NO_DEPTH);
        event.registerPipeline(AURA_SURFACE);
        event.registerPipeline(AURA_SURFACE_NO_DEPTH);
        event.registerPipeline(DIVINE_SENSE_WAVE);
    }
}
