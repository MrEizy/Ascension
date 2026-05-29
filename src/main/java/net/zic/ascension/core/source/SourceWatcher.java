package net.zic.ascension.core.source;

import net.minecraft.world.entity.LivingEntity;
import net.zic.ascension.api.core.source.OriginSource;

import java.util.Objects;
import java.util.UUID;

public class SourceWatcher {
    private final UUID uuid;
    private LivingEntity entity;
    public SourceWatcher(LivingEntity entity){
        this.uuid = entity.getUUID();
        this.entity = entity;
    }

    public boolean isLoaded(){
        return entity != null;
    }
    public LivingEntity getEntity(){
        return entity;
    }
    public UUID getUuid(){
        return uuid;
    }
    public void setEntity(LivingEntity entity){
        this.entity = entity;
    }


    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof SourceWatcher that)) return false;
        return Objects.equals(uuid, that.uuid);
    }
}
