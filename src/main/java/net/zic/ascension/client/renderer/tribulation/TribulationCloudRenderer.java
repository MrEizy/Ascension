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
import net.zic.ascension.api.ascension.core.tribulation.TribulationCloudPalette;
import net.zic.ascension.api.ascension.core.tribulation.TribulationCloudSettings;
import net.zic.ascension.api.ascension.core.tribulation.TribulationCloudTransition;
import net.zic.ascension.api.ascension.core.tribulation.TribulationData;
import net.zic.ascension.api.ascension.core.tribulation.client.ClientTribulation;
import net.zic.ascension.api.ascension.core.tribulation.client.TribulationRenderer;
import net.zic.ascension.client.renderer.ModRenderTypes;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.ToIntFunction;

/**
 * renders a hurricane of storm clouds around a glowing eye, with lightning crawling along the spiral rainbands.
 * the cloud base is flat, the shader gives it thickness: a tall eyewall, thick bands and thin cloud between them.
 * the eye follows the tribulation on x/z and sits at the configured y.
 * <p>
 * the mesh is a funnel of rings drawn as two sheets, the tribulation_clouds shader bulges them apart into billows and shades them per pixel.
 * each vertex passes its spiral coordinate and log radius in the uv and how lit up it is in the colour alpha,
 * the pixel count and inflow offset go through the texture matrix
 */
public class TribulationCloudRenderer<T extends TribulationData> implements TribulationRenderer<T> {
    //dense enough for the shader to displace the billows smoothly. ring 0 is the centre point
    protected static final int RINGS = 90;
    protected static final int SEGMENTS = 200;
    //the innermost real ring as a fraction of the radius, the shader draws the glowing eye from further out than this
    private static final float INNER_RING = 0.01F;
    //log radius given to the centre point, log(0) would be -infinity
    private static final float CENTER_LOG = (float) Math.log(0.001F);
    //full thickness of the storm at the eyewall, as a fraction of the radius
    private static final float THICKNESS = 0.3F;
    //how much of the thickness rises above the base, must match TOP_SHARE in the tribulation_clouds vertex shader
    private static final float TOP_SHARE = 0.65F;
    //radians the spiral arms turn per unit of log radius
    protected static final float TWIST = 2.0F;
    //how fast the billows drift towards the eye, in noise cells per tick
    protected static final float INFLOW = 0.02F;
    //strikes light up the clouds within this fraction of the radius around the eye
    private static final float FLASH_RADIUS = 0.3F;
    //flicker bolts light up the clouds within this many blocks of their middle
    private static final float BOLT_GLOW_RADIUS = 14.0F;

    //bolts are drawn as stacked glowing tubes, so they are dimmer than the eye highlight they are coloured with
    private static final float BOLT_BRIGHTNESS = 0.75F;

    private static final int STRIKE_FLASH_TICKS = 6;
    //1 in this chance per tick of a new flicker bolt somewhere in the cloud
    private static final int FLICKER_CHANCE = 6;

    //the streams a gathering vortex pulls its cloud in along, from unseen source clouds past the rim.
    //STREAMS, STREAM_SOURCE and STREAM_WIDTH / 2π must match VORTEX_STREAMS, VORTEX_STREAM_SOURCE and VORTEX_STREAM_WIDTH in the tribulation_clouds include
    private static final int STREAMS = 3;
    //log of where the streams start and end, as a fraction of the radius. they start past the rim and run into the eye
    private static final float STREAM_SOURCE = 0.47F;
    private static final float STREAM_END = (float) Math.log(0.03F);
    //half the width of a stream as a fraction of its distance from the eye, so it narrows as it winds in like the spiral arms do
    private static final float STREAM_WIDTH = 0.055F * Mth.TWO_PI;
    //how far below the cloud base the streams run, as a fraction of the radius, so the gathered cloud swallows them
    private static final float STREAM_DROP = 0.015F;
    //how fast the cloud slides along a stream, in log radius per tick
    private static final float STREAM_FLOW = 0.01F;
    private static final int STREAM_SEGMENTS = 96;

    //coarser meshes for storms that look small on screen. each step must divide SEGMENTS so the seam still closes.
    //a step is used while the storm's radius divided by its distance is at least the matching size
    private static final int[] LOD_STEPS = {1, 2, 4};
    private static final float[] LOD_SIZES = {0.5F, 0.2F, 0.0F};
    //how far past a size the storm has to go before it switches level, so it does not flicker between two
    private static final float LOD_HYSTERESIS = 0.1F;

    protected final TribulationCloudSettings settings;
    private final ToIntFunction<T> strikeCounter;
    private final FormationProgress<T> formation;
    private final Map<UUID, CloudState> states = new HashMap<>();

    //the mesh only depends on the settings, so it is built once
    protected final float[] ringRn = new float[RINGS];
    protected final float[] ringLog = new float[RINGS];
    //how far each ring is bent into the spiral, in turns
    protected final float[] ringTwist = new float[RINGS];
    protected final float[] segmentCos = new float[SEGMENTS + 1];
    protected final float[] segmentSin = new float[SEGMENTS + 1];
    //how far around the circle each segment is, in turns
    protected final float[] segmentTurns = new float[SEGMENTS + 1];

    /**
     * @param strikeCounter reads how many strikes have happened from the synced data,
     *                      the eye flashes whenever it goes up
     * @param formation how far the storm has gathered and which transition it is in
     */
    public TribulationCloudRenderer(TribulationCloudSettings settings, ToIntFunction<T> strikeCounter, FormationProgress<T> formation) {
        this.settings = settings;
        this.strikeCounter = strikeCounter;
        this.formation = formation;

        ringLog[0] = CENTER_LOG;
        for (int ring = 1; ring < RINGS; ring++) {
            //log spaced, so rings bunch up near the eye where the funnel is steepest
            float rn = INNER_RING * (float) Math.pow(1.0F / INNER_RING, (ring - 1) / (float) (RINGS - 2));
            ringRn[ring] = rn;
            ringLog[ring] = (float) Math.log(rn);
        }
        for (int ring = 0; ring < RINGS; ring++) {
            ringTwist[ring] = TWIST * ringLog[ring] / Mth.TWO_PI;
        }
        //the last segment repeats the first at 2π so the uv never wraps inside a quad
        for (int segment = 0; segment <= SEGMENTS; segment++) {
            float theta = segment * Mth.TWO_PI / SEGMENTS;
            segmentTurns[segment] = segment / (float) SEGMENTS;
            segmentCos[segment] = Mth.cos(theta);
            segmentSin[segment] = Mth.sin(theta);
        }
    }

    public TribulationCloudRenderer(TribulationCloudSettings settings, ToIntFunction<T> strikeCounter) {
        this(settings, strikeCounter, (data, ticksSinceSync) -> Formation.FORMED);
    }

    public TribulationCloudRenderer(TribulationCloudSettings settings) {
        this(settings, data -> 0);
    }

    //the client only gets the data when it changes, so it counts the ticks since then itself
    private void syncFormation(CloudState state, ClientTribulation<T> tribulation) {
        T data = tribulation.getData();
        if (data != state.lastData) {
            state.lastData = data;
            state.ticksSinceSync = 0;
        }
    }

    protected Formation formation(CloudState state, ClientTribulation<T> tribulation, float partialTick) {
        syncFormation(state, tribulation);
        return formation.progress(tribulation.getData(), state.ticksSinceSync + partialTick);
    }

    protected CloudState state(ClientTribulation<T> tribulation) {
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

        syncFormation(state, tribulation);
        state.ticksSinceSync++;
        Formation formation = formation(state, tribulation, 0.0F);
        //lightning only crawls through a storm that has mostly gathered
        if (formation.formed() > formation.lightningAt() && random.nextInt(FLICKER_CHANCE) == 0) {
            FlickerBolt bolt = createBolt(state, random);
            if (bolt != null) state.bolts.add(bolt);
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
        Formation formation = formation(state, tribulation, partialTick);
        float formed = formation.formed();
        //the storm spins up as it gathers, so the angle is added up frame by frame instead of worked out from the time
        float dt = state.lastTime < 0.0F ? 0.0F : Math.clamp(time - state.lastTime, 0.0F, 5.0F);
        state.lastTime = time;
        state.angle += dt * settings.rotationSpeed() * Mth.DEG_TO_RAD * Mth.lerp(formed, formation.spinStart(), 1.0F);
        //nothing has gathered yet
        if (formed <= 0.0F) return;
        Vec3 position = tribulation.getPosition(partialTick);
        Vec3 eye = new Vec3(position.x, settings.renderHeight(), position.z).subtract(minecraft.gameRenderer.getMainCamera().position());

        //no tint is white, which leaves the greyscale untouched
        int tint = ARGB.opaque(settings.tint().orElse(0xFFFFFF));
        float flash = state.flashTicks > 0 ? Math.clamp((state.flashTicks - partialTick) / state.flashLength, 0.0F, 1.0F) : 0.0F;
        packColors(settings.cloudColors(), 0);
        packColors(settings.eyeColors(), 3);
        ModRenderTypes.tribulationCloudFormation = formed;
        //a fully formed storm has nothing left to gather, so any transition's settings do
        TribulationCloudTransition transition = formation.transition() != null ? formation.transition() : settings.forming();
        ModRenderTypes.tribulationCloudStyle = transition.style().ordinal();
        ModRenderTypes.tribulationCloudPatchiness = transition.patchiness();
        ModRenderTypes.tribulationCloudClosedAt = transition.closedAt();
        ModRenderTypes.tribulationCloudEyeAt = transition.eyeAt();
        ModRenderTypes.tribulationCloudDissipating = formation.dissipating() ? 1.0F : 0.0F;

        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        poseStack.translate(eye.x, eye.y, eye.z);
        PoseStack.Pose pose = poseStack.last();
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();

        //the camera relative to the eye, for skipping the parts of a huge storm past the far end of the view
        float viewDistance = minecraft.options.getEffectiveRenderDistance() * 16.0F * 4.0F;
        renderClouds(buffers, pose, state, formation, eye.reverse(), time, partialTick, tint, flash, viewDistance);
        renderStreams(buffers, pose, state, formation, time, tint);

        if (!state.bolts.isEmpty()) {
            VertexConsumer lightning = buffers.getBuffer(ModRenderTypes.tribulationLightning());
            for (FlickerBolt bolt : state.bolts) {
                drawBolt(lightning, pose, bolt, settings.eyeColors().highlight());
            }
            buffers.endLastBatch();
        }
        poseStack.popPose();
    }

    @Override
    public void onRemoved(ClientTribulation<T> tribulation) {
        states.remove(tribulation.getId());
    }

    //each rgb colour as one float the shader unpacks, whole numbers below 2^24 survive the trip exactly
    private static void packColors(TribulationCloudPalette palette, int start) {
        ModRenderTypes.tribulationCloudColors[start] = palette.shadow() & 0xFFFFFF;
        ModRenderTypes.tribulationCloudColors[start + 1] = palette.base() & 0xFFFFFF;
        ModRenderTypes.tribulationCloudColors[start + 2] = palette.highlight() & 0xFFFFFF;
    }

    //──Clouds────────────────────────────────────────────────────────

    /**
     * draws the cloud itself, the colours and formation are already in ModRenderTypes and the pose is at the eye
     * @param camera the camera relative to the eye
     * @param time the tribulation's age, partial tick included
     * @param viewDistance parts of the storm further than this from the camera are past the end of the view
     */
    protected void renderClouds(MultiBufferSource.BufferSource buffers, PoseStack.Pose pose, CloudState state, Formation formation,
                                Vec3 camera, float time, float partialTick, int tint, float flash, float viewDistance) {
        //square pixels need a whole number around the circle so the grid closes at the seam
        ModRenderTypes.tribulationCloudPixels = Math.max(16, Math.round(Mth.TWO_PI * settings.radius() / settings.pixelSize()));
        ModRenderTypes.tribulationCloudThickness = settings.radius() * THICKNESS;

        float angle = state.angle;
        //both sheets share the base surface, the shader pushes the underside down and the top up.
        //from below the base the top sheet is always hidden behind the underside, and from above the storm the underside is hidden,
        //so only the sheet that can be seen is drawn. the shader discards, so hidden fragments would otherwise still be shaded
        double cameraHeight = camera.y;
        int step = lodStep(state, (float) (settings.radius() / Math.max(camera.length(), 1.0D)));
        float cameraX = (float) camera.x;
        float cameraZ = (float) camera.z;
        if (cameraHeight < settings.radius() * THICKNESS * TOP_SHARE) {
            drawSheet(buffers, ModRenderTypes.tribulationClouds(), pose, state, time, angle, tint, flash, step, cameraX, cameraZ, viewDistance);
        }
        if (cameraHeight > 0.0D) {
            drawSheet(buffers, ModRenderTypes.tribulationCloudsTop(), pose, state, time, angle, tint, flash, step, cameraX, cameraZ, viewDistance);
        }
    }

    //a gathering vortex pulls its cloud in along streams from outside the storm
    protected void renderStreams(MultiBufferSource.BufferSource buffers, PoseStack.Pose pose, CloudState state, Formation formation,
                                 float time, int tint) {
        if (formation.transition() != null && formation.transition().style() == TribulationCloudTransition.Style.VORTEX
                && !formation.dissipating()) {
            drawStreams(buffers, pose, state, time, tint);
        }
    }

    //shadow at level 0, base at 0.5 and highlight at 1, the same as cloudRamp in the tribulation_clouds include
    protected static int ramp(TribulationCloudPalette palette, float level) {
        return level < 0.5F
                ? ARGB.srgbLerp(level * 2.0F, palette.shadow(), palette.base())
                : ARGB.srgbLerp(level * 2.0F - 1.0F, palette.base(), palette.highlight());
    }

    /**
     * the streams follow the spiral arms in from past the rim to the eye, as flat ribbons just under the cloud base.
     * each is placed where the clouds' spiral coordinate is a whole fraction of a turn, which is where the
     * tribulation_clouds shader settles the cloud they bring in
     */
    private void drawStreams(MultiBufferSource.BufferSource buffers, PoseStack.Pose pose, CloudState state, float time, int tint) {
        VertexConsumer consumer = buffers.getBuffer(ModRenderTypes.tribulationStreams());
        int color = ARGB.color(0, tint);
        for (int stream = 0; stream < STREAMS; stream++) {
            //the angle the clouds' spiral coordinate is stream / STREAMS at the centre, undoing the offset and spin the sheets add
            float start = Mth.TWO_PI * (stream / (float) STREAMS - state.spiralOffset) + state.angle;
            for (int i = 0; i < STREAM_SEGMENTS; i++) {
                streamVertex(consumer, pose, start, i, -1.0F, color);
                streamVertex(consumer, pose, start, i, 1.0F, color);
                streamVertex(consumer, pose, start, i + 1, 1.0F, color);
                streamVertex(consumer, pose, start, i + 1, -1.0F, color);
            }
        }
        //read through the texture matrix when the batch is drawn, so it has to be set before ending it
        ModRenderTypes.tribulationCloudFlow = time * STREAM_FLOW + state.flowOffset;
        buffers.endLastBatch();
    }

    private void streamVertex(VertexConsumer consumer, PoseStack.Pose pose, float start, int segment, float side, int color) {
        float log = Mth.lerp(segment / (float) STREAM_SEGMENTS, STREAM_SOURCE, STREAM_END);
        float r = (float) Math.exp(log) * settings.radius();
        //winds the same way as the spiral arms, turning further round the closer it gets
        float theta = start - TWIST * log;
        float cos = Mth.cos(theta);
        float sin = Mth.sin(theta);
        //the direction along the spiral, turned flat by a quarter to go across it
        float alongX = cos + TWIST * sin;
        float alongZ = sin - TWIST * cos;
        float alongLength = Mth.sqrt(alongX * alongX + alongZ * alongZ);
        float offset = STREAM_WIDTH * r * side / alongLength;
        float x = cos * r - alongZ * offset;
        float z = sin * r + alongX * offset;
        float y = cloudBaseHeight(r / settings.radius()) - STREAM_DROP * settings.radius();
        //uv is across the stream from -1 to 1 and the exact log radius
        consumer.addVertex(pose, x, y, z)
                .setUv(side, log)
                .setColor(color);
    }

    //height of the cloud base above the eye at this fraction of the radius, the bolts crawl just under it
    protected float cloudBaseHeight(float rn) {
        return 0.0F;
    }

    //whether anything is lighting the clouds up this frame
    protected static boolean isLit(CloudState state, float flash) {
        return flash > 0.0F || !state.bolts.isEmpty();
    }

    //picks the mesh level from how big the storm looks, only moving to another level once it is clearly past it
    protected static int lodStep(CloudState state, float apparentSize) {
        int level = state.lodLevel;
        while (level > 0 && apparentSize >= LOD_SIZES[level - 1] * (1.0F + LOD_HYSTERESIS)) level--;
        while (level < LOD_STEPS.length - 1 && apparentSize < LOD_SIZES[level] * (1.0F - LOD_HYSTERESIS)) level++;
        state.lodLevel = level;
        return LOD_STEPS[level];
    }

    /**
     * @param step draws every nth ring and segment, coarser for storms that look small
     * @param viewDistance quads further than this from the camera are past the end of the view and skipped
     */
    private void drawSheet(MultiBufferSource.BufferSource buffers, RenderType renderType, PoseStack.Pose pose, CloudState state,
                           float time, float angle, int tint, float flash, int step, float cameraX, float cameraZ, float viewDistance) {
        VertexConsumer consumer = buffers.getBuffer(renderType);
        float radius = settings.radius();
        //the part of the spiral coordinate that is the same for every vertex this frame
        float spiralShift = state.spiralOffset - angle / Mth.TWO_PI;
        //most frames have nothing lighting the clouds up, then every vertex gets the same colour
        boolean lit = isLit(state, flash);
        int unlitColor = ARGB.color(0, tint);

        for (int ring = 0; ring < RINGS - 1; ring += step) {
            int nextRing = Math.min(ring + step, RINGS - 1);
            //the middle of this ring's quads, and how far a corner can be from it, in blocks
            float middleRn = (ringRn[ring] + ringRn[nextRing]) * 0.5F;
            float middle = middleRn * radius;
            float reach = (ringRn[nextRing] - ringRn[ring]) * radius + Mth.TWO_PI * middle * step / SEGMENTS;
            float limit = viewDistance + reach;
            for (int segment = 0; segment < SEGMENTS; segment += step) {
                int nextSegment = segment + step;
                //past the far end of the view, nothing of it would be drawn anyway
                float dx = segmentCos[segment] * middle - cameraX;
                float dz = segmentSin[segment] * middle - cameraZ;
                if (dx * dx + dz * dz > limit * limit) continue;

                vertex(consumer, pose, state, ring, segment, radius, spiralShift, tint, lit, unlitColor, flash);
                vertex(consumer, pose, state, nextRing, segment, radius, spiralShift, tint, lit, unlitColor, flash);
                vertex(consumer, pose, state, nextRing, nextSegment, radius, spiralShift, tint, lit, unlitColor, flash);
                vertex(consumer, pose, state, ring, nextSegment, radius, spiralShift, tint, lit, unlitColor, flash);
            }
        }
        //read through the texture matrix when the batch is drawn, so it has to be set before ending it
        ModRenderTypes.tribulationCloudFlow = time * INFLOW + state.flowOffset;
        buffers.endLastBatch();
    }

    private void vertex(VertexConsumer consumer, PoseStack.Pose pose, CloudState state, int ring, int segment, float radius,
                        float spiralShift, int tint, boolean lit, int unlitColor, float flash) {
        float rn = ringRn[ring];
        float x = segmentCos[segment] * rn * radius;
        //the flat cloud base, the shader pushes the sheets up and down from here
        float y = 0.0F;
        float z = segmentSin[segment] * rn * radius;
        //turns around the circle, bent into a spiral by the log radius
        float spiral = segmentTurns[segment] + ringTwist[ring] + spiralShift;
        //uv carries the exact log radius, an 8 bit colour channel is too coarse for the shader's pixel grid.
        //rgb is the tint, alpha is how strongly lightning lights up this part of the cloud
        int color = lit ? ARGB.color(Math.round(glow(state, rn, x, y, z, flash) * 255.0F), tint) : unlitColor;
        consumer.addVertex(pose, x, y, z)
                .setUv(spiral, ringLog[ring])
                .setColor(color);
    }

    protected static float glow(CloudState state, float rn, float x, float y, float z, float flash) {
        float glow = flash * Math.max(0.0F, 1.0F - rn / FLASH_RADIUS);
        for (FlickerBolt bolt : state.bolts) {
            float dx = x - bolt.middleX;
            float dy = y - bolt.middleY;
            float dz = z - bolt.middleZ;
            float falloff = 1.0F - (dx * dx + dy * dy + dz * dz) / (BOLT_GLOW_RADIUS * BOLT_GLOW_RADIUS);
            if (falloff > 0.0F) glow += falloff * bolt.strength();
        }
        return Math.min(glow, 1.0F);
    }

    //──Lightning────────────────────────────────────────────────────────

    /**
     * the spiral arms are lines where theta + TWIST * log(rn) - angle stays the same,
     * so the bolt walks inward along one of them in a few straight pieces.
     * storms that are not one solid sheet can return null where there is no cloud to crawl through
     */
    protected @Nullable FlickerBolt createBolt(CloudState state, RandomSource random) {
        float angle = state.angle;
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
                    //just under the cloud base, so the bands hanging lower hide parts of it
                    cloudBaseHeight(rn) - 3.0F + (random.nextFloat() - 0.5F) * jitter,
                    Mth.sin(theta) * rn * radius + (random.nextFloat() - 0.5F) * jitter
            ));
        }
        return new FlickerBolt(points, 3 + random.nextInt(4));
    }

    //drawn like vanilla lightning: square tubes of growing width stacked for the glow
    //the bolts come out of the eye, so they take the eye highlight. the default highlight gives the vanilla lightning colour
    private static void drawBolt(VertexConsumer consumer, PoseStack.Pose pose, FlickerBolt bolt, int eyeHighlight) {
        int color = ARGB.color(0.3F, ARGB.scaleRGB(eyeHighlight, BOLT_BRIGHTNESS));
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

    /**
     * how far the storm has gathered, from the synced data
     * @param ticksSinceSync ticks the client counted since this data arrived, partial ticks included
     */
    @FunctionalInterface
    public interface FormationProgress<T> {
        Formation progress(T data, float ticksSinceSync);
    }

    /**
     * @param progress how far the storm has gathered before easing, 0 for clear sky and 1 for the full storm
     * @param transition the transition it is gathering or breaking up with, null once it has fully formed
     * @param dissipating true while it breaks up. most styles just play backwards, the vortex breaks up in its own order
     */
    public record Formation(float progress, @Nullable TribulationCloudTransition transition, boolean dissipating) {
        public static final Formation FORMED = new Formation(1.0F, null, false);

        //the eased formation the shaders and spin use
        public float formed() {
            return transition == null ? 1.0F : transition.ease(progress);
        }

        public float lightningAt() {
            return transition == null ? 0.0F : transition.lightningAt();
        }

        public float spinStart() {
            return transition == null ? 1.0F : transition.spinStart();
        }
    }

    protected static final class CloudState {
        final RandomSource random = RandomSource.create();
        final List<FlickerBolt> bolts = new ArrayList<>();
        //shifts the noise so tribulations next to each other do not look identical
        final float spiralOffset;
        final float flowOffset;
        //-1 until the first tick, so joining mid tribulation does not flash
        private int lastStrikes = -1;
        //the synced data last seen, and the ticks counted since it arrived
        private Object lastData;
        private int ticksSinceSync;
        //the mesh level it is drawn at, an index into LOD_STEPS
        int lodLevel;
        //how far the storm has turned, and the render time it was last advanced at (-1 before the first frame)
        float angle;
        private float lastTime = -1.0F;
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
    static final class FlickerBolt {
        private final List<Vec3> points;
        //the middle as floats, it is compared against every cloud vertex
        private final float middleX;
        private final float middleY;
        private final float middleZ;
        private final int lifetime;
        private int ticksLeft;

        FlickerBolt(List<Vec3> points, int lifetime) {
            this.points = points;
            Vec3 middle = points.get(points.size() / 2);
            this.middleX = (float) middle.x;
            this.middleY = (float) middle.y;
            this.middleZ = (float) middle.z;
            this.lifetime = lifetime;
            this.ticksLeft = lifetime;
        }

        private float strength() {
            return ticksLeft / (float) lifetime;
        }
    }
}
