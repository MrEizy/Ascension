package net.zic.ascension.client.renderer.npc;

import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.zic.ascension.common.entities.npc.CultivatorNpcEntity;

public class CultivatorNpcRenderer<R extends LivingEntityRenderState & GeoRenderState>
        extends GeoEntityRenderer<CultivatorNpcEntity, R> {
    public CultivatorNpcRenderer(EntityRendererProvider.Context context, String archetype) {
        super(context, new CultivatorNpcModel(archetype));
    }

    @Override
    public void addRenderData(CultivatorNpcEntity npc, Void relatedObject, R state, float partialTick) {
        Identifier texture = NpcPlayerSkinResolver.resolve(npc.getCustomName());
        if (texture == null) {
            texture = net.zic.ascension.AscensionCraft.prefix("textures/entity/npc/"
                    + npc.getNpcType() + "/" + npc.getSkin() + ".png");
        }
        state.addGeckolibData(CultivatorNpcModel.TEXTURE, texture);
    }
}
