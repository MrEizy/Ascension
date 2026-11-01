package net.zic.ascension.client.renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.state.LightmapRenderState;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * The colour vanilla's lightmap gives packed light coordinates this frame, worked out on the CPU the way lightmap.fsh does, for
 * shaders that don't sample the lightmap themselves but should still darken at night and in caves like blocks do (the Life
 * coating's vines, QiCoatingLooks.ShellLook.foliage).
 */
public final class LightmapColor {
    private LightmapColor() {
    }

    /**
     * Opaque white times the light, for packed light coordinates; full white if the lightmap hasn't been worked out yet.
     */
    public static int of(int packedLight) {
        LightmapRenderState state = Minecraft.getInstance().gameRenderer.getGameRenderState().lightmapRenderState;
        if (state.ambientColor == null || state.skyLightColor == null || state.blockLightTint == null) {
            return 0xFFFFFFFF;
        }
        float blockLevel = LightCoordsUtil.block(packedLight) / 15.0F;
        float skyLevel = LightCoordsUtil.sky(packedLight) / 15.0F;
        float blockBrightness = brightness(blockLevel) * state.blockFactor;
        float skyBrightness = brightness(skyLevel) * state.skyFactor;

        Vector3f color = new Vector3f(state.ambientColor);
        if (state.nightVisionColor != null) {
            color.max(new Vector3f(state.nightVisionColor).mul(state.nightVisionEffectIntensity));
        }
        color.add(new Vector3f(state.skyLightColor).mul(skyBrightness));
        float parabola = (2.0F * blockLevel - 1.0F) * (2.0F * blockLevel - 1.0F);
        Vector3f blockColor = new Vector3f(state.blockLightTint).lerp(new Vector3f(1.0F), 0.9F * parabola);
        color.add(blockColor.mul(blockBrightness));
        color.lerp(new Vector3f(color).mul(0.7F, 0.6F, 0.6F), state.bossOverlayWorldDarkening);
        color.sub(state.darknessEffectScale, state.darknessEffectScale, state.darknessEffectScale);
        color.set(Mth.clamp(color.x, 0.0F, 1.0F), Mth.clamp(color.y, 0.0F, 1.0F), Mth.clamp(color.z, 0.0F, 1.0F));
        color.lerp(notGamma(color), state.brightness);
        return ARGB.colorFromFloat(1.0F, color.x, color.y, color.z);
    }

    private static float brightness(float level) {
        return level / (4.0F - 3.0F * level);
    }

    private static Vector3f notGamma(Vector3fc color) {
        float maxComponent = Math.max(Math.max(color.x(), color.y()), color.z());
        if (maxComponent <= 0.0F) {
            return new Vector3f(color);
        }
        float maxInverted = 1.0F - maxComponent;
        float maxScaled = 1.0F - maxInverted * maxInverted * maxInverted * maxInverted;
        return new Vector3f(color).mul(maxScaled / maxComponent);
    }
}
