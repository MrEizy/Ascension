package net.zic.ascension.client.renderer;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;

import static net.minecraft.client.renderer.rendertype.OutputTarget.MAIN_TARGET;

/**
 * Under an Iris shader pack, the energy surfaces and auras (runtime visuals, drawn inside the pack's passes) use vanilla's
 * lightning render type instead of ours, drawn in their vertex colour: the pack has no program for ours, and Iris's fallback
 * for them asked for an overlay sampler they don't bind ("Missing sampler Sampler1"). Lightning takes position and colour with
 * a glowing blend, and Iris knows it; the UV some of ours write isn't in its format, so the buffer skips it. The Qi Coatings'
 * types have no fallback: under a pack they are drawn after it (AfterShaderPackDraws).
 */
public class ModRenderTypes {

    private static RenderType linesNoDepth;
    private static RenderType energyLines;
    private static RenderType energySurface;
    private static RenderType energySurfaceNoDepth;
    private static RenderType qiCoatingSpecks;
    private static RenderType auraSurface;
    private static RenderType auraSurfaceNoDepth;
    private static RenderType qiCoating;
    private static RenderType qiCoatingDepth;
    private static final Map<Identifier, RenderType[]> SWORD_QI = new HashMap<>();
    private static final Map<Identifier, RenderType> QI_COATING_TEXTURE_EMISSIVE = new HashMap<>();
    private static final Map<Identifier, RenderType> QI_COATING_ENTITY_TRANSLUCENT = new HashMap<>();
    private static final Map<Identifier, RenderType> QI_COATING_ARM_DEPTH = new HashMap<>();
    // Per skin-fitted pipeline, per skin texture.
    private static final Map<RenderPipeline, Map<Identifier, RenderType>> QI_COATING_SKIN = new HashMap<>();

    public static RenderType energyLines() {
        if (energyLines == null) {
            energyLines = RenderType.create(
                    "ascension_energy_lines",
                    RenderSetup.builder(ModRenderPipelines.ENERGY_LINES)
                            .setOutputTarget(MAIN_TARGET)
                            .createRenderSetup()
            );
        }
        return energyLines;
    }

    public static RenderType linesNoDepth() {
        if (linesNoDepth == null) {
            linesNoDepth = RenderType.create(
                    "ascension_lines_no_depth",
                    RenderSetup.builder(ModRenderPipelines.LINES_NO_DEPTH)
                            .setOutputTarget(MAIN_TARGET)
                            .createRenderSetup()
            );
        }
        return linesNoDepth;
    }


    public static RenderType energySurface() {
        if (IrisPipelineCompat.isShaderPackInUse()) {
            return RenderTypes.lightning();
        }
        return energySurfaceDirect();
    }

    // energySurface without the shader pack fallback, for geometry drawn after the pack (AfterShaderPackDraws).
    public static RenderType energySurfaceDirect() {
        if (energySurface == null) {
            energySurface = RenderType.create(
                    "ascension_energy_surface",
                    RenderSetup.builder(ModRenderPipelines.ENERGY_SURFACE)
                            .setOutputTarget(MAIN_TARGET)
                            .createRenderSetup()
            );
        }
        return energySurface;
    }

    // The specks a gathering qi coating draws in (QiCoatingGather).
    public static RenderType qiCoatingSpecks() {
        if (qiCoatingSpecks == null) {
            qiCoatingSpecks = RenderType.create(
                    "ascension_qi_coating_specks",
                    RenderSetup.builder(ModRenderPipelines.QI_COATING_SPECKS)
                            .setOutputTarget(MAIN_TARGET)
                            .sortOnUpload()
                            .createRenderSetup()
            );
        }
        return qiCoatingSpecks;
    }

    public static RenderType auraSurface() {
        if (IrisPipelineCompat.isShaderPackInUse()) {
            return RenderTypes.lightning();
        }
        if (auraSurface == null) {
            auraSurface = RenderType.create(
                    "ascension_aura_surface",
                    RenderSetup.builder(ModRenderPipelines.AURA_SURFACE)
                            .setOutputTarget(MAIN_TARGET)
                            .createRenderSetup()
            );
        }
        return auraSurface;
    }

    public static RenderType auraSurfaceNoDepth() {
        if (IrisPipelineCompat.isShaderPackInUse()) {
            return RenderTypes.lightning();
        }
        if (auraSurfaceNoDepth == null) {
            auraSurfaceNoDepth = RenderType.create(
                    "ascension_aura_surface_no_depth",
                    RenderSetup.builder(ModRenderPipelines.AURA_SURFACE_NO_DEPTH)
                            .setOutputTarget(MAIN_TARGET)
                            .createRenderSetup()
            );
        }
        return auraSurfaceNoDepth;
    }

    public static RenderType energySurfaceNoDepth() {
        if (IrisPipelineCompat.isShaderPackInUse()) {
            return RenderTypes.lightning();
        }
        if (energySurfaceNoDepth == null) {
            energySurfaceNoDepth = RenderType.create(
                    "ascension_energy_surface_no_depth",
                    RenderSetup.builder(ModRenderPipelines.ENERGY_SURFACE_NO_DEPTH)
                            .setOutputTarget(MAIN_TARGET)
                            .createRenderSetup()
            );
        }
        return energySurfaceNoDepth;
    }

    public static RenderType qiCoating() {
        if (qiCoating == null) {
            qiCoating = RenderType.create(
                    "ascension_qi_coating",
                    RenderSetup.builder(ModRenderPipelines.QI_COATING)
                            .sortOnUpload()
                            .setOutputTarget(MAIN_TARGET)
                            .createRenderSetup()
            );
        }
        return qiCoating;
    }

    public static RenderType qiCoatingDepth() {
        if (qiCoatingDepth == null) {
            qiCoatingDepth = RenderType.create(
                    "ascension_qi_coating_depth",
                    RenderSetup.builder(ModRenderPipelines.QI_COATING_DEPTH)
                            .sortOnUpload()
                            .setOutputTarget(MAIN_TARGET)
                            .createRenderSetup()
            );
        }
        return qiCoatingDepth;
    }

    /**
     * An emissive texture shell drawn from the given texture: vanilla's entityTranslucentEmissive, but writing depth.
     */
    public static RenderType qiCoatingTextureEmissive(Identifier texture) {
        return QI_COATING_TEXTURE_EMISSIVE.computeIfAbsent(texture, key -> RenderType.create(
                "ascension_qi_coating_texture_emissive",
                RenderSetup.builder(ModRenderPipelines.QI_COATING_TEXTURE_EMISSIVE)
                        .withTexture("Sampler0", key)
                        .useOverlay()
                        .sortOnUpload()
                        .createRenderSetup()
        ));
    }

    /**
     * Vanilla's entityTranslucent through our own copy of its pipeline, for coating geometry drawn after a shader pack
     * (AfterShaderPackDraws): Iris still swaps vanilla's pipelines for its own programs there, which drew nothing.
     */
    public static RenderType qiCoatingEntityTranslucent(Identifier texture) {
        return QI_COATING_ENTITY_TRANSLUCENT.computeIfAbsent(texture, key -> RenderType.create(
                "ascension_qi_coating_entity_translucent",
                RenderSetup.builder(ModRenderPipelines.QI_COATING_ENTITY_TRANSLUCENT)
                        .withTexture("Sampler0", key)
                        .useLightmap()
                        .useOverlay()
                        .sortOnUpload()
                        .createRenderSetup()
        ));
    }

    /**
     * The first-person arm in depth only (ModRenderPipelines.QI_COATING_ARM_DEPTH), from the given skin texture so its
     * see-through pixels are left out.
     */
    public static RenderType qiCoatingArmDepth(Identifier skin) {
        return QI_COATING_ARM_DEPTH.computeIfAbsent(skin, key -> RenderType.create(
                "ascension_qi_coating_arm_depth",
                RenderSetup.builder(ModRenderPipelines.QI_COATING_ARM_DEPTH)
                        .withTexture("Sampler0", key)
                        .useLightmap()
                        .useOverlay()
                        .createRenderSetup()
        ));
    }

    /**
     * A skin-fitted qi coating (one of ModRenderPipelines' QI_COATING_SKIN pipelines) drawn from the given skin texture.
     */
    public static RenderType qiCoatingSkin(RenderPipeline pipeline, Identifier skin) {
        return QI_COATING_SKIN.computeIfAbsent(pipeline, _ -> new HashMap<>()).computeIfAbsent(skin, key -> RenderType.create(
                "ascension_" + pipeline.getLocation().getPath().substring("pipeline/".length()),
                RenderSetup.builder(pipeline)
                        .withTexture("Sampler0", key)
                        .useLightmap()
                        .sortOnUpload()
                        .setOutputTarget(MAIN_TARGET)
                        .createRenderSetup()
        ));
    }


    /**
     * Sword Qi for quads from the given atlas; see ModRenderPipelines.SWORD_QI for the styles.
     */
    public static RenderType swordQi(int style, Identifier atlas) {
        RenderType[] types = SWORD_QI.computeIfAbsent(atlas, _ -> new RenderType[ModRenderPipelines.SWORD_QI.length]);
        if (types[style] == null) {
            types[style] = RenderType.create(
                    "ascension_sword_qi_" + style,
                    RenderSetup.builder(ModRenderPipelines.SWORD_QI[style])
                            .withTexture("Sampler0", atlas)
                            .sortOnUpload()
                            .setOutputTarget(MAIN_TARGET)
                            .createRenderSetup()
            );
        }
        return types[style];
    }
}
