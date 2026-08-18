package net.zic.ascension.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.skill.Skill;
import net.zic.ascension.api.ascension.core.skill.castable.ActiveCastVisualState;
import net.zic.ascension.impl.core.skill.castable.ActiveSkill;
import net.zic.ascension.api.ascension.core.skill.particle_field.ParticleFieldColour;
import net.zic.ascension.api.ascension.core.skill.particle_field.ParticleFieldDefinition;
import net.zic.ascension.api.ascension.core.skill.particle_field.ParticleFieldStyle;
import net.zic.ascension.common.data_attachements.AscensionAttachments;
import net.zic.ascension.skill_casting.AscensionSkillListener;
import net.zic.zenithlib.common.ZenithAttachments;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class ParticleFieldController {
    private static final double TAU = Math.PI * 2.0D;
    private static final double FULL_DENSITY_DISTANCE_SQR = 16.0D * 16.0D;
    private static final double MEDIUM_DENSITY_DISTANCE_SQR = 32.0D * 32.0D;
    private static final double MAX_RENDER_DISTANCE_SQR = 48.0D * 48.0D;
    private static final long REMOTE_TIMEOUT_TICKS = 60L;
    private static final Map<List<ParticleFieldColour>, int[]> PALETTE_CACHE = new HashMap<>();
    private static final Map<UUID, RemoteFieldState> REMOTE_FIELDS = new HashMap<>();
    private static final EmitterState LOCAL_EMITTER = new EmitterState();

    private static ClientLevel activeLevel;
    private static long clientTicks;

    private ParticleFieldController() {
    }

    public static void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        Player localPlayer = minecraft.player;
        if (level == null || localPlayer == null) {
            clear();
            activeLevel = null;
            return;
        }

        if (level != activeLevel) {
            clear();
            activeLevel = level;
        }

        clientTicks++;
        tickLocal(level, localPlayer);
        tickRemote(level, localPlayer);
    }

    public static void updateRemoteCast(UUID playerId, Identifier skillId, int stage, double progress) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level != null && level != activeLevel) {
            clear();
            activeLevel = level;
        }
        if (minecraft.player != null && minecraft.player.getUUID().equals(playerId)) {
            return;
        }
        if (skillId == null) {
            REMOTE_FIELDS.remove(playerId);
            return;
        }

        RemoteFieldState state = REMOTE_FIELDS.computeIfAbsent(playerId, ignored -> new RemoteFieldState());
        state.setSkill(skillId, stage, progress);
        state.lastSyncTick = clientTicks;
    }

    private static void tickLocal(ClientLevel level, Player player) {
        if (!player.getData(ZenithAttachments.ACTION_MANAGER).isActive(AscensionSkillListener.skillCast)) {
            LOCAL_EMITTER.reset();
            return;
        }

        var handler = player.getData(AscensionAttachments.ASCENSION_SKILL_CAST_HANDLER);
        ActiveCastVisualState state = handler.getActiveCastVisualState();
        if (state == null || !tickEmitter(
                level,
                player,
                state.skill(),
                state.stage(),
                state.progress(),
                1.0D,
                LOCAL_EMITTER
        )) {
            LOCAL_EMITTER.reset();
        }
    }

    private static void tickRemote(ClientLevel level, Player localPlayer) {
        Iterator<Map.Entry<UUID, RemoteFieldState>> iterator = REMOTE_FIELDS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, RemoteFieldState> entry = iterator.next();
            RemoteFieldState state = entry.getValue();
            if (clientTicks - state.lastSyncTick > REMOTE_TIMEOUT_TICKS) {
                iterator.remove();
                continue;
            }

            Player remotePlayer = level.getPlayerByUUID(entry.getKey());
            if (remotePlayer == null || remotePlayer == localPlayer || remotePlayer.isRemoved()) {
                continue;
            }

            double densityMultiplier = densityMultiplier(localPlayer.distanceToSqr(remotePlayer));
            if (densityMultiplier <= 0.0D) {
                continue;
            }

            if (!tickEmitter(
                    level,
                    remotePlayer,
                    state.skillId,
                    state.stage,
                    state.progress,
                    densityMultiplier,
                    state.emitter
            )) {
                iterator.remove();
            }
        }
    }

    private static boolean tickEmitter(
            ClientLevel level,
            Player player,
            Identifier skillId,
            int stage,
            double charge,
            double densityMultiplier,
            EmitterState state
    ) {
        Skill skill = CoreRegistries.safeAccess(CoreRegistries.SKILL_REGISTRY, skillId, player.registryAccess());
        if (!(skill instanceof ActiveSkill activeSkill) || activeSkill.particleField(stage).isEmpty()) {
            return false;
        }
        ParticleFieldDefinition definition = activeSkill.particleField(stage).get();

        state.activate(skillId, stage);
        state.activeTicks++;
        state.emissionCarry += definition.density() * densityMultiplier / 20.0D;
        int spawnCount = Math.min(8, (int) state.emissionCarry);
        state.emissionCarry -= spawnCount;

        for (int index = 0; index < spawnCount; index++) {
            spawn(level, player, definition, state);
        }
        return true;
    }

    private static double densityMultiplier(double distanceSqr) {
        if (distanceSqr <= FULL_DENSITY_DISTANCE_SQR) {
            return 1.0D;
        }
        if (distanceSqr <= MEDIUM_DENSITY_DISTANCE_SQR) {
            return 0.6D;
        }
        if (distanceSqr <= MAX_RENDER_DISTANCE_SQR) {
            return 0.3D;
        }
        return 0.0D;
    }

    private static void spawn(
            ClientLevel level,
            Player player,
            ParticleFieldDefinition definition,
            EmitterState state
    ) {
        RandomSource random = level.getRandom();
        SpawnPoint spawnPoint = spawnPoint(player, definition, random, state);
        double targetYOffset = targetYOffset(player, definition.style(), random);
        double targetX = player.getX();
        double targetY = player.getY() + targetYOffset;
        double targetZ = player.getZ();
        double speed = definition.speed().random(random);
        double[] velocity = initialVelocity(
                definition.style(),
                spawnPoint.x(),
                spawnPoint.y(),
                spawnPoint.z(),
                targetX,
                targetY,
                targetZ,
                targetYOffset,
                speed,
                spawnPoint.orbitDirection(),
                random
        );
        Identifier particleId = definition.randomParticle(random);
        int colour = randomPaletteColour(definition.colours(), random);
        float size = (float) definition.size().random(random);
        int lifetime = definition.lifetime().random(random);

        ParticleFieldParticle particle = ParticleFieldParticle.create(
                particleId,
                level,
                spawnPoint.x(),
                spawnPoint.y(),
                spawnPoint.z(),
                velocity[0],
                velocity[1],
                velocity[2],
                random,
                definition.style(),
                player.getId(),
                targetX,
                targetY,
                targetZ,
                targetYOffset,
                speed,
                spawnPoint.orbitDirection(),
                size,
                lifetime,
                colour,
                definition.fullBright()
        );
        if (particle != null) {
            Minecraft.getInstance().particleEngine.add(particle);
            return;
        }

        ParticleType<?> type = BuiltInRegistries.PARTICLE_TYPE.getValue(particleId);
        if (type instanceof SimpleParticleType simple) {
            level.addParticle(
                    simple,
                    spawnPoint.x(),
                    spawnPoint.y(),
                    spawnPoint.z(),
                    velocity[0],
                    velocity[1],
                    velocity[2]
            );
        }
    }

    private static double targetYOffset(Player player, ParticleFieldStyle style, RandomSource random) {
        double height = player.getBbHeight();
        return switch (style) {
            case RISING, INWARD_FLOW, SPIRAL -> height * (0.46D + random.nextDouble() * 0.22D);
            case MERIDIAN_FLOW -> height * (0.28D + random.nextDouble() * 0.48D);
            case GATHERING_RING -> height * (0.38D + random.nextDouble() * 0.18D);
            case BREATH_FLOW -> player.getEyeHeight() - 0.12D + (random.nextDouble() - 0.5D) * 0.08D;
        };
    }

    private static SpawnPoint spawnPoint(
            Player player,
            ParticleFieldDefinition definition,
            RandomSource random,
            EmitterState state
    ) {
        double minRadius = definition.radius().min();
        double maxRadius = definition.radius().max();
        double minHeight = definition.height().min();
        double maxHeight = definition.height().max();
        double radius;
        double height;
        double angle;
        double orbitDirection;

        switch (definition.style()) {
            case RISING -> {
                angle = random.nextDouble() * TAU;
                radius = Mth.lerp(Math.pow(random.nextDouble(), 1.7D), minRadius, maxRadius);
                height = Mth.lerp(Math.pow(random.nextDouble(), 2.2D), minHeight, maxHeight);
                orbitDirection = random.nextBoolean() ? 1.0D : -1.0D;
            }
            case INWARD_FLOW -> {
                int streamCount = 5;
                int stream = state.emissionSequence % streamCount;
                angle = state.activeTicks * 0.075D
                        + stream * TAU / streamCount
                        + (random.nextDouble() - 0.5D) * 0.16D;
                radius = Mth.lerp(0.72D + random.nextDouble() * 0.28D, minRadius, maxRadius);
                double heightPhase = (state.activeTicks * 0.038D
                        + stream / (double) streamCount
                        + random.nextDouble() * 0.08D) % 1.0D;
                height = Mth.lerp(Math.min(1.0D, heightPhase * 0.82D), minHeight, maxHeight);
                orbitDirection = (stream & 1) == 0 ? 1.0D : -1.0D;
            }
            case SPIRAL -> {
                int streamCount = 4;
                int stream = state.emissionSequence % streamCount;
                angle = state.activeTicks * 0.11D
                        + stream * TAU / streamCount
                        + (random.nextDouble() - 0.5D) * 0.12D;
                radius = Mth.lerp(0.68D + random.nextDouble() * 0.32D, minRadius, maxRadius);
                double heightPhase = (state.activeTicks * 0.045D + stream / (double) streamCount) % 1.0D;
                height = Mth.lerp(heightPhase, minHeight, maxHeight);
                orbitDirection = 1.0D;
            }
            case MERIDIAN_FLOW -> {
                int streamCount = 6;
                int stream = state.emissionSequence % streamCount;
                angle = stream * TAU / streamCount
                        + state.activeTicks * 0.032D
                        + (random.nextDouble() - 0.5D) * 0.1D;
                radius = Mth.lerp(
                        0.36D + random.nextDouble() * 0.28D,
                        minRadius,
                        Math.max(minRadius, minRadius + (maxRadius - minRadius) * 0.45D)
                );
                double[] lanes = {0.16D, 0.28D, 0.44D, 0.58D, 0.72D, 0.84D};
                double lane = lanes[stream % lanes.length] + (random.nextDouble() - 0.5D) * 0.06D;
                height = Mth.lerp(Mth.clamp(lane, 0.0D, 1.0D), minHeight, maxHeight);
                orbitDirection = (stream & 1) == 0 ? 1.0D : -1.0D;
            }
            case GATHERING_RING -> {
                int streamCount = 7;
                int stream = state.emissionSequence % streamCount;
                angle = state.activeTicks * 0.085D
                        + stream * TAU / streamCount
                        + (random.nextDouble() - 0.5D) * 0.1D;
                radius = Mth.lerp(0.8D + random.nextDouble() * 0.2D, minRadius, maxRadius);
                double lowBand = 0.08D + random.nextDouble() * 0.18D;
                height = Mth.lerp(lowBand, minHeight, maxHeight);
                orbitDirection = 1.0D;
            }
            case BREATH_FLOW -> {
                double facingAngle = Math.toRadians(player.getYRot() + 90.0F);
                int streamCount = 5;
                int stream = state.emissionSequence % streamCount;
                double spread = (stream - (streamCount - 1) * 0.5D) * 0.075D + (random.nextDouble() - 0.5D) * 0.06D;
                angle = facingAngle + spread;
                radius = Mth.lerp(0.7D + random.nextDouble() * 0.3D, minRadius, maxRadius);
                double heightPhase = 0.5D + (stream - 2) * 0.08D + (random.nextDouble() - 0.5D) * 0.08D;
                height = player.getEyeHeight() + Mth.lerp(heightPhase, minHeight, maxHeight);
                orbitDirection = (stream & 1) == 0 ? 1.0D : -1.0D;
            }
            default -> throw new IllegalStateException("Unexpected particle field style: " + definition.style());
        }

        state.emissionSequence++;
        return new SpawnPoint(
                player.getX() + Math.cos(angle) * radius,
                player.getY() + height,
                player.getZ() + Math.sin(angle) * radius,
                orbitDirection
        );
    }

    private static double[] initialVelocity(
            ParticleFieldStyle style,
            double x,
            double y,
            double z,
            double centerX,
            double centerY,
            double centerZ,
            double targetYOffset,
            double speed,
            double orbitDirection,
            RandomSource random
    ) {
        double dx = centerX - x;
        double dy = centerY - y;
        double dz = centerZ - z;
        double horizontalLength = Math.max(0.001D, Math.sqrt(dx * dx + dz * dz));
        double fullLength = Math.max(0.001D, Math.sqrt(dx * dx + dy * dy + dz * dz));
        double tangentX = -dz / horizontalLength * orbitDirection;
        double tangentZ = dx / horizontalLength * orbitDirection;

        return switch (style) {
            case RISING -> new double[]{
                    dx / horizontalLength * speed * 0.16D + tangentX * speed * 0.08D,
                    speed * (0.72D + random.nextDouble() * 0.22D),
                    dz / horizontalLength * speed * 0.16D + tangentZ * speed * 0.08D
            };
            case INWARD_FLOW -> new double[]{
                    dx / fullLength * speed * 1.08D + tangentX * speed * 0.24D,
                    dy / fullLength * speed * 0.52D + speed * 0.24D,
                    dz / fullLength * speed * 1.08D + tangentZ * speed * 0.24D
            };
            case SPIRAL -> new double[]{
                    tangentX * speed * 1.05D + dx / horizontalLength * speed * 0.14D,
                    speed * (0.32D + random.nextDouble() * 0.14D),
                    tangentZ * speed * 1.05D + dz / horizontalLength * speed * 0.14D
            };
            case MERIDIAN_FLOW -> new double[]{
                    dx / fullLength * speed * 0.82D + tangentX * speed * 0.18D,
                    dy / fullLength * speed * 0.42D + speed * 0.08D,
                    dz / fullLength * speed * 0.82D + tangentZ * speed * 0.18D
            };
            case GATHERING_RING -> new double[]{
                    tangentX * speed * 0.92D + dx / horizontalLength * speed * 0.12D,
                    Math.max(0.0D, dy / Math.max(0.1D, targetYOffset)) * speed * 0.2D + speed * 0.12D,
                    tangentZ * speed * 0.92D + dz / horizontalLength * speed * 0.12D
            };
            case BREATH_FLOW -> new double[]{
                    dx / fullLength * speed * 1.18D + tangentX * speed * 0.08D,
                    dy / fullLength * speed * 1.04D,
                    dz / fullLength * speed * 1.18D + tangentZ * speed * 0.08D
            };
        };
    }

    private static int randomPaletteColour(List<ParticleFieldColour> colours, RandomSource random) {
        int[] palette = PALETTE_CACHE.computeIfAbsent(colours, ParticleFieldController::buildPalette);
        return palette[random.nextInt(palette.length)];
    }

    private static int[] buildPalette(List<ParticleFieldColour> colours) {
        int[] palette = new int[colours.size() * 7];
        int index = 0;
        for (ParticleFieldColour colour : colours) {
            palette[index++] = adjustLightness(colour.rgb(), -0.12F);
            palette[index++] = adjustLightness(colour.rgb(), -0.06F);
            palette[index++] = colour.rgb();
            palette[index++] = colour.rgb();
            palette[index++] = colour.rgb();
            palette[index++] = adjustLightness(colour.rgb(), 0.06F);
            palette[index++] = adjustLightness(colour.rgb(), 0.12F);
        }
        return palette;
    }

    private static int adjustLightness(int rgb, float adjustment) {
        float red = (rgb >> 16 & 0xFF) / 255.0F;
        float green = (rgb >> 8 & 0xFF) / 255.0F;
        float blue = (rgb & 0xFF) / 255.0F;
        float max = Math.max(red, Math.max(green, blue));
        float min = Math.min(red, Math.min(green, blue));
        float lightness = (max + min) * 0.5F;
        float saturation;
        float hue;

        if (max == min) {
            hue = 0.0F;
            saturation = 0.0F;
        } else {
            float delta = max - min;
            saturation = lightness > 0.5F ? delta / (2.0F - max - min) : delta / (max + min);
            if (max == red) {
                hue = (green - blue) / delta + (green < blue ? 6.0F : 0.0F);
            } else if (max == green) {
                hue = (blue - red) / delta + 2.0F;
            } else {
                hue = (red - green) / delta + 4.0F;
            }
            hue /= 6.0F;
        }

        lightness = Mth.clamp(lightness + adjustment, 0.0F, 1.0F);
        if (saturation == 0.0F) {
            int channel = Math.round(lightness * 255.0F);
            return channel << 16 | channel << 8 | channel;
        }

        float q = lightness < 0.5F
                ? lightness * (1.0F + saturation)
                : lightness + saturation - lightness * saturation;
        float p = 2.0F * lightness - q;
        int adjustedRed = Math.round(hueToRgb(p, q, hue + 1.0F / 3.0F) * 255.0F);
        int adjustedGreen = Math.round(hueToRgb(p, q, hue) * 255.0F);
        int adjustedBlue = Math.round(hueToRgb(p, q, hue - 1.0F / 3.0F) * 255.0F);
        return adjustedRed << 16 | adjustedGreen << 8 | adjustedBlue;
    }

    private static float hueToRgb(float p, float q, float hue) {
        if (hue < 0.0F) {
            hue += 1.0F;
        }
        if (hue > 1.0F) {
            hue -= 1.0F;
        }
        if (hue < 1.0F / 6.0F) {
            return p + (q - p) * 6.0F * hue;
        }
        if (hue < 0.5F) {
            return q;
        }
        if (hue < 2.0F / 3.0F) {
            return p + (q - p) * (2.0F / 3.0F - hue) * 6.0F;
        }
        return p;
    }

    private static void clear() {
        LOCAL_EMITTER.reset();
        REMOTE_FIELDS.clear();
        clientTicks = 0L;
    }

    private record SpawnPoint(double x, double y, double z, double orbitDirection) {
    }

    private static final class EmitterState {
        private Identifier activeSkill;
        private int activeStage = -1;
        private double emissionCarry;
        private long activeTicks;
        private int emissionSequence;

        private void activate(Identifier skillId, int stage) {
            if (!skillId.equals(activeSkill) || activeStage != stage) {
                reset();
                activeSkill = skillId;
                activeStage = stage;
            }
        }

        private void reset() {
            activeSkill = null;
            activeStage = -1;
            emissionCarry = 0.0D;
            activeTicks = 0L;
            emissionSequence = 0;
        }
    }

    private static final class RemoteFieldState {
        private Identifier skillId;
        private int stage;
        private double progress;
        private long lastSyncTick;
        private final EmitterState emitter = new EmitterState();

        private void setSkill(Identifier newSkillId, int newStage, double newProgress) {
            if (!newSkillId.equals(skillId) || stage != newStage) {
                emitter.reset();
            }
            skillId = newSkillId;
            stage = Math.max(0, newStage);
            progress = Double.isFinite(newProgress) ? Math.clamp(newProgress, 0.0D, 1.0D) : 0.0D;
        }
    }
}
