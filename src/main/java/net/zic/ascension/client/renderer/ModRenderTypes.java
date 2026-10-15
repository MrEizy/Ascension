package net.zic.ascension.client.renderer;

import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.TextureTransform;
import org.joml.Matrix4f;

import static net.minecraft.client.renderer.rendertype.OutputTarget.MAIN_TARGET;

public class ModRenderTypes {

    private static RenderType linesNoDepth;
    private static RenderType energyLines;
    private static RenderType energySurface;
    private static RenderType energySurfaceNoDepth;
    private static RenderType auraSurface;
    private static RenderType auraSurfaceNoDepth;
    //set by the tribulation cloud renderer right before each sheet is drawn, the shader reads them from the texture matrix translation.
    //pixels around the circle, and how far the billows have flowed towards the eye
    public static float tribulationCloudPixels = 128.0F;
    public static float tribulationCloudFlow;
    //full thickness of the storm in blocks
    public static float tribulationCloudThickness;
    //cloud shadow, base, highlight then eye shadow, base, highlight, each rgb packed into one float (exact below 2^24).
    //read from TextureMat[0][0..2] and TextureMat[1][0..2]
    public static final float[] tribulationCloudColors = new float[6];
    private static final TextureTransform TRIBULATION_CLOUD_FLOW = new TextureTransform(
            "ascension_tribulation_cloud_flow",
            () -> new Matrix4f().translation(tribulationCloudPixels, tribulationCloudFlow, tribulationCloudThickness)
                    .m00(tribulationCloudColors[0]).m01(tribulationCloudColors[1]).m02(tribulationCloudColors[2])
                    .m10(tribulationCloudColors[3]).m11(tribulationCloudColors[4]).m12(tribulationCloudColors[5])
    );

    private static RenderType tribulationClouds;
    private static RenderType tribulationCloudsTop;
    private static RenderType tribulationLightning;

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

    public static RenderType tribulationClouds() {
        if (tribulationClouds == null) {
            tribulationClouds = RenderType.create(
                    "ascension_tribulation_clouds",
                    RenderSetup.builder(ModRenderPipelines.TRIBULATION_CLOUDS)
                            .setOutputTarget(MAIN_TARGET)
                            .setTextureTransform(TRIBULATION_CLOUD_FLOW)
                            .createRenderSetup()
            );
        }
        return tribulationClouds;
    }

    public static RenderType tribulationCloudsTop() {
        if (tribulationCloudsTop == null) {
            tribulationCloudsTop = RenderType.create(
                    "ascension_tribulation_clouds_top",
                    RenderSetup.builder(ModRenderPipelines.TRIBULATION_CLOUDS_TOP)
                            .setOutputTarget(MAIN_TARGET)
                            .setTextureTransform(TRIBULATION_CLOUD_FLOW)
                            .createRenderSetup()
            );
        }
        return tribulationCloudsTop;
    }

    public static RenderType tribulationLightning() {
        if (tribulationLightning == null) {
            tribulationLightning = RenderType.create(
                    "ascension_tribulation_lightning",
                    RenderSetup.builder(ModRenderPipelines.TRIBULATION_LIGHTNING)
                            .setOutputTarget(MAIN_TARGET)
                            .sortOnUpload()
                            .createRenderSetup()
            );
        }
        return tribulationLightning;
    }
}
