package net.zic.ascension.client.renderer.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.HumanoidArm;

/**
 * What a coating effect (a coating's {@code effects}, ids in QiCoatingEffects) does on the client beyond the coating's looks.
 * QiCoatingLayer keeps one per effect id (registerEffect) and calls it at each of these points for the coatings that add it;
 * every method does nothing unless the effect needs it.
 */
public interface QiCoatingEffect {
    /**
     * Each client tick, for every player, with whether a coating they show adds this effect (so it can forget them when not).
     */
    default void tickPlayer(ClientLevel level, AbstractClientPlayer player, boolean active) {
    }

    /**
     * Each client tick, once every player has had theirs.
     */
    default void tickLevel(ClientLevel level) {
    }

    /**
     * How opaque the player's own model is drawn while it has this effect (1 as normal). Below 1, vanilla's first person hand
     * is left out too, and the effect draws its own in submitArm.
     */
    default float bodyAlpha() {
        return 1.0F;
    }

    /**
     * Draws with a whole-body coating in third person, in the layer's model space.
     */
    default void submitBody(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, AvatarRenderState state) {
    }

    /**
     * Draws with one arm of a coating (a hands coating in third person, or any coating in first person), the pose stack in the
     * arm's local space.
     */
    default void submitArm(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, Arm arm) {
    }

    /**
     * The arm being drawn: which, its skin (slim, sleeve shown, texture, light) and how far the coating has faded in (0 to 1;
     * 1 unless its gather fades it in).
     */
    record Arm(HumanoidArm side, boolean slim, boolean sleeve, Identifier skin, int lightCoords, int overlayCoords, float fadeIn) {
    }
}
