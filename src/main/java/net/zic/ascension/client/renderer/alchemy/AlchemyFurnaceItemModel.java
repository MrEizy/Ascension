package net.zic.ascension.client.renderer.alchemy;

import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.item.geckolibblockitems.AlchemyFurnaceItem;

public class AlchemyFurnaceItemModel extends GeoModel<AlchemyFurnaceItem> {
    private static final Identifier MODEL = AscensionCraft.prefix("block/alchemy_furnace");
    private static final Identifier ANIMATIONS = AscensionCraft.prefix("block/alchemy_furnace");
    private static final Identifier TEXTURE = AscensionCraft.prefix("textures/block/alchemy_furnace.png");

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return MODEL;
    }

    @Override
    public Identifier getAnimationResource(AlchemyFurnaceItem animatable) {
        return ANIMATIONS;
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return TEXTURE;
    }
}
