package net.zic.ascension.mixins.client;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.world.item.ItemDisplayContext;
import net.zic.ascension.client.renderer.SwordQiRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

/**
 * Gives SwordQiRenderer each held item layer's quads, already in the item's transformed pose, and lets it replace them.
 */
@Mixin(targets = "net.minecraft.client.renderer.item.ItemStackRenderState$LayerRenderState")
public abstract class ItemLayerSwordQiMixin {
    @WrapWithCondition(
            method = "submit",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitItem(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemDisplayContext;III[ILjava/util/List;Lnet/minecraft/client/renderer/item/ItemStackRenderState$FoilType;)V"
            )
    )
    private boolean ascension$applySwordQi(SubmitNodeCollector submitNodeCollector, PoseStack poseStack, ItemDisplayContext displayContext,
                                           int lightCoords, int overlayCoords, int outlineColor, int[] tintLayers, List<BakedQuad> quads,
                                           ItemStackRenderState.FoilType foilType) {
        return SwordQiRenderer.submit(submitNodeCollector, poseStack, quads);
    }
}
