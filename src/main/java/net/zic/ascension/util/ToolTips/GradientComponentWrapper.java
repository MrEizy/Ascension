package net.zic.ascension.util.ToolTips;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class GradientComponentWrapper {
    private final String text;
    private float hue = 0.0f;
    private float speed = 0.01f;
    private boolean useDynamicHue = false;
    private static float globalTime = 0.0f;

    private GradientComponentWrapper(String text) {
        this.text = text;
    }

    public static GradientComponentWrapper of(String text) {
        return new GradientComponentWrapper(text);
    }

    public static GradientComponentWrapper translatable(String key) {
        return new GradientComponentWrapper(Component.translatable(key).getString());
    }

    /**
     * Set the base hue for the gradient (0.0 - 1.0)
     */
    public GradientComponentWrapper hue(float hue) {
        this.hue = hue;
        return this;
    }

    /**
     * Set the speed of the color transition
     */
    public GradientComponentWrapper speed(float speed) {
        this.speed = speed;
        return this;
    }

    /**
     * Enable dynamic hue that updates over time
     */
    public GradientComponentWrapper dynamic() {
        this.useDynamicHue = true;
        return this;
    }

    /**
     * Build the final component with gradient applied
     */
    public MutableComponent build() {
        float currentHue = useDynamicHue ? getUpdatedHue() : hue;
        return ToolTipsGradient.RGBEachLetter(currentHue, text, speed);
    }

    /**
     * Get updated hue that increments each call (like your original code)
     */
    private float getUpdatedHue() {
        globalTime += 0.001f;
        if (globalTime > 1.0f) globalTime = 0;
        return globalTime;
    }

    /**
     * Reset the global time (useful if you want to sync animations)
     */
    public static void resetTime() {
        globalTime = 0.0f;
    }

    /**
     * Set the global time manually
     */
    public static void setTime(float time) {
        globalTime = time;
    }

    /**
     * Get the current global time
     */
    public static float getTime() {
        return globalTime;
    }

    // Convenience methods
    public static MutableComponent withRGB(String text) {
        return of(text).dynamic().build();
    }

    public static MutableComponent withRGB(String text, float speed) {
        return of(text).dynamic().speed(speed).build();
    }

    public static MutableComponent withStaticRGB(String text, float hue) {
        return of(text).hue(hue).build();
    }
}
