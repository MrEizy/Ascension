package net.zic.ascension.api.ascension.core.qi_coating;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;

import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.NullMarked;

/**
 * A Qi Coating, loaded from {@code data/<namespace>/ascension/skill_system/qi_coatings/}. It says where the coating goes and which
 * of the built-in looks draw it (a look is shader code, named in the client's QiCoatingLooks), with everything else about it
 * (shell geometry, how it grows, flags and the named code effects it adds) as data. Qi Coating passives put it on their owner
 * by its id (QiCoatingManager): through an element's {@code body_coating} or {@code external_coating}
 * ({@code ascension:qi_coating_visual}), or named directly ({@code ascension:qi_coating}).
 *
 * @param coverage       where the coating goes
 * @param shells         boxes drawn around the limbs, shaded by a shell look
 * @param skin           the coating painted into the player's own skin pixels by a skin look
 * @param sword          the held weapon redrawn by a sword look (coverage {@code weapon} only)
 * @param growMillis     how long the coating takes to grow in when it comes on and to shrink away when it goes off; 0 to just
 *                       appear and vanish
 * @param echoes         whether flickering offset copies of the arm (or body, or weapon) are drawn alongside it
 * @param reactsTo       what the coating reacts to: looks with a hit animation play it after these, and an echoing coating
 *                       echoes only for a moment after them (always, if empty), drawing its looks' {@code quiet_look} (if any)
 *                       the rest of the time
 * @param effects        named effects coded in Java (QiCoatingEffects) that the coating adds
 * @param gather         how a hands coating's shells form while they grow: gathered from specks drawn in from round the arm,
 *                       instead of being cut from the hand up
 * @param foliageMillis  for a look coloured by the biome's foliage (ShellLook/SwordLook foliage): it comes on in the shade of
 *                       the biome it was turned on in, then takes about this long to take on a new biome's shade; 0 follows
 *                       the biome at once
 * @param foliageLocked  for a look coloured by the biome's foliage: keeps the shade it came on in, whatever biome the player
 *                       walks into, until it is turned on again
 */
public record QiCoatingDefinition(
        Coverage coverage,
        Optional<Shells> shells,
        Optional<Skin> skin,
        Optional<Sword> sword,
        int growMillis,
        boolean echoes,
        List<Reaction> reactsTo,
        List<Identifier> effects,
        Optional<Gather> gather,
        int foliageMillis,
        boolean foliageLocked
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
            Codec.BOOL.optionalFieldOf("echoes", false).forGetter(QiCoatingDefinition::echoes),
            Reaction.CODEC.listOf().optionalFieldOf("reacts_to", List.of()).forGetter(QiCoatingDefinition::reactsTo),
            Identifier.CODEC.listOf().optionalFieldOf("effects", List.of()).forGetter(QiCoatingDefinition::effects),
            Gather.CODEC.optionalFieldOf("gather").forGetter(QiCoatingDefinition::gather),
            Codec.intRange(0, 600000).optionalFieldOf("foliage_millis", 0).forGetter(QiCoatingDefinition::foliageMillis),
            Codec.BOOL.optionalFieldOf("foliage_locked", false).forGetter(QiCoatingDefinition::foliageLocked)
    ).apply(instance, QiCoatingDefinition::new)).validate(QiCoatingDefinition::validate);

    public QiCoatingDefinition {
        reactsTo = List.copyOf(reactsTo);
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
        if (definition.gather().isPresent()) {
            if ((definition.coverage() != Coverage.HANDS && definition.coverage() != Coverage.WEAPON) || definition.growMillis() <= 0) {
                return DataResult.error(() -> "A qi coating's \"gather\" needs coverage \"hands\" or \"weapon\" and a \"grow_millis\" above 0");
            }
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
     * from the skin area of the body part it covers (its inner layer). The frames are stacked top to bottom in one image, so a 5
     * frame texture is 64 wide and 320 tall.
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
     */
    public record Layer(float inflate, float reach, float overhang, int color, boolean squareCap) {
        public static final Codec<Layer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.floatRange(0.0F, 8.0F).fieldOf("inflate").forGetter(Layer::inflate),
                Codec.floatRange(0.0F, 16.0F).fieldOf("reach").forGetter(Layer::reach),
                Codec.floatRange(0.0F, 8.0F).optionalFieldOf("overhang", 0.0F).forGetter(Layer::overhang),
                ARGB_CODEC.optionalFieldOf("color", 0xFFFFFFFF).forGetter(Layer::color),
                Codec.BOOL.optionalFieldOf("square_cap", false).forGetter(Layer::squareCap)
        ).apply(instance, Layer::new));
    }

    /**
     * How a body coating's shells cover the body in third person.
     *
     * @param mode          {@code film}: one box round every part; {@code limbs}: one box round each arm and leg, with the look's
     *                      per-limb pattern
     * @param inflate       how far the boxes sit out from the body, in pixels
     * @param legsOnly      limbs only: whether only the legs get boxes
     * @param color         texture shells only: ARGB tint multiplied into the texture; its alpha sets how see-through the boxes are
     */
    public record Body(BodyMode mode, float inflate, boolean legsOnly, int color) {
        public static final Codec<Body> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BodyMode.CODEC.fieldOf("mode").forGetter(Body::mode),
                Codec.floatRange(0.0F, 4.0F).optionalFieldOf("inflate", 0.4F).forGetter(Body::inflate),
                Codec.BOOL.optionalFieldOf("legs_only", false).forGetter(Body::legsOnly),
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
     * How a hands coating's shells form as it grows (QiCoating.growMillis): specks of the element appear a little way round the
     * arm and are drawn into it, and the shells form from them. Played backwards as the coating shrinks away, the specks flying
     * back out.
     *
     * @param style            how the shells form from the specks
     * @param colors           the specks' ARGB colours, picked at random per speck
     * @param landings         how many places round each arm the specks land
     * @param specksPerLanding how many specks fly in to each place
     * @param speckSize        a speck's half size, in pixels (each varies a little from it)
     * @param glow             how strongly each speck glows, 0 for none
     */
    public record Gather(GatherStyle style, List<Integer> colors, int landings, int specksPerLanding, float speckSize, float glow) {
        public static final Codec<Gather> CODEC = RecordCodecBuilder.<Gather>create(instance -> instance.group(
                GatherStyle.CODEC.fieldOf("style").forGetter(Gather::style),
                ARGB_CODEC.listOf().fieldOf("colors").forGetter(Gather::colors),
                Codec.intRange(1, 32).optionalFieldOf("landings", 9).forGetter(Gather::landings),
                Codec.intRange(1, 8).optionalFieldOf("specks_per_landing", 3).forGetter(Gather::specksPerLanding),
                Codec.floatRange(0.05F, 2.0F).optionalFieldOf("speck_size", 0.18F).forGetter(Gather::speckSize),
                Codec.floatRange(0.0F, 1.0F).optionalFieldOf("glow", 0.1F).forGetter(Gather::glow)
        ).apply(instance, Gather::new)).validate(gather -> gather.colors().isEmpty()
                ? DataResult.error(() -> "A qi coating's \"gather\" needs at least one colour in \"colors\"")
                : DataResult.success(gather));

        public Gather {
            colors = List.copyOf(colors);
        }
    }

    @NullMarked
    public enum GatherStyle implements StringRepresentable {
        // The shells and skin look show only round where specks have landed, each patch spreading until they join.
        PATCHES("patches"),
        // The shells, the skin look and the wind rings fade in evenly while the specks arrive.
        FADE("fade"),
        // Just the specks; the coating forms its own way (a skin look's spread from the hand, a sword look's spread up the blade).
        SPECKS("specks");

        public static final Codec<GatherStyle> CODEC = StringRepresentable.fromEnum(GatherStyle::values);

        private final String name;

        GatherStyle(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
