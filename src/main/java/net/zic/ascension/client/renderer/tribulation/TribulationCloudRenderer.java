package net.zic.ascension.client.renderer.tribulation;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.zic.ascension.api.ascension.core.tribulation.TribulationCloudSettings;
import net.zic.ascension.api.ascension.core.tribulation.TribulationData;
import net.zic.ascension.api.ascension.core.tribulation.client.ClientTribulation;
import net.zic.ascension.api.ascension.core.tribulation.client.TribulationRenderer;
import net.zic.ascension.client.renderer.ModRenderTypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.ToIntFunction;

/**
 * renders a whirlpool of storm clouds sinking into a black eye, with lightning crawling along the spiral arms.
 * the eye follows the tribulation on x/z and sits at the configured y.
 * <p>
 * the mesh is a funnel of rings drawn as two sheets, the tribulation_clouds shader bulges them apart into billows and shades them per pixel.
 * each vertex passes its spiral coordinate and log radius in the uv and how lit up it is in the colour alpha,
 * the pixel count and inflow offset go through the texture matrix
 */
public class TribulationCloudRenderer<T extends TribulationData> implements TribulationRenderer<T> {
    //dense enough for the shader to displace the billows smoothly. ring 0 is the centre point
    private static final int RINGS = 90;
    private static final int SEGMENTS = 200;
    //the innermost real ring as a fraction of the radius, the shader draws the eye black from further out than this
    private static final float INNER_RING = 0.01F;
    //log radius given to the centre point, log(0) would be -infinity
    private static final float CENTER_LOG = (float) Math.log(0.001F);
    //how far the rim rises above the eye, as a fraction of the radius
    private static final float FUNNEL_DEPTH = 0.4F;
    //radians the spiral arms turn per unit of log radius
    private static final float TWIST = 2.0F;
    //how fast the billows drift towards the eye, in noise cells per tick
    private static final float INFLOW = 0.02F;
    //strikes light up the clouds within this fraction of the radius around the eye
    private static final float FLASH_RADIUS = 0.3F;
    //flicker bolts light up the clouds within this many blocks of their middle
    private static final float BOLT_GLOW_RADIUS = 14.0F;

    private static final int STRIKE_FLASH_TICKS = 6;
    //1 in this chance per tick of a new flicker bolt somewhere in the cloud
    private static final int FLICKER_CHANCE = 6;

    private final TribulationCloudSettings settings;
    private final ToIntFunction<T> strikeCounter;
    private final Map<UUID, CloudState> states = new HashMap<>();

    //the mesh only depends on the settings, so it is built once
    private final float[] ringRn = new float[RINGS];
    private final float[] ringLog = new float[RINGS];
    private final float[] ringY = new float[RINGS];
    private final float[] segmentCos = new float[SEGMENTS + 1];
    private final float[] segmentSin = new float[SEGMENTS + 1];
    private final float[] segmentTheta = new float[SEGMENTS + 1];

    /**
     * @param strikeCounter reads how many strikes have happened from the synced data,
     *                      the eye flashes whenever it goes up
     */
    public TribulationCloudRenderer(TribulationCloudSettings settings, ToIntFunction<T> strikeCounter) {
        this.settings = settings;
        this.strikeCounter = strikeCounter;

        ringLog[0] = CENTER_LOG;
        for (int ring = 1; ring < RINGS; ring++) {
            //log spaced, so rings bunch up near the eye where the funnel is steepest
            float rn = INNER_RING * (float) Math.pow(1.0F / INNER_RING, (ring - 1) / (float) (RINGS - 2));
            ringRn[ring] = rn;
            ringLog[ring] = (float) Math.log(rn);
            ringY[ring] = funnelY(rn);
        }
        //the last segment repeats the first at 2π so the uv never wraps inside a quad
        for (int segment = 0; segment <= SEGMENTS; segment++) {
            float theta = segment * Mth.TWO_PI / SEGMENTS;
            segmentTheta[segment] = theta;
            segmentCos[segment] = Mth.cos(theta);
            segmentSin[segment] = Mth.sin(theta);
        }
    }

    public TribulationCloudRenderer(TribulationCloudSettings settings) {
        this(settings, data -> 0);
    }

    //height of the base surface above the eye, steep at the eye and flattening towards the rim
    private float funnelY(float rn) {
        return settings.radius() * FUNNEL_DEPTH * Mth.sqrt(rn);
    }

    private CloudState state(ClientTribulation<T> tribulation) {
        return states.computeIfAbsent(tribulation.getId(), CloudState::new);
    }

    @Override
    public void clientTick(ClientTribulation<T> tribulation) {
        CloudState state = state(tribulation);
        RandomSource random = state.random;

        int strikes = strikeCounter.applyAsInt(tribulation.getData());
        if (state.lastStrikes >= 0 && strikes > state.lastStrikes) {
            state.flash(STRIKE_FLASH_TICKS);
        }
        state.lastStrikes = strikes;

        if (random.nextInt(FLICKER_CHANCE) == 0) {
            float angle = tribulation.getAge() * settings.rotationSpeed() * Mth.DEG_TO_RAD;
            state.bolts.add(createBolt(random, angle));
        }

        if (state.flashTicks > 0) state.flashTicks--;
        state.bolts.removeIf(bolt -> --bolt.ticksLeft <= 0);
    }

    @Override
    public void render(ClientTribulation<T> tribulation, RenderLevelStageEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return;
        CloudState state = state(tribulation);

        float partialTick = minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(true);
        float time = tribulation.getAge() + partialTick;
        Vec3 position = tribulation.getPosition(partialTick);
        Vec3 eye = new Vec3(position.x, settings.renderHeight(), position.z).subtract(minecraft.gameRenderer.getMainCamera().position());

        //no tint is white, which leaves the greyscale untouched
        int tint = ARGB.opaque(settings.tint().orElse(0xFFFFFF));
        float flash = state.flashTicks > 0 ? Math.clamp((state.flashTicks - partialTick) / state.flashLength, 0.0F, 1.0F) : 0.0F;
        //square pixels need a whole number around the circle so the grid closes at the seam
        ModRenderTypes.tribulationCloudPixels = Math.max(16, Math.round(Mth.TWO_PI * settings.radius() / settings.pixelSize()));

        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        poseStack.translate(eye.x, eye.y, eye.z);
        PoseStack.Pose pose = poseStack.last();
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();

        float angle = time * settings.rotationSpeed() * Mth.DEG_TO_RAD;
        //both sheets share the base surface, the shader pushes the underside down and the top up
        drawSheet(buffers, ModRenderTypes.tribulationClouds(), pose, state, time, angle, tint, flash);
        drawSheet(buffers, ModRenderTypes.tribulationCloudsTop(), pose, state, time, angle, tint, flash);

        if (!state.bolts.isEmpty()) {
            VertexConsumer lightning = buffers.getBuffer(ModRenderTypes.tribulationLightning());
            for (FlickerBolt bolt : state.bolts) {
                drawBolt(lightning, pose, bolt, tint);
            }
            buffers.endLastBatch();
        }
        poseStack.popPose();
    }

    @Override
    public void onRemoved(ClientTribulation<T> tribulation) {
        states.remove(tribulation.getId());
    }

    //──Clouds────────────────────────────────────────────────────────

    private void drawSheet(MultiBufferSource.BufferSource buffers, RenderType renderType, PoseStack.Pose pose, CloudState state,
                           float time, float angle, int tint, float flash) {
        VertexConsumer consumer = buffers.getBuffer(renderType);
        float radius = settings.radius();

        for (int ring = 0; ring < RINGS - 1; ring++) {
            for (int segment = 0; segment < SEGMENTS; segment++) {
                vertex(consumer, pose, state, ring, segment, radius, angle, tint, flash);
                vertex(consumer, pose, state, ring + 1, segment, radius, angle, tint, flash);
                vertex(consumer, pose, state, ring + 1, segment + 1, radius, angle, tint, flash);
                vertex(consumer, pose, state, ring, segment + 1, radius, angle, tint, flash);
            }
        }
        //read through the texture matrix when the batch is drawn, so it has to be set before ending it
        ModRenderTypes.tribulationCloudFlow = time * INFLOW + state.flowOffset;
        buffers.endLastBatch();
    }

    private void vertex(VertexConsumer consumer, PoseStack.Pose pose, CloudState state, int ring, int segment, float radius,
                        float angle, int tint, float flash) {
        float rn = ringRn[ring];
        float x = segmentCos[segment] * rn * radius;
        float y = ringY[ring];
        float z = segmentSin[segment] * rn * radius;
        //turns around the circle, bent into a spiral by the log radius
        float spiral = (segmentTheta[segment] - angle + TWIST * ringLog[ring]) / Mth.TWO_PI + state.spiralOffset;
        //uv carries the exact log radius, an 8 bit colour channel is too coarse for the shader's pixel grid.
        //rgb is the tint, alpha is how strongly lightning lights up this part of the cloud
        consumer.addVertex(pose, x, y, z)
                .setUv(spiral, ringLog[ring])
                .setColor(ARGB.color(Math.round(glow(state, rn, x, y, z, flash) * 255.0F), tint));
    }

    private static float glow(CloudState state, float rn, float x, float y, float z, float flash) {
        float glow = flash * Math.max(0.0F, 1.0F - rn / FLASH_RADIUS);
        for (FlickerBolt bolt : state.bolts) {
            Vec3 middle = bolt.middle;
            double dx = x - middle.x;
            double dy = y - middle.y;
            double dz = z - middle.z;
            float falloff = 1.0F - (float) (dx * dx + dy * dy + dz * dz) / (BOLT_GLOW_RADIUS * BOLT_GLOW_RADIUS);
            if (falloff > 0.0F) glow += falloff * bolt.strength();
        }
        return Math.min(glow, 1.0F);
    }

    //──Lightning────────────────────────────────────────────────────────

    /**
     * the spiral arms are lines where theta + TWIST * log(rn) - angle stays the same,
     * so the bolt walks inward along one of them in a few straight pieces
     */
    private FlickerBolt createBolt(RandomSource random, float angle) {
        float radius = settings.radius();
        float startRn = 0.15F + random.nextFloat() * 0.65F;
        float startTheta = random.nextFloat() * Mth.TWO_PI;
        float arm = startTheta + TWIST * (float) Math.log(startRn) - angle;

        float length = 12.0F + random.nextFloat() * 18.0F;
        //along an arm the path is about sqrt(1 + TWIST^2) times longer than the change in radius
        float endRn = Math.max(0.06F, startRn - length / (radius * Mth.sqrt(1.0F + TWIST * TWIST)));
        int pieces = 3 + random.nextInt(3);

        List<Vec3> points = new ArrayList<>(pieces + 1);
        for (int i = 0; i <= pieces; i++) {
            float rn = Mth.lerp(i / (float) pieces, startRn, endRn);
            float theta = arm - TWIST * (float) Math.log(rn) + angle;
            float jitter = i == 0 || i == pieces ? 0.0F : 1.2F;
            points.add(new Vec3(
                    Mth.cos(theta) * rn * radius + (random.nextFloat() - 0.5F) * jitter,
                    //just under the base surface, so the billows hanging lower hide parts of it
                    funnelY(rn) - 2.5F + (random.nextFloat() - 0.5F) * jitter,
                    Mth.sin(theta) * rn * radius + (random.nextFloat() - 0.5F) * jitter
            ));
        }
        return new FlickerBolt(points, 3 + random.nextInt(4));
    }

    //drawn like vanilla lightning: square tubes of growing width stacked for the glow
    private static void drawBolt(VertexConsumer consumer, PoseStack.Pose pose, FlickerBolt bolt, int tint) {
        int color = ARGB.multiply(ARGB.colorFromFloat(0.3F, 0.45F, 0.45F, 0.5F), tint);
        List<Vec3> points = bolt.points;
        for (int i = 0; i < points.size() - 1; i++) {
            Vec3 start = points.get(i);
            Vec3 end = points.get(i + 1);
            //two directions across the piece, so the tube keeps its thickness however the piece is angled
            Vec3 direction = end.subtract(start).normalize();
            Vec3 side = direction.cross(new Vec3(0.0D, 1.0D, 0.0D));
            side = side.lengthSqr() < 1.0E-4D ? new Vec3(1.0D, 0.0D, 0.0D) : side.normalize();
            Vec3 up = side.cross(direction);
            for (int layer = 0; layer < 4; layer++) {
                double width = 0.05D + layer * 0.1D;
                Vec3 a = side.add(up).scale(width);
                Vec3 b = side.subtract(up).scale(width);
                tubeSide(consumer, pose, color, start, end, a, b);
                tubeSide(consumer, pose, color, start, end, b, a.reverse());
                tubeSide(consumer, pose, color, start, end, a.reverse(), b.reverse());
                tubeSide(consumer, pose, color, start, end, b.reverse(), a);
            }
        }
    }

    private static void tubeSide(VertexConsumer consumer, PoseStack.Pose pose, int color, Vec3 start, Vec3 end, Vec3 cornerA, Vec3 cornerB) {
        boltVertex(consumer, pose, color, start.add(cornerA));
        boltVertex(consumer, pose, color, start.add(cornerB));
        boltVertex(consumer, pose, color, end.add(cornerB));
        boltVertex(consumer, pose, color, end.add(cornerA));
    }

    private static void boltVertex(VertexConsumer consumer, PoseStack.Pose pose, int color, Vec3 position) {
        consumer.addVertex(pose, (float) position.x, (float) position.y, (float) position.z).setColor(color);
    }

    //──State────────────────────────────────────────────────────────

    private static final class CloudState {
        private final RandomSource random = RandomSource.create();
        private final List<FlickerBolt> bolts = new ArrayList<>();
        //shifts the noise so tribulations next to each other do not look identical
        private final float spiralOffset;
        private final float flowOffset;
        //-1 until the first tick, so joining mid tribulation does not flash
        private int lastStrikes = -1;
        private int flashTicks;
        private int flashLength = 1;

        private CloudState(UUID id) {
            this.spiralOffset = (id.getLeastSignificantBits() & 0xFF) / 256.0F;
            this.flowOffset = (id.getMostSignificantBits() & 0xFFF) / 16.0F;
        }

        //a weaker flash never cuts a stronger one short
        private void flash(int ticks) {
            if (ticks < flashTicks) return;
            flashTicks = ticks;
            flashLength = ticks;
        }
    }

    //a bolt following a spiral arm just under the cloud, relative to the eye
    private static final class FlickerBolt {
        private final List<Vec3> points;
        private final Vec3 middle;
        private final int lifetime;
        private int ticksLeft;

        private FlickerBolt(List<Vec3> points, int lifetime) {
            this.points = points;
            this.middle = points.get(points.size() / 2);
            this.lifetime = lifetime;
            this.ticksLeft = lifetime;
        }

        private float strength() {
            return ticksLeft / (float) lifetime;
        }
    }
}
