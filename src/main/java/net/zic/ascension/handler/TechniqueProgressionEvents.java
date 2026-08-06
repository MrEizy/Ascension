package net.zic.ascension.handler;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.zic.ascension.api.ascension.core.CoreRegistries;
import net.zic.ascension.api.ascension.core.path.PathData;
import net.zic.ascension.api.ascension.core.source.AscensionOriginSourceHelper;
import net.zic.ascension.api.ascension.core.technique.Technique;
import net.zic.ascension.api.rpg_engine.source.OriginSource;
import net.zic.ascension.impl.core.technique.KillProgressionTechnique;

import java.util.List;

@EventBusSubscriber
public final class TechniqueProgressionEvents {
    private TechniqueProgressionEvents() {
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide() || victim instanceof ArmorStand) {
            return;
        }

        LivingEntity killer = victim.getKillCredit();
        if (killer == null && event.getSource().getEntity() instanceof LivingEntity sourceEntity) {
            killer = sourceEntity;
        }
        if (killer == null || killer == victim) {
            return;
        }

        OriginSource source = AscensionOriginSourceHelper.getEntitySource(killer);
        if (source == null || source.getRegistryAccess() == null) {
            return;
        }

        for (Identifier pathId : List.copyOf(AscensionOriginSourceHelper.getPaths(source))) {
            PathData pathData = AscensionOriginSourceHelper.getPathData(source, pathId);
            if (pathData == null || pathData.getCurrentTechnique() == null) {
                continue;
            }

            Technique technique = CoreRegistries.safeAccess(CoreRegistries.TECHNIQUE_REGISTRY, pathData.getCurrentTechnique(), source.getRegistryAccess());

            if (technique instanceof KillProgressionTechnique killProgressionTechnique) {
                killProgressionTechnique.handleKill(killer, victim, source, pathData);
            }
        }
    }
}
