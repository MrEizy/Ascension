package net.zic.ascension.client.visual;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.zic.ascension.api.client.visual.RuntimeVisualFlags;
import net.zic.ascension.api.client.visual.RuntimeVisualState;

public final class RuntimeVisualTransforms {
    private RuntimeVisualTransforms() {
    }

    public static Vec3 position(RuntimeVisualState state) {
        if (!RuntimeVisualFlags.has(state.flags(), RuntimeVisualFlags.OWNER_RELATIVE)
                || state.ownerId() == null
                || Minecraft.getInstance().level == null) {
            return state.position();
        }

        Player owner = Minecraft.getInstance().level.getPlayerByUUID(state.ownerId());
        if (owner == null) {
            return state.position();
        }

        Vec3 offset = state.velocity();
        if (RuntimeVisualFlags.has(state.flags(), RuntimeVisualFlags.ROTATE_WITH_OWNER)) {
            double radians = Math.toRadians(-owner.getYRot());
            double cos = Math.cos(radians);
            double sin = Math.sin(radians);
            offset = new Vec3(
                    offset.x * cos - offset.z * sin,
                    offset.y,
                    offset.x * sin + offset.z * cos
            );
        }
        return owner.position().add(offset);
    }
}
