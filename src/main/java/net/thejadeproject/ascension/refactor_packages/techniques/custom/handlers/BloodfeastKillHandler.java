package net.thejadeproject.ascension.refactor_packages.techniques.custom.handlers;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.thejadeproject.ascension.data_attachments.ModAttachments;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
import net.thejadeproject.ascension.refactor_packages.paths.ModPaths;
import net.thejadeproject.ascension.refactor_packages.paths.PathData;
import net.thejadeproject.ascension.refactor_packages.registries.AscensionRegistries;
import net.thejadeproject.ascension.refactor_packages.techniques.ITechniqueData;
import net.thejadeproject.ascension.refactor_packages.techniques.custom.essence.BloodfeastSoulRefiningTechnique;
import net.thejadeproject.ascension.refactor_packages.techniques.custom.technique_data.BloodfeastTechniqueData;


public class BloodfeastKillHandler {

    private static final ResourceLocation PLAYER_ID =
            ResourceLocation.withDefaultNamespace("player");

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        LivingEntity victim = event.getEntity();

        if (!(victim.getKillCredit() instanceof Player killer)) return;

        ResourceLocation entityTypeId = victim instanceof Player
                ? PLAYER_ID
                : BuiltInRegistries.ENTITY_TYPE.getKey(victim.getType());

        if (entityTypeId == null) return;

        if (!killer.hasData(ModAttachments.ENTITY_DATA)) return;

        IEntityData entityData = killer.getData(ModAttachments.ENTITY_DATA);
        PathData pathData = entityData.getPathData(ModPaths.ESSENCE.getId());

        if (pathData == null) return;
        if (pathData.getLastUsedTechnique() == null) return;

        Object rawTechnique = AscensionRegistries.Techniques.TECHNIQUES_REGISTRY.get(
                pathData.getLastUsedTechnique()
        );

        if (!(rawTechnique instanceof BloodfeastSoulRefiningTechnique)) return;

        ITechniqueData rawData = pathData.getTechniqueData(pathData.getLastUsedTechnique());

        if (!(rawData instanceof BloodfeastTechniqueData data)) return;

        data.recordKill(entityTypeId);

    }
}