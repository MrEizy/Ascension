package net.zic.ascension.client.renderer.tribulation;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.phys.Vec3;
import net.zic.ascension.api.ascension.core.tribulation.TribulationCloudSettings;
import net.zic.ascension.api.ascension.core.tribulation.TribulationCloudTransition;
import net.zic.ascension.api.ascension.core.tribulation.TribulationData;
import net.zic.ascension.api.ascension.core.tribulation.client.ClientTribulation;
import net.zic.ascension.client.renderer.ModRenderTypes;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.ToIntFunction;

/**
 * a hurricane made only of minecraft clouds. clouds cut from the vanilla cloud texture appear around the edge of the storm
 * in the sky's own cloud colour, are pulled inward in a spiral, take on the storm's colours as they go, and are swallowed by the eye.
 * the flashes and lightning work the same as in the other storms.
 * <p>
 * each cloud is drawn like a vanilla one, as 12 by 4 by 12 block cells with flat shaded faces, but it slides smoothly instead of
 * being stuck to the world's cloud grid. the clouds are simulated on the client every tick and drawn between ticks
 */
public class MinecraftCloudTribulationRenderer<T extends TribulationData> extends TribulationCloudRenderer<T> {
    //the size of a vanilla cloud cell in blocks
    private static final float CELL = 12.0F;
    private static final float CELL_HEIGHT = 4.0F;
    //how much of the forming time a cloud takes to be pulled from the edge to the eye, so the storm is full by the first strike
    private static final float TRAVEL_SHARE = 0.6F;
    private static final int MIN_TRAVEL_TICKS = 160;
    private static final int MAX_TRAVEL_TICKS = 1200;
    //how far into forming the clouds are coming in at their full rate
    private static final float SPAWN_RAMP = 0.35F;
    //clouds appear this far out, as a fraction of the radius, so they drift in from outside the storm
    private static final float SPAWN_MIN = 1.0F;
    private static final float SPAWN_MAX = 1.3F;
    //radians a cloud winds round the eye on top of the storm's spin, per unit of log radius it is pulled in, so it turns faster further in
    private static final float SWIRL = 1.2F;
    //how far a cloud can sit above or below the others, in blocks, so the faces of overlapping clouds never meet in one plane
    private static final float HEIGHT_JITTER = 6.0F;
    //how much cloud the storm is aiming for, as cells per cell of area. more than 1 as the clouds overlap and crowd towards the eye
    private static final float COVERAGE = 1.2F;
    private static final int MAX_CLOUDS = 600;
    //radius of the clear eye, as a fraction of the radius but never under this many blocks
    private static final float EYE = 0.06F;
    private static final float MIN_EYE_BLOCKS = 18.0F;
    //clouds start turning the storm's colours here and are fully coloured here, as fractions of the radius
    private static final float COLOUR_START = 0.95F;
    private static final float COLOUR_FULL = 0.7F;
    //clouds tried for each flicker bolt before giving up, and how far its middle points stray in blocks
    private static final int BOLT_ATTEMPTS = 8;
    private static final float BOLT_JITTER = 3.0F;
    //half the size of the rift's quad as a multiple of the eye radius, its height above the eye in blocks, and its clock per tick
    private static final float RIFT_SIZE = 1.1F;
    private static final float RIFT_HEIGHT = 2.0F;
    private static final float RIFT_TIME = 0.05F;

    //vanilla's face shading: bottom, top, north and south, west and east
    private static final float[] VANILLA_SHADE = {0.7F, 1.0F, 0.8F, 0.9F};
    //where the same faces sit on the storm palettes, from shadow to highlight
    private static final float[] STORM_LEVEL = {0.0F, 1.0F, 0.45F, 0.6F};
    private static final int BOTTOM = 0;
    private static final int TOP = 1;
    private static final int NORTH_SOUTH = 2;
    private static final int WEST_EAST = 3;

    private final Map<CloudState, CloudField> fields = new IdentityHashMap<>();

    public MinecraftCloudTribulationRenderer(TribulationCloudSettings settings, ToIntFunction<T> strikeCounter, FormationProgress<T> formation) {
        super(settings, strikeCounter, formation);
    }

    private CloudField field(CloudState state) {
        return fields.computeIfAbsent(state, s -> new CloudField());
    }

    @Override
    public void clientTick(ClientTribulation<T> tribulation) {
        super.clientTick(tribulation);
        CloudState state = state(tribulation);
        CloudField field = field(state);
        Formation formation = formation(state, tribulation, 0.0F);
        //a player who arrives part way through sees the clouds that would already have been pulled in
        if (!field.warmedUp) {
            field.warmedUp = true;
            warmUp(field, state.random, formation);
        }
        step(field, state.random, formation);
    }

    @Override
    public void onRemoved(ClientTribulation<T> tribulation) {
        fields.remove(state(tribulation));
        super.onRemoved(tribulation);
    }

    //a storm that has not formed yet has nothing to have pulled in
    private void warmUp(CloudField field, RandomSource random, Formation formation) {
        TribulationCloudTransition transition = formation.transition();
        int ticks;
        Formation warmFormation = formation;
        if (transition == null || formation.dissipating()) {
            //running, or already breaking up: it ran fully formed for at least the travel time before
            ticks = travelTicks();
            warmFormation = Formation.FORMED;
        } else {
            ticks = Math.min(travelTicks(), Math.round(formation.progress() * transition.ticks()));
        }
        for (int i = 0; i < ticks; i++) {
            step(field, random, warmFormation);
        }
        for (Cloud cloud : field.clouds) {
            cloud.lastRn = cloud.rn;
        }
    }

    private void step(CloudField field, RandomSource random, Formation formation) {
        int travelTicks = travelTicks();
        //a steady pull while gathering and running, so the storm fills in time. breaking up, it slows down with the spin
        float pull = (formation.dissipating() ? Mth.lerp(formation.formed(), formation.spinStart(), 1.0F) : 1.0F) / travelTicks;
        field.clouds.removeIf(cloud -> {
            cloud.lastRn = cloud.rn;
            cloud.rn -= pull;
            return cloud.rn <= 0.0F;
        });

        //nothing new is pulled in once the storm starts breaking up. while gathering, a trickle that quickly builds to the full rate
        if (formation.dissipating()) return;
        float rate = targetClouds() / (float) travelTicks;
        if (formation.transition() != null) rate *= smoothstep(0.0F, SPAWN_RAMP, formation.progress());
        field.spawnBudget += rate;
        List<MinecraftCloudShapes.Shape> shapes = MinecraftCloudShapes.get();
        while (field.spawnBudget >= 1.0F) {
            field.spawnBudget -= 1.0F;
            field.clouds.add(new Cloud(
                    shapes.get(random.nextInt(shapes.size())),
                    Mth.lerp(random.nextFloat(), SPAWN_MIN, SPAWN_MAX),
                    random.nextFloat() * Mth.TWO_PI,
                    (random.nextFloat() * 2.0F - 1.0F) * HEIGHT_JITTER,
                    random.nextInt()
            ));
        }
    }

    //ticks a cloud takes from the edge to the eye, worked out from how long the storm takes to form
    private int travelTicks() {
        return Mth.clamp(Math.round(settings.forming().ticks() * TRAVEL_SHARE), MIN_TRAVEL_TICKS, MAX_TRAVEL_TICKS);
    }

    //enough clouds to cover the storm, given how big the average one is
    private int targetClouds() {
        float area = Mth.PI * settings.radius() * settings.radius();
        float cloudArea = MinecraftCloudShapes.averageCells() * CELL * CELL;
        return Mth.clamp(Math.round(COVERAGE * area / cloudArea), 8, MAX_CLOUDS);
    }

    @Override
    protected void renderClouds(MultiBufferSource.BufferSource buffers, PoseStack.Pose pose, CloudState state, Formation formation,
                                Vec3 camera, float time, float partialTick, int tint, float flash, float viewDistance) {
        float formed = formation.formed();
        float eyeAt = formation.transition() != null ? formation.transition().eyeAt() : 0.0F;
        //the eye lights once the storm has mostly gathered, and is the first thing to go dark as it breaks up
        float eyeOn = formation.transition() == null ? 1.0F : smoothstep(eyeAt, 1.0F, formed);
        CloudField field = fields.get(state);
        if (field != null && !field.clouds.isEmpty()) {
            drawClouds(buffers, pose, state, field, formation, camera, partialTick, tint, flash, viewDistance, eyeOn);
        }
        //the rift tears open in the eye, over the clouds
        if (eyeOn > 0.0F) {
            drawRift(buffers, pose, state, time, flash);
        }
    }

    private void drawClouds(MultiBufferSource.BufferSource buffers, PoseStack.Pose pose, CloudState state, CloudField field, Formation formation,
                            Vec3 camera, float partialTick, int tint, float flash, float viewDistance, float eyeOn) {
        float radius = settings.radius();
        float formed = formation.formed();
        boolean dissipating = formation.dissipating();
        float eyeRadius = eyeRadius();
        //breaking up, the clouds lose the storm's colours as it fades
        float stormColour = dissipating ? smoothstep(0.0F, 0.6F, formed) : 1.0F;
        boolean lit = isLit(state, flash);

        //every face's colour at the edge, inside the storm and at the eye
        int sky = skyCloudColour(partialTick);
        int[] vanilla = new int[4];
        int[] storm = new int[4];
        int[] eye = new int[4];
        for (int face = 0; face < 4; face++) {
            vanilla[face] = ARGB.scaleRGB(sky, VANILLA_SHADE[face]);
            storm[face] = ARGB.multiply(ramp(settings.cloudColors(), STORM_LEVEL[face]), tint);
            eye[face] = ramp(settings.eyeColors(), STORM_LEVEL[face]);
        }
        int glowColour = settings.eyeColors().highlight();
        int[] colours = new int[4];

        float cameraX = (float) camera.x;
        float cameraY = (float) camera.y;
        float cameraZ = (float) camera.z;
        VertexConsumer consumer = buffers.getBuffer(ModRenderTypes.tribulationMinecraftClouds());
        for (Cloud cloud : field.clouds) {
            float rn = Mth.lerp(partialTick, cloud.lastRn, cloud.rn);
            float theta = cloudAngle(cloud, rn, state);
            float centreX = Mth.cos(theta) * rn * radius;
            float centreZ = Mth.sin(theta) * rn * radius;
            //past the far end of the view, nothing of it would be drawn anyway
            float dx = centreX - cameraX;
            float dz = centreZ - cameraZ;
            float limit = viewDistance + 12.0F * CELL;
            if (dx * dx + dz * dz > limit * limit) continue;

            float y0 = cloud.height;
            float y1 = y0 + CELL_HEIGHT;
            MinecraftCloudShapes.Shape shape = cloud.shape;
            for (int cell = 0; cell < shape.size(); cell++) {
                float x = centreX + shape.offsetX()[cell] * CELL;
                float z = centreZ + shape.offsetZ()[cell] * CELL;
                float distance = Mth.sqrt(x * x + z * z);
                //swallowed by the eye
                if (distance < eyeRadius) continue;
                //breaking up, each cell goes at its own point as the storm fades
                if (dissipating && formed < cellThreshold(cloud.seed, cell)) continue;

                float cellRn = distance / radius;
                float stormBlend = (1.0F - smoothstep(COLOUR_FULL, COLOUR_START, cellRn)) * stormColour;
                float eyeBlend = (1.0F - smoothstep(eyeRadius / radius, eyeRadius * 3.0F / radius, cellRn)) * eyeOn;
                float glow = lit ? glow(state, cellRn, x, y0, z, flash) : 0.0F;
                for (int face = 0; face < 4; face++) {
                    int colour = ARGB.srgbLerp(stormBlend, vanilla[face], storm[face]);
                    colour = ARGB.srgbLerp(eyeBlend, colour, eye[face]);
                    if (glow > 0.0F) colour = ARGB.srgbLerp(glow * 0.6F, colour, glowColour);
                    colours[face] = ARGB.opaque(colour);
                }
                drawCell(consumer, pose, x - CELL * 0.5F, y0, z - CELL * 0.5F, x + CELL * 0.5F, y1, z + CELL * 0.5F,
                        shape.open()[cell], cameraX, cameraY, cameraZ, colours);
            }
        }
        buffers.endLastBatch();
    }

    /**
     * one flat quad across the eye, the tribulation_rift shader tears it open as the eye lights.
     * it turns with the storm, and each tribulation's tear lies at its own angle
     */
    private void drawRift(MultiBufferSource.BufferSource buffers, PoseStack.Pose pose, CloudState state, float time, float flash) {
        VertexConsumer consumer = buffers.getBuffer(ModRenderTypes.tribulationRift());
        float half = eyeRadius() * RIFT_SIZE;
        float angle = state.angle + state.spiralOffset * Mth.TWO_PI;
        float alongX = Mth.cos(angle) * half;
        float alongZ = Mth.sin(angle) * half;
        //the flash goes in the alpha, it flares and widens the tear
        int colour = ARGB.color(Math.round(flash * 255.0F), 0xFFFFFF);
        riftVertex(consumer, pose, -1.0F, -1.0F, alongX, alongZ, colour);
        riftVertex(consumer, pose, 1.0F, -1.0F, alongX, alongZ, colour);
        riftVertex(consumer, pose, 1.0F, 1.0F, alongX, alongZ, colour);
        riftVertex(consumer, pose, -1.0F, 1.0F, alongX, alongZ, colour);
        //read through the texture matrix when the batch is drawn, the shader's clock for the flickering edges and drifting stars
        ModRenderTypes.tribulationCloudFlow = time * RIFT_TIME;
        buffers.endLastBatch();
    }

    //u runs along the tear and v across it, both -1 to 1
    private static void riftVertex(VertexConsumer consumer, PoseStack.Pose pose, float u, float v, float alongX, float alongZ, int colour) {
        consumer.addVertex(pose, u * alongX - v * alongZ, RIFT_HEIGHT, u * alongZ + v * alongX)
                .setUv(u, v)
                .setColor(colour);
    }

    //radius of the clear eye in blocks
    private float eyeRadius() {
        return Math.max(EYE * settings.radius(), MIN_EYE_BLOCKS);
    }

    //how far round the eye a cloud is: where it started, turned with the storm and wound further in the closer it has been pulled
    private static float cloudAngle(Cloud cloud, float rn, CloudState state) {
        return cloud.angle + state.angle + SWIRL * (float) Math.log(cloud.spawnRn / Math.max(rn, 0.02F));
    }

    /**
     * lightning only crawls under clouds that are already part of the storm, never through empty sky.
     * it jumps between two cells of one coloured cloud, just under its base
     */
    @Override
    protected @Nullable FlickerBolt createBolt(CloudState state, RandomSource random) {
        CloudField field = fields.get(state);
        if (field == null || field.clouds.isEmpty()) return null;
        float radius = settings.radius();
        float innerLimit = eyeRadius() + CELL;
        for (int attempt = 0; attempt < BOLT_ATTEMPTS; attempt++) {
            Cloud cloud = field.clouds.get(random.nextInt(field.clouds.size()));
            float distance = cloud.rn * radius;
            MinecraftCloudShapes.Shape shape = cloud.shape;
            if (distance < innerLimit || cloud.rn > COLOUR_FULL || shape.size() < 2) continue;

            float theta = cloudAngle(cloud, cloud.rn, state);
            float centreX = Mth.cos(theta) * distance;
            float centreZ = Mth.sin(theta) * distance;
            int from = random.nextInt(shape.size());
            int to = random.nextInt(shape.size() - 1);
            if (to >= from) to++;
            float y = cloud.height - 1.0F;
            Vec3 start = new Vec3(centreX + shape.offsetX()[from] * CELL, y, centreZ + shape.offsetZ()[from] * CELL);
            Vec3 end = new Vec3(centreX + shape.offsetX()[to] * CELL, y, centreZ + shape.offsetZ()[to] * CELL);

            int pieces = 2 + random.nextInt(2);
            List<Vec3> points = new ArrayList<>(pieces + 1);
            for (int i = 0; i <= pieces; i++) {
                Vec3 point = start.lerp(end, i / (float) pieces);
                if (i > 0 && i < pieces) {
                    point = point.add((random.nextFloat() - 0.5F) * BOLT_JITTER, (random.nextFloat() - 0.5F) * 0.5F * BOLT_JITTER,
                            (random.nextFloat() - 0.5F) * BOLT_JITTER);
                }
                points.add(point);
            }
            return new FlickerBolt(points, 3 + random.nextInt(4));
        }
        return null;
    }

    //only the faces that can be seen: the top from above, the bottom from below, and open sides facing the camera, like vanilla
    private static void drawCell(VertexConsumer consumer, PoseStack.Pose pose, float x0, float y0, float z0, float x1, float y1, float z1,
                                 int open, float cameraX, float cameraY, float cameraZ, int[] colours) {
        if (cameraY > y1) {
            quad(consumer, pose, colours[TOP], x0, y1, z0, x0, y1, z1, x1, y1, z1, x1, y1, z0);
        }
        if (cameraY < y0) {
            quad(consumer, pose, colours[BOTTOM], x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1);
        }
        if ((open & MinecraftCloudShapes.OPEN_NORTH) != 0 && cameraZ < z0) {
            quad(consumer, pose, colours[NORTH_SOUTH], x0, y0, z0, x0, y1, z0, x1, y1, z0, x1, y0, z0);
        }
        if ((open & MinecraftCloudShapes.OPEN_SOUTH) != 0 && cameraZ > z1) {
            quad(consumer, pose, colours[NORTH_SOUTH], x1, y0, z1, x1, y1, z1, x0, y1, z1, x0, y0, z1);
        }
        if ((open & MinecraftCloudShapes.OPEN_WEST) != 0 && cameraX < x0) {
            quad(consumer, pose, colours[WEST_EAST], x0, y0, z1, x0, y1, z1, x0, y1, z0, x0, y0, z0);
        }
        if ((open & MinecraftCloudShapes.OPEN_EAST) != 0 && cameraX > x1) {
            quad(consumer, pose, colours[WEST_EAST], x1, y0, z0, x1, y1, z0, x1, y1, z1, x1, y0, z1);
        }
    }

    private static void quad(VertexConsumer consumer, PoseStack.Pose pose, int colour,
                             float ax, float ay, float az, float bx, float by, float bz,
                             float cx, float cy, float cz, float dx, float dy, float dz) {
        consumer.addVertex(pose, ax, ay, az).setColor(colour);
        consumer.addVertex(pose, bx, by, bz).setColor(colour);
        consumer.addVertex(pose, cx, cy, cz).setColor(colour);
        consumer.addVertex(pose, dx, dy, dz).setColor(colour);
    }

    //the colour of the clouds in the sky right now, which changes with the time of day and the weather. white where there are none
    private static int skyCloudColour(float partialTick) {
        int colour = Minecraft.getInstance().gameRenderer.getMainCamera().attributeProbe().getValue(EnvironmentAttributes.CLOUD_COLOR, partialTick);
        return ARGB.alpha(colour) == 0 ? 0xFFFFFFFF : ARGB.opaque(colour);
    }

    //the formation below which a cell has broken away, between 0 and 1, fixed for each cell of each cloud
    private static float cellThreshold(int seed, int cell) {
        int hash = seed ^ (cell * 0x9E3779B9);
        hash ^= hash >>> 16;
        hash *= 0x85EBCA6B;
        hash ^= hash >>> 13;
        hash *= 0xC2B2AE35;
        hash ^= hash >>> 16;
        return 0.02F + 0.96F * ((hash & 0xFFFFFF) / (float) 0x1000000);
    }

    private static float smoothstep(float edge0, float edge1, float value) {
        float t = Math.clamp((value - edge0) / (edge1 - edge0), 0.0F, 1.0F);
        return t * t * (3.0F - 2.0F * t);
    }

    //vortex streams belong to the other storms, these clouds bring themselves in
    @Override
    protected void renderStreams(MultiBufferSource.BufferSource buffers, PoseStack.Pose pose, CloudState state, Formation formation,
                                 float time, int tint) {
    }

    private static final class CloudField {
        private final List<Cloud> clouds = new ArrayList<>();
        //fractions of a cloud waiting to be spawned
        private float spawnBudget;
        private boolean warmedUp;
    }

    //one vanilla cloud being pulled into the storm
    private static final class Cloud {
        private final MinecraftCloudShapes.Shape shape;
        //where it started, as a fraction of the radius
        private final float spawnRn;
        //where it started around the storm, before the spin and swirl
        private final float angle;
        //its base, in blocks above or below the eye
        private final float height;
        private final int seed;
        //how far out it is now and was last tick, as fractions of the radius
        private float rn;
        private float lastRn;

        private Cloud(MinecraftCloudShapes.Shape shape, float spawnRn, float angle, float height, int seed) {
            this.shape = shape;
            this.spawnRn = spawnRn;
            this.angle = angle;
            this.height = height;
            this.seed = seed;
            this.rn = spawnRn;
            this.lastRn = spawnRn;
        }
    }
}
