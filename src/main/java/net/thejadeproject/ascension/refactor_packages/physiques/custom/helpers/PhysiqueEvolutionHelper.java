package net.thejadeproject.ascension.refactor_packages.physiques.custom.helpers;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.thejadeproject.ascension.data_attachments.ModAttachments;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
import net.thejadeproject.ascension.refactor_packages.physiques.custom.EvolvingPhysique;


public final class PhysiqueEvolutionHelper {

    private PhysiqueEvolutionHelper() {}

    public static boolean tryEvolveInto(ServerPlayer player, ResourceLocation evolvesInto) {
        if (player == null) return false;
        if (!player.hasData(ModAttachments.ENTITY_DATA)) return false;

        return tryEvolveInto(player, player.getData(ModAttachments.ENTITY_DATA), evolvesInto);
    }

    public static boolean tryEvolveInto(ServerPlayer player, IEntityData entityData, ResourceLocation evolvesInto) {
        if (player == null) return false;
        if (entityData == null) return false;
        if (evolvesInto == null) return false;

        if (!(entityData.getPhysique() instanceof EvolvingPhysique evolvingPhysique)) {
            return false;
        }

        return evolvingPhysique.tryEvolveInto(player, entityData, evolvesInto);
    }
}