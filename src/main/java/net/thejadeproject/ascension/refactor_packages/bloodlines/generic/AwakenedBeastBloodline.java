package net.thejadeproject.ascension.refactor_packages.bloodlines.generic;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;

/**
 * Tier 2 Beast bloodline. Extends GenericEvolvableBloodline directly
 * to show how you can set a custom default and override milestone behaviour
 * without being tied to the PurityBloodline concrete class.
 *
 * Kill weights are set in ModBloodlines via .setKillWeights(...).
 */
public class AwakenedBeastBloodline extends GenericEvolvableBloodline {

    public AwakenedBeastBloodline(Component title, ResourceLocation evolvesInto) {
        super(title, evolvesInto);
    }

    /**
     * Halved default gain compared to the base Beast bloodline —
     * harder to evolve at this tier.
     * This is overridden further if setKillWeights is called in ModBloodlines.
     */
    @Override
    protected double getDefaultPurityPerKill() {
        return 0.005;
    }

    @Override
    protected void onPurityChanged(ServerPlayer player, IEntityData entityData, PurityBloodlineData data) {
        super.onPurityChanged(player, entityData, data);
        // Add tier-specific effects here — sounds, particles, auras, etc.
    }

    @Override
    protected void onEvolved(ServerPlayer player, IEntityData entityData, ResourceLocation evolvedInto) {
        // Add post-evolution effects here
    }
}
