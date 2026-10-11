package net.zic.ascension.client.tribulation;

import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.zic.ascension.api.ascension.core.tribulation.Tribulation;
import net.zic.ascension.api.ascension.core.tribulation.TribulationData;

import java.util.UUID;

/**
 * client side view of a tribulation the player is tracking
 */
public final class ClientTribulation {
    private final UUID id;
    private final Identifier tribulationId;
    private final Tribulation tribulation;
    private final ResourceKey<Level> dimension;
    private Vec3 position;
    private Vec3 previousPosition;
    private TribulationData data;
    private long age;

    ClientTribulation(UUID id, Identifier tribulationId, Tribulation tribulation, ResourceKey<Level> dimension, Vec3 position, TribulationData data) {
        this.id = id;
        this.tribulationId = tribulationId;
        this.tribulation = tribulation;
        this.dimension = dimension;
        this.position = position;
        this.previousPosition = position;
        this.data = data;
    }

    public UUID getId() {return id;}
    public Identifier getTribulationId() {return tribulationId;}
    public Tribulation getTribulation() {return tribulation;}
    public ResourceKey<Level> getDimension() {return dimension;}
    public Vec3 getPosition() {return position;}
    public Vec3 getPreviousPosition() {return previousPosition;}
    public long getAge() {return age;}

    //the same data as the server side TribulationInstance, updated whenever the server marks it dirty
    public TribulationData getData() {
        return data;
    }

    public Vec3 getPosition(float partialTick) {
        return previousPosition.lerp(position, partialTick);
    }

    void update(Vec3 position, TribulationData data) {
        this.position = position;
        this.data = data;
    }

    void tick() {
        previousPosition = position;
        age++;
    }
}
