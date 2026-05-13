package net.thejadeproject.ascension.particle.aura;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import org.jetbrains.annotations.Nullable;

public class AuraFlameParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    protected AuraFlameParticle(ClientLevel level, double x, double y, double z,
                                double xd, double yd, double zd,
                                AuraParticleOptions options, SpriteSet sprites) {
        super(level, x, y, z, xd, yd, zd);
        this.sprites = sprites;

        this.xd = xd;
        this.yd = yd;
        this.zd = zd;

        this.setColor(options.red(), options.green(), options.blue());
        this.quadSize = 0.18f * options.scale();
        this.lifetime = options.lifetime();
        this.gravity = -0.012f;
        this.friction = 0.94f;
        this.alpha = 0.85f;

        this.setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprites);

        float progress = (float) this.age / (float) this.lifetime;
        this.alpha = 0.85f * (1.0f - progress);
        this.quadSize *= 0.985f;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<AuraParticleOptions> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public @Nullable Particle createParticle(AuraParticleOptions options, ClientLevel level,
                                                 double x, double y, double z,
                                                 double xd, double yd, double zd) {
            return new AuraFlameParticle(level, x, y, z, xd, yd, zd, options, sprites);
        }
    }
}