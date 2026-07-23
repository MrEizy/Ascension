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
    private static final Map<List<ParticleFieldColour>, int[]> PALETTE_CACHE = new HashMap<>();

    private static Identifier activeSkill;
    private static double emissionCarry;

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
        }

        ParticleFieldDefinition definition = cultivationSkill.particleField().get();
        emissionCarry += definition.density() / 20.0D;
        int spawnCount = Math.min(24, (int) emissionCarry);
        emissionCarry -= spawnCount;

        for (int i = 0; i < spawnCount; i++) {
            spawn(level, player, definition);
        }
    }

    private static void spawn(ClientLevel level, Player player, ParticleFieldDefinition definition) {
        RandomSource random = level.getRandom();
        double angle = random.nextDouble() * Math.PI * 2.0D;
        double radius = definition.radius().random(random);
        double x = player.getX() + Math.cos(angle) * radius;
        double y = player.getY() + definition.height().random(random);
        double z = player.getZ() + Math.sin(angle) * radius;
        double centerX = player.getX();
        double centerY = player.getY() + player.getBbHeight() * 0.56D;
        double centerZ = player.getZ();
        double speed = definition.speed().random(random);
        double[] velocity = initialVelocity(definition.style(), x, y, z, centerX, centerY, centerZ, speed, random);
        ParticleFieldParticleKind kind = definition.randomParticle(random);
        int colour = randomPaletteColour(definition.colours(), random);
        float size = (float) definition.size().random(random);
        int lifetime = definition.lifetime().random(random);
        double steeringStrength = 0.0015D + speed * 0.055D;

        ParticleFieldParticle particle = ParticleFieldParticle.create(
                kind,
                level,
                x,
                y,
                z,
                velocity[0],
                velocity[1],
                velocity[2],
                random,
                definition.style(),
                centerX,
                centerY,
                centerZ,
                steeringStrength,
                size,
                lifetime,
                colour,
                definition.fullBright()
        );
        if (particle != null) {
            Minecraft.getInstance().particleEngine.add(particle);
        }
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
            RandomSource random
    ) {
        double dx = centerX - x;
        double dy = centerY - y;
        double dz = centerZ - z;
        double horizontalLength = Math.max(0.001D, Math.sqrt(dx * dx + dz * dz));
        double fullLength = Math.max(0.001D, Math.sqrt(dx * dx + dy * dy + dz * dz));

        return switch (style) {
            case RISING -> new double[]{
                    (random.nextDouble() - 0.5D) * speed * 0.5D,
                    speed * (0.75D + random.nextDouble() * 0.5D),
                    (random.nextDouble() - 0.5D) * speed * 0.5D
            };
            case INWARD_FLOW -> new double[]{
                    dx / fullLength * speed,
                    dy / fullLength * speed + speed * 0.24D,
                    dz / fullLength * speed
            };
            case SPIRAL -> new double[]{
                    -dz / horizontalLength * speed + dx / horizontalLength * speed * 0.18D,
                    speed * (0.28D + random.nextDouble() * 0.28D),
                    dx / horizontalLength * speed + dz / horizontalLength * speed * 0.18D
            };
        };
    }

    private static int randomPaletteColour(List<ParticleFieldColour> colours, RandomSource random) {
        int[] palette = PALETTE_CACHE.computeIfAbsent(colours, ParticleFieldController::buildPalette);
        return palette[random.nextInt(palette.length)];
    }

    private static int[] buildPalette(List<ParticleFieldColour> colours) {
        int[] palette = new int[colours.size() * 6];
        int index = 0;
        for (ParticleFieldColour colour : colours) {
            palette[index++] = adjustLightness(colour.rgb(), -0.28F);
            palette[index++] = adjustLightness(colour.rgb(), -0.14F);
            palette[index++] = colour.rgb();
            palette[index++] = colour.rgb();
            palette[index++] = adjustLightness(colour.rgb(), 0.14F);
            palette[index++] = adjustLightness(colour.rgb(), 0.28F);
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
    }
}
