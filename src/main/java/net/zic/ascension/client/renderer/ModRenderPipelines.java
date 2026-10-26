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
    public static final String SPACE_RIFT_UNIFORM = "SpaceRift";
    public static final String WORLD_DEPTH_SAMPLER = "WorldDepth";
    public static final String END_SKY_SAMPLER = "EndSky";
    public static final String END_PORTAL_SAMPLER = "EndPortal";
    public static final String SCENE_COLOR_SAMPLER = "SceneColor";

    public static RenderPipeline LINES_NO_DEPTH;
    public static RenderPipeline ENERGY_LINES;
    public static RenderPipeline ENERGY_SURFACE;
    public static RenderPipeline ENERGY_SURFACE_NO_DEPTH;
    public static RenderPipeline AURA_SURFACE;
    public static RenderPipeline AURA_SURFACE_NO_DEPTH;
    public static RenderPipeline QI_COATING;
    public static RenderPipeline QI_COATING_DEPTH;
    public static RenderPipeline QI_COATING_TEXTURE_EMISSIVE;
    public static RenderPipeline QI_COATING_SKIN;
    public static RenderPipeline QI_COATING_SKIN_WIND;
    public static RenderPipeline QI_COATING_SKIN_METAL;
    // Indexed by sword_qi.fsh style (QiCoatingLooks sword looks): 0 starfield, 1 echo, 2 stone, 3 water, 4 lightning, 5 frost,
    // 6 gale, 7 molten, 8 heat.
    public static final RenderPipeline[] SWORD_QI = new RenderPipeline[11];
    public static RenderPipeline DIVINE_SENSE_WAVE;
    public static RenderPipeline SPHERICAL_DESTRUCTION_WAVE;
    public static RenderPipeline SPACE_RIFT;

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

        QI_COATING = RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET, RenderPipelines.GLOBALS_SNIPPET)
                .withLocation(Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "pipeline/qi_coating"))
                .withVertexShader(Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "core/aura"))
                .withFragmentShader(Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "core/qi_coating"))
                .withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS)
                .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                .withCull(false)
                .build();

        // QI_COATING that also writes depth, for near-opaque coatings: without it the skin's sleeve layer, drawn later in the
        // translucent pass, paints over the coating even though it sits underneath.
        QI_COATING_DEPTH = RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET, RenderPipelines.GLOBALS_SNIPPET)
                .withLocation(Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "pipeline/qi_coating_depth"))
                .withVertexShader(Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "core/aura"))
                .withFragmentShader(Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "core/qi_coating"))
                .withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS)
                .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                .withDepthStencilState(DepthStencilState.DEFAULT)
                .withCull(false)
                .build();

        // Emissive texture shells (QiCoatingTextureShells): vanilla's ENTITY_TRANSLUCENT_EMISSIVE, but writing depth like
        // ENTITY_TRANSLUCENT does; without it clouds and other translucent geometry drawn later show through the coating.
        // Biased toward the camera like vanilla's decals, so a layer laid flush on the skin wins over it instead of z-fighting.
        QI_COATING_TEXTURE_EMISSIVE = RenderPipeline.builder(RenderPipelines.ENTITY_EMISSIVE_SNIPPET)
                .withLocation(Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "pipeline/qi_coating_texture_emissive"))
                .withShaderDefine("ALPHA_CUTOUT", 0.1F)
                .withShaderDefine("PER_FACE_LIGHTING")
                .withSampler("Sampler1")
                .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                .withCull(false)
                .withDepthStencilState(new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, true, -1.0F, -10.0F))
                .build();

        // Skin-fitted qi coatings: the coated limbs redrawn from the player's skin (Sampler0) with the coating mixed into its
        // pixels, lit like vanilla entities (Sampler2 is the lightmap). Writes depth like the skin it covers. Ice by default;
        // the defines switch qi_coating_skin.fsh to the Tornado Fist's wind or the metal coatings' molten gold.
        QI_COATING_SKIN = skinPipeline("qi_coating_skin");
        QI_COATING_SKIN_WIND = skinPipeline("qi_coating_skin_wind", "WIND");
        QI_COATING_SKIN_METAL = skinPipeline("qi_coating_skin_metal", "METAL");


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
        event.registerPipeline(QI_COATING);
        event.registerPipeline(QI_COATING_DEPTH);
        event.registerPipeline(QI_COATING_TEXTURE_EMISSIVE);
        event.registerPipeline(QI_COATING_SKIN);
        event.registerPipeline(QI_COATING_SKIN_WIND);
        event.registerPipeline(QI_COATING_SKIN_METAL);

        // One shader, one pipeline per look; the item atlas in Sampler0 masks the effect to the blade's pixels.
        for (int style = 0; style < SWORD_QI.length; style++) {
            RenderPipeline.Builder builder = RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET, RenderPipelines.GLOBALS_SNIPPET)
                    .withLocation(Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "pipeline/sword_qi_" + style))
                    .withVertexShader(Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "core/aura"))
                    .withFragmentShader(Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "core/sword_qi"))
                    .withShaderDefine("STYLE", style)
                    .withSampler("Sampler0")
                    .withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS)
                    .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                    .withCull(false);
            // Every look but the echo redraws the item opaque in place of vanilla's, so it writes depth like the item would;
            // without it, anything drawn later (clouds, other translucent geometry) showed through the item. The echo's
            // see-through copies sit beside the real item and keep the snippet's no-depth-write state.
            if (style != 1) {
                builder.withDepthStencilState(DepthStencilState.DEFAULT);
            }
            SWORD_QI[style] = builder.build();
            event.registerPipeline(SWORD_QI[style]);
        }
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

        SPACE_RIFT = RenderPipeline.builder()
                .withLocation(Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "pipeline/space_rift"))
                .withVertexShader(Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "core/screen_effect"))
                .withFragmentShader(Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "core/space_rift"))
                .withSampler(WORLD_DEPTH_SAMPLER)
                .withSampler(END_SKY_SAMPLER)
                .withSampler(END_PORTAL_SAMPLER)
                .withSampler(SCENE_COLOR_SAMPLER)
                .withUniform(SPACE_RIFT_UNIFORM, UniformType.UNIFORM_BUFFER)
                .withVertexFormat(DefaultVertexFormat.POSITION, VertexFormat.Mode.TRIANGLES)
                .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                .withDepthStencilState(Optional.empty())
                .withCull(false)
                .build();
        event.registerPipeline(SPACE_RIFT);
    }

    /**
     * A skin-fitted qi coating pipeline (qi_coating_skin), with each of `defines` set to 1.
     */
    private static RenderPipeline skinPipeline(String name, String... defines) {
        RenderPipeline.Builder builder = RenderPipeline.builder(RenderPipelines.ENTITY_SNIPPET, RenderPipelines.GLOBALS_SNIPPET)
                .withLocation(Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "pipeline/" + name))
                .withVertexShader(Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "core/qi_coating_skin"))
                .withFragmentShader(Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "core/qi_coating_skin"))
                .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                .withCull(false);
        for (String define : defines) {
            builder.withShaderDefine(define, 1);
        }
        return builder.build();
    }
}
