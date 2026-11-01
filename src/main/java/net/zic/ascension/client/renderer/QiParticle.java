package net.zic.ascension.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

import java.util.List;

/**
 * Qi particles: the small specks of qi the coatings gather from (QiCoatingGather), for anything that wants them. Each is a
 * cube in a darker shade of its colour with its colour on the middle of each face, and optionally a faint glow (added light)
 * round it. Positions and sizes are in the pose's own units, so the same particles work in an arm's pixels or the world's
 * blocks. Under a shader pack they're drawn after it with our own shaders (AfterShaderPackDraws).
 * <p>
 * Where the particles are is up to the caller; gather and scatter are the easings the coatings fly them in and out with.
 */
public final class QiParticle {
    // A particle's edge and core as shares of its colour's brightness, how much of each face the core covers, how far proud of
    // the cube the core sits (times its half size), and its glow's size (times its own).
    private static final float EDGE_SHADE = 0.6F;
    private static final float CORE_SHADE = 1.0F;
    private static final float CORE_SHARE = 0.6F;
    private static final float CORE_LIFT = 1.1F;
    private static final float GLOW_SCALE = 1.35F;
    // Specks gathering into something (a coating's gather, the Qi Barrier forming) land one after another between FIRST_ARRIVAL
    // and LAST_ARRIVAL of the gathering, each flying in over about TRAVEL of it, at SIZE_LOW to SIZE_HIGH of their usual size.
    private static final float FIRST_ARRIVAL = 0.3F;
    private static final float LAST_ARRIVAL = 0.85F;
    private static final float TRAVEL = 0.3F;
    private static final float SIZE_LOW = 0.73F;
    private static final float SIZE_HIGH = 1.33F;

    private QiParticle() {
    }

    /**
     * One particle: its centre and half size in the pose's units, its colour (ARGB) and how faded in it is (0 to 1).
     */
    public record Speck(float x, float y, float z, float half, int color, float alpha) {
    }

    /**
     * Flying in to where it lands (t from 0 to 1): slow at first, quickening as it closes in.
     */
    public static float gather(float t) {
        return t * t;
    }

    /**
     * Flying out from where it was (t from 0 to 1): fast at first, slowing as it drifts off.
     */
    public static float scatter(float t) {
        float rest = 1.0F - t;
        return 1.0F - rest * rest;
    }

    /**
     * When the `index`th of `count` gathering specks lands, as a share of the gathering (0 to 1).
     */
    public static float arrival(int index, int count) {
        return count <= 1 ? FIRST_ARRIVAL : FIRST_ARRIVAL + (LAST_ARRIVAL - FIRST_ARRIVAL) * index / (count - 1);
    }

    /**
     * When a speck landing at `arrive` sets off, from a random `roll` (0 to 1): about TRAVEL before.
     */
    public static float departure(float arrive, float roll) {
        return arrive - TRAVEL * (0.8F + roll * 0.4F);
    }

    /**
     * A speck's size as a share of its usual one, from a random `roll` (0 to 1).
     */
    public static float sizeScale(float roll) {
        return Mth.lerp(roll, SIZE_LOW, SIZE_HIGH);
    }

    /**
     * Particles from an entity layer or item renderer, in the pose stack's space. glow is the glow's opacity (0 for none).
     */
    public static void submit(SubmitNodeCollector submitNodeCollector, PoseStack poseStack, List<Speck> specks, float glow) {
        if (specks.isEmpty()) {
            return;
        }
        if (glow > 0.0F) {
            AfterShaderPackDraws.submitCustomGeometry(submitNodeCollector, poseStack, ModRenderTypes.energySurfaceDirect(),
                    (pose, buffer) -> drawGlow(buffer, pose, specks, glow));
        }
        AfterShaderPackDraws.submitCustomGeometry(submitNodeCollector, poseStack, ModRenderTypes.qiCoatingSpecks(),
                (pose, buffer) -> drawSpecks(buffer, pose, specks));
    }

    /**
     * Particles from a level stage (RenderLevelStageEvent), in the pose stack's space (camera-relative world blocks there).
     */
    public static void drawOrDefer(PoseStack poseStack, List<Speck> specks, float glow) {
        if (specks.isEmpty()) {
            return;
        }
        if (glow > 0.0F) {
            AfterShaderPackDraws.drawOrDefer(poseStack, ModRenderTypes.energySurfaceDirect(),
                    (pose, buffer) -> drawGlow(buffer, pose, specks, glow));
        }
        AfterShaderPackDraws.drawOrDefer(poseStack, ModRenderTypes.qiCoatingSpecks(), (pose, buffer) -> drawSpecks(buffer, pose, specks));
    }

    private static void drawGlow(VertexConsumer buffer, PoseStack.Pose pose, List<Speck> specks, float glow) {
        for (Speck speck : specks) {
            int color = ARGB.color(Math.round(glow * speck.alpha() * 255.0F), speck.color());
            cube(buffer, pose, speck.x(), speck.y(), speck.z(), speck.half() * GLOW_SCALE, color);
        }
    }

    private static void drawSpecks(VertexConsumer buffer, PoseStack.Pose pose, List<Speck> specks) {
        for (Speck speck : specks) {
            int alpha = Math.round(speck.alpha() * ARGB.alpha(speck.color()));
            cube(buffer, pose, speck.x(), speck.y(), speck.z(), speck.half(), ARGB.color(alpha, ARGB.scaleRGB(speck.color(), EDGE_SHADE)));
            coreFaces(buffer, pose, speck.x(), speck.y(), speck.z(), speck.half(), ARGB.color(alpha, ARGB.scaleRGB(speck.color(), CORE_SHADE)));
        }
    }

    /**
     * A smaller square on each face of a cube, a hair proud of it, so the cube's own colour shows only as a rim round them.
     */
    private static void coreFaces(VertexConsumer buffer, PoseStack.Pose pose, float x, float y, float z, float half, int argb) {
        float c = half * CORE_SHARE;
        float o = half * CORE_LIFT;
        quad(buffer, pose, argb, x - c, y - c, z - o, x + c, y - c, z - o, x + c, y + c, z - o, x - c, y + c, z - o);
        quad(buffer, pose, argb, x - c, y - c, z + o, x - c, y + c, z + o, x + c, y + c, z + o, x + c, y - c, z + o);
        quad(buffer, pose, argb, x - o, y - c, z - c, x - o, y + c, z - c, x - o, y + c, z + c, x - o, y - c, z + c);
        quad(buffer, pose, argb, x + o, y - c, z - c, x + o, y - c, z + c, x + o, y + c, z + c, x + o, y + c, z - c);
        quad(buffer, pose, argb, x - c, y - o, z - c, x - c, y - o, z + c, x + c, y - o, z + c, x + c, y - o, z - c);
        quad(buffer, pose, argb, x - c, y + o, z - c, x + c, y + o, z - c, x + c, y + o, z + c, x - c, y + o, z + c);
    }

    private static void cube(VertexConsumer buffer, PoseStack.Pose pose, float x, float y, float z, float half, int argb) {
        float x0 = x - half, x1 = x + half;
        float y0 = y - half, y1 = y + half;
        float z0 = z - half, z1 = z + half;
        quad(buffer, pose, argb, x0, y0, z0, x1, y0, z0, x1, y1, z0, x0, y1, z0);
        quad(buffer, pose, argb, x0, y0, z1, x0, y1, z1, x1, y1, z1, x1, y0, z1);
        quad(buffer, pose, argb, x0, y0, z0, x0, y1, z0, x0, y1, z1, x0, y0, z1);
        quad(buffer, pose, argb, x1, y0, z0, x1, y0, z1, x1, y1, z1, x1, y1, z0);
        quad(buffer, pose, argb, x0, y0, z0, x0, y0, z1, x1, y0, z1, x1, y0, z0);
        quad(buffer, pose, argb, x0, y1, z0, x1, y1, z0, x1, y1, z1, x0, y1, z1);
    }

    private static void quad(VertexConsumer buffer, PoseStack.Pose pose, int argb,
                             float ax, float ay, float az, float bx, float by, float bz,
                             float cx, float cy, float cz, float dx, float dy, float dz) {
        buffer.addVertex(pose, ax, ay, az).setColor(argb);
        buffer.addVertex(pose, bx, by, bz).setColor(argb);
        buffer.addVertex(pose, cx, cy, cz).setColor(argb);
        buffer.addVertex(pose, dx, dy, dz).setColor(argb);
    }
}
