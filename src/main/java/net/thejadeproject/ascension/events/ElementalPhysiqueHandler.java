package net.thejadeproject.ascension.events;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
import net.thejadeproject.ascension.refactor_packages.events.PhysiqueChangeEvent;
import net.thejadeproject.ascension.refactor_packages.forms.IEntityFormData;
import net.thejadeproject.ascension.refactor_packages.network.client_bound.entity_data.physique.SyncPhysique;
import net.thejadeproject.ascension.refactor_packages.paths.ModPaths;
import net.thejadeproject.ascension.refactor_packages.paths.PathBonusHandler;
import net.thejadeproject.ascension.refactor_packages.physiques.IPhysique;
import net.thejadeproject.ascension.refactor_packages.physiques.custom.ElementalBodyPhysique;
import net.thejadeproject.ascension.refactor_packages.physiques.custom.ElementalPhysiqueData;
import net.thejadeproject.ascension.refactor_packages.registries.AscensionRegistries;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class ElementalPhysiqueHandler {

    /**
     * Generative cycle: each element generates the next.
     * Wood → Fire → Earth → Metal → Water → Wood
     */
    public static boolean isGeneratedBy(ResourceLocation existing, ResourceLocation candidate) {
        if (existing.equals(ModPaths.WOOD.getId()))  return candidate.equals(ModPaths.FIRE.getId());
        if (existing.equals(ModPaths.FIRE.getId()))  return candidate.equals(ModPaths.EARTH.getId());
        if (existing.equals(ModPaths.EARTH.getId())) return candidate.equals(ModPaths.METAL.getId());
        if (existing.equals(ModPaths.METAL.getId())) return candidate.equals(ModPaths.WATER.getId());
        if (existing.equals(ModPaths.WATER.getId())) return candidate.equals(ModPaths.WOOD.getId());
        return false;
    }

    /** Returns true if the given element can be fused into the existing physique data. */
    public static boolean canMerge(ElementalPhysiqueData data, ResourceLocation newElement) {
        if (data.hasElement(newElement)) return false;
        return data.getActiveElements().stream().anyMatch(el -> isGeneratedBy(el, newElement));
    }

    @SubscribeEvent
    public static void onPhysiqueChangePre(PhysiqueChangeEvent.Pre event) {
        if (!(event.oldPhysiqueData instanceof ElementalPhysiqueData oldData)) return;

        IPhysique incoming = AscensionRegistries.Physiques.PHSIQUES_REGISTRY.get(event.getNewPhysique());
        if (!(incoming instanceof ElementalBodyPhysique newElemental)) return;

        if (!canMerge(oldData, newElemental.getElement())) return;

        event.setCanceled(true);

        IEntityData entityData = event.entityData;
        PathBonusHandler bonuses = entityData.getPathBonusHandler();

        // Remove current tier bonuses
        double oldBonus = ElementalBodyPhysique.bonusForTier(oldData.getActiveCount());
        bonuses.removePathBonus(ModPaths.BODY.getId(), oldBonus);
        for (ResourceLocation el : oldData.getActiveElements()) {
            bonuses.removePathBonus(el, oldBonus);
        }

        // Add the new element to the existing data in-place
        oldData.setFlag(newElemental.getElement(), true);

        // Apply upgraded tier bonuses
        double newBonus = ElementalBodyPhysique.bonusForTier(oldData.getActiveCount());
        bonuses.addPathBonus(ModPaths.BODY.getId(), newBonus);
        for (ResourceLocation el : oldData.getActiveElements()) {
            bonuses.addPathBonus(el, newBonus);
        }

        // Sync updated physique data to the client
        if (!(entityData.getAttachedEntity() instanceof ServerPlayer serverPlayer)) return;
        if (serverPlayer.connection == null) return;

        for (IEntityFormData formData : entityData.getFormData()) {
            if (formData.getPhysiqueKey() == null) continue;
            PacketDistributor.sendToPlayer(serverPlayer,
                    new SyncPhysique(formData.getEntityFormId(), formData.getPhysiqueKey(), oldData));
            break;
        }
    }
}
