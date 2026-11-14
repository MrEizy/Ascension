package net.zic.ascension.common.item.geckolibblockitems;

import com.geckolib.animatable.GeoItem;
import com.geckolib.animatable.client.GeoRenderProvider;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.renderer.GeoItemRenderer;
import com.geckolib.renderer.layer.builtin.AutoGlowingGeoLayer;
import com.geckolib.util.GeckoLibUtil;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.zic.ascension.client.renderer.alchemy.AlchemyFurnaceItemModel;

import java.util.function.Consumer;


public class AlchemyFurnaceItem extends BlockItem implements GeoItem {
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("misc.idle");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public AlchemyFurnaceItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<AlchemyFurnaceItem>("furnace_item", 0,
                state -> state.setAndContinue(IDLE)));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private GeoItemRenderer<AlchemyFurnaceItem> renderer;

            @Override
            public GeoItemRenderer<AlchemyFurnaceItem> getGeoItemRenderer() {
                if (renderer == null) {
                    renderer = new GeoItemRenderer<>(new AlchemyFurnaceItemModel());
                    renderer.withRenderLayer(geoRenderer -> new AutoGlowingGeoLayer<>(geoRenderer));
                }
                return renderer;
            }
        });
    }
}
