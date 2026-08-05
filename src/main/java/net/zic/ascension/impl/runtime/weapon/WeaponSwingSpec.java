package net.zic.ascension.impl.runtime.weapon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public record WeaponSwingSpec(
        Identifier skillId,
        Optional<Identifier> path,
        Optional<Identifier> technique,
        String vfxType,
        String colorFolder,
        Vec3 radius,
        double damage,
        double knockback,
        int duration,
        float rotationZ,
        Vec3 movement,
        HitShape hitShape,
        BlockImpact blockImpact,
        Optional<HitEffect> hitEffect,
        List<Identifier> classifications
) {
    public static final String TYPE_SWORD = "sword_swing";
    public static final String TYPE_AXE = "axe_particle";
    public static final String TYPE_SPEAR = "spear_particle";
    public static final String TYPE_MACE = "mace_particle";
    public static final String TYPE_FIST = "fist_punch";

    public WeaponSwingSpec {
        path = path == null ? Optional.empty() : path;
        technique = technique == null ? Optional.empty() : technique;
        vfxType = vfxType == null || vfxType.isBlank() ? TYPE_SWORD : vfxType;
        colorFolder = colorFolder == null || colorFolder.isBlank() ? "blue" : colorFolder;
        radius = radius == null ? new Vec3(2.0D, 2.0D, 2.0D) : new Vec3(
                Math.max(0.05D, radius.x),
                Math.max(0.05D, radius.y),
                Math.max(0.05D, radius.z)
        );
        damage = Double.isFinite(damage) ? Math.max(0.0D, damage) : 0.0D;
        knockback = Double.isFinite(knockback) ? Math.max(0.0D, knockback) : 0.0D;
        duration = Math.clamp(duration, 1, 1200);
        movement = movement == null ? Vec3.ZERO : movement;
        hitShape = hitShape == null ? HitShape.AUTO : hitShape;
        blockImpact = blockImpact == null ? BlockImpact.NONE : blockImpact;
        hitEffect = hitEffect == null ? Optional.empty() : hitEffect;
        classifications = classifications == null ? List.of() : List.copyOf(classifications);
    }

    public boolean usesRayHitDetection() {
        return hitShape == HitShape.RAY
                || hitShape == HitShape.AUTO && TYPE_SPEAR.equals(vfxType);
    }

    public enum HitShape implements StringRepresentable {
        AUTO("auto"),
        AREA("area"),
        RAY("ray");

        public static final Codec<HitShape> CODEC = StringRepresentable.fromEnum(HitShape::values);
        private final String name;

        HitShape(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    /** Behaviour when a moving projection reaches a solid block. */
    public record BlockImpact(
            Mode mode,
            double radius,
            int depth,
            int maximumBlocks,
            double maximumHardness,
            Optional<Identifier> allowedTag,
            Optional<Identifier> blockedTag,
            boolean consumeProjection
    ) {
        public static final BlockImpact NONE = new BlockImpact(
                Mode.IGNORE,
                0.0D,
                1,
                0,
                0.0D,
                Optional.empty(),
                Optional.empty(),
                false
        );

        public static final Codec<BlockImpact> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Mode.CODEC.optionalFieldOf("mode", Mode.IGNORE).forGetter(BlockImpact::mode),
                Codec.DOUBLE.optionalFieldOf("radius", 1.0D).forGetter(BlockImpact::radius),
                Codec.intRange(1, 16).optionalFieldOf("depth", 1).forGetter(BlockImpact::depth),
                Codec.intRange(0, 256).optionalFieldOf("max_blocks", 0).forGetter(BlockImpact::maximumBlocks),
                Codec.DOUBLE.optionalFieldOf("max_hardness", 0.0D).forGetter(BlockImpact::maximumHardness),
                Identifier.CODEC.optionalFieldOf("allowed_tag").forGetter(BlockImpact::allowedTag),
                Identifier.CODEC.optionalFieldOf("blocked_tag").forGetter(BlockImpact::blockedTag),
                Codec.BOOL.optionalFieldOf("consume_projection", true).forGetter(BlockImpact::consumeProjection)
        ).apply(instance, BlockImpact::new));

        public BlockImpact {
            mode = mode == null ? Mode.IGNORE : mode;
            radius = Double.isFinite(radius) ? Math.clamp(radius, 0.0D, 16.0D) : 0.0D;
            depth = Math.clamp(depth, 1, 16);
            maximumBlocks = Math.clamp(maximumBlocks, 0, 256);
            maximumHardness = Double.isFinite(maximumHardness)
                    ? Math.max(0.0D, maximumHardness)
                    : 0.0D;
            allowedTag = allowedTag == null ? Optional.empty() : allowedTag;
            blockedTag = blockedTag == null ? Optional.empty() : blockedTag;
        }

        public enum Mode implements StringRepresentable {
            IGNORE("ignore"),
            STOP("stop"),
            BREAK("break");

            public static final Codec<Mode> CODEC = StringRepresentable.fromEnum(Mode::values);
            private final String name;

            Mode(String name) {
                this.name = name;
            }

            @Override
            public String getSerializedName() {
                return name;
            }
        }
    }

    /** Resolved Ascension skill effect applied after a successful hit. */
    public record HitEffect(Identifier definition, int duration, double potency) {
        public HitEffect {
            duration = Math.clamp(duration, 1, 72000);
            potency = Double.isFinite(potency) ? Math.max(0.0D, potency) : 0.0D;
        }
    }
}
