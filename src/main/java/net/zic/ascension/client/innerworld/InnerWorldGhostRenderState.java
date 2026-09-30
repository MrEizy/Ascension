package net.zic.ascension.client.innerworld;

import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.Identifier;

/** Adds the per-ghost skin texture, resolved once in extractRenderState rather than every frame. */
public final class InnerWorldGhostRenderState extends HumanoidRenderState {
    public Identifier skinTexture;
}