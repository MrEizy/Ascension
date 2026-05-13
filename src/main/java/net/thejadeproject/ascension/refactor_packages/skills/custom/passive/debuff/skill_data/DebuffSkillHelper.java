package net.thejadeproject.ascension.refactor_packages.skills.custom.passive.debuff.skill_data;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.thejadeproject.ascension.data_attachments.ModAttachments;
import net.thejadeproject.ascension.refactor_packages.entity_data.IEntityData;
import net.thejadeproject.ascension.refactor_packages.entity_data_source.custom.TemporarySkill;
import net.thejadeproject.ascension.refactor_packages.entity_data_source.custom.TemporarySkillContainer;
import net.thejadeproject.ascension.refactor_packages.forms.forms.ModForms;
import net.thejadeproject.ascension.refactor_packages.skills.IPersistentSkillData;

public final class DebuffSkillHelper {

    private DebuffSkillHelper() {}
    public static void giveTempDebuffSource(ServerPlayer serverPlayer,ResourceLocation debuffId,ResourceLocation skillId,int ticks,ResourceLocation form){
        TemporarySkillContainer temporarySkillContainer = TemporarySkill.create(skillId,ticks,debuffId);
        long gameTime = serverPlayer.serverLevel().getGameTime();
        long expiresAt = gameTime + ticks;
        DebuffSkillData debuffSkillData = DebuffSkillData.temporary(expiresAt, gameTime);

        IEntityData entityData = serverPlayer.getData(ModAttachments.ENTITY_DATA);

        if (!entityData.hasSkill(skillId)) {
            entityData.giveSkill(skillId, debuffSkillData, form);

        }else{
            IPersistentSkillData existingRawData = entityData.getSkillData(skillId);

            DebuffSkillData finalData = debuffSkillData;

            if (existingRawData instanceof DebuffSkillData existingData) {
                finalData = existingData.mergeWith(debuffSkillData);
            }


            entityData.removeSkill(skillId, form);
            entityData.giveSkill(skillId, finalData, form);
        }
        if(entityData.getSourceContainer(debuffId) == null) {
            entityData.addEntityDataSource(temporarySkillContainer);
        }else if(entityData.getSourceContainer(debuffId) instanceof  TemporarySkillContainer skillContainer){
            skillContainer.setTicksLeft(skillContainer.getTicksLeft()+ticks);
        }




    }
    public static void giveTemporaryDebuff(ServerPlayer player, ResourceLocation skillId, int durationTicks) {
        giveTemporaryDebuff(player, skillId, durationTicks, ModForms.MORTAL_VESSEL.getId());

    }

    public static void giveTemporaryDebuff(ServerPlayer player, ResourceLocation skillId, int durationTicks, ResourceLocation formId) {
        if (!player.hasData(ModAttachments.ENTITY_DATA)) return;

        long gameTime = player.serverLevel().getGameTime();
        long expiresAt = gameTime + durationTicks;

        giveDebuff(
                player,
                skillId,
                DebuffSkillData.temporary(expiresAt, gameTime),
                formId
        );
    }

    public static void givePermanentDebuff(ServerPlayer player, ResourceLocation skillId) {
        givePermanentDebuff(player, skillId, ModForms.MORTAL_VESSEL.getId());
    }

    public static void givePermanentDebuff(ServerPlayer player, ResourceLocation skillId, ResourceLocation formId) {
        long gameTime = player.serverLevel().getGameTime();

        giveDebuff(
                player,
                skillId,
                DebuffSkillData.permanent(gameTime),
                formId
        );
    }

    private static void giveDebuff(ServerPlayer player, ResourceLocation skillId, DebuffSkillData newData, ResourceLocation formId) {
        if (!player.hasData(ModAttachments.ENTITY_DATA)) return;

        IEntityData entityData = player.getData(ModAttachments.ENTITY_DATA);

        if (!entityData.hasSkill(skillId)) {
            entityData.giveSkill(skillId, newData, formId);
            return;
        }

        IPersistentSkillData existingRawData = entityData.getSkillData(skillId);

        DebuffSkillData finalData = newData;

        if (existingRawData instanceof DebuffSkillData existingData) {
            finalData = existingData.mergeWith(newData);
        }


        entityData.removeSkill(skillId, formId);
        entityData.giveSkill(skillId, finalData, formId);


    }

    public static boolean removeIfExpired(ServerPlayer player, IEntityData entityData, ResourceLocation skillId) {
        return removeIfExpired(player, entityData, skillId, ModForms.MORTAL_VESSEL.getId());
    }

    public static boolean removeIfExpired(ServerPlayer player, IEntityData entityData, ResourceLocation skillId, ResourceLocation formId) {
        if (!entityData.hasSkill(skillId)) return false;

        IPersistentSkillData rawData = entityData.getSkillData(skillId);

        if (!(rawData instanceof DebuffSkillData debuffData)) return false;

        if (!debuffData.isExpired(player.serverLevel().getGameTime())) return false;

        entityData.removeSkill(skillId, formId);
        return true;
    }

    public static DebuffSkillData getDebuffData(IEntityData entityData, ResourceLocation skillId) {
        IPersistentSkillData rawData = entityData.getSkillData(skillId);

        if (rawData instanceof DebuffSkillData debuffData) {
            return debuffData;
        }

        return null;
    }
}