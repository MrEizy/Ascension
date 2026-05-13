package net.zic.ascension.entity.custom.form;

import com.mojang.authlib.GameProfile;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

public class PlayerBodyEntity extends Mob {
    private GameProfile profile;
    public PlayerBodyEntity(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
    }
    public void setProfile(GameProfile profile) {
        this.profile = profile;
    }

    public GameProfile getProfile() {
        return profile;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 8d)
                .add(Attributes.MOVEMENT_SPEED, 0.0D)
                .add(Attributes.FOLLOW_RANGE,0.0);
    }
}
