package net.zic.ascension.common.starter;

import net.minecraft.server.level.ServerPlayer;
import net.zic.ascension.api.ascension.capabilities.CoreCapabilities;
import net.zic.ascension.api.ascension.core.CoreAttachments;
import net.zic.ascension.api.ascension.core.alchemy.medicinal.MedicinalImpurities;
import net.zic.ascension.api.ascension.core.effect.SkillEffectDefinition;
import net.zic.ascension.api.ascension.core.qi_coating.QiCoatingSelection;
import net.zic.ascension.api.ascension.core.resource.DatapackResourceData;
import net.zic.ascension.api.ascension.core.tribulation.TribulationManager;
import net.zic.ascension.common.data_attachements.AscensionAttachments;
import net.zic.ascension.impl.core.control.StaggerService;
import net.zic.ascension.impl.core.effect.FrozenStateService;
import net.zic.ascension.impl.core.effect.SkillEffectManager;
import net.zic.ascension.impl.core.innerworld.InnerWorldSessions;
import net.zic.ascension.impl.resource.stamina.StaminaService;
import net.zic.ascension.skill_casting.SkillCastHandler;
import net.zic.ascension.skill_casting.SkillCaster;

public final class PlayerProgressionReset {
    private PlayerProgressionReset() {
    }

    public static boolean reset(ServerPlayer player) {
        if (!player.isAlive() || InnerWorldSessions.isInside(player)) return false;

        TribulationManager tribulations = TribulationManager.getInstance();
        if (tribulations != null) {
            for (var tribulation : tribulations.getForEntity(player.getUUID())) {
                tribulations.discard(tribulation.getId());
            }
        }
        SkillCaster caster = SkillCaster.getExisting(player);
        if (caster != null) caster.invalidateCast();
        SkillEffectManager.clear(player, SkillEffectDefinition.RemovalReason.CLEARED);
        FrozenStateService.clear(player);
        StaggerService.clear(player);
        player.getData(AscensionAttachments.MOVEMENT_ANCHORS).clear();

        player.getData(AscensionAttachments.SIMPLE_ENTITY_DATA).resetProgression();
        player.setData(AscensionAttachments.ASCENSION_SKILL_CAST_HANDLER, new SkillCastHandler(player));
        player.syncData(AscensionAttachments.ASCENSION_SKILL_CAST_HANDLER);
        player.setData(CoreAttachments.QI_COATING_SELECTION, new QiCoatingSelection());
        player.syncData(CoreAttachments.QI_COATING_SELECTION);
        player.setData(CoreAttachments.DATAPACK_RESOURCES, new DatapackResourceData());
        player.setData(AscensionAttachments.MEDICINAL_IMPURITIES, new MedicinalImpurities.State(0.0D, 0L));
        player.syncData(AscensionAttachments.MEDICINAL_IMPURITIES);
        var qi = player.getCapability(CoreCapabilities.ASCENSION_ENTITY_QI_PROVIDER);
        if (qi != null) qi.setQi(qi.getMaxQi());
        StaminaService.setStamina(player, StaminaService.getMaximumStamina(player));
        StaminaService.setRemainingRegenerationDelay(player, 0);
        StarterSelectionManager.openIfIncomplete(player);
        return true;
    }
}
