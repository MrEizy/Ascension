package net.zic.ascension.client.renderer;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.resources.Identifier;
import net.zic.ascension.AscensionCraft;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * The built-in looks Qi Coatings are drawn with, by name. A look is shader code, so datapacks can only pick from these; a coating
 * (QiCoatingDefinition) names the looks it uses. Other mods can add looks with the register methods during client setup.
 * <ul>
 *     <li>Shell looks shade the boxes round the limbs, in qi_coating.fsh: each is a shader style, and
 *     patterns that differ per limb have a style per limb (right arm, left arm, right leg, left leg; limbs past the end use the
 *     last).</li>
 *     <li>Skin looks paint into the player's own skin pixels, in qi_coating_skin.fsh: each is a render pipeline (the shader with
 *     its defines).</li>
 *     <li>Sword looks redraw the held weapon, in sword_qi.fsh: each is a STYLE, and some take extra vertex data.</li>
 * </ul>
 */
public final class QiCoatingLooks {
    private static final Map<Identifier, ShellLook> SHELL_LOOKS = new HashMap<>();
    private static final Map<Identifier, Supplier<RenderPipeline>> SKIN_LOOKS = new HashMap<>();
    private static final Map<Identifier, SwordLook> SWORD_LOOKS = new HashMap<>();
    private static final Set<Identifier> WARNED = new HashSet<>();

    static {
        // Pure Qi: an even pale cyan film of plain qi, the element-less coating anyone can use.
        registerShell(id("pure_qi"), ShellLook.plain(57));
        // Space: a window into the starry depths of space on the hands, or over the whole body.
        registerShell(id("space_starfield"), ShellLook.plain(5));
        // Shelved (its JSON is in src/main/resources-disabled/qi_coatings).
        // registerShell(id("void_echo_layer"), ShellLook.plain(10));
        // Time: a thin violet film whose bands tear sideways (time_echo), drawn still between triggers (time_echo_quiet).
        registerShell(id("time_echo"), ShellLook.plain(9));
        registerShell(id("time_echo_quiet"), ShellLook.plain(58));
        registerShell(id("space_starfield_layer"), ShellLook.plain(11));
        // Lightning: pixel bolts racing up the arms (hand coating) or every limb, and Lightning Step's crackling legs, which
        // don't write depth so the see-through body shows behind them.
        registerShell(id("lightning_crackle"), new ShellLook(new int[]{14, 45}, true, false, Render.DEPTH));
        registerShell(id("lightning_crackle_limbs"), new ShellLook(new int[]{16, 46, 47, 48}, true, false, Render.DEPTH));
        registerShell(id("lightning_step"), new ShellLook(new int[]{15, 15, 49, 50}, true, false, Render.NORMAL));
        // Water: the Serpent Coil, on the arms only or on every limb.
        registerShell(id("water_serpent_arms"), new ShellLook(new int[]{27, 44}, true, false, Render.DEPTH));
        registerShell(id("water_serpent"), new ShellLook(new int[]{28, 41, 42, 43}, true, false, Render.DEPTH));

        registerSkin(id("ice"), () -> ModRenderPipelines.QI_COATING_SKIN);
        registerSkin(id("metal"), () -> ModRenderPipelines.QI_COATING_SKIN_METAL);
        registerSkin(id("wind"), () -> ModRenderPipelines.QI_COATING_SKIN_WIND);

        registerSword(id("pure_qi"), new SwordLook(9, SwordData.PLAIN));
        registerSword(id("starfield"), new SwordLook(0, SwordData.PLAIN));
        // Time: tearing violet copies of the blade (time_echo) over a still violet film (time_echo_quiet).
        registerSword(id("time_echo"), new SwordLook(1, SwordData.PLAIN));
        registerSword(id("time_echo_quiet"), new SwordLook(10, SwordData.PLAIN));
        registerSword(id("cracked"), new SwordLook(2, SwordData.PLAIN));
        registerSword(id("water"), new SwordLook(3, SwordData.PLAIN));
        registerSword(id("arcs"), new SwordLook(4, SwordData.PLAIN));
        registerSword(id("frost"), new SwordLook(5, SwordData.SPREAD));
        registerSword(id("gale"), new SwordLook(6, SwordData.GALE));
        registerSword(id("molten"), new SwordLook(7, SwordData.SPREAD));
        registerSword(id("heat"), new SwordLook(8, SwordData.SPREAD));
    }

    private QiCoatingLooks() {
    }

    private static Identifier id(String path) {
        return AscensionCraft.prefix(path);
    }

    public static void registerShell(Identifier id, ShellLook look) {
        SHELL_LOOKS.put(id, look);
    }

    public static void registerSkin(Identifier id, Supplier<RenderPipeline> pipeline) {
        SKIN_LOOKS.put(id, pipeline);
    }

    public static void registerSword(Identifier id, SwordLook look) {
        SWORD_LOOKS.put(id, look);
    }

    /**
     * The shell look with this name, or null (logged once) if there isn't one.
     */
    public static ShellLook shell(Identifier id) {
        return found(id, SHELL_LOOKS.get(id), "shell");
    }

    /**
     * The skin look's render pipeline, or null (logged once) if there isn't one.
     */
    public static RenderPipeline skin(Identifier id) {
        Supplier<RenderPipeline> pipeline = found(id, SKIN_LOOKS.get(id), "skin");
        return pipeline == null ? null : pipeline.get();
    }

    /**
     * The sword look with this name, or null (logged once) if there isn't one.
     */
    public static SwordLook sword(Identifier id) {
        return found(id, SWORD_LOOKS.get(id), "sword");
    }

    private static <T> T found(Identifier id, T look, String kind) {
        if (look == null && WARNED.add(id)) {
            AscensionCraft.LOGGER.warn("Qi coating names unknown {} look {}; it is not drawn", kind, id);
        }
        return look;
    }

    /**
     * How a shell look's boxes are drawn.
     */
    public enum Render {
        // Translucent, without writing depth.
        NORMAL,
        // Writing depth, for near-opaque looks: without it the skin's sleeve layer, drawn later, shows over them.
        DEPTH
    }

    /**
     * @param styles  the qi_coating.fsh style, or one per limb when the pattern differs per limb
     * @param perLimb whether each limb gets its own pattern (styles per limb, or shifted round the limb for stone plates); a body
     *                coating's {@code limbs} shells use it
     * @param stone   whether the pattern is the stone plates, which start at a different point round each limb
     * @param render  how the boxes are drawn
     */
    public record ShellLook(int[] styles, boolean perLimb, boolean stone, Render render) {
        static ShellLook plain(int style) {
            return new ShellLook(new int[]{style}, false, false, Render.NORMAL);
        }

        public int style() {
            return styles[0];
        }

        /**
         * The style for one limb (0 right arm, 1 left arm, 2 right leg, 3 left leg).
         */
        public int limbStyle(int limb) {
            return styles[Math.min(limb, styles.length - 1)];
        }
    }

    /**
     * What a sword look needs in its vertex colour (SwordQiRenderer).
     */
    public enum SwordData {
        // Just the painted mask.
        PLAIN,
        // The mask and last hit's seed, how far the coating has spread up the blade, how far along it the pixel is, and how long
        // ago the last hit was (frostColor). For looks that spread from the hilt when they come on.
        SPREAD,
        // The mask, and how far across and along the item the pixel is (galeColor).
        GALE
    }

    /**
     * @param style the sword_qi.fsh STYLE
     * @param data  what it needs in its vertex colour
     */
    public record SwordLook(int style, SwordData data) {
    }
}
