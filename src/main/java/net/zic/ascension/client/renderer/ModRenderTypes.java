package net.zic.ascension.client.renderer;

import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;

import static net.minecraft.client.renderer.rendertype.OutputTarget.MAIN_TARGET;

public class ModRenderTypes {

    private static RenderType linesNoDepth;
    private static RenderType energyLines;

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
}