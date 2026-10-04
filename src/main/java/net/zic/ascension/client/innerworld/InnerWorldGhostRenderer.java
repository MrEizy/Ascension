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

import java.util.UUID;


public final class InnerWorldGhostRenderer extends LivingEntityRenderer<InnerWorldGhost, InnerWorldGhostRenderState, HumanoidModel<InnerWorldGhostRenderState>> {

    public InnerWorldGhostRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5F);
    }

    @Override
    public InnerWorldGhostRenderState createRenderState() {
        return new InnerWorldGhostRenderState();
    }

    /** Vanilla's own Steve texture — guaranteed to exist in every build. Last-resort only. */
    private static final Identifier FALLBACK_SKIN = Identifier.withDefaultNamespace("textures/entity/player/wide/steve.png");


    @Override
    public void extractRenderState(InnerWorldGhost entity, InnerWorldGhostRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.skinTexture = FALLBACK_SKIN;
        try {
            UUID ownerId = entity.ownerId();
            var skin = InnerWorldSkinCache.get(ownerId, entity.ownerName());
            Identifier texture = InnerWorldSkinTexture.resolve(skin);
            if (texture == null && ownerId != null) {
                texture = InnerWorldSkinTexture.resolve(DefaultPlayerSkin.get(ownerId));
            }
            if (texture != null) {
                state.skinTexture = texture;
            }
        } catch (Exception e) {

        }
    }

    @Override
    public Identifier getTextureLocation(InnerWorldGhostRenderState state) {
        return state.skinTexture;
    }

    @Override
    public RenderType getRenderType(InnerWorldGhostRenderState state, boolean isVisible, boolean isVisibleToPlayer, boolean isGlowing) {
        Identifier texture = state.skinTexture;
        if (isVisible) {
            return RenderTypes.entityTranslucent(texture);
        }
        if (isVisibleToPlayer || isGlowing) {
            return RenderTypes.outline(texture);
        }
        return null;
    }
}