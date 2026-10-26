package net.zic.ascension.api.ascension.core.qi_coating;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.phys.Vec3;
import net.zic.ascension.api.ascension.datapack.CodecHelpers;

import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.NullMarked;

/**
 * A Qi Coating, loaded from {@code data/<namespace>/ascension/skill_system/qi_coatings/}. It says where the coating goes and which
 * of the built-in looks draw it (a look is shader code, named in the client's QiCoatingLooks), with everything else about it
 * (shell geometry, how it grows, particles, flags and the named code effects it adds) as data. A runtime visual puts it on its
 * owner with an {@code ascension:custom} element whose {@code resources.visual} is the coating's id; Qi Coating passives show
 * that runtime visual with {@code ascension:persistent_visual}.
 *
 * @param coverage       where the coating goes
 * @param shells         boxes drawn around the limbs, shaded by a shell look
 * @param skin           the coating painted into the player's own skin pixels by a skin look
 * @param sword          the held weapon redrawn by a sword look (coverage {@code weapon} only)
 * @param growMillis     how long the coating takes to grow in when it comes on and to shrink away when it goes off; 0 to just
 *                       appear and vanish
 * @param fadesArm       whether the arm itself fades from opaque at the shoulder to mostly see-through at the hand
 * @param echoes         whether flickering offset copies of the arm (or body, or weapon) are drawn alongside it
 * @param reactsTo       what the coating reacts to: looks with a hit animation play it after these, and an echoing coating
 *                       echoes only for a moment after them (always, if empty), drawing its looks' {@code quiet_look} (if any)
 *                       the rest of the time
 * @param overlay        another coating drawn over this one wherever this one is drawn (its shells only)
 * @param handParticles  particles spawned at the hands while the coating is on
 * @param effects        named effects coded in Java (QiCoatingEffects) that the coating adds
 */
public record QiCoatingDefinition(
        Coverage coverage,
        Optional<Shells> shells,
        Optional<Skin> skin,
        Optional<Sword> sword,
        int growMillis,
        boolean fadesArm,
        boolean echoes,
        List<Reaction> reactsTo,
        Optional<Identifier> overlay,
        List<HandParticles> handParticles,
        List<Identifier> effects
) {
    /**
     * ARGB colours as 8 digit hex strings ({@code #AARRGGBB}), or 6 digits for opaque. HexColorCodec drops the alpha when it
     * encodes, and datapack registries are re-encoded to sync them to clients, so shell colours keep their own codec.
     */
    public static final Codec<Integer> ARGB_CODEC = Codec.STRING.comapFlatMap(
            QiCoatingDefinition::parseArgb,
            color -> String.format("#%08X", color)
    );

    public static final Codec<QiCoatingDefinition> CODEC = RecordCodecBuilder.<QiCoatingDefinition>create(instance -> instance.group(
            Coverage.CODEC.fieldOf("coverage").forGetter(QiCoatingDefinition::coverage),
            Shells.CODEC.optionalFieldOf("shells").forGetter(QiCoatingDefinition::shells),
            Skin.CODEC.optionalFieldOf("skin").forGetter(QiCoatingDefinition::skin),
            Sword.CODEC.optionalFieldOf("sword").forGetter(QiCoatingDefinition::sword),
            Codec.intRange(0, 60000).optionalFieldOf("grow_millis", 0).forGetter(QiCoatingDefinition::growMillis),
            Codec.BOOL.optionalFieldOf("fades_arm", false).forGetter(QiCoatingDefinition::fadesArm),
            Codec.BOOL.optionalFieldOf("echoes", false).forGetter(QiCoatingDefinition::echoes),
            Reaction.CODEC.listOf().optionalFieldOf("reacts_to", List.of()).forGetter(QiCoatingDefinition::reactsTo),
            Identifier.CODEC.optionalFieldOf("overlay").forGetter(QiCoatingDefinition::overlay),
            HandParticles.CODEC.listOf().optionalFieldOf("hand_particles", List.of()).forGetter(QiCoatingDefinition::handParticles),
            Identifier.CODEC.listOf().optionalFieldOf("effects", List.of()).forGetter(QiCoatingDefinition::effects)
    ).apply(instance, QiCoatingDefinition::new)).validate(QiCoatingDefinition::validate);

    public QiCoatingDefinition {
        reactsTo = List.copyOf(reactsTo);
        handParticles = List.copyOf(handParticles);
        effects = List.copyOf(effects);
    }

    private static DataResult<QiCoatingDefinition> validate(QiCoatingDefinition definition) {
        boolean weapon = definition.coverage() == Coverage.WEAPON;
        if (weapon != definition.sword().isPresent()) {
            return DataResult.error(() -> "A qi coating has a \"sword\" look exactly when its coverage is \"weapon\"");
        }
        if (!weapon && definition.shells().isEmpty() && definition.skin().isEmpty()) {
            return DataResult.error(() -> "A qi coating needs \"shells\" or a \"skin\" look (or both)");
        }
        return DataResult.success(definition);
    }

    private static DataResult<Integer> parseArgb(String value) {
        String hex = value.startsWith("#") ? value.substring(1) : value;
        if (hex.length() != 6 && hex.length() != 8) {
            return DataResult.error(() -> "Colour must be a 6 or 8 digit hex code (#RRGGBB or #AARRGGBB), got: " + value);
        }
        try {
            long parsed = Long.parseLong(hex, 16);
            return DataResult.success((int) (hex.length() == 6 ? parsed | 0xFF000000L : parsed));
        } catch (NumberFormatException exception) {
            return DataResult.error(() -> "Invalid hex colour: " + value);
        }
    }

    /**
     * Where a coating goes.
     */
    @NullMarked
    public enum Coverage implements StringRepresentable {
        // The hands and forearms (in third person both arms, in first person the arm on screen).
        HANDS("hands"),
        // The whole body: shells over every part or every limb (Shells.body), or a skin look on the arms and legs.
        BODY("body"),
        // Like BODY, but a skin look also covers the head and torso, pouring on part by part.
        WHOLE_SKIN("whole_skin"),
        // The held weapon, if it is bladed or a polearm.
        WEAPON("weapon");

        public static final Codec<Coverage> CODEC = StringRepresentable.fromEnum(Coverage::values);

        private final String name;

        Coverage(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    /**
     * Something the player does that a coating can react to (reacts_to).
     */
    @NullMarked
    public enum Reaction implements StringRepresentable {
        // The player is hurt (both arms react).
        HURT("hurt"),
        // The player swings (the swinging arm reacts).
        ATTACK("attack");

        public static final Codec<Reaction> CODEC = StringRepresentable.fromEnum(Reaction::values);

        private final String name;

        Reaction(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    /**
     * Boxes drawn around the limbs, shaded by a shell look or painted with an animated texture (exactly one of the two).
     *
     * @param look      the shell look (QiCoatingLooks) that shades them
     * @param quietLook the shell look drawn instead while a coating with a {@code reacts_to} isn't echoing
     * @param texture the animated texture painted on them instead, laid out like a player skin
     * @param layers  the boxes round each arm, from the hand tip up, outermost first so the inner ones draw over it. These are what
     *                a hands coating draws, and what a body coating draws on the arm in first person
     * @param body    how a body coating covers the body in third person; a body coating without it only shows on the arm in
     *                first person
     */
    public record Shells(Optional<Identifier> look, Optional<Identifier> quietLook, Optional<Texture> texture, List<Layer> layers,
                         Optional<Body> body) {
        public static final Codec<Shells> CODEC = RecordCodecBuilder.<Shells>create(instance -> instance.group(
                Identifier.CODEC.optionalFieldOf("look").forGetter(Shells::look),
                Identifier.CODEC.optionalFieldOf("quiet_look").forGetter(Shells::quietLook),
                Texture.CODEC.optionalFieldOf("texture").forGetter(Shells::texture),
                Layer.CODEC.listOf().optionalFieldOf("layers", List.of()).forGetter(Shells::layers),
                Body.CODEC.optionalFieldOf("body").forGetter(Shells::body)
        ).apply(instance, Shells::new)).validate(shells -> shells.look().isPresent() == shells.texture().isPresent()
                ? DataResult.error(() -> "Qi coating shells need either a \"look\" or a \"texture\" (one of the two)")
                : DataResult.success(shells));

        public Shells {
            layers = List.copyOf(layers);
        }

        /**
         * The look to shade the shells with: the quiet look if {@code quiet} and there is one.
         */
        public Optional<Identifier> look(boolean quiet) {
            return quiet && quietLook.isPresent() ? quietLook : look;
        }
    }

    /**
     * An animated texture for shells, laid out like a 64x64 player skin (any multiple of that size works): each box is painted
     * from the skin area of the body part it covers, the outer layer's area for layers marked {@code outer}. The frames are
     * stacked top to bottom in one image, so a 5 frame texture is 64 wide and 320 tall.
     *
     * @param path        the texture, e.g. {@code ascension:textures/qi_coating/jade_fist.png}
     * @param frames      how many frames are stacked in it
     * @param frameMillis how long each frame shows
     * @param emissive    whether it glows at full brightness, like the shader looks; otherwise it is lit like the skin
     */
    public record Texture(Identifier path, int frames, int frameMillis, boolean emissive) {
        public static final Codec<Texture> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Identifier.CODEC.fieldOf("path").forGetter(Texture::path),
                Codec.intRange(1, 256).optionalFieldOf("frames", 1).forGetter(Texture::frames),
                Codec.intRange(1, 60000).optionalFieldOf("frame_millis", 250).forGetter(Texture::frameMillis),
                Codec.BOOL.optionalFieldOf("emissive", true).forGetter(Texture::emissive)
        ).apply(instance, Texture::new));
    }

    /**
     * One box round an arm, in pixels (an arm is 12 long).
     *
     * @param inflate   how far it sits out from the arm on each side
     * @param reach     how far up the arm it goes from the hand tip (12 is the shoulder; 12 + inflate covers the top cap's edge)
     * @param overhang  how far past the hand tip it goes
     * @param color     ARGB tint; its alpha sets how see-through the whole box is
     * @param squareCap whether the fist cap is mapped so a square texel grid on the sides stays square on it (shell looks only)
     * @param outer     texture shells only: whether the box is painted from the skin's outer layer area (the sleeve's) instead
     *                  of the inner one
     */
    public record Layer(float inflate, float reach, float overhang, int color, boolean squareCap, boolean outer) {
        public static final Codec<Layer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.floatRange(0.0F, 8.0F).fieldOf("inflate").forGetter(Layer::inflate),
                Codec.floatRange(0.0F, 16.0F).fieldOf("reach").forGetter(Layer::reach),
                Codec.floatRange(0.0F, 8.0F).optionalFieldOf("overhang", 0.0F).forGetter(Layer::overhang),
                ARGB_CODEC.optionalFieldOf("color", 0xFFFFFFFF).forGetter(Layer::color),
                Codec.BOOL.optionalFieldOf("square_cap", false).forGetter(Layer::squareCap),
                Codec.BOOL.optionalFieldOf("outer", false).forGetter(Layer::outer)
        ).apply(instance, Layer::new));
    }

    /**
     * How a body coating's shells cover the body in third person.
     *
     * @param mode          {@code film}: one box round every part; {@code limbs}: one box round each arm and leg, with the look's
     *                      per-limb pattern
     * @param inflate       how far the boxes sit out from the body, in pixels
     * @param limbReach     film only: how far up each limb the boxes go from the hand or foot, in pixels, with no caps; absent
     *                      for whole limbs
     * @param torsoAndHead  film only: whether the head and torso get a box too
     * @param legsOnly      limbs only: whether only the legs get boxes
     * @param outer         texture shells only: whether the boxes are painted from the skin's outer layer areas
     * @param color         texture shells only: ARGB tint multiplied into the texture; its alpha sets how see-through the boxes are
     */
    public record Body(BodyMode mode, float inflate, Optional<Float> limbReach, boolean torsoAndHead, boolean legsOnly,
                       boolean outer, int color) {
        public static final Codec<Body> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BodyMode.CODEC.fieldOf("mode").forGetter(Body::mode),
                Codec.floatRange(0.0F, 4.0F).optionalFieldOf("inflate", 0.4F).forGetter(Body::inflate),
                Codec.floatRange(0.0F, 12.0F).optionalFieldOf("limb_reach").forGetter(Body::limbReach),
                Codec.BOOL.optionalFieldOf("torso_and_head", true).forGetter(Body::torsoAndHead),
                Codec.BOOL.optionalFieldOf("legs_only", false).forGetter(Body::legsOnly),
                Codec.BOOL.optionalFieldOf("outer", false).forGetter(Body::outer),
                ARGB_CODEC.optionalFieldOf("color", 0xFFFFFFFF).forGetter(Body::color)
        ).apply(instance, Body::new));
    }

    @NullMarked
    public enum BodyMode implements StringRepresentable {
        FILM("film"),
        LIMBS("limbs");

        public static final Codec<BodyMode> CODEC = StringRepresentable.fromEnum(BodyMode::values);

        private final String name;

        BodyMode(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    /**
     * @param look the skin look (QiCoatingLooks) that paints the coating into the skin
     */
    public record Skin(Identifier look) {
        public static final Codec<Skin> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Identifier.CODEC.fieldOf("look").forGetter(Skin::look)
        ).apply(instance, Skin::new));
    }

    /**
     * @param look      the sword look (QiCoatingLooks) that redraws the weapon
     * @param quietLook the sword look drawn instead while a coating with a {@code reacts_to} isn't echoing
     */
    public record Sword(Identifier look, Optional<Identifier> quietLook) {
        public static final Codec<Sword> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Identifier.CODEC.fieldOf("look").forGetter(Sword::look),
                Identifier.CODEC.optionalFieldOf("quiet_look").forGetter(Sword::quietLook)
        ).apply(instance, Sword::new));
    }

    /**
     * Particles spawned at each hand every tick (at the hand on screen in first person).
     *
     * @param particle the particle
     * @param chance   chance per tick, per hand, of spawning
     * @param count    how many spawn each time
     * @param spread   how far from the hand (blocks, either way across) each may start
     * @param offsetY  how far above the hand they start
     * @param velocity their starting velocity
     * @param converge if above 0, each starts at the hand with a velocity this long in a random direction instead (particles
     *                 like portal ones, that travel from their start plus velocity back to their start, then stream into the
     *                 hand)
     */
    public record HandParticles(ParticleOptions particle, float chance, int count, float spread, float offsetY, Vec3 velocity,
                                float converge) {
        public static final Codec<HandParticles> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ParticleTypes.CODEC.fieldOf("particle").forGetter(HandParticles::particle),
                Codec.floatRange(0.0F, 1.0F).optionalFieldOf("chance", 1.0F).forGetter(HandParticles::chance),
                Codec.intRange(1, 32).optionalFieldOf("count", 1).forGetter(HandParticles::count),
                Codec.floatRange(0.0F, 2.0F).optionalFieldOf("spread", 0.0F).forGetter(HandParticles::spread),
                Codec.floatRange(-2.0F, 2.0F).optionalFieldOf("offset_y", 0.0F).forGetter(HandParticles::offsetY),
                CodecHelpers.VEC3.optionalFieldOf("velocity", Vec3.ZERO).forGetter(HandParticles::velocity),
                Codec.floatRange(0.0F, 4.0F).optionalFieldOf("converge", 0.0F).forGetter(HandParticles::converge)
        ).apply(instance, HandParticles::new));
    }
}
