package net.zic.ascension.skill_casting;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.zic.ascension.api.ascension.core.skill.castable.CastingSkillInstance;
import net.zic.ascension.api.ascension.event.skill.SkillEvent;
import net.zic.ascension.AscensionCraft;
import net.zic.ascension.common.data_attachements.AscensionAttachments;
import net.zic.zenithlib.input.action.ActionEvent;

/**
 * handles the cast input for skill cast handler
 *
 * will call castSelectedSkill on both the client and the server
 *
 * does not handle action end, since the skill and cast instance handle that
 */
@EventBusSubscriber(modid = AscensionCraft.MOD_ID)
public final class AscensionSkillListener {

    public static final Identifier skillCast = CastingSkillInstance.CAST_INPUT;

    private AscensionSkillListener() {
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Pre event) {
        if (event.getEntity() instanceof Player player) {
            //ticks the player's caster as well
            player.getData(AscensionAttachments.ASCENSION_SKILL_CAST_HANDLER).tick();
            return;
        }
        if (event.getEntity() instanceof LivingEntity entity) {
            SkillCaster caster = SkillCaster.getExisting(entity);
            if (caster != null) caster.tick();
        }
    }

    @SubscribeEvent
    public static void onActionStart(ActionEvent.Start event) {
        //other entities start their casts through their SkillCaster, the action only keeps the cast held
        if (!event.getAction().equals(skillCast) || !(event.getEntity() instanceof Player player)) {return;}

        SkillCastHandler handler = player.getData(AscensionAttachments.ASCENSION_SKILL_CAST_HANDLER);
        handler.castSelectedSkill();
    }

    @SubscribeEvent
    public static void onActionEnd(ActionEvent.End event) {
        if (!event.getAction().equals(skillCast) || !(event.getEntity() instanceof Player player)) {return;}

        SkillCastHandler handler = player.getData(AscensionAttachments.ASCENSION_SKILL_CAST_HANDLER);
        handler.releaseCastInput();
    }

    //──Lifecycle────────────────────────────────────────────────────────
    //a cast never resolves once the caster leaves, so cooldowns and costs can not be skipped

    @SubscribeEvent
    public static void onLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity().level().isClientSide()) {return;}
        invalidate(event.getEntity());
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide()) {return;}
        invalidate(event.getEntity());
    }

    //casts are not saved, so an entity unloading mid-cast ends it. players are covered by logout and death
    @SubscribeEvent
    public static void onLeaveLevel(EntityLeaveLevelEvent event) {
        if (event.getLevel().isClientSide() || event.getEntity() instanceof Player || !(event.getEntity() instanceof LivingEntity entity)) {return;}
        invalidate(entity);
    }

    private static void invalidate(LivingEntity entity) {
        SkillCaster caster = SkillCaster.getExisting(entity);
        if (caster != null) caster.invalidateCast();
    }

    //the hot bar is saved separately from the origin, so drop anything the origin lost while offline
    @SubscribeEvent
    public static void onLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity().level().isClientSide()) {return;}
        event.getEntity().getData(AscensionAttachments.ASCENSION_SKILL_CAST_HANDLER).removeUnownedSkills();
    }

    @SubscribeEvent
    public static void onSkillRemoved(SkillEvent.Removed.Post event) {
        for (LivingEntity entity : event.getSource().getAttachedEntities()) {
            if (entity.level().isClientSide()) {continue;}
            if (entity instanceof Player player) {
                player.getData(AscensionAttachments.ASCENSION_SKILL_CAST_HANDLER).removeSkill(event.getSkill());
                continue;
            }
            SkillCaster caster = SkillCaster.getExisting(entity);
            if (caster != null && event.getSkill().equals(caster.getCastingSkill())) {
                caster.invalidateCast();
            }
        }
    }
}
