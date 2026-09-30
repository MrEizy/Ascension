package net.zic.ascension.client.innerworld;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.Identifier;
import net.zic.ascension.impl.core.innerworld.InnerWorldGhost;

/**
 * Renders the body left behind in the overworld, translucent, wearing the owner's real skin.
 *
 * Confidence note (unchanged from last pass): using plain HumanoidModel + HumanoidRenderState
 * rather than your PlayerModel/AvatarRenderState pipeline, since that one looked tied to your
 * Avatar interface for real players in the Graveless sources you shared. This compiles cleanly
 * against LivingEntityRenderer's actual contract in your build — getTextureLocation(S) turned
 * out to be the required abstract hook, not getRenderType (I'd guessed wrong on that).
 *
 * The skin texture lookup goes through InnerWorldSkinTexture (reflection stopgap) since
 * PlayerSkin's real accessor name in your build is still unconfirmed.
 */
public final class InnerWorldGhostRenderer extends LivingEntityRenderer<InnerWorldGhost, InnerWorldGhostRenderState, HumanoidModel<InnerWorldGhostRenderState>> {

    public InnerWorldGhostRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5F);
    }

    @Override
    public InnerWorldGhostRenderState createRenderState() {
        return new InnerWorldGhostRenderState();
    }

    @Override
    public void extractRenderState(InnerWorldGhost entity, InnerWorldGhostRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        var skin = InnerWorldSkinCache.get(entity.ownerId(), entity.ownerName());
        Identifier texture = InnerWorldSkinTexture.resolve(skin);
        if (texture == null) {
            texture = InnerWorldSkinTexture.resolve(DefaultPlayerSkin.get(entity.ownerId()));
        }
        state.skinTexture = texture;
    }

    @Override
    public Identifier getTextureLocation(InnerWorldGhostRenderState state) {
        return state.skinTexture;
    }

    @Override
    public RenderType getRenderType(InnerWorldGhostRenderState state, boolean isVisible, boolean isVisibleToPlayer, boolean isGlowing) {
        return RenderTypes.entityTranslucent(state.skinTexture);
    }
}