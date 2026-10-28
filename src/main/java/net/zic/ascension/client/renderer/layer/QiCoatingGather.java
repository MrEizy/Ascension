package net.zic.ascension.client.renderer.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.HumanoidArm;
import net.zic.ascension.api.ascension.core.qi_coating.QiCoatingDefinition;
import net.zic.ascension.client.renderer.ModRenderTypes;

import java.util.ArrayList;
import java.util.List;

/**
 * A hands coating's gather (QiCoatingDefinition.Gather): while it grows, specks of the element appear a little way round the arm
 * and are drawn into it, and the coating forms from them, either with its shells in patches round where they land (through
 * QiCoatingTextureShells and QiCoatingLayer's shell boxes) or fading in evenly. Played backwards as the coating shrinks, the
 * specks flying back out. Everything
 * here is in the arm's local pixels, with growth (0 to 1) as the clock.
 */
public final class QiCoatingGather {
    // The specks land between FIRST_ARRIVAL and LAST_ARRIVAL of the growth, one landing after another.
    private static final float FIRST_ARRIVAL = 0.3F;
    private static final float LAST_ARRIVAL = 0.85F;
    // How much of the growth a speck takes to fly in, and how far out it starts, in pixels.
    private static final float TRAVEL = 0.3F;
    private static final float START_MIN = 5.0F;
    private static final float START_MAX = 11.0F;
    // How far out a speck starts from a held weapon, in item pixels (the item is 16 across).
    private static final float ITEM_START_MIN = 4.0F;
    private static final float ITEM_START_MAX = 8.0F;
    // Patches: a landed patch's radius grows to PATCH_RADIUS over PATCH_GROW of the growth, then on to FILL_RADIUS by the end.
    private static final float PATCH_RADIUS = 3.5F;
    private static final float PATCH_GROW = 0.2F;
    private static final float FILL_RADIUS = 9.0F;
    // Fade: the shells' alpha rises between these shares of the growth.
    private static final float FADE_FROM = 0.3F;
    private static final float FADE_TO = 1.0F;
    // Each speck's size varies between these shares of Gather.speckSize.
    private static final float SIZE_LOW = 0.73F;
    private static final float SIZE_HIGH = 1.33F;
    // Each speck's look: its edge and core as shares of its colour's brightness, how much of each face the core covers, and its
    // glow's size (times the speck's).
    private static final float EDGE_SHADE = 0.6F;
    private static final float CORE_SHADE = 1.0F;
    private static final float CORE_SHARE = 0.6F;
    private static final float GLOW_SCALE = 1.35F;

    private QiCoatingGather() {
    }

    /**
     * A FADE coating's layer colour with its alpha scaled by how far it has faded in.
     */
    public static int fade(int argb, float growth) {
        int alpha = Math.round(((argb >>> 24) & 0xFF) * fadeAmount(growth));
        return (alpha << 24) | (argb & 0xFFFFFF);
    }

    /**
     * How far a FADE coating has faded in, 0 to 1, when it has grown this far.
     */
    public static float fadeAmount(float growth) {
        float amount = Mth.clamp((growth - FADE_FROM) / (FADE_TO - FADE_FROM), 0.0F, 1.0F);
        return amount * amount * (3.0F - 2.0F * amount);
    }

    /**
     * Where the specks land round one arm: x, y, z and the share of the growth they arrive at. The first lands on the hand's end;
     * the rest are spread round the arm up to `reach` from the hand tip, the same each time.
     */
    static float[][] landings(QiCoatingDefinition.Gather gather, HumanoidArm side, boolean slim, float reach) {
        RandomSource random = RandomSource.create(side == HumanoidArm.RIGHT ? 1931L : 7177L);
        float centerX = centerX(side, slim);
        float halfX = slim ? 1.5F : 2.0F;
        int count = gather.landings();
        float[][] landings = new float[count][4];
        for (int i = 0; i < count; i++) {
            float[] landing = landings[i];
            if (i == 0) {
                landing[0] = centerX;
                landing[1] = 10.0F;
                landing[2] = 0.0F;
            } else {
                // A point on the arm's surface in a random direction round it.
                float angle = random.nextFloat() * (float) (Math.PI * 2.0);
                float dx = Mth.cos(angle);
                float dz = Mth.sin(angle);
                float scale = 1.0F / Math.max(Math.abs(dx) / halfX, Math.abs(dz) / 2.0F);
                landing[0] = centerX + dx * scale;
                landing[1] = 10.0F - random.nextFloat() * Math.min(reach, 12.0F);
                landing[2] = dz * scale;
            }
            landing[3] = count == 1 ? FIRST_ARRIVAL : FIRST_ARRIVAL + (LAST_ARRIVAL - FIRST_ARRIVAL) * i / (count - 1);
        }
        return landings;
    }

    /**
     * Whether a PATCHES coating shows at this point when it has grown this far: within a patch spreading from a landing the
     * specks have reached. `jitter` (0 to 1, steady per pixel) roughens the patch edges.
     */
    public static boolean revealed(float[][] landings, float growth, float x, float y, float z, float jitter) {
        if (growth >= 0.999F) {
            return true;
        }
        for (float[] landing : landings) {
            float since = growth - landing[3];
            if (since <= 0.0F) {
                continue;
            }
            float radius = since < PATCH_GROW
                    ? PATCH_RADIUS * since / PATCH_GROW
                    : PATCH_RADIUS + (FILL_RADIUS - PATCH_RADIUS) * (since - PATCH_GROW) / (1.0F - landing[3] - PATCH_GROW + 1.0E-3F);
            radius += (jitter - 0.5F) * 1.5F;
            float dx = x - landing[0];
            float dy = y - landing[1];
            float dz = z - landing[2];
            if (dx * dx + dy * dy + dz * dz <= radius * radius) {
                return true;
            }
        }
        return false;
    }

    /**
     * A steady 0 to 1 per pixel, for revealed's jitter.
     */
    public static float pixelJitter(float x, float y, float z) {
        float h = Mth.sin(Mth.floor(x) * 12.9898F + Mth.floor(y) * 78.233F + Mth.floor(z) * 37.719F) * 43758.547F;
        return h - Mth.floor(h);
    }

    /**
     * The specks in flight to one arm: each a faint glow (added light) round a cube in a darker shade of its colour with its
     * colour on the middle of each face. Expects the pose stack in the arm's local space.
     */
    static void submitSpecks(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, QiCoatingDefinition.Gather gather,
                             HumanoidArm side, boolean slim, float reach, float growth) {
        if (growth <= 0.0F || growth >= 1.0F) {
            return;
        }
        drawSpecks(poseStack, submitNodeCollector, gather, specks(gather, side, slim, reach, growth));
    }

    /**
     * Where the specks land on a held weapon, picked from its coated pixels: x, y, z in the item model's pixels (1/16 of a block)
     * and the share of the growth they arrive at, the same each time for the same item.
     *
     * @param candidates the centres of the item's coated pixels, in model pixels
     */
    public static float[][] itemLandings(QiCoatingDefinition.Gather gather, List<float[]> candidates) {
        if (candidates.isEmpty()) {
            return new float[0][];
        }
        RandomSource random = RandomSource.create(candidates.size() * 4219L + 31L);
        int count = gather.landings();
        float[][] landings = new float[count][4];
        for (int i = 0; i < count; i++) {
            float[] pixel = candidates.get(random.nextInt(candidates.size()));
            landings[i][0] = pixel[0];
            landings[i][1] = pixel[1];
            landings[i][2] = pixel[2];
            landings[i][3] = count == 1 ? FIRST_ARRIVAL : FIRST_ARRIVAL + (LAST_ARRIVAL - FIRST_ARRIVAL) * i / (count - 1);
        }
        return landings;
    }

    /**
     * The specks in flight to a held weapon, each starting out from the item's middle (centreX, centreY, in model pixels)
     * through its landing. Expects the pose stack in the item model's space.
     */
    public static void submitItemSpecks(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, QiCoatingDefinition.Gather gather,
                                        float[][] landings, float centreX, float centreY, float growth) {
        if (growth <= 0.0F || growth >= 1.0F || landings.length == 0) {
            return;
        }
        List<Integer> colors = gather.colors();
        List<Speck> specks = new ArrayList<>();
        RandomSource random = RandomSource.create(landings.length * 7703L + 11L);
        for (float[] landing : landings) {
            float outX = landing[0] - centreX;
            float outY = landing[1] - centreY;
            float outLength = Mth.sqrt(outX * outX + outY * outY);
            if (outLength < 0.01F) {
                outX = 0.0F;
                outY = 1.0F;
            } else {
                outX /= outLength;
                outY /= outLength;
            }
            for (int j = 0; j < gather.specksPerLanding(); j++) {
                float distance = ITEM_START_MIN + random.nextFloat() * (ITEM_START_MAX - ITEM_START_MIN);
                float startX = landing[0] + outX * distance + (random.nextFloat() - 0.5F) * 3.0F;
                float startY = landing[1] + outY * distance + (random.nextFloat() - 0.5F) * 3.0F;
                float startZ = landing[2] + (random.nextFloat() - 0.5F) * 6.0F;
                float arrive = landing[3] - j * 0.03F;
                float depart = arrive - TRAVEL * (0.8F + random.nextFloat() * 0.4F);
                int color = colors.get(random.nextInt(colors.size()));
                float size = gather.speckSize() * Mth.lerp(random.nextFloat(), SIZE_LOW, SIZE_HIGH);
                if (growth < depart || growth > arrive) {
                    continue;
                }
                float t = (growth - depart) / (arrive - depart);
                float eased = t * t;
                specks.add(new Speck(Mth.lerp(eased, startX, landing[0]), Mth.lerp(eased, startY, landing[1]),
                        Mth.lerp(eased, startZ, landing[2]), size * (1.0F - 0.5F * t), color, Math.min(1.0F, t / 0.2F)));
            }
        }
        drawSpecks(poseStack, submitNodeCollector, gather, specks);
    }

    private static void drawSpecks(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, QiCoatingDefinition.Gather gather,
                                   List<Speck> specks) {
        if (specks.isEmpty()) {
            return;
        }
        if (gather.glow() > 0.0F) {
            submitNodeCollector.submitCustomGeometry(poseStack, ModRenderTypes.energySurface(), (pose, buffer) -> {
                for (Speck speck : specks) {
                    int glow = ARGB.color(Math.round(gather.glow() * speck.alpha() * 255.0F), speck.color());
                    cube(buffer, pose, speck.x(), speck.y(), speck.z(), speck.half() * GLOW_SCALE, glow);
                }
            });
        }
        submitNodeCollector.submitCustomGeometry(poseStack, ModRenderTypes.qiCoatingSpecks(), (pose, buffer) -> {
            for (Speck speck : specks) {
                int alpha = Math.round(speck.alpha() * ARGB.alpha(speck.color()));
                cube(buffer, pose, speck.x(), speck.y(), speck.z(), speck.half(), ARGB.color(alpha, ARGB.scaleRGB(speck.color(), EDGE_SHADE)));
                coreFaces(buffer, pose, speck.x(), speck.y(), speck.z(), speck.half(),
                        ARGB.color(alpha, ARGB.scaleRGB(speck.color(), CORE_SHADE)));
            }
        });
    }

    /**
     * A speck in flight: where it is, its half size, its colour and how faded in it is (0 to 1).
     */
    private record Speck(float x, float y, float z, float half, int color, float alpha) {
    }

    /**
     * Where each speck in flight is this far into the growth. Each speck's path is drawn from a fixed seed, so it is the same
     * every frame: it starts a little way out from its landing and is drawn in, slowly at first and quickening as it closes in.
     */
    private static List<Speck> specks(QiCoatingDefinition.Gather gather, HumanoidArm side, boolean slim, float reach, float growth) {
        float[][] landings = landings(gather, side, slim, reach);
        float centerX = centerX(side, slim);
        List<Integer> colors = gather.colors();
        List<Speck> specks = new ArrayList<>();
        RandomSource random = RandomSource.create(side.ordinal() * 92821L + 17L);
        for (float[] landing : landings) {
            for (int j = 0; j < gather.specksPerLanding(); j++) {
                // Out from the arm's middle through the landing, or straight up the arm for the one on the hand's end.
                float outX = landing[0] - centerX;
                float outZ = landing[2];
                float outLength = Mth.sqrt(outX * outX + outZ * outZ);
                boolean handEnd = outLength < 0.01F;
                if (!handEnd) {
                    outX /= outLength;
                    outZ /= outLength;
                }
                float distance = START_MIN + random.nextFloat() * (START_MAX - START_MIN);
                float startX = landing[0] + (handEnd ? 0.0F : outX * distance) + (random.nextFloat() - 0.5F) * 4.0F;
                float startY = landing[1] + (handEnd ? distance : 0.0F) + (random.nextFloat() - 0.5F) * 5.0F;
                float startZ = landing[2] + (handEnd ? 0.0F : outZ * distance) + (random.nextFloat() - 0.5F) * 4.0F;
                float arrive = landing[3] - j * 0.03F;
                float depart = arrive - TRAVEL * (0.8F + random.nextFloat() * 0.4F);
                int color = colors.get(random.nextInt(colors.size()));
                float size = gather.speckSize() * Mth.lerp(random.nextFloat(), SIZE_LOW, SIZE_HIGH);
                if (growth < depart || growth > arrive) {
                    continue;
                }
                float t = (growth - depart) / (arrive - depart);
                float eased = t * t;
                specks.add(new Speck(Mth.lerp(eased, startX, landing[0]), Mth.lerp(eased, startY, landing[1]),
                        Mth.lerp(eased, startZ, landing[2]), size * (1.0F - 0.5F * t), color, Math.min(1.0F, t / 0.2F)));
            }
        }
        return specks;
    }

    private static float centerX(HumanoidArm side, boolean slim) {
        return (slim ? 0.5F : 1.0F) * (side == HumanoidArm.RIGHT ? -1.0F : 1.0F);
    }

    /**
     * A smaller square on each face of a cube, a hair proud of it, so the cube's own colour shows only as a rim round them.
     */
    private static void coreFaces(VertexConsumer buffer, PoseStack.Pose pose, float x, float y, float z, float half, int argb) {
        float c = half * CORE_SHARE / 16.0F;
        float o = (half + 0.02F) / 16.0F;
        float cx = x / 16.0F, cy = y / 16.0F, cz = z / 16.0F;
        quad(buffer, pose, argb, cx - c, cy - c, cz - o, cx + c, cy - c, cz - o, cx + c, cy + c, cz - o, cx - c, cy + c, cz - o);
        quad(buffer, pose, argb, cx - c, cy - c, cz + o, cx - c, cy + c, cz + o, cx + c, cy + c, cz + o, cx + c, cy - c, cz + o);
        quad(buffer, pose, argb, cx - o, cy - c, cz - c, cx - o, cy + c, cz - c, cx - o, cy + c, cz + c, cx - o, cy - c, cz + c);
        quad(buffer, pose, argb, cx + o, cy - c, cz - c, cx + o, cy - c, cz + c, cx + o, cy + c, cz + c, cx + o, cy + c, cz - c);
        quad(buffer, pose, argb, cx - c, cy - o, cz - c, cx - c, cy - o, cz + c, cx + c, cy - o, cz + c, cx + c, cy - o, cz - c);
        quad(buffer, pose, argb, cx - c, cy + o, cz - c, cx + c, cy + o, cz - c, cx + c, cy + o, cz + c, cx - c, cy + o, cz + c);
    }

    private static void cube(VertexConsumer buffer, PoseStack.Pose pose, float x, float y, float z, float half, int argb) {
        float x0 = (x - half) / 16.0F, x1 = (x + half) / 16.0F;
        float y0 = (y - half) / 16.0F, y1 = (y + half) / 16.0F;
        float z0 = (z - half) / 16.0F, z1 = (z + half) / 16.0F;
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
