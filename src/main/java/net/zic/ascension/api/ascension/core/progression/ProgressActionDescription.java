package net.zic.ascension.api.ascension.core.progression;

import net.minecraft.network.chat.Component;

import java.util.Objects;

public record ProgressActionDescription(Component label, Component value, Tone tone) {
    public ProgressActionDescription {
        Objects.requireNonNull(label, "label");
        Objects.requireNonNull(value, "value");
        Objects.requireNonNull(tone, "tone");
    }

    public static ProgressActionDescription numeric(Component label, Component value, double signedValue) {
        return new ProgressActionDescription(label, value, Tone.fromSignedValue(signedValue));
    }

    public enum Tone {
        POSITIVE,
        NEGATIVE,
        SPECIAL,
        NEUTRAL;

        public static Tone fromSignedValue(double value) {
            if (value > 0.0) {
                return POSITIVE;
            }
            if (value < 0.0) {
                return NEGATIVE;
            }
            return NEUTRAL;
        }
    }
}
