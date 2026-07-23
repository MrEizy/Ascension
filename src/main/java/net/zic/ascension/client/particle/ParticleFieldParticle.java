package net.zic.ascension.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;
import net.zic.ascension.api.core.skill.castable.particle_field.ParticleFieldParticleKind;
import net.zic.ascension.api.core.skill.castable.particle_field.ParticleFieldStyle;
import org.jspecify.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

public class ParticleFieldParticle extends SingleQuadParticle {
    private final Layer layer;
    private final ParticleFieldStyle style;
    private final double centerX;
    private final double centerY;
    private final double centerZ;
    private final double steeringStrength;
    private final boolean fullBright;
    private final float baseAlpha;

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
            double centerX,
            double centerY,
            double centerZ,
            double steeringStrength,
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
        this.centerX = centerX;
        this.centerY = centerY;
        this.centerZ = centerZ;
        this.steeringStrength = steeringStrength;
        this.fullBright = fullBright;
        this.baseAlpha = 0.85F + random.nextFloat() * 0.15F;
        this.quadSize = size;
        this.lifetime = lifetime;
        this.gravity = 0.0F;
        this.friction = 0.96F;
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
            double centerX,
            double centerY,
            double centerZ,
            double steeringStrength,
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
                centerX,
                centerY,
                centerZ,
                steeringStrength,
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

        switch (style) {
            case RISING -> tickRising();
            case INWARD_FLOW -> tickInwardFlow();
            case SPIRAL -> tickSpiral();
        }

        this.oRoll = this.roll;
        this.roll += 0.015F + (float) steeringStrength * 0.8F;
        float fadeIn = Math.min(1.0F, this.age / 4.0F);
        float fadeOut = Math.min(1.0F, (this.lifetime - this.age) / 7.0F);
        this.setAlpha(this.baseAlpha * Math.max(0.0F, Math.min(fadeIn, fadeOut)));
    }

    private void tickRising() {
        this.yd += steeringStrength * 0.12D;
        this.xd += (this.random.nextDouble() - 0.5D) * steeringStrength * 0.05D;
        this.zd += (this.random.nextDouble() - 0.5D) * steeringStrength * 0.05D;
    }

    private void tickInwardFlow() {
        double dx = centerX - this.x;
        double dy = centerY - this.y;
        double dz = centerZ - this.z;
        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (length > 0.001D) {
            double force = steeringStrength / Math.max(0.65D, length);
            this.xd += dx / length * force;
            this.yd += dy / length * force * 0.72D + steeringStrength * 0.08D;
            this.zd += dz / length * force;
        }
    }

    private void tickSpiral() {
        double dx = this.x - centerX;
        double dz = this.z - centerZ;
        double horizontalLength = Math.sqrt(dx * dx + dz * dz);
        if (horizontalLength > 0.001D) {
            double tangentX = -dz / horizontalLength;
            double tangentZ = dx / horizontalLength;
            this.xd += tangentX * steeringStrength * 0.85D - dx / horizontalLength * steeringStrength * 0.24D;
            this.zd += tangentZ * steeringStrength * 0.85D - dz / horizontalLength * steeringStrength * 0.24D;
        }
        this.yd += steeringStrength * 0.16D + (centerY - this.y) * steeringStrength * 0.025D;
    }

    @Override
    protected int getLightCoords(float partialTick) {
        return fullBright ? 0xF000F0 : super.getLightCoords(partialTick);
    }

    @Override
    protected Layer getLayer() {
        return layer;
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
                    x,
                    y + 1.0D,
                    z,
                    0.004D,
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
