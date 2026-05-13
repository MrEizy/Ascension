package net.thejadeproject.ascension.refactor_packages.events.skills.debuff;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.data_attachments.ModAttachments;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
import net.thejadeproject.ascension.refactor_packages.skills.custom.ModSkills;
import net.thejadeproject.ascension.refactor_packages.skills.custom.passive.debuff.skill_data.DebuffSkillHelper;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public class DebuffSkillEvents {

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer target)) return;
        if (!target.hasData(ModAttachments.ENTITY_DATA)) return;

        IEntityData targetData = target.getData(ModAttachments.ENTITY_DATA);

        if (targetData.hasSkill(ModSkills.CRACKED_MERIDIANS.getId())) {
            event.setAmount(event.getAmount() * 1.50F);
        }

        DebuffSkillHelper.removeIfExpired(target, targetData, ModSkills.CRACKED_MERIDIANS.getId());
        DebuffSkillHelper.removeIfExpired(target, targetData, ModSkills.BLINDED_SENSES.getId());


    }


}