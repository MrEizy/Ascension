package net.zic.ascension.client.renderer;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.platform.CompareOp;
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
    public static final String SPHERICAL_DESTRUCTION_UNIFORM = "SphericalDestructionWave";
    public static final String WORLD_DEPTH_SAMPLER = "WorldDepth";

    public static RenderPipeline LINES_NO_DEPTH;
    public static RenderPipeline ENERGY_LINES;
    public static RenderPipeline ENERGY_SURFACE;
    public static RenderPipeline ENERGY_SURFACE_NO_DEPTH;
    public static RenderPipeline AURA_SURFACE;
    public static RenderPipeline AURA_SURFACE_NO_DEPTH;
    public static RenderPipeline DIVINE_SENSE_WAVE;
    public static RenderPipeline SPHERICAL_DESTRUCTION_WAVE;
    public static RenderPipeline TRIBULATION_CLOUDS;
    public static RenderPipeline TRIBULATION_CLOUDS_TOP;
    public static RenderPipeline TRIBULATION_WISPS;
    public static RenderPipeline TRIBULATION_STREAMS;
    public static RenderPipeline TRIBULATION_MINECRAFT_CLOUDS;
    public static RenderPipeline TRIBULATION_RIFT;
    public static RenderPipeline TRIBULATION_LIGHTNING;

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

        SPHERICAL_DESTRUCTION_WAVE = RenderPipeline.builder()
                .withLocation(Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "pipeline/spherical_destruction_wave"))
                .withVertexShader(Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "core/screen_effect"))
                .withFragmentShader(Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "core/spherical_destruction_wave"))
                .withSampler(WORLD_DEPTH_SAMPLER)
                .withUniform(SPHERICAL_DESTRUCTION_UNIFORM, UniformType.UNIFORM_BUFFER)
                .withVertexFormat(DefaultVertexFormat.POSITION, VertexFormat.Mode.TRIANGLES)
                .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                .withDepthStencilState(Optional.empty())
                .withCull(false)
                .build();
        event.registerPipeline(SPHERICAL_DESTRUCTION_WAVE);

        //clouds are drawn per pixel by the shader and cut out with discard, so the output is opaque and depth can be written
        TRIBULATION_CLOUDS = tribulationClouds("pipeline/tribulation_clouds", false);
        TRIBULATION_CLOUDS_TOP = tribulationClouds("pipeline/tribulation_clouds_top", true);
        //the wisps are see through layers stacked on each other, so they blend and leave the depth alone
        TRIBULATION_WISPS = RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
                .withLocation(Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "pipeline/tribulation_wisps"))
                .withVertexShader(Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "core/tribulation_wisps"))
                .withFragmentShader(Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "core/tribulation_wisps"))
                .withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS)
                .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false))
                .withCull(false)
                .build();
        //the streams a vortex pulls in while it gathers, see through like the wisps
        TRIBULATION_STREAMS = RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
                .withLocation(Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "pipeline/tribulation_streams"))
                .withVertexShader(Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "core/tribulation_streams"))
                .withFragmentShader(Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "core/tribulation_streams"))
                .withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS)
                .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false))
                .withCull(false)
                .build();
        //minecraft style cloud boxes with their colours baked into the vertices. solid, so they write depth and need no sorting
        TRIBULATION_MINECRAFT_CLOUDS = RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
                .withLocation(Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "pipeline/tribulation_minecraft_clouds"))
                .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, true))
                .withCull(false)
                .build();
        //the tear in the sky at the eye of a minecraft cloud storm, a dark void with a glowing edge drawn over the clouds
        TRIBULATION_RIFT = RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
                .withLocation(Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "pipeline/tribulation_rift"))
                .withVertexShader(Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "core/tribulation_rift"))
                .withFragmentShader(Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "core/tribulation_rift"))
                .withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS)
                .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false))
                .withCull(false)
                .build();
        //same glow as vanilla lightning, without the fog uniform
        TRIBULATION_LIGHTNING = RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
                .withLocation(Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "pipeline/tribulation_lightning"))
                .withColorTargetState(new ColorTargetState(BlendFunction.LIGHTNING))
                .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false))
                .withCull(false)
                .build();
        event.registerPipeline(TRIBULATION_CLOUDS);
        event.registerPipeline(TRIBULATION_CLOUDS_TOP);
        event.registerPipeline(TRIBULATION_WISPS);
        event.registerPipeline(TRIBULATION_STREAMS);
        event.registerPipeline(TRIBULATION_MINECRAFT_CLOUDS);
        event.registerPipeline(TRIBULATION_RIFT);
        event.registerPipeline(TRIBULATION_LIGHTNING);
    }

    private static RenderPipeline tribulationClouds(String location, boolean topLayer) {
        RenderPipeline.Builder builder = RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
                .withLocation(Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, location))
                .withVertexShader(Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "core/tribulation_clouds"))
                .withFragmentShader(Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "core/tribulation_clouds"))
                .withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS)
                .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, true))
                .withCull(false);
        if (topLayer) builder.withShaderDefine("TOP_LAYER");
        return builder.build();
    }
}
