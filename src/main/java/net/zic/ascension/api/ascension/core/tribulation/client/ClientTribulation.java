package net.zic.ascension.api.ascension.core.tribulation.client;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.api.ascension.core.tribulation.Tribulation;
import net.zic.ascension.api.ascension.core.tribulation.TribulationData;
import net.zic.ascension.api.ascension.core.tribulation.TribulationSyncHelper;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

/**
 * client side view of a tribulation the player is tracking
 */
public final class ClientTribulation<T extends TribulationData> {
    private final UUID id;
    private final Identifier tribulationId;
    private final Tribulation<T> tribulation;
    private final ResourceKey<Level> dimension;
    private Vec3 position;
    private Vec3 previousPosition;
    private T data;
    private long age;

    private ClientTribulation(UUID id, Identifier tribulationId, Tribulation<T> tribulation, ResourceKey<Level> dimension, Vec3 position, T data) {
        this.id = id;
        this.tribulationId = tribulationId;
        this.tribulation = tribulation;
        this.dimension = dimension;
        this.position = position;
        this.previousPosition = position;
        this.data = data;
    }

    //returns null if the data could not be decoded
    static <T extends TribulationData> @Nullable ClientTribulation<T> create(UUID id, Identifier tribulationId, Tribulation<T> tribulation, ResourceKey<Level> dimension, Vec3 position, ByteBuf data, RegistryAccess access) {
        try {
            return new ClientTribulation<>(id, tribulationId, tribulation, dimension, position, TribulationSyncHelper.decode(tribulation, data, access));
        } catch (Exception exception) {
            AscensionCraft.LOGGER.debug("Failed to decode tribulation data for {}", tribulationId, exception);
            return null;
        }
    }

    public UUID getId() {return id;}
    public Identifier getTribulationId() {return tribulationId;}
    public Tribulation<T> getTribulation() {return tribulation;}
    public ResourceKey<Level> getDimension() {return dimension;}
    public Vec3 getPosition() {return position;}
    public Vec3 getPreviousPosition() {return previousPosition;}
    public long getAge() {return age;}

    //the same data as the server side TribulationInstance, updated whenever the server marks it dirty
    public T getData() {
        return data;
    }

    public Vec3 getPosition(float partialTick) {
        return previousPosition.lerp(position, partialTick);
    }

    //only the parts that changed are sent, keeps the old data if the new data fails to decode
    void update(@Nullable Vec3 position, @Nullable ByteBuf data, RegistryAccess access) {
        if (position != null) this.position = position;
        if (data == null) return;
        try {
            this.data = TribulationSyncHelper.decode(tribulation, data, access);
        } catch (Exception exception) {
            AscensionCraft.LOGGER.debug("Failed to decode tribulation data for {}", tribulationId, exception);
        }
    }

    void tick() {
        previousPosition = position;
        age++;
        tribulation.getRenderer().clientTick(this);
    }

    void render(RenderLevelStageEvent event) {
        tribulation.getRenderer().render(this, event);
    }

    void onRemoved() {
        tribulation.getRenderer().onRemoved(this);
    }
}
