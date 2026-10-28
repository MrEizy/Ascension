package net.zic.ascension.client.renderer;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.neoforged.fml.ModList;
import net.zic.ascension.AscensionCraft;

import java.lang.reflect.Method;

/**
 * Tells Iris which of a shader pack's programs draws each of Ascension's own render pipelines (IrisApi.assignPipeline), so they
 * render as part of the pack (its lighting, fog, sorting) instead of through Iris's fallback, which left them broken or as bare
 * outlines. With a pack on, the pack's program replaces our shader, so shader-drawn looks show simplified; without a pack
 * nothing changes. Iris is optional, so its API is reached by reflection and only when it is installed.
 * <p>
 * The fullscreen effects (Divine Sense, Spherical Destruction, the space rift) are left out: they draw after the level
 * (RenderLevelStageEvent.AfterLevel), past the pack's passes, and already work with packs.
 */
public final class IrisPipelineCompat {
    private static final String IRIS_API = "net.irisshaders.iris.api.v0.IrisApi";
    private static final String IRIS_PROGRAM = "net.irisshaders.iris.api.v0.IrisProgram";
    // IrisApi.getInstance() and its isShaderPackInUse, looked up once; lookupFailed stops retrying after an error.
    private static Object irisApi;
    private static Method shaderPackInUse;
    private static boolean lookupFailed;
    // The last answer and when it was asked, so per-vertex callers don't each reflect into Iris.
    private static final long CACHE_MILLIS = 250L;
    private static boolean cachedInUse;
    private static boolean cached;
    private static long cachedAt;

    private IrisPipelineCompat() {
    }

    /**
     * Whether an Iris shader pack is in use (asked at most every CACHE_MILLIS); false without Iris. Cheap enough to call per
     * vertex.
     */
    public static boolean isShaderPackInUse() {
        long now = net.minecraft.util.Util.getMillis();
        if (cached && now - cachedAt < CACHE_MILLIS) {
            return cachedInUse;
        }
        cachedInUse = askIris();
        cachedAt = now;
        cached = true;
        return cachedInUse;
    }

    private static boolean askIris() {
        if (lookupFailed || !ModList.get().isLoaded("iris")) {
            return false;
        }
        try {
            if (shaderPackInUse == null) {
                Class<?> apiClass = Class.forName(IRIS_API);
                irisApi = apiClass.getMethod("getInstance").invoke(null);
                shaderPackInUse = apiClass.getMethod("isShaderPackInUse");
            }
            return (boolean) shaderPackInUse.invoke(irisApi);
        } catch (Exception | LinkageError exception) {
            lookupFailed = true;
            AscensionCraft.LOGGER.warn("Could not ask Iris whether a shader pack is in use", exception);
            return false;
        }
    }

    static void assignPipelines() {
        if (!ModList.get().isLoaded("iris")) {
            return;
        }
        try {
            Class<?> apiClass = Class.forName(IRIS_API);
            Class<?> programClass = Class.forName(IRIS_PROGRAM);
            Object api = apiClass.getMethod("getInstance").invoke(null);
            Method assign = apiClass.getMethod("assignPipeline", RenderPipeline.class, programClass);
            Assigner assigner = (pipeline, program) -> {
                if (pipeline != null) {
                    assign.invoke(api, pipeline, programValue(programClass, program));
                }
            };
            // Untextured geometry: the shell looks, specks, glows, auras and lines.
            assigner.assign(ModRenderPipelines.QI_COATING, "BASIC");
            assigner.assign(ModRenderPipelines.QI_COATING_DEPTH, "BASIC");
            assigner.assign(ModRenderPipelines.QI_COATING_SPECKS, "BASIC");
            assigner.assign(ModRenderPipelines.ENERGY_SURFACE, "BASIC");
            assigner.assign(ModRenderPipelines.ENERGY_SURFACE_NO_DEPTH, "BASIC");
            assigner.assign(ModRenderPipelines.AURA_SURFACE, "BASIC");
            assigner.assign(ModRenderPipelines.AURA_SURFACE_NO_DEPTH, "BASIC");
            assigner.assign(ModRenderPipelines.LINES_NO_DEPTH, "LINES");
            assigner.assign(ModRenderPipelines.ENERGY_LINES, "LINES");
            // Texture shells: textured entity geometry that binds the overlay and lightmap the pack's entity programs sample.
            assigner.assign(ModRenderPipelines.QI_COATING_TEXTURE_EMISSIVE, "EMISSIVE_ENTITIES");
            // Not assigned: the skin looks (QI_COATING_SKIN*) and the sword looks (SWORD_QI). The pack's entity programs sample
            // the overlay texture (Sampler1), which their render types don't bind, so assigning them crashed with "Missing sampler
            // Sampler1" (first-person ice hand); they also carry coating data in the vertex colour (and the sword looks in UV2),
            // which the pack would read as colour and light. Instead, while a pack is in use they draw through vanilla's entity
            // render type in their look's tint (QiCoatingLayer.submitSkinLimb, SwordQiRenderer.submitShaderPackTinted), and the
            // qi_coating vertices carry their style's tint (QiCoatingLooks.shellShaderPackColor).
            AscensionCraft.LOGGER.info("Assigned Ascension's render pipelines to Iris programs");
        } catch (Exception | LinkageError exception) {
            AscensionCraft.LOGGER.warn("Could not assign Ascension's render pipelines to Iris programs", exception);
        }
    }

    private static Object programValue(Class<?> programClass, String name) {
        for (Object constant : programClass.getEnumConstants()) {
            if (((Enum<?>) constant).name().equals(name)) {
                return constant;
            }
        }
        throw new IllegalArgumentException("Iris has no program " + name);
    }

    @FunctionalInterface
    private interface Assigner {
        void assign(RenderPipeline pipeline, String program) throws Exception;
    }
}
