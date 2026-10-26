package net.zic.ascension.client.renderer;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;

import static net.minecraft.client.renderer.rendertype.OutputTarget.MAIN_TARGET;

public class ModRenderTypes {

    private static RenderType linesNoDepth;
    private static RenderType energyLines;
    private static RenderType energySurface;
    private static RenderType energySurfaceNoDepth;
    private static RenderType auraSurface;
    private static RenderType auraSurfaceNoDepth;
    private static RenderType qiCoating;
    private static RenderType qiCoatingDepth;
    private static final Map<Identifier, RenderType[]> SWORD_QI = new HashMap<>();
    private static final Map<Identifier, RenderType> QI_COATING_TEXTURE_EMISSIVE = new HashMap<>();
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

    public static RenderType auraSurface() {
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
     * A skin-fitted qi coating (one of ModRenderPipelines' QI_COATING_SKIN pipelines) drawn from the given skin texture.
     */
    public static RenderType qiCoatingSkin(RenderPipeline pipeline, Identifier skin) {
        return QI_COATING_SKIN.computeIfAbsent(pipeline, key -> new HashMap<>()).computeIfAbsent(skin, key -> RenderType.create(
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
        RenderType[] types = SWORD_QI.computeIfAbsent(atlas, key -> new RenderType[ModRenderPipelines.SWORD_QI.length]);
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
