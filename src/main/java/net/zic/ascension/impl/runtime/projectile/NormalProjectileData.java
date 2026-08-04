package net.zic.ascension.impl.runtime.projectile;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.zic.ascension.api.ascension.datapack.CodecHelpers;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class NormalProjectileData {
    public static final Codec<NormalProjectileData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.listOf().optionalFieldOf("profiles", List.of()).forGetter(NormalProjectileData::profiles),
            CodecHelpers.VEC3.optionalFieldOf("launch_position", Vec3.ZERO).forGetter(NormalProjectileData::launchPosition),
            Codec.DOUBLE.optionalFieldOf("initial_speed", 0.0D).forGetter(NormalProjectileData::initialSpeed),
            Codec.INT.optionalFieldOf("age", 0).forGetter(NormalProjectileData::age),
            Codec.BOOL.optionalFieldOf("initialized", false).forGetter(NormalProjectileData::initialized),
            Codec.STRING.xmap(UUID::fromString, UUID::toString).optionalFieldOf("steering_target")
                    .forGetter(value -> Optional.ofNullable(value.steeringTarget))
    ).apply(instance, (profiles, launchPosition, initialSpeed, age, initialized, steeringTarget) ->
            new NormalProjectileData(profiles, launchPosition, initialSpeed, age, initialized, steeringTarget.orElse(null))));

    private List<Identifier> profiles;
    private Vec3 launchPosition;
    private double initialSpeed;
    private int age;
    private boolean initialized;
    private UUID steeringTarget;

    public NormalProjectileData() {
        this(List.of(), Vec3.ZERO, 0.0D, 0, false, null);
    }

    private NormalProjectileData(
            List<Identifier> profiles,
            Vec3 launchPosition,
            double initialSpeed,
            int age,
            boolean initialized,
            UUID steeringTarget
    ) {
        this.profiles = profiles == null ? List.of() : List.copyOf(profiles);
        this.launchPosition = launchPosition == null ? Vec3.ZERO : launchPosition;
        this.initialSpeed = Math.max(0.0D, initialSpeed);
        this.age = Math.max(0, age);
        this.initialized = initialized;
        this.steeringTarget = steeringTarget;
    }

    public void initialize(List<Identifier> profiles, Vec3 launchPosition, double initialSpeed) {
        this.profiles = profiles == null ? List.of() : List.copyOf(profiles);
        this.launchPosition = launchPosition == null ? Vec3.ZERO : launchPosition;
        this.initialSpeed = Math.max(0.0D, initialSpeed);
        this.age = 0;
        this.initialized = true;
        this.steeringTarget = null;
    }

    public boolean initialized() {
        return initialized;
    }

    public boolean active() {
        return !profiles.isEmpty();
    }

    public List<Identifier> profiles() {
        return profiles;
    }

    public Vec3 launchPosition() {
        return launchPosition;
    }

    public double initialSpeed() {
        return initialSpeed;
    }

    public int age() {
        return age;
    }

    public void tick() {
        age++;
    }

    public UUID steeringTarget() {
        return steeringTarget;
    }

    public void setSteeringTarget(UUID steeringTarget) {
        this.steeringTarget = steeringTarget;
    }
}
