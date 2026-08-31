package net.zic.ascension.api.ascension.core.runtime;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.phys.Vec3;
import net.zic.ascension.api.ascension.datapack.CodecHelpers;

import java.util.List;
import java.util.Optional;

public record RuntimeVisualDefinition(List<Element> elements) {
    public static final Codec<RuntimeVisualDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Element.CODEC.listOf().fieldOf("elements").forGetter(RuntimeVisualDefinition::elements)
    ).apply(instance, RuntimeVisualDefinition::new));

    public RuntimeVisualDefinition {
        elements = elements == null ? List.of() : List.copyOf(elements);
    }

    public record Element(
            Identifier type,
            PositionMode position,
            Transform transform,
            Appearance appearance,
            Geometry geometry,
            Motion motion,
            Resources resources
    ) {
        public static final Codec<Element> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Identifier.CODEC.fieldOf("type").forGetter(Element::type),
                PositionMode.CODEC.optionalFieldOf("position", PositionMode.ORIGIN).forGetter(Element::position),
                Transform.MAP_CODEC.forGetter(Element::transform),
                Appearance.MAP_CODEC.forGetter(Element::appearance),
                Geometry.MAP_CODEC.forGetter(Element::geometry),
                Motion.MAP_CODEC.forGetter(Element::motion),
                Resources.MAP_CODEC.forGetter(Element::resources)
        ).apply(instance, Element::new));

        public Element {
            type = type == null ? Types.RING : type;
            position = position == null ? PositionMode.ORIGIN : position;
            transform = transform == null ? Transform.DEFAULT : transform;
            appearance = appearance == null ? Appearance.DEFAULT : appearance;
            geometry = geometry == null ? Geometry.DEFAULT : geometry;
            motion = motion == null ? Motion.DEFAULT : motion;
            resources = resources == null ? Resources.EMPTY : resources;
        }
    }

    public record Transform(Vec3 offset, Vec3 rotation, VisualValue scale) {
        public static final Transform DEFAULT = new Transform(Vec3.ZERO, Vec3.ZERO, VisualValue.constant(1.0D));
        public static final com.mojang.serialization.MapCodec<Transform> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                CodecHelpers.VEC3.optionalFieldOf("offset", Vec3.ZERO).forGetter(Transform::offset),
                CodecHelpers.VEC3.optionalFieldOf("rotation", Vec3.ZERO).forGetter(Transform::rotation),
                VisualValue.CODEC.optionalFieldOf("scale", VisualValue.constant(1.0D)).forGetter(Transform::scale)
        ).apply(instance, Transform::new));

        public Transform {
            offset = offset == null ? Vec3.ZERO : offset;
            rotation = rotation == null ? Vec3.ZERO : rotation;
            scale = scale == null ? VisualValue.constant(1.0D) : scale;
        }
    }

    public record Appearance(VisualColor tint, VisualColor secondaryTint, float lineWidth, boolean filled, boolean noDepth) {
        public static final Appearance DEFAULT = new Appearance(
                VisualColor.WHITE,
                VisualColor.WHITE,
                1.5F,
                false,
                false
        );
        public static final com.mojang.serialization.MapCodec<Appearance> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                VisualColor.CODEC.optionalFieldOf("tint", DEFAULT.tint()).forGetter(Appearance::tint),
                VisualColor.CODEC.optionalFieldOf("secondary_tint", DEFAULT.secondaryTint()).forGetter(Appearance::secondaryTint),
                Codec.floatRange(0.1F, 16.0F).optionalFieldOf("line_width", 1.5F).forGetter(Appearance::lineWidth),
                Codec.BOOL.optionalFieldOf("filled", false).forGetter(Appearance::filled),
                Codec.BOOL.optionalFieldOf("no_depth", false).forGetter(Appearance::noDepth)
        ).apply(instance, Appearance::new));

        public Appearance {
            tint = tint == null ? DEFAULT.tint() : tint;
            secondaryTint = secondaryTint == null ? tint : secondaryTint;
            lineWidth = Math.clamp(lineWidth, 0.1F, 16.0F);
        }

        public VisualColor resolved(RuntimeVisualState state) {
            float progress = state == null ? 0.0F : state.progress();
            VisualColor local = tint.lerp(secondaryTint, Math.clamp(progress, 0.0F, 1.0F));
            if (state == null) {
                return local;
            }
            VisualColor runtime = VisualColor.fromArgb(state.tint())
                    .lerp(VisualColor.fromArgb(state.secondaryTint()), Math.clamp(progress, 0.0F, 1.0F));
            return local.multiply(runtime);
        }
    }

    public record Geometry(
            VisualValue radius,
            VisualValue innerRadius,
            VisualValue height,
            VisualValue width,
            VisualValue length,
            int segments,
            int count,
            AuraStyle style
    ) {
        public static final Geometry DEFAULT = new Geometry(
                VisualValue.constant(1.0D),
                VisualValue.constant(0.0D),
                VisualValue.constant(1.0D),
                VisualValue.constant(1.0D),
                VisualValue.constant(1.0D),
                24,
                1,
                AuraStyle.FLAME
        );
        public static final com.mojang.serialization.MapCodec<Geometry> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                VisualValue.CODEC.optionalFieldOf("radius", DEFAULT.radius()).forGetter(Geometry::radius),
                VisualValue.CODEC.optionalFieldOf("inner_radius", DEFAULT.innerRadius()).forGetter(Geometry::innerRadius),
                VisualValue.CODEC.optionalFieldOf("height", DEFAULT.height()).forGetter(Geometry::height),
                VisualValue.CODEC.optionalFieldOf("width", DEFAULT.width()).forGetter(Geometry::width),
                VisualValue.CODEC.optionalFieldOf("length", DEFAULT.length()).forGetter(Geometry::length),
                Codec.intRange(3, 128).optionalFieldOf("segments", 24).forGetter(Geometry::segments),
                Codec.intRange(1, 128).optionalFieldOf("count", 1).forGetter(Geometry::count),
                AuraStyle.CODEC.optionalFieldOf("style", AuraStyle.FLAME).forGetter(Geometry::style)
        ).apply(instance, Geometry::new));

        public Geometry(
                VisualValue radius,
                VisualValue innerRadius,
                VisualValue height,
                VisualValue width,
                VisualValue length,
                int segments,
                int count
        ) {
            this(radius, innerRadius, height, width, length, segments, count, AuraStyle.FLAME);
        }

        public Geometry {
            radius = radius == null ? DEFAULT.radius() : radius;
            innerRadius = innerRadius == null ? DEFAULT.innerRadius() : innerRadius;
            height = height == null ? DEFAULT.height() : height;
            width = width == null ? DEFAULT.width() : width;
            length = length == null ? DEFAULT.length() : length;
            segments = Math.clamp(segments, 3, 128);
            count = Math.clamp(count, 1, 128);
            style = style == null ? AuraStyle.FLAME : style;
        }
    }

    public record Motion(double spin, double pulse, double pulseSpeed, double bob, double bobSpeed, int interval, int history) {
        public static final Motion DEFAULT = new Motion(0.0D, 0.0D, 0.1D, 0.0D, 0.1D, 1, 12);
        public static final com.mojang.serialization.MapCodec<Motion> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.DOUBLE.optionalFieldOf("spin", 0.0D).forGetter(Motion::spin),
                Codec.doubleRange(0.0D, 4.0D).optionalFieldOf("pulse", 0.0D).forGetter(Motion::pulse),
                Codec.doubleRange(0.0D, 8.0D).optionalFieldOf("pulse_speed", 0.1D).forGetter(Motion::pulseSpeed),
                Codec.doubleRange(0.0D, 16.0D).optionalFieldOf("bob", 0.0D).forGetter(Motion::bob),
                Codec.doubleRange(0.0D, 8.0D).optionalFieldOf("bob_speed", 0.1D).forGetter(Motion::bobSpeed),
                Codec.intRange(1, 1200).optionalFieldOf("interval", 1).forGetter(Motion::interval),
                Codec.intRange(2, 128).optionalFieldOf("history", 12).forGetter(Motion::history)
        ).apply(instance, Motion::new));

        public Motion {
            pulse = Math.clamp(pulse, 0.0D, 4.0D);
            pulseSpeed = Math.clamp(pulseSpeed, 0.0D, 8.0D);
            bob = Math.clamp(bob, 0.0D, 16.0D);
            bobSpeed = Math.clamp(bobSpeed, 0.0D, 8.0D);
            interval = Math.clamp(interval, 1, 1200);
            history = Math.clamp(history, 2, 128);
        }
    }

    public record Resources(
            Optional<Identifier> texture,
            Optional<Identifier> model,
            Optional<Identifier> particle,
            Optional<Identifier> visual,
            int frames,
            int frameTicks
    ) {
        public static final Resources EMPTY = new Resources(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), 1, 1);
        public static final com.mojang.serialization.MapCodec<Resources> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Identifier.CODEC.optionalFieldOf("texture").forGetter(Resources::texture),
                Identifier.CODEC.optionalFieldOf("model").forGetter(Resources::model),
                Identifier.CODEC.optionalFieldOf("particle").forGetter(Resources::particle),
                Identifier.CODEC.optionalFieldOf("visual").forGetter(Resources::visual),
                Codec.intRange(1, 256).optionalFieldOf("frames", 1).forGetter(Resources::frames),
                Codec.intRange(1, 1200).optionalFieldOf("frame_ticks", 1).forGetter(Resources::frameTicks)
        ).apply(instance, Resources::new));

        public Resources {
            texture = texture == null ? Optional.empty() : texture;
            model = model == null ? Optional.empty() : model;
            particle = particle == null ? Optional.empty() : particle;
            visual = visual == null ? Optional.empty() : visual;
            frames = Math.clamp(frames, 1, 256);
            frameTicks = Math.clamp(frameTicks, 1, 1200);
        }

        public Identifier modelResource() {
            Identifier base = model.orElse(null);
            if (base == null) {
                return null;
            }
            String path = base.getPath();
            if (path.startsWith("models/") && path.endsWith(".json")) {
                return base;
            }
            return Identifier.fromNamespaceAndPath(base.getNamespace(), "models/" + path + ".json");
        }

        public Identifier textureResource(double age) {
            Identifier base = texture.orElse(null);
            if (base == null) {
                return null;
            }
            String path = base.getPath();
            if (path.startsWith("textures/") && path.endsWith(".png")) {
                return base;
            }
            if (frames <= 1) {
                return Identifier.fromNamespaceAndPath(base.getNamespace(), "textures/" + path + ".png");
            }
            int frame = Math.floorMod((int) Math.floor(Math.max(0.0D, age) / frameTicks), frames);
            return Identifier.fromNamespaceAndPath(base.getNamespace(), "textures/" + path + "/" + frame + ".png");
        }
    }

    public record VisualValue(double base, Source source, double scale, double minimum, double maximum) {
        private static final Codec<VisualValue> OBJECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.DOUBLE.optionalFieldOf("base", 0.0D).forGetter(VisualValue::base),
                Source.CODEC.optionalFieldOf("source", Source.CONSTANT).forGetter(VisualValue::source),
                Codec.DOUBLE.optionalFieldOf("scale", 1.0D).forGetter(VisualValue::scale),
                Codec.DOUBLE.optionalFieldOf("minimum", -1.0E9D).forGetter(VisualValue::minimum),
                Codec.DOUBLE.optionalFieldOf("maximum", 1.0E9D).forGetter(VisualValue::maximum)
        ).apply(instance, VisualValue::new));
        public static final Codec<VisualValue> CODEC = Codec.either(Codec.DOUBLE, OBJECT_CODEC)
                .xmap(value -> value.map(VisualValue::constant, entry -> entry), value -> {
                    if (value.source == Source.CONSTANT
                            && value.scale == 1.0D
                            && value.minimum == -1.0E9D
                            && value.maximum == 1.0E9D) {
                        return com.mojang.datafixers.util.Either.left(value.base);
                    }
                    return com.mojang.datafixers.util.Either.right(value);
                });

        public VisualValue {
            source = source == null ? Source.CONSTANT : source;
            if (maximum < minimum) {
                double value = minimum;
                minimum = maximum;
                maximum = value;
            }
        }

        public static VisualValue constant(double value) {
            return new VisualValue(value, Source.CONSTANT, 1.0D, -1.0E9D, 1.0E9D);
        }

        public double resolve(RuntimeVisualState state, double time) {
            double sourceValue = switch (source) {
                case CONSTANT -> 0.0D;
                case PROGRESS -> state.progress();
                case PRIMARY -> state.primaryValue();
                case SECONDARY -> state.secondaryValue();
                case STAGE -> state.stage();
                case TIME -> time;
                case SPEED -> state.offset().length();
                case POINT_COUNT -> state.points().size();
                case LINK_COUNT -> state.links().size();
            };
            return Math.clamp(base + sourceValue * scale, minimum, maximum);
        }
    }

    public record VisualColor(int red, int green, int blue, int alpha) {
        public static final VisualColor WHITE = new VisualColor(255, 255, 255, 255);
        public static final Codec<VisualColor> CODEC = Codec.intRange(0, 255).listOf().comapFlatMap(values -> {
            if (values.size() == 3) {
                return DataResult.success(new VisualColor(values.get(0), values.get(1), values.get(2), 255));
            }
            if (values.size() == 4) {
                return DataResult.success(new VisualColor(values.get(0), values.get(1), values.get(2), values.get(3)));
            }
            return DataResult.error(() -> "Visual tint must contain three or four values");
        }, color -> List.of(color.red, color.green, color.blue, color.alpha));

        public VisualColor {
            red = Math.clamp(red, 0, 255);
            green = Math.clamp(green, 0, 255);
            blue = Math.clamp(blue, 0, 255);
            alpha = Math.clamp(alpha, 0, 255);
        }

        public VisualColor lerp(VisualColor other, float progress) {
            float value = Math.clamp(progress, 0.0F, 1.0F);
            return new VisualColor(
                    Math.round(red + (other.red - red) * value),
                    Math.round(green + (other.green - green) * value),
                    Math.round(blue + (other.blue - blue) * value),
                    Math.round(alpha + (other.alpha - alpha) * value)
            );
        }

        public VisualColor multiply(VisualColor other) {
            return new VisualColor(
                    red * other.red / 255,
                    green * other.green / 255,
                    blue * other.blue / 255,
                    alpha * other.alpha / 255
            );
        }

        public VisualColor withAlpha(int value) {
            return new VisualColor(red, green, blue, value);
        }

        public int argb() {
            return alpha << 24 | red << 16 | green << 8 | blue;
        }

        public static VisualColor fromArgb(int value) {
            return new VisualColor(value >>> 16 & 255, value >>> 8 & 255, value & 255, value >>> 24 & 255);
        }
    }

    public static final class Types {
        public static final Identifier PARTICLES = id("particles");
        public static final Identifier SPRITE = id("sprite");
        public static final Identifier DECAL = id("decal");
        public static final Identifier MODEL = id("model");
        public static final Identifier RING = id("ring");
        public static final Identifier SHELL = id("shell");
        public static final Identifier BEAM = id("beam");
        public static final Identifier ENERGY_BEAM = id("energy_beam");
        public static final Identifier AURA = id("aura");
        public static final Identifier TRAIL = id("trail");
        public static final Identifier AFTERIMAGE = id("afterimage");
        public static final Identifier ENTITY_OVERLAY = id("entity_overlay");
        public static final Identifier CUSTOM = id("custom");
        public static final Identifier COMPOSITE = id("composite");

        private Types() {
        }

        private static Identifier id(String path) {
            return Identifier.fromNamespaceAndPath("ascension", path);
        }
    }

    public enum AuraStyle implements StringRepresentable {
        FLAME("flame"),
        FLOWING("flowing"),
        MIST("mist"),
        STORM("storm");

        public static final Codec<AuraStyle> CODEC = StringRepresentable.fromEnum(AuraStyle::values);
        private final String name;

        AuraStyle(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public enum PositionMode implements StringRepresentable {
        ORIGIN("origin"),
        OWNER("owner"),
        EACH_POINT("each_point");

        public static final Codec<PositionMode> CODEC = StringRepresentable.fromEnum(PositionMode::values);
        private final String name;

        PositionMode(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public enum Source implements StringRepresentable {
        CONSTANT("constant"),
        PROGRESS("progress"),
        PRIMARY("primary"),
        SECONDARY("secondary"),
        STAGE("stage"),
        TIME("time"),
        SPEED("speed"),
        POINT_COUNT("point_count"),
        LINK_COUNT("link_count");

        public static final Codec<Source> CODEC = StringRepresentable.fromEnum(Source::values);
        private final String name;

        Source(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
