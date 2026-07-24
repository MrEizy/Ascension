package net.zic.ascension.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.zic.ascension.api.core.CoreRegistries;
import net.zic.ascension.api.core.skill.Skill;
import net.zic.ascension.api.core.skill.castable.particle_field.ParticleFieldColour;
import net.zic.ascension.api.core.skill.castable.particle_field.ParticleFieldDefinition;
import net.zic.ascension.api.core.skill.castable.particle_field.ParticleFieldParticleKind;
import net.zic.ascension.api.core.skill.castable.particle_field.ParticleFieldStyle;
import net.zic.ascension.common.data_attachements.AscensionAttachments;
import net.zic.ascension.impl.core.skill.castable.cultivation.SimpleCultivationSkill;
import net.zic.ascension.skill_casting.AscensionSkillListener;
import net.zic.zenithlib.common.ZenithAttachments;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ParticleFieldController {
    private static final double TAU = Math.PI * 2.0D;
    private static final Map<List<ParticleFieldColour>, int[]> PALETTE_CACHE = new HashMap<>();

    private static Identifier activeSkill;
    private static double emissionCarry;
    private static long activeTicks;
    private static int emissionSequence;

    private ParticleFieldController() {
    }

    public static void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        Player player = minecraft.player;
        if (level == null || player == null) {
            reset();
            return;
        }

        if (!player.getData(ZenithAttachments.ACTION_MANAGER).isActive(AscensionSkillListener.skillCast)) {
            reset();
            return;
        }

        var handler = player.getData(AscensionAttachments.ASCENSION_SKILL_CAST_HANDLER);
        Identifier castingSkill = handler.getCastingSkill();
        Identifier skillId = castingSkill != null ? castingSkill : handler.getSkill(handler.getSelectedSlot());
        if (skillId == null) {
            reset();
            return;
        }

        Skill skill = CoreRegistries.safeAccess(CoreRegistries.SKILL_REGISTRY, skillId, player.registryAccess());
        if (!(skill instanceof SimpleCultivationSkill cultivationSkill) || cultivationSkill.particleField().isEmpty()) {
            reset();
            return;
        }

        if (!skillId.equals(activeSkill)) {
            activeSkill = skillId;
            emissionCarry = 0.0D;
            activeTicks = 0L;
            emissionSequence = 0;
        }

        activeTicks++;
        ParticleFieldDefinition definition = cultivationSkill.particleField().get();
        emissionCarry += definition.density() / 20.0D;
        int spawnCount = Math.min(8, (int) emissionCarry);
        emissionCarry -= spawnCount;

        for (int i = 0; i < spawnCount; i++) {
            spawn(level, player, definition);
        }
    }

    private static void spawn(ClientLevel level, Player player, ParticleFieldDefinition definition) {
        RandomSource random = level.getRandom();
        SpawnPoint spawnPoint = spawnPoint(player, definition, random);
        double targetYOffset = player.getBbHeight() * (0.46D + random.nextDouble() * 0.22D);
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
                speed,
                spawnPoint.orbitDirection(),
                random
        );
        ParticleFieldParticleKind kind = definition.randomParticle(random);
        int colour = randomPaletteColour(definition.colours(), random);
        float size = (float) definition.size().random(random);
        int lifetime = definition.lifetime().random(random);

        ParticleFieldParticle particle = ParticleFieldParticle.create(
                kind,
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
        }
    }

    private static SpawnPoint spawnPoint(Player player, ParticleFieldDefinition definition, RandomSource random) {
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
                int stream = emissionSequence % streamCount;
                angle = activeTicks * 0.075D + stream * TAU / streamCount + (random.nextDouble() - 0.5D) * 0.16D;
                radius = Mth.lerp(0.72D + random.nextDouble() * 0.28D, minRadius, maxRadius);
                double heightPhase = (activeTicks * 0.038D + stream / (double) streamCount + random.nextDouble() * 0.08D) % 1.0D;
                height = Mth.lerp(Math.min(1.0D, heightPhase * 0.82D), minHeight, maxHeight);
                orbitDirection = (stream & 1) == 0 ? 1.0D : -1.0D;
            }
            case SPIRAL -> {
                int streamCount = 4;
                int stream = emissionSequence % streamCount;
                angle = activeTicks * 0.11D + stream * TAU / streamCount + (random.nextDouble() - 0.5D) * 0.12D;
                radius = Mth.lerp(0.68D + random.nextDouble() * 0.32D, minRadius, maxRadius);
                double heightPhase = (activeTicks * 0.045D + stream / (double) streamCount) % 1.0D;
                height = Mth.lerp(heightPhase, minHeight, maxHeight);
                orbitDirection = 1.0D;
            }
            default -> throw new IllegalStateException("Unexpected particle field style: " + definition.style());
        }

        emissionSequence++;
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

    private static void reset() {
        activeSkill = null;
        emissionCarry = 0.0D;
        activeTicks = 0L;
        emissionSequence = 0;
    }

    private record SpawnPoint(double x, double y, double z, double orbitDirection) {
    }
}
