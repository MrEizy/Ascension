package net.zic.ascension.client.renderer;

import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;

import static net.minecraft.client.renderer.rendertype.OutputTarget.MAIN_TARGET;

public class ModRenderTypes {

    private static RenderType linesNoDepth;
    private static RenderType energyLines;
    private static RenderType energySurface;
    private static RenderType energySurfaceNoDepth;
    private static RenderType auraSurface;
    private static RenderType auraSurfaceNoDepth;

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
}