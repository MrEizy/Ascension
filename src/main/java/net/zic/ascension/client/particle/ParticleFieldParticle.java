package net.zic.ascension.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.zic.ascension.api.core.skill.castable.particle_field.ParticleFieldParticleKind;
import net.zic.ascension.api.core.skill.castable.particle_field.ParticleFieldStyle;
import org.jspecify.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

public class ParticleFieldParticle extends SingleQuadParticle {
    private final Layer layer;
    private final ParticleFieldStyle style;
    private final int targetEntityId;
    private final double fallbackCenterX;
    private final double fallbackCenterY;
    private final double fallbackCenterZ;
    private final double targetYOffset;
    private final double flowSpeed;
    private final double orbitDirection;
    private final double initialRadius;
    private final double initialAngle;
    private final boolean fullBright;
    private final float baseAlpha;
    private final float baseSize;

    private ParticleFieldParticle(
            ClientLevel level,
            double x,
            double y,
            double z,
            double xSpeed,
            double ySpeed,
            double zSpeed,
            SpriteSet sprites,
            RandomSource random,
            ParticleFieldStyle style,
            int targetEntityId,
            double centerX,
            double centerY,
            double centerZ,
            double targetYOffset,
            double flowSpeed,
            double orbitDirection,
            float size,
            int lifetime,
            float red,
            float green,
            float blue,
            boolean fullBright
    ) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites.get(random));
        this.layer = Layer.bySprite(this.sprite);
        this.style = style;
        this.targetEntityId = targetEntityId;
        this.fallbackCenterX = centerX;
        this.fallbackCenterY = centerY;
        this.fallbackCenterZ = centerZ;
        this.targetYOffset = targetYOffset;
        this.flowSpeed = flowSpeed;
        this.orbitDirection = orbitDirection;
        this.initialRadius = Math.max(0.1D, Math.sqrt((x - centerX) * (x - centerX) + (z - centerZ) * (z - centerZ)));
        this.initialAngle = Math.atan2(z - centerZ, x - centerX);
        this.fullBright = fullBright;
        this.baseAlpha = 0.58F + random.nextFloat() * 0.24F;
        this.baseSize = size;
        this.quadSize = size;
        this.lifetime = lifetime;
        this.gravity = 0.0F;
        this.friction = 0.88F;
        this.hasPhysics = false;
        this.roll = random.nextFloat() * (float) (Math.PI * 2.0D);
        this.oRoll = this.roll;
        this.setColor(red, green, blue);
        this.setAlpha(0.0F);
    }

    public static @Nullable ParticleFieldParticle create(
            ParticleFieldParticleKind kind,
            ClientLevel level,
            double x,
            double y,
            double z,
            double xSpeed,
            double ySpeed,
            double zSpeed,
            RandomSource random,
            ParticleFieldStyle style,
            int targetEntityId,
            double centerX,
            double centerY,
            double centerZ,
            double targetYOffset,
            double flowSpeed,
            double orbitDirection,
            float size,
            int lifetime,
            int colour,
            boolean fullBright
    ) {
        SpriteSet sprites = Provider.spriteSet(kind);
        if (sprites == null) {
            return null;
        }
        float red = (colour >> 16 & 0xFF) / 255.0F;
        float green = (colour >> 8 & 0xFF) / 255.0F;
        float blue = (colour & 0xFF) / 255.0F;
        return new ParticleFieldParticle(
                level,
                x,
                y,
                z,
                xSpeed,
                ySpeed,
                zSpeed,
                sprites,
                random,
                style,
                targetEntityId,
                centerX,
                centerY,
                centerZ,
                targetYOffset,
                flowSpeed,
                orbitDirection,
                size,
                lifetime,
                red,
                green,
                blue,
                fullBright
        );
    }

    @Override
    public void tick() {
        super.tick();
        if (this.removed) {
            return;
        }

        Center center = currentCenter();
        float progress = Mth.clamp(this.age / (float) this.lifetime, 0.0F, 1.0F);
        switch (style) {
            case RISING -> tickRising(center, progress);
            case INWARD_FLOW -> tickInwardFlow(center, progress);
            case SPIRAL -> tickSpiral(center, progress);
            case MERIDIAN_FLOW -> tickMeridianFlow(center, progress);
            case GATHERING_RING -> tickGatheringRing(center, progress);
            case BREATH_FLOW -> tickBreathFlow(center, progress);
        }

        this.oRoll = this.roll;
        this.roll += 0.008F + (float) flowSpeed * 0.18F;
        float fadeIn = Math.min(1.0F, this.age / 5.0F);
        float fadeOut = Math.min(1.0F, (this.lifetime - this.age) / 8.0F);
        this.setAlpha(this.baseAlpha * Math.max(0.0F, Math.min(fadeIn, fadeOut)));
    }

    private void tickRising(Center center, float progress) {
        double dx = center.x() - this.x;
        double dz = center.z() - this.z;
        double horizontalLength = Math.max(0.001D, Math.sqrt(dx * dx + dz * dz));
        double tangentX = -dz / horizontalLength * orbitDirection;
        double tangentZ = dx / horizontalLength * orbitDirection;
        double desiredX = dx / horizontalLength * flowSpeed * 0.12D + tangentX * flowSpeed * 0.06D;
        double desiredZ = dz / horizontalLength * flowSpeed * 0.12D + tangentZ * flowSpeed * 0.06D;
        double desiredY = flowSpeed * (0.72D + progress * 0.18D);
        steer(desiredX, desiredY, desiredZ, 0.18D);
        this.quadSize = this.baseSize * (0.88F + progress * 0.12F);
    }

    private void tickInwardFlow(Center center, float progress) {
        double dx = center.x() - this.x;
        double dz = center.z() - this.z;
        double horizontalLength = Math.max(0.001D, Math.sqrt(dx * dx + dz * dz));
        double tangentX = -dz / horizontalLength * orbitDirection;
        double tangentZ = dx / horizontalLength * orbitDirection;
        double inwardSpeed = flowSpeed * (1.08D + progress * 0.48D);
        double curveStrength = flowSpeed * 0.24D * (1.0D - progress);
        double verticalCorrection = Mth.clamp(
                (center.y() - this.y) * 0.075D,
                -flowSpeed * 0.42D,
                flowSpeed * 0.82D
        );
        double desiredX = dx / horizontalLength * inwardSpeed + tangentX * curveStrength;
        double desiredY = flowSpeed * 0.24D + verticalCorrection;
        double desiredZ = dz / horizontalLength * inwardSpeed + tangentZ * curveStrength;
        steer(desiredX, desiredY, desiredZ, 0.31D);
        this.quadSize = this.baseSize * (1.0F - progress * 0.38F);

        if (horizontalLength < 0.14D && Math.abs(center.y() - this.y) < 0.28D && this.age < this.lifetime - 6) {
            this.age = this.lifetime - 6;
        }
    }

    private void tickSpiral(Center center, float progress) {
        double dx = this.x - center.x();
        double dz = this.z - center.z();
        double horizontalLength = Math.max(0.001D, Math.sqrt(dx * dx + dz * dz));
        double radialX = dx / horizontalLength;
        double radialZ = dz / horizontalLength;
        double tangentX = -radialZ * orbitDirection;
        double tangentZ = radialX * orbitDirection;
        double targetRadius = Math.max(0.34D, initialRadius * (1.0D - progress * 0.58D));
        double radialCorrection = Mth.clamp(
                (horizontalLength - targetRadius) * 0.09D,
                -flowSpeed * 0.55D,
                flowSpeed * 0.85D
        );
        double desiredX = tangentX * flowSpeed - radialX * radialCorrection;
        double desiredZ = tangentZ * flowSpeed - radialZ * radialCorrection;
        double desiredY = flowSpeed * 0.34D + Mth.clamp(
                (center.y() - this.y) * 0.035D,
                -flowSpeed * 0.35D,
                flowSpeed * 0.5D
        );
        steer(desiredX, desiredY, desiredZ, 0.24D);
        this.quadSize = this.baseSize * (0.92F - progress * 0.16F);
    }

    private void tickMeridianFlow(Center center, float progress) {
        double dx = center.x() - this.x;
        double dz = center.z() - this.z;
        double horizontalLength = Math.max(0.001D, Math.sqrt(dx * dx + dz * dz));
        double tangentX = -dz / horizontalLength * orbitDirection;
        double tangentZ = dx / horizontalLength * orbitDirection;
        double laneRadius = 0.12D + 0.08D * (0.5D + 0.5D * Math.sin(initialAngle * 2.0D + progress * Math.PI * 2.0D));
        double radialPull = Mth.clamp(
                (horizontalLength - laneRadius) * 0.18D,
                -flowSpeed * 0.85D,
                flowSpeed * 1.05D
        );
        double laneY = center.y() + Math.sin(initialAngle + progress * Math.PI) * 0.16D;
        double verticalCorrection = Mth.clamp(
                (laneY - this.y) * 0.12D,
                -flowSpeed * 0.55D,
                flowSpeed * 0.75D
        );
        double tangentStrength = flowSpeed * (0.32D - progress * 0.1D);
        double desiredX = dx / horizontalLength * radialPull + tangentX * tangentStrength;
        double desiredY = flowSpeed * 0.12D + verticalCorrection;
        double desiredZ = dz / horizontalLength * radialPull + tangentZ * tangentStrength;
        steer(desiredX, desiredY, desiredZ, 0.28D);
        this.quadSize = this.baseSize * (0.96F - progress * 0.2F);

        if (horizontalLength < 0.18D && Math.abs(laneY - this.y) < 0.2D && this.age < this.lifetime - 7) {
            this.age = this.lifetime - 7;
        }
    }

    private void tickGatheringRing(Center center, float progress) {
        double dx = this.x - center.x();
        double dz = this.z - center.z();
        double horizontalLength = Math.max(0.001D, Math.sqrt(dx * dx + dz * dz));
        double radialX = dx / horizontalLength;
        double radialZ = dz / horizontalLength;
        double tangentX = -radialZ * orbitDirection;
        double tangentZ = radialX * orbitDirection;
        double targetRadius = Math.max(0.26D, initialRadius * (1.0D - progress * 0.52D));
        double inwardBias = progress > 0.58F ? (progress - 0.58F) / 0.42F : 0.0D;
        double radialCorrection = Mth.clamp(
                (horizontalLength - targetRadius) * 0.11D + inwardBias * flowSpeed * 0.95D,
                -flowSpeed * 0.55D,
                flowSpeed * 1.05D
        );
        double ringY = center.y() - targetYOffset * 0.52D + progress * targetYOffset * 0.38D;
        double verticalCorrection = Mth.clamp(
                (ringY - this.y) * 0.08D,
                -flowSpeed * 0.42D,
                flowSpeed * 0.65D
        );
        double desiredX = tangentX * flowSpeed * 0.92D - radialX * radialCorrection;
        double desiredY = flowSpeed * 0.18D + verticalCorrection;
        double desiredZ = tangentZ * flowSpeed * 0.92D - radialZ * radialCorrection;
        steer(desiredX, desiredY, desiredZ, 0.23D);
        this.quadSize = this.baseSize * (0.94F - progress * 0.14F);

        if (horizontalLength < 0.18D && progress > 0.72F && this.age < this.lifetime - 5) {
            this.age = this.lifetime - 5;
        }
    }

    private void tickBreathFlow(Center center, float progress) {
        double dx = center.x() - this.x;
        double dy = center.y() - this.y;
        double dz = center.z() - this.z;
        double horizontalLength = Math.max(0.001D, Math.sqrt(dx * dx + dz * dz));
        double fullLength = Math.max(0.001D, Math.sqrt(dx * dx + dy * dy + dz * dz));
        double tangentX = -dz / horizontalLength * orbitDirection;
        double tangentZ = dx / horizontalLength * orbitDirection;
        double inwardSpeed = flowSpeed * (1.12D + progress * 0.58D);
        double curl = flowSpeed * 0.08D * (1.0D - progress);
        steer(
                dx / fullLength * inwardSpeed + tangentX * curl,
                dy / fullLength * inwardSpeed,
                dz / fullLength * inwardSpeed + tangentZ * curl,
                0.36D
        );
        this.quadSize = this.baseSize * (1.0F - progress * 0.48F);

        if (fullLength < 0.12D && this.age < this.lifetime - 5) {
            this.age = this.lifetime - 5;
        }
    }

    private void steer(double desiredX, double desiredY, double desiredZ, double blend) {
        this.xd += (desiredX - this.xd) * blend;
        this.yd += (desiredY - this.yd) * blend;
        this.zd += (desiredZ - this.zd) * blend;
    }

    private Center currentCenter() {
        Entity target = targetEntityId >= 0 ? this.level.getEntity(targetEntityId) : null;
        if (target == null) {
            return new Center(fallbackCenterX, fallbackCenterY, fallbackCenterZ);
        }
        return new Center(target.getX(), target.getY() + targetYOffset, target.getZ());
    }

    @Override
    protected int getLightCoords(float partialTick) {
        return fullBright ? 0xF000F0 : super.getLightCoords(partialTick);
    }

    @Override
    protected Layer getLayer() {
        return layer;
    }

    private record Center(double x, double y, double z) {
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        private static final Map<ParticleFieldParticleKind, SpriteSet> SPRITES = new EnumMap<>(ParticleFieldParticleKind.class);

        private final SpriteSet sprites;

        public Provider(ParticleFieldParticleKind kind, SpriteSet sprites) {
            this.sprites = sprites;
            SPRITES.put(kind, sprites);
        }

        @Override
        public Particle createParticle(
                SimpleParticleType particleType,
                ClientLevel level,
                double x,
                double y,
                double z,
                double xSpeed,
                double ySpeed,
                double zSpeed,
                RandomSource random
        ) {
            return new ParticleFieldParticle(
                    level,
                    x,
                    y,
                    z,
                    xSpeed,
                    ySpeed,
                    zSpeed,
                    sprites,
                    random,
                    ParticleFieldStyle.RISING,
                    -1,
                    x,
                    y + 1.0D,
                    z,
                    1.0D,
                    0.05D,
                    1.0D,
                    0.1F,
                    30,
                    1.0F,
                    1.0F,
                    1.0F,
                    true
            );
        }

        private static @Nullable SpriteSet spriteSet(ParticleFieldParticleKind kind) {
            return SPRITES.get(kind);
        }
    }
}
