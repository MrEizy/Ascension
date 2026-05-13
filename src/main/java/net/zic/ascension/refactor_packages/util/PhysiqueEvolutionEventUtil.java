package net.zic.ascension.refactor_packages.util;

import net.minecraft.resources.ResourceLocation;
import net.zic.ascension.refactor_packages.entity_data.IEntityData;
import net.zic.ascension.refactor_packages.registries.AscensionRegistries;

public final class PhysiqueEvolutionEventUtil {

    private PhysiqueEvolutionEventUtil() {}

    public static boolean hasPhysique(IEntityData entityData, ResourceLocation physiqueId) {
        if (entityData == null) return false;
        if (physiqueId == null) return false;
        if (entityData.getPhysique() == null) return false;

        ResourceLocation currentPhysiqueId =
                AscensionRegistries.Physiques.PHSIQUES_REGISTRY.getKey(entityData.getPhysique());

        return physiqueId.equals(currentPhysiqueId);
    }
}
