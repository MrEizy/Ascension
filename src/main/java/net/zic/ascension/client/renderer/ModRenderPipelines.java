package net.zic.ascension.client.renderer;

import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
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

    public static RenderPipeline LINES_NO_DEPTH;

    @SubscribeEvent
    public static void onRegisterRenderPipelines(RegisterRenderPipelinesEvent event) {
        LINES_NO_DEPTH = RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
                .withLocation(Identifier.fromNamespaceAndPath(AscensionCraft.MOD_ID, "pipeline/lines_no_depth"))
                .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                .withDepthStencilState(Optional.empty())
                .build();

        event.registerPipeline(LINES_NO_DEPTH);
    }
}
