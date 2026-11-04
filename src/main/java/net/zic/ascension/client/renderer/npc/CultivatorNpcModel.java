package net.zic.ascension.client.renderer.npc;

import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.model.DefaultedEntityGeoModel;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.resources.Identifier;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.entities.npc.CultivatorNpcEntity;

public class CultivatorNpcModel extends DefaultedEntityGeoModel<CultivatorNpcEntity> {
    public static final DataTicket<Identifier> TEXTURE = DataTicket.create("ascension_npc_texture", Identifier.class);
    private final String archetype;

    public CultivatorNpcModel(String archetype) {
        super(AscensionCraft.prefix(archetype));
        this.archetype = archetype;
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return renderState.getOrDefaultGeckolibData(TEXTURE,
                AscensionCraft.prefix("textures/entity/npc/" + archetype + "/default.png"));
    }
}
