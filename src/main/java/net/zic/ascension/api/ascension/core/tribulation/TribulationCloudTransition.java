package net.zic.ascension.api.ascension.core.tribulation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.StringRepresentable;

/**
 * how a tribulation storm gathers or breaks up.
 * the style describes where the cloud is at a given formation, forming plays it from 0 to 1 and dissipating from 1 back to 0
 * @param ticks how long it takes, 0 is instant
 * @param style which part of the storm exists first while forming, and last while dissipating
 * @param easing the curve the progress follows over the ticks
 * @param patchiness how broken up and ragged the edge of the gathered cloud is
 * @param closedAt the formation at which the cloud is complete, with no holes left
 * @param eyeAt the formation at which the eye starts to glow
 * @param lightningAt the formation above which lightning crawls through the clouds
 * @param spinStart how fast the storm turns at formation 0, as a fraction of its rotation speed
 */
public record TribulationCloudTransition(int ticks, Style style, Easing easing, float patchiness, float closedAt, float eyeAt,
                                         float lightningAt, float spinStart) {

    //every field is optional and falls back to the matching one in defaults
    public static Codec<TribulationCloudTransition> codec(TribulationCloudTransition defaults) {
        return RecordCodecBuilder.create(
                instance -> instance.group(
                        Codec.intRange(0, 6000).optionalFieldOf("ticks", defaults.ticks()).forGetter(TribulationCloudTransition::ticks),
                        Style.CODEC.optionalFieldOf("style", defaults.style()).forGetter(TribulationCloudTransition::style),
                        Easing.CODEC.optionalFieldOf("easing", defaults.easing()).forGetter(TribulationCloudTransition::easing),
                        Codec.floatRange(0.0F, 1.0F).optionalFieldOf("patchiness", defaults.patchiness()).forGetter(TribulationCloudTransition::patchiness),
                        Codec.floatRange(0.01F, 1.0F).optionalFieldOf("closed_at", defaults.closedAt()).forGetter(TribulationCloudTransition::closedAt),
                        Codec.floatRange(0.0F, 1.0F).optionalFieldOf("eye_at", defaults.eyeAt()).forGetter(TribulationCloudTransition::eyeAt),
                        Codec.floatRange(0.0F, 1.0F).optionalFieldOf("lightning_at", defaults.lightningAt()).forGetter(TribulationCloudTransition::lightningAt),
                        Codec.floatRange(0.0F, 1.0F).optionalFieldOf("spin_start", defaults.spinStart()).forGetter(TribulationCloudTransition::spinStart)
                ).apply(instance, TribulationCloudTransition::new)
        );
    }

    //the formation after the raw 0 to 1 progress of this transition has been eased
    public float ease(float progress) {
        return easing.apply(Math.clamp(progress, 0.0F, 1.0F));
    }

    public enum Style implements StringRepresentable {
        //the eyewall and band cores first, then the thin cloud between them
        CORES("cores"),
        //from the outer edge inward, so the eye is last
        RIM("rim"),
        //outward from the eye, so the rim is last
        EYE("eye"),
        //random patches everywhere that merge
        SCATTER("scatter"),
        //a dot of light grows into the eye, then clouds gather at the rim and are drawn inward into the spin.
        //breaking up is not this played backwards: the eye goes out first, then the clouds thin away
        VORTEX("vortex");

        public static final Codec<Style> CODEC = StringRepresentable.fromEnum(Style::values);
        private final String name;

        Style(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public enum Easing implements StringRepresentable {
        LINEAR("linear"),
        //slow start, fast finish
        EASE_IN("ease_in"),
        //fast start, slow finish
        EASE_OUT("ease_out"),
        //slow at both ends
        SMOOTH("smooth");

        public static final Codec<Easing> CODEC = StringRepresentable.fromEnum(Easing::values);
        private final String name;

        Easing(String name) {
            this.name = name;
        }

        public float apply(float t) {
            return switch (this) {
                case LINEAR -> t;
                case EASE_IN -> t * t;
                case EASE_OUT -> 1.0F - (1.0F - t) * (1.0F - t);
                case SMOOTH -> t * t * (3.0F - 2.0F * t);
            };
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
