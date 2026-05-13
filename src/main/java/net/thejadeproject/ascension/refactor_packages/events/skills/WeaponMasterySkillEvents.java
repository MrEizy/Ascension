package net.thejadeproject.ascension.refactor_packages.events.skills;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.thejadeproject.ascension.AscensionCraft;
import net.thejadeproject.ascension.data_attachments.ModAttachments;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
import net.thejadeproject.ascension.refactor_packages.registries.AscensionRegistries;
import net.thejadeproject.ascension.refactor_packages.skills.ISkill;
import net.thejadeproject.ascension.refactor_packages.skills.custom.passive.weapon.GenericWeaponMasterySkill;

@EventBusSubscriber(modid = AscensionCraft.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class WeaponMasterySkillEvents {

    @SubscribeEvent
    public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        if (!player.hasData(ModAttachments.ENTITY_DATA)) return;

        IEntityData entityData = player.getData(ModAttachments.ENTITY_DATA);

        Registry<ISkill> skillRegistry = player.registryAccess()
                .registryOrThrow(AscensionRegistries.Skills.SKILL_REGISTRY_KEY);

        double bestMultiplier = 1.0D;

        for (ResourceLocation skillId : entityData.getAllSkills()) {
            ISkill rawSkill = skillRegistry.get(skillId);
            if (!(rawSkill instanceof GenericWeaponMasterySkill masterySkill)) continue;

            if (!masterySkill.matchesDamage(player, event.getSource())) continue;

            bestMultiplier = Math.max(
                    bestMultiplier,
                    masterySkill.getDamageMultiplier(entityData)
            );
        }

        if (bestMultiplier <= 1.0D) return;

        event.setAmount((float) (event.getAmount() * bestMultiplier));
    }
}