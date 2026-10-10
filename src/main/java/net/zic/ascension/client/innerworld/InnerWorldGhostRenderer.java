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

/**
 * Renders the body left behind in the overworld, translucent, wearing the owner's real skin.
 *
 * Confidence note (unchanged from last pass): using plain HumanoidModel + HumanoidRenderState
 * rather than your PlayerModel/AvatarRenderState pipeline, since that one looked tied to your
 * Avatar interface for real players in the Graveless sources you shared. This compiles cleanly
 * against LivingEntityRenderer's actual contract in your build — getTextureLocation(S) turned
 * out to be the required abstract hook, not getRenderType (I'd guessed wrong on that).
 *
 * The skin texture lookup (InnerWorldSkinTexture) is now a direct, confirmed call against your
 * real PlayerSkin/ClientAsset sources — no reflection. The Steve fallback below stays as a
 * safety net purely for the case body() itself is null (e.g. skin still loading).
 */
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

    /**
     * This is the method that actually crashed sessions: DefaultPlayerSkin.get(entity.ownerId())
     * was called with no null-guard on ownerId. If ownerId is ever null the first time some OTHER
     * client renders your ghost (a sync-timing edge case), DefaultPlayerSkin.get(null) throws —
     * and on a listen/LAN server the host's client crashing takes the whole session down with it.
     * The whole method is now wrapped in try/catch as well, on top of the null-guards, because a
     * crash here is categorically worse than a wrong texture: it should be physically impossible
     * for anything in this method to escape and reach the render thread as an exception.
     */
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
            // Deliberately swallowed: a wrong/missing skin is cosmetic, a render-thread crash is
            // not. state.skinTexture already defaults to FALLBACK_SKIN above.
        }
    }

    @Override
    public Identifier getTextureLocation(InnerWorldGhostRenderState state) {
        return state.skinTexture;
    }

    /**
     * This is standard vanilla EntityRenderer branching (unchanged across many MC versions), not
     * something specific to your build: the glow effect is produced by handing back the OUTLINE
     * render type when the entity is glowing or only visible through walls — my previous version
     * ignored all three flags and always returned the plain translucent type, so the outline pass
     * never had anything to draw and the glow never appeared regardless of entity state.
     */
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