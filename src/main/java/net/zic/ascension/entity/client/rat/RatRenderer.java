package net.zic.ascension.entity.client.rat;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.Util;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.entity.RatVariant;
import net.zic.ascension.entity.custom.TreasureRatEntity;

import java.util.Map;

public class RatRenderer extends MobRenderer<TreasureRatEntity, RatModel<TreasureRatEntity>> {
    private static final Map<RatVariant, ResourceLocation> LOCATION_BY_VARIANT =
            Util.make(Maps.newEnumMap(RatVariant.class), map -> {
                map.put(RatVariant.JADE,
                        ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "textures/entity/rat/jade_rat.png"));
                map.put(RatVariant.CYAN,
                        ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "textures/entity/rat/cyan_rat.png"));
                map.put(RatVariant.GOLDEN,
                        ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "textures/entity/rat/golden_rat.png"));
                map.put(RatVariant.CRIMSON,
                        ResourceLocation.fromNamespaceAndPath(AscensionCraft.MOD_ID, "textures/entity/rat/crimson_rat.png"));
            });


    public RatRenderer(EntityRendererProvider.Context context) {
        super(context, new RatModel<>(context.bakeLayer(RatModel.LAYER_LOCATION)), 0.25f);
    }

    @Override
    public ResourceLocation getTextureLocation(TreasureRatEntity treasureRatEntity) {
        return LOCATION_BY_VARIANT.get(treasureRatEntity.getVariant());
    }

    @Override
    public void render(TreasureRatEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        if (entity.isBaby()) {
            poseStack.scale(0.45f, 0.45f, 0.45f);
        } else {
            poseStack.scale(1f, 1f, 1f);
        }
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }
}
