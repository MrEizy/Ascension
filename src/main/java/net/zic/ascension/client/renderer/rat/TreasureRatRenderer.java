package net.zic.ascension.client.renderer.rat;

import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.zic.ascension.common.entities.rat.TreasureRatEntity;

public class TreasureRatRenderer<R extends LivingEntityRenderState & GeoRenderState>
        extends GeoEntityRenderer<TreasureRatEntity, R> {

    public TreasureRatRenderer(EntityRendererProvider.Context context) {
        super(context, new TreasureRatModel());
    }

    @Override
    public void addRenderData(TreasureRatEntity rat, Void relatedObject, R renderState, float partialTick) {
        renderState.addGeckolibData(TreasureRatModel.VARIANT, rat.getVariant().getId());
    }

    @Override
    public void scaleModelForRender(RenderPassInfo<R> renderPassInfo, float widthScale, float heightScale) {
        boolean baby = renderPassInfo.renderState().isBaby;
        float scale = baby ? 0.45f : 1.0f;
        super.scaleModelForRender(renderPassInfo, widthScale * scale, heightScale * scale);
    }
}
