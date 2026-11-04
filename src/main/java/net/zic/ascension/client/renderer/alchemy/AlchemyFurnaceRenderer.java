
package net.zic.ascension.client.renderer.alchemy;

import com.geckolib.model.DefaultedBlockGeoModel;
import com.geckolib.renderer.GeoBlockRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.layer.builtin.AutoGlowingGeoLayer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.blocks.entity.AlchemyFurnaceBlockEntity;

public class AlchemyFurnaceRenderer<R extends BlockEntityRenderState & GeoRenderState>
        extends GeoBlockRenderer<AlchemyFurnaceBlockEntity, R> {

    public AlchemyFurnaceRenderer(BlockEntityRendererProvider.Context context) {
        super(context, new DefaultedBlockGeoModel<>(
                AscensionCraft.prefix("alchemy_furnace")
        ));

        withRenderLayer(renderer -> new AutoGlowingGeoLayer<>(renderer));
    }
}
