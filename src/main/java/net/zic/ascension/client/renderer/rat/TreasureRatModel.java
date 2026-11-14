package net.zic.ascension.client.renderer.rat;

import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.DefaultedEntityGeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.entities.rat.RatVariant;
import net.zic.ascension.common.entities.rat.TreasureRatEntity;

public class TreasureRatModel extends DefaultedEntityGeoModel<TreasureRatEntity> {
    public static final DataTicket<Integer> VARIANT = DataTicket.create("ascension_treasure_rat_variant", Integer.class);

    public TreasureRatModel() {
        super(AscensionCraft.prefix("treasure_rat"));
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        RatVariant variant = RatVariant.byId(renderState.getOrDefaultGeckolibData(VARIANT, 0));
        return switch (variant) {
            case JADE -> AscensionCraft.prefix("textures/entity/rat/jade_rat.png");
            case CYAN -> AscensionCraft.prefix("textures/entity/rat/cyan_rat.png");
            case GOLDEN -> AscensionCraft.prefix("textures/entity/rat/golden_rat.png");
            case CRIMSON -> AscensionCraft.prefix("textures/entity/rat/crimson_rat.png");
        };
    }
}
